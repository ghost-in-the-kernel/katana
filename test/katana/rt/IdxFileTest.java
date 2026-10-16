package katana.rt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * An indexed file's statements and statuses as IBM's text gives them (research §21, LR 6.4 pp. 146-152,
 * 300-301, 428, 433, 455-456, 475-477): records by prime key in binary order, an alternate key with
 * DUPLICATES in the order written, the key of reference, START's comparison cut to the shorter length, and
 * the attributes kept with the file for status 39.
 */
class IdxFileTest {
    @TempDir
    Path dir;

    /** A 6-byte record: the prime key in bytes 0-1, an alternate key with DUPLICATES in 2-3, two bytes more. */
    private final Record area = Record.of(6, Codepage.COBC_ASCII);
    private final Field record = new Field(area, 0, Attr.text(6, false));
    private final Field prime = new Field(area, 0, Attr.text(2, false));
    private final Field alternate = new Field(area, 2, Attr.text(2, false));
    private final Field alternateLead = new Field(area, 2, Attr.text(1, false));

    private IdxFile file(KeyedFile.Access access, boolean duplicates) {
        return new IdxFile(dir.resolve("IDX").toString(), "IDX-FILE", true, false, access).keys(record, 6, new Field[] {prime, alternate}, new boolean[] {false, duplicates});
    }

    private void put(String text) {
        System.arraycopy(text.getBytes(StandardCharsets.ISO_8859_1), 0, area.bytes(), 0, text.length());
    }

    private String text() {
        return new String(area.bytes(), StandardCharsets.ISO_8859_1);
    }

    /** Records written out of key order with random access: B2 and D2 share the alternate key "X1", written D2 first. */
    private void written() {
        IdxFile out = file(KeyedFile.Access.RANDOM, true);
        out.open(SeqFile.Mode.OUTPUT);
        for (String r : new String[] {"C3Y1c3", "D2X1d2", "A1Z1a1", "B2X1b2"}) {
            put(r);
            out.write(record);
        }
        assertEquals("02", out.status(), "p. 300: the record just written made a duplicate alternate key");
        put("A1Q1zz");
        out.write(record);
        assertEquals("22", out.status(), "p. 301: a duplicate prime key");
        out.close(false, false, false);
    }

    private String all(IdxFile f) {
        StringBuilder got = new StringBuilder();
        for (f.readNext(record); f.successful(); f.readNext(record)) got.append(text(), 0, 2).append(f.status().equals("02") ? "*" : "").append(' ');
        return got.toString();
    }

    @Test
    void aSequentialReadFollowsThePrimeKeyAndAnAlternateKeyAfterAStart() {
        written();
        IdxFile f = file(KeyedFile.Access.DYNAMIC, true);
        f.open(SeqFile.Mode.INPUT);
        assertEquals("A1 B2 C3 D2 ", all(f), "p. 150: ascending prime key");
        f.close(false, false, false);
        f.open(SeqFile.Mode.INPUT);
        put("  X1  ");
        f.start(alternate, KeyedFile.Relation.NOT_LESS);
        assertEquals("D2* B2 C3 A1 ", all(f), "p. 428: duplicates in the order written, 02 while the next has the same value");
    }

    @Test
    void aRandomReadByAnAlternateKeyFindsTheFirstWrittenAndMakesItTheKeyOfReference() {
        written();
        IdxFile f = file(KeyedFile.Access.DYNAMIC, true);
        f.open(SeqFile.Mode.INPUT);
        put("  X1  ");
        f.read(record, alternate);
        assertEquals("D2X1d2", text(), "p. 150: only the first record written is available");
        assertEquals("02", f.status());
        f.readNext(record);
        assertEquals("B2", text().substring(0, 2), "the alternate key stays the key of reference; the file points past D2");
        put("Q9    ");
        f.read(record, prime);
        assertEquals("23", f.status());
    }

    @Test
    void startComparesTheKeyCutToTheItemsLength() {
        written();
        IdxFile f = file(KeyedFile.Access.DYNAMIC, true);
        f.open(SeqFile.Mode.INPUT);
        put("  Y   ");
        f.start(alternateLead, KeyedFile.Relation.EQUAL);
        f.readNext(record);
        assertEquals("C3Y1c3", text(), "p. 456: the key cut to the item's one byte, Y");
        put("  Z   ");
        f.start(alternateLead, KeyedFile.Relation.GREATER);
        assertEquals("23", f.status());
    }

    @Test
    void aSequentialFileIsWrittenInAscendingOrderAndRewrittenAtTheKeyLastRead() {
        IdxFile out = file(KeyedFile.Access.SEQUENTIAL, true);
        out.open(SeqFile.Mode.OUTPUT);
        put("B2X1b2");
        out.write(record);
        put("A1Z1a1");
        out.write(record);
        assertEquals("21", out.status(), "p. 475: a prime key not above the last written");
        out.close(false, false, false);
        IdxFile io = file(KeyedFile.Access.SEQUENTIAL, true);
        io.open(SeqFile.Mode.IO);
        io.delete();
        assertEquals("43", io.status(), "no READ before the DELETE");
        io.readNext(record);
        put("C3X1zz");
        io.rewrite(record);
        assertEquals("21", io.status(), "p. 301: the prime key changed since the READ");
    }

    @Test
    void anAlternateKeyWithoutDuplicatesRefusesARepeat() {
        IdxFile f = file(KeyedFile.Access.RANDOM, false);
        f.open(SeqFile.Mode.OUTPUT);
        put("A1X1a1");
        f.write(record);
        put("B2X1b2");
        f.write(record);
        assertEquals("22", f.status(), "p. 301: a duplicate alternate key without DUPLICATES");
    }

    @Test
    void aFileWithOtherKeysIsAConflictOfAttributes() {
        written();
        IdxFile other = new IdxFile(dir.resolve("IDX").toString(), "IDX-FILE", true, false, KeyedFile.Access.RANDOM).keys(record, 6, new Field[] {prime}, new boolean[] {false});
        other.open(SeqFile.Mode.INPUT);
        assertEquals("39", other.status(), "p. 301: the alternate record keys differ");
    }
}
