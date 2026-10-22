package katana.passes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import katana.lower.Lower;
import katana.sir.types.Attributes;
import katana.sir.types.Call;
import katana.sir.types.FileDescriptor;
import katana.sir.types.Item;
import katana.sir.types.Procedure;
import katana.sir.types.Program;
import katana.sir.types.Statement;
import katana.sir.types.Value;

/**
 * 6.3, scalar promotion (research §39, R-068): the numeric items whose bytes nothing but the item's own
 * reads and stores touches, which `lower/` writes as Java variables, every store keeping COBOL's truncation.
 * An item that fails an obligation keeps its bytes, and the pass says why.
 */
public final class Scalars {
    public static final String PASS = "scalars-v1";
    /** The verbs a promoted item may appear in: their reads go through its value or a field encoding it, their stores through `Decimal.store`. */
    private static final Set<String> VERBS = Set.of("MOVE", "ADD", "SUBTRACT", "MULTIPLY", "DIVIDE", "COMPUTE", "DISPLAY", "IF", "EVALUATE", "PERFORM",
            "INITIALIZE", "SET", "STRING", "UNSTRING", "ACCEPT", "CALL");
    /** The verbs that write it through a field of its own (research §40), `lower/`'s one set: a statement of theirs with statements of its own would let them read it before its value is taken back. */
    private static final Set<String> THROUGH = Lower.THROUGH;

    private Scalars() {}

    /** The items promoted; each item refused, by `item NAME`, with why. */
    // Decision: R-032
    public static Set<Integer> promote(Program p, Map<String, List<String>> refused) {
        if (p.procedure().sections().stream().anyMatch(Procedure.Section::declarative)) return Set.of();
        Uses uses = new Uses();
        for (Procedure.Paragraph g : p.procedure().paragraphs()) for (Statement s : g.statements()) uses.statement(s);
        Set<Integer> named = named(p);
        Set<Integer> out = new TreeSet<>();
        for (Item i : p.items()) {
            if (i.attributes().category() != Attributes.Category.NUMERIC || i.level() == 66 || i.level() == 88) continue;
            List<String> why = new ArrayList<>();
            item(i, why);
            aliases(p, i, uses, named, why);
            own(i, uses, why);
            if (why.isEmpty()) out.add(i.id());
            else refused.put("item " + i.name(), why);
        }
        return out;
    }

    /** Obligation 1: the item itself. */
    private static void item(Item i, List<String> why) {
        Attributes a = i.attributes();
        if (!Set.of(Attributes.Usage.DISPLAY, Attributes.Usage.BINARY, Attributes.Usage.PACKED).contains(a.usage())) why.add("usage " + a.usage());
        if (a.digits() > 18) why.add(a.digits() + " digits, over a long's 18");
        if (a.scale() < 0 || a.picture() != null && a.picture().toUpperCase().contains("P")) why.add("a PICTURE with P");
        if (i.section() != Item.Section.WORKING_STORAGE) why.add("in " + i.section());
        if (!i.flags().isEmpty()) why.add(i.flags().toString());
    }

    /** Obligations 2 and 3: nothing reaches its bytes but itself, through it or a group holding it, or a RENAMES over it. */
    private static void aliases(Program p, Item i, Uses uses, Set<Integer> named, List<String> why) {
        Set<Integer> redefined = new HashSet<>();
        for (Item r : p.items()) if (r.redefines() != 0) {
            redefined.add(r.redefines());
            redefined.add(r.id());
        }
        for (Item a = i; a != null; a = a.parent() == 0 ? null : p.items().get(a.parent() - 1)) {
            if (redefined.contains(a.id())) why.add(a.name() + " redefines or is redefined");
            if (named.contains(a.id())) why.add(a.name() + " is named by a file, a table or a CALL");
            if (a.occurs() != null) why.add(a.name() + " occurs");
            if (uses.refmod.contains(a.id())) why.add(a.name() + " is reference-modified");
            if (a != i) group(a, uses, why);
        }
        for (Item r : p.items()) if (r.level() == 66 && r.record() == i.record() && r.offset() < i.offset() + i.length() && i.offset() < r.offset() + r.length()) why.add(r.name() + " RENAMES it");
    }

    /** A group holding the item: used whole, given a VALUE, or EXTERNAL or GLOBAL (written on the record; its bytes are another program's too). */
    private static void group(Item a, Uses uses, List<String> why) {
        if (uses.verbs.containsKey(a.id())) why.add(a.name() + ", a group holding it, is used by " + uses.verbs.get(a.id()));
        if (a.value() != null) why.add(a.name() + ", a group holding it, has a VALUE");
        if (a.flags().contains(Item.Flag.EXTERNAL) || a.flags().contains(Item.Flag.GLOBAL)) why.add(a.name() + ", a group holding it, is " + a.flags());
    }

