package katana.rt;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * The java lane's loader and unloader of a KSDS (LANE-16): records of a fixed length in key order, in or
 * out of an `IdxFile` dataset (LANE-15), as the cobc lane's generated programs load and unload GnuCOBOL's
 * own indexed file (R-025, logical unload). `oracle/` may not import `rt/`, so the lane runs it as a process:
 *
 * `Ksds load|unload LRECL PRIME ALTERNATES SEQUENTIAL INDEXED`: PRIME is `length,offset` as IDCAMS writes a
 * key; ALTERNATES is `-` or each alternate key's `length,offset,u|d` (unique or duplicates) joined by `;`.
 * It exits 0, or 12 with what was wrong on stderr.
 */
public final class Ksds {
    private Ksds() {}

    public static void main(String[] args) {
        try {
            run(args[0], Integer.parseInt(args[1]), args[2], args[3], Path.of(args[4]), Path.of(args[5]));
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
            System.exit(12);
        }
    }

    /** One load or unload: the record area and its keys as fields, the `IdxFile` over the indexed file, the records in or out in key order. */
    // Decision: R-025
    static void run(String mode, int lrecl, String prime, String alternates, Path sequential, Path indexed) {
        Record area = Record.of(lrecl, Codepage.COBC_ASCII);
        Field record = new Field(area, 0, Attr.text(lrecl, false));
        List<Field> keys = new ArrayList<>(List.of(key(area, prime)));
        List<Boolean> duplicates = new ArrayList<>(List.of(false));
        if (!alternates.equals("-")) {
            for (String a : alternates.split(";")) {
                keys.add(key(area, a));
                duplicates.add(a.endsWith(",d"));
            }
        }
        boolean[] dups = new boolean[duplicates.size()];
        for (int k = 0; k < dups.length; k++) dups[k] = duplicates.get(k);
        IdxFile file = new IdxFile(indexed.toString(), "KSDS", true, false, KeyedFile.Access.RANDOM).keys(record, lrecl, keys.toArray(Field[]::new), dups);
        try {
            if (mode.equals("load")) load(file, area, lrecl, Files.readAllBytes(sequential));
            else Files.write(sequential, unload(file, area));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Field key(Record area, String spec) {
        String[] k = spec.split(",");
        return new Field(area, Integer.parseInt(k[1]), Attr.text(Integer.parseInt(k[0]), false));
    }

    private static void load(IdxFile file, Record area, int lrecl, byte[] records) {
        if (records.length % lrecl != 0) throw new IllegalArgumentException(records.length + " bytes are not records of " + lrecl);
        file.open(SeqFile.Mode.OUTPUT);
        for (int at = 0; at < records.length; at += lrecl) {
            System.arraycopy(records, at, area.bytes(), 0, lrecl);
            file.write(new Field(area, 0, Attr.text(lrecl, false)));
            if (!file.successful()) throw new IllegalArgumentException("record " + (at / lrecl + 1) + ": WRITE status " + file.status());
        }
        file.close(false, false, false);
    }

    private static byte[] unload(IdxFile file, Record area) {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        file.open(SeqFile.Mode.INPUT);
        if (!file.successful()) throw new IllegalArgumentException("OPEN status " + file.status());
        Field record = new Field(area, 0, Attr.text(area.bytes().length, false));
        for (file.readNext(record); file.successful(); file.readNext(record)) out.write(area.bytes(), 0, area.bytes().length);
        file.close(false, false, false);
        return out.toByteArray();
    }
}
