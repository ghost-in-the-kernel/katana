package katana.rt.autotest;

import katana.rt.ArithProfile;
import katana.rt.Arithmetic;
import katana.rt.Attr;
import katana.rt.Decimal;
import katana.rt.Field;
import katana.rt.Move;

/** run_misc.at:7348, "PICTURE with Edit mask". */
final class RunMisc7348 implements Lowered {
    public String id() {
        return "run_misc-7348";
    }

    public void run(Out out) {
        Field cost = Out.item(Attr.zoned(8, 3, false), "18536.232");
        Field prod = Out.edited("$$$,$$9.99", 2), wflt = Out.edited("$$$,$$9.99", 2);
        Move.move(Out.literal("18536.23"), wflt);
        out.display("WFLT IS ", wflt);
        ArithProfile p = ArithProfile.IBM_COMPAT;
        Decimal.Term product = Decimal.multiply(p, Decimal.operand(cost.get(), 5), Decimal.operand(Decimal.of("1"), 1), 3, true, true);
        Arithmetic.store(prod, product, true, false);
        out.display("COST IS ", prod);
    }
}
