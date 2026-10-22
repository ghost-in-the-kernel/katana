package katana.lower;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import katana.sir.types.Attributes;
import katana.sir.types.Item;
import katana.sir.types.Program;

/**
 * 6.7's record classes (research §46): a class per record passes/Records names, over the record's bytes, each
 * member a `Field` named for its item (the 01 itself `whole`), a numeric member with a getter returning IBM's
 * read of its bytes, and a member under a fixed OCCURS with an accessor, `Table.at` over its first element. No
 * byte and no operation changes: every store still goes through the `Field`.
 */
final class Views {
    private final Program p;
    private final Exprs x;
    private final Map<Integer, String> classes = new LinkedHashMap<>(), instances = new LinkedHashMap<>(), members = new LinkedHashMap<>();

    /** The names: each record's class and instance, then each member's within its class. */
    Views(Program p, Set<Integer> records, Exprs x, Set<String> taken) {
        this.p = p;
        this.x = x;
        Set<String> types = new HashSet<>(taken);
        for (Item r : p.items()) {
            if (!records.contains(r.id())) continue;
            String instance = x.promoted.reserve(r.name(), r.id());
            String type = Character.toUpperCase(instance.charAt(0)) + instance.substring(1);
            if (types.contains(type) || javaLang(type)) type = type + "View";
            while (!types.add(type)) type = type + "_";
            classes.put(r.id(), type);
            instances.put(r.id(), instance);
            Set<String> used = new HashSet<>(Set.of("whole"));
            for (Item i : p.items()) if (i.record() == r.id() && !x.promoted.has(i.id())) members.put(i.id(), i.id() == r.id() ? "whole" : Promoted.name(i.name(), i.id(), used));
        }
    }

    private static boolean javaLang(String type) {
        try {
            Class.forName("java.lang." + type);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    boolean record(int id) {
        return classes.containsKey(id);
    }

    /** `tranRecord.tranAmt` for a member, null for an item no class has. */
    String path(int item) {
        String m = members.get(item);
        return m == null ? null : instances.get(x.item(item).record()) + "." + m;
    }

    /** A member under a fixed OCCURS, at its subscripts: `tranRecord.entryAmt(i, j)`. */
    String at(int item, List<String> subscripts) {
        return path(item) + "(" + String.join(", ", subscripts) + ")";
    }

    /** The getter's call for a numeric member read whole, `tranRecord.tranAmt()`; null for any other item. */
    String getter(int item) {
        return members.containsKey(item) && typed(x.item(item)) != null ? path(item) + "()" : null;
    }

    /** Whether a member's getter returns a `BigDecimal` (decimal places, or past a long's 18 digits), not a `long`. */
    boolean big(int item) {
        return "java.math.BigDecimal".equals(typed(x.item(item)));
    }

    /** A numeric member's getter type, for an item of DISPLAY, BINARY or PACKED usage with no P, not in a table; null for any other. */
    private String typed(Item i) {
        Attributes a = i.attributes();
        if (a.category() != Attributes.Category.NUMERIC || !Set.of(Attributes.Usage.DISPLAY, Attributes.Usage.BINARY, Attributes.Usage.PACKED).contains(a.usage()) || a.scale() < 0) return null;
        if (a.picture() != null && a.picture().toUpperCase().contains("P") || !x.dimensions(i).isEmpty()) return null;
        return a.scale() == 0 && Lower.stored(a) <= 18 ? "long" : "java.math.BigDecimal";
    }

    /** The record's instance and its class: a `Field` per member, built by chunks from the record; the getters and the tables' accessors. */
    // Decision: R-024
    void declare(Emitter e, Item record) {
        String type = classes.get(record.id()), instance = instances.get(record.id());
        e.line("private " + type + " " + instance + "; // " + record.name());
        e.symbol(instance, "record " + record.id());
        e.line("/** " + record.name() + " as a class over its bytes (6.7, research §46): each member a Field; the bytes stay the truth. */");
        e.open("private static final class " + type);
        e.enter(type);
        List<Item> own = new ArrayList<>();
        for (Item i : p.items()) if (members.containsKey(i.id()) && i.record() == record.id()) own.add(i);
        for (Item i : own) {
            e.line("Field " + members.get(i.id()) + "; // " + i.name());
            e.symbol(members.get(i.id()), "item " + i.id());
        }
        e.open(type + "(Record r, int base)");
        for (int c = 0; c * Lower.CHUNK < own.size(); c++) e.line("members" + c + "(r, base);");
        e.close();
        for (int c = 0; c * Lower.CHUNK < own.size(); c++) {
            e.open("private void members" + c + "(Record r, int base)");
            for (Item i : own.subList(c * Lower.CHUNK, Math.min(own.size(), (c + 1) * Lower.CHUNK)))
                e.line(members.get(i.id()) + " = new Field(r, base + " + i.offset() + ", " + Lower.attr(p, i, x) + ");");
            e.close();
        }
        for (Item i : own) accessors(e, i);
        e.leave();
        e.close();
    }

    /** A numeric member's getter, IBM's read of its bytes; a table member's accessor, its element at the subscripts. */
    private void accessors(Emitter e, Item i) {
        String m = members.get(i.id()), type = typed(i);
        if (type != null) {
            e.open(type + " " + m + "()");
            e.line("return " + (type.equals("long") ? "Scalars.longOf(" + m + ".get())" : m + ".get().toBigDecimal()") + ";");
            e.close();
        }
        List<Item> dims = x.dimensions(i);
        if (dims.isEmpty()) return;
        List<String> params = new ArrayList<>(), names = new ArrayList<>(), strides = new ArrayList<>();
        for (int k = 0; k < dims.size(); k++) {
            params.add("long s" + k);
            names.add("s" + k);
            strides.add(String.valueOf(dims.get(k).length()));
        }
        e.open("Field " + m + "(" + String.join(", ", params) + ")");
        e.line("return Table.at(" + m + ", new int[] {" + String.join(", ", strides) + "}, " + String.join(", ", names) + ");");
        e.close();
    }

    /** The statement building a record's instance over its bytes, once its record exists: its own storage at 0. */
    String create(int record) {
        return instances.get(record) + " = new " + classes.get(record) + "(r" + record + ", 0);";
    }

    /** A LINKAGE record's instance bound to the storage `a` names, at its start (6.7b, research §47). */
    String bind(int record) {
        return instances.get(record) + " = new " + classes.get(record) + "(a.record(), a.offset());";
    }
}
