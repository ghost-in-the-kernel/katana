package katana.passes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import katana.lower.Lower;
import katana.sir.contract.Contract;
import katana.sir.types.Procedure;
import katana.sir.types.Program;
import katana.sir.types.Statement;
import katana.sir.types.Value;

/**
 * 6.2 (research §37): a method range whose paragraphs need no stretch of their own, whose GO TOs all go forward
 * inside it, which nothing enters but its callers' calls, becomes its method's own code, out of the switch. A
 * range that fails an obligation stays 6.1's method over the switch, and the pass says why (R-032, R-034).
 */
final class Inline {
    private Inline() {}

    /** The method ranges, by first paragraph, whose paragraphs become the method's own code; each refused one with why, in `refused`. */
    // Decision: R-032
    static Set<Integer> inline(Program p, Map<Integer, Integer> methods, Set<String> loops, Map<String, List<String>> refused) {
        Set<Integer> out = new HashSet<>();
        for (Map.Entry<Integer, Integer> m : methods.entrySet()) {
            List<String> why = refusals(p, m.getKey(), m.getValue(), methods, loops);
            if (why.isEmpty()) out.add(m.getKey());
            else refused.put("inline " + Structuring.name(p, new katana.intent.types.Subject.Range(m.getKey(), m.getValue())), why);
        }
        return out;
    }

    static List<String> refusals(Program p, int first, int last, Map<Integer, Integer> methods, Set<String> loops) {
        List<String> why = new ArrayList<>();
        for (int g = first; g <= last; g++) {
            for (Statement s : p.procedure().paragraphs().get(g).statements()) {
                Loops.unstructured(s, methods, loops, why);
                local(s, g, last, why);
            }
        }
        entries(p, first, last, loops, why);
        return why;
    }

    /** What a statement does that the method's own code cannot say: NEXT SENTENCE, GO TO DEPENDING or back, EXIT PARAGRAPH or SECTION. */
    private static void local(Statement s, int at, int last, List<String> why) {
        int line = s.origin().line();
        if (s.verb().equals("NEXT SENTENCE")) why.add("line " + line + " is a NEXT SENTENCE");
        if (s.verb().equals("GO TO")) {
            List<Value.Target> to = s.operands().stream().filter(o -> o.value() instanceof Value.Target).map(o -> (Value.Target) o.value()).toList();
            if (to.size() != 1 || s.operands().stream().anyMatch(o -> o.value() instanceof Value.Field)) why.add("line " + line + " is a GO TO DEPENDING or an altered GO TO");
            else if (to.get(0).paragraph() <= at || to.get(0).paragraph() > last) why.add("line " + line + " is a GO TO back (a loop) or out");
        }
        if (s.verb().equals("EXIT") && s.operands().stream().anyMatch(o -> o.value() instanceof Value.Name n && (n.text().equals("PARAGRAPH") || n.text().equals("SECTION")))) why.add("line " + line + " is an EXIT PARAGRAPH or SECTION");
        for (Statement.Branch b : s.branches()) for (Statement t : b.statements()) local(t, at, last, why);
    }

    /**
     * Nothing enters it but its callers' calls: no range the dispatcher runs (a PERFORM's, or a SORT or MERGE
     * procedure's, which are no edges of the graph) overlaps it but its own, nothing PERFORMs it through the
     * dispatcher, and no paragraph from which a fall reaches its first is on the main line or a GO TO's or an
     * ALTER's target (ST146A's SORT output procedure fell into CLEAR-ODO-RECORD: J-212).
     */
    private static void entries(Program p, int first, int last, Set<String> loops, List<String> why) {
        for (int[] r : run(p)) if (r[0] <= last && r[1] >= first && !(r[0] == first && r[1] == last)) why.add("a PERFORM or a SORT procedure at line " + r[2] + " runs through it");
        for (Procedure.Paragraph g : p.procedure().paragraphs()) for (Statement s : g.statements()) dispatched(s, first, last, loops, why);
        Set<String> falls = new HashSet<>();
        for (String l : Contract.lines(p)) if (l.startsWith("falls ")) falls.add(l.split(" ")[1]);
        java.util.BitSet main = Structuring.Obligations.mainLine(p);
        Set<Integer> jumped = new HashSet<>();
        for (Procedure.Edge e : p.procedure().edges()) if (e.kind() != Procedure.Kind.PERFORM) jumped.add(e.to());
        for (int k = first - 1; k >= 0 && falls.contains(String.valueOf(k)); k--) {
            if (main.get(k) || jumped.contains(k)) {
                why.add("a fall from " + p.procedure().paragraphs().get(k).name() + ", entered otherwise than by a PERFORM, reaches it");
                return;
            }
        }
    }

    /** Every range the dispatcher runs: each PERFORM edge's, and each SORT or MERGE procedure's; with its line. */
    private static List<int[]> run(Program p) {
        List<int[]> out = new ArrayList<>();
        for (Procedure.Edge e : p.procedure().edges()) if (e.kind() == Procedure.Kind.PERFORM) out.add(new int[] {e.to(), e.thru(), e.origin().line()});
        for (Procedure.Paragraph g : p.procedure().paragraphs()) for (Statement s : g.statements()) procedures(s, out);
        return out;
    }

    private static void procedures(Statement s, List<int[]> out) {
        // Its INPUT and OUTPUT procedures, each perhaps A THRU B, taken as one span from the first named to the last: wider, never narrower.
        List<Value.Target> named = s.operands().stream().filter(o -> o.value() instanceof Value.Target).map(o -> (Value.Target) o.value()).toList();
        if ((s.verb().equals("SORT") || s.verb().equals("MERGE")) && !named.isEmpty()) {
            out.add(new int[] {named.stream().mapToInt(Value.Target::paragraph).min().orElseThrow(), named.stream().mapToInt(Value.Target::thru).max().orElseThrow(), s.origin().line()});
        }
        for (Statement.Branch b : s.branches()) for (Statement u : b.statements()) procedures(u, out);
    }

    /** A PERFORM of the range the dispatcher still runs: a VARYING no `for`, a SORT or MERGE procedure. */
    private static void dispatched(Statement s, int first, int last, Set<String> loops, List<String> why) {
        Value.Target t = Loops.target(s);
        boolean whole = t != null && t.paragraph() == first && t.thru() == last;
        if (s.verb().equals("PERFORM") && whole && !Lower.varying(s).ids().isEmpty() && !loops.contains(Lower.Plan.loop(s.origin()))) why.add("line " + s.origin().line() + " PERFORMs it VARYING through the dispatcher");
        if ((s.verb().equals("SORT") || s.verb().equals("MERGE")) && s.operands().stream().anyMatch(o -> o.value() instanceof Value.Target v && v.paragraph() == first)) why.add("line " + s.origin().line() + " runs it as a procedure");
        for (Statement.Branch b : s.branches()) for (Statement u : b.statements()) dispatched(u, first, last, loops, why);
    }
}
