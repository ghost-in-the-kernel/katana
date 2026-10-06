package katana.rt.autotest;

import katana.rt.Attr;
import katana.rt.Compare;
import katana.rt.Decimal;
import katana.rt.Field;

/** run_fundamental.at:8846, "condition IS ZERO AND". */
final class RunFundamental8846 implements Lowered {
    public String id() {
        return "run_fundamental-8846";
    }

    public void run(Out out) {
        Field v = Out.item(Attr.zoned(1, 0, false), "0"), w = Out.item(Attr.zoned(1, 0, false), "1");
        Decimal zero = Decimal.of("0"), one = Decimal.of("1");
        boolean vZero = Compare.numbers(v.get(), zero) == 0, wOne = Compare.numbers(w.get(), one) == 0;
        boolean wPositive = Compare.numbers(w.get(), zero) > 0, wNegative = Compare.numbers(w.get(), zero) < 0, vEqual0 = Compare.numbers(v.get(), zero) == 0;
        if (vZero) out.display("V IS ZERO");
        if (vZero && wOne) out.display("V IS ZERO AND W EQUAL 1");
        if (wOne && vZero) out.display("W EQUAL 1 AND V IS ZERO");
        if (wPositive) out.display("W IS POSITIVE");
        if (wNegative) out.display("W IS NEGATIVE");
        if (wPositive && vEqual0) out.display("W IS POSITIVE AND V EQUAL 0");
        if (vEqual0 && wPositive) out.display("V EQUAL 0 AND W IS POSITIVE");
    }
}
