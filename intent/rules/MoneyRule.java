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

/** `money.packed-v99-added`: a `V99 COMP-3` item an ADD or SUBTRACT stores into, and never multiplied by itself, is money (docs/ROADMAP.md 5.2). */
final class MoneyRule implements Rule {
    private static final Set<String> STORED = Set.of("add-to", "add-giving", "subtract-minuend", "subtract-giving");

    public String name() {
        return "money.packed-v99-added";
    }

    public int version() {
        return 1;
    }

    // Unsure: 0.5 though its precision is 1.000, from one fact (CBSTM03A's WS-TOTAL-AMT); CardDemo's other COMP-3 amounts are only MOVEd, its money zoned (research §29).
    public double confidence() {
        return 0.5;
    }

    public List<Fact> infer(Program p) {
        List<Statement> all = Walk.statements(p);
        List<Fact> facts = new ArrayList<>();
        for (Item i : p.items()) {
            Attributes a = i.attributes();
            if (a.category() != Attributes.Category.NUMERIC || a.scale() != 2 || a.usage() != Attributes.Usage.PACKED) continue;
            if (!stored(all, i.id()) || squared(all, i.id())) continue;
            facts.add(new Fact(Kind.MONEY, new Subject.Datum(i.id()), new Evidence(name(), version(), List.of(new Node.ItemNode(i.id()))), confidence(), State.INFERRED));
        }
        return facts;
    }

    /** Whether an ADD or SUBTRACT stores into the item. */
    private static boolean stored(List<Statement> all, int item) {
        for (Statement s : all) for (Statement.Operand o : s.operands()) if (STORED.contains(o.role()) && Walk.refers(o.value(), item) > 0) return true;
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
