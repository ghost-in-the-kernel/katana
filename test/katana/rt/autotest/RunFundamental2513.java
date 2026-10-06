package katana.rt.autotest;

import katana.rt.Attr;
import katana.rt.Field;
import katana.rt.Move;

/** run_fundamental.at:2513, "MOVE to item with +, -, B, 0, / and ,". */
final class RunFundamental2513 implements Lowered {
    public String id() {
        return "run_fundamental-2513";
    }

    public void run(Out out) {
        Field p4 = Out.item(Attr.zoned(1, 0, true), "4"), m4 = Out.item(Attr.zoned(1, 0, true), "-4");
        Field p123 = Out.item(Attr.zoned(3, 0, true), "123"), m123 = Out.item(Attr.zoned(3, 0, true), "-123");
        Field wm = Out.edited("-B-", 0), wp = Out.edited("+B+", 0), mm = Out.edited("---B--9", 0), pp = Out.edited("+++B++9", 0), wx = Out.edited("-//00BB,,-", 0);
        Field[][] moves = {{p4, wm}, {m4, wm}, {p4, wp}, {m4, wp}, {p123, mm}, {m123, mm}, {p123, pp}, {m123, pp}, {p4, wx}, {m4, wx}, {p123, wx}, {m123, wx}};
        for (Field[] m : moves) {
            Move.move(m[0], m[1]);
            out.display(m[1]);
        }
    }
}
