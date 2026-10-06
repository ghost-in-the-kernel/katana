package katana.rt.autotest;

import katana.rt.Arithmetic;
import katana.rt.Attr;
import katana.rt.Field;

/** run_misc.at:156, "DECIMAL-POINT is COMMA (5)". */
final class RunMisc156 implements Lowered {
    public String id() {
        return "run_misc-156";
    }

    public void run(Out out) {
        Field x = Out.item(Attr.zoned(4, 2, false));
        Calc c = new Calc(2);
        Arithmetic.store(x, c.addLast(Calc.of("1"), Calc.of("0.1"), false), false, false);
        out.display(x);
        Arithmetic.store(x, c.multiplyLast(Calc.of("1"), Calc.of("0.1"), false), false, false);
        out.display(x);
    }
}
