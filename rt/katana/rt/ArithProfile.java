package katana.rt;

/**
 * How many integer and decimal places a fixed-point intermediate result carries (phase 3.1), as IBM's
 * Enterprise COBOL 6.4 Programming Guide, Appendix A, gives them (docs/research/ibm-options.md §4):
 * the places theoretically possible for + - * / (p. 795), then cut to the profile's cap, 30 digits
 * under ARITH(COMPAT) and 31 under ARITH(EXTEND) (pp. 795-796). EXACT carries the theoretical places
 * uncut. The places an intermediate cannot carry are truncated, not rounded: cobc -std=ibm prints 1.98
 * for `A / B * B` into 9V99 with A 2 and B 3, where rounding would give 2.01 (J-097).
 */
public enum ArithProfile {
    IBM_COMPAT(30), IBM_EXTEND(31), EXACT(Integer.MAX_VALUE);

    /** An intermediate result's integer and decimal places. */
    public record Places(int integer, int decimal) {}

    /** The most digits a fixed-point intermediate carries. */
    public final int cap;

    ArithProfile(int cap) {
        this.cap = cap;
    }

    /**
     * A statement's dmax (p. 794): the greatest of the receivers' decimal places and the operands'
     * (divisors and exponents left out by the caller). ROUNDED adds nothing here; its place is the last
     * operation's (`last`). On cobc -std=ibm, with A 2 and B 3: `A / B * B` into 9V9 ROUNDED is 1.8, so
     * the earlier intermediates carry no extra place (J-097, J-099).
     */
    // Decision: R-049
    public static int dmax(int receiverDecimals, int... operandDecimals) {
        int dmax = receiverDecimals;
        for (int d : operandDecimals) dmax = Math.max(dmax, d);
        return dmax;
    }

    /**
     * The places a statement's last operation carries: one more decimal place when the receiver is
     * ROUNDED, so the rounding has a digit to look at. On cobc -std=ibm, `A / B` into 9V9 is 0.6 and
     * `A / B` into 9V9 ROUNDED is 0.7 (2 and 3; J-099), which no reading without that place gives.
     */
    // Unsure: IBM's text says one more place "might be carried" under ROUNDED (PG p. 794) and APAR PH41526 speaks of ROUNDED intermediates over 31 digits; whether IBM carries it in every intermediate, not only the last, is unsettled without a run on IBM (J-099).
    // Decision: R-049
    public Places last(Places places, boolean rounded) {
        return rounded ? new Places(places.integer(), places.decimal() + 1) : places;
    }

    /** `op1 + op2` or `op1 - op2`: the greater integer places and one more, the greater decimal places (p. 795). */
    // Decision: R-049
    public Places add(int i1, int d1, int i2, int d2, int dmax) {
        return carried(Math.max(i1, i2) + 1, Math.max(d1, d2), dmax);
    }

    /** `op1 * op2`: the integer places summed and the decimal places summed (p. 795). */
    // Decision: R-049
    public Places multiply(int i1, int d1, int i2, int d2, int dmax) {
        return carried(i1 + i2, d1 + d2, dmax);
    }

    /** `op2 / op1`, op1 the divisor and op2 the dividend as IBM names them: i2 + d1 integer places, d2 - d1 or dmax decimal places, whichever is greater (p. 795). */
    // Decision: R-049
    public Places divide(int i1, int d1, int i2, int d2, int dmax) {
        return carried(i2 + d1, Math.max(d2 - d1, dmax), dmax);
    }

    /**
     * `op1 ** op2`, op2 an integer (p. 796): an integral literal `n` carries d1 * |n| decimal places and
     * i1 * |n| integer places (for a literal op1, `literalIntegers`, the integers of op1 ** |n|) while
     * i + d is under the cap, or at a 30-digit cap with op1 of an odd number of digits; any other
     * integral exponent, a data-name or variable, carries cap - dmax integer and dmax decimal places
     * (p. 797). `literal` is null for a data-name exponent.
     */
    // Decision: R-049
    // Unsure: IBM's exception at the cap ("a 30-digit integer raised to the power of literal 1") is not modelled; nor is the odd-digit case under EXTEND, which the text gives only for COMPAT (J-124).
    public Places power(int i1, int d1, Long literal, int literalIntegers, int dmax) {
        if (literal == null) return this == EXACT ? new Places(i1, d1) : new Places(cap - dmax, dmax);
        long n = Math.abs(literal);
        int i = literalIntegers >= 0 ? literalIntegers : (int) (i1 * n), d = (int) (d1 * n);
        boolean odd = this == IBM_COMPAT && i + d == cap && (i1 + d1) % 2 == 1;
        if (this == EXACT || i + d < cap || odd) return new Places(i, d);
        return new Places(cap - dmax, dmax);
    }

    /**
     * IBM's table of places carried when i + d passes the cap (pp. 795-796): within it, all of them;
     * past it with d no more than dmax, the decimals kept and integer places lost; with d more than
     * dmax, decimals cut to what the integer places leave, but never below dmax.
     */
    // Decision: R-049
    Places carried(int i, int d, int dmax) {
        if (this == EXACT || i + d <= cap) return new Places(i, d);
        if (d <= dmax) return new Places(cap - d, d);
        if (i + dmax <= cap) return new Places(i, cap - i);
        return new Places(cap - dmax, dmax);
    }
}
