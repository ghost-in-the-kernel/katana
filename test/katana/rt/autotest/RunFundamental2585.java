package katana.rt.autotest;

import katana.rt.Attr;
import katana.rt.Field;
import katana.rt.Move;

/** run_fundamental.at:2585, "MOVE to shorter edited item (truncation)". */
final class RunFundamental2585 implements Lowered {
    public String id() {
        return "run_fundamental-2585";
    }

    public void run(Out out) {
        Field src1 = Out.item(Attr.zoned(5, 2, false), "123.45"), src2 = Out.item(Attr.zoned(5, 1, false), "1234.5"), src3 = Out.item(Attr.zoned(5, 0, false), "12345");
        Field dst1 = Out.edited("ZZZ", 0), dst2 = Out.edited("ZZZZZ", 0);
        for (Field src : new Field[] {src1, src2, src3}) {
            Move.move(src, dst1);
            out.display(dst1);
        }
        Move.move(src1, dst2);
        out.display(dst2);
        Move.move(src2, dst2);
        out.display(dst2);
        Move.move(src3, dst2);
        out.noAdvancing(dst2);
    }
}
