package katana.lower;

import java.util.ArrayList;
import java.util.List;
import katana.sir.types.Statement;
import katana.sir.types.Value;

/**
 * PERFORM in its forms (LR pp. 412-425): out of line through the dispatcher (R-021), once, TIMES, UNTIL
 * (TEST BEFORE or AFTER), VARYING with AFTER; inline, the same loops over the statements written in it.
 * The loop's control is read from the operands (out of line) or the inline branch's condition (inline),
 * where the IR keeps it as a tree of `perform-*` phrases.
 */
final class Performs {
    private final Flow f;

    Performs(Flow f) {
        this.f = f;
    }

    /** One VARYING or AFTER phrase: the identifier, FROM, BY, UNTIL. */
    private static final class Phrase {
        Value.Field id;
        Value from, by, until;
    }

    /** A PERFORM's control: TIMES, UNTIL with TEST AFTER, or VARYING phrases. */
    private static final class Loop {
        Value times, until;
        boolean testAfter;
        final List<Phrase> varying = new ArrayList<>();
        Phrase current;
    }

    private static Loop loop(Statement s) {
        Loop l = new Loop();
        for (Statement.Operand o : s.operands()) node(l, o.role(), o.value());
        for (Statement.Branch b : s.branches()) if (b.condition() != null) node(l, b.label(), b.condition());
        return l;
    }

    private static void node(Loop l, String role, Value v) {
        if (v instanceof Value.Expression e && e.operator().startsWith("perform-")) {
            if (e.operator().equals("perform-varying-phrase")) l.current = null;
            for (Value c : e.operands()) node(l, e.operator(), c);
            return;
        }
        if (v instanceof Value.Name n && n.kind().equals("keyword")) {
            if (role.equals("perform-test-clause") && n.text().equals("AFTER")) l.testAfter = true;
            return;
        }
        if (role.equals("perform-varying-phrase") && v instanceof Value.Field id && (l.current == null || l.current.id != null)) {
            l.current = new Phrase();
            l.current.id = id;
            l.varying.add(l.current);
            return;
        }
        Phrase p = l.current;
        switch (role) {
            case "perform-times" -> l.times = v;
            case "perform-from" -> p.from = v;
            case "perform-by" -> p.by = v;
            case "perform-until" -> {
                if (p != null && p.until == null) p.until = v;
                else l.until = v;
            }
            default -> { }
        }
    }

    /** Whether a PERFORM varies an identifier: always lowered as stretches. */
    static boolean varies(Statement s) {
        return s.verb().equals("PERFORM") && !loop(s).varying.isEmpty();
    }

    // Decision: R-021
    void statement(Statement s) {
        Value.Target target = Flow.target(s);
        List<Statement> body = Verbs.branch(s, "perform-inline-statement");
        Loop l = loop(s);
        if (!l.varying.isEmpty()) varying(s, l, target, body);
        else if (l.times != null) repeat(target, body, "t[" + count(l.times) + "]-- <= 0", false);
        else if (l.until != null) repeat(target, body, f.conditions.of(l.until), l.testAfter);
        else once(target, body);
    }

    /** A TIMES count into a counter of its own; returns the counter. */
    private int count(Value times) {
        int c = f.counter();
        f.e.line("t[" + c + "] = " + f.x.index(times) + ";");
        return c;
    }

    private void once(Value.Target target, List<Statement> body) {
        if (target == null) {
            f.statements(body);
            return;
        }
        int back = f.label();
        f.e.line("return d.perform(" + target.paragraph() + ", " + target.thru() + ", " + back + ");");
        f.split(back);
    }

    /** Repeats the procedures or the body until `done` holds, tested before each time (or after, for TEST AFTER). */
    private void repeat(Value.Target target, List<Statement> body, String done, boolean testAfter) {
        if (target == null && !Flow.splits(body)) {
            f.e.open(testAfter ? "do" : "while (!(" + done + "))");
            f.statements(body);
            if (testAfter) {
                f.e.close();
                f.e.line("while (!(" + done + "));");
            } else f.e.close();
            return;
        }
        int test = f.label(), afterLoop = f.label();
        if (testAfter) perform(target, body, test);
        else f.e.line("return " + test + ";");
        f.split(test);
        f.e.line("if (" + done + ") return " + afterLoop + ";");
        perform(target, body, test);
        f.split(afterLoop);
    }

    /** Once more: the procedures through the dispatcher, or the body, then on to `back`. */
    private void perform(Value.Target target, List<Statement> body, int back) {
        if (target != null) {
            f.e.line("return d.perform(" + target.paragraph() + ", " + target.thru() + ", " + back + ");");
            return;
        }
        f.statements(body);
        f.e.line("return " + back + ";");
    }

    /**
     * VARYING id1 ... AFTER id2 ...: nested loops, outermost first; each identifier set FROM, its
     * condition tested, the next one in set and tested, the innermost running the procedures, then BY.
     */
    private void varying(Statement s, Loop l, Value.Target target, List<Statement> body) {
        for (Phrase p : l.varying) {
            if (p.from == null || p.until == null) {
                f.e.line(f.x.unsupported("PERFORM VARYING at " + s.origin()) + ";");
                return;
            }
        }
        int n = l.varying.size(), afterLoop = f.label();
        int[] tests = new int[n], steps = new int[n];
        for (int k = 0; k < n; k++) {
            tests[k] = f.label();
            steps[k] = f.label();
        }
        start(l.varying.get(0), tests[0]);
        for (int k = 0; k < n; k++) {
            f.split(tests[k]);
            f.e.line("if (" + f.conditions.of(l.varying.get(k).until) + ") return " + (k == 0 ? afterLoop : steps[k - 1]) + ";");
            if (k + 1 < n) start(l.varying.get(k + 1), tests[k + 1]);
            else perform(target, body, steps[k]);
        }
        for (int k = n - 1; k >= 0; k--) {
            f.split(steps[k]);
            step(l.varying.get(k));
            f.e.line("return " + tests[k] + ";");
        }
        f.split(afterLoop);
    }

    private void start(Phrase p, int test) {
        f.e.line(f.x.moveInto(p.from, f.x.field(p.id), f.x.item(p.id.item())) + ";");
        f.e.line("return " + test + ";");
    }

    private void step(Phrase p) {
        Value by = p.by == null ? new Value.Literal("1", Value.Literal.Kind.NUMERIC, "1", 0) : p.by;
        Value increment = new Value.Expression("+", List.of(p.id, by));
        int dmax = f.arith.dmax(List.of(f.x.item(p.id.item())), List.of(increment));
        f.e.line("Arithmetic.store(" + f.x.field(p.id) + ", " + f.arith.result(increment, dmax, false) + ", false, false);");
    }
}
