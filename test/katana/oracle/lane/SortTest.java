package katana.oracle.lane;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

/** The DFSORT subset (docs/ROADMAP.md 1.5) on CardDemo's own cards: reading, ZD, conditions, OUTREC and EDIT, and what is refused. */
class SortTest {
    private static final List<String> TRANREPT_SYMNAMES = List.of("TRAN-CARD-NUM,263,16,ZD", "TRAN-PROC-DT,305,10,CH",
            "PARM-START-DATE,C'2024-03-01'                                      //Date", "PARM-END-DATE,C'2024-03-01'");
    private static final List<String> TRANREPT_SYSIN = List.of(" SORT FIELDS=(TRAN-CARD-NUM,A)",
            " INCLUDE COND=(TRAN-PROC-DT,GE,PARM-START-DATE,AND,", "         TRAN-PROC-DT,LE,PARM-END-DATE)");

    private static byte[] bytes(String s) {
        return s.getBytes(StandardCharsets.ISO_8859_1);
    }

    @Test
    void aStatementContinuesWhileItsOperandEndsInAComma() {
        List<String[]> statements = SortCards.statements(TRANREPT_SYSIN);
        assertEquals(2, statements.size());
        assertEquals("INCLUDE", statements.get(1)[0]);
        assertEquals("COND=(TRAN-PROC-DT,GE,PARM-START-DATE,AND,TRAN-PROC-DT,LE,PARM-END-DATE)", statements.get(1)[1]);
    }

    @Test
    void tranreptsIncludeKeepsOneDate() {
        SortCards.Spec spec = SortCards.read(TRANREPT_SYMNAMES, TRANREPT_SYSIN);
        byte[] record = bytes(" ".repeat(304) + "2024-03-01" + " ".repeat(36));
        assertTrue(spec.select().test(record));
        assertFalse(spec.select().test(bytes(" ".repeat(304) + "2024-03-02" + " ".repeat(36))));
        assertEquals(1, spec.keys().size());
    }

    @Test
    void zonedDecimalReadsIbmOverpunchesAndRefusesAnythingElse() {
        SortCards.Field f = new SortCards.Field(1, 3, "ZD");
        assertEquals(BigInteger.valueOf(120), Zoned.value(bytes("12{"), f));
        assertEquals(BigInteger.valueOf(-120), Zoned.value(bytes("12}"), f));
        assertEquals(BigInteger.valueOf(-121), Zoned.value(bytes("12J"), f));
        assertEquals(BigInteger.valueOf(129), Zoned.value(bytes("12I"), f));
        assertThrows(SortCards.Unsupported.class, () -> Zoned.value(bytes("1 2"), f));
        assertThrows(SortCards.Unsupported.class, () -> Zoned.value(bytes("12p"), f));
    }

    @Test
    void prtcatblsOutrecBuildsFortyOneBytesThroughItsEditMask() {
        SortCards.Spec spec = SortCards.read(List.of("TRANCAT-ACCT-ID,1,11,ZD", "TRANCAT-TYPE-CD,12,2,CH", "TRANCAT-CD,14,4,ZD", "TRAN-CAT-BAL,18,11,ZD"),
                List.of(" SORT FIELDS=(TRANCAT-ACCT-ID,A,TRANCAT-TYPE-CD,A,TRANCAT-CD,A)", " OUTREC FIELDS=(TRANCAT-ACCT-ID,X,",
                        "     TRANCAT-TYPE-CD,X,", "     TRANCAT-CD,X,", "     TRAN-CAT-BAL,EDIT=(TTTTTTTTT.TT),9X)"));
        byte[] built = SortOutrec.build(spec.outrec(), bytes("00000000001" + "01" + "0001" + "0000001234J" + "0".repeat(22)));
        assertEquals("00000000001 01 0001 000000123.41" + " ".repeat(9), new String(built, StandardCharsets.ISO_8859_1));
    }

    @Test
    void creastmtsOutrecPlacesFieldsByColumn() {
        SortCards.Spec spec = SortCards.read(List.of(), List.of("  SORT FIELDS=(263,16,CH,A,1,16,CH,A)", "  OUTREC FIELDS=(1:263,16,17:1,262,279:279,50)"));
        byte[] record = bytes("T".repeat(262) + "C".repeat(16) + "R".repeat(72));
        String built = new String(SortOutrec.build(spec.outrec(), record), StandardCharsets.ISO_8859_1);
        assertEquals(328, built.length());
        assertEquals("C".repeat(16) + "T".repeat(262) + "R".repeat(50), built);
    }

    @Test
    void whatTheLaneDoesNotRunIsRefusedWithItsCard() {
        assertTrue(assertThrows(SortCards.Unsupported.class, () -> SortCards.read(List.of(), List.of(" SORT FIELDS=(1,4,CH,A)", " SUM FIELDS=NONE")))
                .getMessage().contains("SUM FIELDS=NONE"));
        assertThrows(SortCards.Unsupported.class, () -> SortCards.read(List.of(), List.of(" SORT FIELDS=(1,4,PD,A)")));
        assertThrows(SortCards.Unsupported.class, () -> SortCards.read(List.of(), List.of(" SORT FIELDS=(1,4,CH,A)",
                " INCLUDE COND=(1,1,CH,EQ,C'A',AND,2,1,CH,EQ,C'B',OR,3,1,CH,EQ,C'C')")));
        assertThrows(SortCards.Unsupported.class, () -> SortCards.read(List.of("X,=,4,CH"), List.of(" SORT FIELDS=(1,4,CH,A)")));
    }

    @Test
    void theGeneratedSortKeepsEqualKeysInOrderAndRefusesOutrec() {
        String source = SortCobol.source(SortCards.read(TRANREPT_SYMNAMES, TRANREPT_SYSIN), 350);
        assertTrue(source.contains("WITH DUPLICATES IN ORDER"));
        assertTrue(source.contains("C1 >= '2024-03-01'"), source);
        assertThrows(SortCards.Unsupported.class, () -> SortCobol.source(SortCards.read(List.of(),
                List.of("  SORT FIELDS=(263,16,CH,A)", "  OUTREC FIELDS=(1:263,16)")), 350));
    }
}
