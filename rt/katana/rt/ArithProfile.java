package katana.rt;

/**
 * How many integer and decimal places a fixed-point intermediate result carries (phase 3.1), as IBM's
 * Enterprise COBOL 6.4 Programming Guide, Appendix A, gives them (docs/research/ibm-options.md §4):
 * the places theoretically possible for + - * / (p. 795), then cut to the profile's cap, 30 digits
 * under ARITH(COMPAT) and 31 under ARITH(EXTEND) (pp. 795-796). EXACT carries the theoretical places
 * uncut. `rt/Decimal` computes with the places this says; how it drops the places an intermediate
 * cannot carry is its own, still open (PROFILE.md).
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
