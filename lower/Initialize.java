package katana.lower;

import java.util.List;
import katana.sir.types.Attributes;
import katana.sir.types.Item;
import katana.sir.types.Statement;
import katana.sir.types.Value;

/**
 * INITIALIZE as IBM's implicit MOVEs (LR 6.4 pp. 351-352): every elementary item of the target, each
 * occurrence, FILLER items and those under a REDEFINES left out; without REPLACING, numeric and
 * numeric-edited given ZERO and the others SPACE; with it, only the items of the categories it names,
 * each given its category's sender. WITH FILLER, VALUE and DEFAULT are not in the grammar (COBOL 85's).
 */
final class Initialize {
    private Initialize() {}

    /** INITIALIZE: each elementary item set to its category's value, or REPLACING's; a REPLACING category not lowered stops the program where it runs. */
    // Decision: R-019
    static void statement(Flow f, Statement s) {
        java.util.Map<Attributes.Category, Value> replacing = replacing(s);
        if (replacing == null) {
            f.e.line(f.x.unsupported("INITIALIZE REPLACING a category not lowered at " + s.origin()) + ";");
            return;
        }
        for (Value v : Verbs.operands(s, "initialize-statement")) {
            Value.Field target = (Value.Field) v;
            if (!target.subscripts().isEmpty() || target.start() != null) {
                f.e.line(f.x.unsupported("INITIALIZE of a subscripted or reference-modified item at " + s.origin()) + ";");
                continue;
            }
            for (Item i : f.p.items()) {
                if (!receives(f, i, target.item())) continue;
                if (replacing.isEmpty()) move(f, i, f.x.item(target.item()));
                else if (replacing.containsKey(i.attributes().category())) replace(f, i, replacing.get(i.attributes().category()), f.x.item(target.item()));
            }
        }
    }

    /** REPLACING's categories, each with its sender; empty without the phrase; null for a category the lowering does not hold (DBCS, national). */
    private static java.util.Map<Attributes.Category, Value> replacing(Statement s) {
        java.util.Map<Attributes.Category, Value> out = new java.util.LinkedHashMap<>();
        String category = null;
        for (Statement.Operand o : s.operands()) {
            if (!o.role().equals("initialize-replacing-by")) continue;
            if (o.value() instanceof Value.Name n && n.kind().equals("keyword")) {
                if (!n.text().equals("BY") && !n.text().equals("DATA")) category = n.text();
                continue;
            }
            Attributes.Category c = switch (category == null ? "" : category) {
                case "ALPHABETIC" -> Attributes.Category.ALPHABETIC;
                case "ALPHANUMERIC" -> Attributes.Category.ALPHANUMERIC;
                case "ALPHANUMERIC-EDITED" -> Attributes.Category.ALPHANUMERIC_EDITED;
                case "NUMERIC" -> Attributes.Category.NUMERIC;
                case "NUMERIC-EDITED" -> Attributes.Category.NUMERIC_EDITED;
                default -> null;
            };
            if (c == null) return null;
            out.put(c, o.value());
        }
        return out;
    }

    /** One receiver given REPLACING's sender for its category, each occurrence. */
    private static void replace(Flow f, Item i, Value from, Item scope) {
        String each = f.x.occurrences(i, scope), target = each == null ? f.x.slide.where(i, scope) : "t";
        String move = f.x.moveInto(from, target, i);
        f.e.line(each == null ? move + ";" : each + "t -> " + move + ");");
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

    private static void move(Flow f, Item i, Item scope) {
        boolean zero = List.of(Attributes.Category.NUMERIC, Attributes.Category.NUMERIC_EDITED).contains(i.attributes().category());
        String each = f.x.occurrences(i, scope), target = each == null ? f.x.slide.where(i, scope) : "t";
        String move = zero ? "Move.move(Literal.number(\"0\", 0, cp), " + target + ")" : "Move.fill(" + target + ", new byte[] {Literal.figurative(\"SPACE\", cp)}, false)";
        f.e.line(each == null ? move + ";" : each + "t -> " + move + ");");
    }
}
