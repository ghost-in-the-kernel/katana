package katana.lower;

import java.util.ArrayList;
import java.util.List;
import katana.sir.types.Attributes;
import katana.sir.types.Item;
import katana.sir.types.Program;
import katana.sir.types.Value;

/** Operands as Java: a field (subscripted, reference-modified), a literal or figurative constant as a sending item, a move into a field. */
final class Exprs {
    final Program p;
    private final List<String> findings;

    Exprs(Program p, List<String> findings) {
        this.p = p;
        this.findings = findings;
    }

    Item item(int id) {
        return p.items().get(id - 1);
    }

    /** What cannot be lowered: a finding, and Java that compiles and fails when it runs. */
    String unsupported(Item i, String what) {
        findings.add("not lowered: " + i.name() + " (" + what + ") at " + i.origin());
        return "Attr.text(" + Math.max(1, i.length()) + ", false)";
    }

    String unsupported(String what) {
        findings.add("not lowered: " + what);
        return "Unsupported.fail(" + Emitter.quote(what) + ")";
    }

    /** The items with OCCURS from the outermost to the item itself. */
    List<Item> dimensions(Item i) {
        List<Item> dims = new ArrayList<>();
        for (Item a = i; a != null; a = a.parent() == 0 ? null : item(a.parent())) if (a.occurs() != null) dims.add(0, a);
        return dims;
    }

    /** `Table.each(f, counts, strides, ` for a table's item, null for an item that occurs once. */
    String occurrences(Item i) {
        List<Item> dims = dimensions(i);
        if (dims.isEmpty()) return null;
        return "Table.each(f" + i.id() + ", " + ints(dims, true) + ", " + ints(dims, false) + ", ";
    }

    private static String ints(List<Item> dims, boolean counts) {
        List<String> v = new ArrayList<>();
        for (Item d : dims) v.add(String.valueOf(counts ? d.occurs().max() : d.length()));
        return "new int[] {" + String.join(", ", v) + "}";
    }

    /** A field reference as a Java expression of type Field. */
    String field(Value.Field v) {
        Item i = item(v.item());
        String f = "f" + v.item();
        if (!v.subscripts().isEmpty()) {
            List<String> subs = new ArrayList<>();
            for (Value s : v.subscripts()) subs.add(index(s));
            f = "Table.at(" + f + ", " + ints(dimensions(i), false) + ", " + String.join(", ", subs) + ")";
        }
        if (v.start() != null) f = "Table.ref(" + f + ", " + index(v.start()) + ", " + (v.length() == null ? "0" : index(v.length())) + ")";
        return f;
    }

    /** A subscript, a reference modifier, a count: a long. */
    String index(Value v) {
        return switch (v) {
            case Value.Literal l when l.numeric() -> l.payload() + "L";
            case Value.Field f -> "Table.index(" + field(f) + ")";
            case Value.Expression e when e.operands().size() == 2 && List.of("+", "-").contains(e.operator()) -> "(" + index(e.operands().get(0)) + " " + e.operator() + " " + index(e.operands().get(1)) + ")";
            default -> unsupported("index " + v) + "";
        };
    }

    /** A value as a sending item (a Field), for MOVE and comparison. */
    String sending(Value v) {
        return switch (v) {
            case Value.Field f -> field(f);
            case Value.Literal l when l.numeric() -> "Literal.number(" + Emitter.quote(l.payload()) + ", " + l.scale() + ", cp)";
            case Value.Literal l when l.kind() == Value.Literal.Kind.ALPHANUMERIC -> "Literal.text(" + Emitter.quote(l.payload()) + ", cp)";
            case Value.Literal l when l.kind() == Value.Literal.Kind.HEX -> "Literal.hex(" + Emitter.quote(l.payload()) + ", cp)";
            default -> unsupported("sending " + v);
        };
    }

    /** A figurative constant's or ALL literal's repeated characters, as a byte[] expression; null for any other value. */
    String pattern(Value v) {
        if (!(v instanceof Value.Figurative f)) return null;
        if (f.all() != null) return "Literal.bytes(" + sending(f.all()) + ")";
        return "new byte[] {Literal.figurative(" + Emitter.quote(singular(f.name())) + ", cp)}";
    }

    /** A figurative constant's name in the singular: SPACES is SPACE, ZEROES and ZEROS are ZERO. */
    static String singular(String name) {
        return name.equals("ZEROES") || name.equals("ZEROS") ? "ZERO" : name.endsWith("S") ? name.substring(0, name.length() - 1) : name;
    }

    /** `value` MOVEd into `target` (a Java Field expression), whose item is `into`. */
    // Decision: R-019
    String moveInto(Value value, String target, Item into) {
        String pattern = pattern(value);
        if (pattern != null) return "Move.fill(" + target + ", " + pattern + ", " + (value instanceof Value.Figurative f && singular(f.name()).equals("ZERO") && f.all() == null) + ")";
        boolean group = into.attributes().category() == Attributes.Category.GROUP || (value instanceof Value.Field f && item(f.item()).attributes().category() == Attributes.Category.GROUP);
        return (group ? "Move.group(" : "Move.move(") + sending(value) + ", " + target + ")";
    }

    /** Whether a value is numeric: a numeric item, a numeric literal, an arithmetic expression. */
    boolean numeric(Value v) {
        return switch (v) {
            case Value.Field f -> List.of(Attributes.Category.NUMERIC, Attributes.Category.INDEX).contains(item(f.item()).attributes().category()) && f.start() == null;
            case Value.Literal l -> l.numeric();
            case Value.Expression e -> List.of("+", "-", "*", "/", "**", "neg").contains(e.operator());
            case Value.Figurative f -> singular(f.name()).equals("ZERO") && f.all() == null;
            default -> false;
        };
    }
}
