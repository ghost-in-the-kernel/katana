package katana.rt.autotest;

import katana.rt.Attr;
import katana.rt.Field;
import katana.rt.Move;

/** run_fundamental.at:296, "MOVE to edited item (2)". */
final class RunFundamental296 implements Lowered {
    public String id() {
        return "run_fundamental-296";
    }

    public void run(Out out) {
        Field[] src = {Out.item(Attr.zoned(4, 2, true), "-0.06"), Out.item(Attr.zoned(4, 2, true), "-0.07"), Out.item(Attr.zoned(4, 2, true), "-0.08"),
            Out.item(Attr.zoned(4, 2, true), "-0.09"), Out.item(Attr.zoned(4, 2, true), "-1.10")};
        Field[] edt = {Out.edited("9999-", 0), Out.edited("9999+", 0), Out.edited("ZZZZ+", 0), Out.edited("9999DB", 0), Out.edited("9999DB", 0)};
        for (int k = 0; k < 5; k++) Move.move(src[k], edt[k]);
        for (Field e : edt) out.display(">", e, "<");
    }
}
