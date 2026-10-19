package katana.rt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * R-068: a promoted item's store leaves what `Arithmetic.store` leaves in the item's bytes, size error and an
 * unchanged receiver included, for each usage, signed or not, rounded or not, with and without ON SIZE ERROR,
 * a division by zero among the results; and a field encoding a promoted value reads back that value.
 */
class ScalarsTest {
    private static final List<Attr> ATTRS = List.of(Attr.numeric(Attr.Usage.DISPLAY, 3, 0, false), Attr.numeric(Attr.Usage.DISPLAY, 5, 2, true),
            Attr.numeric(Attr.Usage.BINARY, 4, 0, true), Attr.numeric(Attr.Usage.PACKED, 7, 2, true), Attr.numeric(Attr.Usage.PACKED, 4, 1, false));
    private static final Codepage CP = Codepage.COBC_ASCII;

    @Test
    void aStoreLeavesWhatTheBytesWouldHold() {
        int cases = 0;
        for (Attr a : ATTRS) for (Decimal.Term t : terms()) for (boolean rounded : new boolean[] {false, true}) for (boolean phrase : new boolean[] {false, true}) {
            Decimal start = new Decimal(BigInteger.valueOf(a.signed() ? -7 : 7), a.scale());
            Field bytes = Scalars.field(a, CP, start);
            boolean expected = Arithmetic.store(bytes, t, rounded, phrase);
            Decimal[] held = {start};
            boolean actual = Scalars.store(t, a.digits(), a.scale(), a.signed(), rounded, phrase, v -> held[0] = v);
            String what = a + " " + t.value() + " rounded=" + rounded + " phrase=" + phrase;
            assertEquals(expected, actual, what + ": the size error");
            assertEquals(bytes.get().toBigDecimal(), held[0].toBigDecimal(), what + ": the value held");
            cases++;
        }
        assertEquals(ATTRS.size() * terms().size() * 4, cases);
    }

    @Test
    void aFieldEncodingTheValueReadsItBack() {
        for (Attr a : ATTRS) for (long u : new long[] {0, 5, 99, a.signed() ? -42 : 42}) {
            Decimal v = new Decimal(BigInteger.valueOf(u), a.scale());
            assertEquals(v.toBigDecimal(), Scalars.field(a, CP, v).get().toBigDecimal(), a + " " + u);
        }
        assertEquals(1234L, Scalars.longOf(Scalars.of(1234L, 0)));
        assertEquals(new java.math.BigDecimal("12.50"), Scalars.bigOf(Scalars.of(new java.math.BigDecimal("12.50"))));
    }

    /** Results that fit, that overflow, that need rounding, that are negative, and a division by zero. */
    private static List<Decimal.Term> terms() {
        ArithProfile p = ArithProfile.IBM_COMPAT;
        Decimal.Term one = Decimal.operand(new Decimal(BigInteger.ONE, 0), 1);
        return List.of(Decimal.operand(new Decimal(BigInteger.valueOf(42), 0), 2), Decimal.operand(new Decimal(BigInteger.valueOf(123456), 0), 6),
                Decimal.operand(new Decimal(BigInteger.valueOf(-3456), 3), 1), Decimal.operand(new Decimal(BigInteger.valueOf(99999), 1), 4),
                Decimal.operand(new Decimal(BigInteger.valueOf(-12345678), 2), 6),
                Decimal.divide(p, one, Decimal.operand(new Decimal(BigInteger.ZERO, 0), 1), 9, true, false),
                Decimal.divide(p, one, Decimal.operand(new Decimal(BigInteger.valueOf(3), 0), 1), 9, true, false));
    }
}
