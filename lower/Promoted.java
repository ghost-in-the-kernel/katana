package katana.lower;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.lang.model.SourceVersion;
import katana.sir.types.Attributes;
import katana.sir.types.Item;
import katana.sir.types.Program;

/**
 * The items 6.3 promotes (R-068), as Java: each a `long` (an integer item) or a `BigDecimal` (an item with
 * decimal places) named for the item, its `Field` gone; its value read directly in arithmetic, encoded into a
 * field of its own where a read needs bytes; every store the function the byte form stores by.
 */
final class Promoted {
    /** The class's own members and locals a promoted item's name must not take. */
    private static final Set<String> TAKEN = Set.of("u", "up", "called", "cp", "in", "upsi", "out", "err", "rc", "dbg", "d", "co", "sz", "ov", "ae", "cx", "P", "t", "run", "call", "end", "initial", "cancel", "main");
    private final Program p;
    private final Map<Integer, String> names = new LinkedHashMap<>();

    Promoted(Program p, Set<Integer> items) {
        this.p = p;
        Set<String> used = new HashSet<>();
        for (Item i : p.items()) if (items.contains(i.id())) names.put(i.id(), name(i, used));
    }

    /** A promoted item a statement has as a field of its own for now (`p<id>`, research §40). */
    private final Set<Integer> bound = new HashSet<>();

    /** Whether the item is a variable here: promoted, and not a field for the statement being written. */
    boolean has(int item) {
        return names.containsKey(item) && !bound.contains(item);
    }

    /** The name of the Field an item is reached by: its own `f<id>`, or for a statement that has a promoted item as a field, `p<id>`. */
    String fieldName(int item) {
        return (bound.contains(item) ? "p" : "f") + item;
    }

    /** The promoted items a statement names itself (not in its branches' statements). */
    Set<Integer> in(katana.sir.types.Statement s) {
        Set<Integer> out = new java.util.TreeSet<>();
        for (katana.sir.types.Statement.Operand o : s.operands()) items(o.value(), out);
        out.retainAll(names.keySet());
        return out;
    }

    private static void items(katana.sir.types.Value v, Set<Integer> out) {
        switch (v) {
            case katana.sir.types.Value.Field f -> {
                out.add(f.item());
                f.subscripts().forEach(x -> items(x, out));
            }
            case katana.sir.types.Value.ConditionName c -> out.add(c.item());
            case katana.sir.types.Value.Expression e -> e.operands().forEach(x -> items(x, out));
            case null, default -> {}
        }
    }

    /**
     * A statement that writes promoted items' bytes, run on a field of each item's own holding its value, the
     * value taken back after it (research §40): exact for the verbs passes/Scalars lets through, each leaving
     * bytes that encode a number.
     */
    // Decision: R-068
    void through(Set<Integer> items, Emitter e, Runnable statement) {
        e.open("");
        for (int id : items) e.line("Field p" + id + " = " + field(id) + ";");
        bound.addAll(items);
        statement.run();
        bound.removeAll(items);
        for (int id : items) e.line(assign(id, "p" + id + ".get()") + ";");
        e.close();
    }

    String name(int item) {
        return names.get(item);
    }

    /** `wsTranCount` for WS-TRAN-COUNT: lower camel case, a clash or a keyword given the item's number. */
    private static String name(Item i, Set<String> used) {
        StringBuilder b = new StringBuilder();
        for (String w : i.name().toLowerCase().split("[^a-z0-9]+")) {
            if (w.isEmpty()) continue;
            b.append(b.isEmpty() ? w : Character.toUpperCase(w.charAt(0)) + w.substring(1));
        }
        String n = b.isEmpty() || !Character.isJavaIdentifierStart(b.charAt(0)) ? "item" + b : b.toString();
        if (!SourceVersion.isName(n) || TAKEN.contains(n) || n.matches("[frsa]\\d+|perform\\d+") || !used.add(n)) n = n + "_" + i.id();
        used.add(n);
        return n;
    }

    private Item item(int id) {
        return p.items().get(id - 1);
    }

    private boolean big(int id) {
        return item(id).attributes().scale() > 0;
    }

    /** The declaration: the variable, its item's PICTURE and usage beside it, and the attributes a read needing bytes encodes it by. */
    String declaration(int id, Exprs x) {
        Item i = item(id);
        Attributes a = i.attributes();
        return "private " + (big(id) ? "java.math.BigDecimal " : "long ") + name(id) + (big(id) ? " = java.math.BigDecimal.ZERO.setScale(" + a.scale() + ")" : "") + "; // " + i.name() + " PIC " + a.picture() + " " + a.usage()
                + "\n    private static final Attr " + attr(id) + " = " + Lower.attr(p, i, x) + ";";
    }

    String attr(int id) {
        return "A" + id;
    }

    /** The value as an `rt` Decimal. */
    String decimal(int id) {
        return big(id) ? "Scalars.of(" + name(id) + ")" : "Scalars.of(" + name(id) + ", 0)";
    }

    /** A field holding the value, for a read that needs bytes. */
    String field(int id) {
        return "Scalars.field(" + attr(id) + ", cp, " + decimal(id) + ")";
    }

    /** `name = <value>` for a Decimal expression already fitted to the item. */
    String assign(int id, String decimal) {
        return name(id) + " = " + (big(id) ? "Scalars.bigOf(" : "Scalars.longOf(") + decimal + ")";
    }

    /** What a statement on a field of the item's attributes leaves, taken as its value: a MOVE into it, its VALUE. */
    String receive(int id, String statementOnScratch) {
        return assign(id, "Scalars.received(" + attr(id) + ", cp, scratch -> " + statementOnScratch + ")");
    }

    /**
     * A MOVE of a numeric literal or ZERO that the item holds as written (no more decimal places than its scale,
     * no more integer digits than its PICTURE's, no sign it would drop) as an assignment of the literal, which is
     * what MOVE's truncation leaves; null for any other value.
     */
    // Decision: R-068
    String constant(int id, katana.sir.types.Value v) {
        Attributes a = item(id).attributes();
        java.math.BigDecimal n;
        if (v instanceof katana.sir.types.Value.Literal l && l.numeric() && l.kind() == katana.sir.types.Value.Literal.Kind.NUMERIC) n = new java.math.BigDecimal(new java.math.BigInteger(l.payload()), l.scale());
        else if (v instanceof katana.sir.types.Value.Figurative f && Exprs.singular(f.name()).equals("ZERO") && f.all() == null) n = java.math.BigDecimal.ZERO;
        else return null;
        if (n.scale() > a.scale() || n.signum() < 0 && !a.signed()) return null;
        java.math.BigDecimal held = n.setScale(a.scale());
        if (held.unscaledValue().abs().compareTo(java.math.BigInteger.TEN.pow(Lower.stored(a))) >= 0) return null;
        return name(id) + " = " + (big(id) ? "new java.math.BigDecimal(\"" + held.toPlainString() + "\")" : held.unscaledValue().toString() + (held.unscaledValue().bitLength() > 31 ? "L" : ""));
    }

    /** An arithmetic store, as `Arithmetic.store` does it into the bytes; a boolean, the size error. */
    // Decision: R-068
    String store(int id, String term, String rounded, String onSizeError) {
        Attributes a = item(id).attributes();
        return "Scalars.store(" + term + ", " + Lower.stored(a) + ", " + a.scale() + ", " + a.signed() + ", " + rounded + ", " + onSizeError + ", stored -> " + assign(id, "stored") + ")";
    }
}
