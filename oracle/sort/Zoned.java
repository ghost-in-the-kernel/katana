package katana.oracle.sort;

import java.math.BigInteger;

/**
 * A zoned-decimal (ZD) field in the lane's ASCII data, as CardDemo ships it: digits, and in the last
 * byte the sign overpunched as IBM's EBCDIC zones read in ASCII (`{` and `A`-`I` for +0 to +9, `}` and
 * `J`-`R` for -0 to -9). Any other byte is refused with the record and field named, never read as zero.
 */
final class Zoned {
    private static final String POSITIVE = "{ABCDEFGHI";
    private static final String NEGATIVE = "}JKLMNOPQR";

    private Zoned() {}

    /** The field's value: its digits, the last byte's overpunch giving the sign. */
    static BigInteger value(byte[] record, SortCards.Field field) {
        StringBuilder digits = new StringBuilder();
        boolean negative = false;
        int from = field.position() - 1, last = from + field.length() - 1;
        if (last >= record.length) throw new SortCards.Unsupported("ZD field " + field.position() + "," + field.length() + " runs past a " + record.length + "-byte record");
        for (int i = from; i <= last; i++) {
            char c = (char) (record[i] & 0xff);
            if (c >= '0' && c <= '9') digits.append(c);
            else if (i == last && POSITIVE.indexOf(c) >= 0) digits.append(POSITIVE.indexOf(c));
            else if (i == last && NEGATIVE.indexOf(c) >= 0) {
                digits.append(NEGATIVE.indexOf(c));
                negative = true;
            } else {
                throw new SortCards.Unsupported("ZD field " + field.position() + "," + field.length() + " holds '" + c + "', not a digit or a sign overpunch");
            }
        }
        BigInteger value = new BigInteger(digits.toString());
        return negative ? value.negate() : value;
    }
}
