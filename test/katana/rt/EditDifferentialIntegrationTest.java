package katana.rt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import katana.oracle.nist.Cobc;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Edited MOVEs (phase 3.1), cobc -std=ibm their oracle: 400 moves from a fixed seed, a numeric sender
 * into a numeric-edited receiver (fixed and floating insertion, zero suppression with Z or `*`, simple
 * insertion, CR DB + -, BLANK WHEN ZERO; values often zero or small, so suppression shows) and the
 * edited item moved back into a numeric item (de-editing); and a text sender into an alphanumeric-edited
 * receiver. Each receiver's bytes are DISPLAYed; `rt/Move` must leave the same bytes. IBM's own examples
 * are `EditorTest`. Its mutation: suppression read as 9s (no Z, `*` or floating string) differs.
 */
@Tag("integration")
class EditDifferentialIntegrationTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));

    /** A move: the sender's attributes and literal, the edited receiver, and (numeric) the numeric item it is de-edited into. */
    record Case(Attr from, String literal, Attr to, Attr back) {}

    static List<Case> cases() {
        Random r = new Random(20261008L);
        List<Case> cases = new ArrayList<>();
        for (int n = 0; n < 400; n++) {
            if (n % 8 == 7) {
                Attr from = Attr.text(1 + r.nextInt(12), false);
                cases.add(new Case(from, text(r, from.digits()), Attr.alphanumericEdited(new Edit(textMask(r), '$', false, false)), null));
                continue;
            }
            Attr to = numericEdited(r);
            int scale = r.nextInt(4), digits = scale + 1 + r.nextInt(9);
            Attr from = leadingSeparate(digits, scale);
            cases.add(new Case(from, value(r, from, n % 5 == 0), to, leadingSeparate(18, 6)));
        }
        return cases;
    }

    private static Attr leadingSeparate(int digits, int scale) {
        return new Attr(Attr.Usage.DISPLAY, Attr.Category.NUMERIC, digits, scale, true, true, true, false, null);
    }

    /** A numeric-edited picture's shape: its floating or suppression symbol, its currency and sign symbols, its integer positions. */
    record Shape(char floating, char suppress, boolean allSuppressed, boolean currency, int sign, char signChar, int integer, int lead, boolean commas) {}

    /** A shape drawn at random; `sign` 0 none, 1 leading, 2 trailing + or -, 3 CR, 4 DB. */
    static Shape shape(Random r) {
        int style = r.nextInt(6);
        char floating = style == 3 ? '$' : style == 4 ? (r.nextBoolean() ? '+' : '-') : 0;
        char suppress = style == 1 ? 'Z' : style == 2 ? '*' : 0;
        boolean allSuppressed = suppress != 0 && r.nextInt(4) == 0;
        boolean currency = floating != '$' && r.nextInt(3) == 0;
        int sign = floating == '+' || floating == '-' ? 0 : r.nextInt(5);
        char signChar = r.nextBoolean() ? '+' : '-';
        int integer = 1 + r.nextInt(8);
        return new Shape(floating, suppress, allSuppressed, currency, sign, signChar, integer, lead(r, floating, suppress, allSuppressed, integer), r.nextBoolean());
    }

    /** How many leading integer positions float or suppress: all, some, or (a floating string) at least one. */
    private static int lead(Random r, char floating, char suppress, boolean allSuppressed, int integer) {
        if (suppress == 0 && floating == 0) return 0;
        return Math.max(allSuppressed ? integer : r.nextInt(integer + 1), floating != 0 ? 1 : 0);
    }

    /** A numeric-edited picture, expanded: a sign and a currency sign fixed or floating, suppression or not, insertions, a fraction, a trailing sign. */
    static Attr numericEdited(Random r) {
        Shape h = shape(r);
        StringBuilder m = integerPart(h);
        if (!h.commas() && h.suppress() == 0 && h.floating() == 0 && r.nextInt(4) == 0) m.insert(m.length() - 1, "B0/".charAt(r.nextInt(3)));
        int scale = r.nextInt(3);
        if (scale > 0) m.append('.').append(String.valueOf(h.allSuppressed() ? h.suppress() : '9').repeat(scale));
        m.append(switch (h.sign()) { case 2 -> String.valueOf(h.signChar()); case 3 -> "CR"; case 4 -> "DB"; default -> ""; });
        Edit e = new Edit(m.toString(), '$', false, h.suppress() != '*' && r.nextInt(6) == 0);
        int digits = (int) m.chars().filter(c -> c == '9' || c == 'Z' || c == '*' || (h.floating() != 0 && c == h.floating())).count() - (h.floating() != 0 ? 1 : 0);
        return Attr.numericEdited(e, digits, scale);
    }

    /** The picture up to its point: a leading sign, a currency sign, the floating symbol's extra place, the integer positions with commas. */
    private static StringBuilder integerPart(Shape h) {
        StringBuilder m = new StringBuilder();
        if (h.sign() == 1) m.append(h.signChar());
        if (h.currency()) m.append('$');
        if (h.floating() != 0) m.append(h.floating());
        for (int k = 0; k < h.integer(); k++) {
            if (h.commas() && k > 0 && (h.integer() - k) % 3 == 0) m.append(',');
            m.append(k < h.lead() ? (h.floating() != 0 ? h.floating() : h.suppress()) : '9');
        }
        return m;
    }

    static String textMask(Random r) {
        StringBuilder m = new StringBuilder();
        int n = 2 + r.nextInt(10);
        for (int k = 0; k < n; k++) m.append(r.nextInt(4) == 0 ? "B0/".charAt(r.nextInt(3)) : 'X');
        return m.indexOf("X") < 0 ? m.append('X').toString() : m.toString();
    }

    private static String value(Random r, Attr a, boolean zero) {
        StringBuilder s = new StringBuilder();
        int significant = r.nextInt(a.digits() + 1);
        for (int k = 0; k < a.digits(); k++) s.append(zero || k < a.digits() - significant ? '0' : (char) ('0' + r.nextInt(10)));
        BigInteger unscaled = new BigInteger(s.toString());
        if (r.nextBoolean()) unscaled = unscaled.negate();
        String plain = new Decimal(unscaled, a.scale()).toBigDecimal().toPlainString();
        return plain.startsWith("-.") ? "-0" + plain.substring(1) : plain.startsWith(".") ? "0" + plain : plain;
    }

    private static String text(Random r, int length) {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder s = new StringBuilder();
        for (int k = 0; k < length; k++) s.append(alphabet.charAt(r.nextInt(alphabet.length())));
        return s.toString();
    }

    private static String picture(Attr a) {
        return switch (a.category()) {
            case ALPHANUMERIC -> "X(" + a.digits() + ")";
            case NUMERIC -> "S9(" + (a.digits() - a.scale()) + ")" + (a.scale() > 0 ? "V9(" + a.scale() + ")" : "") + " SIGN LEADING SEPARATE";
            default -> a.edit().mask() + (a.edit().blankWhenZero() ? " BLANK WHEN ZERO" : "");
        };
    }

    static String program(List<Case> cases) {
        StringBuilder data = new StringBuilder(), proc = new StringBuilder();
        for (int n = 0; n < cases.size(); n++) {
            Case c = cases.get(n);
            data.append("01 S").append(n).append(" PIC ").append(picture(c.from())).append(".\n");
            data.append("01 RG").append(n).append(".\n  05 R").append(n).append(" PIC ").append(picture(c.to())).append(".\n");
            String literal = c.from().category() == Attr.Category.ALPHANUMERIC ? "\"" + c.literal() + "\"" : c.literal();
            proc.append("MOVE ").append(literal).append(" TO S").append(n).append("\nMOVE S").append(n).append(" TO R").append(n).append("\nDISPLAY RG").append(n).append('\n');
            if (c.back() != null) {
                data.append("01 B").append(n).append(" PIC ").append(picture(c.back())).append(".\n");
                proc.append("MOVE R").append(n).append(" TO B").append(n).append("\nDISPLAY B").append(n).append('\n');
            }
        }
        return "IDENTIFICATION DIVISION.\nPROGRAM-ID. EDITS.\nDATA DIVISION.\nWORKING-STORAGE SECTION.\n" + data + "PROCEDURE DIVISION.\n" + proc + "STOP RUN.\n";
    }

    /** cobc's bytes for each case: the edited receiver, and the de-edited number when there is one. */
    static List<byte[][]> cobc(List<Case> cases) throws Exception {
        Path dir = Files.createDirectories(ROOT.resolve("build/rt-edit"));
        Files.writeString(dir.resolve("EDITS.cbl"), program(cases));
        Cobc cobc = new Cobc(Duration.ofSeconds(120), "ibm");
        katana.oracle.nist.Runner.Result compiled = cobc.compile(dir, "EDITS.cbl", List.of("-free"));
        assertEquals(0, compiled.exit(), compiled.output());
        byte[] out = cobc.execute(dir, "EDITS", Map.of()).output().getBytes(StandardCharsets.ISO_8859_1);
        List<byte[][]> images = new ArrayList<>();
        int at = 0;
        for (Case c : cases) {
            byte[][] image = new byte[c.back() == null ? 1 : 2][];
            Attr[] shown = {c.to(), c.back()};
            for (int k = 0; k < image.length; k++) {
                int n = shown[k].length();
                image[k] = Arrays.copyOfRange(out, at, at + n);
                assertEquals('\n', out[at + n], "a newline after each item's bytes, case " + images.size());
                at += n + 1;
            }
            images.add(image);
        }
        return images;
    }

    /** `rt/Move`'s bytes for a case; `suppress` false reads every suppression and floating symbol as a 9 (the mutation). */
    static byte[][] rt(Case c, boolean suppress) {
        Codepage cp = Codepage.COBC_ASCII;
        Field from = new Field(Record.of(c.from().length(), cp), 0, c.from());
        if (c.from().category() == Attr.Category.NUMERIC) from.put(Decimal.of(c.literal()));
        else System.arraycopy(c.literal().getBytes(cp.charset), 0, from.record().bytes(), 0, c.literal().length());
        Attr to = c.to();
        if (!suppress && to.category() == Attr.Category.NUMERIC_EDITED) {
            String m = to.edit().mask().replace('Z', '9').replace('*', '9');
            Edit e = new Edit(m, '$', false, to.edit().blankWhenZero());
            to = Attr.numericEdited(e, to.digits(), to.scale());
        }
        Field into = new Field(Record.of(to.length(), cp), 0, to);
        Move.move(from, into);
        if (c.back() == null) return new byte[][] {into.record().bytes()};
        Field back = new Field(Record.of(c.back().length(), cp), 0, c.back());
        Move.move(into, back);
        return new byte[][] {into.record().bytes(), back.record().bytes()};
    }

    /** A case's differences: which image ("edited" or "de-edited"), rt's bytes and cobc's. */
    record Difference(int n, int image, String rt, String cobc) {}

    static List<Difference> differences(List<Case> cases, List<byte[][]> cobc, boolean suppress) {
        List<Difference> out = new ArrayList<>();
        for (int n = 0; n < cases.size(); n++) {
            byte[][] rt = rt(cases.get(n), suppress);
            for (int k = 0; k < rt.length; k++)
                if (!Arrays.equals(rt[k], cobc.get(n)[k])) out.add(new Difference(n, k, new String(rt[k], StandardCharsets.ISO_8859_1), new String(cobc.get(n)[k], StandardCharsets.ISO_8859_1)));
        }
        return out;
    }

    /**
     * Where cobc parts from IBM's text on editing (J-104), each family with what shows it; Katana follows
     * IBM (`EditorTest` holds rt to IBM's own examples). A difference no family explains fails the test.
     */
    enum Finding {
        /** A + or - sign next to a currency sign: cobc shows the wrong sign, a stray character or a currency position as a digit; IBM's own example `-$999.99` gives ` $123.45` for +123.456 (LR p. 222). */
        SIGN_WITH_CURRENCY,
        /** Zero suppression and the all-suppressed zero judged on the value before its digits were cut: IBM suppresses "any leading zero in the data that appears in the same character position" (LR p. 224). */
        SUPPRESSION_BEFORE_TRUNCATION,
        /** DB not read as negative in de-editing; IBM: the de-edited value "can be signed" (LR p. 404), and DB shows only for a negative value (p. 222). */
        DB_NOT_DE_EDITED,
        /** A 0 insertion character read as a digit in de-editing; IBM de-edits to "the unedited value" (LR p. 404), and an inserted 0 is not data (p. 220). */
        INSERTED_ZERO_DE_EDITED,
        /** A fixed trailing - shown for a negative value cut to zero, where cobc's +, CR, DB and floating signs show positive; IBM's table: "positive or zero" (LR p. 222). */
        MINUS_ON_ZERO;

        /** The family a difference belongs to, checked by what cobc's bytes show; null when none explains it. */
        static Finding of(Case c, Difference d) {
            String mask = c.to().edit() == null ? "" : c.to().edit().mask();
            boolean minusOnZero = mask.endsWith("-") && new BigDecimal(c.literal()).signum() < 0 && Editor.number(c.to().edit(), c.to().scale(), new BigDecimal(c.literal())).endsWith(" ");
            if (minusOnZero && (d.image() == 0 ? d.cobc().endsWith("-") : d.cobc().equals("-" + d.rt().substring(1)))) return MINUS_ON_ZERO;
            if (mask.contains("+$") || mask.contains("-$") || mask.contains("$+") || mask.contains("$-")) return SIGN_WITH_CURRENCY;
            if (d.image() == 1) {
                if (mask.endsWith("DB") && d.rt().charAt(0) == '-' && d.cobc().equals("+" + d.rt().substring(1))) return DB_NOT_DE_EDITED;
                if (mask.indexOf('0') >= 0 && new BigDecimal(d.cobc().trim()).movePointLeft(6).compareTo(withInsertedZeros(c)) == 0) return INSERTED_ZERO_DE_EDITED;
                return null;
            }
            if (cut(c) && d.rt().length() == d.cobc().length()) return SUPPRESSION_BEFORE_TRUNCATION;
            return null;
        }

        /** Whether the receiver cuts nonzero digits off the sender's value, high or low. */
        private static boolean cut(Case c) {
            BigDecimal v = new BigDecimal(c.literal()).abs();
            BigDecimal kept = v.setScale(c.to().scale(), java.math.RoundingMode.DOWN).remainder(BigDecimal.TEN.pow(c.to().digits() - c.to().scale()));
            return kept.compareTo(v) != 0;
        }

        /** The edited image de-edited with its 0 insertions read as digits, as cobc reads it. */
        private static BigDecimal withInsertedZeros(Case c) {
            Edit e = c.to().edit();
            String shown = Editor.number(e, c.to().scale(), new BigDecimal(c.literal()));
            Edit read = new Edit(e.mask().replace('0', '9'), e.currency(), e.decimalComma(), e.blankWhenZero());
            return Editor.deEdit(read, c.to().scale(), shown);
        }
    }

    /** The cases of each finding, listed so a change in cobc or rt shows. */
    static final Map<Finding, List<Integer>> FINDINGS = Map.of(
            Finding.SIGN_WITH_CURRENCY, List.of(2, 26, 38, 59, 76, 99, 104, 146, 214, 230, 264, 334, 358, 362, 364, 372, 386),
            Finding.SUPPRESSION_BEFORE_TRUNCATION, List.of(122, 173, 228),
            Finding.DB_NOT_DE_EDITED, List.of(21, 86, 128, 133, 172, 173, 176, 193, 198, 218, 232, 238, 301, 302, 316, 336, 356, 357, 369),
            Finding.INSERTED_ZERO_DE_EDITED, List.of(266),
            Finding.MINUS_ON_ZERO, List.of(118, 203));

    static List<String> differ(List<Case> cases, List<byte[][]> cobc, boolean suppress) {
        return differences(cases, cobc, suppress).stream().map(d -> d.n() + (d.image() == 0 ? " edited " : " de-edited ") + cases.get(d.n()).literal() + " -> " + picture(cases.get(d.n()).to()) + ": rt '" + d.rt() + "', cobc '" + d.cobc() + "'").toList();
    }

    @Test
    void rtEditsAsCobcDoes() throws Exception {
        List<Case> cases = cases();
        List<String> unexplained = new ArrayList<>();
        Map<Finding, java.util.SortedSet<Integer>> found = new java.util.EnumMap<>(Finding.class);
        for (Difference d : differences(cases, cobc(cases), true)) {
            Finding f = Finding.of(cases.get(d.n()), d);
            if (f == null) unexplained.add(d.n() + (d.image() == 0 ? " edited " : " de-edited ") + cases.get(d.n()).literal() + " -> " + picture(cases.get(d.n()).to()) + ": rt '" + d.rt() + "', cobc '" + d.cobc() + "'");
            else found.computeIfAbsent(f, k -> new java.util.TreeSet<>()).add(d.n());
        }
        found.forEach((f, ns) -> System.out.println(f + " " + ns));
        assertEquals(List.of(), unexplained.subList(0, Math.min(15, unexplained.size())), unexplained.size() + " of " + cases.size() + " differ unexplained");
        Map<Finding, List<Integer>> listed = new java.util.EnumMap<>(Finding.class);
        found.forEach((f, ns) -> listed.put(f, List.copyOf(ns)));
        assertEquals(new java.util.EnumMap<>(FINDINGS), listed, "the cases where cobc parts from IBM's editing rules");
    }

    /** R-056's mutation: with suppression read as 9s, cases differ from cobc. */
    @Test
    void ignoringSuppressionDiffers() throws Exception {
        List<Case> cases = cases();
        assertTrue(differ(cases, cobc(cases), false).size() > 20);
    }
}
