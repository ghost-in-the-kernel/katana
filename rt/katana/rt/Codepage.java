package katana.rt;

import java.nio.charset.Charset;

/**
 * How a program's bytes are written (R-019: bytes kept in the program's declared encoding): the
 * characters of alphanumeric data, a zoned digit's byte with its sign, a separate sign, and the order
 * of a native binary's bytes. `COBC_ASCII` is the cobc lane as cobc writes it, observed in its output
 * (J-102): digits `0x30`-`0x39`, a negative zoned sign the digit plus `0x40` (`p`-`y`), a positive one
 * the plain digit, native binary little-endian. `EBCDIC_1140` is IBM's: digits `0xF0`-`0xF9`, the sign
 * in the zone nibble (C positive, D negative, F unsigned), native binary big-endian.
 */
public enum Codepage {
    COBC_ASCII(Charset.forName("ISO-8859-1"), 0x30, false),
    EBCDIC_1140(Charset.forName("IBM01140"), 0xF0, true);

    /** The charset of alphanumeric data. */
    public final Charset charset;
    private final int zero;
    /** Whether a native binary (COMP-5) is big-endian. */
    public final boolean nativeBigEndian;

    Codepage(Charset charset, int zero, boolean nativeBigEndian) {
        this.charset = charset;
        this.zero = zero;
        this.nativeBigEndian = nativeBigEndian;
    }

    /** The byte of a character. */
    public byte character(char c) {
        return String.valueOf(c).getBytes(charset)[0];
    }

    /** A zoned digit's byte; `sign` +1 or -1 when this byte carries an overpunched sign, 0 when it does not. */
    // Decision: R-019
    public byte digit(int d, int sign) {
        if (sign == 0) return (byte) (zero + d);
        if (this == COBC_ASCII) return (byte) (sign < 0 ? 0x70 + d : 0x30 + d);
        return (byte) ((sign < 0 ? 0xD0 : 0xC0) + d);
    }

    /** A zoned byte's digit: its low nibble. */
    public int digitOf(byte b) {
        return b & 0x0F;
    }

    /** Whether a zoned byte carrying a sign says negative: zone 7 in the cobc lane, D or B in EBCDIC. */
    public boolean negative(byte b) {
        int zone = (b >> 4) & 0x0F;
        return this == COBC_ASCII ? zone == 0x7 : zone == 0xD || zone == 0xB;
    }
}
