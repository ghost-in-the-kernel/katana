package katana.sir;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.oracle.nist.NistSources;
import katana.sir.build.Build;
import katana.sir.check.Check;
import katana.sir.json.IrJson;
import katana.sir.types.Program;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Phase 2.5's check, its other half: every program of the corpus that parses builds to an IR that
 * holds its invariants (SIR-01 to SIR-07, checked as it is built), and serializes to JSON and back to
 * an equal IR. sir/ir.golden holds, per corpus, the programs built and round-tripped, and how many
 * findings building and sir/check made; BankDemo, when fetched, is counted privately and not held.
 */
@Tag("integration")
class IrIntegrationTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));
    private static final Path GOLDEN = ROOT.resolve("sir/ir.golden");
    private static final String HASH = "sha256:feca25654120f84bcd60468e51f9dfe083ab409a44bac92c65771ab6e525fcfc";
    private static final String HEADER = """
            # Phase 2.5 (docs/ROADMAP.md): each corpus's programs built to the Semantic IR under the ibm profile, as
            # `corpus NAME programs P built B round-trip R build-findings F check-findings C`, then each program
            # that did not build or round-trip. Written by `gradle integration -Dkatana.record=true`.
            """;

    @Test
    void everyProgramBuildsAndRoundTrips() throws Exception {
        StringBuilder report = new StringBuilder(HEADER);
        Path carddemo = ROOT.resolve("corpus/carddemo/app");
        report.append(corpus("carddemo", programs(carddemo.resolve("cbl")), new Copybooks(List.of(carddemo.resolve("cpy"), carddemo.resolve("cpy-bms"), ROOT.resolve("front/ibm")),
                n -> n.equalsIgnoreCase("CUSTREC.cpy") ? 4 : 8)));
        Map<String, Path> nist = NistSources.write(ROOT, Files.createDirectories(ROOT.resolve("build/sir-ir-nist")));
        report.append(corpus("nist", nist, new Copybooks(List.of(nist.values().iterator().next().getParent()), n -> 8)));
        Path omp = ROOT.resolve("corpus/omp-cobol-course");
        report.append(corpus("omp", programs(omp), new Copybooks(folders(omp), n -> 8)));
        Path bankdemo = ROOT.resolve("corpus/bankdemo");
        if (Files.isDirectory(bankdemo)) System.out.print("privately: " + corpus("bankdemo", programs(bankdemo), new Copybooks(folders(bankdemo), n -> 8)).lines().findFirst().orElse(""));
        String recorded = Files.exists(GOLDEN) ? Files.readString(GOLDEN) : HEADER;
        if (Boolean.getBoolean("katana.record")) Files.writeString(GOLDEN, report.toString());
        System.out.print(report);
        assertEquals(recorded, report.toString());
    }

    /** One corpus: each program that parses built, checked, and round-tripped; the counts, and what failed. */
    private static String corpus(String name, Map<String, Path> programs, Copybooks books) throws Exception {
        int parsed = 0, built = 0, roundTrip = 0, buildFindings = 0, checkFindings = 0;
        List<String> failures = new ArrayList<>();
        for (var entry : programs.entrySet()) {
            Parse.Result parse = Parse.file(entry.getValue(), books, 8);
            if (!parse.clean()) continue;
            parsed++;
            try {
                Build.Result result = Build.program(parse, HASH);
                built++;
                buildFindings += result.findings().size();
                checkFindings += Check.findings(result.program()).size();
                String json = IrJson.write(result.program());
                Program back = IrJson.read(json, Program.class);
                if (back.equals(result.program()) && json.equals(IrJson.write(back))) roundTrip++;
                else failures.add(entry.getKey() + ": the JSON does not round-trip");
            } catch (RuntimeException e) {
                failures.add(entry.getKey() + ": " + e.getMessage());
            }
        }
        StringBuilder out = new StringBuilder("corpus " + name + " programs " + parsed + " built " + built + " round-trip " + roundTrip
                + " build-findings " + buildFindings + " check-findings " + checkFindings + "\n");
        failures.forEach(f -> out.append(f).append('\n'));
        return out.toString();
    }

    private static Map<String, Path> programs(Path root) throws Exception {
        try (Stream<Path> files = Files.walk(root)) {
            return files.filter(Files::isRegularFile).filter(p -> p.getFileName().toString().toLowerCase().matches(".*\\.(cbl|cob|cobol)"))
                    .collect(Collectors.toMap(p -> root.relativize(p).toString(), p -> p, (a, b) -> a, TreeMap::new));
        }
    }

    private static List<Path> folders(Path root) throws Exception {
        try (Stream<Path> files = Files.walk(root)) {
            List<Path> folders = new ArrayList<>(files.filter(p -> p.getFileName().toString().toLowerCase().matches(".*\\.(cpy|copy)")).map(Path::getParent).distinct().sorted().toList());
            folders.add(ROOT.resolve("front/ibm"));
            return folders;
        }
    }
}
