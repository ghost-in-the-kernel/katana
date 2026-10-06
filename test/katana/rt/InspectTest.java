package katana.rt;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

/** `rt/Inspect` held to IBM's own INSPECT examples (LR 6.4 pp. 356-360), each as printed there. */
class InspectTest {
    private static byte[] b(String s) {
        return s == null ? null : s.getBytes(StandardCharsets.ISO_8859_1);
    }

    private static Inspect.Phrase tally(Inspect.Kind kind, String subject, String before, String after) {
        return new Inspect.Phrase(kind, b(subject), null, b(before), b(after));
    }

    private static Inspect.Phrase replace(Inspect.Kind kind, String subject, String by, String before, String after) {
        return new Inspect.Phrase(kind, b(subject), b(by), b(before), b(after));
    }

    @Test
    void tallyingSpaces() {
        byte[] chars = b("In order to form a");
        assertArrayEquals(new long[] {4}, Inspect.tally(chars, 0, chars.length, List.of(tally(Inspect.Kind.ALL, " ", null, null))));
    }

    @Test
    void tallyingAndReplacingCommas() {
        byte[] chars = b("more,perfect,union");
        assertArrayEquals(new long[] {2}, Inspect.tally(chars, 0, chars.length, List.of(tally(Inspect.Kind.ALL, ",", null, null))));
        Inspect.replace(chars, 0, chars.length, List.of(replace(Inspect.Kind.ALL, ",", " ", null, null)));
        assertEquals("more perfect union", new String(chars, StandardCharsets.ISO_8859_1));
    }

    @Test
    void beforeAndAfter() {
        byte[] chars = b("$some.confusing_text with.hyphens-periods.spaces" + "  ");
        long[] counts = Inspect.tally(chars, 0, chars.length, List.of(
                tally(Inspect.Kind.CHARACTERS, null, "_", "$"),
                tally(Inspect.Kind.ALL, ".", null, "h"),
                tally(Inspect.Kind.ALL, ".", "a", null)));
        assertArrayEquals(new long[] {14, 2, 0}, counts);
        Inspect.replace(chars, 0, chars.length, List.of(
                replace(Inspect.Kind.ALL, ".", " ", "h", null),
                replace(Inspect.Kind.ALL, "\"", " ", null, "s"),
                replace(Inspect.Kind.ALL, "_", " ", null, null)));
        assertEquals("$some confusing text with.hyphens-periods.spaces  ", new String(chars, StandardCharsets.ISO_8859_1));
    }

    @Test
    void converting() {
        String[][] table = {{"a/five/?six", "a/FIVE/?six"}, {"r/Rexx/RRRr", "r/REXX/RRRR"}, {"zfour?inspe", "zfour?inspe"}};
        for (String[] row : table) {
            byte[] data = b(row[0]);
            Inspect.convert(data, 0, data.length, b("abcdefghijklmnopqrstuvwxyz"), b("ABCDEFGHIJKLMNOPQRSTUVWXYZ"), b("?"), b("/"));
            assertEquals(row[1], new String(data, StandardCharsets.ISO_8859_1), row[0]);
        }
    }
}
