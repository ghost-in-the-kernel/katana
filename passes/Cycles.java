package katana.passes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import katana.sir.types.Program;
import katana.sir.types.Statement;
import katana.sir.types.Value;

/**
 * The loops of a region (research §38): each GO TO back, to a paragraph at or before the one it is in, makes its
 * target a loop's header, the loop running to the last paragraph that jumps back to it. Written as a labeled
 * `while`, a loop must be entered only at its header, and two loops nest or are apart: what text order makes
 * of a reducible graph. Read on the GO TO statements, as the lowering reads them (J-212's lesson, edges and
 * statements read alike).
 */
final class Cycles {
    private Cycles() {}

    /** Each loop's header to its last paragraph; a reason in `why` for each loop the `while` form cannot say. */
    // Decision: R-033
    static Map<Integer, Integer> cycles(Program p, List<Integer> ps, List<String> why) {
        List<int[]> jumps = new ArrayList<>();
        for (int g : ps) for (Statement s : p.procedure().paragraphs().get(g).statements()) gotos(s, g, jumps);
        Map<Integer, Integer> loops = new TreeMap<>();
        for (int[] j : jumps) if (j[1] <= j[0] && ps.contains(j[1])) loops.merge(j[1], j[0], Math::max);
        List<Map.Entry<Integer, Integer>> all = new ArrayList<>(loops.entrySet());
        for (int i = 0; i < all.size(); i++) {
            for (int k = i + 1; k < all.size(); k++) {
                Map.Entry<Integer, Integer> a = all.get(i), b = all.get(k);
                if (b.getKey() <= a.getValue() && b.getValue() > a.getValue()) why.add("loops at " + name(p, a.getKey()) + " and " + name(p, b.getKey()) + " overlap");
            }
        }
        for (Map.Entry<Integer, Integer> l : loops.entrySet()) {
            for (int[] j : jumps) {
                boolean inside = j[1] > l.getKey() && j[1] <= l.getValue(), from = j[0] >= l.getKey() && j[0] <= l.getValue();
                if (inside && !from) why.add("a GO TO at line " + j[2] + " enters the loop at " + name(p, l.getKey()) + " past its header");
            }
        }
        return loops;
    }

    /** Each GO TO naming one paragraph: from, to, line. */
    private static void gotos(Statement s, int from, List<int[]> out) {
        if (s.verb().equals("GO TO")) for (Statement.Operand o : s.operands()) if (o.value() instanceof Value.Target t) out.add(new int[] {from, t.paragraph(), s.origin().line()});
        for (Statement.Branch b : s.branches()) for (Statement t : b.statements()) gotos(t, from, out);
    }

    private static String name(Program p, int g) {
        return p.procedure().paragraphs().get(g).name();
    }
}
