package katana.oracle.prove;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import katana.findings.Divergence;
import katana.oracle.fixture.Fixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

/**
 * 8A.2's check (research §49): every divergence kind's generated case through the lanes, its exact port the SAME and
 * its wrong port, the kind's mutation, differing in the receiving field and no other; the share of records it
 * catches printed.
 */
@Tag("integration")
class DivergenceKindsIntegrationTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));
    private static final long SEED = 49;

    /** For each kind: its case generated and written, both ports proved, the exact one the SAME, the mutation caught in the receiver alone, the share of records it catches. */
    @Test
    void everyKindsExactPortIsTheSameAndItsMutationDiffersInItsReceiver() throws Exception {
        String profile = Files.readAllLines(ROOT.resolve("oracle/fixture/trivial/COPY.fixture")).stream().filter(l -> l.startsWith("profile ")).findFirst().orElseThrow();
        List<String> rates = new ArrayList<>();
        for (Divergence.Kind kind : Divergence.Kind.values()) {
            Generators.Case c = Generators.generate(kind, SEED);
            Path dir = ROOT.resolve("build/divergence/" + kind.code);
            Fixture fixture = Fixture.read(Files.readString(Generators.write(c, dir, profile)));
            Prove.Result exact = prove(kind, c, dir, fixture, "exact"), wrong = prove(kind, c, dir, fixture, "wrong");
            assertTrue(exact.same(), kind.code + " exact: " + exact.text());
            assertTrue(wrong.refused().isEmpty() && !wrong.same(), kind.code + " wrong: " + wrong.text());
            List<String> findings = wrong.datasets().get(0).findings();
            assertEquals(1, findings.size(), kind.code + ": only the receiver differs: " + wrong.text());
            Matcher m = Pattern.compile("DV-REC\\." + c.receiver() + ": (\\d+) of (\\d+) records differ").matcher(findings.get(0));
            assertTrue(m.find(), kind.code + ": the receiver " + c.receiver() + " named: " + findings.get(0));
            rates.add(kind.code + " " + kind.title + ": the mutation caught on " + m.group(1) + " of " + m.group(2) + " records");
        }
        rates.forEach(System.out::println);
    }

    private static Prove.Result prove(Divergence.Kind kind, Generators.Case c, Path dir, Fixture fixture, String mode) throws Exception {
        Prove.Setup s = new Prove.Setup(ROOT, Map.of("repo", dir), dir.resolve("cbl"), List.of(dir.resolve("cpy")), dir.resolve(mode), Duration.ofSeconds(60));
        List<String> command = new ArrayList<>(List.of(Path.of(System.getProperty("java.home"), "bin", "java").toString(), "-cp", System.getProperty("java.class.path"),
                KindPorts.class.getName(), kind.code, mode));
        if (kind == Divergence.Kind.DV_02) command.add(c.cobol().replaceAll("(?s).*OP-A \\* ([0-9.]+).*", "$1"));
        return Prove.run(fixture, s, Map.of(c.program(), command));
    }
}
