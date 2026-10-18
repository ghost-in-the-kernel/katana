package katana.intent.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import katana.intent.types.Evidence;
import katana.intent.types.Fact;
import katana.intent.types.Kind;
import katana.intent.types.Node;
import katana.intent.types.State;
import katana.intent.types.Subject;
import katana.sir.types.Attributes;
import katana.sir.types.Item;
import katana.sir.types.Program;
import katana.sir.types.Statement;

/**
 * `money.used` (R-065): a numeric item of two places an ADD, SUBTRACT or COMPUTE stores into with another
 * two-place item among its operands, or one MOVEd to an edited picture with a currency sign or comma
 * insertion, and never multiplied by itself, is money. Its version 1, `money.packed-v99-added`, read COMP-3,
 * a packing, as the meaning (J-195).
 */
final class MoneyRule implements Rule {
    private static final Set<String> STORED = Set.of("add-to", "add-giving", "subtract-minuend", "subtract-giving", "compute-store");

    public String name() {
        return "money.used";
    }

    public int version() {
        return 2;
    }

    // Unsure: 0.9, under the 1.000 recorded on CardDemo's key from 29 facts (research §32): a small sample, one corpus.
    public double confidence() {
        return 0.9;
    }

    /** Each numeric two-place item stored into with another, or MOVEd to a currency-edited picture, and none multiplies by itself. */
    public List<Fact> infer(Program p) {
        List<Statement> all = Walk.statements(p);
        List<Fact> facts = new ArrayList<>();
        for (Item i : p.items()) {
            Attributes a = i.attributes();
            if (!twoPlaces(i) || squared(all, i.id()) || !(stored(p, all, i.id()) || edited(p, all, i.id()))) continue;
            facts.add(new Fact(Kind.MONEY, new Subject.Datum(i.id()), new Evidence(name(), version(), List.of(new Node.ItemNode(i.id()))), confidence(), State.INFERRED));
        }
        return facts;
    }

    private static boolean twoPlaces(Item i) {
        return i.attributes().category() == Attributes.Category.NUMERIC && i.attributes().scale() == 2;
    }

    /** Whether an ADD, SUBTRACT or COMPUTE stores into the item with another two-place item among its operands. */
    private static boolean stored(Program p, List<Statement> all, int item) {
        for (Statement s : all) {
            if (s.operands().stream().noneMatch(o -> STORED.contains(o.role()) && Walk.refers(o.value(), item) > 0)) continue;
            for (Item other : p.items()) if (other.id() != item && twoPlaces(other) && s.operands().stream().anyMatch(o -> Walk.refers(o.value(), other.id()) > 0)) return true;
        }
        return false;
    }

    /** Whether a MOVE sends the item to an edited picture with a currency sign or comma insertion. */
    private static boolean edited(Program p, List<Statement> all, int item) {
        for (Statement s : all) {
            if (!s.verb().equals("MOVE") || s.operands().stream().noneMatch(o -> o.role().equals("move-to-sending-area") && Walk.refers(o.value(), item) > 0)) continue;
            for (Statement.Operand o : s.operands()) {
                if (!o.role().equals("move-to-statement") || !(o.value() instanceof katana.sir.types.Value.Field f)) continue;
                Attributes to = p.items().get(f.item() - 1).attributes();
                if (to.category() == Attributes.Category.NUMERIC_EDITED && (to.picture().contains(p.header().currency()) || to.picture().contains(","))) return true;
            }
        }
        return false;
    }

    /** Whether the item is multiplied by itself: twice among a MULTIPLY's operands, or on both sides of a `*` or as the base of a `**`. */
    private static boolean squared(List<Statement> all, int item) {
        for (Statement s : all) {
            if (s.verb().equals("MULTIPLY") && s.operands().stream().mapToInt(o -> Walk.refers(o.value(), item)).sum() > 1) return true;
            for (Statement.Operand o : s.operands()) if (squared(o.value(), item)) return true;
        }
        return false;
    }

    private static boolean squared(katana.sir.types.Value v, int item) {
        if (!(v instanceof katana.sir.types.Value.Expression e)) return false;
        if (e.operator().equals("*") && e.operands().stream().allMatch(o -> Walk.refers(o, item) > 0)) return true;
        if (e.operator().equals("**") && Walk.refers(e.operands().get(0), item) > 0) return true;
        return e.operands().stream().anyMatch(o -> squared(o, item));
    }
}
