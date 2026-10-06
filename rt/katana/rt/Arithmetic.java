package katana.rt;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * An arithmetic statement's final result stored into a resultant identifier, with the size error
 * condition as IBM's Language Reference 6.4 gives it (p. 296): a size error when the result, after
 * decimal point alignment and any rounding, exceeds what the PICTURE can hold (a binary item's PICTURE
 * too, whatever TRUNC says), or on division by zero. With ON SIZE ERROR the identifier is not altered;
 * without it the truncation rules apply (a native binary cut at its binary size, LR p. 239). Each
 * resultant identifier is stored on its own; the lowering runs the phrase after all of them.
 */
public final class Arithmetic {
    private Arithmetic() {}

    /** Stores `result` into `to`; true when a size error occurred. `onSizeError` whether the statement has the phrase. */
    // Decision: R-019
    // Unsure: a division by zero without ON SIZE ERROR leaves the identifier unaltered here; IBM's p. 296 gives truncation rules no value to apply to, and a mainframe may end the program (J-108).
    public static boolean store(Field to, Decimal.Term result, boolean rounded, boolean onSizeError) {
        Attr a = to.attr();
        Decimal.Stored s = Decimal.store(result, a.digits(), a.scale(), a.signed(), rounded);
        if (result.divisionByZero() || (s.sizeError() && onSizeError)) return s.sizeError();
        if (a.usage() == Attr.Usage.NATIVE_BINARY) {
            BigDecimal aligned = result.value().toBigDecimal().setScale(a.scale(), rounded ? RoundingMode.HALF_UP : RoundingMode.DOWN);
            Move.number(new Decimal(aligned.unscaledValue(), a.scale()), to);
        } else to.put(s.value());
        return s.sizeError();
    }
}
