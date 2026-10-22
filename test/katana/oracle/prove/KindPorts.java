package katana.oracle.prove;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 8A.2's ports of the generated cases (research §49), a process in LANE-06's shape: `<code> exact` keeps the kind's
 * rule, `<code> wrong` breaks that one rule and nothing else, the kind's mutation (docs/DIVERGENCES.md).
 */
final class KindPorts {
    private KindPorts() {}

    public static void main(String[] args) throws IOException {
        String kind = args[0];
        boolean exact = args[1].equals("exact");
        byte[] in = Files.readAllBytes(Path.of(System.getenv("DD_INFILE")));
        StringBuilder out = new StringBuilder();
        int records = in.length / 20;
        for (int r = 0; r < records; r++) {
            String rec = new String(in, r * 20, 20, StandardCharsets.US_ASCII);
            out.append(switch (kind) {
                case "DV-01" -> add(rec, exact);
                case "DV-02" -> multiply(rec, exact, args[2]);
                default -> divide(rec, exact);
            });
        }
        Files.writeString(Path.of(System.getenv("DD_OUTFILE")), out, StandardCharsets.US_ASCII);
        System.out.println("DONE " + String.format("%05d", records) + " RECORDS");
    }

    /** DV-01: B = A + B into 9(3)V99; exact the sum modulo 1000, wrong the formatted sum cut at its right end. */
    private static String add(String rec, boolean exact) {
        long a = Long.parseLong(rec.substring(4, 9)), b = Long.parseLong(rec.substring(9, 14)), sum = a + b;
        String field = exact ? String.format("%05d", sum % 100000) : String.format("%05d", sum).substring(0, 5);
        return rec.substring(0, 9) + field + rec.substring(14);
    }

    /** DV-02: RES = A * K into 9(5)V99; exact rounding down, wrong HALF_UP. */
    private static String multiply(String rec, boolean exact, String k) {
        BigDecimal a = new BigDecimal(rec.substring(4, 11)).movePointLeft(2);
        BigDecimal res = a.multiply(new BigDecimal(k)).setScale(2, exact ? RoundingMode.DOWN : RoundingMode.HALF_UP);
        return rec.substring(0, 11) + cents(res) + rec.substring(18);
    }

    /** DV-03: RES = A / B * C into 9(5)V99; exact the quotient at dmax (2) places first, wrong the whole quotient. */
    private static String divide(String rec, boolean exact) {
        BigDecimal a = new BigDecimal(rec.substring(4, 7)), b = new BigDecimal(rec.substring(7, 8)), c = new BigDecimal(rec.substring(8, 10));
        BigDecimal q = exact ? a.divide(b, 2, RoundingMode.DOWN) : a.divide(b, MathContext.DECIMAL128);
        return rec.substring(0, 10) + cents(q.multiply(c).setScale(2, RoundingMode.DOWN)) + rec.substring(17);
    }

    private static String cents(BigDecimal v) {
        return String.format("%07d", v.movePointRight(2).longValueExact());
    }
}
