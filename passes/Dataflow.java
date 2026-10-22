package katana.passes;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import katana.sir.types.Item;
import katana.sir.types.Procedure;
import katana.sir.types.Program;
import katana.sir.types.Statement;
import katana.sir.types.Value;

/**
 * 6.6's dataflow (research §44): the bytes of WORKING-STORAGE written on every path from the program's entry, a
 * must-analysis over paragraphs whose entries meet every edge that may reach them (falls, GO TOs, PERFORMs). Bytes,
 * not items, so groups, FILLER, REDEFINES and RENAMES are one model. Wherever it is not exact it errs towards "not
 * written"; a read of a byte that some statement writes and that is not written on every path to it is a finding.
 */
final class Dataflow {
    /** Receivers a statement writes whole, for certain when whole, unsubscripted and not reference-modified. */
    private static final Set<String> WRITES = Set.of("move-to-statement", "set-to", "add-giving", "subtract-giving", "multiply-giving-result", "compute-store", "add-to", "subtract-minuend",
            "multiply-regular-operand", "initialize-statement", "accept-statement");
    /** Receivers only written, never read by their statement. */
    private static final Set<String> ONLY_WRITTEN = Set.of("move-to-statement", "set-to", "add-giving", "subtract-giving", "multiply-giving-result", "divide-giving", "divide-remainder",
            "compute-store", "initialize-statement", "accept-statement", "read-into", "return-into", "string-into-phrase", "unstring-into", "unstring-count-in", "unstring-delimiter-in",
            "move-corresponding-to-statement");
    /** Receivers that may be written in part, or not at all: never a write for certain. */
    private static final Set<String> MAYBE = Set.of("read-into", "return-into", "string-into-phrase", "string-with-pointer-phrase", "unstring-into", "unstring-count-in", "unstring-delimiter-in",
            "unstring-tallying-phrase", "unstring-with-pointer-phrase", "search-varying", "move-corresponding-to-statement", "divide-into", "divide-giving", "divide-remainder", "inspect-to");

    private final Program p;
    private final Map<Integer, Integer> base = new HashMap<>();
    /** Every WORKING-STORAGE byte some statement may write. */
    final BitSet written = new BitSet();
    /** Each read that the analysis cannot show written first: `NAME at line N`. */
    final List<String> findings = new ArrayList<>();
    /** While an INITIALIZE without REPLACING's receivers are read: a group then writes its receivers, not its FILLER. */
    private boolean initializing;
    private final BitSet[] in;
    private final BitSet[] end;

    /** Lays WORKING-STORAGE's records out byte by byte, collects every byte any statement writes, then solves and checks from the first paragraph. */
    Dataflow(Program p) {
        this.p = p;
        int next = 0;
        for (Item i : p.items()) if (i.record() == i.id() && i.section() == Item.Section.WORKING_STORAGE) {
            base.put(i.id(), next);
            next += Math.max(1, i.length());
        }
        int n = p.procedure().paragraphs().size();
        in = new BitSet[n];
        end = new BitSet[n];
        for (Procedure.Paragraph g : p.procedure().paragraphs()) for (Statement s : g.statements()) collect(s);
        if (n > 0) {
            in[0] = new BitSet();
            if (solve()) check();
            else findings.add("the dataflow did not settle");
        }
    }

    /** The bytes an item occupies, a table element its whole table's; empty for an item outside WORKING-STORAGE. */
    BitSet bytes(Value.Field f) {
        Item i = p.items().get(f.item() - 1);
        Item whole = i;
        if (!f.subscripts().isEmpty()) for (Item a = i; a != null; a = a.parent() == 0 ? null : p.items().get(a.parent() - 1)) if (a.occurs() != null) whole = a;
        BitSet b = new BitSet();
        Integer at = base.get(whole.record());
        if (at == null) return b;
        int length = whole.occurs() != null ? whole.length() * whole.occurs().max() : whole.length();
        b.set(at + whole.offset(), at + whole.offset() + Math.max(1, length));
        return b;
    }

    /** Every byte a statement may write, its branches' statements too. */
    private void collect(Statement s) {
        initializing = plainInitialize(s);
        for (Statement.Operand o : s.operands()) if (receives(s, o.role())) written.or(target(o.value()));
        initializing = false;
        if (s.verb().equals("INSPECT") || s.verb().equals("CALL")) for (Statement.Operand o : s.operands()) fields(o.value(), written);
        if (s.verb().equals("PERFORM")) for (Statement.Branch b : s.branches()) varied(b.condition(), written);
        for (Statement.Operand o : s.operands()) varied(o.value(), written);
        for (Statement.Branch b : s.branches()) for (Statement t : b.statements()) collect(t);
    }

    private static boolean receives(Statement s, String role) {
        return WRITES.contains(role) || MAYBE.contains(role) || role.equals("divide-into");
    }

    private BitSet target(Value v) {
        if (v instanceof Value.Field f && initializes(f)) return initialized(f.item());
        if (v instanceof Value.Field f) return bytes(f);
        if (v instanceof Value.ConditionName c) return bytes(new Value.Field(c.item(), c.subscripts(), null, null));
        return new BitSet();
    }

