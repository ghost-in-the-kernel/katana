package katana.oracle.prove;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import katana.oracle.fixture.Fixture;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** 8A.1's trial (research §48, R-072): three foreign ports of ACCRUE held to the COBOL through the lanes. */
@Tag("integration")
class ProveIntegrationTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", ".")), FOREIGN = ROOT.resolve("oracle/fixture/foreign");

    private static Prove.Result prove(String port) throws Exception {
        Fixture fixture = Fixture.read(Files.readString(FOREIGN.resolve("ACCRUE.fixture")));
        Prove.Setup s = new Prove.Setup(ROOT, Map.of("repo", ROOT), FOREIGN.resolve("cbl"), List.of(FOREIGN.resolve("cpy")), ROOT.resolve("build/prove-" + port), Duration.ofSeconds(60));
        List<String> command = List.of(Path.of(System.getProperty("java.home"), "bin", "java").toString(), "-cp", System.getProperty("java.class.path"), AccruePorts.class.getName() + "$" + port);
        Prove.Result r = Prove.run(fixture, s, Map.of("ACCRUE", command));
        System.out.print(r.text());
        return r;
    }

    @Test
    void aFaithfulPortIsTheSame() throws Exception {
        Prove.Result r = prove("Faithful");
        assertTrue(r.same(), r.text());
        assertEquals(1, r.datasets().size(), r.text());
    }

    @Test
    void aPortThatRoundsWhereTheCobolTruncatesDiffersInTheFieldsItRounds() throws Exception {
        Prove.Result r = prove("Naive");
        assertFalse(r.same(), r.text());
        assertTrue(r.refused().isEmpty(), r.text());
        List<String> findings = r.datasets().get(0).findings();
        assertTrue(findings.stream().anyMatch(f -> f.contains("ACCT-INT") && f.contains("2 of 5 records differ")), r.text());
        assertTrue(findings.stream().anyMatch(f -> f.contains("ACCT-BAL") && f.contains("2 of 5 records differ")), r.text());
        assertTrue(findings.stream().noneMatch(f -> f.contains("ACCT-ID")), r.text());
        assertEquals(Map.of("ACCRUE", true), r.stdout(), "the count on stdout is the same");
    }

    @Test
    void aPortThatIgnoresItsDdIsRefused() throws Exception {
        Prove.Result r = prove("Deaf");
        assertEquals(1, r.refused().size(), r.text());
        assertTrue(r.refused().get(0).contains("DD OUTFILE"), r.text());
        assertTrue(r.datasets().isEmpty(), "a refused port's datasets are not compared");
    }
}
