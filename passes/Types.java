package katana.passes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import katana.intent.types.Fact;
import katana.intent.types.Intent;
import katana.intent.types.Kind;
import katana.intent.types.Subject;
import katana.sir.types.Item;
import katana.sir.types.Program;

/**
 * 6.5 (research §42, R-069): the items an `Enum` or `Flag` fact gives names (a predicate per condition name,
 * constants for a promoted item's values), and the promoted items a `Money` fact makes `Money`. A refuted fact
 * is never acted on (R-029, `Intent.actionable`); an unconfirmed one is, and the ledger says so.
 */
public final class Types {
    public static final String PASS = "types-v1";

    private Types() {}

    /** The items named, the items typed money, and the fact behind each. */
    public record Result(Set<Integer> enums, Set<Integer> money, Map<Integer, Fact> facts) {}

    /** Each fact's item, named or typed; each one not acted on, by `item NAME`, with why. */
    // Decision: R-069
    public static Result types(Program p, Intent intent, Set<Integer> scalars, Map<String, List<String>> refused) {
        Set<Integer> enums = new TreeSet<>(), money = new TreeSet<>();
        Map<Integer, Fact> facts = new HashMap<>();
        for (Fact f : intent.actionable()) {
            int id = switch (f.subject()) {
                case Subject.Choice c when f.kind() == Kind.ENUM -> c.item();
                case Subject.Datum d when f.kind() == Kind.FLAG || f.kind() == Kind.MONEY -> d.item();
                default -> 0;
            };
            if (id == 0) continue;
            Item i = p.items().get(id - 1);
            List<String> why = new ArrayList<>();
            if (f.kind() == Kind.MONEY && !scalars.contains(id)) why.add("a Money fact on an item in bytes (a record's field waits for 6.7)");
            if (f.kind() != Kind.MONEY && i.conditions().isEmpty()) why.add("an " + f.kind() + " fact on an item with no condition names");
            if (f.kind() != Kind.MONEY && table(p, i)) why.add("a table's item: its condition names take a subscript");
            if (!why.isEmpty()) {
                refused.computeIfAbsent("item " + i.name(), k -> new ArrayList<>()).addAll(why);
                continue;
            }
            (f.kind() == Kind.MONEY ? money : enums).add(id);
            facts.put(id, f);
        }
        return new Result(enums, money, facts);
    }

    private static boolean table(Program p, Item i) {
        for (Item a = i; a != null; a = a.parent() == 0 ? null : p.items().get(a.parent() - 1)) if (a.occurs() != null) return true;
        return false;
    }
}
