package katana.findings;

/**
 * A divergence kind (8A.2, research §49, R-072): one way a Java port departs from the COBOL it was written from, as
 * a catalogue row: the COBOL rule and the IBM text it rests on, the wrong Java shape a port takes, and the exact
 * Java that keeps the rule (`rt/`'s call, what the divergence report offers as the fix). docs/DIVERGENCES.md is this
 * table; each kind has a generator (`oracle/prove/Generators`) and a mutation that shows it.
 */
public final class Divergence {
    private Divergence() {}

    /** The kinds, each a row. */
    public enum Kind {
        DV_01("DV-01", "high-order truncation", "a result too large for its receiver loses its high-order digits, with no SIZE ERROR phrase (LR 6.4 p. 285; TRUNC(STD))",
                "the value modulo 10 to the receiver's integer digits (`Arithmetic.store` without the phrase)", "a fixed-width writer cutting the formatted value at its right end"),
        DV_02("DV-02", "scale truncation", "excess low-order digits are dropped, not rounded, without ROUNDED (LR 6.4 p. 263)",
                "the value set to the receiver's scale, rounding down (`Decimal.store`, ROUNDED false)", "HALF_UP, the common reading of a COMPUTE"),
        DV_03("DV-03", "intermediate precision (dmax)", "a quotient is carried to dmax decimal places, the most of any operand or receiver, before the next operation (PG 6.4 Appendix A)",
                "each operation at Appendix A's places (`Decimal.divide` at dmax)", "the whole quotient in `MathContext.DECIMAL128`, cut only at the store");

        public final String code, title, rule, exact, wrong;

        Kind(String code, String title, String rule, String exact, String wrong) {
            this.code = code;
            this.title = title;
            this.rule = rule;
            this.exact = exact;
            this.wrong = wrong;
        }
    }
}
