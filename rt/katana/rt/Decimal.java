package katana.rt;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * A fixed-point COBOL number (R-020): an unscaled `BigInteger` and an `int` scale, never a
 * `MathContext`. Arithmetic is done one operation at a time on `Term`s, each carrying the integer and
 * decimal places its `ArithProfile` gives it (Appendix A), the places it cannot carry dropped:
 * decimals truncated, high-order integer digits lost (J-097). `store` puts a result into a receiver
 * as the Language Reference says (p. 296): truncated unless ROUNDED, which adds 1 to the last digit
 * kept when the first digit dropped is 5 or more; high-order digits that do not fit are a size error,
 * the receiver keeping the truncated value only when the statement has no SIZE ERROR phrase (the
 * caller's to decide). Division by zero is a status, never an exception.
 */
public record Decimal(BigInteger unscaled, int scale) {
    /** A number from its text, as COBOL writes a numeric literal: `-12.34`, `+5`, `.5`. */
    public static Decimal of(String text) {
        BigDecimal b = new BigDecimal(text);
        return new Decimal(b.unscaledValue(), Math.max(0, b.scale()));
    }

    /** This number as Java's own decimal, for printing and comparing. */
    public BigDecimal toBigDecimal() {
        return new BigDecimal(unscaled, scale);
    }

    @Override
    public String toString() {
        return toBigDecimal().toPlainString();
    }

    /** A value in an arithmetic statement and the places it carries; `divisionByZero` once a division by zero has been met. */
    public record Term(Decimal value, ArithProfile.Places places, boolean divisionByZero) {}

    /**
     * An exact value as a term (6.4, research §41): what Appendix A's step-wise `+ - *` carries when no step
     * passes the cap, written as plain arithmetic; its places the value's own.
     */
    // Decision: R-020
    public static Term exact(BigDecimal value) {
        BigInteger whole = value.setScale(0, RoundingMode.DOWN).abs().toBigInteger();
        return new Term(new Decimal(value.unscaledValue(), value.scale()), new ArithProfile.Places(Math.max(1, whole.toString().length()), Math.max(0, value.scale())), false);
    }

    /** An exact integer value as a term. */
    public static Term exact(long value) {
        return exact(BigDecimal.valueOf(value));
    }

    /** An operand: a data item or literal with the integer places its PICTURE (or its digits) gives it. */
    public static Term operand(Decimal value, int integerPlaces) {
        return new Term(value, new ArithProfile.Places(integerPlaces, value.scale()), false);
    }

    /** `a + b`, carried as the profile says; `last` when it is the statement's last operation, `rounded` when its receiver is ROUNDED. */
    // Decision: R-020
    public static Term add(ArithProfile p, Term a, Term b, int dmax, boolean last, boolean rounded) {
        ArithProfile.Places places = p.add(a.places().integer(), a.places().decimal(), b.places().integer(), b.places().decimal(), dmax);
        return cut(a.value().toBigDecimal().add(b.value().toBigDecimal()), last ? p.last(places, rounded) : places, a, b);
    }

    /** `a - b`, carried as `add` is. */
    // Decision: R-020
    public static Term subtract(ArithProfile p, Term a, Term b, int dmax, boolean last, boolean rounded) {
        ArithProfile.Places places = p.add(a.places().integer(), a.places().decimal(), b.places().integer(), b.places().decimal(), dmax);
        return cut(a.value().toBigDecimal().subtract(b.value().toBigDecimal()), last ? p.last(places, rounded) : places, a, b);
    }

    /** `a * b`, carried as the profile says. */
    // Decision: R-020
    public static Term multiply(ArithProfile p, Term a, Term b, int dmax, boolean last, boolean rounded) {
        ArithProfile.Places places = p.multiply(a.places().integer(), a.places().decimal(), b.places().integer(), b.places().decimal(), dmax);
        return cut(a.value().toBigDecimal().multiply(b.value().toBigDecimal()), last ? p.last(places, rounded) : places, a, b);
    }

    /** Unary minus: the value negated, its places unchanged. */
    public static Term negate(Term a) {
        return new Term(new Decimal(a.value().unscaled().negate(), a.value().scale()), a.places(), a.divisionByZero());
    }

    /** `a / b`: the divisor is IBM's op1 and the dividend op2; the quotient computed to the places carried and truncated; a zero divisor a status. */
    // Decision: R-020
    public static Term divide(ArithProfile p, Term a, Term b, int dmax, boolean last, boolean rounded) {
        ArithProfile.Places places = p.divide(b.places().integer(), b.places().decimal(), a.places().integer(), a.places().decimal(), dmax);
        if (last) places = p.last(places, rounded);
        if (b.value().unscaled().signum() == 0 || a.divisionByZero() || b.divisionByZero()) return new Term(new Decimal(BigInteger.ZERO, places.decimal()), places, true);
        BigDecimal quotient = a.value().toBigDecimal().divide(b.value().toBigDecimal(), places.decimal(), RoundingMode.DOWN);
        if (Events.ON && quotient.multiply(b.value().toBigDecimal()).compareTo(a.value().toBigDecimal()) != 0) Events.note("DMAX_CUT");
        return cut(quotient, places, a, b);
    }

    /**
     * `a ** b`, b an integer (PG 6.4 pp. 796-797): its places as the profile gives them (`literal` the
     * exponent when it is an integral literal, else null; `literalBase` when a is a literal); an exponent
     * of more than nine digits truncated to nine; b zero gives 1; a negative b then divides 1 by the
     * result, a zero result a division by zero. The repeated multiplication is exact, then cut.
     */
    // Decision: R-020
    public static Term power(ArithProfile p, Term a, Term b, Long literal, boolean literalBase, int dmax, boolean last, boolean rounded) {
        BigInteger whole = b.value().toBigDecimal().toBigInteger();
        int k = whole.abs().remainder(BigInteger.TEN.pow(9)).intValueExact();
        boolean negative = whole.signum() < 0 && k != 0;
        BigDecimal exact = k == 0 ? BigDecimal.ONE : a.value().toBigDecimal().pow(k);
        BigInteger integerPart = exact.setScale(0, RoundingMode.DOWN).abs().toBigInteger();
        int integers = !literalBase ? -1 : integerPart.signum() == 0 ? 0 : integerPart.toString().length();
        ArithProfile.Places places = p.power(a.places().integer(), a.places().decimal(), literal, integers, dmax);
        if (k == 0) places = new ArithProfile.Places(Math.max(1, places.integer()), places.decimal());
        if (!negative) return cut(exact, last ? p.last(places, rounded) : places, a, b);
        Term preliminary = cut(exact, places, a, b);
        return divide(p, operand(Decimal.of("1"), 1), preliminary, dmax, last, rounded);
    }

    /** An exact result cut to the places carried: decimals truncated, high-order integer digits beyond the integer places lost. */
    private static Term cut(BigDecimal exact, ArithProfile.Places places, Term a, Term b) {
        BigInteger unscaled = exact.setScale(places.decimal(), RoundingMode.DOWN).unscaledValue();
        BigInteger limit = BigInteger.TEN.pow(places.integer() + places.decimal());
        return new Term(new Decimal(unscaled.remainder(limit), places.decimal()), places, a.divisionByZero() || b.divisionByZero());
    }

    /** What a store left: the receiver's new value, whether high-order digits did not fit or a division by zero was met (a size error), and whether a negative value lost its sign to an unsigned receiver. */
    public record Stored(Decimal value, boolean sizeError, boolean signDropped) {}

    /**
     * A result into a receiver of `digits` digits, `scale` of them decimal: rounded half away from zero
     * when ROUNDED, else truncated; the sign dropped for an unsigned receiver; high-order digits beyond
     * the receiver lost, and a size error said (LR p. 296; rounding before the size check).
     */
    // Decision: R-020
    public static Stored store(Term result, int digits, int scale, boolean signed, boolean rounded) {
        if (result.divisionByZero()) return new Stored(null, true, false);
        BigDecimal aligned = result.value().toBigDecimal().setScale(scale, rounded ? RoundingMode.HALF_UP : RoundingMode.DOWN);
        BigInteger unscaled = aligned.unscaledValue();
        boolean signDropped = !signed && unscaled.signum() < 0;
        if (!signed) unscaled = unscaled.abs();
        BigInteger limit = BigInteger.TEN.pow(digits);
        boolean sizeError = unscaled.abs().compareTo(limit) >= 0;
        return new Stored(new Decimal(unscaled.remainder(limit), scale), sizeError, signDropped);
    }
}