    /** Whether a field is an unsubscripted group INITIALIZE names: it writes the group's receivers, not its FILLER. */
    private boolean initializes(Value.Field f) {
        return initializing && f.subscripts().isEmpty() && f.start() == null && p.items().get(f.item() - 1).attributes().category() == katana.sir.types.Attributes.Category.GROUP;
    }

    /** The bytes INITIALIZE of a group writes (LR 6.4 p. 366, as `lower/Initialize` reads it): each elementary item under it but FILLER, none under a REDEFINES below it, every occurrence. */
    private BitSet initialized(int target) {
        BitSet out = new BitSet();
        for (Item i : p.items()) {
            if (i.attributes().category() == katana.sir.types.Attributes.Category.GROUP || i.name().equals("FILLER") || i.level() == 66 || i.level() == 88) continue;
            boolean under = false;
            for (Item a = i; a != null && !under; a = a.parent() == 0 ? null : p.items().get(a.parent() - 1)) {
                if (a.id() == target) under = true;
                else if (a.redefines() != 0) break;
            }
            if (under) out.or(bytes(new Value.Field(i.id(), i.occurs() != null || table(i) ? List.of(new Value.Literal("1", Value.Literal.Kind.NUMERIC, "1", 0)) : List.of(), null, null)));
        }
        return out;
    }

    private boolean table(Item i) {
        for (Item a = i; a != null; a = a.parent() == 0 ? null : p.items().get(a.parent() - 1)) if (a.occurs() != null) return true;
        return false;
    }

    /** The bytes of every field a value names, its subscripts' included. */
    private void fields(Value v, BitSet out) {
        switch (v) {
            case Value.Field f -> {
                out.or(bytes(f));
                f.subscripts().forEach(x -> fields(x, out));
            }
            case Value.ConditionName c -> out.or(target(c));
            case Value.Expression e -> e.operands().forEach(x -> fields(x, out));
            case null, default -> {}
        }
    }

    /** A PERFORM VARYING's identifiers, which it writes: a field directly under a `perform-varying-phrase`. */
    private void varied(Value v, BitSet out) {
        if (v instanceof Value.Expression e && e.operator().equals("perform-varying-phrase")) for (Value c : e.operands()) if (c instanceof Value.Field f) out.or(bytes(f));
        if (v instanceof Value.Expression e) e.operands().forEach(c -> varied(c, out));
    }

    /** A fixed point: each paragraph's entry the meet of every edge reaching it, the program's entry nothing written; any fixed point of these equations errs towards "not written". False when it does not settle. */
    private boolean solve() {
        boolean changed = true;
        for (int round = 0; changed; round++) {
            if (round > 50 + 4 * in.length) return false;
            BitSet[] ends = end.clone();
            Map<Integer, BitSet> reach = new HashMap<>();
            reach.put(0, new BitSet());
            List<Procedure.Paragraph> all = p.procedure().paragraphs();
            for (int g = 0; g < all.size(); g++) {
                if (in[g] == null) continue;
                BitSet state = (BitSet) in[g].clone();
                for (Statement s : all.get(g).statements()) state = flow(s, state, reach, false);
                end[g] = state;
                if (state != null && g + 1 < all.size()) meet(reach, g + 1, state);
            }
            changed = !java.util.Arrays.equals(ends, end);
            for (int g = 0; g < all.size(); g++) {
                BitSet next = reach.get(g);
                if (next != null && !next.equals(in[g])) {
                    in[g] = next;
                    changed = true;
                }
            }
        }
        return true;
    }

    private static void meet(Map<Integer, BitSet> reach, int g, BitSet state) {
        BitSet old = reach.get(g);
        if (old == null) reach.put(g, (BitSet) state.clone());
        else old.and(state);
    }

    /** One statement's effect on the bytes written on every path: null once no path goes on; `checking` records each read not written first. */
    private BitSet flow(Statement s, BitSet state, Map<Integer, BitSet> reach, boolean checking) {
        if (state == null) return null;
        BitSet before = entering(s, state);
        if (checking) reads(s, before);
        BitSet after = transfer(s, writes(s, before), reach);
        if (after == null) return null;
        BitSet out = after;
        for (Statement.Branch b : s.branches()) {
            BitSet branch = (BitSet) after.clone();
            for (Statement t : b.statements()) branch = flow(t, branch, reach, checking);
            if (branch != null) out.and(branch);
        }
        return out;
    }

    /** The state a statement's reads see: a PERFORM's FROM gives a VARYING identifier its value before UNTIL is first tested. */
    private BitSet entering(Statement s, BitSet state) {
        if (!s.verb().equals("PERFORM")) return state;
        BitSet before = (BitSet) state.clone();
        for (Statement.Operand o : s.operands()) varied(o.value(), before);
        for (Statement.Branch b : s.branches()) varied(b.condition(), before);
        return before;
    }

