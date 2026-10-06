package katana.rt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

/** `rt/Strings` held to what IBM's text states in words (LR 6.4 pp. 457-471); its examples are figures. */
class StringsTest {
    private static byte[] b(String s) {
        return s.getBytes(StandardCharsets.ISO_8859_1);
    }

    private static Field text(int n) {
        return new Field(Record.of(n, Codepage.COBC_ASCII), 0, Attr.text(n, false));
    }

    private static String s(Field f) {
        return new String(f.record().bytes(), StandardCharsets.ISO_8859_1);
    }

    /** "An occurrence of ABC is considered an occurrence of AB" (p. 467), with DELIMITED BY "AB" OR "BC". */
    @Test
    void theFirstDelimiterWrittenWins() {
        Field r1 = text(3), r2 = text(3), d1 = text(2);
        Strings.Unstrung u = Strings.unstring(b("xABCy"), 0, 5, List.of(new Strings.Delimiter(b("AB"), false), new Strings.Delimiter(b("BC"), false)),
                List.of(new Strings.Receiver(r1, d1), new Strings.Receiver(r2, null)), 1, 0);
        assertEquals("x  ", s(r1));
        assertEquals("AB", s(d1));
        assertEquals("Cy ", s(r2));
        assertEquals(new Strings.Result(6, 2, false), u.result());
    }

    /** "Only that part of the receiving field into which data was transferred is changed" and the pointer one past the last character (p. 461). */
    @Test
    void stringChangesOnlyWhatItTransfers() {
        byte[] into = b("########");
        Strings.Result r = Strings.string(List.of(new Strings.Sender(b("AB,CD"), b(",")), new Strings.Sender(b("EF"), null)), into, 0, 8, 2);
        assertEquals("#ABEF###", new String(into, StandardCharsets.ISO_8859_1));
        assertEquals(new Strings.Result(6, 0, false), r);
    }

    /** Overflow: a pointer less than 1, and a receiver filled with data left (p. 460). */
    @Test
    void stringOverflows() {
        byte[] into = b("###");
        assertEquals(true, Strings.string(List.of(new Strings.Sender(b("AB"), null)), into, 0, 3, 0).overflow());
        assertEquals(true, Strings.string(List.of(new Strings.Sender(b("ABCD"), null)), into, 0, 3, 1).overflow());
        assertEquals("ABC", new String(into, StandardCharsets.ISO_8859_1));
    }

    /** "All data receiving fields have been acted upon and the sending field still contains unexamined character positions" (p. 468). */
    @Test
    void unstringOverflowsWhenReceiversRunOut() {
        Field r1 = text(2);
        Strings.Unstrung u = Strings.unstring(b("AB,CD"), 0, 5, List.of(new Strings.Delimiter(b(","), false)), List.of(new Strings.Receiver(r1, null)), 1, 0);
        assertEquals(new Strings.Result(4, 1, true), u.result());
        assertEquals(2, u.counts()[0]);
    }
}
