package katana.lower;

import java.util.List;
import katana.sir.types.Attributes;
import katana.sir.types.Item;
import katana.sir.types.Statement;
import katana.sir.types.Value;

/**
 * INITIALIZE as IBM's implicit MOVEs (LR 6.4 p. 352): every elementary item of the target, each
 * occurrence, numeric and numeric-edited given ZERO, the others SPACE; FILLER items and those under a
 * REDEFINES left out. REPLACING, WITH FILLER and VALUE are not lowered yet.
 */
final class Initialize {
    private Initialize() {}

    // Decision: R-019
    static void statement(Flow f, Statement s) {
        if (s.operands().stream().anyMatch(o -> !o.role().equals("initialize-statement"))) {
            f.e.line(f.x.unsupported("INITIALIZE with REPLACING, FILLER or VALUE at " + s.origin()) + ";");
            return;
        }
        for (Value v : Verbs.operands(s, "initialize-statement")) {
            Value.Field target = (Value.Field) v;
            if (!target.subscripts().isEmpty() || target.start() != null) {
                f.e.line(f.x.unsupported("INITIALIZE of a subscripted or reference-modified item at " + s.origin()) + ";");
                continue;
            }
            for (Item i : f.p.items()) if (receives(f, i, target.item())) move(f, i);
        }
    }

    /** Whether an item is one of the target's receivers: elementary, the target or under it, not FILLER, not under a REDEFINES below the target. */
    private static boolean receives(Flow f, Item i, int target) {
        if (i.attributes().category() == Attributes.Category.GROUP || (i.name().equals("FILLER") && i.id() != target)) return false;
        for (Item a = i; a != null; a = a.parent() == 0 ? null : f.x.item(a.parent())) {
            if (a.id() == target) return true;
            if (a.redefines() != 0) return false;
        }
        return false;
    }

    private static void move(Flow f, Item i) {
        boolean zero = List.of(Attributes.Category.NUMERIC, Attributes.Category.NUMERIC_EDITED).contains(i.attributes().category());
        String each = f.x.occurrences(i), target = each == null ? "f" + i.id() : "t";
        String move = zero ? "Move.move(Literal.number(\"0\", 0, cp), " + target + ")" : "Move.fill(" + target + ", new byte[] {Literal.figurative(\"SPACE\", cp)}, false)";
        f.e.line(each == null ? move + ";" : each + "t -> " + move + ");");
    }
}
