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
import katana.sir.build.Build;
import katana.sir.build.Roles;
import katana.profile.Profile;
import katana.sir.check.Check;
import katana.sir.check.NonExact;
import katana.sir.contract.Contract;
import katana.sir.json.IrJson;
import katana.sir.types.Program;
import katana.sir.types.Value;

/**
 * Phase 2.5's check, its other half: every program of the corpus that parses builds to an IR that
 * holds its invariants (SIR-01 to SIR-07, checked as it is built), and serializes to JSON and back to
 * an equal IR with the same contract (2.6). sir/ir.golden holds, per corpus, the programs built and round-tripped, and how many
 * findings building and sir/check made; BankDemo, when fetched, is counted privately and not held.
 * Run by `katana.front.CorpusIntegrationTest`, which parses each program once for this, the data map and
 * the front end's checks (J-169); this assembles the IR's half from each program's parse.
 */
public final class IrCorpus {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));
    private static final Path GOLDEN = ROOT.resolve("sir/ir.golden");
    private static final String HASH = "sha256:25d21d9dc3d4db6554821c53cb0927a5bcc95f750d04631a50e43bb636866296";
    private static final Map<String, Roles.Verb> ROLES = Roles.vocabulary();
    private static final String HEADER = """
            # Phase 2.5 (docs/ROADMAP.md): each corpus's programs built to the Semantic IR under the ibm profile, as
            # `corpus NAME programs P built B round-trip R build-findings F check-findings C contract-facts N tokens-left T`, then each program
            # that did not build or round-trip; then `non-exact NAME` with each F-08 kind found and how often (2.7), and
            # `valid-under-hypothesis NAME` with each HY- kind (R-049's mechanism, J-120).
            # Written by `gradle integration -Dkatana.record=true`.
            """;

    /** Every literal of CardDemo, NIST and OMP, gathered while they are built once for both checks (C4). */
    private static final Set<Value.Literal> LITERALS = new java.util.LinkedHashSet<>();

    /** The IR golden held against the corpora's report. */
    public static void hold(String report) throws Exception {
        String recorded = Files.exists(GOLDEN) ? Files.readString(GOLDEN) : HEADER;
        if (Boolean.getBoolean("katana.record")) Files.writeString(GOLDEN, report);
        System.out.print(report);
        assertEquals(recorded, report);
    }

    /** SIR-08 on the corpora: cobc prints each literal as its payload (LiteralIntegrationTest's comparison; the golden programs' too). */
    public static void literals() throws Exception {
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

    /** The golden's header, before the corpora. */
    public static String header() {
        return HEADER;
    }

    /** One program's part of a corpus's counts, its failures, and the literals it holds. */
    public record One(boolean parsed, boolean built, boolean trips, int buildFindings, int checkFindings, int facts, int tokensLeft,
            Map<String, Integer> nonExact, List<String> failures, Set<Value.Literal> literals) {}

    private static final Profile IBM = profile();

    private static Profile profile() {
        try {
            return Profile.read(Files.readString(ROOT.resolve("profile/ibm.profile")));
        } catch (java.io.IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    }

    /** One program, from its parse: built, checked and round-tripped; independent of every other. */
    public static One one(String key, Parse.Result parse, String name) {
        Profile ibm = IBM;
        Map<String, Integer> nonExact = new TreeMap<>();
        List<String> failures = new ArrayList<>();
        Set<Value.Literal> literals = new java.util.LinkedHashSet<>();
        if (!parse.clean()) return new One(false, false, false, 0, 0, 0, 0, nonExact, failures, literals);
        int buildFindings = 0, checkFindings = 0, facts = 0, tokensLeft = 0;
        try {
            List<Build.Result> results = Build.programs(parse, HASH);
            boolean trips = true;
            for (Build.Result result : results) {
                buildFindings += result.findings().size();
                checkFindings += Check.findings(result.program()).size();
                NonExact.findings(result.program(), ibm).forEach(f -> nonExact.merge(f.kind().code, 1, Integer::sum));
                String json = IrJson.write(result.program());
                Program back = IrJson.read(json, Program.class);
                List<String> contract = Contract.lines(result.program());
                facts += contract.size();
                if (!name.equals("bankdemo")) LiteralIntegrationTest.literals(result.program(), literals);
                trips &= back.equals(result.program()) && json.equals(IrJson.write(back)) && contract.equals(Contract.lines(back));
            }
            if (!trips) failures.add(key + ": the JSON or the contract does not round-trip");
            List<String> left = TokenCoverage.missing(parse, results.stream().map(Build.Result::program).toList());
            tokensLeft += left.size();
            if (!left.isEmpty()) failures.add(key + ": " + left.size() + " tokens left behind, first " + left.get(0).replaceAll("^.*?:\\d+: ", ""));
            Set<String> outside = RolesGoldenTest.outside(results.stream().map(Build.Result::program).toList(), ROLES, new java.util.HashSet<>());
            if (!outside.isEmpty()) failures.add(key + ": outside sir/roles.golden: " + outside);
            return new One(true, true, trips, buildFindings, checkFindings, facts, tokensLeft, nonExact, failures, literals);
        } catch (RuntimeException e) {
            failures.add(key + ": " + e.getMessage());
            return new One(true, false, false, buildFindings, checkFindings, facts, tokensLeft, nonExact, failures, literals);
        }
    }

    /** One corpus's lines of the golden from its programs' parts, in their order; its literals gathered. */
    public static String corpus(String name, List<One> ones) {
        int parsed = 0, built = 0, roundTrip = 0, buildFindings = 0, checkFindings = 0, facts = 0, tokensLeft = 0;
        Map<String, Integer> nonExact = new TreeMap<>();
        List<String> failures = new ArrayList<>();
        for (One o : ones) {
            if (!o.parsed()) continue;
            parsed++;
            if (o.built()) built++;
            if (o.trips()) roundTrip++;
            buildFindings += o.buildFindings();
            checkFindings += o.checkFindings();
            facts += o.facts();
            tokensLeft += o.tokensLeft();
            o.nonExact().forEach((k, v) -> nonExact.merge(k, v, Integer::sum));
            failures.addAll(o.failures());
            LITERALS.addAll(o.literals());
        }
        StringBuilder out = new StringBuilder("corpus " + name + " programs " + parsed + " built " + built + " round-trip " + roundTrip
                + " build-findings " + buildFindings + " check-findings " + checkFindings + " contract-facts " + facts + " tokens-left " + tokensLeft + "\n");
        out.append(kinds("non-exact ", name, nonExact, false)).append(kinds("valid-under-hypothesis ", name, nonExact, true));
        failures.forEach(f -> out.append(f).append('\n'));
        return out.toString();
    }

    public static Map<String, Path> programs(Path root) throws Exception {
        try (Stream<Path> files = Files.walk(root)) {
            return files.filter(Files::isRegularFile).filter(p -> p.getFileName().toString().toLowerCase().matches(".*\\.(cbl|cob|cobol)"))
                    .collect(Collectors.toMap(p -> root.relativize(p).toString(), p -> p, (a, b) -> a, TreeMap::new));
        }
    }

    public static List<Path> folders(Path root) throws Exception {
        try (Stream<Path> files = Files.walk(root)) {
            List<Path> folders = new ArrayList<>(files.filter(p -> p.getFileName().toString().toLowerCase().matches(".*\\.(cpy|copy)")).map(Path::getParent).distinct().sorted().toList());
            folders.add(ROOT.resolve("front/ibm"));
            return folders;
        }
    }

    /** One line of the IR golden: the kinds found of one class (`NE-` or `HY-`) and how often; `none` when there are none. */
    private static String kinds(String head, String name, java.util.Map<String, Integer> found, boolean hypothesis) {
        String listed = found.entrySet().stream().filter(e -> e.getKey().startsWith("HY-") == hypothesis).map(e -> " " + e.getKey() + " " + e.getValue()).collect(Collectors.joining());
        return head + name + (listed.isEmpty() ? " none" : listed) + "\n";
    }
}
