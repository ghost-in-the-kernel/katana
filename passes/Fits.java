package katana.passes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import katana.lower.Forms;
import katana.lower.Lower;
import katana.sir.types.Attributes;
import katana.sir.types.Procedure;
import katana.sir.types.Program;
import katana.sir.types.Statement;
import katana.sir.types.Value;

/**
 * 6.6b, the PICTURE rule (research §45, R-068): an exact statement whose value always fits its one promoted
 * receiver, by its operands' PICTUREs, stores it as plain assignment: the store would truncate nothing, round
 * nothing and raise no size error. Its obligations: one promoted receiver, whole; the expression's integer places
 * within the receiver's integer digits and its decimal places within its scale; for an unsigned receiver, every
 * operand unsigned and no subtraction or negation.
 */
public final class Fits {
    public static final String PASS = "fits-v1";

    private Fits() {}

    /** The exact statements whose store is plain assignment, by where each starts (`Lower.Plan.loop`); each one refused, by its line, with why. */
    // Decision: R-068
    public static Set<String> fits(Program p, Set<Integer> scalars, Set<String> exact, Map<String, List<String>> refused) {
        Set<String> out = new TreeSet<>();
        for (Procedure.Paragraph g : p.procedure().paragraphs()) for (Statement s : g.statements()) statement(p, s, scalars, exact, out, refused);
        return out;
    }

    /** One exact statement held to the obligations (its branches' statements after it). */
    private static void statement(Program p, Statement s, Set<Integer> scalars, Set<String> exact, Set<String> out, Map<String, List<String>> refused) {
        if (exact.contains(Lower.Plan.loop(s.origin()))) {
            List<String> why = refusals(p, s, scalars);
            if (why.isEmpty()) out.add(Lower.Plan.loop(s.origin()));
            else refused.put(s.verb() + " at line " + s.origin().line() + " fits", why);
        }
        for (Statement.Branch b : s.branches()) for (Statement t : b.statements()) statement(p, t, scalars, exact, out, refused);
    }

    /** Why the statement's value may not fit its receiver; empty when it always does. */
    private static List<String> refusals(Program p, Statement s, Set<Integer> scalars) {
        List<String> why = new ArrayList<>();
        Forms.Form form = Forms.of(s);
        List<Value> receivers = s.operands().stream().filter(o -> o.role().equals(form.receivers())).map(Statement.Operand::value).filter(v -> !(v instanceof Value.Name)).toList();
        if (receivers.size() != 1 || !(receivers.get(0) instanceof Value.Field r && scalars.contains(r.item()) && r.subscripts().isEmpty() && r.start() == null)) {
            why.add("not one promoted receiver, whole");
            return why;
        }
        Attributes a = p.items().get(r.item() - 1).attributes();
        String name = p.items().get(r.item() - 1).name();
        int[] places = Exact.places(p, form.expression(), scalars, why);
        int digits = Lower.stored(a) - a.scale();
        if (places[0] > digits) why.add("its value carries " + places[0] + " integer places, past " + name + "'s " + digits);
        if (places[1] > a.scale()) why.add("its value carries " + places[1] + " decimal places, past " + name + "'s " + a.scale());
        if (!a.signed() && signed(p, form.expression())) why.add(name + " is unsigned and the value may be negative");
        return why;
    }

    /** Whether a value may be negative: a signed item, a negative literal, a subtraction or a negation anywhere in it. */
    private static boolean signed(Program p, Value v) {
        return switch (v) {
            case Value.Field f -> p.items().get(f.item() - 1).attributes().signed();
            case Value.Literal l -> l.payload().startsWith("-");
            case Value.Expression e -> Set.of("-", "neg").contains(e.operator()) || e.operands().stream().anyMatch(o -> signed(p, o));
            default -> false;
        };
    }
}
