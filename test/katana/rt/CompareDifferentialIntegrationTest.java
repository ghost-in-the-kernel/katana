package katana.rt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import katana.oracle.nist.Cobc;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Relation conditions (phase 3.1), cobc -std=ibm their oracle: 400 comparisons from a fixed seed, each
 * of the six relations: alphanumeric items of unequal sizes, numeric items of every usage, sign and
 * scale, a zoned integer against an alphanumeric item, an alphanumeric item against SPACES, ZEROS,
 * HIGH-VALUES, LOW-VALUES and an ALL literal. The alphanumeric ones run twice, natively and under
 * PROGRAM COLLATING SEQUENCE IS an EBCDIC alphabet (letters of both cases, digits and punctuation
 * order differently there). Each answer is DISPLAYed as T or F; `rt/Compare` must give the same. Its
 * mutation: the native order used under the EBCDIC sequence differs.
 */
@Tag("integration")
class CompareDifferentialIntegrationTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));
    private static final String ALPHABET = "AaBb019 -.";
    private static final List<String> RELATIONS = List.of("=", "<", ">", "<=", ">=", "NOT =");
    private static final List<String> FIGURATIVES = List.of("SPACES", "ZEROS", "HIGH-VALUES", "LOW-VALUES", "ALL \"Ab\"");

    /** A comparison: its kind (0 text, 1 numbers, 2 integer and text, 3 figurative), the two operands' attributes and values, the relation. */
    record Case(int kind, Attr left, String leftValue, Attr right, String rightValue, String relation) {}

    static List<Case> cases() {
        Random r = new Random(20261011L);
        List<Case> cases = new ArrayList<>();
        for (int n = 0; n < 400; n++) {
            String rel = RELATIONS.get(r.nextInt(RELATIONS.size()));
            switch (n % 4) {
                case 0 -> {
                    String a = text(r, 1 + r.nextInt(6), ALPHABET), b = r.nextInt(4) == 0 ? a + "  ".substring(0, r.nextInt(3)) : text(r, 1 + r.nextInt(6), ALPHABET);
                    cases.add(new Case(0, Attr.text(a.length(), false), a, Attr.text(b.length(), false), b, rel));
                }
                case 1 -> {
                    Attr a = number(r), b = number(r);
                    String va = value(r, a), vb = r.nextInt(4) == 0 ? va : value(r, b);
                    cases.add(new Case(1, a, va, b, vb, rel));
                }
                case 2 -> {
                    Attr a = Attr.zoned(1 + r.nextInt(6), 0, r.nextBoolean());
                    String b = text(r, 1 + r.nextInt(7), "0123456789 ");
                    cases.add(new Case(2, a, value(r, a), Attr.text(b.length(), false), b, rel));
                }
                default -> {
                    String a = r.nextInt(3) == 0 ? "" : text(r, 1 + r.nextInt(6), ALPHABET + "0 ");
                    cases.add(new Case(3, Attr.text(Math.max(1, a.length()), false), a, null, FIGURATIVES.get(r.nextInt(FIGURATIVES.size())), rel));
                }
            }
        }
        return cases;
    }

    private static Attr number(Random r) {
        Attr.Usage usage = Attr.Usage.values()[r.nextInt(4)];
        int digits = (usage == Attr.Usage.NATIVE_BINARY ? 3 : 1) + r.nextInt(9);
        return Attr.numeric(usage, digits, r.nextInt(Math.min(digits, 3) + 1), r.nextBoolean());
    }

    private static String value(Random r, Attr a) {
        StringBuilder s = new StringBuilder();
        for (int k = 0; k < a.digits(); k++) s.append((char) ('0' + (r.nextInt(3) == 0 ? 0 : r.nextInt(10))));
        BigInteger unscaled = new BigInteger(s.toString());
        if (a.signed() && r.nextBoolean()) unscaled = unscaled.negate();
        String plain = new Decimal(unscaled, a.scale()).toBigDecimal().toPlainString();
        return plain.startsWith("-.") ? "-0" + plain.substring(1) : plain.startsWith(".") ? "0" + plain : plain;
    }

    private static String text(Random r, int length, String alphabet) {
        StringBuilder s = new StringBuilder();
        for (int k = 0; k < length; k++) s.append(alphabet.charAt(r.nextInt(alphabet.length())));
        return s.toString();
    }

    private static String picture(Attr a) {
        if (a.category() == Attr.Category.ALPHANUMERIC) return "X(" + a.digits() + ")";
        int integer = a.digits() - a.scale();
        String p = (a.signed() ? "S" : "") + (integer > 0 ? "9(" + integer + ")" : "") + (a.scale() > 0 ? "V9(" + a.scale() + ")" : "");
        return p + switch (a.usage()) {
            case DISPLAY -> "";
            case PACKED -> " COMP-3";
            case BINARY -> " COMP";
            case NATIVE_BINARY -> " COMP-5";
        };
    }

    private static String literal(Attr a, String v) {
        return a.category() == Attr.Category.ALPHANUMERIC ? "\"" + v + "\"" : v;
    }

    /** The program: each case's operands given their values by MOVE, then one IF a line; `ebcdic` under an EBCDIC program collating sequence. */
    static String program(List<Case> cases, boolean ebcdic) {
        StringBuilder data = new StringBuilder(), proc = new StringBuilder();
        for (int n = 0; n < cases.size(); n++) {
            Case c = cases.get(n);
            data.append("01 L").append(n).append(" PIC ").append(picture(c.left())).append(".\n");
            proc.append("MOVE ").append(c.kind() == 3 && c.leftValue().isEmpty() ? "SPACES" : literal(c.left(), c.leftValue())).append(" TO L").append(n).append('\n');
            String right = c.rightValue();
            if (c.right() != null) {
                data.append("01 R").append(n).append(" PIC ").append(picture(c.right())).append(".\n");
                proc.append("MOVE ").append(literal(c.right(), c.rightValue())).append(" TO R").append(n).append('\n');
                right = "R" + n;
            }
            proc.append("IF L").append(n).append(' ').append(c.relation()).append(' ').append(right).append(" DISPLAY \"T\" ELSE DISPLAY \"F\" END-IF\n");
        }
        String env = ebcdic ? "ENVIRONMENT DIVISION.\nCONFIGURATION SECTION.\nOBJECT-COMPUTER. X PROGRAM COLLATING SEQUENCE IS EB.\nSPECIAL-NAMES. ALPHABET EB IS EBCDIC.\n" : "";
        return "IDENTIFICATION DIVISION.\nPROGRAM-ID. COMPARES.\n" + env + "DATA DIVISION.\nWORKING-STORAGE SECTION.\n" + data + "PROCEDURE DIVISION.\n" + proc + "STOP RUN.\n";
    }

    static List<String> cobc(List<Case> cases, boolean ebcdic) throws Exception {
        Path dir = Files.createDirectories(ROOT.resolve("build/rt-compare" + (ebcdic ? "-ebcdic" : "")));
        Files.writeString(dir.resolve("COMPARES.cbl"), program(cases, ebcdic));
        Cobc cobc = new Cobc(Duration.ofSeconds(120), "ibm");
        katana.oracle.nist.Runner.Result compiled = cobc.compile(dir, "COMPARES.cbl", List.of("-free"));
        assertEquals(0, compiled.exit(), compiled.output());
        List<String> lines = cobc.execute(dir, "COMPARES", Map.of()).output().lines().toList();
        assertEquals(cases.size(), lines.size(), "one line a case");
        return lines;
    }

    /** `rt/Compare`'s answer for a case under a collating sequence. */
    static String rt(Case c, Collation order) {
        Codepage cp = Codepage.COBC_ASCII;
        Field l = field(c.left(), c.kind() == 3 && c.leftValue().isEmpty() ? " " : c.leftValue(), cp);
        int cmp;
        if (c.kind() == 1) cmp = Compare.numbers(l.get(), field(c.right(), c.rightValue(), cp).get());
        else if (c.kind() == 3) {
            byte[] a = Compare.operand(l);
            cmp = Compare.all(a, 0, a.length, figurative(c.rightValue(), order), order);
        } else {
            byte[] a = Compare.operand(l), b = Compare.operand(field(c.right(), c.rightValue(), cp));
            cmp = Compare.text(a, 0, a.length, b, 0, b.length, cp.character(' '), order);
        }
        boolean holds = switch (c.relation()) {
            case "=" -> cmp == 0;
            case "<" -> cmp < 0;
            case ">" -> cmp > 0;
            case "<=" -> cmp <= 0;
            case ">=" -> cmp >= 0;
            default -> cmp != 0;
        };
        return holds ? "T" : "F";
    }

    /** A figurative constant's character, HIGH-VALUES and LOW-VALUES the highest and lowest of the collating sequence (LR p. 115). */
    private static byte[] figurative(String f, Collation order) {
        return switch (f) {
            case "SPACES" -> new byte[] {' '};
            case "ZEROS" -> new byte[] {'0'};
            case "HIGH-VALUES" -> new byte[] {extreme(order, true)};
            case "LOW-VALUES" -> new byte[] {extreme(order, false)};
            default -> "Ab".getBytes(StandardCharsets.ISO_8859_1);
        };
    }

    private static byte extreme(Collation order, boolean highest) {
        int best = 0;
        for (int b = 1; b < 256; b++) if (highest ? order.weight((byte) b) > order.weight((byte) best) : order.weight((byte) b) < order.weight((byte) best)) best = b;
        return (byte) best;
    }

    private static Field field(Attr a, String value, Codepage cp) {
        Field f = new Field(Record.of(a.length(), cp), 0, a);
        if (a.category() == Attr.Category.NUMERIC) {
            Decimal v = Decimal.of(value);
            Attr literal = Attr.zoned(Math.max(1, v.unscaled().abs().toString().length()), v.scale(), true);
            Field from = new Field(Record.of(literal.length(), cp), 0, literal);
            from.put(v);
            Move.move(from, f);
        }
        else System.arraycopy(value.getBytes(cp.charset), 0, f.record().bytes(), 0, value.length());
        return f;
    }

    static final Collation EBCDIC = Collation.of(Codepage.COBC_ASCII, Charset.forName("IBM01140"));

    static List<String> differ(List<Case> cases, List<String> cobc, Collation order) {
        List<String> differ = new ArrayList<>();
        for (int n = 0; n < cases.size(); n++) {
            String rt = rt(cases.get(n), order);
            if (!rt.equals(cobc.get(n))) differ.add(n + " " + cases.get(n) + ": rt " + rt + ", cobc " + cobc.get(n));
        }
        return differ;
    }

    @Test
    void rtComparesAsCobcDoes() throws Exception {
        List<Case> cases = cases();
        List<String> differ = differ(cases, cobc(cases, false), Collation.nativeOrder());
        assertEquals(List.of(), differ.subList(0, Math.min(15, differ.size())), differ.size() + " of " + cases.size() + " differ");
    }

    @Test
    void rtComparesAsCobcDoesUnderAnEbcdicSequence() throws Exception {
        List<Case> cases = cases().stream().filter(c -> c.kind() != 1).toList();
        List<String> differ = differ(cases, cobc(cases, true), EBCDIC);
        assertEquals(List.of(), differ.subList(0, Math.min(15, differ.size())), differ.size() + " of " + cases.size() + " differ");
    }

    /** R-056's mutation: the native order under the EBCDIC sequence differs from cobc. */
    @Test
    void theNativeOrderUnderAnEbcdicSequenceDiffers() throws Exception {
        List<Case> cases = cases().stream().filter(c -> c.kind() != 1).toList();
        assertTrue(differ(cases, cobc(cases, true), Collation.nativeOrder()).size() > 10);
    }
}
