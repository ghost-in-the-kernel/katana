package katana.lower;

import java.util.List;
import katana.sir.types.Statement;
import katana.sir.types.Value;

/**
 * PERFORM in its forms (LR pp. 412-425): out of line through the dispatcher (R-021), once, TIMES, UNTIL
 * (TEST BEFORE or AFTER), VARYING; inline, the same loops over the statements written in it, as
 * structured Java unless they hold an out-of-line PERFORM.
 */
final class Performs {
    private final Flow f;

    Performs(Flow f) {
        this.f = f;
    }

    // Decision: R-021
    void statement(Statement s) {
        Value.Target target = Flow.target(s);
        List<Statement> body = Verbs.branch(s, "perform-inline-statement");
        if (!Verbs.operands(s, "perform-varying-clause", "perform-varying-phrase", "perform-after").isEmpty()) {
            varying(s, target, body);
            return;
        }
        Value times = Verbs.operand(s, "perform-times");
        Value until = Verbs.operand(s, "perform-until");
        boolean after = Verbs.keyword(s, "AFTER");
        if (times != null) loop(target, body, "t[" + count(times) + "]-- <= 0", false);
        else if (until != null) loop(target, body, f.conditions.of(until), after);
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
    private void loop(Value.Target target, List<Statement> body, String done, boolean testAfter) {
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
        if (testAfter) {
            perform(target, body, test);
        } else f.e.line("return " + test + ";");
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

    /** PERFORM VARYING id FROM a BY b UNTIL c: one identifier for now (AFTER is not yet lowered). */
    private void varying(Statement s, Value.Target target, List<Statement> body) {
        List<Value> v = Verbs.operands(s, "perform-varying-phrase", "perform-varying-clause");
        Value from = Verbs.operand(s, "perform-from"), by = Verbs.operand(s, "perform-by"), until = Verbs.operand(s, "perform-until");
        if (!Verbs.operands(s, "perform-after").isEmpty() || v.isEmpty() || !(v.get(0) instanceof Value.Field id) || from == null || until == null) {
            f.e.line(f.x.unsupported("PERFORM VARYING at " + s.origin()) + ";");
            return;
        }
        String field = f.x.field(id);
        f.e.line(f.x.moveInto(from, field, f.x.item(id.item())) + ";");
        int test = f.label(), step = f.label(), afterLoop = f.label();
        f.e.line("return " + test + ";");
        f.split(test);
        f.e.line("if (" + f.conditions.of(until) + ") return " + afterLoop + ";");
        perform(target, body, step);
        f.split(step);
        Value increment = new Value.Expression("+", List.of(id, by == null ? new Value.Literal("1", Value.Literal.Kind.NUMERIC, "1", 0) : by));
        int dmax = f.arith.dmax(List.of(f.x.item(id.item())), List.of(increment));
        f.e.line("Arithmetic.store(" + field + ", " + f.arith.result(increment, dmax, false) + ", false, false);");
        f.e.line("return " + test + ";");
        f.split(afterLoop);
    }
}
