package katana.rt.autotest;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import katana.rt.Attr;
import katana.rt.Codepage;
import katana.rt.Decimal;
import katana.rt.Display;
import katana.rt.Field;
import katana.rt.Record;

/**
 * A hand-lowered program's surroundings: the cobc lane's codepage, items as the lowering will allocate
 * them, and DISPLAY. DISPLAY's sign form is SEP until the profile carries IBM's DISPSIGN (J-109).
 */
final class Out {
    static final Codepage CP = Codepage.COBC_ASCII;
    // Decision: R-049 (ibm.dispsign; the cobc lane compares under SEP)
    static final Display.Sign SIGN = Display.Sign.SEP;
    private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();

    /** DISPLAY: each operand (a field, or a literal's characters) in order, then a new line. */
    void display(Object... operands) {
        noAdvancing(operands);
        bytes.write('\n');
    }

    /** DISPLAY ... WITH NO ADVANCING: the operands (a field, raw bytes such as a reference modification, or a literal's characters). */
    void noAdvancing(Object... operands) {
        for (Object o : operands) bytes.writeBytes(o instanceof Field f ? Display.of(f, SIGN) : o instanceof byte[] b ? b : o.toString().getBytes(CP.charset));
    }

    /** A numeric-edited item of a picture expanded to its mask (`$` the currency sign, `.` the point). */
    static Field edited(String mask, int scale) {
        int digits = (int) mask.chars().filter(c -> c == '9' || c == 'Z' || c == '*').count();
        for (char f : new char[] {'$', '+', '-'}) {
            long n = mask.chars().filter(c -> c == f).count();
            if (n >= 2) digits += (int) n - 1;
        }
        return item(Attr.numericEdited(new katana.rt.Edit(mask, '$', false, false), digits, scale));
    }

    /** A reference modification's bytes, `(start:length)` from 1. */
    static byte[] ref(Field f, int start, int length) {
        return java.util.Arrays.copyOfRange(f.record().bytes(), f.offset() + start - 1, f.offset() + start - 1 + length);
    }

    /** What was displayed. */
    byte[] bytes() {
        return bytes.toByteArray();
    }

    /** An elementary item of its own, spaces before any VALUE. */
    static Field item(Attr a) {
        return new Field(Record.of(a.length(), CP), 0, a);
    }

    /** A numeric item with a VALUE, fitted as the data map fits it. */
    static Field item(Attr a, String value) {
        Field f = item(a);
        f.put(Decimal.store(Decimal.operand(Decimal.of(value), 18), a.digits(), a.scale(), a.signed(), false).value());
        return f;
    }

    /** An alphanumeric item with a VALUE. */
    static Field text(int length, boolean justified, String value) {
        Field f = item(Attr.text(length, justified));
        byte[] v = value.getBytes(CP.charset);
        System.arraycopy(v, 0, f.record().bytes(), 0, Math.min(v.length, length));
        return f;
    }

    /** A numeric literal as a sending item: its digits, its decimal places, signed when written with a sign. */
    static Field literal(String text) {
        String digits = text.replaceAll("[^0-9]", "");
        int point = text.indexOf('.'), scale = point < 0 ? 0 : text.length() - point - 1;
        return item(Attr.zoned(digits.length(), scale, text.startsWith("-") || text.startsWith("+")), text);
    }

    /** An alphanumeric literal as a sending item. */
    static Field alphanumeric(String text) {
        return text(text.length(), false, text);
    }

    /** A field over part of a record (a group's subordinate item). */
    static Field at(Record r, int offset, Attr a) {
        return new Field(r, offset, a);
    }

    static Attr numeric(Attr.Usage usage, int digits, int scale, boolean signed) {
        return Attr.numeric(usage, digits, scale, signed);
    }

    static BigInteger big(long v) {
        return BigInteger.valueOf(v);
    }
}
