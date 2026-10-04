package katana.oracle.fixture;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A dataset's logical content (docs/FIXTURE.md, FIXTURE-06): its records, each lrecl bytes, in file
 * order, or in key order for a KSDS. A fixture's sha256= is over these bytes, so a source's line
 * endings and a KSDS's load order do not change the address, and a physical index file is never
 * hashed (F-02: BDB pages differ between identical runs).
 */
public final class Content {
    private Content() {}

    /** The records a source holds under its framing; keys is "length,offset" for a KSDS, else null. */
    public static byte[] records(byte[] source, String framing, int lrecl, String keys) {
        List<byte[]> records = framing.equals("lines") ? lines(source, lrecl) : fixed(source, lrecl);
        if (keys != null) {
            String[] k = keys.split(",");
            int length = Integer.parseInt(k[0]), offset = Integer.parseInt(k[1]);
            records.sort((a, b) -> Arrays.compareUnsigned(a, offset, offset + length, b, offset, offset + length));
            for (int i = 1; i < records.size(); i++) {
                if (Arrays.equals(records.get(i - 1), offset, offset + length, records.get(i), offset, offset + length)) {
                    throw new IllegalArgumentException("two records with key " + new String(records.get(i), offset, length));
                }
            }
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        records.forEach(out::writeBytes);
        return out.toByteArray();
    }

    /** "sha256:" and the hex SHA-256 of the records. */
    public static String sha256(byte[] records) {
        return Fixture.sha256(records);
    }

    /** One record a line: the newline and a carriage return before it dropped, padded with spaces to lrecl. */
    private static List<byte[]> lines(byte[] source, int lrecl) {
        List<byte[]> records = new ArrayList<>();
        int start = 0;
        for (int i = 0; i <= source.length; i++) {
            if (i < source.length && source[i] != '\n') continue;
            int end = i > start && source[i - 1] == '\r' ? i - 1 : i;
            if (i < source.length || end > start) records.add(padded(Arrays.copyOfRange(source, start, end), lrecl));
            start = i + 1;
        }
        return records;
    }

    private static byte[] padded(byte[] line, int lrecl) {
        if (line.length > lrecl) throw new IllegalArgumentException("a line of " + line.length + " bytes is longer than lrecl " + lrecl);
        byte[] record = Arrays.copyOf(line, lrecl);
        Arrays.fill(record, line.length, lrecl, (byte) ' ');
        return record;
    }

    private static List<byte[]> fixed(byte[] source, int lrecl) {
        if (source.length % lrecl != 0) throw new IllegalArgumentException(source.length + " bytes is not a whole number of " + lrecl + "-byte records");
        List<byte[]> records = new ArrayList<>();
        for (int at = 0; at < source.length; at += lrecl) records.add(Arrays.copyOfRange(source, at, at + lrecl));
        return records;
    }
}
