package katana.passes;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import katana.sir.contract.Contract;
import katana.sir.types.Procedure;
import katana.sir.types.Program;
import katana.sir.types.Statement;

/**
 * The main line as `run`'s own code (research §38): the paragraphs control reaches from the start without a
 * PERFORM, in order, their loops labeled `while`s; only where nothing but the main line enters them, no range the
 * dispatcher runs passes through them, and their code needs no stretch. Else it stays in the switch, and the
 * pass says why (R-034).
 */
final class MainLine {
    private MainLine() {}

    /** The main line's paragraphs when they become `run`'s code; empty, and a reason in `refused`, when not. */
    // Decision: R-032
    static Set<Integer> main(Program p, Map<Integer, Integer> methods, Set<String> loops, Map<Integer, Integer> cycles, Map<String, List<String>> refused) {
        BitSet line = Structuring.Obligations.mainLine(p);
        List<Integer> ps = new ArrayList<>();
        for (int g = line.nextSetBit(0); g >= 0; g = line.nextSetBit(g + 1)) ps.add(g);
        if (ps.isEmpty()) return Set.of();
        List<String> why = new ArrayList<>();
        int last = p.procedure().paragraphs().size() - 1;
        for (int g : ps) {
            for (Statement s : p.procedure().paragraphs().get(g).statements()) {
                Loops.unstructured(s, methods, loops, why);
                Inline.local(s, g, last, why);
            }
            for (Map.Entry<Integer, Integer> m : methods.entrySet()) if (g >= m.getKey() && g <= m.getValue()) why.add(p.procedure().paragraphs().get(g).name() + " is in a method's range");
        }
        entries(p, line, why);
        Map<Integer, Integer> found = Cycles.cycles(p, ps, why);
        if (!why.isEmpty()) {
            refused.put("main line", why);
            return Set.of();
        }
        cycles.putAll(found);
        return new TreeSet<>(ps);
    }

    /** Nothing enters the main line but itself: no range the dispatcher runs passes through it, no GO TO, fall or ALTER from elsewhere reaches it. */
    private static void entries(Program p, BitSet line, List<String> why) {
        for (int[] r : Inline.run(p)) for (int g = r[0]; g <= r[1]; g++) if (line.get(g)) {
            why.add("a PERFORM or a SORT procedure at line " + r[2] + " runs " + p.procedure().paragraphs().get(g).name());
            break;
        }
        for (Procedure.Edge e : p.procedure().edges()) {
            if (e.kind() == Procedure.Kind.GO_TO && line.get(e.to()) && !line.get(e.from())) why.add("a GO TO at line " + e.origin().line() + " enters it from outside it");
            if (e.kind() == Procedure.Kind.ALTER && (line.get(e.to()) || line.get(e.from()))) why.add("an ALTER at line " + e.origin().line() + " reaches it");
        }
        for (String l : Contract.lines(p)) {
            String[] w = l.split(" ");
            if (w[0].equals("falls") && w[1].equals(p.header().program()) && !line.get(Integer.parseInt(w[2])) && line.get(Integer.parseInt(w[3]))) why.add(p.procedure().paragraphs().get(Integer.parseInt(w[2])).name() + " falls into it");
        }
    }
}
