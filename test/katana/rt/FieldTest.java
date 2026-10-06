package katana.rt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HexFormat;
import org.junit.jupiter.api.Test;

/**
 * The EBCDIC codepage's byte images, which cobc (an ASCII lane) cannot be the oracle for: IBM's
 * preferred signs (docs/PROFILE.md, `ibm.numproc`): zoned digits F0-F9 with the sign in the last
 * digit's zone, C positive and D negative; packed with a sign nibble C, D or F (unsigned); binary
 * big-endian two's complement, COMP-5 too.
 */
class FieldTest {
    private static String put(Attr a, String value) {
        Field f = new Field(Record.of(a.length(), Codepage.EBCDIC_1140), 0, a);
        f.put(Decimal.of(value));
        assertEquals(Decimal.of(value).toBigDecimal().abs().compareTo(f.get().toBigDecimal().abs()), 0, "reads back");
        return HexFormat.of().formatHex(f.record().bytes()).toUpperCase();
    }

    @Test
    void ibmsPreferredSigns() {
        assertEquals("F1F2D3", put(Attr.zoned(3, 0, true), "-123"));
        assertEquals("F1F2C3", put(Attr.zoned(3, 0, true), "123"));
        assertEquals("F1F2F3", put(Attr.zoned(3, 0, false), "123"));
        assertEquals("60F1F2F3", put(new Attr(Attr.Usage.DISPLAY, Attr.Category.NUMERIC, 3, 0, true, true, true, false), "-123"));
        assertEquals("123D", put(Attr.numeric(Attr.Usage.PACKED, 3, 0, true), "-123"));
        assertEquals("123F", put(Attr.numeric(Attr.Usage.PACKED, 3, 0, false), "123"));
        assertEquals("FFFE", put(Attr.numeric(Attr.Usage.BINARY, 4, 0, true), "-2"));
        assertEquals("0102", put(Attr.numeric(Attr.Usage.NATIVE_BINARY, 4, 0, false), "258"));
    }
}
