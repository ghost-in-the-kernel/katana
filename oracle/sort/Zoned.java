package katana.oracle.sort;

import java.math.BigInteger;
import katana.oracle.compare.Overpunch;

/**
 * A zoned-decimal (ZD) field in the lane's ASCII data, as CardDemo ships it: digits, and in the last
 * byte the sign overpunched as IBM's EBCDIC zones read in ASCII (`{` and `A`-`I` for +0 to +9, `}` and
 * `J`-`R` for -0 to -9). Any other byte is refused with the record and field named, never read as zero.
 */
final class Zoned {
    private Zoned() {}

    /** The field's value: its digits, the last byte's overpunch giving the sign (`Overpunch`, the one reader of it). */
    static BigInteger value(byte[] record, SortCards.Field field) {
        int from = field.position() - 1, last = from + field.length() - 1;
        if (last >= record.length) throw new SortCards.Unsupported("ZD field " + field.position() + "," + field.length() + " runs past a " + record.length + "-byte record");
        BigInteger value = Overpunch.value(java.util.Arrays.copyOfRange(record, from, last + 1));
        if (value == null) throw new SortCards.Unsupported("ZD field " + field.position() + "," + field.length() + " holds a byte that is not a digit or a sign overpunch");
        return value;
    }
}
