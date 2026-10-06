package katana.rt.autotest;

import katana.rt.Attr;
import katana.rt.Field;
import katana.rt.Move;

/** run_refmod.at:57, "Dynamic reference-modification". */
final class RunRefmod57 implements Lowered {
    public String id() {
        return "run_refmod-57";
    }

    public void run(Out out) {
        Field x = Out.text(4, false, "abcd"), i = Out.item(Attr.zoned(1, 0, false));
        String[][] steps = {{"1", "start"}, {"4", "start"}, {"1", "length"}, {"4", "length"}};
        for (String[] s : steps) {
            Move.move(Out.literal(s[0]), i);
            int n = i.get().unscaled().intValue();
            out.display(s[1].equals("start") ? Out.ref(x, n, 1) : Out.ref(x, 1, n));
        }
    }
}
