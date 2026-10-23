package katana.lower;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import katana.sir.types.Item;

/**
 * Items variably located (LR 6.4, OCCURS DEPENDING ON: "any nonsubordinate item that follows an item described
 * with an OCCURS DEPENDING ON clause is a variably located item"): an item after an ODO table in its record is
 * located after the table's current length, not its maximum, so its field moves with the table's object (R-088,
 * `-fodoslide`). The IR gives the table, its object and its bounds; this class only writes the sum.
 */
final class Slide {
    private final Exprs x;
    private final Map<Integer, List<Item>> tables = new HashMap<>();

    Slide(Exprs x) {
        this.x = x;
    }

    /** A record's ODO tables a slide can follow: one dimension (a table inside another OCCURS stays at its maximum, IBM's complex ODO). */
    private List<Item> tables(int record) {
        return tables.computeIfAbsent(record, r -> {
            List<Item> out = new ArrayList<>();
            for (Item t : x.p.items()) if (t.record() == r && t.occurs() != null && t.occurs().dependingOn() != 0 && x.dimensions(t).size() == 1) out.add(t);
            return out;
        });
    }

    /** How far a table's occurrences, now, fall short of its maximum, in bytes (zero or less): `(object - max) * length`. */
    String shortfall(Item t) {
        return "(Table.index(" + where(x.item(t.occurs().dependingOn()), null) + ") - " + t.occurs().max() + ") * " + t.length();
    }

    /** The tables lying wholly before an item in its record's maximum layout, other than those under `scope`. */
    private List<Item> before(Item i, Item scope) {
        List<Item> out = new ArrayList<>();
        for (Item t : tables(i.record())) if (t.id() != i.id() && t.offset() + t.occurs().max() * t.length() <= i.offset() && (scope == null || !x.under(t, scope))) out.add(t);
        return out;
    }

    /** The name of the Field an item is reached by, moved by the tables before it; the plain name where none is. */
    String where(Item i, Item scope) {
        String name = x.promoted.fieldName(i.id());
        if (x.promoted.has(i.id()) || x.promoted.views().path(i.id()) != null) return name;
        List<Item> before = before(i, scope);
        if (before.isEmpty()) return name;
        StringBuilder delta = new StringBuilder();
        for (Item t : before) delta.append(delta.length() == 0 ? "" : " + ").append(shortfall(t));
        return "Table.slide(" + name + ", " + delta + ")";
    }
}
