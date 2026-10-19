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
    final Funcs funcs = new Funcs(this);
    private Arith arith;

    /** The arithmetic a function's arguments are computed with. */
    Arith arith() {
        if (arith == null) arith = new Arith(this);
        return arith;
    }
    private final List<String> findings;

    /** The items 6.3 promotes in this program (none in a literal build, or a nested program). */
    Promoted promoted;

    Exprs(Program p, List<String> findings) {
        this.p = p;
        this.findings = findings;
        this.promoted = new Promoted(p, java.util.Set.of());
    }

    /**
     * A store of an arithmetic result into a receiver, a boolean (the size error): `Arithmetic.store` into its
     * bytes, or a promoted item's own store, which leaves the same value (R-068).
     */
    String store(Value.Field target, String term, String rounded, String onSizeError) {
        if (promoted.has(target.item())) return promoted.store(target.item(), term, rounded, onSizeError);
        return "Arithmetic.store(" + field(target) + ", " + term + ", " + rounded + ", " + onSizeError + ")";
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

    /** A field reference as a receiver: as `field` gives it, but a group holding its own OCCURS DEPENDING ON object at its maximum length (LR 6.4 p. 205). */
    String receiving(Value.Field v) {
        return field(v, true);
    }

    /** A field reference as a Java expression of type Field: a group holding an OCCURS DEPENDING ON table at its current length. */
    String field(Value.Field v) {
        return field(v, false);
    }

    /** A reference as a Java Field: subscripted, reference-modified, an ODO group at its length; a promoted item's value encoded into a field of its own. */
    private String field(Value.Field v, boolean receiving) {
        // A promoted item has no bytes to write: a statement that receives into it is converted before here (Verbs.move, store), and passes/Scalars keeps every other receiver literal.
        if (promoted.has(v.item()) && receiving) throw new IllegalStateException("promoted item " + item(v.item()).name() + " reached a receiving path not converted");
        if (promoted.has(v.item())) return promoted.field(v.item());
        Item i = item(v.item());
        String f = "f" + v.item();
        if (!v.subscripts().isEmpty()) {
            List<String> subs = new ArrayList<>();
            for (Value s : v.subscripts()) subs.add(index(s));
            f = "Table.at(" + f + ", " + ints(dimensions(i), false) + ", " + String.join(", ", subs) + ")";
        }
        if (v.start() == null) f = variable(i, f, receiving);
        if (v.start() != null) f = "Table.ref(" + f + ", " + index(v.start()) + ", " + (v.length() == null ? "0" : index(v.length())) + ")";
        return f;
    }

    /**
     * A group holding an OCCURS DEPENDING ON table (LR 6.4 pp. 204-205): the part of the table its object
     * says, the object's value at the start of the operation; as a receiver whose own object is in the
     * group, its maximum length. One table at the group's end; any other shape at its maximum.
     */
    // Decision: R-019
    // Unsure: a group whose ODO table is followed by other items, holds two, or nests one in an OCCURS (IBM's complex ODO) keeps its maximum length here (J-133). Stays: IBM's complex-ODO rules read, and a program that needs them, settle it.
    private String variable(Item group, String f, boolean receiving) {
        if (group.attributes().category() != Attributes.Category.GROUP) return f;
        List<Item> tables = new ArrayList<>();
        for (Item t : p.items()) if (t.occurs() != null && t.occurs().dependingOn() != 0 && t.id() != group.id() && under(t, group)) tables.add(t);
        if (tables.size() != 1) return f;
        Item t = tables.get(0), object = item(t.occurs().dependingOn());
        int before = t.offset() - group.offset();
        boolean last = before + t.occurs().max() * t.length() == group.length();
        boolean inside = object.record() == group.record() && object.offset() >= group.offset() && object.offset() < group.offset() + group.length();
        if (!last || dimensions(t).size() != 1 || (receiving && inside)) return f;
        return "Table.current(" + f + ", " + before + " + Table.index(f" + object.id() + ") * " + t.length() + ")";
    }

    private boolean under(Item i, Item group) {
        for (Item a = i; a != null; a = a.parent() == 0 ? null : item(a.parent())) if (a.id() == group.id()) return true;
        return false;
    }

    /** A subscript, a reference modifier, a count: a long. */
    String index(Value v) {
        if (counter(v) != null) return "Table.index(" + counter(v) + ")";
        return switch (v) {
            case Value.Literal l when l.numeric() -> l.payload() + "L";
            // ZERO where an integer goes: WRITE ... ADVANCING ZERO, whose "integer ... can be zero" (LR 6.4 p. 473; SQ101M, J-172).
            case Value.Figurative f when numeric(f) -> "0L";
            case Value.Field f -> "Table.index(" + field(f) + ")";
            case Value.Expression e when e.operands().size() == 2 && List.of("+", "-").contains(e.operator()) -> "(" + index(e.operands().get(0)) + " " + e.operator() + " " + index(e.operands().get(1)) + ")";
            case Value.Expression e when e.operator().equals("pos") -> index(e.operands().get(0));
            case Value.Expression e when e.operator().equals("neg") -> "(-" + index(e.operands().get(0)) + ")";
            default -> unsupported("index " + v) + "";
        };
    }

    /**
     * LINAGE-COUNTER as the Field its file keeps (LR 6.4 p. 23): `LINAGE-COUNTER OF file`, or the bare
     * register when one file has a LINAGE clause; null for any other value.
     */
    String counter(Value v) {
        if (v instanceof Value.Expression e && e.operator().equals("LINAGE-COUNTER OF") && e.operands().get(0) instanceof Value.File f) return FileVerbs.name(f.name()) + ".counter()";
        if (!(v instanceof Value.Name n && n.kind().equals("register") && n.text().equals("LINAGE-COUNTER"))) return null;
        List<katana.sir.types.FileDescriptor> paged = p.files().stream().filter(f -> f.fd() != null && f.fd().linage() != null).toList();
        return paged.size() == 1 ? FileVerbs.name(paged.get(0).name()) + ".counter()" : unsupported("LINAGE-COUNTER unqualified, with " + paged.size() + " LINAGE files");
    }

    /** A value as a sending item (a Field), for MOVE and comparison. */
    String sending(Value v) {
        if (counter(v) != null) return counter(v);
        if (returnCode(v)) return "rc";
        if (v instanceof Value.Name n && n.kind().equals("register") && Debugging.held(p) && Debugging.register(n.text()) != null) return Debugging.register(n.text());
        return switch (v) {
            case Value.Field f -> field(f);
            case Value.Literal l when l.numeric() -> "Literal.number(" + Emitter.quote(written(l)) + ", " + l.scale() + ", cp)";
            case Value.Literal l when l.kind() == Value.Literal.Kind.ALPHANUMERIC -> "Literal.text(" + Emitter.quote(l.payload()) + ", cp)";
            case Value.Literal l when l.kind() == Value.Literal.Kind.HEX -> "Literal.hex(" + Emitter.quote(l.payload()) + ", cp)";
            // An alphanumeric function is a new item; a numeric one is used "only where an arithmetic expression can be specified" (LR 6.4 p. 499).
            case Value.Expression e when Funcs.name(e) != null && !numeric(e) -> funcs.alphanumeric(e, arith());
            // A function reference-modified: "the function's returned value is reference-modified" (LR 6.4 p. 498).
            case Value.Expression e when e.operator().equals("slice") -> "Table.ref(" + sending(e.operands().get(0)) + ", " + index(e.operands().get(1)) + ", " + (e.operands().size() > 2 ? index(e.operands().get(2)) : "0") + ")";
            default -> unsupported("sending " + v);
        };
    }

    /** Whether a value is the RETURN-CODE special register, which each program holds as its field `rc` (LR 6.4 p. 24). */
    static boolean returnCode(Value v) {
        return v instanceof Value.Name n && n.kind().equals("register") && n.text().equals("RETURN-CODE");
    }

    /** A figurative constant's or ALL literal's repeated characters, as a byte[] expression; null for any other value. */
    String pattern(Value v) {
        if (!(v instanceof Value.Figurative f)) return null;
        if (f.all() != null) return "Literal.bytes(" + sending(f.all()) + ")";
        String one = singular(f.name());
        boolean sequenced = !p.header().collating().isEmpty();
        if (sequenced && one.equals("LOW-VALUE")) return "new byte[] {co.low()}";
        if (sequenced && one.equals("HIGH-VALUE")) return "new byte[] {co.high()}";
        return "new byte[] {Literal.figurative(" + Emitter.quote(singular(f.name())) + ", cp)}";
    }

    /** A figurative constant's name in the singular: SPACES is SPACE, ZEROES and ZEROS are ZERO; ALL before one adds nothing (`ALL QUOTE` is QUOTE). */
    static String singular(String written) {
        String name = written.replaceFirst("^ALL ", "");
        return name.equals("ZEROES") || name.equals("ZEROS") ? "ZERO" : name.endsWith("S") ? name.substring(0, name.length() - 1) : name;
    }

    /** `value` MOVEd into `target` (a Java Field expression), whose item is `into`. */
    // Decision: R-019
    String moveInto(Value value, String target, Item into) {
        // A promoted item is moved into only through a field of its own read back (Promoted.receive); any other path would write bytes it does not have.
        if (promoted.has(into.id()) && !target.equals("scratch")) throw new IllegalStateException("a MOVE into promoted item " + into.name() + " not converted");
        String pattern = pattern(value);
        if (pattern != null) return "Move.fill(" + target + ", " + pattern + ", " + (value instanceof Value.Figurative f && singular(f.name()).equals("ZERO") && f.all() == null) + ")";
        boolean group = into.attributes().category() == Attributes.Category.GROUP || (value instanceof Value.Field f && item(f.item()).attributes().category() == Attributes.Category.GROUP);
        if (group && value instanceof Value.Literal l && l.numeric()) return "Move.group(Literal.text(" + Emitter.quote(written(l)) + ", cp), " + target + ")";
        return (group ? "Move.group(" : "Move.move(") + sending(value) + ", " + target + ")";
    }

    /** A numeric literal's characters as written, its sign kept, its point and a leading + left out (`060820000200`, not its value's digits). */
    // Unsure: the characters a numeric literal gives a group MOVE or a DISPLAY are taken as written, leading zeros kept; cobc agrees (NC202A), IBM's text speaks of the literal's value (J-130). Stays: an IBM run of a group MOVE of a literal with leading zeros settles it.
    static String written(Value.Literal l) {
        return l.text().replace("+", "").replace(".", "").replace(",", "");
    }

    /** Whether a value is numeric: a numeric item, a numeric literal, an arithmetic expression. */
    boolean numeric(Value v) {
        if (counter(v) != null) return true;
        return switch (v) {
            case Value.Field f -> List.of(Attributes.Category.NUMERIC, Attributes.Category.INDEX).contains(item(f.item()).attributes().category()) && f.start() == null;
            case Value.Literal l -> l.numeric();
            case Value.Expression e when Funcs.name(e) != null -> funcs.numeric(e);
            case Value.Expression e -> List.of("+", "-", "*", "/", "**", "neg", "pos", "LENGTH OF").contains(e.operator());
            case Value.Figurative f -> singular(f.name()).equals("ZERO") && f.all() == null;
            default -> false;
        };
    }
}