    /** Obligations 4 and 5: its own uses, each one the lowering converts. */
    private static void own(Item i, Uses uses, List<String> why) {
        Set<String> verbs = uses.verbs.get(i.id());
        if (verbs == null) {
            why.add("never referenced");
            return;
        }
        for (String v : verbs) if (!VERBS.contains(v)) why.add("used by " + v);
        if (uses.varied.contains(i.id())) why.add("varied by a PERFORM");
        if (uses.raw.contains(i.id())) why.add("ACCEPTed from the input device, whose bytes may be no number");
        if (uses.phrased.contains(i.id())) why.add("written by a statement with ON OVERFLOW or ON EXCEPTION statements");
    }

    /** Every item named outside the procedure's statements: REDEFINES, ODO objects, table keys and indexes, a file's keys, status, record length and LINAGE, a CALL's arguments but BY CONTENT. */
    private static Set<Integer> named(Program p) {
        Set<Integer> out = new HashSet<>();
        for (Item i : p.items()) if (i.occurs() != null) {
            out.add(i.occurs().dependingOn());
            i.occurs().keys().forEach(k -> out.add(k.item()));
            out.addAll(i.occurs().indexedBy());
        }
        for (FileDescriptor f : p.files()) {
            out.addAll(List.of(f.recordKey(), f.relativeKey(), f.status(), f.vsamStatus()));
            f.alternateKeys().forEach(k -> out.add(k.item()));
            if (f.fd() != null) {
                out.add(f.fd().recordDepending());
                FileDescriptor.Linage l = f.fd().linage();
                if (l != null) for (Value v : java.util.Arrays.asList(l.body(), l.footing(), l.top(), l.bottom())) fields(v, out);
            }
        }
        // A CALL's argument BY CONTENT is a copy the called program cannot write; BY REFERENCE (or BY VALUE) is not.
        for (Call c : p.calls()) {
            for (Call.Argument a : c.using()) if (a.mode() != Call.Mode.CONTENT) fields(a.value(), out);
            fields(c.returning(), out);
        }
        out.remove(0);
        return out;
    }

    private static void fields(Value v, Set<Integer> out) {
        if (v instanceof Value.Field f) out.add(f.item());
        if (v instanceof Value.Expression e) e.operands().forEach(o -> fields(o, out));
    }

    /** Each item's verbs (an 88's under `SET` when set), its reference modifications, and the identifiers PERFORM varies. */
    private static final class Uses {
        final Map<Integer, Set<String>> verbs = new HashMap<>();
        final Set<Integer> refmod = new HashSet<>(), varied = new HashSet<>(), raw = new HashSet<>(), phrased = new HashSet<>();

        void statement(Statement s) {
            through(s);
            for (Statement.Operand o : s.operands()) {
                if (s.verb().equals("PERFORM")) varying(o.role(), o.value());
                value(o.value(), s.verb());
            }
            for (Statement.Branch b : s.branches()) {
                // An inline PERFORM holds its VARYING phrase as its branch's condition, as lower/Performs reads it.
                if (s.verb().equals("PERFORM")) varying(b.label(), b.condition());
                value(b.condition(), s.verb());
                for (Statement t : b.statements()) statement(t);
            }
        }

        /** The items a verb writing through a field names, when it has statements of its own; an ACCEPT's from the input device (no FROM DATE, DAY or TIME). */
        void through(Statement s) {
            if (!THROUGH.contains(s.verb())) return;
            Set<Integer> named = new HashSet<>();
            for (Statement.Operand o : s.operands()) fields(o.value(), named);
            for (Statement.Operand o : s.operands()) if (o.value() instanceof Value.ConditionName c) named.add(c.item());
            if (s.branches().stream().anyMatch(b -> !b.statements().isEmpty())) phrased.addAll(named);
            if (s.verb().equals("ACCEPT") && s.operands().stream().noneMatch(o -> o.role().equals("accept-from-date-statement"))) raw.addAll(named);
        }

        /** A PERFORM's VARYING and AFTER identifiers: a field directly under a `perform-varying-phrase`, as `lower/Performs` reads them. */
        void varying(String role, Value v) {
            if (v instanceof Value.Expression e && e.operator().startsWith("perform-")) e.operands().forEach(c -> varying(e.operator(), c));
            else if (role.equals("perform-varying-phrase") && v instanceof Value.Field f) varied.add(f.item());
        }

        void value(Value v, String verb) {
            switch (v) {
                case null -> {}
                case Value.Field f -> {
                    verbs.computeIfAbsent(f.item(), k -> new TreeSet<>()).add(verb);
                    if (f.start() != null) refmod.add(f.item());
                    f.subscripts().forEach(x -> value(x, verb));
                    value(f.start(), verb);
                    value(f.length(), verb);
                }
                case Value.ConditionName c -> verbs.computeIfAbsent(c.item(), k -> new TreeSet<>()).add(verb);
                case Value.Expression e -> e.operands().forEach(x -> value(x, verb));
                default -> {}
            }
        }
    }
}
