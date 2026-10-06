package katana.rt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;
import org.junit.jupiter.api.Test;

/** `rt/Display` held to IBM's Table 48 (Programming Guide 6.4, DISPSIGN), every row in both modes, in EBCDIC. */
class DisplayTest {
    private static String shown(Attr a, long value, Display.Sign sign) {
        Field f = new Field(Record.of(a.length(), Codepage.EBCDIC_1140), 0, a);
        f.put(new Decimal(BigInteger.valueOf(value), 0));
        return new String(Display.of(f, sign), Codepage.EBCDIC_1140.charset);
    }

    private static void row(Attr a, long value, String compat, String sep) {
        assertEquals(compat, shown(a, value, Display.Sign.COMPAT), a + " " + value + " COMPAT");
        assertEquals(sep, shown(a, value, Display.Sign.SEP), a + " " + value + " SEP");
    }

    @Test
    void table48() {
        row(Attr.numeric(Attr.Usage.BINARY, 3, 0, false), 111, "111", "111");
        row(Attr.numeric(Attr.Usage.BINARY, 3, 0, true), 111, "111", "+111");
        row(Attr.numeric(Attr.Usage.BINARY, 3, 0, true), -111, "11J", "-111");
        row(Attr.numeric(Attr.Usage.PACKED, 3, 0, false), 222, "222", "222");
        row(Attr.numeric(Attr.Usage.PACKED, 3, 0, true), 222, "222", "+222");
        row(Attr.numeric(Attr.Usage.PACKED, 3, 0, true), -222, "22K", "-222");
        row(Attr.zoned(3, 0, false), 333, "333", "333");
        row(Attr.zoned(3, 0, true), 333, "33C", "+333");
        row(Attr.zoned(3, 0, true), -333, "33L", "-333");
        Attr leading = new Attr(Attr.Usage.DISPLAY, Attr.Category.NUMERIC, 3, 0, true, false, true, false, null);
        row(leading, 333, "C33", "+333");
        row(leading, -333, "L33", "-333");
    }
}
