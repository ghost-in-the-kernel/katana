package katana.rt.autotest;

import katana.rt.ArithProfile;
import katana.rt.Decimal;
import katana.rt.Field;

/**
 * Appendix A's arithmetic as a hand-lowered statement calls it: each operand a term with its places,
 * each operation with the statement's dmax, the last one told it is last (ROUNDED's place, J-099).
 */
final class Calc {
    private static final ArithProfile P = ArithProfile.IBM_COMPAT;
    private final int dmax;

    // Unsure: dmax counts every operand but a direct divisor, those inside a divisor expression too; IBM's "except divisors" (PG Appendix A) does not say which (J-111).
    Calc(int dmax) {
        this.dmax = dmax;
    }

    static Decimal.Term of(Field f) {
        return Decimal.operand(f.get(), Math.max(0, f.attr().digits() - f.attr().scale()));
    }

    /** A numeric literal as an operand: its integer places as written. */
    static Decimal.Term of(String literal) {
        String unsigned = literal.replace("-", "").replace("+", "");
        int point = unsigned.indexOf('.');
        return Decimal.operand(Decimal.of(literal), point < 0 ? unsigned.length() : point);
    }

    Decimal.Term add(Decimal.Term a, Decimal.Term b) {
        return Decimal.add(P, a, b, dmax, false, false);
    }

    Decimal.Term subtract(Decimal.Term a, Decimal.Term b) {
        return Decimal.subtract(P, a, b, dmax, false, false);
    }

    Decimal.Term multiply(Decimal.Term a, Decimal.Term b) {
        return Decimal.multiply(P, a, b, dmax, false, false);
    }

    /** `a / b`. */
    Decimal.Term divide(Decimal.Term a, Decimal.Term b) {
        return Decimal.divide(P, a, b, dmax, false, false);
    }

    Decimal.Term addLast(Decimal.Term a, Decimal.Term b, boolean rounded) {
        return Decimal.add(P, a, b, dmax, true, rounded);
    }

    Decimal.Term subtractLast(Decimal.Term a, Decimal.Term b, boolean rounded) {
        return Decimal.subtract(P, a, b, dmax, true, rounded);
    }

    Decimal.Term multiplyLast(Decimal.Term a, Decimal.Term b, boolean rounded) {
        return Decimal.multiply(P, a, b, dmax, true, rounded);
    }

    Decimal.Term divideLast(Decimal.Term a, Decimal.Term b, boolean rounded) {
        return Decimal.divide(P, a, b, dmax, true, rounded);
    }
}
