package katana.oracle.compare;

import java.math.BigInteger;

/**
 * Zoned decimal in ASCII, as CardDemo ships it: digits, and in the last byte the sign overpunched as
 * IBM's EBCDIC zones read in ASCII (`{` and `A`-`I` for +0 to +9, `}` and `J`-`R` for -0 to -9). The
 * one reader of it (tithe pass 2, B1): the comparator's and the sort's.
 */
public final class Overpunch {
    private static final String POSITIVE = "{ABCDEFGHI";
    private static final String NEGATIVE = "}JKLMNOPQR";

    private Overpunch() {}

    /** The unscaled value of the bytes, or null when one is neither a digit nor, last, an overpunch. */
    public static BigInteger value(byte[] b) {
        StringBuilder digits = new StringBuilder();
        boolean negative = false;
        for (int i = 0; i < b.length; i++) {
            char c = (char) (b[i] & 0xff);
            boolean last = i == b.length - 1;
            if (c >= '0' && c <= '9') digits.append(c);
            else if (last && POSITIVE.indexOf(c) >= 0) digits.append(POSITIVE.indexOf(c));
            else if (last && NEGATIVE.indexOf(c) >= 0) {
                digits.append(NEGATIVE.indexOf(c));
                negative = true;
            } else {
                return null;
            }
        }
        BigInteger value = new BigInteger(digits.toString());
        return negative ? value.negate() : value;
    }
}
