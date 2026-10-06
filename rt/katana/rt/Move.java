package katana.rt;

import java.util.Arrays;

/**
 * MOVE between fields (phase 3.1), as IBM's Language Reference 6.4 gives it (pp. 174, 402-405):
 * into a numeric item, aligned on the decimal point and truncated or zero-filled at either end, the
 * sender's sign kept by a signed receiver and dropped by an unsigned one; into an alphanumeric item,
 * left-aligned and space-filled or truncated on the right (JUSTIFIED: on the left); a numeric integer
 * sender into an alphanumeric receiver as its unsigned digits, a separate sign not moved; an
 * alphanumeric sender into a numeric receiver as an unsigned integer; a group move a byte copy with no
 * conversion; into an edited item, the value or characters edited by its PICTURE (`Editor`); from a
 * numeric-edited item into a numeric or numeric-edited one, its value de-edited first (LR p. 404).
 */
public final class Move {
    private Move() {}

    /** An elementary MOVE from one field to another, by their categories. */
    // Decision: R-019
    public static void move(Field from, Field to) {
        Attr.Category fc = from.attr().category(), tc = to.attr().category();
        boolean fromNumeric = fc == Attr.Category.NUMERIC, fromEdited = fc == Attr.Category.NUMERIC_EDITED;
        boolean toNumeric = tc == Attr.Category.NUMERIC || tc == Attr.Category.NUMERIC_EDITED;
        if (toNumeric) {
            Decimal value = fromNumeric ? from.get() : fromEdited ? deEdited(from) : unsignedInteger(from);
            if (tc == Attr.Category.NUMERIC) number(value, to);
            else write(Editor.number(to.attr().edit(), to.attr().scale(), value.toBigDecimal()), to);
        } else if (tc == Attr.Category.ALPHANUMERIC_EDITED) {
            Codepage cp = from.record().codepage();
            write(Editor.text(to.attr().edit(), new String(fromNumeric ? digits(from) : bytes(from), cp.charset)), to);
        } else text(fromNumeric ? digits(from) : bytes(from), to);
    }

    /** A group MOVE: the sender's bytes into the receiver's, left-aligned, space-filled, with no conversion. */
    // Decision: R-019
    public static void group(Record from, int fromOffset, int fromLength, Record to, int toOffset, int toLength) {
        byte[] src = Arrays.copyOfRange(from.bytes(), fromOffset, fromOffset + fromLength);
        place(src, to.bytes(), toOffset, toLength, false, to.codepage().character(' '));
    }

    /**
     * A value into a numeric field: aligned on the decimal point, high-order digits beyond the receiver
     * lost; a native binary (COMP-5) is cut at its binary size, not its PICTURE (LR p. 239: "truncation
     * occurs at the binary field size rather than at the COBOL picture size limit").
     */
    static void number(Decimal value, Field to) {
        Attr a = to.attr();
        if (a.usage() == Attr.Usage.NATIVE_BINARY) {
            java.math.BigInteger aligned = value.toBigDecimal().setScale(a.scale(), java.math.RoundingMode.DOWN).unscaledValue();
            if (!a.signed()) aligned = aligned.abs();
            int bits = 8 * a.length();
            java.math.BigInteger wrapped = aligned.mod(java.math.BigInteger.ONE.shiftLeft(bits));
            if (a.signed() && wrapped.testBit(bits - 1)) wrapped = wrapped.subtract(java.math.BigInteger.ONE.shiftLeft(bits));
            to.put(new Decimal(wrapped, a.scale()));
            return;
        }
        Decimal.Stored s = Decimal.store(Decimal.operand(value, value.unscaled().abs().toString().length()), a.digits(), a.scale(), a.signed(), false);
        to.put(s.value());
    }

    /** Characters into an alphanumeric field, aligned and filled as JUSTIFIED says. */
    private static void text(byte[] src, Field to) {
        place(src, to.record().bytes(), to.offset(), to.attr().length(), to.attr().justified(), to.record().codepage().character(' '));
    }

    private static void place(byte[] src, byte[] dst, int at, int length, boolean right, byte space) {
        Arrays.fill(dst, at, at + length, space);
        int n = Math.min(src.length, length);
        if (right) System.arraycopy(src, src.length - n, dst, at + length - n, n);
        else System.arraycopy(src, 0, dst, at, n);
    }

    /** An edited image into a field, in its codepage. */
    private static void write(String shown, Field to) {
        byte[] b = shown.getBytes(to.record().codepage().charset);
        System.arraycopy(b, 0, to.record().bytes(), to.offset(), to.attr().length());
    }

    /** A numeric-edited field's value, de-edited. */
    private static Decimal deEdited(Field f) {
        String shown = new String(bytes(f), f.record().codepage().charset);
        java.math.BigDecimal v = Editor.deEdit(f.attr().edit(), f.attr().scale(), shown);
        return new Decimal(v.unscaledValue(), v.scale());
    }

    /** A field's own bytes. */
    private static byte[] bytes(Field f) {
        return Arrays.copyOfRange(f.record().bytes(), f.offset(), f.offset() + f.attr().length());
    }

    /** A numeric integer field as the unsigned digits of its PICTURE, in its codepage, each P a zero (LR p. 404, usage note 1). */
    static byte[] digits(Field f) {
        int width = f.attr().digits() - Math.min(0, f.attr().scale());
        String digits = String.format("%0" + width + "d", f.get().toBigDecimal().abs().toBigInteger());
        byte[] out = new byte[width];
        for (int k = 0; k < out.length; k++) out[k] = f.record().codepage().digit(digits.charAt(k) - '0', 0);
        return out;
    }

    /** An alphanumeric field read as an unsigned integer: each character's digit nibble. */
    private static Decimal unsignedInteger(Field f) {
        java.math.BigInteger v = java.math.BigInteger.ZERO;
        for (byte b : bytes(f)) v = v.multiply(java.math.BigInteger.TEN).add(java.math.BigInteger.valueOf(f.record().codepage().digitOf(b)));
        return new Decimal(v, 0);
    }
}
