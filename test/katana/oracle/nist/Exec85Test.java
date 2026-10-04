package katana.oracle.nist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class Exec85Test {
    @Test
    void cardsLeaveTheNotesOut() {
        assertEquals(List.of("*EXTRACT-ALL", "X-001   TAPE1."), Exec85.cards("# a note\n\n*EXTRACT-ALL\n# another\nX-001   TAPE1.\n"));
    }

    @Test
    void theRepositorysCardsPutEveryXCardTextInColumnNine() throws Exception {
        for (String card : Exec85.cards(Files.readString(Path.of(System.getProperty("katana.root", "."), "oracle/nist/exec85.cards")))) {
            if (!card.startsWith("X-")) continue;
            assertTrue(card.matches("X-\\d{3}(\\d\\d| {2}) \\S.*"), "text not in column 9: " + card);
            assertTrue(card.length() <= 69, "text past column 69: " + card);
        }
    }

    @Test
    void exec85NamesItsOwnFiveFiles() {
        List<Population.Member> population = List.of(new Population.Member("COBOL", "EXEC85", "EXEC85", "", List.of(
                "002600     ASSIGN TO", "002700     XXXXX001.", "003000     XXXXX002", "003400     XXXXX003.",
                "003700     XXXXX055.", "004000     XXXXX058.")));
        assertEquals(List.of("002600     ASSIGN TO", "002700     \"population.dat\".", "003000     \"source.dat\"",
                "003400     \"newpop.dat\".", "003700     \"exec85.print\".", "004000     \"cards.dat\"."), Exec85.program(population));
    }

    @Test
    void recordsAreEightyCharactersEachAndReadBack() {
        byte[] records = Exec85.records(List.of("ABC", ""));
        assertEquals(160, records.length);
        assertEquals(List.of("ABC" + " ".repeat(77), " ".repeat(80)), Exec85.lines(records, 80));
        assertThrows(IllegalArgumentException.class, () -> Exec85.records(List.of("x".repeat(81))));
        assertThrows(IllegalArgumentException.class, () -> Exec85.lines(new byte[81], 80));
    }

    @Test
    void anUndefinedCardCountsOnlyInLiveCode() {
        List<Population.Member> extracted = List.of(new Population.Member("COBOL", "SQ219A", "SQ219A", "", List.of(
                "004200     **** X-CARD UNDEFINED ****.",
                "004300*C   **** X-CARD UNDEFINED ****")));
        assertEquals(List.of("SQ219A 004200"), Exec85.undefined(extracted));
    }
}
