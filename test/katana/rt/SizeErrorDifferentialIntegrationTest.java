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
 * The size error condition (phase 3.1), cobc -std=ibm with the lane profile's binary truncation its
 * oracle: 300 arithmetic statements from a fixed seed whose results often pass their receivers'
 * PICTUREs: ADD into two receivers, COMPUTE of a product, DIVIDE INTO (by zero only with the phrase),
 * ROUNDED or not, with ON SIZE ERROR / NOT ON SIZE ERROR or without, into receivers of every usage.
 * Each receiver's bytes and which phrase ran are DISPLAYed; `rt/Arithmetic.store` must give the same.
 * Its mutation: storing despite the phrase differs.
 */
@Tag("integration")
class SizeErrorDifferentialIntegrationTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));

    /** A statement: its form (0 ADD, 1 COMPUTE, 2 DIVIDE), operands A and B, receivers with their values before, ROUNDED, the phrase. */
    record Case(int form, Attr a, String av, Attr b, String bv, List<Attr> into, List<String> before, boolean rounded, boolean phrase) {}

    static List<Case> cases() {
        Random r = new Random(20261012L);
        List<Case> cases = new ArrayList<>();
        for (int n = 0; n < 300; n++) {
            int form = n % 3;
            boolean phrase = r.nextInt(3) != 0;
            Attr a = Attr.zoned(1 + r.nextInt(5), r.nextInt(3), true), b = Attr.zoned(1 + r.nextInt(4), r.nextInt(3), true);
            String av = value(r, a, form == 2 && phrase && r.nextInt(4) == 0), bv = value(r, b, false);
            if (form == 2 && !phrase && new java.math.BigDecimal(av).signum() == 0) av = "1";
            List<Attr> into = new ArrayList<>();
            List<String> before = new ArrayList<>();
            for (int k = 0; k < (form == 0 ? 2 : 1); k++) {
                Attr t = receiver(r);
                into.add(t);
                before.add(value(r, t, false));
            }
            cases.add(new Case(form, a, av, b, bv, into, before, r.nextBoolean(), phrase));
        }
        return cases;
    }

    private static Attr receiver(Random r) {
        Attr.Usage usage = Attr.Usage.values()[r.nextInt(4)];
        int digits = (usage == Attr.Usage.NATIVE_BINARY ? 3 : 1) + r.nextInt(4);
        return Attr.numeric(usage, digits, r.nextInt(Math.min(digits, 2) + 1), r.nextBoolean());
    }

    private static String value(Random r, Attr a, boolean zero) {
        StringBuilder s = new StringBuilder();
        for (int k = 0; k < a.digits(); k++) s.append(zero ? '0' : (char) ('0' + r.nextInt(10)));
        BigInteger unscaled = new BigInteger(s.toString());
        if (a.signed() && r.nextBoolean()) unscaled = unscaled.negate();
        String plain = new Decimal(unscaled, a.scale()).toBigDecimal().toPlainString();
        return plain.startsWith("-.") ? "-0" + plain.substring(1) : plain.startsWith(".") ? "0" + plain : plain;
    }

    private static String picture(Attr a) {
        int integer = a.digits() - a.scale();
        String p = (a.signed() ? "S" : "") + (integer > 0 ? "9(" + integer + ")" : "") + (a.scale() > 0 ? "V9(" + a.scale() + ")" : "");
        return p + switch (a.usage()) {
            case DISPLAY -> "";
            case PACKED -> " COMP-3";
            case BINARY -> " COMP";
            case NATIVE_BINARY -> " COMP-5";
        };
    }

    static String program(List<Case> cases) {
        StringBuilder data = new StringBuilder("01 F PIC X.\n"), proc = new StringBuilder();
        for (int n = 0; n < cases.size(); n++) {
            Case c = cases.get(n);
            data.append("01 A").append(n).append(" PIC ").append(picture(c.a())).append(" VALUE ").append(c.av()).append(".\n");
            data.append("01 B").append(n).append(" PIC ").append(picture(c.b())).append(" VALUE ").append(c.bv()).append(".\n");
            data.append("01 G").append(n).append(".\n");
            StringBuilder names = new StringBuilder();
            for (int k = 0; k < c.into().size(); k++) {
                data.append("  05 R").append(n).append('-').append(k).append(" PIC ").append(picture(c.into().get(k))).append(".\n");
                proc.append("MOVE ").append(c.before().get(k)).append(" TO R").append(n).append('-').append(k).append('\n');
                names.append(" R").append(n).append('-').append(k).append(c.rounded() ? " ROUNDED" : "");
            }
            proc.append("MOVE \"-\" TO F\n").append(switch (c.form()) {
                case 0 -> "ADD A" + n + " TO" + names;
                case 1 -> "COMPUTE" + names + " = A" + n + " * B" + n;
                default -> "DIVIDE A" + n + " INTO" + names;
            });
            if (c.phrase()) proc.append(" ON SIZE ERROR MOVE \"E\" TO F NOT ON SIZE ERROR MOVE \"N\" TO F");
            proc.append(switch (c.form()) { case 0 -> " END-ADD"; case 1 -> " END-COMPUTE"; default -> " END-DIVIDE"; }).append('\n');
            proc.append("DISPLAY F G").append(n).append('\n');
        }
        return "IDENTIFICATION DIVISION.\nPROGRAM-ID. SIZES.\nDATA DIVISION.\nWORKING-STORAGE SECTION.\n" + data + "PROCEDURE DIVISION.\n" + proc + "STOP RUN.\n";
    }

    static List<byte[]> cobc(List<Case> cases) throws Exception {
        Path dir = Files.createDirectories(ROOT.resolve("build/rt-size"));
        Files.writeString(dir.resolve("SIZES.cbl"), program(cases));
        Cobc cobc = new Cobc(Duration.ofSeconds(120), "ibm");
        katana.oracle.nist.Runner.Result compiled = cobc.compile(dir, "SIZES.cbl", List.of("-free", "-fbinary-truncate"));
        assertEquals(0, compiled.exit(), compiled.output());
        byte[] out = cobc.execute(dir, "SIZES", Map.of()).output().getBytes(StandardCharsets.ISO_8859_1);
        List<byte[]> images = new ArrayList<>();
        int at = 0;
        for (Case c : cases) {
            int n = 1 + c.into().stream().mapToInt(Attr::length).sum();
            images.add(Arrays.copyOfRange(out, at, at + n));
            assertEquals('\n', out[at + n], "a newline after each line, case " + images.size());
            at += n + 1;
        }
        return images;
    }

    /** `rt`'s bytes for a case: the phrase that ran, then each receiver's; `keep` false stores despite the phrase (the mutation). */
    static byte[] rt(Case c, boolean keep) {
        return rt(c, keep, false);
    }

    /** `capacity` true judges a native binary's size error by its binary field's capacity, not its PICTURE (cobc's reading; J-108). */
    static byte[] rt(Case c, boolean keep, boolean capacity) {
        Codepage cp = Codepage.COBC_ASCII;
        ArithProfile p = ArithProfile.IBM_COMPAT;
        Decimal a = Decimal.of(c.av()), b = Decimal.of(c.bv());
        boolean error = false;
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        for (int k = 0; k < c.into().size(); k++) {
            Attr t = c.into().get(k);
            Field f = new Field(Record.of(t.length(), cp), 0, t);
            Move.number(Decimal.of(c.before().get(k)), f);
            Decimal.Term ta = Decimal.operand(a, c.a().digits() - c.a().scale()), tb = Decimal.operand(b, c.b().digits() - c.b().scale()), tr = Decimal.operand(f.get(), t.digits() - t.scale());
            int dmax = Math.max(t.scale(), Math.max(c.a().scale(), c.form() == 1 ? c.b().scale() : 0));
            Decimal.Term result = switch (c.form()) {
                case 0 -> Decimal.add(p, tr, ta, dmax, true, c.rounded());
                case 1 -> Decimal.multiply(p, ta, tb, dmax, true, c.rounded());
                default -> Decimal.divide(p, tr, ta, Math.max(t.scale(), 0), true, c.rounded());
            };
            if (capacity && t.usage() == Attr.Usage.NATIVE_BINARY) {
                java.math.BigInteger v = result.value().toBigDecimal().setScale(t.scale(), c.rounded() ? java.math.RoundingMode.HALF_UP : java.math.RoundingMode.DOWN).unscaledValue();
                int bits = 8 * t.length();
                java.math.BigInteger max = java.math.BigInteger.ONE.shiftLeft(t.signed() ? bits - 1 : bits), min = t.signed() ? max.negate() : java.math.BigInteger.ZERO;
                boolean over = (t.signed() ? v : v.abs()).compareTo(max) >= 0 || (t.signed() ? v : v.abs()).compareTo(min) < 0;
                if (!(over && c.phrase())) Arithmetic.store(f, result, c.rounded(), false);
                error |= over;
            } else error |= Arithmetic.store(f, result, c.rounded(), c.phrase() && keep);
            out.writeBytes(f.record().bytes());
        }
        byte flag = (byte) (!c.phrase() ? '-' : error ? 'E' : 'N');
        byte[] body = out.toByteArray(), line = new byte[body.length + 1];
        line[0] = flag;
        System.arraycopy(body, 0, line, 1, body.length);
        return line;
    }

    static List<String> differ(List<Case> cases, List<byte[]> cobc, boolean keep) {
        List<String> differ = new ArrayList<>();
        for (int n = 0; n < cases.size(); n++) {
            byte[] rt = rt(cases.get(n), keep);
            if (!Arrays.equals(rt, cobc.get(n))) differ.add(n + " " + cases.get(n) + ": rt " + HexFormat.of().formatHex(rt) + ", cobc " + HexFormat.of().formatHex(cobc.get(n)));
        }
        return differ;
    }

    /**
     * The cases where cobc judges a native binary (COMP-5) receiver's size error by its binary field's
     * capacity, not its PICTURE, and so stores a value IBM's text calls a size error (LR p. 297: "the
     * largest value that the resultant data item can contain is the value implied by the item's decimal
     * PICTURE character-string, regardless of the TRUNC compiler option"). Each checked to be exactly
     * that reading; a finding against cobc, D-009 (J-108).
     */
    static final List<Integer> CAPACITY_FINDINGS = List.of(115, 130, 148, 150, 243, 283);

    @Test
    void rtStoresAsCobcDoes() throws Exception {
        List<Case> cases = cases();
        List<byte[]> cobc = cobc(cases);
        List<String> differ = new ArrayList<>();
        List<Integer> findings = new ArrayList<>();
        for (int n = 0; n < cases.size(); n++) {
            Case c = cases.get(n);
            if (Arrays.equals(rt(c, true), cobc.get(n))) continue;
            if (Arrays.equals(rt(c, true, true), cobc.get(n))) findings.add(n);
            else differ.add(n + " " + c + ": rt " + HexFormat.of().formatHex(rt(c, true)) + ", cobc " + HexFormat.of().formatHex(cobc.get(n)));
        }
        System.out.println("capacity findings " + findings);
        assertEquals(List.of(), differ.subList(0, Math.min(10, differ.size())), differ.size() + " of " + cases.size() + " differ");
        assertEquals(CAPACITY_FINDINGS, findings, "the cases where cobc judges a COMP-5 size error by its binary capacity");
    }

    /** R-056's mutation: storing despite ON SIZE ERROR differs from cobc. */
    @Test
    void storingDespiteThePhraseDiffers() throws Exception {
        List<Case> cases = cases();
        assertTrue(differ(cases, cobc(cases), false).size() > 10);
    }
}
