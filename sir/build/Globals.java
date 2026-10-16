package katana.sir.build;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import katana.sir.types.Item;
import katana.sir.types.Program;

/**
 * SIR-12: the GLOBAL records a nested program can see, copied after its own items so that its statements
 * name them as they name their own: "A statement in a program contained directly or indirectly within a
 * program that describes a global name can reference that name without describing it again" (LR 6.4 p.
 * 197). Each copy keeps its offsets and attributes; `Program.Global` says whose record it is.
 */
final class Globals {
    private Globals() {}

    /**
     * The program's items and then, nearest container first, each GLOBAL 01 of each containing program
     * (`outer`, the outermost first) with its subordinates and the indexes its tables are INDEXED BY;
     * each copied record named in `globals`, each copied item's depth in `tiers`.
     */
    // Decision: R-062
    static List<Item> copied(List<Item> own, List<List<Item>> outer, List<Program.Global> globals, Map<Integer, Integer> tiers) {
        List<Item> items = new ArrayList<>(own);
        for (int k = outer.size() - 1; k >= 0; k--) {
            int depth = outer.size() - k;
            List<Item> theirs = outer.get(k);
            for (Item r : theirs) {
                if (r.record() != r.id() || r.level() != 1 || !r.flags().contains(Item.Flag.GLOBAL)) continue;
                List<Item> subtree = new ArrayList<>(theirs.stream().filter(i -> i.record() == r.id()).toList());
                for (Item i : List.copyOf(subtree)) if (i.occurs() != null) for (int x : i.occurs().indexedBy()) subtree.add(theirs.get(x - 1));
                Map<Integer, Integer> ids = new HashMap<>();
                for (Item i : subtree) ids.put(i.id(), items.size() + ids.size() + 1);
                for (Item i : subtree) {
                    int id = ids.get(i.id());
                    items.add(copy(i, ids));
                    tiers.put(id, depth);
                    if (i.record() == i.id()) globals.add(new Program.Global(id, depth, i.id()));
                }
            }
        }
        return items;
    }

    /** An item renumbered: its id, parent, record, REDEFINES and table references to their copies, 0 where one was not copied. */
    private static Item copy(Item i, Map<Integer, Integer> ids) {
        Item.Occurs o = i.occurs() == null ? null : new Item.Occurs(i.occurs().min(), i.occurs().max(), ids.getOrDefault(i.occurs().dependingOn(), 0),
                i.occurs().keys().stream().filter(k -> ids.containsKey(k.item())).map(k -> new Item.Key(ids.get(k.item()), k.ascending())).toList(),
                i.occurs().indexedBy().stream().filter(ids::containsKey).map(ids::get).toList());
        return new Item(ids.get(i.id()), i.name(), i.level(), ids.getOrDefault(i.parent(), 0), ids.get(i.record()), i.section(), i.offset(), i.length(), i.attributes(),
                ids.getOrDefault(i.redefines(), 0), o, i.syncSlack(), i.value(), i.conditions(), i.flags(), i.origin());
    }
}
