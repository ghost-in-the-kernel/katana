package katana.rt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Random;
import katana.oracle.nist.Cobc;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * MOVE (phase 3.1, J-103), cobc -std=ibm with the lane profile's binary truncation its oracle: 400 moves
 * from a fixed seed, numeric to numeric (every usage and sign), numeric integer to alphanumeric,
 * alphanumeric to alphanumeric (JUSTIFIED or not) and digits to numeric. The sender is given its value
 * by MOVE from a literal, then moved, and the receiver's raw bytes DISPLAYed; `rt/Move` must leave the
 * same bytes. A native binary of 1 or 2 digits, which cobc sizes apart from IBM (J-102), is not
 * generated. Its mutation: JUSTIFIED ignored, the justified cases differ.
 */
@Tag("integration")
class MoveDifferentialIntegrationTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));
    private static final String TEXT = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcxyz0123456789 -";

    /** A move: the sender's attributes and its literal value, the receiver's attributes. */
    record Case(Attr from, String literal, Attr to) {}

    static List<Case> cases() {
        Random r = new Random(20261007L);
        List<Case> cases = new ArrayList<>();
        for (int n = 0; n < 400; n++) {
            switch (n % 4) {
                case 0 -> {
                    Attr from = number(r, false), to = number(r, false);
                    cases.add(new Case(from, value(r, from), to));
                }
                case 1 -> {
                    Attr from = number(r, true);
                    cases.add(new Case(from, value(r, from), Attr.text(1 + r.nextInt(20), r.nextBoolean())));
                }
                case 2 -> {
                    Attr from = Attr.text(1 + r.nextInt(20), false);
                    cases.add(new Case(from, text(r, from.digits(), TEXT), Attr.text(1 + r.nextInt(20), r.nextBoolean())));
                }
                default -> {
                    Attr from = Attr.text(1 + r.nextInt(18), false);
                    cases.add(new Case(from, text(r, from.digits(), "0123456789"), number(r, false)));
                }
            }
        }
        return cases;
    }

    private static Attr number(Random r, boolean integer) {
        Attr.Usage usage = Attr.Usage.values()[r.nextInt(4)];
        int digits = (usage == Attr.Usage.NATIVE_BINARY ? 3 : 1) + r.nextInt(usage == Attr.Usage.NATIVE_BINARY ? 16 : 18);
        int scale = integer ? 0 : r.nextInt(Math.min(digits, 4) + 1);
        boolean signed = r.nextBoolean(), display = usage == Attr.Usage.DISPLAY;
        return new Attr(usage, Attr.Category.NUMERIC, digits, scale, signed, display && signed && r.nextBoolean(), display && signed && r.nextBoolean(), false, null);
    }

    private static String value(Random r, Attr a) {
        StringBuilder s = new StringBuilder();
        for (int k = 0; k < a.digits(); k++) s.append((char) ('0' + r.nextInt(10)));
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
        if (a.category() == Attr.Category.ALPHANUMERIC) return "X(" + a.digits() + ")" + (a.justified() ? " JUSTIFIED RIGHT" : "");
        int integer = a.digits() - a.scale();
        String p = (a.signed() ? "S" : "") + (integer > 0 ? "9(" + integer + ")" : "") + (a.scale() > 0 ? "V9(" + a.scale() + ")" : "");
        return p + switch (a.usage()) {
            case DISPLAY -> a.signed() && (a.signSeparate() || a.signLeading()) ? " SIGN " + (a.signLeading() ? "LEADING" : "TRAILING") + (a.signSeparate() ? " SEPARATE" : "") : "";
            case PACKED -> " COMP-3";
            case BINARY -> " COMP";
            case NATIVE_BINARY -> " COMP-5";
        };
    }

    static String program(List<Case> cases) {
        StringBuilder data = new StringBuilder(), proc = new StringBuilder();
        for (int n = 0; n < cases.size(); n++) {
            Case c = cases.get(n);
            data.append("01 SG").append(n).append(".\n  05 S").append(n).append(" PIC ").append(picture(c.from())).append(".\n");
            data.append("01 RG").append(n).append(".\n  05 R").append(n).append(" PIC ").append(picture(c.to())).append(".\n");
            String literal = c.from().category() == Attr.Category.ALPHANUMERIC ? "\"" + c.literal() + "\"" : c.literal();
            proc.append("MOVE ").append(literal).append(" TO S").append(n).append("\nMOVE S").append(n).append(" TO R").append(n).append("\nDISPLAY RG").append(n).append('\n');
        }
        return "IDENTIFICATION DIVISION.\nPROGRAM-ID. MOVES.\nDATA DIVISION.\nWORKING-STORAGE SECTION.\n" + data + "PROCEDURE DIVISION.\n" + proc + "STOP RUN.\n";
    }

    static List<byte[]> cobc(List<Case> cases) throws Exception {
        Path dir = Files.createDirectories(ROOT.resolve("build/rt-move"));
        Files.writeString(dir.resolve("MOVES.cbl"), program(cases));
        Cobc cobc = new Cobc(Duration.ofSeconds(120), "ibm");
        katana.oracle.nist.Runner.Result compiled = cobc.compile(dir, "MOVES.cbl", List.of("-free", "-fbinary-truncate"));
        assertEquals(0, compiled.exit(), compiled.output());
        byte[] out = cobc.execute(dir, "MOVES", Map.of()).output().getBytes(StandardCharsets.ISO_8859_1);
        List<byte[]> images = new ArrayList<>();
        int at = 0;
        for (Case c : cases) {
            int n = c.to().length();
            images.add(Arrays.copyOfRange(out, at, at + n));
            assertEquals('\n', out[at + n], "a newline after each receiver's bytes, case " + images.size());
            at += n + 1;
        }
        return images;
    }

    /** `rt/Move`'s receiver bytes for a case; `justify` false ignores JUSTIFIED (the mutation). */
    static byte[] rt(Case c, boolean justify) {
        Codepage cp = Codepage.COBC_ASCII;
        Field from = new Field(Record.of(c.from().length(), cp), 0, c.from());
        if (c.from().category() == Attr.Category.NUMERIC) from.put(Decimal.of(c.literal()));
        else System.arraycopy(c.literal().getBytes(cp.charset), 0, from.record().bytes(), 0, c.literal().length());
        Attr to = justify ? c.to() : new Attr(c.to().usage(), c.to().category(), c.to().digits(), c.to().scale(), c.to().signed(), c.to().signSeparate(), c.to().signLeading(), false, null);
        Field into = new Field(Record.of(to.length(), cp), 0, to);
        Move.move(from, into);
        return into.record().bytes();
    }

    static List<String> differ(List<Case> cases, List<byte[]> cobc, boolean justify) {
        List<String> differ = new ArrayList<>();
        for (int n = 0; n < cases.size(); n++) {
            byte[] rt = rt(cases.get(n), justify);
            if (!Arrays.equals(rt, cobc.get(n))) differ.add(n + " " + cases.get(n) + ": rt " + HexFormat.of().formatHex(rt) + ", cobc " + HexFormat.of().formatHex(cobc.get(n)));
        }
        return differ;
    }

    /**
     * The cases where cobc parts from IBM: a numeric sender longer than a JUSTIFIED alphanumeric
     * receiver. IBM (LR p. 198): "If the sending item is larger than the receiving item, the leftmost
     * character positions are truncated"; cobc keeps the leftmost digits, as if JUSTIFIED were not
     * there (each checked so below). A finding against cobc's dialect (J-103); listed so a change shows.
     */
    static final List<Integer> JUSTIFIED_FINDINGS = List.of(5, 77, 209, 253, 361);

    @Test
    void rtMovesAsCobcDoes() throws Exception {
        List<Case> cases = cases();
        List<byte[]> cobc = cobc(cases);
        List<String> differ = new ArrayList<>();
        List<Integer> findings = new ArrayList<>();
        for (int n = 0; n < cases.size(); n++) {
            Case c = cases.get(n);
            if (Arrays.equals(rt(c, true), cobc.get(n))) continue;
            boolean numericIntoJustified = c.from().category() == Attr.Category.NUMERIC && c.to().justified() && c.from().digits() > c.to().digits();
            if (numericIntoJustified && Arrays.equals(rt(c, false), cobc.get(n))) findings.add(n);
            else differ.add(n + " " + c + ": rt " + HexFormat.of().formatHex(rt(c, true)) + ", cobc " + HexFormat.of().formatHex(cobc.get(n)));
        }
        assertEquals(List.of(), differ.subList(0, Math.min(15, differ.size())), differ.size() + " of " + cases.size() + " differ");
        assertEquals(JUSTIFIED_FINDINGS, findings, "the numeric moves into a shorter JUSTIFIED receiver, where cobc truncates on the right");
    }

    /** R-056's mutation: with JUSTIFIED ignored, cases differ from cobc. */
    @Test
    void ignoringJustifiedDiffers() throws Exception {
        List<Case> cases = cases();
        assertTrue(differ(cases, cobc(cases), false).size() > 10);
    }
}
