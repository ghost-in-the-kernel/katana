package katana.rt.autotest;

import katana.rt.Field;

/** run_refmod.at:25, "Static reference-modification". */
final class RunRefmod25 implements Lowered {
    public String id() {
        return "run_refmod-25";
    }

    public void run(Out out) {
        Field x = Out.text(4, false, "abcd");
        out.display(Out.ref(x, 1, 1), ":", Out.ref(x, 1, 2), ":", Out.ref(x, 1, 3), ":", Out.ref(x, 1, 4), ":", Out.ref(x, 1, 4));
        out.display(Out.ref(x, 2, 1), ":", Out.ref(x, 2, 2), ":", Out.ref(x, 2, 3), ":", Out.ref(x, 2, 3));
        out.display(Out.ref(x, 3, 1), ":", Out.ref(x, 3, 2), ":", Out.ref(x, 3, 2));
        out.display(Out.ref(x, 4, 1), ":", Out.ref(x, 4, 1));
    }
}
