package katana.passes;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import katana.lower.Lower;
import katana.sir.types.Procedure;
import katana.sir.types.Program;
import katana.sir.types.Statement;
import katana.sir.types.Value;

/**
 * 6.1's third pattern (research §36): a PERFORM VARYING of one phrase, its body structured Java, its counter
 * monotone, becomes a `for`. Each obligation read on sir/'s statements and graph; a loop that fails one stays
 * stretches, and the pass says why (R-032).
 */
final class Loops {
    /** The verbs whose any operand naming the counter may store into it: conservative, as a store missed would let a `for` lie. */
    private static final Set<String> STORING_VERBS = Set.of("MULTIPLY", "DIVIDE", "SET", "INSPECT", "CALL", "INITIALIZE", "ACCEPT", "STRING", "UNSTRING", "READ", "RETURN");
    private static final Set<String> STORING_ROLES = Set.of("move-to-statement", "move-corresponding-to-statement", "add-to", "add-giving", "add-to-giving", "subtract-minuend",
            "subtract-giving", "subtract-minuend-giving", "subtract-minuend-corresponding", "compute-store");

    private Loops() {}

    /** The PERFORM VARYINGs that become `for` loops, inner ones decided first; each refused one with why, in `refused`. */
    // Decision: R-032
    static Set<String> loops(Program p, Map<Integer, Integer> methods, Map<String, List<String>> refused) {
        Set<String> out = new HashSet<>();
        for (Procedure.Paragraph g : p.procedure().paragraphs()) for (Statement s : g.statements()) visit(p, s, methods, out, refused);
        return out;
    }

    /** A statement's inner statements first, then the statement. */
    private static void visit(Program p, Statement s, Map<Integer, Integer> methods, Set<String> out, Map<String, List<String>> refused) {
        for (Statement.Branch b : s.branches()) for (Statement t : b.statements()) visit(p, t, methods, out, refused);
        if (!s.verb().equals("PERFORM") || Lower.varying(s).ids().isEmpty()) return;
        List<String> why = refusals(p, s, methods, out);
        if (why.isEmpty()) out.add(Lower.Plan.loop(s.origin()));
        else refused.put("PERFORM VARYING at line " + s.origin().line(), why);
    }

    static List<String> refusals(Program p, Statement s, Map<Integer, Integer> methods, Set<String> loops) {
        List<String> why = new ArrayList<>();
        Lower.Varying v = Lower.varying(s);
        if (v.ids().size() != 1) why.add("it varies " + v.ids().size() + " identifiers (AFTER)");
        Value by = v.bys().get(0);
        if (by != null && !(by instanceof Value.Literal l && l.numeric() && !l.payload().matches("-?0"))) why.add("its BY is no constant other than zero");
        Value.Target target = target(s);
        List<Statement> body = body(s);
        for (Statement t : body) unstructured(t, methods, loops, why);
        int counter = v.ids().get(0).item();
        for (Statement t : scope(p, body, target)) if (t != s && stores(p, t, counter)) why.add("line " + t.origin().line() + " may store into its counter");
        return why;
    }

    /** What keeps code from being structured Java: a VARYING no `for`, a SEARCH, a SORT's procedure (a PERFORM of a range no method is `dispatch`, research §38). */
    static void unstructured(Statement t, Map<Integer, Integer> methods, Set<String> loops, List<String> why) {
        if (t.verb().equals("PERFORM") && !Lower.varying(t).ids().isEmpty() && !loops.contains(Lower.Plan.loop(t.origin()))) why.add("line " + t.origin().line() + " is a VARYING that is no for");
        if (t.verb().equals("SEARCH")) why.add("line " + t.origin().line() + " is a SEARCH");
        if ((t.verb().equals("SORT") || t.verb().equals("MERGE")) && t.operands().stream().anyMatch(o -> o.value() instanceof Value.Target)) why.add("line " + t.origin().line() + " has a procedure");
        for (Statement.Branch b : t.branches()) for (Statement u : b.statements()) unstructured(u, methods, loops, why);
    }

    /** Every statement the loop runs: its body, and the paragraphs any PERFORM in it reaches, through theirs. */
    private static List<Statement> scope(Program p, List<Statement> body, Value.Target target) {
        List<Statement> out = new ArrayList<>();
        Deque<int[]> ranges = new ArrayDeque<>();
        if (target != null) ranges.push(new int[] {target.paragraph(), target.thru()});
        for (Statement t : body) flatten(t, out, ranges);
        Set<Integer> seen = new HashSet<>();
        while (!ranges.isEmpty()) {
            int[] r = ranges.pop();
            for (int g = r[0]; g <= r[1] && g < p.procedure().paragraphs().size(); g++) {
                if (!seen.add(g)) continue;
                for (Statement t : p.procedure().paragraphs().get(g).statements()) flatten(t, out, ranges);
            }
        }
        return out;
    }

    private static void flatten(Statement t, List<Statement> out, Deque<int[]> ranges) {
        out.add(t);
        Value.Target target = target(t);
        if (target != null) ranges.push(new int[] {target.paragraph(), target.thru()});
        for (Statement.Branch b : t.branches()) for (Statement u : b.statements()) flatten(u, out, ranges);
    }

    /** Whether a statement may store into the counter, or a group holding it: by a receiving operand, a verb that may store into any it names, or a VARYING of its own. */
    private static boolean stores(Program p, Statement t, int counter) {
        boolean anyStores = STORING_VERBS.contains(t.verb()) || t.verb().equals("PERFORM") && Lower.varying(t).ids().stream().anyMatch(id -> holds(p, id.item(), counter));
        for (Statement.Operand o : t.operands()) {
            if (!(o.value() instanceof Value.Field f) || !holds(p, f.item(), counter)) continue;
            if (anyStores || STORING_ROLES.contains(o.role())) return true;
        }
        return anyStores && t.verb().equals("PERFORM");
    }

    /** Whether `outer` is the counter or a group holding it. */
    private static boolean holds(Program p, int outer, int counter) {
        for (int at = counter; at > 0; at = p.items().get(at - 1).parent()) if (at == outer) return true;
        return false;
    }

    static Value.Target target(Statement s) {
        Value.Target first = null, last = null;
        for (Statement.Operand o : s.operands()) {
            if (!(o.value() instanceof Value.Target t) || !o.role().equals("perform-procedure-statement")) continue;
            if (first == null) first = t;
            last = t;
        }
        return first == null ? null : new Value.Target(first.paragraph(), last.thru(), first.section());
    }

    private static List<Statement> body(Statement s) {
        for (Statement.Branch b : s.branches()) if (b.label().equals("perform-inline-statement")) return b.statements();
        return List.of();
    }
}
