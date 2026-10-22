package katana.rt;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Consumer;

/**
 * A promoted item (6.3, R-068): a numeric item no bytes alias, held as a Java `long` or `BigDecimal`
 * instead of in a record. Every store into it is the function the byte form stores by, so the value it
 * holds is the value `Field.get()` would have returned; a read that needs bytes (a DISPLAY, a MOVE to
 * text) encodes the value into a field of its own at that point.
 */
public final class Scalars {
    private Scalars() {}

    /** A `long` item's value, its unscaled digits at its scale. */
    public static Decimal of(long unscaled, int scale) {
        return new Decimal(BigInteger.valueOf(unscaled), scale);
    }

    /** A `BigDecimal` item's value. */
    public static Decimal of(BigDecimal value) {
        return new Decimal(value.unscaledValue(), value.scale());
    }

    /** A value as a `long` item holds it: its unscaled digits (at most 18, the item's PICTURE's). */
    public static long longOf(Decimal value) {
        return value.unscaled().longValueExact();
    }

    /** A value as a `BigDecimal` item holds it, at its scale. */
    public static BigDecimal bigOf(Decimal value) {
        return value.toBigDecimal();
    }

    /** A field of the item's attributes, its own bytes, holding `value`: what a read that needs bytes reads. */
    public static Field field(Attr attr, Codepage cp, Decimal value) {
        Field f = new Field(Record.of(attr.length(), cp), 0, attr);
        f.put(value);
        return f;
    }

    /** What a statement leaves in a field of the item's attributes, starting from zero: a MOVE into the item, or its VALUE. */
    public static Decimal received(Attr attr, Codepage cp, Consumer<Field> statement) {
        Field f = field(attr, cp, new Decimal(BigInteger.ZERO, attr.scale()));
        statement.accept(f);
        return f.get();
    }

    /**
     * A result stored into the item as `Arithmetic.store` stores it into its bytes (R-020): `set` given the
     * truncated (or rounded) value unless the division was by zero or a size error has its phrase; true
     * when a size error occurred.
     */
    // Decision: R-068
    public static boolean store(Decimal.Term result, int digits, int scale, boolean signed, boolean rounded, boolean onSizeError, Consumer<Decimal> set) {
        Decimal.Stored s = Decimal.store(result, digits, scale, signed, rounded);
        Events.stored(s, onSizeError, result.divisionByZero());
        if (result.divisionByZero() || (s.sizeError() && onSizeError)) return s.sizeError();
        set.accept(s.value());
        return s.sizeError();
    }
}
