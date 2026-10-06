package katana.rt;

/**
 * Relation conditions (phase 3.1), as IBM's Language Reference 6.4 gives them (pp. 274-279): numeric
 * operands by algebraic value (length, usage and a zero's sign not significant); alphanumeric operands
 * character by character from the left in the collating sequence, the shorter as if extended on the
 * right with spaces; a zoned integer against an alphanumeric operand as if moved to an alphanumeric
 * item of its digits, unsigned (ZWB, IBM's default); a group by its bytes. Each answer is -1, 0 or 1.
 */
public final class Compare {
    private Compare() {}

    /** Two numeric values, algebraically. */
    // Decision: R-019
    public static int numbers(Decimal a, Decimal b) {
        return Integer.signum(a.toBigDecimal().compareTo(b.toBigDecimal()));
    }

    /** Two alphanumeric operands (or groups) in a collating sequence, the shorter extended with spaces. */
    // Decision: R-019
    public static int text(byte[] a, int aOff, int aLen, byte[] b, int bOff, int bLen, byte space, Collation c) {
        for (int k = 0; k < Math.max(aLen, bLen); k++) {
            byte x = k < aLen ? a[aOff + k] : space, y = k < bLen ? b[bOff + k] : space;
            if (x != y) return Integer.signum(c.weight(x) - c.weight(y));
        }
        return 0;
    }

    /** An alphanumeric operand against a figurative constant or ALL literal: its characters repeated to the operand's length. */
    // Decision: R-019
    public static int all(byte[] a, int aOff, int aLen, byte[] pattern, Collation c) {
        for (int k = 0; k < aLen; k++) {
            byte x = a[aOff + k], y = pattern[k % pattern.length];
            if (x != y) return Integer.signum(c.weight(x) - c.weight(y));
        }
        return 0;
    }

    /** A field as an alphanumeric operand: its bytes, or a zoned integer's unsigned digits (ZWB). */
    // Decision: R-019
    public static byte[] operand(Field f) {
        Attr a = f.attr();
        if (a.category() != Attr.Category.NUMERIC) return java.util.Arrays.copyOfRange(f.record().bytes(), f.offset(), f.offset() + a.length());
        return Move.digits(f);
    }

    /** Two items or literals compared as alphanumeric operands (a zoned integer as its digits), the shorter extended with spaces. */
    // Decision: R-019
    public static int alphanumeric(Field a, Field b, Collation c) {
        byte[] x = operand(a), y = operand(b);
        return text(x, 0, x.length, y, 0, y.length, a.record().codepage().character(' '), c);
    }

    /** An item against a figurative constant or ALL literal's characters. */
    public static int figurative(Field a, byte[] pattern, Collation c) {
        byte[] x = operand(a);
        return all(x, 0, x.length, pattern, c);
    }
}
