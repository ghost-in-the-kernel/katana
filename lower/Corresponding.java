package katana.lower;

import java.util.ArrayList;
import java.util.List;
import katana.sir.types.Attributes;
import katana.sir.types.Item;
import katana.sir.types.Value;

/**
 * The CORRESPONDING phrase's pairs (LR 6.4 p. 295), found from the data map: two subordinate items
 * correspond when they have the same name and the same qualifiers up to the two groups named, neither
 * FILLER, a 66, an index, a pointer, or described with REDEFINES or OCCURS; for MOVE at least one is
 * elementary (two groups are looked into instead), for ADD and SUBTRACT both are elementary numeric.
 * Each pair is subscripted as the groups named are.
 */
final class Corresponding {
    private Corresponding() {}

    // Decision: R-019
    // Unsure: an item under one with REDEFINES or OCCURS is left out with it; IBM names only the items described with those clauses, and such an item could not be referred to without that item's subscript anyway (J-123). Stays: IBM's text names no such item; an IBM run of CORRESPONDING over one settles it.
    static List<Value.Field[]> pairs(Exprs x, Value.Field from, Value.Field to, boolean numeric) {
        List<Value.Field[]> out = new ArrayList<>();
        walk(x, from, to, numeric, out);
        return out;
    }

    /** One level of the two groups: a name in both, looked into while both are groups, else a pair when the verb allows it. */
    private static void walk(Exprs x, Value.Field from, Value.Field to, boolean numeric, List<Value.Field[]> out) {
        List<Item> receivers = children(x, to.item());
        for (Item s : children(x, from.item())) {
            for (Item r : receivers) {
                if (!r.name().equals(s.name())) continue;
                Value.Field sf = new Value.Field(s.id(), from.subscripts(), null, null), rf = new Value.Field(r.id(), to.subscripts(), null, null);
                boolean sGroup = group(s), rGroup = group(r);
                if (sGroup && rGroup) walk(x, sf, rf, numeric, out);
                else if (!numeric || (!sGroup && !rGroup && number(s) && number(r))) out.add(new Value.Field[] {sf, rf});
                break;
            }
        }
    }

    /** A group's children that may correspond, in textual order. */
    private static List<Item> children(Exprs x, int parent) {
        List<Item> out = new ArrayList<>();
        for (Item i : x.p.items()) if (i.parent() == parent && eligible(i)) out.add(i);
        return out;
    }

    private static boolean eligible(Item i) {
        Attributes a = i.attributes();
        boolean excluded = i.name().equals("FILLER") || i.level() == 66 || i.redefines() != 0 || i.occurs() != null
                || a.category() == Attributes.Category.INDEX || a.category() == Attributes.Category.POINTER;
        return !excluded;
    }

    private static boolean group(Item i) {
        return i.attributes().category() == Attributes.Category.GROUP;
    }

    private static boolean number(Item i) {
        return i.attributes().category() == Attributes.Category.NUMERIC;
    }
}
