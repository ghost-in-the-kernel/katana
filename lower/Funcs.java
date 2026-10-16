package katana.lower;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import katana.sir.types.Value;

/**
 * Intrinsic functions in the lowering (research §15): which are numeric, which floating point, and the
 * Java each becomes. A floating-point function makes its whole expression a `double` (PG 6.4 p. 800,
 * `rt/Floats`); an integer or mixed one is a fixed-point term (`rt/Functions`), its places `Places`';
 * an alphanumeric one a new item (a sending item).
 */
final class Funcs {
    /** The floating-point functions: every numeric function of LR Table 59 that PG pp. 798-799 does not give as integer or mixed. */
    static final Set<String> FLOATING = Set.of("ACOS", "ANNUITY", "ASIN", "ATAN", "COS", "LOG", "LOG10", "MEAN", "MEDIAN", "MIDRANGE", "NUMVAL", "NUMVAL-C",
            "PRESENT-VALUE", "RANDOM", "SIN", "SQRT", "STANDARD-DEVIATION", "TAN", "VARIANCE");
    /** Integer functions and the mixed ones, fixed point unless an argument is floating point. */
    static final Set<String> FIXED = Set.of("INTEGER", "INTEGER-PART", "MOD", "REM", "FACTORIAL", "MAX", "MIN", "RANGE", "SUM", "ORD-MAX", "ORD-MIN", "ORD",
            "LENGTH", "DATE-OF-INTEGER", "DAY-OF-INTEGER", "INTEGER-OF-DATE", "INTEGER-OF-DAY");
    static final Set<String> MIXED = Set.of("MAX", "MIN", "RANGE", "SUM", "REM");

    private final Exprs x;

    Funcs(Exprs x) {
        this.x = x;
    }

    /**
     * A function's arguments with each `ALL` subscript expanded: "Specifying ALL as a subscript is equivalent
     * to specifying all table elements possible using every valid subscript in that subscript position",
     * Table-name(1) first; with several, "the first implicit argument is Table-name(1, 1, 1)" and "the
     * rightmost subscript" is incremented first (LR 6.4 pp. 501-502), over tables of a fixed number of
     * occurrences.
     */
    List<Value> arguments(Value.Expression e) {
        List<Value> out = new ArrayList<>();
        for (Value a : e.operands()) expand(a, out);
        return out;
    }

    /** One argument's elements, its leftmost `ALL` the outermost loop, so the rightmost varies fastest. */
    // Decision: R-019
    private void expand(Value a, List<Value> out) {
        int all = a instanceof Value.Field f ? f.subscripts().indexOf(new Value.Name("keyword", "ALL")) : -1;
        if (all < 0) {
            out.add(a);
            return;
        }
        Value.Field f = (Value.Field) a;
        katana.sir.types.Item table = x.dimensions(x.item(f.item())).get(all);
        if (table.occurs().dependingOn() != 0) {
            out.add(new Value.Name("unresolved", x.unsupported("ALL over " + table.name() + ", an OCCURS DEPENDING ON table")));
            return;
        }
        for (int k = 1; k <= table.occurs().max(); k++) {
            List<Value> subscripts = new ArrayList<>(f.subscripts());
            subscripts.set(all, new Value.Literal(String.valueOf(k), Value.Literal.Kind.NUMERIC, String.valueOf(k), 0));
            expand(new Value.Field(f.item(), subscripts, f.start(), f.length()), out);
        }
    }

    /** A FUNCTION call's name, or null for any other value. */
    static String name(Value v) {
        return v instanceof Value.Expression e && e.operator().startsWith("FUNCTION ") ? e.operator().substring(9) : null;
    }

    /** Whether a function's value is numeric (integer or numeric type); MAX and MIN of alphanumeric arguments are not. */
    boolean numeric(Value.Expression e) {
        String n = name(e);
        if (FLOATING.contains(n)) return true;
        if (List.of("MAX", "MIN").contains(n)) return e.operands().stream().allMatch(x::numeric);
        return FIXED.contains(n);
    }

    /** Whether an expression is computed in floating point: it holds a floating-point function, a mixed one over a floating-point argument, or a floating-point literal (PG 6.4 p. 800). */
    boolean floating(Value v) {
        if (v instanceof Value.Literal l) return l.kind() == Value.Literal.Kind.FLOAT;
        if (!(v instanceof Value.Expression e)) return false;
        String n = name(e);
        if (n != null && FLOATING.contains(n)) return true;
        if (n != null && !MIXED.contains(n)) return false;
        return e.operands().stream().anyMatch(this::floating);
    }

    /** A numeric value as a Java `double`, every operation in floating point. */
    // Decision: R-023
    String dbl(Value v, Arith arith) {
        if (x.counter(v) != null) return "Floats.of(" + x.counter(v) + ".get())";
        if (v instanceof Value.Literal l && l.kind() == Value.Literal.Kind.FLOAT) return "Double.parseDouble(" + Emitter.quote(l.text().replace("+", "")) + ")";
        if (!(v instanceof Value.Expression e)) return "Floats.of(" + arith.result(v, 0, false) + ".value())";
        String n = name(e);
        List<String> args = new ArrayList<>();
        // NUMVAL and NUMVAL-C take an alphanumeric argument, read as text in `call`, never as a number.
        if (n == null || !n.startsWith("NUMVAL")) for (Value o : n == null ? e.operands() : arguments(e)) args.add(dbl(o, arith));
        return n == null ? operation(e.operator(), args) : call(n, e, args, arith);
    }

