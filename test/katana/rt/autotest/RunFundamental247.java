package katana.rt.autotest;

import katana.rt.Attr;
import katana.rt.Field;
import katana.rt.Move;

/** run_fundamental.at:247, "MOVE to edited item (1)". */
final class RunFundamental247 implements Lowered {
    public String id() {
        return "run_fundamental-247";
    }

    public void run(Out out) {
        Field[] src = {Out.item(Attr.zoned(4, 2, true), "1.10"), Out.item(Attr.zoned(4, 2, true), "0.02"), Out.item(Attr.zoned(4, 2, true), "-0.03"),
            Out.item(Attr.zoned(4, 2, true), "-0.04"), Out.item(Attr.zoned(4, 2, true), "-0.05")};
        Field[] edt = {Out.edited("----9", 0), Out.edited("----9", 0), Out.edited("----9", 0), Out.edited("++++9", 0), Out.edited("-----", 0)};
        for (int k = 0; k < 5; k++) Move.move(src[k], edt[k]);
        for (Field e : edt) out.display(">", e, "<");
    }
}
