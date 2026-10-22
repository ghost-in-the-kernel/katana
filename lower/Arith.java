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
            // An integer function's outer-dmax "is always zero"; a mixed one's is its arguments' (PG 6.4 pp. 798-799).
            case Value.Expression e when Funcs.name(e) != null -> Funcs.MIXED.contains(Funcs.name(e)) ? e.operands().stream().mapToInt(this::decimals).max().orElse(0) : 0;
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
        // An expression holding a floating-point function is computed whole in floating point (PG 6.4 p. 800).
        if (x.funcs.floating(v)) return "Floats.term(" + x.funcs.dbl(v, this) + ")";
        if (Funcs.name(v) != null && x.numeric(v)) return "Functions.term(" + x.funcs.fixed((Value.Expression) v, this) + ", " + Places.of(v, x).places() + ")";
        if (!(v instanceof Value.Expression e) || !x.numeric(v) || e.operator().equals("LENGTH OF")) return operand(v);
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

    /**
     * An expression passes/Exact holds exact (6.4, research §41) as plain arithmetic, a term: `long` when every
     * operand is an integer and every operation's integer places are at most 18 (no `long` overflows), else
     * `BigDecimal`'s.
     */
    // Decision: R-020
    String exact(Value v) {
        return "Decimal.exact(" + (integers(v) <= 18 ? whole(v) : big(v)) + ")";
    }

    /** The most integer places any operation of an all-integer expression carries; past 18 when an operand has decimal places. */
    private int integers(Value v) {
        return switch (v) {
            case Value.Field f -> x.promoted.big(f.item()) ? 99 : Lower.stored(x.item(f.item()).attributes());
            case Value.Literal l -> l.scale() > 0 ? 99 : l.payload().replace("-", "").length();
            case Value.Expression e when e.operands().size() == 1 -> integers(e.operands().get(0));
            case Value.Expression e when e.operator().equals("*") -> integers(e.operands().get(0)) + integers(e.operands().get(1));
            case Value.Expression e -> Math.max(Math.max(integers(e.operands().get(0)), integers(e.operands().get(1))) + 1, 0);
            default -> 1;
        };
    }

    /** As `long` arithmetic: a promoted item's name, a literal, `(a + b)`. */
    private String whole(Value v) {
        return switch (v) {
            case Value.Field f -> x.promoted.name(f.item());
            case Value.Literal l -> l.payload() + (new java.math.BigInteger(l.payload()).bitLength() > 31 ? "L" : "");
            case Value.Expression e when e.operator().equals("neg") -> "(-" + whole(e.operands().get(0)) + ")";
            case Value.Expression e when e.operator().equals("pos") -> whole(e.operands().get(0));
            case Value.Expression e -> "(" + whole(e.operands().get(0)) + " " + e.operator() + " " + whole(e.operands().get(1)) + ")";
            default -> "0";
        };
    }

    /** As `BigDecimal` arithmetic: `a.add(b)`, a `long` item `BigDecimal.valueOf(it)`, a literal at its scale. */
    private String big(Value v) {
        return switch (v) {
            case Value.Field f when x.promoted.money(f.item()) -> x.promoted.name(f.item()) + ".amount()";
            case Value.Field f when x.promoted.big(f.item()) -> x.promoted.name(f.item());
            case Value.Field f -> "java.math.BigDecimal.valueOf(" + x.promoted.name(f.item()) + ")";
            case Value.Literal l -> "new java.math.BigDecimal(\"" + new java.math.BigDecimal(new java.math.BigInteger(l.payload()), l.scale()).toPlainString() + "\")";
            case Value.Expression e when e.operator().equals("neg") -> big(e.operands().get(0)) + ".negate()";
            case Value.Expression e when e.operator().equals("pos") -> big(e.operands().get(0));
            case Value.Expression e -> big(e.operands().get(0)) + "." + switch (e.operator()) {
                case "+" -> "add";
                case "-" -> "subtract";
                default -> "multiply";
            } + "(" + big(e.operands().get(1)) + ")";
            default -> "java.math.BigDecimal.ZERO";
        };
    }

    /** One operand as a term: its value with the integer places `Places` states for it. */
    String operand(Value v) {
        Places.Size size = Places.of(v, x);
        if (size.refused() != null) return x.unsupported("arithmetic operand " + size.refused());
        if (x.counter(v) != null) return "Decimal.operand(" + x.counter(v) + ".get(), " + size.places() + ")";
        return switch (v) {
            case Value.Field f when x.promoted.has(f.item()) -> "Decimal.operand(" + x.promoted.decimal(f.item()) + ", " + size.places() + ")";
            case Value.Field f -> "Decimal.operand(" + x.field(f) + ".get(), " + size.places() + ")";
            case Value.Literal l when l.numeric() -> "Decimal.operand(new Decimal(new java.math.BigInteger(" + Emitter.quote(l.payload()) + "), " + l.scale() + "), " + size.places() + ")";
            case Value.Figurative f when x.numeric(f) -> "Decimal.operand(new Decimal(java.math.BigInteger.ZERO, 0), " + size.places() + ")";
            // LENGTH OF: "the number of bytes used by a data item" (LR 6.4 p. 22), the item's length.
            case Value.Expression e when e.operator().equals("LENGTH OF") && e.operands().get(0) instanceof Value.Field f && x.item(f.item()).occurs() == null
                    -> "Decimal.operand(new Decimal(java.math.BigInteger.valueOf(" + x.item(f.item()).length() + "), 0), " + size.places() + ")";
            default -> x.unsupported("arithmetic operand " + v + ", not held (its places would be " + size.places() + ")");
        };
    }

    /** An item's integer places, as `Places` states them. */
    static int integerPlaces(Item i) {
        return Places.item(i).places();
    }

    /** dmax for a statement: its receivers' decimal places and its operands'. */
    int dmax(List<Item> receivers, List<Value> operands) {
        int d = 0;
        for (Item r : receivers) d = Math.max(d, Math.max(0, r.attributes().scale()));
        for (Value v : operands) d = Math.max(d, decimals(v));
        return d;
    }
}
