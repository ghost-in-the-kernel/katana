package katana.rt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/** ACCEPT held to IBM's text: format 2 to its examples (LR 6.4 p. 310), format 1 to its reading of records (pp. 307-308). */
class AcceptTest {
    private static final Codepage CP = Codepage.COBC_ASCII;

    private static String accepted(String conceptual, LocalDateTime now, Attr into) {
        Field f = new Field(Record.of(into.length(), CP), 0, into);
        Accept.from(f, conceptual, now);
        return new String(f.record().bytes(), StandardCharsets.ISO_8859_1);
    }

    @Test
    void theConceptualItemsAreIbmsExamples() {
        LocalDateTime april27 = LocalDateTime.of(2003, 4, 27, 14, 41, 0);
        assertEquals("030427", accepted("DATE", april27, Attr.zoned(6, 0, false)));
        assertEquals("20030427", accepted("DATE YYYYMMDD", april27, Attr.zoned(8, 0, false)));
        assertEquals("03117", accepted("DAY", april27, Attr.zoned(5, 0, false)));
        assertEquals("2003117", accepted("DAY YYYYDDD", april27, Attr.zoned(7, 0, false)));
        assertEquals("14410000", accepted("TIME", april27, Attr.zoned(8, 0, false)));
        assertEquals("3", accepted("DAY-OF-WEEK", LocalDateTime.of(2003, 4, 23, 0, 0), Attr.zoned(1, 0, false)));
    }

    @Test
    void aConceptualItemIsMovedAsAnUnsignedDisplayInteger() {
        LocalDateTime april27 = LocalDateTime.of(2003, 4, 27, 14, 41, 0);
        assertEquals("030427  ", accepted("DATE", april27, Attr.text(8, false)));
        assertEquals("0427", accepted("DATE", april27, Attr.zoned(4, 0, false)));
    }

    private static String read(String input, String before, int... lengths) {
        Sysin in = new Sysin(new ByteArrayInputStream(input.getBytes(StandardCharsets.ISO_8859_1)));
        StringBuilder out = new StringBuilder();
        for (int n : lengths) {
            Field f = new Field(new Record(before.substring(0, n).getBytes(StandardCharsets.ISO_8859_1), CP), 0, Attr.text(n, false));
            in.accept(f);
            out.append(new String(f.record().bytes(), StandardCharsets.ISO_8859_1)).append('|');
        }
        return out.toString();
    }

    @Test
    void recordsFillTheItemAndTheLastIsCut() {
        assertEquals("ABC|DEFGH|", read("ABC\nDEF\nGHIJ\n", "xxxxxxxx", 3, 5));
        assertEquals("AB|CD|", read("ABCD\nCDEF\n", "xxxx", 2, 2));
    }

    @Test
    void endOfInputPadsAfterDataAndLeavesTheItemBefore() {
        assertEquals("AB   |xxxxx|", read("AB", "xxxxx", 5, 5));
    }
}
