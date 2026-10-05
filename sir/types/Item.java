package katana.sir.types;

import java.util.List;
import katana.front.Mapped;

/**
 * One data item of the data map: its number (1, 2, ... in textual order), name ("FILLER" for none),
 * level, parent (0 for a record), the record it belongs to, the section it is in, its offset in that
 * record and its length in bytes, its attribute tuple, the item it REDEFINES (0 for none), its OCCURS
 * (null for none), the slack bytes SYNC put before it, its VALUE as written (null for none), its
 * condition names (88s), and its origin.
 */
public record Item(int id, String name, int level, int parent, int record, Section section, int offset, int length, Attributes attributes,
        int redefines, Occurs occurs, int syncSlack, String value, List<Condition> conditions, Mapped.Origin origin) {
    /** Where an item is declared. */
    public enum Section { FILE, WORKING_STORAGE, LOCAL_STORAGE, LINKAGE }

    /** OCCURS: at least `min`, at most `max` times; `dependingOn` the ODO object (0 for none); its KEY items and INDEXED BY items. */
    public record Occurs(int min, int max, int dependingOn, List<Key> keys, List<Integer> indexedBy) {
        public Occurs {
            if (min < 0 || max < 1 || min > max) throw new Invalid("SIR-03", "OCCURS " + min + " TO " + max);
            keys = List.copyOf(keys);
            indexedBy = List.copyOf(indexedBy);
        }
    }

    /** An ASCENDING or DESCENDING KEY of a table. */
    public record Key(int item, boolean ascending) {}

    /** An 88: its name and its values as written, a range as `low THRU high`. */
    public record Condition(String name, List<String> values, Mapped.Origin origin) {
        public Condition {
            values = List.copyOf(values);
            Invalid.origin(origin, "condition " + name);
        }
    }

    /** SIR-02's ranges for one item: a positive number, a level from 1 to 88, no negative place or size. */
    public Item {
        if (id < 1 || level < 1 || level > 88 || offset < 0 || length < 0 || syncSlack < 0) throw new Invalid("SIR-02", "item " + id + " " + name + ": id, level, offset or length out of range");
        conditions = List.copyOf(conditions);
        Invalid.origin(origin, "item " + name);
    }
}
