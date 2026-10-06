package katana.rt.autotest;

import katana.rt.Attr;
import katana.rt.Field;

/** run_fundamental.at:4521, "Separate sign positions (1)". */
final class RunFundamental4521 implements Lowered {
    public String id() {
        return "run_fundamental-4521";
    }

    public void run(Out out) {
        Field x = Out.item(new Attr(Attr.Usage.DISPLAY, Attr.Category.NUMERIC, 1, 0, true, true, true, false, null), "-1");
        Field y = Out.item(new Attr(Attr.Usage.DISPLAY, Attr.Category.NUMERIC, 1, 0, true, true, false, false, null), "-1");
        out.noAdvancing(Out.ref(x, 1, 1), Out.ref(x, 2, 1));
        out.noAdvancing(Out.ref(y, 1, 1), Out.ref(y, 2, 1));
    }
}
