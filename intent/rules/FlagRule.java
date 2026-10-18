package katana.intent.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import katana.intent.types.Evidence;
import katana.intent.types.Fact;
import katana.intent.types.Kind;
import katana.intent.types.Node;
import katana.intent.types.State;
import katana.intent.types.Subject;
import katana.sir.types.Item;
import katana.sir.types.Program;
import katana.sir.types.Statement;
import katana.sir.types.Value;

/**
 * `flag.yes-no` (R-065): an item that is a yes-or-no. Two condition-names whose values, SPACE and LOW-VALUE
 * set aside as the nothing state, are Y and N or 0 and 1, or one of which is only the nothing state; or one
 * condition-name, the item also compared with or set to SPACE or LOW-VALUE. Run before enum, which leaves
 * what this takes.
 */
final class FlagRule implements Rule {
    private static final Set<String> NOTHING = Set.of("SPACE", "LOW-VALUE");
    private static final List<Set<String>> PAIRS = List.of(Set.of("tY", "tN"), Set.of("t0", "t1"), Set.of("n0", "n1"));

    public String name() {
        return "flag.yes-no";
    }

    public int version() {
        return 1;
    }

    // Unsure: 0.9, under the 0.986 recorded on CardDemo's key (research §32); it misses a switch no statement sets to SPACE or LOW-VALUE.
    public double confidence() {
        return 0.9;
    }

    /** Each item read as a yes-or-no, citing the item. */
    public List<Fact> infer(Program p) {
        List<Statement> all = Walk.statements(p);
        List<Fact> facts = new ArrayList<>();
        for (Item i : p.items()) {
            if (!flag(i, all)) continue;
            facts.add(new Fact(Kind.FLAG, new Subject.Datum(i.id()), new Evidence(name(), version(), List.of(new Node.ItemNode(i.id()))), confidence(), State.INFERRED));
        }
        return facts;
    }

    /** Whether an item reads as a yes-or-no, by its condition-names and what the program does with it. */
    static boolean flag(Item i, List<Statement> all) {
        if (i.conditions().size() == 1) return nothingUsed(all, i.id());
        if (i.conditions().size() != 2) return false;
        Set<String> a = keys(i.conditions().get(0)), b = keys(i.conditions().get(1));
        if (a == null || b == null || a.isEmpty() || b.isEmpty()) return false;
        Set<String> shared = new HashSet<>(a);
        shared.retainAll(b);
        if (!shared.isEmpty()) return false;
        if (NOTHING.containsAll(a) || NOTHING.containsAll(b)) return true;
        a.removeAll(NOTHING);
        b.removeAll(NOTHING);
        return a.size() == 1 && b.size() == 1 && PAIRS.contains(Set.of(a.iterator().next(), b.iterator().next()));
    }

    /** A condition-name's values as enum.disjoint-88s compares them, or null with a range among them. */
    private static Set<String> keys(Item.Condition c) {
        Set<String> keys = new HashSet<>();
        for (Value v : c.values()) {
            String k = EnumRule.key(v);
            if (k == null) return null;
            keys.add(k);
        }
        return keys;
    }

    /** Whether a statement MOVEs SPACE or LOW-VALUE into the item, or a condition compares the item with one. */
    private static boolean nothingUsed(List<Statement> all, int item) {
        for (Statement s : all) {
            boolean nothingSent = s.operands().stream().anyMatch(o -> o.role().equals("move-to-sending-area") && nothing(o.value()));
            if (s.verb().equals("MOVE") && nothingSent && s.operands().stream().anyMatch(o -> o.role().equals("move-to-statement") && Walk.refers(o.value(), item) > 0)) return true;
            for (Statement.Branch b : s.branches()) if (compared(b.condition(), item)) return true;
        }
        return false;
    }

    private static boolean compared(Value v, int item) {
        if (!(v instanceof Value.Expression e)) return false;
        if (e.operands().size() == 2 && e.operands().stream().anyMatch(o -> o instanceof Value.Field f && f.item() == item) && e.operands().stream().anyMatch(FlagRule::nothing)) return true;
        return e.operands().stream().anyMatch(o -> compared(o, item));
    }

    private static boolean nothing(Value v) {
        return v instanceof Value.Figurative && NOTHING.contains(EnumRule.key(v));
    }
}
