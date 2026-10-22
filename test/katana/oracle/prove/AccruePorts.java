package katana.oracle.prove;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 8A.1's three foreign ports of ACCRUE (research §48), hand-written as a port's author would write them, each a
 * process in LANE-06's shape: the faithful one truncates as the COBOL does, the naive one rounds HALF_UP where
 * the COBOL truncates, and the deaf one writes a file of its own instead of `DD_OUTFILE`.
 */
final class AccruePorts {
    private static final BigDecimal RATE = new BigDecimal("0.0375"), WRAP = new BigDecimal("100000");

    private AccruePorts() {}

    /** One port's run: each 20-byte record, its interest and new balance; the count on stdout. */
    static int run(String in, String out, RoundingMode rounding) {
        try {
            byte[] bytes = Files.readAllBytes(Path.of(in));
            StringBuilder written = new StringBuilder();
            int records = bytes.length / 20;
            for (int r = 0; r < records; r++) {
                String rec = new String(bytes, r * 20, 20, StandardCharsets.US_ASCII);
                BigDecimal balance = new BigDecimal(rec.substring(6, 13)).movePointLeft(2);
                BigDecimal interest = balance.multiply(RATE).setScale(2, rounding);
                BigDecimal sum = balance.add(interest).remainder(WRAP);
                written.append(rec, 0, 6).append(cents(sum)).append(cents(interest));
            }
            Files.writeString(Path.of(out), written, StandardCharsets.US_ASCII);
            System.out.println("ACCRUED " + String.format("%05d", records) + " RECORDS");
            return 0;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String cents(BigDecimal v) {
        return String.format("%07d", v.movePointRight(2).longValueExact());
    }

    /** The faithful port: truncated to cents, as COMPUTE without ROUNDED. */
    static final class Faithful {
        public static void main(String[] args) {
            System.exit(run(System.getenv("DD_INFILE"), System.getenv("DD_OUTFILE"), RoundingMode.DOWN));
        }
    }

    /** The naive port: HALF_UP, the most common reading of a COMPUTE. */
    static final class Naive {
        public static void main(String[] args) {
            System.exit(run(System.getenv("DD_INFILE"), System.getenv("DD_OUTFILE"), RoundingMode.HALF_UP));
        }
    }

    /** The deaf port: its output to a fixed file in its working directory, `DD_OUTFILE` never read. */
    static final class Deaf {
        public static void main(String[] args) {
            System.exit(run(System.getenv("DD_INFILE"), "accrue.out", RoundingMode.DOWN));
        }
    }
}
