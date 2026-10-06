package katana.lower;

import java.util.ArrayList;
import java.util.List;
import katana.sir.types.Statement;
import katana.sir.types.Value;

/**
 * EVALUATE (LR 6.4 pp. 337-342): each WHEN phrase a condition, its WHENs OR'd and within one WHEN each
 * subject paired with its object (a value, ANY, a THRU range, NOT, or a condition for TRUE and FALSE);
 * the first phrase that holds runs, else WHEN OTHER. An if-else chain, or stretches when a phrase holds
 * an out-of-line PERFORM.
 */
final class Evaluate {
    private final Flow f;

    Evaluate(Flow f) {
        this.f = f;
    }

    private static boolean isKeyword(Value v, String word) {
        return v instanceof Value.Name n && n.kind().equals("keyword") && n.text().equals(word);
    }

    // Decision: R-021
    void statement(Statement s) {
        List<Value> subjects = Verbs.operands(s, "evaluate-select");
        List<String> conditions = new ArrayList<>();
        List<List<Statement>> bodies = new ArrayList<>();
        List<Statement> other = List.of();
        for (Statement.Branch b : s.branches()) {
            if (b.label().equals("evaluate-when-other")) other = b.statements();
            else {
                conditions.add(phrase(subjects, b.condition()));
                bodies.add(b.statements());
            }
        }
        chain(conditions, bodies, other, 0);
    }

    /** The phrases from `k` on: the first's condition, its statements, else the rest. */
    private void chain(List<String> conditions, List<List<Statement>> bodies, List<Statement> other, int k) {
        if (k == conditions.size()) {
            f.statements(other);
            return;
        }
        boolean split = Flow.splits(bodies.get(k)) || Flow.splits(other) || bodies.subList(k + 1, bodies.size()).stream().anyMatch(Flow::splits);
        if (!split) {
            f.e.open("if (" + conditions.get(k) + ")");
            f.statements(bodies.get(k));
            f.e.close();
            f.e.open("else");
            chain(conditions, bodies, other, k + 1);
            f.e.close();
            return;
        }
        int next = f.label(), after = f.label();
        f.e.line("if (!(" + conditions.get(k) + ")) return " + next + ";");
        f.statements(bodies.get(k));
        f.e.line("return " + after + ";");
        f.split(next);
        chain(conditions, bodies, other, k + 1);
        f.e.line("return " + after + ";");
        f.split(after);
    }

    /** A WHEN phrase's condition: any of its WHENs. */
    private String phrase(List<Value> subjects, Value condition) {
        List<String> whens = new ArrayList<>();
        if (condition instanceof Value.Expression e && e.operator().equals("evaluate-when")) whens.add(when(subjects, e));
        else if (condition instanceof Value.Expression e) for (Value w : e.operands()) if (w instanceof Value.Expression when && when.operator().equals("evaluate-when")) whens.add(when(subjects, when));
        return whens.isEmpty() ? f.x.unsupported("EVALUATE phrase " + condition) : "(" + String.join(" || ", whens) + ")";
    }

    /** One object of a WHEN: a value (through `thru`), ANY, or NOT one of these. */
    private record Obj(Value value, Value thru, boolean any, boolean not) {}

    /** One WHEN: each subject against its object, the objects after the first behind ALSO. */
    private String when(List<Value> subjects, Value.Expression when) {
        List<Obj> objects = new ArrayList<>();
        read(when.operands(), objects, false);
        List<String> pairs = new ArrayList<>();
        for (int k = 0; k < Math.min(subjects.size(), objects.size()); k++) pairs.add(pair(subjects.get(k), objects.get(k)));
        return "(" + String.join(" && ", pairs) + ")";
    }

    /** The objects in a WHEN's parts, in order: a THRU joins the value before it, ALSO and evaluate-condition group their parts. */
    private static void read(List<Value> parts, List<Obj> objects, boolean not) {
        boolean negate = not;
        for (Value v : parts) {
            if (isKeyword(v, "WHEN") || isKeyword(v, "ALSO")) continue;
            if (isKeyword(v, "NOT")) negate = true;
            else if (isKeyword(v, "ANY")) objects.add(new Obj(null, null, true, false));
            else if (v instanceof Value.Expression e && e.operator().equals("evaluate-through") && !objects.isEmpty()) {
                Obj last = objects.remove(objects.size() - 1);
                objects.add(new Obj(last.value(), through(e), false, last.not()));
            } else if (v instanceof Value.Expression e && (e.operator().equals("evaluate-also-condition") || e.operator().equals("evaluate-condition"))) {
                read(e.operands(), objects, negate);
                negate = false;
            } else {
                objects.add(new Obj(v, null, false, negate));
                negate = false;
            }
        }
    }

    /** A subject and its object. */
    private String pair(Value subject, Obj o) {
        if (o.any()) return "true";
        String test = o.thru() != null
                ? "(" + f.conditions.compare(subject, o.value()) + " >= 0 && " + f.conditions.compare(subject, o.thru()) + " <= 0)"
                : plain(subject, o.value());
        return o.not() ? "!(" + test + ")" : test;
    }

    private static Value through(Value.Expression t) {
        return t.operands().stream().filter(o -> !(o instanceof Value.Name)).findFirst().orElseThrow();
    }

    /** TRUE or FALSE against a condition, a condition against TRUE or FALSE, else equality. */
    private String plain(Value subject, Value object) {
        if (bool(subject) != null) return (bool(subject) ? "" : "!") + "(" + f.conditions.of(object) + ")";
        if (bool(object) != null) return (bool(object) ? "" : "!") + "(" + f.conditions.of(subject) + ")";
        return f.conditions.compare(subject, object) + " == 0";
    }

    private static Boolean bool(Value v) {
        return v instanceof Value.Literal l && l.kind() == Value.Literal.Kind.BOOLEAN ? Boolean.valueOf(l.payload().equals("TRUE")) : null;
    }
}
