package katana.rt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/**
 * `rt/Editor` held to IBM's own examples (LR 6.4 pp. 220-225), each picture, value and edited result
 * as printed there; the PDF's text drops leading and trailing spaces, so each result is written here at
 * the picture's length. A mask is the picture expanded, V and S left out; scale its digits after the point.
 */
class EditorTest {
    private static String edit(String mask, int scale, String value) {
        return Editor.number(new Edit(mask, '$', false, false), scale, new BigDecimal(value));
    }

    @Test
    void simpleInsertion() {
        assertEquals("01, 234, 000", edit("99,B999,B000", 0, "1234"));
        assertEquals("12,345", edit("99,999", 0, "12345"));
        assertEquals("ALPHANUMER/01", Editor.text(new Edit("XXXXXXXXXX/XX", '$', false, false), "ALPHANUMER01"));
        assertEquals("ALPHA NUMERIC", Editor.text(new Edit("XXXXXBXXXXXXX", '$', false, false), "ALPHANUMERIC"));
    }

    @Test
    void specialInsertion() {
        assertEquals("001.23", edit("999.99", 2, "1.234"));
        assertEquals("012.34", edit("999.99", 2, "12.34"));
        assertEquals("123.45", edit("999.99", 2, "123.45"));
        assertEquals("234.50", edit("999.99", 2, "1234.5"));
    }

    @Test
    void fixedInsertion() {
        assertEquals("555.55+", edit("999.99+", 2, "6555.556"));
        assertEquals("-6555.55", edit("+9999.99", 2, "-6555.555"));
        assertEquals("1234.56", edit("9999.99", 2, "1234.56"));
        assertEquals("$123.45", edit("$999.99", 2, "-123.45"));
        assertEquals("-$123.45", edit("-$999.99", 2, "-123.456"));
        assertEquals(" $123.45", edit("-$999.99", 2, "123.456"));
        assertEquals("$0123.45  ", edit("$9999.99CR", 2, "123.45"));
        assertEquals("$0123.45CR", edit("$9999.99CR", 2, "-123.45"));
    }

    @Test
    void floatingInsertion() {
        assertEquals("   $.12", edit("$$$$.99", 2, ".123"));
        assertEquals("  $0.12", edit("$$$9.99", 2, ".12"));
        assertEquals("   $1,234.56", edit("$,$$$,999.99", 2, "-1234.56"));
        assertEquals(" -123,456.78", edit("+,+++,999.99", 2, "-123456.789"));
        assertEquals("$1,234,567.00CR", edit("$$,$$$,$$$.99CR", 2, "-1234567"));
        assertEquals("              ", edit("++,+++,+++.+++", 3, "0"));
    }

    @Test
    void zeroSuppression() {
        assertEquals("****.**", edit("****.**", 2, "0"));
        assertEquals("       ", edit("ZZZZ.ZZ", 2, "0"));
        assertEquals("    .00", edit("ZZZZ.99", 2, "0"));
        assertEquals("****.00", edit("****.99", 2, "0"));
        assertEquals("  00.00", edit("ZZ99.99", 2, "0"));
        assertEquals("  123.45+", edit("Z,ZZZ.ZZ+", 2, "123.456"));
        assertEquals("**123.45-", edit("*,***.**+", 2, "-123.45"));
        assertEquals("12,345,678.90+", edit("**,***,***.**+", 2, "12345678.9"));
        assertEquals("$   12,345.67  ", edit("$Z,ZZZ,ZZZ.ZZCR", 2, "12345.67"));
    }

    @Test
    void blankWhenZeroAndTheProgramsSymbols() {
        assertEquals("      ", Editor.number(new Edit("999.99", '$', false, true), 2, BigDecimal.ZERO));
        assertEquals("£1.234,50", Editor.number(new Edit("$9,999.99", '£', true, false), 2, new BigDecimal("1234.5")));
    }

    @Test
    void deEditingReadsTheValueBack() {
        Edit e = new Edit("$$,$$$,$$$.99CR", '$', false, false);
        assertEquals(new BigDecimal("-1234567.00"), Editor.deEdit(e, 2, edit(e.mask(), 2, "-1234567")));
        assertEquals(new BigDecimal("-123.45"), Editor.deEdit(new Edit("*,***.**+", '$', false, false), 2, "**123.45-"));
        assertEquals(new BigDecimal("0.12"), Editor.deEdit(new Edit("$$$$.99", '$', false, false), 2, "   $.12"));
    }
}
