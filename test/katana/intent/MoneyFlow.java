package katana.intent;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import katana.sir.types.Attributes;
import katana.sir.types.Item;
import katana.sir.types.Program;
import katana.sir.types.Statement;
import katana.sir.types.Value;

/**
 * T-M1 (research §33, §34): a `money` fact means the item is an amount, so what it is added with,
 * subtracted with, moved to or from, joined to by + or -, or compared with is an amount too: a number of
 * two places, a field edited for print, or a literal of at most two places. A term multiplied or divided
 * may have any scale (a rate, a count of months).
 */
final class MoneyFlow {
    static final String TEST = "T-M1";
    private static final Set<String> ARITHMETIC = Set.of("add-from", "add-to", "add-giving", "add-to-giving", "subtract-minuend", "subtract-subtrahend", "subtract-giving", "subtract-minuend-giving");
    private static final Set<String> RELATIONS = Set.of("=", "<", ">", "<=", ">=", "NOT =", "<>");

    private MoneyFlow() {}

    static Verdict verdict(Program p, int item) {
        Set<String> partners = new LinkedHashSet<>(), wrong = new LinkedHashSet<>();
        int statements = 0;
        for (BatchReviewEvidence.At a : BatchReviewEvidence.statements(p)) {
            List<Value> with = partners(a.s(), item);
            if (with.isEmpty()) continue;
            statements++;
            for (Value v : with) {
                String name = name(p, v);
                if (amount(p, v)) partners.add(name);
                else wrong.add(a.s().verb() + " with " + name + " (" + why(p, v) + ") in " + a.paragraph());
            }
        }
        if (!wrong.isEmpty()) return Verdict.refutes(TEST, String.join("; ", wrong));
        if (statements == 0) return Verdict.cannotTell(TEST, "no statement adds, subtracts, moves or compares it with anything");
        return Verdict.holds(TEST, statements + " statement" + (statements == 1 ? "" : "s") + " add, subtract, move or compare it, each with an amount: " + String.join(", ", partners));
    }

    /** What one statement joins the item with, by addition, subtraction, a MOVE or a comparison; empty when it does not. */
    static List<Value> partners(Statement s, int item) {
        List<Value> out = new ArrayList<>();
        if (s.verb().equals("ADD") || s.verb().equals("SUBTRACT")) {
            List<Value> terms = s.operands().stream().filter(o -> ARITHMETIC.contains(o.role()) && data(o.value())).map(Statement.Operand::value).toList();
            if (terms.stream().anyMatch(v -> is(v, item))) terms.stream().filter(v -> !is(v, item)).forEach(out::add);
        }
        if (s.verb().equals("MOVE")) {
            List<Value> from = values(s, "move-to-sending-area"), to = values(s, "move-to-statement");
            if (from.stream().anyMatch(v -> is(v, item))) out.addAll(to);
            if (to.stream().anyMatch(v -> is(v, item))) out.addAll(from);
        }
        if (s.verb().equals("COMPUTE")) {
            List<Value> terms = new ArrayList<>(values(s, "compute-store"));
            for (Statement.Operand o : s.operands()) if (o.role().equals("compute-statement")) terms(o.value(), terms);
            if (terms.stream().anyMatch(v -> is(v, item))) terms.stream().filter(v -> !is(v, item) && data(v)).forEach(out::add);
        }
        for (Statement.Operand o : s.operands()) compared(o.value(), item, out);
        for (Statement.Branch b : s.branches()) if (b.condition() != null) compared(b.condition(), item, out);
        return out;
    }

    private static List<Value> values(Statement s, String role) {
        return s.operands().stream().filter(o -> o.role().equals(role) && data(o.value())).map(Statement.Operand::value).toList();
    }

    /** A sum's terms: through + and -, not into a product or quotient, which may have any scale. */
    private static void terms(Value v, List<Value> out) {
        if (v instanceof Value.Expression e && (e.operator().equals("+") || e.operator().equals("-"))) e.operands().forEach(o -> terms(o, out));
        else if (!(v instanceof Value.Expression)) out.add(v);
    }

    private static void compared(Value v, int item, List<Value> out) {
        if (!(v instanceof Value.Expression e)) return;
        if (RELATIONS.contains(e.operator()) && e.operands().size() == 2) {
            List<Value> left = new ArrayList<>(), right = new ArrayList<>();
            terms(e.operands().get(0), left);
            terms(e.operands().get(1), right);
            if (left.stream().anyMatch(x -> is(x, item))) right.stream().filter(MoneyFlow::data).forEach(out::add);
            if (right.stream().anyMatch(x -> is(x, item))) left.stream().filter(MoneyFlow::data).forEach(out::add);
        }
        e.operands().forEach(o -> compared(o, item, out));
    }

    private static boolean data(Value v) {
        return v instanceof Value.Field || v instanceof Value.Literal || v instanceof Value.Figurative;
    }

    private static boolean is(Value v, int item) {
        return v instanceof Value.Field f && f.item() == item && f.start() == null;
    }

    /** A two-place number, a field edited for print, a literal of at most two places, or ZERO. */
    static boolean amount(Program p, Value v) {
        if (v instanceof Value.Figurative f) return f.name().startsWith("ZERO");
        if (v instanceof Value.Literal l) return l.numeric() && l.scale() <= 2;
        Item i = p.items().get(((Value.Field) v).item() - 1);
        Attributes a = i.attributes();
        return a.category() == Attributes.Category.NUMERIC_EDITED || (a.category() == Attributes.Category.NUMERIC && a.scale() == 2);
    }

    private static String why(Program p, Value v) {
        if (v instanceof Value.Literal l) return l.numeric() ? "a literal of " + l.scale() + " places" : "a text literal";
        if (v instanceof Value.Figurative f) return f.name().toLowerCase();
        Attributes a = p.items().get(((Value.Field) v).item() - 1).attributes();
        return a.category() == Attributes.Category.NUMERIC ? "a number of " + a.scale() + " places, picture " + a.picture() : a.category().name().toLowerCase().replace('_', ' ') + (a.picture().isEmpty() ? "" : ", picture " + a.picture());
    }

    static String name(Program p, Value v) {
        if (v instanceof Value.Field f) return p.items().get(f.item() - 1).name();
        if (v instanceof Value.Literal l) return l.text();
        return v instanceof Value.Figurative f ? f.name() : String.valueOf(v);
    }
}
