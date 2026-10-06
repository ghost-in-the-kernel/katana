package katana.rt.autotest;

import katana.rt.Attr;

/** run_fundamental.at:146, "DISPLAY data items with VALUE clause". */
final class RunFundamental146 implements Lowered {
    public String id() {
        return "run_fundamental-146";
    }

    public void run(Out out) {
        out.display(Out.text(3, false, "abc"));
        out.display(Out.item(Attr.zoned(3, 0, false), "123"));
        out.display(Out.item(Attr.zoned(3, 0, true), "123"));
        out.display(Out.item(Attr.zoned(3, 0, true), "-123"));
        out.display(Out.item(Attr.zoned(3, 1, false), "12.3"));
        out.display(Out.item(Attr.zoned(3, 1, true), "12.3"));
        out.display(Out.item(Attr.zoned(3, 1, true), "-12.3"));
    }
}
