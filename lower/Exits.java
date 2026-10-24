package katana.lower;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import katana.sir.types.Statement;

/**
 * EXIT PERFORM [CYCLE] (R-090; IBM's, recollection, R-082): inside an inline PERFORM, EXIT PERFORM leaves it and
 * EXIT PERFORM CYCLE goes on to its next cycle, the end of the body, where a VARYING's step runs and its condition
 * is tested. The innermost inline PERFORM's way out is on a stack while its body is lowered: a Java `break` and
 * `continue` when the loop is a Java loop, a jump to its labels when it is stretches (`Flow.jump`). Outside an
 * inline PERFORM it is a finding (P-03).
 */
final class Exits {
    private final Flow f;
    private final Deque<Way> ways = new ArrayDeque<>();

    /** How the innermost inline PERFORM is left, and how its next cycle is reached. */
    private record Way(Runnable leave, Runnable cycle) {}

    Exits(Flow f) {
        this.f = f;
    }

    /** `body` lowered inside a Java loop (or block) of its own: leaving is `break`, a cycle is `cycle` (a Java statement). */
    void plain(String cycle, Runnable body) {
        within(new Way(() -> f.e.line("if (true) break;"), () -> f.e.line(cycle)), body);
    }

    /** `body` lowered as stretches: leaving is a jump to `leave`, a cycle a jump to `cycle`. */
    void stretch(int leave, int cycle, Runnable body) {
        within(new Way(() -> f.jump(String.valueOf(leave)), () -> f.jump(String.valueOf(cycle))), body);
    }

    private void within(Way way, Runnable body) {
        ways.push(way);
        try {
            body.run();
        } finally {
            ways.pop();
        }
    }

    /** EXIT PERFORM or EXIT PERFORM CYCLE, by the innermost inline PERFORM being lowered. */
    // Decision: R-090
    void exit(Statement s) {
        Way way = ways.peek();
        if (way == null) f.e.line(f.x.unsupported("EXIT PERFORM outside an inline PERFORM at " + s.origin() + ": IBM's EXIT PERFORM is written in one (recollection, R-082)") + ";");
        else (Verbs.keyword(s, "CYCLE") ? way.cycle() : way.leave()).run();
    }

    /** Whether these statements hold an EXIT PERFORM of their own, one in no inline PERFORM nested in them. */
    static boolean holds(List<Statement> list) {
        for (Statement s : list) {
            if (s.verb().equals("EXIT") && Verbs.keyword(s, "PERFORM")) return true;
            boolean inline = s.verb().equals("PERFORM") && Flow.target(s) == null;
            for (Statement.Branch b : s.branches()) if (!(inline && b.label().equals("perform-inline-statement")) && holds(b.statements())) return true;
        }
        return false;
    }
}
