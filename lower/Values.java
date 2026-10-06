package katana.lower;

import java.util.List;
import katana.sir.types.Attributes;
import katana.sir.types.Item;
import katana.sir.types.Program;

/**
 * Each item's value when the program starts: its VALUE, MOVEd as the clause says, in every occurrence;
 * an elementary item without one as INITIALIZE sets it (zero, or spaces), as cobc's ibm dialect starts it.
 */
final class Values {
    private Values() {}

    // Decision: R-019
    // Unsure: IBM leaves an item without VALUE unpredictable; this starts it as cobc -std=ibm does, zero or spaces (J-114).
    static void initial(Emitter e, Program p, Item i, Exprs x) {
        if (governed(p, i)) return;
        boolean group = i.attributes().category() == Attributes.Category.GROUP;
        if (i.value() == null && (group || isText(i))) return;
        String each = x.occurrences(i), target = each == null ? "f" + i.id() : "t";
        String move = i.value() == null ? "Move.move(Literal.number(\"0\", 0, cp), " + target + ")" : x.moveInto(i.value(), target, i);
        e.line(each == null ? move + ";" : each + "t -> " + move + ");");
    }

    /** Whether something above sets the item: a group with a VALUE, or a REDEFINES (its bytes are another item's). */
    private static boolean governed(Program p, Item i) {
        for (Item a = i; a != null; a = a.parent() == 0 ? null : p.items().get(a.parent() - 1)) {
            if (a.redefines() != 0) return true;
            if (a != i && a.value() != null) return true;
        }
        return false;
    }

    private static boolean isText(Item i) {
        return List.of(Attributes.Category.ALPHANUMERIC, Attributes.Category.ALPHABETIC, Attributes.Category.ALPHANUMERIC_EDITED).contains(i.attributes().category());
    }
}
