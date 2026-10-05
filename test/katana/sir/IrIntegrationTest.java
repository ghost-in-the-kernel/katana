package katana.sir;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.oracle.nist.NistSources;
import katana.sir.build.Build;
import katana.sir.build.Roles;
import katana.profile.Profile;
import katana.sir.check.Check;
import katana.sir.check.NonExact;
import katana.sir.contract.Contract;
import katana.sir.json.IrJson;
import katana.sir.types.Program;
import katana.sir.types.Value;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Phase 2.5's check, its other half: every program of the corpus that parses builds to an IR that
 * holds its invariants (SIR-01 to SIR-07, checked as it is built), and serializes to JSON and back to
 * an equal IR with the same contract (2.6). sir/ir.golden holds, per corpus, the programs built and round-tripped, and how many
 * findings building and sir/check made; BankDemo, when fetched, is counted privately and not held.
 */
@Tag("integration")
class IrIntegrationTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));
    private static final Path GOLDEN = ROOT.resolve("sir/ir.golden");
    private static final String HASH = "sha256:feca25654120f84bcd60468e51f9dfe083ab409a44bac92c65771ab6e525fcfc";
    private static final Map<String, Roles.Verb> ROLES = Roles.vocabulary();
    private static final String HEADER = """
            # Phase 2.5 (docs/ROADMAP.md): each corpus's programs built to the Semantic IR under the ibm profile, as
            # `corpus NAME programs P built B round-trip R build-findings F check-findings C contract-facts N tokens-left T`, then each program
            # that did not build or round-trip; then `non-exact NAME` with each F-08 kind found and how often (2.7).
            # Written by `gradle integration -Dkatana.record=true`.
            """;

    /** Every literal of CardDemo, NIST and OMP, gathered while they are built once for both tests (C4). */
    private static final Set<Value.Literal> LITERALS = new java.util.LinkedHashSet<>();
    private static String report;

    @Test
    void everyProgramBuildsAndRoundTrips() throws Exception {
        String recorded = Files.exists(GOLDEN) ? Files.readString(GOLDEN) : HEADER;
        if (Boolean.getBoolean("katana.record")) Files.writeString(GOLDEN, report());
        System.out.print(report());
        assertEquals(recorded, report());
    }

    /** SIR-08 on the corpora: cobc prints each literal as its payload (LiteralIntegrationTest's comparison; the golden programs' too). */
    @Test
    void cobcPrintsEachLiteralOfTheCorporaAsItsPayload() throws Exception {
        report();
        Set<Value.Literal> literals = new java.util.LinkedHashSet<>(LITERALS);
        Path golden = ROOT.resolve("sir/golden");
        for (Build.Result r : Build.programs(Parse.file(golden.resolve("PROBE.cbl"), new Copybooks(List.of(golden), n -> 8), 8), HASH)) LiteralIntegrationTest.literals(r.program(), literals);
        Map<Value.Literal.Kind, Integer> kinds = new TreeMap<>();
        literals.forEach(l -> kinds.merge(l.kind(), 1, Integer::sum));
        System.out.println("literals by kind: " + kinds);
        List<Value.Literal> compared = literals.stream().filter(LiteralIntegrationTest::printable).toList();
        org.junit.jupiter.api.Assertions.assertTrue(kinds.getOrDefault(Value.Literal.Kind.HEX, 0) > 0 && kinds.getOrDefault(Value.Literal.Kind.FLOAT, 0) > 0 && compared.size() > 1000,
                "too few literals to refute anything: " + kinds);
        assertEquals(List.of(), LiteralIntegrationTest.mismatches(compared, Files.createDirectories(ROOT.resolve("build/sir-literals/corpora"))));
    }

    /** The corpora built once: the golden's report, and the literals gathered on the way. */
    private static synchronized String report() throws Exception {
        if (report != null) return report;
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
        IrIntegrationTest.report = report.toString();
        return IrIntegrationTest.report;
    }

    /** One corpus: each program that parses built, checked, and round-tripped; the counts, and what failed. */
    private static String corpus(String name, Map<String, Path> programs, Copybooks books) throws Exception {
        int parsed = 0, built = 0, roundTrip = 0, buildFindings = 0, checkFindings = 0, facts = 0, tokensLeft = 0;
        Map<String, Integer> nonExact = new TreeMap<>();
        Profile ibm = Profile.read(Files.readString(ROOT.resolve("profile/ibm.profile")));
        List<String> failures = new ArrayList<>();
        for (var entry : programs.entrySet()) {
            Parse.Result parse = Parse.file(entry.getValue(), books, 8);
            if (!parse.clean()) continue;
            parsed++;
            try {
                List<Build.Result> results = Build.programs(parse, HASH);
                built++;
                boolean trips = true;
                for (Build.Result result : results) {
                    buildFindings += result.findings().size();
                    checkFindings += Check.findings(result.program()).size();
                    NonExact.findings(result.program(), ibm).forEach(f -> nonExact.merge(f.kind().code, 1, Integer::sum));
                    String json = IrJson.write(result.program());
                    Program back = IrJson.read(json, Program.class);
                    List<String> contract = Contract.lines(result.program());
                    facts += contract.size();
                    if (!name.equals("bankdemo")) LiteralIntegrationTest.literals(result.program(), LITERALS);
                    trips &= back.equals(result.program()) && json.equals(IrJson.write(back)) && contract.equals(Contract.lines(back));
                }
                if (trips) roundTrip++;
                else failures.add(entry.getKey() + ": the JSON or the contract does not round-trip");
                List<String> left = TokenCoverage.missing(parse, results.stream().map(Build.Result::program).toList());
                tokensLeft += left.size();
                if (!left.isEmpty()) failures.add(entry.getKey() + ": " + left.size() + " tokens left behind, first " + left.get(0).replaceAll("^.*?:\\d+: ", ""));
                Set<String> outside = RolesGoldenTest.outside(results.stream().map(Build.Result::program).toList(), ROLES, new java.util.HashSet<>());
                if (!outside.isEmpty()) failures.add(entry.getKey() + ": outside sir/roles.golden: " + outside);
            } catch (RuntimeException e) {
                failures.add(entry.getKey() + ": " + e.getMessage());
            }
        }
        StringBuilder out = new StringBuilder("corpus " + name + " programs " + parsed + " built " + built + " round-trip " + roundTrip
                + " build-findings " + buildFindings + " check-findings " + checkFindings + " contract-facts " + facts + " tokens-left " + tokensLeft + "\n");
        out.append("non-exact ").append(name).append(nonExact.isEmpty() ? " none" : nonExact.entrySet().stream().map(e -> " " + e.getKey() + " " + e.getValue()).collect(Collectors.joining())).append('\n');
        failures.forEach(f -> out.append(f).append('\n'));
        return out.toString();
    }

    static Map<String, Path> programs(Path root) throws Exception {
        try (Stream<Path> files = Files.walk(root)) {
            return files.filter(Files::isRegularFile).filter(p -> p.getFileName().toString().toLowerCase().matches(".*\\.(cbl|cob|cobol)"))
                    .collect(Collectors.toMap(p -> root.relativize(p).toString(), p -> p, (a, b) -> a, TreeMap::new));
        }
    }

    static List<Path> folders(Path root) throws Exception {
        try (Stream<Path> files = Files.walk(root)) {
            List<Path> folders = new ArrayList<>(files.filter(p -> p.getFileName().toString().toLowerCase().matches(".*\\.(cpy|copy)")).map(Path::getParent).distinct().sorted().toList());
            folders.add(ROOT.resolve("front/ibm"));
            return folders;
        }
    }
}
