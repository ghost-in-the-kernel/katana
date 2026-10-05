package katana.oracle.compare;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * A field's bytes as a value, under a declared encoding (`ascii`, `cp037`, `cp1140`): text through the
 * encoding's charset; a zoned number from its digits and the sign in its last byte (EBCDIC zones C, D
 * and F; in ASCII a plain digit, or IBM's overpunch letters `{A-I` and `}J-R`); a packed number from
 * its nibbles (sign C or F positive, D negative); a binary one big-endian. Bytes that are not a valid
 * value of their kind decode to a description of what is wrong, never to zero.
 */
final class Decode {
    private Decode() {}

    static Charset charset(String encoding) {
        return switch (encoding) {
            case "ascii" -> Charset.forName("ISO-8859-1");
            case "cp037" -> Charset.forName("IBM037");
            case "cp1140" -> Charset.forName("IBM01140");
            default -> throw new IllegalArgumentException("encoding " + encoding);
        };
    }

    /** The field's value as text: a string for text, a decimal for numbers, or `invalid ...`. */
    static String value(Layout.Field f, byte[] record, int at, String encoding) {
        byte[] b = Arrays.copyOfRange(record, at, at + f.length());
        return switch (f.kind()) {
            case TEXT -> new String(b, charset(encoding));
            case PACKED -> packed(b, f.scale());
            case BINARY -> new BigDecimal(new BigInteger(b), f.scale()).toPlainString();
            case ZONED -> encoding.equals("ascii") ? zonedAscii(b, f.scale()) : zonedEbcdic(b, f.scale());
        };
    }

    /** Packed decimal: two digits a byte, the last nibble the sign. */
    private static String packed(byte[] b, int scale) {
        StringBuilder digits = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            int high = (b[i] >> 4) & 0xf, low = b[i] & 0xf;
            if (high > 9 || (i < b.length - 1 && low > 9)) return "invalid packed decimal " + hex(b);
            digits.append(high);
            if (i < b.length - 1) digits.append(low);
        }
        int sign = b[b.length - 1] & 0xf;
        if (sign < 0xa) return "invalid packed decimal " + hex(b);
        BigDecimal value = new BigDecimal(new BigInteger(digits.toString()), scale);
        return (sign == 0xd || sign == 0xb ? value.negate() : value).toPlainString();
    }

    /** EBCDIC zoned: F zones, the last byte's zone the sign. */
    private static String zonedEbcdic(byte[] b, int scale) {
        StringBuilder digits = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            int zone = (b[i] >> 4) & 0xf, digit = b[i] & 0xf;
            boolean last = i == b.length - 1;
            if (digit > 9 || (!last && zone != 0xf) || (last && zone < 0xa)) return "invalid zoned decimal " + hex(b);
            digits.append(digit);
        }
        int sign = (b[b.length - 1] >> 4) & 0xf;
        BigDecimal value = new BigDecimal(new BigInteger(digits.toString()), scale);
        return (sign == 0xd || sign == 0xb ? value.negate() : value).toPlainString();
    }

    /** ASCII zoned: digits, the last byte a digit or an overpunch letter (`Overpunch`). */
    private static String zonedAscii(byte[] b, int scale) {
        BigInteger value = Overpunch.value(b);
        return value == null ? "invalid zoned decimal " + hex(b) : new BigDecimal(value, scale).toPlainString();
    }

    static String hex(byte[] b) {
        StringBuilder s = new StringBuilder("X'");
        for (byte x : b) s.append(String.format("%02X", x));
        return s.append("'").toString();
    }
}
