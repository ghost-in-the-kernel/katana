package katana.rt.autotest;

import katana.rt.Compare;
import katana.rt.Decimal;

/** run_misc.at:1029, "Index and parenthesized expression". */
final class RunMisc1029 implements Lowered {
    public String id() {
        return "run_misc-1029";
    }

    public void run(Out out) {
        // An index's occurrence number compared with itself plus 2: true whatever the index holds.
        long i = 1;
        if (Compare.numbers(Decimal.of(Long.toString(i)), Decimal.of(Long.toString(i + 2))) < 0) out.noAdvancing("OK");
    }
}
