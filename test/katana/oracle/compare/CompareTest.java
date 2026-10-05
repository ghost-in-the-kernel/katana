package katana.oracle.compare;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.List;
import katana.oracle.lane.Gate;
import org.junit.jupiter.api.Test;

/** The comparator (docs/ROADMAP.md 1.7) on a small layout: what it normalizes, what it refuses, how it reports. */
class CompareTest {
    private static final String COPYBOOK = String.join("\n",
            "       01  REC.",
            "           05  ID         PIC X(4).",
            "           05  AMT        PIC S9(5)V99 COMP-3.",
            "           05  QTY        PIC 9(3).",
            "      * a comment",
            "           05  TAGS       OCCURS 2 TIMES.",
            "               10  TAG    PIC X(2).",
            "           05  ALT        REDEFINES TAGS PIC X(4).",
            "               88  NONE   VALUE SPACES.");
    private static final Gate.Result PASSED = new Gate.Result(true, List.of());
    private static final Layout LAYOUT = Layout.read(COPYBOOK);

    /** One record: ID and TAGs as text in the encoding, AMT packed, QTY zoned in the encoding. */
    private static byte[] record(String id, byte[] amt, String qty, String tags, String encoding) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Charset cs = Decode.charset(encoding);
        out.writeBytes(id.getBytes(cs));
        out.writeBytes(amt);
        out.writeBytes(qty.getBytes(cs));
        out.writeBytes(tags.getBytes(cs));
        return out.toByteArray();
    }

    private static final byte[] AMT = {0x00, 0x12, 0x34, 0x5c};

    @Test
    void theLayoutPlacesEveryElementaryField() {
        assertEquals(15, LAYOUT.length());
        assertEquals(List.of("REC.ID", "REC.AMT", "REC.QTY", "REC.TAGS(1).TAG", "REC.TAGS(2).TAG"), LAYOUT.fields().stream().map(Layout.Field::path).toList());
        assertEquals(new Layout.Field("REC.AMT", 4, 4, Layout.Kind.PACKED, true, 2), LAYOUT.fields().get(1));
        assertThrows(Layout.Unsupported.class, () -> Layout.read("       01 R.\n           05 N PIC 9 OCCURS 3 DEPENDING ON X."));
    }

    @Test
    void numbersDecodeByTheirKindAndEncoding() {
        Layout.Field packed = LAYOUT.fields().get(1), zoned = LAYOUT.fields().get(2);
        assertEquals("123.45", Decode.value(packed, AMT, 0, "ascii").replaceFirst("^", ""));
        assertEquals("-123.45", Decode.value(packed, new byte[] {0x00, 0x12, 0x34, 0x5d}, 0, "ascii"));
        assertTrue(Decode.value(packed, new byte[] {0x00, 0x12, 0x34, 0x56}, 0, "ascii").startsWith("invalid packed decimal"));
        assertEquals("123", Decode.value(zoned, new byte[] {(byte) 0xf1, (byte) 0xf2, (byte) 0xc3}, 0, "cp037"));
        assertEquals("-123", Decode.value(zoned, "12L".getBytes(), 0, "ascii"));
        assertEquals("123", Decode.value(zoned, "12C".getBytes(), 0, "ascii"));
    }

    @Test
    void textAndZonedFieldsAreTheSameAcrossEncodingsPackedBytesAreNot() {
        Compare.Side ebcdic = new Compare.Side("D", List.of(record("A001", AMT, "042", "XYZW", "cp037")), "cp037");
        Compare.Side ascii = new Compare.Side("D", List.of(record("A001", AMT, "042", "XYZW", "ascii")), "ascii");
        Compare.Result transcoded = Compare.compare(PASSED, ebcdic, ascii, LAYOUT, List.of(), Compare.Numproc.NOPFD);
        assertTrue(transcoded.same(), transcoded.findings().toString());
        byte[] changed = record("A001", new byte[] {0x00, 0x12, 0x35, 0x5c}, "042", "XYZW", "ascii");
        Compare.Result one = Compare.compare(PASSED, ascii, new Compare.Side("D", List.of(changed), "ascii"), LAYOUT, List.of(), Compare.Numproc.NOPFD);
        assertEquals(List.of("D REC.AMT: 1 of 1 records differ; first at record 1, byte offset 4: X'0012345C' (123.45) then X'0012355C' (123.55), compared as bytes"), one.findings());
    }

    @Test
    void recordCountsAreNeverNormalizedAndMasksAreListed() {
        byte[] r = record("A001", AMT, "042", "XYZW", "ascii");
        Compare.Result counts = Compare.compare(PASSED, new Compare.Side("D", List.of(r, r), "ascii"), new Compare.Side("D", List.of(r), "ascii"), LAYOUT, List.of(), Compare.Numproc.NOPFD);
        assertEquals(List.of("D: 2 records, then 1 (record counts are never normalized)"), counts.findings());
        Compare.Result masked = Compare.compare(PASSED, new Compare.Side("D", List.of(r), "ascii"),
                new Compare.Side("D", List.of(record("A001", AMT, "042", "QQQQ", "ascii")), "ascii"), LAYOUT, List.of(new Compare.Mask("REC.TAGS", "a header the run stamps")), Compare.Numproc.NOPFD);
        assertTrue(masked.same());
        assertEquals(List.of("mask REC.TAGS (a header the run stamps): applied to 2 records"), masked.masks());
    }

    @Test
    void nothingIsComparedBeforeTheGatePasses() {
        Compare.Side side = new Compare.Side("D", List.of(), "ascii");
        assertThrows(IllegalStateException.class, () -> Compare.compare(new Gate.Result(false, List.of("dataset X differs")), side, side, LAYOUT, List.of(), Compare.Numproc.NOPFD));
        assertFalse(new Gate.Result(false, List.of()).identical());
    }

    @Test
    void aSignNibbleIsANoteUnderNopfdAndADifferenceUnderPfd() {
        byte[] f = record("A001", AMT, "123", "XYZW", "cp037"), c = f.clone();
        c[10] = (byte) 0xc3;
        Compare.Side expected = new Compare.Side("D", List.of(f), "cp037"), actual = new Compare.Side("D", List.of(c), "cp037");
        Compare.Result nopfd = Compare.compare(PASSED, expected, actual, LAYOUT, List.of(), Compare.Numproc.NOPFD);
        assertTrue(nopfd.same(), nopfd.findings().toString());
        assertEquals(List.of("sign-variant D REC.QTY: 1 of 1 records equal but for the sign nibble (NUMPROC(NOPFD))"), nopfd.notes());
        Compare.Result pfd = Compare.compare(PASSED, expected, actual, LAYOUT, List.of(), Compare.Numproc.PFD);
        assertEquals(List.of("D REC.QTY: 1 of 1 records differ; first at record 1, byte offset 8: X'F1F2F3' (123) then X'F1F2C3' (123)"), pfd.findings());
        assertEquals(Compare.Numproc.PFD, Compare.Numproc.of("PFD"));
        assertEquals(Compare.Numproc.NOPFD, Compare.Numproc.of("NOPFD"));
    }

    @Test
    void aSignedFieldKeepsItsSignClass() {
        Layout.Field signed = new Layout.Field("N", 0, 2, Layout.Kind.ZONED, true, 0);
        byte[] positiveF = {(byte) 0xf1, (byte) 0xf2}, positiveC = {(byte) 0xf1, (byte) 0xc2}, negative = {(byte) 0xf1, (byte) 0xd2};
        assertTrue(Compare.signVariant(signed, positiveF, positiveC));
        assertFalse(Compare.signVariant(signed, positiveC, negative));
        assertTrue(Compare.signVariant(new Layout.Field("N", 0, 2, Layout.Kind.ZONED, false, 0), positiveC, negative));
        assertFalse(Compare.signVariant(signed, positiveF, new byte[] {(byte) 0xf1, (byte) 0xf3}));
    }
}
