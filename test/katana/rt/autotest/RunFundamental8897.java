package katana.rt.autotest;

import katana.rt.Collation;
import katana.rt.Compare;
import katana.rt.Field;

/** run_fundamental.at:8897, "abbreviated conditions". */
final class RunFundamental8897 implements Lowered {
    public String id() {
        return "run_fundamental-8897";
    }

    public void run(Out out) {
        // Each abbreviated relation expanded as IBM's text gives it (LR "Abbreviated combined relation conditions"): the subject and relation carried to each object.
        Field[] v = {Out.text(16, false, "#0001"), Out.text(16, false, "#0002"), Out.text(16, false, "#0003"), Out.text(16, false, "#0004")};
        int c2 = cmp(v[0], v[1]), c3 = cmp(v[0], v[2]), c4 = cmp(v[0], v[3]);
        if (c2 == 0 && c3 == 0 && c4 == 0) out.display("TRUE 1");
        if (c2 != 0 && c3 != 0 && c4 != 0) out.display("TRUE 2");
        if (c2 <= 0 && c3 <= 0 && c4 <= 0) out.display("TRUE 3");
    }

    private static int cmp(Field a, Field b) {
        return Compare.text(a.record().bytes(), a.offset(), a.attr().length(), b.record().bytes(), b.offset(), b.attr().length(), Out.CP.character(' '), Collation.nativeOrder());
    }
}
