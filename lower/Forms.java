package katana.lower;

import java.util.ArrayList;
import java.util.List;
import katana.sir.types.Statement;
import katana.sir.types.Value;

/**
 * An arithmetic statement as one expression over its operands (6.4, research §41): ADD, SUBTRACT and MULTIPLY's
 * value for their one receiver, or for GIVING; COMPUTE's own. What passes/Exact holds to its obligations and
 * `ArithVerbs` writes as exact arithmetic, so the two read a statement alike.
 */
public final class Forms {
    private Forms() {}

    /** The expression and the receivers' role; null for a DIVIDE, CORRESPONDING, or a TO, FROM or BY form with several receivers (R-059's shared temporary). */
    public record Form(Value expression, String receivers) {}

    public static Form of(Statement s) {
        return switch (s.verb()) {
            case "ADD" -> add(s);
            case "SUBTRACT" -> subtract(s);
            case "MULTIPLY" -> multiply(s);
            case "COMPUTE" -> new Form(Verbs.operand(s, "compute-statement"), "compute-store");
            default -> null;
        };
    }

    private static Form add(Statement s) {
        List<Value> from = Verbs.operands(s, "add-from");
        if (from.isEmpty()) return null;
        if (!fields(s, "add-giving").isEmpty()) {
            List<Value> all = new ArrayList<>(from);
            all.addAll(Verbs.operands(s, "add-to-giving"));
            return new Form(sum(all), "add-giving");
        }
        List<Value> to = fields(s, "add-to");
        return to.size() == 1 ? new Form(new Value.Expression("+", List.of(to.get(0), sum(from))), "add-to") : null;
    }

    private static Form subtract(Statement s) {
        List<Value> subtrahends = Verbs.operands(s, "subtract-subtrahend");
        if (subtrahends.isEmpty()) return null;
        if (!fields(s, "subtract-giving").isEmpty()) return new Form(new Value.Expression("-", List.of(Verbs.operand(s, "subtract-minuend-giving"), sum(subtrahends))), "subtract-giving");
        List<Value> minuends = fields(s, "subtract-minuend");
        return minuends.size() == 1 ? new Form(new Value.Expression("-", List.of(minuends.get(0), sum(subtrahends))), "subtract-minuend") : null;
    }

    private static Form multiply(Statement s) {
        Value a = Verbs.operand(s, "multiply-statement");
        if (!fields(s, "multiply-giving-result").isEmpty()) return new Form(new Value.Expression("*", List.of(a, Verbs.operand(s, "multiply-giving-operand"))), "multiply-giving-result");
        List<Value> regular = fields(s, "multiply-regular-operand");
        return regular.size() == 1 ? new Form(new Value.Expression("*", List.of(a, regular.get(0))), "multiply-regular-operand") : null;
    }

    /** The values added together, `a + b + c`, as ADD and SUBTRACT add their senders. */
    static Value sum(List<Value> values) {
        Value total = values.get(0);
        for (int k = 1; k < values.size(); k++) total = new Value.Expression("+", List.of(total, values.get(k)));
        return total;
    }

    /** The data items of a role (its ROUNDED keywords left out). */
    private static List<Value> fields(Statement s, String role) {
        return Verbs.operands(s, role).stream().filter(v -> v instanceof Value.Field).toList();
    }
}
