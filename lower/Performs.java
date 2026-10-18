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

    /** A PERFORM's IR read into a Loop: TIMES, UNTIL, TEST AFTER, and each VARYING phrase's identifier, FROM, BY and UNTIL. */
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

    /** The phrases of a PERFORM, as `Lower.varying` gives them to a pass. */
    static Lower.Varying phrases(Statement s) {
        Loop l = loop(s);
        return new Lower.Varying(l.varying.stream().map(p -> p.id).toList(), l.varying.stream().map(p -> p.by).toList());
    }

    /** Whether a PERFORM varies an identifier: lowered as stretches unless a pass's plan writes it as a `for`. */
    static boolean varies(Statement s) {
        return s.verb().equals("PERFORM") && !loop(s).varying.isEmpty();
    }

    // Decision: R-021
    void statement(Statement s) {
        Value.Target target = Flow.target(s);
        List<Statement> body = Verbs.branch(s, "perform-inline-statement");
        Loop l = loop(s);
        if (l.varying.size() == 1 && f.loop(s) && l.varying.get(0).from != null && l.varying.get(0).until != null) counted(l, target, body);
        else if (!l.varying.isEmpty()) varying(s, l, target, body);
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
        if (f.called(target)) {
            f.e.line(f.call(target));
            return;
        }
        int back = f.label();
        f.e.line("return " + f.perform(target, back) + ";");
        f.split(back);
    }

    /** Repeats the procedures or the body until `done` holds, tested before each time (or after, for TEST AFTER). */
    private void repeat(Value.Target target, List<Statement> body, String done, boolean testAfter) {
        if (target == null && !f.splits(body) || f.called(target)) {
            f.e.open(testAfter ? "do" : "while (!(" + done + "))");
            if (target != null) f.e.line(f.call(target));
            else f.statements(body);
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
            f.e.line("return " + f.perform(target, back) + ";");
            return;
        }
        f.statements(body);
        f.e.line("return " + back + ";");
    }

    /**
     * VARYING id1 ... AFTER id2 ... (TEST BEFORE; LR 6.4 pp. 420-422): every identifier set FROM, then
     * each condition tested outermost first; the innermost's false runs the procedures and steps it; an
     * inner condition's true steps the one outside it and then sets that identifier FROM again (its FROM
     * may name the outer one: NC201A's AFTER B FROM A), so at the end each inner identifier holds its
     * FROM value and the outermost one step past its last use.
     */
    private void varying(Statement s, Loop l, Value.Target target, List<Statement> body) {
        for (Phrase p : l.varying) {
            if (p.from == null || p.until == null) {
                f.e.line(f.x.unsupported("PERFORM VARYING at " + s.origin()) + ";");
                return;
            }
        }
        if (l.testAfter) {
            varyingAfter(l, target, body);
            return;
        }
        int n = l.varying.size(), afterLoop = f.label();
        int[] tests = new int[n], steps = new int[n];
        for (int k = 0; k < n; k++) {
            tests[k] = f.label();
            steps[k] = f.label();
        }
        for (Phrase p : l.varying) f.e.line(f.x.moveInto(p.from, f.x.field(p.id), f.x.item(p.id.item())) + ";");
        f.e.line("return " + tests[0] + ";");
        for (int k = 0; k < n; k++) {
            f.split(tests[k]);
            if (k == 0) f.e.line("if (" + f.conditions.of(l.varying.get(k).until) + ") return " + afterLoop + ";");
            else {
                Phrase p = l.varying.get(k);
                f.e.open("if (" + f.conditions.of(p.until) + ")");
                step(l.varying.get(k - 1));
                f.e.line(f.x.moveInto(p.from, f.x.field(p.id), f.x.item(p.id.item())) + ";");
                f.e.line("return " + tests[k - 1] + ";");
                f.e.close();
            }
            if (k + 1 < n) f.e.line("return " + tests[k + 1] + ";");
            else perform(target, body, steps[k]);
        }
        f.split(steps[n - 1]);
        step(l.varying.get(n - 1));
        f.e.line("return " + tests[n - 1] + ";");
        f.split(afterLoop);
    }

    /**
     * VARYING ... WITH TEST AFTER (LR 6.4 p. 419: "executed at least once, before any condition is
     * tested"): every identifier set FROM, the procedures run, then the innermost condition tested; while
     * it is false that identifier steps and the procedures run again; once true the next one out is
     * tested, and when that is false it steps and every identifier inside it is set FROM again.
     */
    // Decision: R-021
    private void varyingAfter(Loop l, Value.Target target, List<Statement> body) {
        int n = l.varying.size(), run = f.label(), afterLoop = f.label();
        int[] checks = new int[n];
        for (int k = 0; k < n; k++) checks[k] = f.label();
        for (Phrase p : l.varying) f.e.line(f.x.moveInto(p.from, f.x.field(p.id), f.x.item(p.id.item())) + ";");
        f.e.line("return " + run + ";");
        f.split(run);
        perform(target, body, checks[n - 1]);
        for (int k = n - 1; k >= 0; k--) {
            f.split(checks[k]);
            f.e.open("if (!(" + f.conditions.of(l.varying.get(k).until) + "))");
            step(l.varying.get(k));
            for (int j = k + 1; j < n; j++) f.e.line(f.x.moveInto(l.varying.get(j).from, f.x.field(l.varying.get(j).id), f.x.item(l.varying.get(j).id.item())) + ";");
            f.e.line("return " + run + ";");
            f.e.close();
            f.e.line("return " + (k == 0 ? afterLoop : checks[k - 1]) + ";");
        }
        f.split(afterLoop);
    }

    private void step(Phrase p) {
        f.e.line(stepped(p) + ";");
    }

    /** The counter stepped BY, as an expression a `for` header can hold. */
    private String stepped(Phrase p) {
        Value by = p.by == null ? new Value.Literal("1", Value.Literal.Kind.NUMERIC, "1", 0) : p.by;
        Value increment = new Value.Expression("+", List.of(p.id, by));
        int dmax = f.arith.dmax(List.of(f.x.item(p.id.item())), List.of(increment));
        return "Arithmetic.store(" + f.x.field(p.id) + ", " + f.arith.result(increment, dmax, false) + ", false, false)";
    }

    /**
     * One VARYING phrase the plan writes as a `for` (6.1, research §36): FROM, then the test and the body and
     * the step in the order the stretches take them, TEST BEFORE testing first, TEST AFTER after the body.
     */
    // Decision: R-021
    private void counted(Loop l, Value.Target target, List<Statement> body) {
        Phrase p = l.varying.get(0);
        f.e.line(f.x.moveInto(p.from, f.x.field(p.id), f.x.item(p.id.item())) + ";");
        f.e.open(l.testAfter ? "for (;; " + stepped(p) + ")" : "for (; !(" + f.conditions.of(p.until) + "); " + stepped(p) + ")");
        if (target != null) f.e.line(f.call(target));
        else f.statements(body);
        if (l.testAfter) f.e.line("if (" + f.conditions.of(p.until) + ") break;");
        f.e.close();
    }
}