    /** An arithmetic operation over `double` operands. */
    private String operation(String operator, List<String> args) {
        return switch (operator) {
            case "+", "-", "*", "/" -> "(" + args.get(0) + " " + operator + " " + args.get(1) + ")";
            case "**" -> "Floats.power(" + args.get(0) + ", " + args.get(1) + ")";
            case "neg" -> "(-" + args.get(0) + ")";
            case "pos" -> args.get(0);
            default -> "(double) " + x.unsupported("operator " + operator);
        };
    }

    /** A function call in floating point: a floating-point function, or a mixed one over a floating-point argument. */
    private String call(String n, Value.Expression e, List<String> args, Arith arith) {
        return switch (n) {
            case "NUMVAL" -> "Floats.numval(" + x.sending(e.operands().get(0)) + ", " + point() + ", null)";
            case "NUMVAL-C" -> "Floats.numval(" + x.sending(e.operands().get(0)) + ", " + point() + ", " + (e.operands().size() > 1 ? "new String(Literal.bytes(" + x.sending(e.operands().get(1)) + "), cp.charset).trim()" : Emitter.quote(x.p.header().currency())) + ")";
            case "RANDOM" -> "Floats.random(" + (e.operands().isEmpty() ? "null" : "(long) " + args.get(0)) + ")";
            case "STANDARD-DEVIATION" -> "Floats.standardDeviation(" + String.join(", ", args) + ")";
            case "PRESENT-VALUE" -> "Floats.presentValue(" + String.join(", ", args) + ")";
            case "MAX", "MIN" -> "java.util.Arrays.stream(new double[] {" + String.join(", ", args) + "})." + n.toLowerCase() + "().getAsDouble()";
            case "SUM" -> "java.util.Arrays.stream(new double[] {" + String.join(", ", args) + "}).sum()";
            case "RANGE" -> "(java.util.Arrays.stream(new double[] {" + String.join(", ", args) + "}).max().getAsDouble() - java.util.Arrays.stream(new double[] {" + String.join(", ", args) + "}).min().getAsDouble())";
            case "REM" -> "(" + args.get(0) + " - " + args.get(1) + " * (double) (long) (" + args.get(0) + " / " + args.get(1) + "))";
            default -> FLOATING.contains(n) ? "Floats." + camel(n) + "(" + String.join(", ", args) + ")" : "Floats.of(" + fixed(e, arith) + ")";
        };
    }

    /** An integer or mixed function over fixed-point arguments, as a Java `Decimal`. */
    String fixed(Value.Expression e, Arith arith) {
        String n = name(e);
        List<String> args = new ArrayList<>();
        if (List.of("ORD-MAX", "ORD-MIN").contains(n) && !arguments(e).stream().allMatch(x::numeric)) return "Functions." + camel(n) + "Text(co, " + texts(e) + ")";
        if (List.of("ORD", "LENGTH").contains(n)) return "Functions." + n.toLowerCase() + "(" + x.sending(e.operands().get(0)) + (n.equals("ORD") ? ", co" : "") + ")";
        // Each argument computed with the function's inner-dmax, its arguments' decimal places (PG 6.4 p. 798): REM(3 / 2, .75) divides to two places (NIST IF133A).
        int inner = arguments(e).stream().mapToInt(arith::decimals).max().orElse(0);
        for (Value o : arguments(e)) args.add(arith.result(o, inner, false) + ".value()");
        return FIXED.contains(n) ? "Functions." + camel(n) + "(" + String.join(", ", args) + ")" : x.unsupported("FUNCTION " + n);
    }

    /** An alphanumeric function as a sending item: a new item holding its value. */
    String alphanumeric(Value.Expression e, Arith arith) {
        String n = name(e);
        return switch (n) {
            case "UPPER-CASE", "LOWER-CASE", "REVERSE" -> "Functions." + camel(n) + "(" + x.sending(e.operands().get(0)) + ")";
            case "CHAR" -> "Functions.chr(" + arith.result(e.operands().get(0), 0, false) + ".value(), co, cp)";
            case "CURRENT-DATE" -> "Functions.currentDate(cp)";
            case "MAX", "MIN" -> "Functions." + n.toLowerCase() + "Text(co, " + texts(e) + ")";
            case "WHEN-COMPILED" -> "Literal.text(" + Emitter.quote(Lower.compiled()) + ", cp)";
            default -> x.unsupported("FUNCTION " + n + " as an alphanumeric value");
        };
    }

    /** Each argument as a sending item, an `ALL` expanded. */
    private String texts(Value.Expression e) {
        List<String> out = new ArrayList<>();
        for (Value a : arguments(e)) out.add(x.sending(a));
        return String.join(", ", out);
    }

    private String point() {
        return x.p.header().decimalComma() ? "','" : "'.'";
    }

    /** UPPER-CASE as upperCase, INTEGER-OF-DATE as integerOfDate. */
    private static String camel(String name) {
        StringBuilder out = new StringBuilder();
        boolean up = false;
        for (char c : name.toLowerCase().toCharArray()) {
            if (c == '-') up = true;
            else {
                out.append(up ? Character.toUpperCase(c) : c);
                up = false;
            }
        }
        return out.toString();
    }
}
