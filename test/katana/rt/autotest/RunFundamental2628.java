package katana.rt.autotest;

import katana.rt.Attr;
import katana.rt.Field;
import katana.rt.Move;

/** run_fundamental.at:2628, "MOVE to JUSTIFIED item": numeric integers of each usage, and P-scaled ones, into a JUSTIFIED RIGHT X(7). */
final class RunFundamental2628 implements Lowered {
    public String id() {
        return "run_fundamental-2628";
    }

    public void run(Out out) {
        Field src1 = Out.item(Attr.zoned(4, 0, true), "11");
        Field src2 = Out.item(Attr.numeric(Attr.Usage.BINARY, 4, 0, true), "22");
        Field src3 = Out.item(Attr.numeric(Attr.Usage.NATIVE_BINARY, 4, 0, true), "33");
        Field src4 = Out.item(Attr.zoned(4, -2, true), "4400");
        Field src5 = Out.item(Attr.zoned(4, -5, true), "55500000");
        Field edt = Out.item(Attr.text(7, true));
        for (Field src : new Field[] {src1, src2, src3, src4, src5}) {
            Move.move(src, edt);
            out.display(">", edt, "<");
        }
    }
}
