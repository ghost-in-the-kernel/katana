package katana.rt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * A relative file's statements and statuses as IBM's text gives them (research §20, LR 6.4 pp. 300-304,
 * 332, 410-412, 427-428, 434, 456, 475-478), and its file position indicator as the 85 Standard reads it:
 * at a record after OPEN or START, past one after a READ.
 */
class RelFileTest {
    @TempDir
    Path dir;

    private final Field record = new Field(Record.of(4, Codepage.COBC_ASCII), 0, Attr.text(4, false));
    private final Field key = new Field(Record.of(2, Codepage.COBC_ASCII), 0, Attr.zoned(2, 0, false));

    private RelFile file(RelFile.Access access) {
        return new RelFile(dir.resolve("REL").toString(), "REL-FILE", true, false, access);
    }

    private void put(Field f, String text) {
        byte[] b = text.getBytes(StandardCharsets.ISO_8859_1);
        System.arraycopy(b, 0, f.record().bytes(), f.offset(), b.length);
    }

    private void key(long n) {
        key.put(new Decimal(java.math.BigInteger.valueOf(n), 0));
    }

    private String text(Field f) {
        return new String(f.record().bytes(), f.offset(), f.attr().length(), StandardCharsets.ISO_8859_1);
    }

    /** Records 2, 3 and 5 written at their keys with random access, then closed. */
    private void written() {
        RelFile out = file(RelFile.Access.RANDOM);
        out.open(SeqFile.Mode.OUTPUT);
        for (int n : new int[] {5, 2, 3}) {
            key(n);
            put(record, "R00" + n);
            out.write(record, key);
            assertEquals("00", out.status());
        }
        key(3);
        out.write(record, key);
        assertEquals("22", out.status(), "p. 475: a WRITE at a number that holds a record is an invalid key, 22");
        out.close(false, false, false);
    }

    @Test
    void aSequentialReadGivesTheRecordsThatExistInAscendingOrderAndTheirNumbers() {
        written();
        RelFile in = file(RelFile.Access.SEQUENTIAL);
        in.open(SeqFile.Mode.INPUT);
        StringBuilder got = new StringBuilder();
        for (in.readNext(record, key); in.successful(); in.readNext(record, key)) got.append(text(record)).append('@').append(key.get().unscaled()).append(' ');
        assertEquals("R002@2 R003@3 R005@5 ", got.toString(), "pp. 427-428: ascending record numbers, the RELATIVE KEY updated");
        assertEquals("10", in.status());
        in.readNext(record, key);
        assertEquals("46", in.status(), "a READ after the at-end condition");
    }

    @Test
    void startPointsAtARecordAndAReadPointsPastOne() {
        written();
        RelFile f = file(RelFile.Access.DYNAMIC);
        f.open(SeqFile.Mode.IO);
        key(3);
        f.start(key, RelFile.Relation.NOT_LESS);
        f.readNext(record, key);
        assertEquals("R003", text(record), "after START the file points at record 3, which the next READ returns");
        key(2);
        f.read(record, key);
        f.readNext(record, key);
        assertEquals("R003", text(record), "after a random READ of 2 it points past 2: the next READ returns 3");
        key(3);
        f.start(key, RelFile.Relation.GREATER);
        f.readNext(record, key);
        assertEquals("R005", text(record));
        key(4);
        f.start(key, RelFile.Relation.EQUAL);
        assertEquals("23", f.status(), "p. 456: no record satisfies the relation");
    }

    @Test
    void rewriteAndDeleteNeedTheRecordWithSequentialAccessAndTheKeyOtherwise() {
        written();
        RelFile seq = file(RelFile.Access.SEQUENTIAL);
        seq.open(SeqFile.Mode.IO);
        seq.delete(null);
        assertEquals("43", seq.status(), "p. 302: no successful READ before the DELETE");
        seq.readNext(record, null);
        seq.delete(null);
        assertEquals("00", seq.status());
        seq.write(record, null);
        assertEquals("48", seq.status(), "Table 50: no WRITE with sequential access in I-O");
        seq.close(false, false, false);

        RelFile random = file(RelFile.Access.RANDOM);
        random.open(SeqFile.Mode.IO);
        key(2);
        random.read(record, key);
        assertEquals("23", random.status(), "record 2 was deleted");
        key(5);
        put(record, "NEW5");
        random.rewrite(record, key);
        assertEquals("00", random.status());
        key(4);
        random.delete(key);
        assertEquals("23", random.status(), "p. 332: no record 4");
        random.close(false, false, false);
        random.open(SeqFile.Mode.INPUT);
        key(5);
        random.read(record, key);
        assertEquals("NEW5", text(record), "the REWRITE reached the file");
    }

    @Test
    void aStatementTheOpenModeDoesNotAllowIsALogicError() {
        written();
        RelFile f = file(RelFile.Access.RANDOM);
        f.open(SeqFile.Mode.INPUT);
        key(2);
        f.rewrite(record, key);
        assertEquals("49", f.status());
        f.write(record, key);
        assertEquals("48", f.status());
        f.open(SeqFile.Mode.INPUT);
        assertEquals("41", f.status());
        f.close(true, false, false);
        f.open(SeqFile.Mode.INPUT);
        assertEquals("38", f.status());
    }

    @Test
    void anInvalidKeyCallsForErrorHandlingOnlyWithoutAnInvalidKeyPhrase() {
        written();
        RelFile f = file(RelFile.Access.RANDOM);
        f.open(SeqFile.Mode.INPUT);
        key(9);
        f.read(record, key);
        assertEquals(true, f.failed(false), "pp. 303-304: with no INVALID KEY phrase, a declarative runs");
        assertEquals(false, f.failed(true), "with one, the declarative is not executed");
    }

    @Test
    void aSequentialWriteNumbersFromOneOrAfterTheLastRecordWhenExtended() {
        written();
        RelFile f = file(RelFile.Access.SEQUENTIAL);
        f.open(SeqFile.Mode.EXTEND);
        put(record, "EXT6");
        f.write(record, key);
        assertEquals("6", key.get().unscaled().toString(), "p. 410: EXTEND writes after the highest number; p. 478: the number into the RELATIVE KEY");
        f.close(false, false, false);
        f.open(SeqFile.Mode.OUTPUT);
        f.write(record, key);
        assertEquals("1", key.get().unscaled().toString(), "p. 478: the first record released is number 1");
    }
}
