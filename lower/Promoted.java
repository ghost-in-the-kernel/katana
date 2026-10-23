package katana.lower;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.lang.model.SourceVersion;
import katana.sir.types.Attributes;
import katana.sir.types.Item;
import katana.sir.types.Program;
import katana.sir.types.Statement;
import katana.sir.types.Value;

/**
 * Declares, reads and stores the items 6.3 promotes (R-068), as Java: each a `long` (an integer item), a `BigDecimal` (an item with
 * decimal places) or a `Money` (6.5, R-070) named for the item, its `Field` gone; its value read directly in
 * arithmetic, encoded into a field of its own where a read needs bytes; every store the function the byte form
 * stores by. With them, the names 6.5 gives an Enum or Flag item's condition names.
 */
final class Promoted {
    /** The class's own members and locals a promoted item's name must not take. */
    private static final Set<String> TAKEN = Set.of("u", "up", "called", "cp", "in", "upsi", "out", "err", "rc", "dbg", "d", "co", "sz", "ov", "ae", "cx", "P", "t", "run", "call", "end", "initial", "cancel", "main", "valid");
    private final Program p;
    private final Map<Integer, String> names = new LinkedHashMap<>();
    /** Money items (6.5); each Enum or Flag item's condition names as predicates, and a promoted item's single values as constants, by `item|CONDITION`. */
    private final Set<Integer> money;
    private final Map<String, String> predicates = new LinkedHashMap<>(), constants = new LinkedHashMap<>();
    private final Map<String, String> values = new LinkedHashMap<>();
    /** Every name the program's members take, so 6.7's record classes take none of them (research §46). */
    private final Set<String> used = new HashSet<>();
    /** 6.7's record classes, whose members a field is reached by (research §46); set once by `Exprs`' constructor, read through `views()`. */
    Views views;

    /** The record classes; never null once `Exprs` is built (an empty set in a literal build). */
    Views views() {
        if (views == null) throw new IllegalStateException("record classes read before Exprs built them");
        return views;
    }

    Promoted(Program p, Set<Integer> items) {
        this(p, items, Set.of(), Set.of());
    }

    /**
     * A statement the promoted items change, written here and true; false leaves it to its verb. SET of a promoted
     * item's condition TO TRUE is the constant 6.5 names (R-070); a verb in `Plan.THROUGH` that writes a promoted
     * item's bytes runs `lowering` with the item as a field of its own (research §40).
     */
    boolean intercept(Statement s, Emitter e, Runnable lowering) {
        String named = s.verb().equals("SET") ? setTrue(s) : null;
        if (named != null) {
            e.line(named);
            return true;
        }
        if (!Plan.THROUGH.contains(s.verb()) || in(s).isEmpty()) return false;
        through(in(s), e, lowering);
        return true;
    }

    /** The names, every one from one set so none clashes: the promoted items', then the predicates' and constants' (6.5, R-070). */
    Promoted(Program p, Set<Integer> items, Set<Integer> money, Set<Integer> enums) {
        this.p = p;
        this.money = Set.copyOf(money);
        for (Item i : p.items()) if (items.contains(i.id())) names.put(i.id(), name(i.name(), i.id(), used));
        for (Item i : p.items()) {
            if (!enums.contains(i.id())) continue;
            for (Item.Condition k : i.conditions()) {
                String key = i.id() + "|" + k.name();
                if (predicates.containsKey(key)) continue;
                predicates.put(key, name(k.name(), i.id(), used));
                String value = names.containsKey(i.id()) && !big(i.id()) && k.values().size() == 1 && k.values().get(0) instanceof Value.Literal l && l.kind() == Value.Literal.Kind.NUMERIC && l.scale() == 0 ? l.payload() : null;
                String constant = k.name().toUpperCase().replaceAll("[^A-Z0-9]+", "_");
                if (value != null && SourceVersion.isName(constant) && used.add(constant)) {
                    constants.put(key, constant);
                    values.put(key, value);
                }
            }
        }
    }

    /** The predicate a condition name of an Enum or Flag item is (`applAok`), or null. */
    String predicate(int item, String condition) {
        return predicates.get(item + "|" + condition);
    }

