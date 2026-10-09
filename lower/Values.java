package katana.lower;

import java.util.List;
import katana.sir.types.Attributes;
import katana.sir.types.Item;
import katana.sir.types.Program;

/**
 * Each item's value when the program starts: its VALUE, MOVEd as the clause says, in every occurrence;
 * an elementary item without one as INITIALIZE sets it (zero, or spaces), as cobc's ibm dialect starts it
 * (R-057). A VALUE is placed as if the item were not JUSTIFIED (LR 6.4 p. 245: "Initialization is
 * independent of any BLANK WHEN ZERO or JUSTIFIED clause"), and on an edited item as written.
 */
final class Values {
    private Values() {}

    /** An item's initial value: its VALUE (placed as written in an edited item), else zero for a numeric elementary item, nothing for text or a group (R-057). */
    // Decision: R-057
    static void initial(Emitter e, Program p, Item i, Exprs x) {
        if (governed(p, i)) return;
        boolean group = i.attributes().category() == Attributes.Category.GROUP;
        if (i.value() == null && (group || isText(i))) return;
        String each = x.occurrences(i), target = each == null ? "f" + i.id() : "t";
        if (i.flags().contains(Item.Flag.JUSTIFIED)) target = "Table.ref(" + target + ", 1, 0)";
        String move = i.value() == null ? "Move.move(Literal.number(\"0\", 0, cp), " + target + ")" : edited(i) ? placed(i.value(), target, x) : x.moveInto(i.value(), target, i);
        e.line(each == null ? move + ";" : each + "t -> " + move + ");");
    }

    /** Whether something else sets the item: a group with a VALUE, a REDEFINES, or a RENAMES (a 66 names bytes other items hold). */
    private static boolean governed(Program p, Item i) {
        if (i.level() == 66) return true;
        for (Item a = i; a != null; a = a.parent() == 0 ? null : p.items().get(a.parent() - 1)) {
            if (a.redefines() != 0) return true;
            if (a != i && a.value() != null) return true;
        }
        return false;
    }

    private static boolean edited(Item i) {
        return List.of(Attributes.Category.NUMERIC_EDITED, Attributes.Category.ALPHANUMERIC_EDITED).contains(i.attributes().category());
    }

    /** A VALUE on an edited item: its literal "aligned according to the alphanumeric alignment rules" (LR 6.4 p. 245), no editing. */
    // Decision: R-057
    private static String placed(katana.sir.types.Value v, String target, Exprs x) {
        String pattern = x.pattern(v);
        if (pattern != null) return "Move.place(" + target + ", " + pattern + ")";
        return "Move.group(" + x.sending(v) + ", " + target + ")";
    }

    private static boolean isText(Item i) {
        return List.of(Attributes.Category.ALPHANUMERIC, Attributes.Category.ALPHABETIC, Attributes.Category.ALPHANUMERIC_EDITED).contains(i.attributes().category());
    }
}
