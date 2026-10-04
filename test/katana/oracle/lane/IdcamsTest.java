package katana.oracle.lane;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/** IDCAMS's reading of its cards (columns 2 to 72, comments, continuation) and of a command's nested lists. */
class IdcamsTest {
    @Test
    void commandsAreReadFromColumnsTwoToSeventyTwoWithCommentsAndContinuations() {
        List<String> cards = List.of(
                "  DELETE    AWS.TRXFL.VSAM.KSDS                     -",
                "            CLUSTER",
                "  SET       MAXCC = 0 /* a comment */",
                "  DEFINE    CLUSTER  (NAME(AWS.TRXFL.VSAM.KSDS)     -",
                String.format("%-72s", "                      KEYS(32 0) RECORDSIZE(350 350)  -") + "00000050",
                String.format("%-72s", "                      INDEXED)") + "00000060");
        assertEquals(List.of("DELETE AWS.TRXFL.VSAM.KSDS CLUSTER", "SET MAXCC = 0",
                "DEFINE CLUSTER (NAME(AWS.TRXFL.VSAM.KSDS) KEYS(32 0) RECORDSIZE(350 350) INDEXED)"),
                Idcams.commands(cards).stream().map(c -> c.replaceAll("\\s+", " ").strip()).toList());
    }

    @Test
    void aListAfterASpaceBelongsToTheWordBeforeIt() {
        List<Idcams.Node> nodes = Idcams.nodes("DEFINE CLUSTER  (NAME(A.B) KEYS(32 0) INDEXED) DATA (NAME(A.D))");
        assertEquals("DEFINE", nodes.get(0).word());
        assertEquals("CLUSTER", nodes.get(1).word());
        assertEquals(List.of("NAME", "KEYS", "INDEXED"), nodes.get(1).list().stream().map(Idcams.Node::word).toList());
        assertEquals("32 0", nodes.get(1).list().get(1).value());
        assertEquals("DATA", nodes.get(2).word());
        assertEquals(List.of("SET", "MAXCC", "=", "0"), Idcams.nodes("SET MAXCC=0").stream().map(Idcams.Node::word).toList());
    }

    @Test
    void aCommentOpenedInColumnOneIsAFindingNotAComment() {
        Idcams.Result result = new Idcams(null, java.util.Map.of()).run(List.of("/* Copyright */", "    REPRO INFILE(A) OUTFILE(B)"));
        assertEquals(12, result.rc());
        assertTrue(result.finding().contains("card 1 opens a comment in column 1"), result.finding());
    }
}
