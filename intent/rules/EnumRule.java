package katana.intent.rules;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import katana.intent.types.Evidence;
import katana.intent.types.Fact;
import katana.intent.types.Kind;
import katana.intent.types.Node;
import katana.intent.types.State;
import katana.intent.types.Subject;
import katana.sir.types.Item;
import katana.sir.types.Program;
import katana.sir.types.Value;

/** `enum.disjoint-88s`: an item with two or more condition-names whose values no two of them share is an enum (docs/ROADMAP.md 5.2); from version 2, one flag.yes-no takes is not (R-065). */
final class EnumRule implements Rule {
    public String name() {
        return "enum.disjoint-88s";
    }

    public int version() {
        return 2;
    }

    // Unsure: 0.9, under the 0.932 recorded for version 2 on CardDemo's key (research §32); its errors now message texts and numbers with named values.
    public double confidence() {
        return 0.9;
    }

    public List<Fact> infer(Program p) {
        List<Fact> facts = new ArrayList<>();
        List<katana.sir.types.Statement> all = Walk.statements(p);
        for (Item i : p.items()) {
            if (FlagRule.flag(i, all)) continue;
            List<String> names = i.conditions().stream().map(Item.Condition::name).toList();
            if (names.size() < 2 || new HashSet<>(names).size() != names.size() || !disjoint(i.conditions())) continue;
            facts.add(new Fact(Kind.ENUM, new Subject.Choice(i.id(), names), new Evidence(name(), version(), List.of(new Node.ItemNode(i.id()))), confidence(), State.INFERRED));
        }
        return facts;
    }

    /** Whether no value is named by two condition-names; a THRU range, or a value read no way below, counts as shared. */
    private static boolean disjoint(List<Item.Condition> conditions) {
        Set<String> seen = new HashSet<>();
        for (Item.Condition c : conditions) {
            Set<String> own = new HashSet<>();
            for (Value v : c.values()) {
                String key = key(v);
                if (key == null) return false;
                own.add(key);
            }
            for (String k : own) if (!seen.add(k)) return false;
        }
        return true;
    }

    /** A value as compared: a number by its value, text without trailing spaces, SPACE as all spaces, a figurative constant by its name; null for a range. */
    static String key(Value v) {
        if (v instanceof Value.Literal l) {
            if (l.kind() == Value.Literal.Kind.NUMERIC) return "n" + new BigDecimal(l.payload()).stripTrailingZeros().toPlainString();
            String text = l.payload().stripTrailing();
            return text.isEmpty() ? "SPACE" : "t" + text;
        }
        if (v instanceof Value.Figurative f) {
            String n = f.name().toUpperCase(Locale.ROOT).replaceAll("(S|ES)$", "");
            if (n.equals("ZERO")) return "n0";
            return n.equals("SPACE") ? "SPACE" : f.all() == null ? n : null;
        }
        return null;
    }
}
