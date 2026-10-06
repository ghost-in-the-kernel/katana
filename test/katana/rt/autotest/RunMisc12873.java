package katana.rt.autotest;

import katana.rt.Arithmetic;
import katana.rt.Attr;
import katana.rt.Compare;
import katana.rt.Decimal;
import katana.rt.Field;
import katana.rt.Move;

/** run_misc.at:12873, "Default Arithmetic (1)". */
final class RunMisc12873 implements Lowered {
    public String id() {
        return "run_misc-12873";
    }

    public void run(Out out) {
        Field a = Out.item(Attr.zoned(3, 0, false), "399"), b = Out.item(Attr.zoned(3, 0, false), "211"), c = Out.item(Attr.zoned(5, 2, false), "212.34");
        Field numv1 = Out.item(Attr.zoned(4, 1, false)), rslt = Out.item(Attr.zoned(3, 0, false));
        Field v1 = Out.edited("999.9", 1), v2 = Out.edited("999.99", 2);
        Calc d1 = new Calc(1), d0 = new Calc(0), d2 = new Calc(2);
        Arithmetic.store(rslt, d1.addLast(Calc.of(a), Calc.of("1.1"), false), false, false);
        out.display("Simple Compute  RSLT IS ", rslt);
        Arithmetic.store(rslt, reduced(d0, a, b), false, false);
        out.display("Single Variable RSLT IS ", rslt);
        Decimal.Term r = reduced(d2, a, b);
        Arithmetic.store(v2, r, false, false);
        Arithmetic.store(rslt, r, false, false);
        out.display("Compute  RSLT    IS ", rslt);
        out.display("Compute  RSLTv99 IS ", v2);
        r = reduced(d1, a, b);
        Arithmetic.store(v1, r, false, false);
        Arithmetic.store(rslt, r, false, false);
        out.display("Compute  RSLT    IS ", rslt);
        out.display("Compute  RSLTv9  IS ", v1);
        Move.move(Out.literal("0"), rslt);
        Arithmetic.store(rslt, d2.addLast(Calc.of(rslt), Calc.of(c), false), false, false);
        out.display("Add      RSLT    IS ", rslt);
        Move.move(Out.literal("0"), rslt);
        Arithmetic.store(rslt, d2.addLast(Calc.of(rslt), d2.add(d2.add(Calc.of(a), Calc.of(c)), Calc.of("10")), false), false, false);
        out.display("Add      RSLT    IS ", rslt);
        Arithmetic.store(rslt, d2.subtractLast(Calc.of(rslt), Calc.of(c), false), false, false);
        out.display("Subtract RSLT    IS ", rslt);
        Arithmetic.store(rslt, d0.subtractLast(Calc.of(rslt), d0.add(Calc.of(a), Calc.of("-10")), false), false, false);
        out.display("Subtract RSLT    IS ", rslt);
        Move.move(Out.literal("0"), rslt);
        Arithmetic.store(v1, d2.addLast(d2.add(Calc.of(a), Calc.of(c)), Calc.of(rslt), false), false, false);
        out.display("Add      RSLTv9  IS ", v1);
        Arithmetic.store(rslt, d2.multiplyLast(Calc.of(a), Calc.of(c), false), false, false);
        out.display("Multiply RSLT    IS ", rslt);
        // MULTIPLY RSLT BY NUM-C stores into NUM-C, the BY operand (LR p. 390); RSLT is unchanged.
        Arithmetic.store(c, d2.multiplyLast(Calc.of(rslt), Calc.of(c), false), false, false);
        out.display("Multiply RSLT    IS ", rslt);
        Arithmetic.store(rslt, d0.divideLast(Calc.of(a), Calc.of("10"), false), false, false);
        out.display("Divide   RSLT    IS ", rslt);
        Arithmetic.store(v1, d1.divideLast(Calc.of(rslt), Calc.of("4"), false), false, false);
        out.display("Divide   RSLTv9  IS ", v1);
        Arithmetic.store(rslt, d0.divideLast(Calc.of(rslt), Calc.of("4"), false), false, false);
        out.display("Divide   RSLT    IS ", rslt);
        r = reduced(d1, a, b);
        Arithmetic.store(v1, r, false, false);
        Arithmetic.store(rslt, r, false, false);
        out.display("Simple   RSLT    IS ", rslt, " RSLTv9  IS ", v1);
        Calc d3 = new Calc(3);
        Decimal.Term q1 = d3.divide(Calc.of(a), d3.add(Calc.of("100.55"), Calc.of("-0.550")));
        Decimal.Term q2 = d3.divide(Calc.of(b), d3.subtract(d3.multiply(Calc.of("10.11"), Calc.of("10")), Calc.of("1.1")));
        r = d3.multiplyLast(d3.subtract(q1, q2), d3.divide(Calc.of("220"), Calc.of("2.2")), false);
        Arithmetic.store(v1, r, false, false);
        Arithmetic.store(rslt, r, false, false);
        out.display("Complex  RSLT    IS ", rslt, " RSLTv9  IS ", v1);
        q1 = d1.divide(Calc.of(a), d1.subtract(Calc.of("101"), Calc.of("1")));
        q2 = d1.divide(Calc.of(b), d1.multiply(Calc.of("10"), Calc.of("10")));
        r = d1.multiplyLast(d1.subtract(q1, q2), d1.divide(Calc.of("200"), Calc.of("2")), false);
        Arithmetic.store(v1, r, false, false);
        Arithmetic.store(rslt, r, false, false);
        out.display("Reduced  RSLT    IS ", rslt, " RSLTv9  IS ", v1);
        Move.move(a, numv1);
        q1 = d1.divide(Calc.of(numv1), d1.subtract(Calc.of("101"), Calc.of("1")));
        q2 = d1.divide(Calc.of(b), d1.multiply(Calc.of("10"), Calc.of("10")));
        r = d1.multiply(d1.subtract(q1, q2), d1.divide(Calc.of("200"), Calc.of("2")));
        out.display(Compare.numbers(r.value(), Decimal.of("188")) == 0 ? "Not Using ARITHMETIC-OSVS" : "Using ARITHMETIC-OSVS");
    }

    /** ((NUM-A / 100) - (NUM-B / 100)) * 100, as the statement's dmax makes it. */
    private static Decimal.Term reduced(Calc c, Field a, Field b) {
        return c.multiplyLast(c.subtract(c.divide(Calc.of(a), Calc.of("100")), c.divide(Calc.of(b), Calc.of("100"))), Calc.of("100"), false);
    }
}
