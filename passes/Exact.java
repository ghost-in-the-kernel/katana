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
 * 6.4, exact-arithmetic substitution (research §41): an arithmetic statement whose expression IBM's step-wise
 * arithmetic carries without a cut, written as plain arithmetic. Its obligations: `+ - *` only, over promoted
 * items, numeric literals and ZERO (each within its places), every operation's places within the cap.
 */
public final class Exact {
    /** IBM_COMPAT's cap on the places an intermediate result carries (PG 6.4 p. 795), the profile the lowering writes (`Flow`'s `P`). */
    private static final int CAP = 30;

    private Exact() {}

    /** The arithmetic statements written exact, by where each starts (`Lower.Plan.loop`); each one refused, by its line, with why. */
    // Decision: R-032
    public static Set<String> exact(Program p, Set<Integer> scalars, Map<String, List<String>> refused) {
        Set<String> out = new TreeSet<>();
        for (Procedure.Paragraph g : p.procedure().paragraphs()) for (Statement s : g.statements()) statement(p, s, scalars, out, refused);
        return out;
    }

    private static void statement(Program p, Statement s, Set<Integer> scalars, Set<String> out, Map<String, List<String>> refused) {
        if (Set.of("ADD", "SUBTRACT", "MULTIPLY", "COMPUTE", "DIVIDE").contains(s.verb())) {
            List<String> why = new ArrayList<>();
            Forms.Form form = Forms.of(s);
            if (form == null) why.add(s.verb().equals("DIVIDE") ? "a division" : "CORRESPONDING, or several receivers sharing a temporary (R-059)");
            else if (!(form.expression() instanceof Value.Expression)) why.add("no operation");
            else places(p, form.expression(), scalars, why);
            if (why.isEmpty()) out.add(Lower.Plan.loop(s.origin()));
            else refused.put(s.verb() + " at line " + s.origin().line(), why);
        }
        for (Statement.Branch b : s.branches()) for (Statement t : b.statements()) statement(p, t, scalars, out, refused);
    }

    /** An operand's or operation's places as IBM carries them, {integer, decimal}; a reason in `why` for anything exact arithmetic does not hold. */
    private static int[] places(Program p, Value v, Set<Integer> scalars, List<String> why) {
        switch (v) {
            case Value.Field f when scalars.contains(f.item()) && f.subscripts().isEmpty() && f.start() == null -> {
                Attributes a = p.items().get(f.item() - 1).attributes();
                return new int[] {Lower.stored(a) - a.scale(), a.scale()};
            }
            case Value.Field f -> why.add(p.items().get(f.item() - 1).name() + " is no promoted item: its bytes may decode past its PICTURE");
            case Value.Literal l when l.kind() == Value.Literal.Kind.NUMERIC -> {
                return new int[] {Math.max(0, l.payload().replace("-", "").length() - l.scale()), l.scale()};
            }
            case Value.Figurative f when f.name().startsWith("ZERO") && f.all() == null -> {
                return new int[] {1, 0};
            }
            case Value.Expression e when Set.of("neg", "pos").contains(e.operator()) -> {
                return places(p, e.operands().get(0), scalars, why);
            }
            case Value.Expression e when Set.of("+", "-", "*").contains(e.operator()) -> {
                int[] a = places(p, e.operands().get(0), scalars, why), b = places(p, e.operands().get(1), scalars, why);
                int[] r = e.operator().equals("*") ? new int[] {a[0] + b[0], a[1] + b[1]} : new int[] {Math.max(a[0], b[0]) + 1, Math.max(a[1], b[1])};
                if (r[0] + r[1] > CAP) why.add(e.operator() + " carries " + (r[0] + r[1]) + " places, past the cap of " + CAP);
                return r;
            }
            case Value.Expression e -> why.add("a " + e.operator() + (Set.of("/", "**").contains(e.operator()) ? "" : ", not + - *"));
            default -> why.add(v + ", no numeric operand exact arithmetic holds");
        }
        return new int[] {0, 0};
    }
}
