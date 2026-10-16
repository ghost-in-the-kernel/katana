package katana.rt;

import java.nio.charset.Charset;

/**
 * How a program's bytes are written (R-019: bytes kept in the program's declared encoding): the
 * characters of alphanumeric data, a zoned digit's byte with its sign, a separate sign, and the order
 * of a native binary's bytes. `COBC_ASCII` is the cobc lane as cobc writes it, observed in its output
 * (J-102): digits `0x30`-`0x39`, a negative zoned sign the digit plus `0x40` (`p`-`y`), a positive one
 * the plain digit, native binary little-endian. Read, a zoned item's sign byte that is an EBCDIC overpunch
 * translated to ASCII byte for byte (`{`, `A`-`I`, `}`, `J`-`R`, IBM's X'C0'-X'C9' and X'D0'-X'D9') is
 * the digit 0 and positive, as cobc -std=ibm reads it, measured over every printable byte (J-183): not
 * IBM's value of the EBCDIC byte it came from, which `EBCDIC_1140` reads. CardDemo's ASCII data is written
 * so; research §22 says what that does to the CardDemo goldens. `EBCDIC_1140` is IBM's: digits `0xF0`-`0xF9`, the sign
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

    /** A zoned byte's digit: its low nibble; in the cobc lane, an EBCDIC overpunch translated to ASCII is 0, as cobc reads it. */
    // Decision: R-019
    public int digitOf(byte b) {
        if (this == COBC_ASCII && (b == '{' || b == '}' || b >= 'A' && b <= 'R')) return 0;
        return b & 0x0F;
    }

    /** Whether a zoned byte carrying a sign says negative: `p`-`y` in the cobc lane (so not `{`, also in zone 7), zone D or B in EBCDIC. */
    // Decision: R-019
    public boolean negative(byte b) {
        int zone = (b >> 4) & 0x0F;
        if (this == COBC_ASCII) return b >= 0x70 && b <= 0x79;
        return zone == 0xD || zone == 0xB;
    }
}
