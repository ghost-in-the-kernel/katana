package katana.oracle.fixture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Every source a CardDemo fixture cites holds the records its sha256= says (needs `corpus/fetch.sh
 * carddemo`). POSTTRAN's transaction KSDS, as a `job:` source, is checked against the study's frozen
 * unload (F-02, oracle/prototypes/carddemo); the lane checks every `job:` source when it runs.
 */
@Tag("integration")
class FixtureSourcesIntegrationTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));
    private static final Path TRANFILE = ROOT.resolve("oracle/prototypes/carddemo/post1/tranfile.unl");
    private static final Map<String, Path> FROZEN = Map.of("job:POSTTRAN", TRANFILE, "job:POSTTRAN:AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS", TRANFILE);

    /** Each fixture's JCL exists, and each dataset's from= source, framed and keyed as declared, hashes to its sha256=. */
    @Test
    void everySourceHoldsTheRecordsItsHashSays() throws Exception {
        List<String> wrong = new ArrayList<>();
        int checked = 0;
        try (Stream<Path> files = Files.list(ROOT.resolve("oracle/fixture/carddemo"))) {
            for (Path file : files.filter(f -> f.toString().endsWith(".fixture")).sorted().toList()) {
                Fixture fixture = Fixture.read(Files.readString(file));
                assertTrue(Files.exists(source(fixture.first("source").values().get(0))), file + ": its JCL");
                for (Schema.Line d : fixture.datasets()) {
                    Map<String, String> a = d.attributes();
                    // A job: source with no frozen file is checked by the lane when it runs that job.
                    if (!a.containsKey("from") || (a.get("from").startsWith("job:") && !FROZEN.containsKey(a.get("from")))) continue;
                    String keys = "KSDS".equals(a.get("org")) ? a.get("keys") : null;
                    byte[] bytes = Files.readAllBytes(a.get("from").startsWith("job:") ? FROZEN.get(a.get("from")) : source(a.get("from")));
                    String actual = Content.sha256(Content.records(bytes, a.getOrDefault("framing", "fixed"), Integer.parseInt(a.get("lrecl")), keys));
                    if (!actual.equals("sha256:" + a.get("sha256"))) wrong.add(file.getFileName() + " " + d.values().get(0) + " is " + actual);
                    checked++;
                }
            }
        }
        assertEquals(List.of(), wrong);
        assertEquals(28, checked);
    }

    private static Path source(String from) {
        String[] where = from.split(":", 2);
        return where[0].equals("carddemo") ? ROOT.resolve("corpus/carddemo").resolve(where[1]) : ROOT.resolve(where[1]);
    }
}
