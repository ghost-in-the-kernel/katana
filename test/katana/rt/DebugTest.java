package katana.rt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** `Debug`: DEBUG-ITEM as LR 6.4 pp. 19-20 lay it out and fill it, and what executes a section (research §17, J-176). */
class DebugTest {
    private static String text(Debug d, Codepage cp) {
        return new String(d.item.bytes(), cp.charset);
    }

    @Test
    void debugItemHoldsTheLineTheNameAndTheContentsInPlace() {
        Codepage cp = Codepage.COBC_ASCII;
        Debug d = new Debug(cp, true);
        d.perform(3, 120, false);
        d.entered();
        d.fill("PARA-A");
        String item = text(d, cp);
        assertEquals("000120 PARA-A" + " ".repeat(24) + " " + " ".repeat(18) + "PERFORM LOOP" + " ".repeat(18), item);
        assertEquals(56 + Debug.CONTENTS, item.length());
    }

    @Test
    void aSectionIsExecutedWhenNamedFallenIntoOrStartedNotWhenItsParagraphIsNamed() {
        Debug d = new Debug(Codepage.COBC_ASCII, true);
        d.entered();
        assertTrue(d.section(), "START PROGRAM");
        d.perform(5, 10, false);
        d.entered();
        assertFalse(d.section(), "PERFORM of the section's paragraph (NIST DB105A)");
        d.goTo(11, true);
        d.entered();
        assertTrue(d.section());
        d.fell(6, 6, 12);
        d.entered();
        assertTrue(d.section());
    }

    @Test
    void withoutTheObjectTimeSwitchNothingRuns() {
        System.clearProperty("katana.debug");
        assertFalse(new Debug(Codepage.COBC_ASCII, true).on(), "NODEBUG is IBM's default (LR 6.4 p. 760)");
    }
}
