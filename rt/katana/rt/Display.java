package katana.rt;

import java.util.Arrays;

/**
 * An item as the DISPLAY statement writes it (phase 3.1), IBM's Language Reference 6.4 (p. 333) and
 * Programming Guide 6.4 (DISPSIGN, Table 48): binary and packed items converted to zoned decimal of
 * their PICTURE's digits; under DISPSIGN(COMPAT), IBM's default, a negative value's last digit
 * overpunched and a zoned item's bytes as they are; under DISPSIGN(SEP) every signed binary, packed or
 * overpunched zoned item with a leading separate sign. No decimal point is shown. Any other item is
 * its bytes.
 */
public final class Display {
    private Display() {}

    /** IBM's DISPSIGN option. */
    public enum Sign { COMPAT, SEP }

    /** The bytes DISPLAY writes for a field, in its codepage. */
    // Decision: R-019
    public static byte[] of(Field f, Sign sign) {
        Attr a = f.attr();
        byte[] raw = Arrays.copyOfRange(f.record().bytes(), f.offset(), f.offset() + a.length());
        if (a.category() != Attr.Category.NUMERIC) return raw;
        boolean zoned = a.usage() == Attr.Usage.DISPLAY;
        if (zoned && (sign == Sign.COMPAT || !a.signed() || a.signSeparate())) return raw;
        Codepage cp = f.record().codepage();
        Decimal v = f.get();
        String digits = String.format("%0" + a.digits() + "d", v.unscaled().abs());
        boolean negative = v.unscaled().signum() < 0;
        int lead = sign == Sign.SEP && a.signed() ? 1 : 0;
        byte[] out = new byte[lead + a.digits()];
        if (lead == 1) out[0] = cp.character(negative ? '-' : '+');
        for (int k = 0; k < a.digits(); k++) out[lead + k] = cp.digit(digits.charAt(k) - '0', 0);
        if (lead == 0 && negative) out[out.length - 1] = cp.digit(digits.charAt(a.digits() - 1) - '0', -1);
        return out;
    }
}
