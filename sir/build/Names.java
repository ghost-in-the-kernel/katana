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
    private final int own;
    private final Map<Integer, Integer> tiers;
    private final Map<String, List<Integer>> fileRecords;
    private final List<String> findings;

    /**
     * A program's names: its own items, the first `own`, then the GLOBAL records of the programs it is
     * nested in (SIR-12), each item's depth out in `tiers`.
     */
    Names(List<Item> items, int own, Map<Integer, Integer> tiers, Map<String, List<Integer>> fileRecords, List<String> findings) {
        this.items = items;
        this.own = own;
        this.tiers = tiers;
        this.fileRecords = fileRecords;
        this.findings = findings;
    }

    /** The program's own items, without the GLOBAL ones it sees. */
    List<Item> own() {
        return items.subList(0, own);
    }

    /** A build finding about a clause, not a name. */
    void note(String finding) {
        findings.add(finding);
    }

    /**
     * The item a name names, its qualifiers and file given, or a build finding: never a silent 0 (J-148).
     * `what` says where the name stands, for the finding.
     */
    int resolve(String name, List<String> qualifiers, String file, String where, String what) {
        int item = find(name, qualifiers, file, where);
        if (item == 0) findings.add(what + " " + name + (qualifiers.isEmpty() ? "" : " OF " + String.join(" OF ", qualifiers)) + " at " + where + " names no item");
        return item;
    }

    /**
     * The item a qualified name names, or 0 with no finding: only for a caller that tries what else the
     * name can be and ends in a finding itself (`Values`); every other caller resolves (NameRulesTest).
     */
    int find(String name, List<String> qualifiers, String file, String where) {
        List<Item> found = new ArrayList<>();
        for (Item i : items) if (i.name().equals(name) && qualified(i, qualifiers) && inFile(i, file)) found.add(i);
        // A GLOBAL name is seen "as long as the contained program does not itself have a definition for that name" (LR 6.4 p. 197): the nearest definition wins.
        int nearest = found.stream().mapToInt(i -> tiers.getOrDefault(i.id(), 0)).min().orElse(0);
        found.removeIf(i -> tiers.getOrDefault(i.id(), 0) != nearest);
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

    /** Each qualifier, outward, an ancestor of the item, or the file whose record it is in (the grammar cannot tell `IN file` from `IN data`). */
    private boolean qualified(Item item, List<String> qualifiers) {
        int parent = item.parent();
        for (String q : qualifiers) {
            if (fileRecords.containsKey(q) && fileRecords.get(q).contains(item.record())) continue;
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
