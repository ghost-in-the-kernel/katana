package katana.lower;

import java.util.List;
import katana.sir.types.Item;
import katana.sir.types.Value;

/**
 * Arithmetic expressions as Appendix A computes them (R-020, `rt/Decimal`): each operand a term with its
 * places, each operation given the statement's dmax, the last one told it is last (ROUNDED's place).
 */
final class Arith {
    private final Exprs x;

    Arith(Exprs x) {
        this.x = x;
    }

    /** The most decimal places among an expression's operands, a divisor's left out (dmax's operand part). */
    // Decision: R-020
    // Unsure: an operand inside a divisor expression is counted, only a direct divisor left out; IBM's "except divisors" does not say which (J-111). Stays: an IBM run of a divisor expression whose operands carry more places settles it.
    int decimals(Value v) {
        return switch (v) {
            case Value.Field f -> Math.max(0, x.item(f.item()).attributes().scale());
            case Value.Literal l when l.numeric() -> l.scale();
            case Value.Expression e when e.operator().equals("/") -> Math.max(decimals(e.operands().get(0)), e.operands().get(1) instanceof Value.Expression d ? decimals(d) : 0);
            case Value.Expression e when e.operator().equals("**") -> decimals(e.operands().get(0));
            case Value.Expression e -> e.operands().stream().mapToInt(this::decimals).max().orElse(0);
            default -> 0;
        };
    }

    /** An expression's value as a `Decimal.Term`, its outermost operation the statement's last. */
    String result(Value v, int dmax, boolean rounded) {
        return term(v, dmax, true, rounded);
    }

    /** An expression's value as a `Decimal.Term` that is not the statement's last operation. */
    String term(Value v, int dmax) {
        return term(v, dmax, false, false);
    }

    /** An operand, or an operation over two terms (`neg` over one), as `rt/Decimal` computes it. */
    private String term(Value v, int dmax, boolean last, boolean rounded) {
        if (!(v instanceof Value.Expression e) || !x.numeric(v)) return operand(v);
        if (e.operator().equals("neg")) return "Decimal.negate(" + term(e.operands().get(0), dmax, last, rounded) + ")";
        if (e.operator().equals("pos")) return term(e.operands().get(0), dmax, last, rounded);
        if (e.operator().equals("**")) return power(e, dmax, last, rounded);
        String op = switch (e.operator()) {
            case "+" -> "add";
            case "-" -> "subtract";
            case "*" -> "multiply";
            case "/" -> "divide";
            default -> null;
        };
        if (op == null) return x.unsupported("operator " + e.operator());
        String a = term(e.operands().get(0), dmax, false, false), b = term(e.operands().get(1), dmax, false, false);
        return "Decimal." + op + "(P, " + a + ", " + b + ", " + dmax + ", " + last + ", " + rounded + ")";
    }

    /**
     * `a ** b` with an integral exponent, a literal or a data-name or variable (PG 6.4 pp. 796-797); an
     * exponent with decimal places, or an expression holding ** or / when dmax is above zero, is computed
     * in floating point (p. 800), which is not lowered.
     */
    // Decision: R-020
    private String power(Value.Expression e, int dmax, boolean last, boolean rounded) {
        Value base = e.operands().get(0), exponent = e.operands().get(1);
        boolean floating = decimals(exponent) > 0 || (dmax > 0 && exponent instanceof Value.Expression x2 && holds(x2, "**", "/"));
        if (floating) return x.unsupported("floating-point exponentiation " + e);
        String literal = exponent instanceof Value.Literal l && l.numeric() ? l.payload() + "L" : "null";
        String a = term(base, dmax, false, false), b = term(exponent, dmax, false, false);
        return "Decimal.power(P, " + a + ", " + b + ", " + literal + ", " + (base instanceof Value.Literal) + ", " + dmax + ", " + last + ", " + rounded + ")";
    }

    private static boolean holds(Value v, String... operators) {
        if (!(v instanceof Value.Expression e)) return false;
        if (List.of(operators).contains(e.operator())) return true;
        return e.operands().stream().anyMatch(o -> holds(o, operators));
    }

    /** One operand as a term: a numeric item or literal with its integer places. */
    String operand(Value v) {
        if (x.counter(v) != null) return "Decimal.operand(" + x.counter(v) + ".get(), " + x.counterDigits(v) + ")";
        return switch (v) {
            case Value.Field f -> "Decimal.operand(" + x.field(f) + ".get(), " + integerPlaces(x.item(f.item())) + ")";
            case Value.Literal l when l.numeric() -> "Decimal.operand(new Decimal(new java.math.BigInteger(" + Emitter.quote(l.payload()) + "), " + l.scale() + "), " + Math.max(0, l.payload().replace("-", "").length() - l.scale()) + ")";
            case Value.Figurative f when x.numeric(f) -> "Decimal.operand(new Decimal(java.math.BigInteger.ZERO, 0), 1)";
            default -> x.unsupported("arithmetic operand " + v);
        };
    }

    /** An operand's integer places: its PICTURE's; an index, which has none, the fullword's nine (`Lower.index`; SQ214A's VARYING past 99, J-167). */
    static int integerPlaces(Item i) {
        if (i.attributes().category() == katana.sir.types.Attributes.Category.INDEX) return 9;
        return Math.max(0, Lower.stored(i.attributes()) - i.attributes().scale());
    }

    /** dmax for a statement: its receivers' decimal places and its operands'. */
    int dmax(List<Item> receivers, List<Value> operands) {
        int d = 0;
        for (Item r : receivers) d = Math.max(d, Math.max(0, r.attributes().scale()));
        for (Value v : operands) d = Math.max(d, decimals(v));
        return d;
    }
}