    /** A promoted item's constants: each its name, its value and its condition name. */
    List<String[]> constants(int item) {
        List<String[]> out = new ArrayList<>();
        for (Map.Entry<String, String> c : constants.entrySet()) if (c.getKey().startsWith(item + "|")) out.add(new String[] {c.getValue(), values.get(c.getKey()), c.getKey().substring(c.getKey().indexOf('|') + 1)});
        return out;
    }

    /** Each predicate as a method of the class, its body the condition test as `Conditions` writes it (6.5, R-070). */
    // Decision: R-070
    void predicates(Emitter e, Conditions conditions) {
        for (Map.Entry<String, String> k : predicates.entrySet()) {
            int item = Integer.parseInt(k.getKey().substring(0, k.getKey().indexOf('|')));
            String condition = k.getKey().substring(k.getKey().indexOf('|') + 1);
            e.open("private boolean " + k.getValue() + "()");
            e.line("return " + conditions.test(new Value.ConditionName(item, condition, List.of())) + ";");
            e.close();
            e.symbol(k.getValue(), "condition " + item);
        }
    }

    /** SET ... TO TRUE where every condition is a promoted item's with a constant: `applResult = APPL_AOK`, its first value MOVEd by name; null otherwise. */
    String setTrue(Statement s) {
        List<String> out = new ArrayList<>();
        boolean isTrue = false;
        for (Statement.Operand o : s.operands()) {
            if (o.role().equals("set-to-value") && o.value() instanceof Value.Literal l && l.payload().equals("TRUE")) isTrue = true;
            else if (o.role().equals("set-to") && o.value() instanceof Value.ConditionName c && c.subscripts().isEmpty() && has(c.item()) && constants.containsKey(c.item() + "|" + c.name())) out.add(name(c.item()) + " = " + constants.get(c.item() + "|" + c.name()) + ";");
            else if (o.role().equals("set-to")) return null;
        }
        return isTrue && !out.isEmpty() ? String.join(" ", out) : null;
    }

    boolean money(int item) {
        return money.contains(item);
    }

    /** A promoted item a statement has as a field of its own for now (`p<id>`, research §40). */
    private final Set<Integer> bound = new HashSet<>();

    /** Whether the item is a variable here: promoted, and not a field for the statement being written. */
    boolean has(int item) {
        return names.containsKey(item) && !bound.contains(item);
    }

    /** The name of the Field an item is reached by: its own `f<id>`, its record class's member (6.7), or for a statement that has a promoted item as a field, `p<id>`. */
    String fieldName(int item) {
        if (bound.contains(item)) return "p" + item;
        String member = views == null ? null : views.path(item); // null only while the record classes are being named
        return member != null ? member : "f" + item;
    }

    /** A name for a program member no other takes, from a COBOL name. */
    String reserve(String cobol, int id) {
        return name(cobol, id, used);
    }

    /** The promoted items a statement names itself (not in its branches' statements). */
    Set<Integer> in(Statement s) {
        Set<Integer> out = new TreeSet<>();
        for (Statement.Operand o : s.operands()) items(o.value(), out);
        out.retainAll(names.keySet());
        return out;
    }

