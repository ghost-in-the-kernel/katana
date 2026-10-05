package katana.oracle.compare;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.sir.build.Build;
import katana.sir.types.Attributes;
import katana.sir.types.Item;

/**
 * A record's layout from copybook text (docs/ROADMAP.md 1.7), through sir/'s data map since tithe
 * pass 2, B1: each elementary field's path, offset, length, kind (text, zoned, packed, binary) and
 * digits after the point, with OCCURS repeated and a REDEFINES alternative left out (the first
 * definition is the one compared), the record's length its extent. A copybook of more than one record,
 * or a clause outside this (OCCURS DEPENDING ON, a separate or leading sign, P scaling, floating
 * point, pointers, national), is refused, never guessed.
 */
public final class Layout {
    /** How a field holds its value. */
    public enum Kind { TEXT, ZONED, PACKED, BINARY }

    /** An elementary field: its path (`ACCT-FIELDS.CLIENT-ADDR.CITY-COUNTY`, `(n)` for an occurrence), where it is and how it holds its value. */
    public record Field(String path, int offset, int length, Kind kind, boolean signed, int scale) {}

    /** A clause the reader does not handle, quoted. */
    public static final class Unsupported extends RuntimeException {
        Unsupported(String message) {
            super(message);
        }
    }

    private final List<Field> fields;
    private final int length;

    private Layout(List<Field> fields, int length) {
        this.fields = fields;
        this.length = length;
    }

    /** A record with no copybook (`copybook=-`): one text field of its whole length, compared as text. */
    public static Layout raw(int lrecl) {
        return new Layout(List.of(new Field("(record)", 0, lrecl, Kind.TEXT, false, 0)), lrecl);
    }

    public List<Field> fields() {
        return fields;
    }

    public int length() {
        return length;
    }

    /**
     * A copybook's one record from the data map sir/ builds (tithe pass 2, B1): the copybook as the
     * WORKING-STORAGE of a program of its own, its items flattened to elementary fields.
     */
    public static Layout read(String copybook) {
        String program = "       IDENTIFICATION DIVISION.\n       PROGRAM-ID. LAYOUT.\n       DATA DIVISION.\n       WORKING-STORAGE SECTION.\n" + copybook + "\n";
        Parse.Result parse = Parse.text(program, "copybook", new Copybooks(List.of(), n -> 8));
        if (!parse.clean()) throw new Unsupported("the copybook does not parse: " + parse.errors().get(0));
        Build.Data data = Build.data(parse);
        if (!data.findings().isEmpty()) throw new Unsupported(data.findings().get(0));
        List<Item> records = data.items().stream().filter(i -> i.parent() == 0).toList();
        if (records.size() != 1) throw new Unsupported(records.size() + " records at level 01 or 77, not one");
        Map<Integer, List<Item>> children = new HashMap<>();
        data.items().forEach(i -> children.computeIfAbsent(i.parent(), k -> new ArrayList<>()).add(i));
        List<Field> fields = new ArrayList<>();
        Item record = records.get(0);
        flatten(record, record.name(), 0, children, fields);
        return new Layout(fields, record.length());
    }

    /** An item at an offset under a path: a REDEFINES left out, each occurrence of a table, a group's children, an elementary item a field. */
    private static void flatten(Item item, String path, int at, Map<Integer, List<Item>> children, List<Field> fields) {
        if (item.redefines() != 0) return;
        if (item.level() == 66 || item.level() == 77) throw new Unsupported("level " + item.level() + ": " + item.name());
        int times = 1;
        if (item.occurs() != null) {
            if (item.occurs().dependingOn() != 0) throw new Unsupported("OCCURS DEPENDING ON: " + item.name());
            times = item.occurs().max();
        }
        for (int n = 1; n <= times; n++) {
            String name = path + (times > 1 ? "(" + n + ")" : "");
            int base = at + (n - 1) * item.length();
            List<Item> under = children.getOrDefault(item.id(), List.of());
            if (under.isEmpty()) fields.add(field(item, name, base));
            for (Item child : under) flatten(child, name + "." + child.name(), base + child.offset() - item.offset(), children, fields);
        }
    }

    /** An elementary item as a field the comparator decodes; a representation it does not decode is refused. */
    private static Field field(Item item, String path, int at) {
        Attributes a = item.attributes();
        switch (a.usage()) {
            case FLOAT_SHORT, FLOAT_LONG, INDEX, POINTER, PROCEDURE_POINTER, FUNCTION_POINTER, NATIONAL -> throw new Unsupported("a usage the comparator does not decode: " + item.name() + " " + a.usage());
            default -> { }
        }
        if (a.signSeparate() || a.leading()) throw new Unsupported("a sign the comparator does not decode (SEPARATE or LEADING): " + item.name());
        if (a.category() != Attributes.Category.NUMERIC) {
            if (a.usage() != Attributes.Usage.DISPLAY) throw new Unsupported("a non-numeric PICTURE with a computational usage: " + item.name());
            return new Field(path, at, item.length(), Kind.TEXT, false, 0);
        }
        if (a.scale() < 0) throw new Unsupported("a P scaling the comparator does not decode: " + item.name());
        Kind kind = switch (a.usage()) {
            case PACKED -> Kind.PACKED;
            case BINARY, NATIVE_BINARY -> Kind.BINARY;
            default -> Kind.ZONED;
        };
        return new Field(path, at, item.length(), kind, a.signed(), a.scale());
    }
}
