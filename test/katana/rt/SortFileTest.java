package katana.rt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * A sort file's order (research §23, LR 6.4 pp. 450-452): keys by significance, ASCENDING or DESCENDING,
 * a numeric key by its value, an alphanumeric one by the collating sequence, equal keys in the order
 * released (R-026).
 */
class SortFileTest {
    /** A 4-byte record: a letter key, a signed zoned digit key (overpunched, the cobc lane's form), two tag bytes. */
    private final Record area = Record.of(4, Codepage.COBC_ASCII);
    private final Field record = new Field(area, 0, Attr.text(4, false));
    private final Field letter = new Field(area, 0, Attr.text(1, false));
    private final Field digit = new Field(area, 1, Attr.zoned(1, 0, true));

    private String sorted(List<SortFile.Key> keys, Collation collation, String... records) {
        SortFile sort = new SortFile();
        sort.begin(keys, collation);
        for (String r : records) {
            System.arraycopy(r.getBytes(StandardCharsets.ISO_8859_1), 0, area.bytes(), 0, 4);
            sort.release(record, 4);
        }
        sort.sort();
        StringBuilder out = new StringBuilder();
        while (sort.returnNext(record)) out.append(new String(area.bytes(), 2, 2, StandardCharsets.ISO_8859_1)).append(' ');
        return out.toString().trim();
    }

    @Test
    void equalKeysComeBackInTheOrderReleased() {
        assertEquals("a1 a2 b1 b2", sorted(List.of(new SortFile.Key(letter, false)), Collation.nativeOrder(), "B0b1", "A0a1", "B0b2", "A0a2"),
                "p. 451: a stable sort keeps the release order of equal keys");
    }

    @Test
    void keysAreTakenInOrderOfSignificanceEachInItsDirection() {
        assertEquals("a2 a1 b1", sorted(List.of(new SortFile.Key(letter, false), new SortFile.Key(digit, true)), Collation.nativeOrder(), "A1a1", "B0b1", "A5a2"));
    }

    @Test
    void aNumericKeyComparesByValueNotByItsBytes() {
        // 'q' is -1 in the cobc lane's overpunch, whose byte is above any digit's.
        assertEquals("m1 z0 p1", sorted(List.of(new SortFile.Key(digit, false)), Collation.nativeOrder(), "Aqm1", "A0z0", "A1p1"));
    }

    @Test
    void anAlphanumericKeyFollowsTheCollatingSequence() {
        assertEquals("d1 l1", sorted(List.of(new SortFile.Key(letter, false)), Collation.nativeOrder(), "A0l1", "10d1"), "ASCII: digits before letters");
        assertEquals("l1 d1", sorted(List.of(new SortFile.Key(letter, false)), Collation.of(Codepage.COBC_ASCII, Codepage.EBCDIC_1140.charset), "A0l1", "10d1"), "EBCDIC: letters before digits");
    }
}
