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
        if (condition instanceof Value.Expression e) for (Value w : e.operands()) if (w instanceof Value.Expression when) whens.add(when(subjects, when));
        return whens.isEmpty() ? f.x.unsupported("EVALUATE phrase " + condition) : "(" + String.join(" || ", whens) + ")";
    }

    /** One WHEN: each subject against its object, the objects after the first behind ALSO. */
    private String when(List<Value> subjects, Value.Expression when) {
        List<Value> objects = new ArrayList<>();
        for (Value v : when.operands()) {
            if (isKeyword(v, "WHEN")) continue;
            if (v instanceof Value.Expression a && a.operator().equals("evaluate-also-condition")) objects.add(a.operands().stream().filter(o -> !isKeyword(o, "ALSO")).findFirst().orElse(null));
            else objects.add(v);
        }
        List<String> pairs = new ArrayList<>();
        for (int k = 0; k < Math.min(subjects.size(), objects.size()); k++) pairs.add(pair(subjects.get(k), objects.get(k)));
        return "(" + String.join(" && ", pairs) + ")";
    }

    /** A subject and its object. */
    private String pair(Value subject, Value object) {
        if (object instanceof Value.Expression c && c.operator().equals("evaluate-condition")) return condition(subject, c.operands());
        return plain(subject, object);
    }

    private String condition(Value subject, List<Value> parts) {
        if (parts.stream().anyMatch(p -> isKeyword(p, "ANY"))) return "true";
        boolean not = parts.stream().anyMatch(p -> isKeyword(p, "NOT"));
        List<Value> values = parts.stream().filter(p -> !(p instanceof Value.Name)).toList();
        String test = values.size() == 2 && values.get(1) instanceof Value.Expression t && t.operator().equals("evaluate-through")
                ? "(" + f.conditions.compare(subject, values.get(0)) + " >= 0 && " + f.conditions.compare(subject, through(t)) + " <= 0)"
                : plain(subject, values.get(0));
        return not ? "!" + test : test;
    }

    private static Value through(Value.Expression t) {
        return t.operands().stream().filter(o -> !(o instanceof Value.Name)).findFirst().orElseThrow();
    }

    /** TRUE or FALSE against a condition, a condition against TRUE or FALSE, else equality. */
    private String plain(Value subject, Value object) {
        if (bool(subject) != null) return (bool(subject) ? "" : "!") + f.conditions.of(object);
        if (bool(object) != null) return (bool(object) ? "" : "!") + f.conditions.of(subject);
        return f.conditions.compare(subject, object) + " == 0";
    }

    private static Boolean bool(Value v) {
        return v instanceof Value.Literal l && l.kind() == Value.Literal.Kind.BOOLEAN ? Boolean.valueOf(l.payload().equals("TRUE")) : null;
    }
}
