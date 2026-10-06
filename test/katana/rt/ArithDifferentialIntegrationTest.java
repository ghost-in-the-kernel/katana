package katana.rt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
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
 * The differential test of Appendix A (phase 3.1, J-101): 400 COMPUTEs of two operations over data
 * items, generated from a fixed seed (sizes small and large enough to pass the 30-digit cap, signs,
 * ROUNDED or not), compiled into one program under cobc -std=ibm, and each computed three ways: by cobc,
 * by `AppendixAModel` (written from IBM's text, no code shared with `rt/`) and by `rt/Decimal`. The model
 * and `rt` must agree on every case, and cobc with them on every case no intermediate of which passes
 * the 30-digit cap; past it, where cobc keeps places IBM's table cuts, the cases are listed as findings
 * against cobc. Its mutation: the model with ROUNDED's place taken away disagrees with cobc.
 */
@Tag("integration")
class ArithDifferentialIntegrationTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));
    private static final int CASES = 400;
    private static final String OPS = "+-*/";

    /** An operand: integer and decimal places of its PICTURE, and its value as text. */
    record Operand(int i, int d, String value) {}

    /** `a op1 b op2 c` into `S9(ri)V9(rd)`, ROUNDED or not. */
    record Case(Operand a, char op1, Operand b, char op2, Operand c, int ri, int rd, boolean rounded) {
        /** Whether `b op2 c` is evaluated first (a multiplication or division after an addition or subtraction). */
        boolean rightFirst() {
            return "*/".indexOf(op2) >= 0 && "+-".indexOf(op1) >= 0;
        }

        /** The statement's dmax: the receiver's decimals and every operand's but a divisor's. */
        int dmax() {
            int dmax = rd;
            if (op1 != '/') dmax = Math.max(dmax, b.d());
            if (op2 != '/') dmax = Math.max(dmax, c.d());
            return Math.max(dmax, a.d());
        }
    }

    static List<Case> cases() {
        Random r = new Random(20261006L);
        List<Case> cases = new ArrayList<>();
        for (int n = 0; n < CASES; n++) {
            boolean large = n % 5 == 4;
            char op1 = OPS.charAt(r.nextInt(4)), op2 = OPS.charAt(r.nextInt(4));
            Operand a = operand(r, large, false), b = operand(r, large, op1 == '/'), c = operand(r, large, op2 == '/');
            cases.add(new Case(a, op1, b, op2, c, 1 + r.nextInt(large ? 17 : 6), r.nextInt(large ? 6 : 4), r.nextBoolean()));
        }
        return cases;
    }

    private static Operand operand(Random r, boolean large, boolean divisor) {
        int i = large ? 10 + r.nextInt(7) : 1 + r.nextInt(5), d = large ? 2 + r.nextInt(6) : r.nextInt(4);
        String digits;
        do {
            StringBuilder s = new StringBuilder();
            for (int k = 0; k < i + d; k++) s.append((char) ('0' + r.nextInt(10)));
            digits = s.toString();
        } while (divisor && new BigInteger(digits).signum() == 0);
        String value = (r.nextInt(3) == 0 ? "-" : "") + digits.substring(0, i) + (d > 0 ? "." + digits.substring(i) : "");
        return new Operand(i, d, value);
    }

    /** The program: three operands and a receiver per case, each COMPUTE then its DISPLAY on a line of its own. */
    static String program(List<Case> cases) {
        StringBuilder data = new StringBuilder(), proc = new StringBuilder();
        for (int n = 0; n < cases.size(); n++) {
            Case c = cases.get(n);
            item(data, "A" + n, c.a());
            item(data, "B" + n, c.b());
            item(data, "C" + n, c.c());
            data.append("01 R").append(n).append(" PIC S9(").append(c.ri()).append(")").append(c.rd() > 0 ? "V9(" + c.rd() + ")" : "").append(" SIGN LEADING SEPARATE.\n");
            proc.append("COMPUTE R").append(n).append(c.rounded() ? " ROUNDED" : "").append(" = A").append(n).append(' ').append(c.op1()).append(" B").append(n)
                    .append(' ').append(c.op2()).append(" C").append(n).append("\nDISPLAY R").append(n).append('\n');
        }
        return "IDENTIFICATION DIVISION.\nPROGRAM-ID. DIFF.\nDATA DIVISION.\nWORKING-STORAGE SECTION.\n" + data + "PROCEDURE DIVISION.\n" + proc + "STOP RUN.\n";
    }

    private static void item(StringBuilder data, String name, Operand o) {
        data.append("01 ").append(name).append(" PIC S9(").append(o.i()).append(")").append(o.d() > 0 ? "V9(" + o.d() + ")" : "").append(" VALUE ").append(o.value()).append(".\n");
    }

    /** The model's answer for a case; `rounding` false takes ROUNDED's place away (the mutation). */
    static String model(Case c, boolean rounding) {
        return modelled(c, rounding).text();
    }

    /** The model's answer and whether an intermediate passed the 30-digit cap on the way. */
    record Modelled(String text, boolean capped) {}

    static Modelled modelled(Case c, boolean rounding) {
        return modelled(c, rounding, 30);
    }

    /** The model under a cap: 30 as ARITH(COMPAT) gives it, or none (Integer.MAX_VALUE) to see what a case would be uncut. */
    static Modelled modelled(Case c, boolean rounding, int cap) {
        AppendixAModel.Carried a = carried(c.a()), b = carried(c.b()), k = carried(c.c());
        int extra = c.rounded() && rounding ? 1 : 0, dmax = c.dmax();
        AppendixAModel.Carried result = c.rightFirst()
                ? AppendixAModel.apply(c.op1(), a, AppendixAModel.apply(c.op2(), b, k, dmax, cap, 0), dmax, cap, extra)
                : AppendixAModel.apply(c.op2(), AppendixAModel.apply(c.op1(), a, b, dmax, cap, 0), k, dmax, cap, extra);
        return new Modelled(AppendixAModel.store(result, c.ri() + c.rd(), c.rd(), true, c.rounded()), result.capped());
    }

    private static AppendixAModel.Carried carried(Operand o) {
        return new AppendixAModel.Carried(AppendixAModel.Fraction.of(o.value()), o.i(), o.d(), false);
    }

    /** `rt/Decimal`'s answer for a case, as the lowering will call it. */
    static String rt(Case c) {
        ArithProfile p = ArithProfile.IBM_COMPAT;
        Decimal.Term a = term(c.a()), b = term(c.b()), k = term(c.c());
        int dmax = c.dmax();
        Decimal.Term result = c.rightFirst()
                ? op(p, c.op1(), a, op(p, c.op2(), b, k, dmax, false, false), dmax, true, c.rounded())
                : op(p, c.op2(), op(p, c.op1(), a, b, dmax, false, false), k, dmax, true, c.rounded());
        Decimal.Stored s = Decimal.store(result, c.ri() + c.rd(), c.rd(), true, c.rounded());
        return printed(s.value(), c.ri(), c.rd());
    }

    private static Decimal.Term term(Operand o) {
        return Decimal.operand(Decimal.of(o.value()), o.i());
    }

    private static Decimal.Term op(ArithProfile p, char op, Decimal.Term x, Decimal.Term y, int dmax, boolean last, boolean rounded) {
        return switch (op) {
            case '+' -> Decimal.add(p, x, y, dmax, last, rounded);
            case '-' -> Decimal.subtract(p, x, y, dmax, last, rounded);
            case '*' -> Decimal.multiply(p, x, y, dmax, last, rounded);
            default -> Decimal.divide(p, x, y, dmax, last, rounded);
        };
    }

    /** A stored value as the model prints it: its sign, its integer digits, a point, its decimals. */
    private static String printed(Decimal v, int ri, int rd) {
        String digits = String.format("%0" + (ri + rd) + "d", v.unscaled().abs());
        return (v.unscaled().signum() < 0 ? "-" : "+") + digits.substring(0, ri) + (rd > 0 ? "." + digits.substring(ri) : "");
    }

    /** cobc's line for a case (sign, digits, no point under -std=ibm) as the model prints it. */
    private static String cobc(String line, Case c) {
        String digits = line.substring(1);
        return line.charAt(0) + digits.substring(0, c.ri()) + (c.rd() > 0 ? "." + digits.substring(c.ri()) : "");
    }

    private static List<String> run(List<Case> cases) throws Exception {
        Path dir = Files.createDirectories(ROOT.resolve("build/rt-differential"));
        Files.writeString(dir.resolve("DIFF.cbl"), program(cases));
        Cobc cobc = new Cobc(Duration.ofSeconds(120), "ibm");
        katana.oracle.nist.Runner.Result compiled = cobc.compile(dir, "DIFF.cbl", List.of("-free"));
        assertEquals(0, compiled.exit(), compiled.output());
        List<String> lines = cobc.execute(dir, "DIFF", Map.of()).output().lines().toList();
        assertEquals(cases.size(), lines.size(), "one line a case");
        List<String> out = new ArrayList<>();
        for (int n = 0; n < cases.size(); n++) out.add(cobc(lines.get(n), cases.get(n)));
        return out;
    }

    /**
     * The cases where cobc and IBM's table part, each past the 30-digit cap and each matching what the
     * model gives with no cap at all: cobc keeps the places IBM's table cuts there (first seen as a
     * product of 26 integer and 8 decimal places, which the table carries as 25 and 5 and cobc with at
     * least 6). A finding against cobc's dialect (Fable: Katana follows IBM's text); listed so that a
     * change in either shows (J-101).
     */
    static final List<Integer> CAP_FINDINGS = List.of(39, 84, 89, 99, 129, 169, 239);

    @Test
    void cobcTheModelAndRtAgreeOnEveryCase() throws Exception {
        List<Case> cases = cases();
        List<String> cobc = run(cases), differ = new ArrayList<>();
        List<Integer> capFindings = new ArrayList<>();
        int capped = 0;
        for (int n = 0; n < cases.size(); n++) {
            Modelled model = modelled(cases.get(n), true);
            String rt = rt(cases.get(n));
            if (model.capped()) capped++;
            if (!rt.equals(model.text())) differ.add("case " + n + " " + cases.get(n) + ": model " + model.text() + ", rt " + rt);
            else if (!cobc.get(n).equals(model.text()) && model.capped() && cobc.get(n).equals(modelled(cases.get(n), true, Integer.MAX_VALUE).text())) capFindings.add(n);
            else if (!cobc.get(n).equals(model.text())) differ.add("case " + n + " " + cases.get(n) + ": cobc " + cobc.get(n) + ", model and rt " + model.text());
        }
        System.out.println("cases " + cases.size() + ", past the cap " + capped + ", cobc parting from IBM's table there " + capFindings);
        assertTrue(capped >= 20, "too few cases pass the cap to try its rows: " + capped);
        assertEquals(List.of(), differ.subList(0, Math.min(differ.size(), 15)), differ.size() + " of " + cases.size() + " disagree");
        assertEquals(CAP_FINDINGS, capFindings, "the cases where cobc keeps places IBM's table cuts");
    }

    /** R-056's mutation: the model without ROUNDED's place on the last operation disagrees with cobc. */
    @Test
    void theModelWithoutRoundedsPlaceDisagreesWithCobc() throws Exception {
        List<Case> cases = cases();
        List<String> cobc = run(cases);
        int differ = 0;
        for (int n = 0; n < cases.size(); n++) if (!cobc.get(n).equals(model(cases.get(n), false))) differ++;
        assertTrue(differ > 0, "taking ROUNDED's place away changed no case: the cases cannot see it");
    }
}
