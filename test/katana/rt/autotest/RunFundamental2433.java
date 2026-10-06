package katana.rt.autotest;

import katana.rt.Attr;
import katana.rt.Field;
import katana.rt.Move;

/** run_fundamental.at:2433, "MOVE to item with simple and floating insertion". */
final class RunFundamental2433 implements Lowered {
    public String id() {
        return "run_fundamental-2433";
    }

    public void run(Out out) {
        Field n1 = Out.edited("-*B*99", 0), n2 = Out.edited("$BB**,***.**", 2), n3 = Out.edited("$BB--,---.--", 2), n4 = Out.edited("$,$$$,$$9.99CR", 2), n5 = Out.edited("--,---,--9", 0);
        Field packed = Out.item(Attr.numeric(Attr.Usage.PACKED, 12, 3, true), "0");
        Object[][] direct = {{"-123", n1}, {"1234.56", n2}, {"1234.56", n3}, {"456789", n4}, {"-100", n5}, {"-1000", n5}};
        for (Object[] m : direct) {
            Move.move(Out.literal((String) m[0]), (Field) m[1]);
            out.display(">", m[1], "<");
        }
        Object[][] viaPacked = {{"-123", n1}, {"1234.56", n2}, {"1234.56", n3}, {"123456789", n4}, {"-100", n5}, {"-1000", n5}};
        for (Object[] m : viaPacked) {
            Move.move(Out.literal((String) m[0]), packed);
            Move.move(packed, (Field) m[1]);
            out.display(">", m[1], "<");
        }
    }
}
