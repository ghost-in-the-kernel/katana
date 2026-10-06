package katana.rt.autotest;

import katana.rt.Attr;
import katana.rt.Edit;
import katana.rt.Field;
import katana.rt.Move;

/** run_misc.at:190, "CURRENCY SIGN". */
final class RunMisc190 implements Lowered {
    public String id() {
        return "run_misc-190";
    }

    public void run(Out out) {
        // PIC Y(6)9.99 with CURRENCY SIGN IS "Y": the mask's currency positions written `$`, shown as Y.
        Field amount = Out.item(Attr.numericEdited(new Edit("$$$$$$9.99", 'Y', false, false), 8, 2));
        Move.move(Out.literal("1512.34"), amount);
        out.noAdvancing("Amount is #", amount, "#");
    }
}
