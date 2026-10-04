package katana.oracle.nist;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** EXEC85 on the pinned newcob.val under cobc (1.1c). Needs the image and the fetched corpus. */
@Tag("integration")
class Exec85IntegrationTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));

    @Test
    void extractsEveryMemberWithNoUndefinedCardOutsideTheCommunicationsModule(@TempDir Path work) throws Exception {
        List<String> cards = Exec85.cards(Files.readString(ROOT.resolve("oracle/nist/exec85.cards")));
        List<Population.Member> extracted = Exec85.run(
                ROOT.resolve("corpus/nist-cobol85-test-suite/newcob.val"), cards, work, new Cobc(Duration.ofMinutes(2)));
        Map<String, Integer> kinds = new TreeMap<>();
        extracted.forEach(m -> kinds.merge(m.kind(), 1, Integer::sum));
        assertEquals(Map.of("CLBRY", 51, "COBOL", 459, "DATA*", 2), kinds);
        // CM, the communications module, is not among the twelve the NIST run covers; EXEC85 is the
        // extractor itself.
        List<String> undefined = Exec85.undefined(extracted).stream()
                .filter(at -> !at.startsWith("CM") && !at.startsWith("EXEC85")).toList();
        assertEquals(List.of(), undefined);
    }
}
