package katana.rt;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Floating-point intrinsic functions and the arithmetic around them (PG 6.4 pp. 800-801, research §15):
 * "If any operation in an arithmetic expression is computed in floating-point arithmetic, the entire
 * expression is computed as if all operands were converted to floating point"; "In compatibility mode,
 * floating-point intrinsic functions always return a long (64-bit) floating-point value". Computed in
 * Java `double`, IEEE binary64, where IBM's long is hexadecimal: R-023's approximation (NE-06), whose C-2
 * NIST IF tries.
 */
public final class Floats {
    private Floats() {}

    /** A fixed-point value converted to floating point. */
    public static double of(Decimal d) {
        return d.toBigDecimal().doubleValue();
    }

    /**
     * A floating-point result as a term for the statement to store or compare: its exact binary value,
     * cut to forty decimal places (no receiver holds more), its places what the value needs.
     */
    // Decision: R-023
    public static Decimal.Term term(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) return new Decimal.Term(new Decimal(java.math.BigInteger.ZERO, 0), new ArithProfile.Places(1, 0), true);
        BigDecimal exact = new BigDecimal(value);
        if (exact.scale() > 40) exact = exact.setScale(40, RoundingMode.DOWN);
        if (exact.scale() < 0) exact = exact.setScale(0);
        Decimal d = new Decimal(exact.unscaledValue(), exact.scale());
        return Decimal.operand(d, Math.max(1, exact.precision() - exact.scale()));
    }

    public static double acos(double x) {
        return Math.acos(x);
    }

    public static double asin(double x) {
        return Math.asin(x);
    }

    public static double atan(double x) {
        return Math.atan(x);
    }

    public static double cos(double x) {
        return Math.cos(x);
    }

    public static double sin(double x) {
        return Math.sin(x);
    }

    public static double tan(double x) {
        return Math.tan(x);
    }

    public static double log(double x) {
        return Math.log(x);
    }

    public static double log10(double x) {
        return Math.log10(x);
    }

    public static double sqrt(double x) {
        return Math.sqrt(x);
    }

    /** `x ** y` in floating point. */
    public static double power(double x, double y) {
        return Math.pow(x, y);
    }

    /** ANNUITY(N1, I2): "Ratio of annuity paid for I2 periods at interest of N1 to initial investment of one" (LR 6.4 pp. 509, 522): N1 / (1 - (1 + N1) ** -I2), or 1 / I2 when N1 is zero. */
    public static double annuity(double rate, double periods) {
        return rate == 0 ? 1 / periods : rate / (1 - Math.pow(1 + rate, -periods));
    }

    /** PRESENT-VALUE(N1, N2...): the sum of each N2 / (1 + N1) ** its position (LR 6.4 p. 620). */
    public static double presentValue(double rate, double... amounts) {
        double sum = 0;
        for (int k = 0; k < amounts.length; k++) sum += amounts[k] / Math.pow(1 + rate, k + 1);
        return sum;
    }

    public static double mean(double... xs) {
        double sum = 0;
        for (double v : xs) sum += v;
        return sum / xs.length;
    }

    /** MEDIAN: the middle argument in order, or the mean of the two middle ones. */
    public static double median(double... xs) {
        double[] s = xs.clone();
        java.util.Arrays.sort(s);
        int n = s.length;
        return n % 2 == 1 ? s[n / 2] : (s[n / 2 - 1] + s[n / 2]) / 2;
    }

    /** MIDRANGE: the mean of the least and the greatest argument. */
    public static double midrange(double... xs) {
        double lo = xs[0], hi = xs[0];
        for (double v : xs) {
            lo = Math.min(lo, v);
            hi = Math.max(hi, v);
        }
        return (lo + hi) / 2;
    }

    /** VARIANCE: the mean of the squares of each argument's difference from the mean. */
    public static double variance(double... xs) {
        double m = mean(xs), sum = 0;
        for (double v : xs) sum += (v - m) * (v - m);
        return sum / xs.length;
    }

    public static double standardDeviation(double... xs) {
        return Math.sqrt(variance(xs));
    }

    private static long seed = 0;
    private static boolean seeded;

    /**
     * RANDOM [(I1)]: a value from 0 up to but not including 1; I1 starts a new sequence, its first value
     * returned; with no argument the next, and a first reference with none seeds zero (LR 6.4 p. 622). IBM names no generator: this
     * is the lane's (a 48-bit linear congruential one), so a program that prints its values differs.
     */
    // Unsure: RANDOM's sequence is IBM's own and not published; the lane's generator is its own. Stays: only a value compared by range is IBM's; a program that prints one cannot be matched without IBM's generator.
    public static double random(Long start) {
        if (start != null || !seeded) {
            seed = ((start == null ? 0 : start) ^ 0x5DEECE66DL) & ((1L << 48) - 1);
            seeded = true;
        }
        seed = (seed * 0x5DEECE66DL + 0xBL) & ((1L << 48) - 1);
        return (seed >>> 22) / (double) (1L << 26);
    }

    /**
     * NUMVAL and NUMVAL-C (LR 6.4 pp. 606-609): the value of a numeric string: leading and trailing spaces,
     * a sign (+ or - before or after, or CR or DB after), digits with one decimal point; for NUMVAL-C also
     * the currency sign and commas between digits. `point` is the decimal point ('.' or ',' under
     * DECIMAL-POINT IS COMMA), `currency` the currency string or null for NUMVAL. A string that is not
     * such a number gives 0.
     */
    // Decision: R-023
    public static double numval(Field arg, char point, String currency) {
        String s = new String(Literal.bytes(arg), arg.record().codepage().charset).trim().toUpperCase();
        boolean negative = false;
        if (s.endsWith("CR") || s.endsWith("DB")) {
            negative = true;
            s = s.substring(0, s.length() - 2).trim();
        }
        if (s.startsWith("-") || s.endsWith("-")) negative = true;
        s = s.replaceAll("^[+-]|[+-]$", "").trim();
        if (currency != null) {
            if (s.startsWith(currency)) s = s.substring(currency.length()).trim();
            s = s.replace(String.valueOf(point == '.' ? ',' : '.'), "");
        }
        s = s.replace(point, '.').replace(" ", "");
        if (s.isEmpty() || !s.matches("\\d*\\.?\\d*") || s.equals(".")) return 0;
        double v = Double.parseDouble(s);
        return negative ? -v : v;
    }
}
