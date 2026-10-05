package katana.sir.build;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import katana.sir.types.Item;

/**
 * A data name as COBOL resolves it: the items of that name whose parents include each qualifier
 * (`A OF B OF C`), in that order outward, and whose record is one of the qualifying file's when a file
 * is named. One item is the answer; none or more than one is a finding, and the first is kept when
 * there is more than one so the IR stays whole.
 */
final class Names {
    private final List<Item> items;
    private final Map<String, List<Integer>> fileRecords;
    private final List<String> findings;

    Names(List<Item> items, Map<String, List<Integer>> fileRecords, List<String> findings) {
        this.items = items;
        this.fileRecords = fileRecords;
        this.findings = findings;
    }

    /** The item a qualified name names, or 0. */
    int item(String name, List<String> qualifiers, String file, String where) {
        List<Item> found = new ArrayList<>();
        for (Item i : items) if (i.name().equals(name) && qualified(i, qualifiers) && inFile(i, file)) found.add(i);
        if (found.size() > 1) findings.add(name + " at " + where + " is ambiguous: " + found.size() + " items");
        return found.isEmpty() ? 0 : found.get(0).id();
    }

    /** The item an 88 of this name belongs to, or 0. */
    int condition(String name, List<String> qualifiers, String file) {
        for (Item i : items) {
            if (i.conditions().stream().anyMatch(c -> c.name().equals(name)) && (qualifiers.isEmpty() || qualified(i, qualifiers) || i.name().equals(qualifiers.get(0)) && qualified(i, qualifiers.subList(1, qualifiers.size()))) && inFile(i, file)) {
                return i.id();
            }
        }
        return 0;
    }

    private boolean qualified(Item item, List<String> qualifiers) {
        int parent = item.parent();
        for (String q : qualifiers) {
            while (parent != 0 && !items.get(parent - 1).name().equals(q)) parent = items.get(parent - 1).parent();
            if (parent == 0) return false;
            parent = items.get(parent - 1).parent();
        }
        return true;
    }

    private boolean inFile(Item item, String file) {
        return file == null || fileRecords.getOrDefault(file, List.of()).contains(item.record());
    }
}
