package katana.rt;

import java.math.BigInteger;

/**
 * A data item (R-019): a view of a record, at an offset, of its attributes' length. A numeric field
 * reads to and writes from a `Decimal` at its own scale, through its usage's codec: zoned digits with an
 * overpunched or separate sign, packed nibbles with a sign nibble (C positive, D negative, F unsigned),
 * or a two's-complement binary (native order for COMP-5). Writing takes a value `Decimal.store` has
 * already fitted to the field; the codec only encodes.
 */
public record Field(Record record, int offset, Attr attr) {
    /** The field's value. */
    // Decision: R-019
    public Decimal get() {
        byte[] b = record.bytes();
        BigInteger unscaled = switch (attr.usage()) {
            case DISPLAY -> zonedGet(b);
            case PACKED -> packedGet(b);
            case BINARY -> binaryGet(b, true);
            case NATIVE_BINARY -> binaryGet(b, record.codepage().nativeBigEndian);
        };
        return new Decimal(unscaled, attr.scale());
    }

    /** Writes a value already fitted to the field (its scale, its digits). */
    // Decision: R-019
    public void put(Decimal value) {
        BigInteger unscaled = value.unscaled();
        if (!attr.signed()) unscaled = unscaled.abs();
        switch (attr.usage()) {
            case DISPLAY -> zonedPut(unscaled);
            case PACKED -> packedPut(unscaled);
            case BINARY -> binaryPut(unscaled, true);
            case NATIVE_BINARY -> binaryPut(unscaled, record.codepage().nativeBigEndian);
        }
    }

    /** Where the digits start, and where a separate sign is, in a zoned field. */
    private int digitsAt() {
        return offset + (attr.signed() && attr.signSeparate() && attr.signLeading() ? 1 : 0);
    }

    /** A zoned item's value: its digits, signed by the separate sign byte or the overpunched digit, as its SIGN clause places them. */
    private BigInteger zonedGet(byte[] b) {
        Codepage cp = record.codepage();
        int at = digitsAt();
        BigInteger v = BigInteger.ZERO;
        for (int k = 0; k < attr.digits(); k++) v = v.multiply(BigInteger.TEN).add(BigInteger.valueOf(cp.digitOf(b[at + k])));
        boolean negative = false;
        if (attr.signed() && attr.signSeparate()) negative = b[attr.signLeading() ? offset : at + attr.digits()] == cp.character('-');
        else if (attr.signed()) negative = cp.negative(b[attr.signLeading() ? at : at + attr.digits() - 1]);
        return negative ? v.negate() : v;
    }

    /**
     * A value stored as zoned digits, its sign separate or overpunched on the leading or trailing digit, as the
     * item's SIGN clause says. An item whose PICTURE is P alone has no digit stored, its value always zero
     * (LR 6.4 p. 210: "zero in place of the digit position specified by the symbol P"), so nothing is written
     * (NIST IX110A's STATUS-TEST-10).
     */
    private void zonedPut(BigInteger unscaled) {
        if (attr.digits() == 0) return;
        Codepage cp = record.codepage();
        byte[] b = record.bytes();
        String digits = String.format("%0" + attr.digits() + "d", unscaled.abs());
        int at = digitsAt(), sign = unscaled.signum() < 0 ? -1 : 1;
        boolean overpunch = attr.signed() && !attr.signSeparate();
        int signAt = attr.signLeading() ? 0 : attr.digits() - 1;
        for (int k = 0; k < attr.digits(); k++) b[at + k] = cp.digit(digits.charAt(k) - '0', overpunch && k == signAt ? sign : 0);
        if (attr.signed() && attr.signSeparate()) b[attr.signLeading() ? offset : at + attr.digits()] = cp.character(sign < 0 ? '-' : '+');
    }

    private BigInteger packedGet(byte[] b) {
        int n = attr.length();
        BigInteger v = BigInteger.ZERO;
        for (int k = 0; k < n; k++) {
            int high = (b[offset + k] >> 4) & 0xF, low = b[offset + k] & 0xF;
            v = v.multiply(BigInteger.TEN).add(BigInteger.valueOf(high));
            if (k < n - 1) v = v.multiply(BigInteger.TEN).add(BigInteger.valueOf(low));
        }
        int sign = b[offset + n - 1] & 0xF;
        return sign == 0xD || sign == 0xB ? v.negate() : v;
    }

    private void packedPut(BigInteger unscaled) {
        int n = attr.length();
        String digits = String.format("%0" + (2 * n - 1) + "d", unscaled.abs());
        int sign = !attr.signed() ? 0xF : unscaled.signum() < 0 ? 0xD : 0xC;
        byte[] b = record.bytes();
        for (int k = 0; k < n; k++) {
            int high = digits.charAt(2 * k) - '0', low = k < n - 1 ? digits.charAt(2 * k + 1) - '0' : sign;
            b[offset + k] = (byte) (high << 4 | low);
        }
    }

    private BigInteger binaryGet(byte[] b, boolean bigEndian) {
        int n = attr.length();
        byte[] be = new byte[n];
        for (int k = 0; k < n; k++) be[k] = b[offset + (bigEndian ? k : n - 1 - k)];
        return attr.signed() ? new BigInteger(be) : new BigInteger(1, be);
    }

    private void binaryPut(BigInteger unscaled, boolean bigEndian) {
        int n = attr.length();
        byte[] two = unscaled.toByteArray(), b = record.bytes();
        byte fill = (byte) (unscaled.signum() < 0 ? 0xFF : 0x00);
        for (int k = 0; k < n; k++) {
            int from = two.length - n + k;
            byte v = from >= 0 ? two[from] : fill;
            b[offset + (bigEndian ? k : n - 1 - k)] = v;
        }
    }
}
