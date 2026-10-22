package katana.rt;

import java.math.BigDecimal;

/**
 * An amount of money (6.5, R-069): a promoted item a `Money` fact names, its value at its PICTURE's scale. A value
 * type only: every store into it is still COBOL's (`Scalars.store`, `Decimal.store`), so what it holds is what the
 * item's bytes would have held.
 */
public record Money(BigDecimal amount) {
    /** Zero at a scale. */
    public static Money zero(int scale) {
        return new Money(BigDecimal.ZERO.setScale(scale));
    }

    /** A stored value as money. */
    public static Money of(Decimal value) {
        return new Money(value.toBigDecimal());
    }

    @Override
    public String toString() {
        return amount.toPlainString();
    }
}
