package katana.lower;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import katana.sir.types.Statement;
import katana.sir.types.Value;

/**
 * A pass's structured control (6.1, 6.2, research §35 to §38), written through the one emitter (MOD-04): a
 * range's method; its paragraphs, or the main line's, as their own code, a forward GO TO a `break` out of the
 * labeled block that ends before its target and a backward one a `continue` of the labeled `while` its target
 * heads; and `dispatch`, a PERFORM through the dispatcher as a call, each with its own mark, so an end reached
 * with PERFORMs begun inside it still active unwinds to it as `Dispatcher.end` drops them.
 */
final class Regions {
    private final Flow f;
    /** While a region is written: each paragraph a jump may reach, and what reaches it. */
    private Map<Integer, String> forward, back;

    Regions(Flow f) {
        this.f = f;
    }

    boolean active() {
        return forward != null;
    }

    /** Whether a paragraph's code is a region's, out of the switch. */
    boolean inlined(int g) {
        if (f.plan.main().contains(g)) return true;
        for (int first : f.plan.inline()) if (g >= first && g <= f.plan.methods().get(first)) return true;
        return false;
    }

    /** A jump from paragraph `at` inside a region: `continue` back, `break` forward, `Halt` for the run's end; null for a target no region reaches. */
    String jump(String to, int at) {
        if (to.equals(String.valueOf(f.end()))) return "throw new Halt()";
        if (!to.matches("\\d+")) return null;
        int q = Integer.parseInt(to);
        return q <= at ? back.get(q) : forward.get(q);
    }

    /** Each method of the plan: its range's own code, or a PERFORM of it through `dispatch`. */
    // Decision: R-021
    void methods() {
        for (var m : new TreeMap<>(f.plan.methods()).entrySet()) {
            f.e.open("private void perform" + m.getKey() + "()");
            f.e.symbol("perform" + m.getKey(), "paragraph " + m.getKey());
            if (f.plan.inline().contains(m.getKey())) {
                List<Integer> ps = new ArrayList<>();
                for (int g = m.getKey(); g <= m.getValue(); g++) ps.add(g);
                region(ps);
            } else f.e.line("dispatch(" + m.getKey() + ", " + m.getValue() + ");");
            f.e.close();
        }
    }

    /**
     * `dispatch`: a PERFORM through the dispatcher returning to a mark of its own, below every label; a mark
     * that is another's, an end reached with this PERFORM begun inside it, unwinds to the call that set it.
     */
    // Decision: R-021
    void dispatch() {
        f.e.line("private static final class Unwind extends RuntimeException { final int pc; Unwind(int pc) { super(null, null, false, false); this.pc = pc; } }");
        f.e.open("private void dispatch(int first, int last)");
        f.e.line("int mark = -3 - d.depth();");
        f.e.line("int pc = d.perform(first, last, mark);");
        f.e.open("try");
        f.e.open("while (pc != mark)");
        f.e.line("if (pc == " + f.end() + ") throw new Halt();");
        f.e.line("if (pc < -2) throw new Unwind(pc);");
        f.e.line("pc = step(pc);");
        f.e.close();
        f.e.close();
        f.e.line("catch (Unwind u) { if (u.pc != mark) throw u; }");
        f.e.close();
    }

    /** Paragraphs in order as their own code, the plan's loops among them. */
    void region(List<Integer> ps) {
        Map<Integer, String> forwardBefore = forward, backBefore = back;
        forward = new HashMap<>();
        back = new HashMap<>();
        level(ps, 0, ps.size(), -1);
        forward = forwardBefore;
        back = backBefore;
    }

    /** The paragraphs from position `a` to before `b`: this level's forward targets' blocks, its loops, its paragraphs; `own` the header of the loop this level is the body of. */
    private void level(List<Integer> ps, int a, int b, int own) {
        TreeSet<Integer> targets = targets(ps, a, b, own);
        for (int q : targets.descendingSet()) {
            f.e.open("B" + q + ":");
            forward.put(q, "break B" + q);
        }
        for (int i = a; i < b; ) {
            int g = ps.get(i);
            if (targets.contains(g)) f.e.close();
            Integer last = f.plan.cycles().get(g);
            if (last != null && g != own) {
                int j = ps.indexOf(last);
                f.e.open("W" + g + ": while (true)");
                back.put(g, "continue W" + g);
                level(ps, i, j + 1, g);
                f.e.line("break W" + g + ";");
                f.e.close();
                i = j + 1;
            } else {
                f.paragraphCode(g);
                i++;
            }
        }
    }

    /** The paragraphs a forward GO TO inside the level names, outside its loops' bodies (a loop's own header is its block's). */
    private TreeSet<Integer> targets(List<Integer> ps, int a, int b, int own) {
        TreeSet<Integer> inside = new TreeSet<>();
        for (int i = a; i < b; i++) {
            int g = ps.get(i);
            Integer last = f.plan.cycles().get(g);
            if (last != null && g != own) {
                for (int k = i + 1; k <= ps.indexOf(last); k++) inside.add(ps.get(k));
            }
        }
        TreeSet<Integer> out = new TreeSet<>();
        for (int i = a; i < b; i++) for (Statement s : f.p.procedure().paragraphs().get(ps.get(i)).statements()) gotos(s, ps.get(i), ps.subList(a, b), inside, out);
        return out;
    }

    private static void gotos(Statement s, int from, List<Integer> level, java.util.Set<Integer> inside, java.util.Set<Integer> out) {
        if (s.verb().equals("GO TO")) for (Statement.Operand o : s.operands()) if (o.value() instanceof Value.Target t && t.paragraph() > from && level.contains(t.paragraph()) && !inside.contains(t.paragraph())) out.add(t.paragraph());
        for (Statement.Branch b : s.branches()) for (Statement t : b.statements()) gotos(t, from, level, inside, out);
    }
}
