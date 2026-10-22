package katana.oracle.prove;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Random;
import katana.findings.Divergence;

/**
 * 8A.2's generators (research §49): for a divergence kind and a seed, a case whose COBOL program does one statement of
 * the kind's shape in a read-compute-write loop over twenty 20-byte records, about half drawn to cross the kind's
 * boundary. A port of it that keeps the kind's rule is the SAME; one that breaks it differs (docs/DIVERGENCES.md).
 */
public final class Generators {
    /** A case: its program's name, its COBOL, its copybook's name and text, the field that receives, and its records. */
    public record Case(Divergence.Kind kind, String program, String cobol, String copybook, String copybookText, String receiver, byte[] records) {}

    public static final int RECORDS = 20, LRECL = 20;

    private Generators() {}

    /** The case of a kind for a seed. */
    // Decision: R-072
    public static Case generate(Divergence.Kind kind, long seed) {
        Random r = new Random(seed);
        StringBuilder records = new StringBuilder();
        return switch (kind) {
            case DV_01 -> {
                for (int i = 0; i < RECORDS; i++) {
                    int a = 1 + r.nextInt(99999), b = i % 2 == 0 ? 100000 - a + r.nextInt(a) : r.nextInt(100000 - a);
                    records.append(id(i)).append(digits(a, 5)).append(digits(b, 5)).append(" ".repeat(6));
                }
                yield new Case(kind, "DVADD", program("DVADD", "DVREC01", "ADD OP-A TO OP-B"), "DVREC01",
                        copybook("05  OP-A            PIC 9(3)V99.", "05  OP-B            PIC 9(3)V99.", "05  FILLER          PIC X(6)."), "OP-B", bytes(records));
            }
            case DV_02 -> {
                String k = "0." + digits(1 + r.nextInt(9999), 4);
                for (int i = 0; i < RECORDS; i++) records.append(id(i)).append(digits(r.nextInt(10000000), 7)).append("0".repeat(7)).append("  ");
                yield new Case(kind, "DVMUL", program("DVMUL", "DVREC02", "COMPUTE RES = OP-A * " + k), "DVREC02",
                        copybook("05  OP-A            PIC 9(5)V99.", "05  RES             PIC 9(5)V99.", "05  FILLER          PIC X(2)."), "RES", bytes(records));
            }
            case DV_03 -> {
                for (int i = 0; i < RECORDS; i++)
                    records.append(id(i)).append(digits(r.nextInt(1000), 3)).append(1 + r.nextInt(9)).append(digits(r.nextInt(100), 2)).append("0".repeat(7)).append("   ");
                yield new Case(kind, "DVDIV", program("DVDIV", "DVREC03", "COMPUTE RES = OP-A / OP-B * OP-C"), "DVREC03",
                        copybook("05  OP-A            PIC 9(3).", "05  OP-B            PIC 9.", "05  OP-C            PIC 9(2).", "05  RES             PIC 9(5)V99.", "05  FILLER          PIC X(3)."), "RES", bytes(records));
            }
        };
    }

    /** The case's files under `dir`: `cbl/`, `cpy/`, `IN.txt` (a record a line) and `<PROGRAM>.fixture`, its profile line given. */
    public static Path write(Case c, Path dir, String profile) throws IOException {
        Files.createDirectories(dir.resolve("cbl"));
        Files.createDirectories(dir.resolve("cpy"));
        Files.writeString(dir.resolve("cbl").resolve(c.program() + ".cbl"), c.cobol());
        Files.writeString(dir.resolve("cpy").resolve(c.copybook() + ".cpy"), c.copybookText());
        StringBuilder lines = new StringBuilder();
        for (int i = 0; i < RECORDS; i++) lines.append(new String(c.records(), i * LRECL, LRECL, StandardCharsets.US_ASCII)).append('\n');
        Files.writeString(dir.resolve("IN.txt"), lines);
        String fixture = "# " + c.kind().code + "'s generated case (8A.2, research §49).\njob " + c.program() + "\n" + profile + "\nclock 2024-03-01 12:00:00.00 +00:00\ntz UTC\n\n"
                + "dataset KATANA.DV.IN org=PS lrecl=20 from=repo:IN.txt framing=lines sha256=" + sha256(c.records()) + "\n\n"
                + "step " + c.program() + " pgm=" + c.program() + "\ndd SYSOUT sysout\n"
                + "dd INFILE dsn=KATANA.DV.IN disp=SHR recfm=F lrecl=20 copybook=" + c.copybook() + " encoding=ascii\n"
                + "dd OUTFILE dsn=KATANA.DV.OUT disp=NEW,CATLG,DELETE recfm=F lrecl=20 copybook=" + c.copybook() + " encoding=ascii\n";
        Path f = dir.resolve(c.program() + ".fixture");
        Files.writeString(f, fixture);
        return f;
    }

    /** The program: each record read, the kind's statement, the record written; the count on stdout. */
    private static String program(String name, String copybook, String statement) {
        return String.join("\n",
                "       IDENTIFICATION DIVISION.", "       PROGRAM-ID. " + name + ".", "       ENVIRONMENT DIVISION.", "       INPUT-OUTPUT SECTION.", "       FILE-CONTROL.",
                "           SELECT IN-FILE ASSIGN TO INFILE ORGANIZATION IS SEQUENTIAL.", "           SELECT OUT-FILE ASSIGN TO OUTFILE ORGANIZATION IS SEQUENTIAL.",
                "       DATA DIVISION.", "       FILE SECTION.", "       FD  IN-FILE.", "       COPY " + copybook + ".", "       FD  OUT-FILE.", "       01  OUT-REC             PIC X(20).",
                "       WORKING-STORAGE SECTION.", "       01  EOF                 PIC X VALUE 'N'.", "       01  DONE                PIC 9(5) VALUE 0.",
                "       PROCEDURE DIVISION.", "           OPEN INPUT IN-FILE OUTPUT OUT-FILE", "           PERFORM UNTIL EOF = 'Y'", "               READ IN-FILE",
                "                   AT END MOVE 'Y' TO EOF", "                   NOT AT END", "                       " + statement,
                "                       WRITE OUT-REC FROM DV-REC", "                       ADD 1 TO DONE", "               END-READ", "           END-PERFORM",
                "           CLOSE IN-FILE OUT-FILE", "           DISPLAY 'DONE ' DONE ' RECORDS'", "           STOP RUN.", "");
    }

    private static String copybook(String... fields) {
        StringBuilder b = new StringBuilder("       01  DV-REC.\n           05  KEY-ID          PIC X(4).\n");
        for (String f : fields) b.append("           ").append(f).append('\n');
        return b.toString();
    }

    private static String id(int i) {
        return "R" + digits(i + 1, 3);
    }

    private static String digits(long v, int n) {
        return String.format("%0" + n + "d", v);
    }

    private static byte[] bytes(StringBuilder s) {
        return s.toString().getBytes(StandardCharsets.US_ASCII);
    }

    private static String sha256(byte[] b) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(b));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
