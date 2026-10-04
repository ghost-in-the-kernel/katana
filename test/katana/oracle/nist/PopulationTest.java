package katana.oracle.nist;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class PopulationTest {
    private static final List<String> POPULATION = List.of(
            "CCVS85  VERSION 4.0   01 OCT 1992 0032",
            "*HEADER,COBOL,NC101A",
            "000100 IDENTIFICATION DIVISION.                                         NC1014.2",
            "*END-OF,NC101A",
            "*HEADER,CLBRY,ALTL1",
            "000100 01  ALTL1-ITEM PIC X.",
            "*END-OF,ALTL1",
            "*HEADER,COBOL,IC101A,SUBPRG,IC102A",
            "000100 PROGRAM-ID. IC102A.",
            "000200 EXIT PROGRAM.",
            "*END-OF,IC102A",
            "*HEADER,COBOL,ST140A                                                    TES00010",
            "*END-OF,ST140A",
            "*HEADER,DATA*,SQ302D",
            "SOME DATA",
            "*END-OF,SQ302D");

    @Test
    void readsEachMemberWithItsKindQualifiersAndLines() {
        List<Population.Member> members = Population.read(POPULATION);
        assertEquals(5, members.size());
        assertEquals(new Population.Member("COBOL", "NC101A", "NC101A",
                List.of("000100 IDENTIFICATION DIVISION.                                         NC1014.2")), members.get(0));
        assertEquals("CLBRY", members.get(1).kind());
        assertEquals("IC102A", members.get(2).name());
        assertEquals("IC101A", members.get(2).owner());
        assertEquals(2, members.get(2).lines().size());
        assertEquals("ST140A", members.get(3).name());
        assertEquals("DATA*", members.get(4).kind());
    }

    @Test
    void readsExec85OutputWhereTheCardsAreIndented() {
        List<Population.Member> members = Population.read(List.of(
                "      *HEADER,COBOL,NC101A", "000100 IDENTIFICATION DIVISION.", "      *END-OF,NC101A"));
        assertEquals("NC101A", members.get(0).name());
        assertEquals(1, members.get(0).lines().size());
    }

    @Test
    void refusesAMemberLeftOpenOrClosedByTheWrongName() {
        assertThrows(IllegalArgumentException.class, () -> Population.read(List.of("*HEADER,COBOL,A", "x")));
        assertThrows(IllegalArgumentException.class, () -> Population.read(List.of("*HEADER,COBOL,A", "*END-OF,B")));
        assertThrows(IllegalArgumentException.class, () -> Population.read(List.of("*HEADER,COBOL,A", "*HEADER,COBOL,B")));
        assertThrows(IllegalArgumentException.class, () -> Population.read(List.of("*HEADER,COBOL,A,SUBPRG", "*END-OF,A")));
    }
}
