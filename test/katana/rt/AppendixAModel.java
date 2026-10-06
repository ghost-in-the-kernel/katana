package katana.rt;

import java.math.BigInteger;
import java.util.List;
import java.util.function.IntBinaryOperator;

/**
 * An executable model of Appendix A (docs/research/ibm-options.md §4), written from the quoted text and
 * sharing nothing with `rt/`: numbers are exact fractions, not scaled integers; the places tables are
 * rows transcribed as the PDF prints them, not branches. Two facts it takes from cobc, not IBM (J-097,
 * J-099): what an intermediate cannot carry is truncated, and ROUNDED gives the last operation one more
 * decimal place. The differential test holds `rt/Decimal`, this model and cobc to the same answers.
 */
final class AppendixAModel {
    private AppendixAModel() {}

    /** An exact value: numerator over a positive denominator. */
    record Fraction(BigInteger num, BigInteger den) {
        static Fraction of(String text) {
            String t = text.startsWith("+") ? text.substring(1) : text;
            int point = t.indexOf('.');
            if (point < 0) return new Fraction(new BigInteger(t), BigInteger.ONE);
            return new Fraction(new BigInteger(t.replace(".", "")), BigInteger.TEN.pow(t.length() - point - 1));
        }

        Fraction plus(Fraction o) { return new Fraction(num.multiply(o.den).add(o.num.multiply(den)), den.multiply(o.den)); }
        Fraction minus(Fraction o) { return plus(new Fraction(o.num.negate(), o.den)); }
        Fraction times(Fraction o) { return new Fraction(num.multiply(o.num), den.multiply(o.den)); }

        Fraction over(Fraction o) {
            BigInteger n = num.multiply(o.den), d = den.multiply(o.num);
            return d.signum() < 0 ? new Fraction(n.negate(), d.negate()) : new Fraction(n, d);
        }

        /** Truncated toward zero to `decimals` places, as an integer count of 10^-decimals. */
        BigInteger truncated(int decimals) {
            return num.multiply(BigInteger.TEN.pow(decimals)).divide(den);
        }
    }

    /** A value with the places an intermediate carries: i integer, d decimal; `capped` once an operation's theoretical places passed the cap. */
    record Carried(Fraction value, int i, int d, boolean capped) {}

    /** One row of the p. 795-796 table: when it applies (i, d, dmax, C) and the integer and decimal places it carries. */
    private record Row(String when, Rule applies, IntBinaryOperator integer, IntBinaryOperator decimal) {}

    private interface Rule { boolean test(int i, int d, int dmax, int cap); }

    /** The table, row by row as printed; the operators take (value, cap) where the row needs both. */
    private static List<Row> table(int i, int d, int dmax) {
        return List.of(
                new Row("i + d <= C", (a, b, m, c) -> a + b <= c, (x, c) -> i, (x, c) -> d),
                new Row("i + d > C, d <= dmax", (a, b, m, c) -> a + b > c && b <= m, (x, c) -> c - d, (x, c) -> d),
                new Row("i + d > C, d > dmax, i + dmax <= C", (a, b, m, c) -> a + b > c && b > m && a + m <= c, (x, c) -> i, (x, c) -> c - i),
                new Row("i + d > C, d > dmax, i + dmax > C", (a, b, m, c) -> a + b > c && b > m && a + m > c, (x, c) -> c - dmax, (x, c) -> dmax));
    }

    /** The places carried for theoretical places (i, d), from the first table row that applies. */
    static int[] places(int i, int d, int dmax, int cap) {
        for (Row r : table(i, d, dmax)) if (r.applies().test(i, d, dmax, cap)) return new int[] {r.integer().applyAsInt(0, cap), r.decimal().applyAsInt(0, cap)};
        throw new IllegalStateException("no row of the table applies to i " + i + " d " + d + " dmax " + dmax);
    }

    /** One operation, as p. 795's first table gives its theoretical places, then carried and truncated; `extra` the ROUNDED place of a last operation. */
    static Carried apply(char op, Carried a, Carried b, int dmax, int cap, int extra) {
        int i, d;
        Fraction exact;
        switch (op) {
            case '+', '-' -> { i = Math.max(a.i(), b.i()) + 1; d = Math.max(a.d(), b.d()); exact = op == '+' ? a.value().plus(b.value()) : a.value().minus(b.value()); }
            case '*' -> { i = a.i() + b.i(); d = a.d() + b.d(); exact = a.value().times(b.value()); }
            // in division op1 is the divisor (b) and op2 the dividend (a): i2 + d1, max(d2 - d1, dmax)
            case '/' -> { i = a.i() + b.d(); d = Math.max(a.d() - b.d(), dmax); exact = a.value().over(b.value()); }
            default -> throw new IllegalArgumentException("operator " + op);
        }
        int[] carried = places(i, d, dmax, cap);
        int ci = carried[0], cd = carried[1] + extra;
        BigInteger units = exact.truncated(cd).remainder(BigInteger.TEN.pow(ci + cd));
        return new Carried(new Fraction(units, BigInteger.TEN.pow(cd)), ci, cd, a.capped() || b.capped() || i + d > cap);
    }

    /** A result stored into `digits` digits, `scale` decimal: half away from zero when ROUNDED, else truncated; the sign kept or dropped; the text it prints with its sign. */
    static String store(Carried r, int digits, int scale, boolean signed, boolean rounded) {
        Fraction v = r.value();
        BigInteger shifted = v.num().abs().multiply(BigInteger.TEN.pow(scale + 1)).divide(v.den());
        BigInteger kept = shifted.divide(BigInteger.TEN);
        if (rounded && shifted.mod(BigInteger.TEN).intValue() >= 5) kept = kept.add(BigInteger.ONE);
        kept = kept.remainder(BigInteger.TEN.pow(digits));
        boolean negative = signed && v.num().signum() < 0 && kept.signum() != 0;
        String text = String.format("%0" + digits + "d", kept);
        return (negative ? "-" : "+") + text.substring(0, digits - scale) + (scale > 0 ? "." + text.substring(digits - scale) : "");
    }
}