    /** The state after a statement's own receivers, those it writes for certain. */
    private BitSet writes(Statement s, BitSet before) {
        BitSet after = (BitSet) before.clone();
        initializing = plainInitialize(s);
        for (Statement.Operand o : s.operands()) if (WRITES.contains(o.role()) && definite(s, o)) after.or(target(o.value()));
        initializing = false;
        return after;
    }

    /** Where control goes after a statement: GO TO feeds its targets and ends the path (but DEPENDING), GOBACK and STOP RUN end it (null), a PERFORM runs its range. */
    private BitSet transfer(Statement s, BitSet after, Map<Integer, BitSet> reach) {
        switch (s.verb()) {
            case "GO TO" -> {
                for (Statement.Operand o : s.operands()) if (o.value() instanceof Value.Target t && reach != null) meet(reach, t.paragraph(), after);
                return s.operands().stream().anyMatch(o -> o.value() instanceof Value.Field) ? after : null;
            }
            case "GOBACK" -> {
                return null;
            }
            case "STOP" -> {
                // STOP RUN ends the run; STOP with a literal pauses and goes on.
                return s.operands().stream().anyMatch(o -> o.value() instanceof Value.Name n && n.text().equals("RUN")) ? null : after;
            }
            case "PERFORM" -> {
                return perform(s, after, reach);
            }
            default -> {
                return after;
            }
        }
    }

    /** An out-of-line PERFORM: its range's entry fed this state; after it, the range's writes to its end if it runs once for certain. */
    private BitSet perform(Statement s, BitSet state, Map<Integer, BitSet> reach) {
        Value.Target t = Loops.target(s);
        if (t == null) return state;
        if (reach != null) meet(reach, t.paragraph(), state);
        boolean once = s.operands().stream().noneMatch(o -> loops(o.role(), o.value())) && s.branches().stream().noneMatch(b -> loops(b.label(), b.condition()));
        BitSet after = (BitSet) state.clone();
        if (once && end[t.thru()] != null) after.or(end[t.thru()]);
        return after;
    }

    /** Whether a PERFORM's phrase may run its range other than once: TIMES, UNTIL or VARYING, anywhere in it. */
    private static boolean loops(String role, Value v) {
        if (role != null && (role.contains("times") || role.contains("until") || role.contains("varying"))) return true;
        return v instanceof Value.Expression e && (loops(e.operator(), null) || e.operands().stream().anyMatch(x -> loops(null, x)));
    }

    /** A receiver written for certain: whole, unsubscripted, not reference-modified; an arithmetic one only without SIZE ERROR's phrase or a division; ACCEPT only FROM a date or time; INITIALIZE only of an elementary item. */
    private boolean definite(Statement s, Statement.Operand o) {
        if (!(o.value() instanceof Value.Field f && f.subscripts().isEmpty() && f.start() == null) && !(o.value() instanceof Value.ConditionName c && c.subscripts().isEmpty())) return false;
        if (Set.of("add-giving", "subtract-giving", "multiply-giving-result", "compute-store", "add-to", "subtract-minuend", "multiply-regular-operand").contains(o.role()))
            return s.branches().isEmpty() && !divides(s);
        if (o.role().equals("accept-statement")) return s.operands().stream().anyMatch(x -> x.role().equals("accept-from-date-statement"));
        if (o.role().equals("initialize-statement")) return plainInitialize(s);
        return true;
    }

    /** An INITIALIZE without REPLACING, which writes every receiver it has (with REPLACING, only the categories named). */
    private static boolean plainInitialize(Statement s) {
        return s.verb().equals("INITIALIZE") && s.operands().stream().noneMatch(o -> o.role().startsWith("initialize-replacing"));
    }

    private static boolean divides(Statement s) {
        return s.operands().stream().anyMatch(o -> holds(o.value(), "/") || holds(o.value(), "**"));
    }

    private static boolean holds(Value v, String operator) {
        return v instanceof Value.Expression e && (e.operator().equals(operator) || e.operands().stream().anyMatch(x -> holds(x, operator)));
    }

    /** The bytes a statement reads before it writes: every operand not only received, a CALL's arguments, subscripts; recorded when some statement writes them and this path may not have. */
    private void reads(Statement s, BitSet state) {
        BitSet r = new BitSet();
        for (Statement.Operand o : s.operands()) {
            if (ONLY_WRITTEN.contains(o.role()) && o.value() instanceof Value.Field f) f.subscripts().forEach(x -> fields(x, r));
            else if (!ONLY_WRITTEN.contains(o.role())) fields(o.value(), r);
        }
        for (Statement.Branch b : s.branches()) if (b.condition() != null) fields(b.condition(), r);
        r.and(written);
        r.andNot(state);
        if (!r.isEmpty()) findings.add(s.verb() + " at line " + s.origin().line() + " reads bytes no path need have written");
    }

    /** With the fixed point: every paragraph walked once more, each read held to the bytes written on every path to it. */
    private void check() {
        List<Procedure.Paragraph> all = p.procedure().paragraphs();
        for (int g = 0; g < all.size(); g++) {
            BitSet state = in[g] == null ? null : (BitSet) in[g].clone();
            for (Statement s : all.get(g).statements()) state = flow(s, state, null, true);
        }
    }
}
