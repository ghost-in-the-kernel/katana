package katana.sir.build;

import static org.junit.jupiter.api.Assertions.assertEquals;

import katana.sir.types.Attributes.Category;
import org.junit.jupiter.api.Test;

/** PICTURE strings read as IBM reads them, each case one cobc's layout confirmed on NIST (sir/datamap.golden). */
class PicturesTest {
    private static String read(String picture) {
        Pictures p = Pictures.read(picture);
        return p.category() + " " + p.digits() + " " + p.scale() + " " + (p.signed() ? "signed" : "unsigned") + " " + p.positions();
    }

    @Test
    void numbersAndText() {
        assertEquals("NUMERIC 7 2 unsigned 7", read("9(5)V99"));
        assertEquals("NUMERIC 4 2 signed 4", read("S99V99"));
        assertEquals("ALPHANUMERIC 0 0 unsigned 10", read("X(10)"));
        assertEquals("ALPHABETIC 0 0 unsigned 3", read("AAA"));
        assertEquals("NATIONAL 0 0 unsigned 4", read("NN"));
    }

    @Test
    void scalingPositions() {
        assertEquals("NUMERIC 5 5 unsigned 2", read("PPP99"));
        assertEquals("NUMERIC 5 -3 unsigned 2", read("99PPP"));
        assertEquals("NUMERIC 1 1 unsigned 0", read("P"));
        assertEquals("NUMERIC 4 4 unsigned 2", read("VPP99"));
    }

    @Test
    void editedPictures() {
        assertEquals("NUMERIC_EDITED 11 2 signed 15", read("-ZZZ,ZZZ,ZZZ.ZZ"));
        assertEquals("NUMERIC_EDITED 6 2 signed 8", read("+++++.++"));
        assertEquals("NUMERIC_EDITED 4 1 signed 10", read("$9,9B9.90+"));
        assertEquals("NUMERIC_EDITED 5 2 signed 8", read("ZZZ.99CR"));
        assertEquals("ALPHANUMERIC_EDITED 0 0 unsigned 5", read("XXBXX"));
    }

    @Test
    void currencyAndDecimalComma() {
        Pictures w = Pictures.read("WWWWW", 'W', false);
        assertEquals(Category.NUMERIC_EDITED, w.category());
        assertEquals(4, w.digits());
        assertEquals(2, Pictures.read("9.999.999,99", '$', true).scale());
    }
}