    private static void items(Value v, Set<Integer> out) {
        switch (v) {
            case Value.Field f -> {
                out.add(f.item());
                f.subscripts().forEach(x -> items(x, out));
            }
            case Value.ConditionName c -> out.add(c.item());
            case Value.Expression e -> e.operands().forEach(x -> items(x, out));
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
        // A statement inside another (a phrase's statements) may go through other items; one item twice cannot, its
        // field `p<id>` declared in both blocks. passes/Scalars keeps out the items a phrased statement names itself,
        // not those in its subscripts, so the overlap is refused here, loudly.
        if (!Collections.disjoint(bound, items)) throw new IllegalStateException("promoted items " + items + " through a statement inside one already through " + bound);
        e.open("");
        for (int id : items) e.line("Field p" + id + " = " + field(id) + ";");
        bound.addAll(items);
        try {
            statement.run();
        } finally {
            bound.removeAll(items);
        }
        for (int id : items) e.line(assign(id, "p" + id + ".get()") + ";");
        e.close();
    }

    String name(int item) {
        return names.get(item);
    }

    /** `wsTranCount` for WS-TRAN-COUNT: lower camel case, a clash or a keyword given the item's number. */
    static String name(String cobol, int id, Set<String> used) {
        StringBuilder b = new StringBuilder();
        for (String w : cobol.toLowerCase().split("[^a-z0-9]+")) {
            if (w.isEmpty()) continue;
            b.append(b.isEmpty() ? w : Character.toUpperCase(w.charAt(0)) + w.substring(1));
        }
        String n = b.isEmpty() || !Character.isJavaIdentifierStart(b.charAt(0)) ? "item" + b : b.toString();
        if (!SourceVersion.isName(n) || TAKEN.contains(n) || n.matches("[frsa]\\d+|perform\\d+") || !used.add(n)) n = n + "_" + id;
        used.add(n);
        return n;
    }

    private Item item(int id) {
        return p.items().get(id - 1);
    }

    /** Whether the variable is a `BigDecimal` (decimal places) or `Money` (a Money fact), not a `long`. */
    boolean big(int id) {
        return item(id).attributes().scale() > 0 || money.contains(id);
    }

    /** The declaration: the variable, its item's PICTURE and usage beside it, and the attributes a read needing bytes encodes it by. */
    String declaration(int id, Exprs x) {
        Item i = item(id);
        Attributes a = i.attributes();
        String type = money(id) ? "Money " : big(id) ? "java.math.BigDecimal " : "long ";
        String start = money(id) ? " = Money.zero(" + a.scale() + ")" : big(id) ? " = java.math.BigDecimal.ZERO.setScale(" + a.scale() + ")" : "";
        return "private " + type + name(id) + start + "; // " + i.name() + " PIC " + a.picture() + " " + a.usage() + (money(id) ? ", money (a Money fact: the ledger has its state)" : "")
                + "\n    private static final Attr " + attr(id) + " = " + Storage.attr(p, i, x) + ";";
    }

    String attr(int id) {
        return "A" + id;
    }

    /** The value as an `rt` Decimal. */
    String decimal(int id) {
        return money(id) ? "Scalars.of(" + name(id) + ".amount())" : big(id) ? "Scalars.of(" + name(id) + ")" : "Scalars.of(" + name(id) + ", 0)";
    }

    /** A field holding the value, for a read that needs bytes. */
    String field(int id) {
        return "Scalars.field(" + attr(id) + ", cp, " + decimal(id) + ")";
    }

    /** `name = <value>` for a Decimal expression already fitted to the item. */
    String assign(int id, String decimal) {
        return name(id) + " = " + (money(id) ? "Money.of(" : big(id) ? "Scalars.bigOf(" : "Scalars.longOf(") + decimal + ")";
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
    String constant(int id, Value v) {
        Attributes a = item(id).attributes();
        BigDecimal n;
        if (v instanceof Value.Literal l && l.numeric() && l.kind() == Value.Literal.Kind.NUMERIC) n = new BigDecimal(new BigInteger(l.payload()), l.scale());
        else if (v instanceof Value.Figurative f && Exprs.singular(f.name()).equals("ZERO") && f.all() == null) n = BigDecimal.ZERO;
        else return null;
        if (n.scale() > a.scale() || n.signum() < 0 && !a.signed()) return null;
        BigDecimal held = n.setScale(a.scale());
        if (held.unscaledValue().abs().compareTo(BigInteger.TEN.pow(Storage.stored(a))) >= 0) return null;
        if (money(id)) return name(id) + " = new Money(new java.math.BigDecimal(\"" + held.toPlainString() + "\"))";
        if (big(id)) return name(id) + " = new java.math.BigDecimal(\"" + held.toPlainString() + "\")";
        for (Map.Entry<String, String> c : values.entrySet()) if (c.getKey().startsWith(id + "|") && new BigInteger(c.getValue()).equals(held.unscaledValue())) return name(id) + " = " + constants.get(c.getKey());
        return name(id) + " = " + held.unscaledValue() + (held.unscaledValue().bitLength() > 31 ? "L" : "");
    }

    /** An arithmetic store, as `Arithmetic.store` does it into the bytes; a boolean, the size error. */
    // Decision: R-068
    String store(int id, String term, String rounded, String onSizeError) {
        Attributes a = item(id).attributes();
        return "Scalars.store(" + term + ", " + Storage.stored(a) + ", " + a.scale() + ", " + a.signed() + ", " + rounded + ", " + onSizeError + ", stored -> " + assign(id, "stored") + ")";
    }
}
