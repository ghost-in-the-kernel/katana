package katana.oracle.lane;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.tools.ToolProvider;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.lower.Lower;
import katana.oracle.Runs;
import katana.oracle.fixture.Fixture;
import katana.sir.build.Build;
import katana.sir.types.Program;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Phase 4.1's CardDemo check (research §22): each CardDemo fixture through the java lane, its programs
 * lowered to Java and its indexed datasets `rt/IdxFile`'s, loaded and unloaded by `rt/Ksds` (LANE-16),
 * reports what the cobc lane's golden (oracle/lane/carddemo.golden) holds: every step's return code,
 * every stdout's lines and hash, every dataset's records and hash, every finding. The java lane's report
 * says it is the java lane, and has no program sources to hash; nothing else may differ. CREASTMT, which the
 * cobc lane abends in at the PSA, runs end to end through the z/OS storage facade (LANE-20), its statements
 * held to their own input (`Statements`).
 */
@Tag("integration")
class CardDemoJavaLaneIntegrationTest {
    private static final Path ROOT = LaneRuns.ROOT, CARDDEMO = ROOT.resolve("corpus/carddemo");
    private static final List<String> PROGRAMS = List.of("CBTRN02C", "CBACT04C", "CBTRN03C", "CBSTM03A", "CBSTM03B");
    private static final List<String> JOBS = List.of("POSTTRAN", "TRANREPT", "TRANREPT-STUDY", "INTCALC", "PRTCATBL", "PRTCATBL-INLINE", "COMBTRAN", "SORTTEST");

    @Test
    void everyCardDemoJobTheJavaLaneRunsReportsWhatTheCobcLanesGoldenHolds() throws Exception {
        Path work = Files.createDirectories(ROOT.resolve("build/carddemo-java"));
        Path classes = Files.createTempDirectory(work, "classes");
        Map<String, List<String>> commands = new LinkedHashMap<>();
        String launcher = Path.of(System.getProperty("java.home"), "bin", "java").toString(), path = classes + java.io.File.pathSeparator + System.getProperty("java.class.path");
        Fixture posttran = fixture("POSTTRAN");
        // The profile's DISPSIGN and DYNAM (ibm.dispsign COMPAT, ibm.dynam NODYNAM), which the generated main reads (J-109, J-186).
        java.util.Map<String, String> ibm = katana.profile.Profile.read(Files.readString(ROOT.resolve("profile/ibm.profile"))).ibm();
        Fixture statements = fixture("CREASTMT");
        // CBSTM03A's copybooks are read at CREASTMT's tab widths; a CALL finds CBSTM03B's class on the path (LANE-19).
        for (String name : PROGRAMS) commands.put(name, List.of(launcher, "-cp", path, "-Dkatana.dispsign=" + ibm.get("ibm.dispsign"), "-Dkatana.dynam=" + ibm.get("ibm.dynam"), lowered(name, classes, name.startsWith("CBSTM") ? statements : posttran)));
        commands.put(CobcLane.KSDS, List.of(launcher, "-cp", path, "katana.rt.Ksds"));
        Map<String, String> golden = sections(Files.readString(ROOT.resolve("oracle/lane/carddemo.golden")));
        List<Path> runs = new ArrayList<>();
        Map<String, List<String>> differ = new LinkedHashMap<>();
        for (String job : JOBS) {
            Path dir = Files.createTempDirectory(work, job);
            runs.add(dir);
            List<String> cobc = comparable(golden.get(job)), java = comparable(CobcLane.run(fixture(job), setup(dir, commands)).text());
            List<String> unexplained = java.stream().filter(l -> !cobc.contains(l)).filter(l -> !d005(job, l, dir)).toList();
            if (!unexplained.isEmpty() || cobc.size() != java.size()) differ.put(job, unexplained);
        }
        assertEquals(Map.of(), differ, "the java lane's lines the cobc lane's golden does not hold, by job");
        Path dir = Files.createTempDirectory(work, "CREASTMT");
        runs.add(dir);
        // CREASTMT end to end (R-027 C-2, research §25): the cobc lane abends at the PSA; here CBSTM03A reads its job, step and DDs through the
        // z/OS storage facade, and its statements are checked against the transactions they summarize.
        List<String> creastmt = comparable(CobcLane.run(fixture("CREASTMT"), setup(dir, commands)).text()), before = comparable(golden.get("CREASTMT"));
        int at = creastmt.indexOf(creastmt.stream().filter(l -> l.startsWith("stdout STEP040")).findFirst().orElseThrow());
        assertEquals(List.of(), creastmt.subList(0, at).stream().filter(l -> !before.contains(l)).toList(), "CREASTMT's steps before STEP040");
        org.junit.jupiter.api.Assertions.assertTrue(creastmt.contains("step STEP040 CBSTM03A rc=0") && creastmt.stream().noneMatch(l -> l.startsWith("finding")), creastmt.toString());
        assertEquals(List.of("Running JCL : CREASTMT Step STEP040 ", "DD Names from TIOT: ", ": SYSPRINT --  null UCB", ": SYSOUT   --  null UCB", ": TRNXFILE -- valid UCB",
                ": XREFFILE -- valid UCB", ": ACCTFILE -- valid UCB", ": CUSTFILE -- valid UCB", ": STMTFILE -- valid UCB", ": HTMLFILE -- valid UCB"),
                Files.readAllLines(dir.resolve("sysout/STEP040.stdout"), StandardCharsets.ISO_8859_1));
        assertEquals("50 statements, 262 transactions, each once, every total its lines' sum", Statements.check(dataset(dir, "STATEMNT.PS"), dataset(dir, "TRXFL.SEQ")));
        runs.add(classes);
        Runs.passed(runs.toArray(Path[]::new));
    }

    private static Fixture fixture(String job) throws Exception {
        return Fixture.read(Files.readString(ROOT.resolve("oracle/fixture/carddemo/" + job + ".fixture")));
    }

    private static CobcLane.Setup setup(Path dir, Map<String, List<String>> java) {
        LaneRuns cobc = new LaneRuns("carddemo-java-sources");
        return new CobcLane.Setup(ROOT, Map.of("carddemo", CARDDEMO, "repo", ROOT), CARDDEMO.resolve("app/cbl"), List.of(CARDDEMO.resolve("app/cpy")),
                dir, ROOT.resolve("build/lane-cache"), Duration.ofSeconds(60), (j, d) -> cobcUnload(cobc, j, d), null, java);
    }

    /** A `job:` source: what that job leaves of a dataset in the cobc lane, its records being the same in both lanes when this test passes. */
    private static byte[] cobcUnload(LaneRuns cobc, String job, String dsn) {
        cobc.run(job);
        try {
            Path file = cobc.work(job).resolve("unload/" + dsn);
            return Files.exists(file) ? Files.readAllBytes(file) : null;
        } catch (java.io.IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
    }

    /** The file a dataset of a run is held in, by the end of its name. */
    private static byte[] dataset(Path dir, String suffix) throws java.io.IOException {
        try (var files = Files.walk(dir)) {
            return Files.readAllBytes(files.filter(Files::isRegularFile).filter(f -> f.getFileName().toString().endsWith(suffix)).findFirst().orElseThrow());
        }
    }

    /** A CardDemo program lowered and compiled into `classes`, its copybooks at the fixture's tab widths; its class name. */
    private static String lowered(String name, Path classes, Fixture fixture) throws Exception {
        Path source = CARDDEMO.resolve("app/cbl/" + name + ".cbl");
        if (!Files.exists(source)) source = CARDDEMO.resolve("app/cbl/" + name + ".CBL");
        Parse.Result parse = Parse.file(source, new Copybooks(List.of(CARDDEMO.resolve("app/cpy")), Tabs.widths(fixture)), 8);
        assertEquals(List.of(), parse.errors(), name + " parses");
        Program program = Build.programs(parse, "sha256:" + "0".repeat(64)).get(0).program();
        String className = Lower.className(program);
        Files.writeString(classes.resolve(className + ".java"), Lower.java(program, className).java());
        ByteArrayOutputStream diagnostics = new ByteArrayOutputStream();
        int status = ToolProvider.getSystemJavaCompiler().run(null, null, diagnostics, "-classpath", System.getProperty("java.class.path"), "-d", classes.toString(), classes.resolve(className + ".java").toString());
        assertEquals(0, status, name + " compiles: " + diagnostics.toString(StandardCharsets.UTF_8));
        return className;
    }

    private static final LaneRuns COBC = new LaneRuns("carddemo-java-cobc");

    /**
     * Whether a java lane's stdout line differs from the cobc lane's only as D-005 says: cobc DISPLAYs an
     * overpunched zoned item with a separate trailing sign, where IBM's DISPSIGN(COMPAT) shows its bytes.
     * Each line of the two stdouts must be the same, or the same once each run of digits cobc ends with `+`
     * or `-` is written as COMPAT shows it (the last digit plain, or `p`-`y`, the cobc lane's negative zone).
     */
    private static boolean d005(String job, String line, Path dir) {
        if (!line.startsWith("stdout ")) return false;
        String step = line.split(" ")[1];
        try {
            if (!Files.exists(COBC.work(job).resolve("sysout/" + step + ".stdout"))) COBC.run(job);
            List<String> cobc = Files.readAllLines(COBC.work(job).resolve("sysout/" + step + ".stdout"), StandardCharsets.ISO_8859_1);
            List<String> java = Files.readAllLines(dir.resolve("sysout/" + step + ".stdout"), StandardCharsets.ISO_8859_1);
            if (cobc.size() != java.size()) return false;
            for (int k = 0; k < cobc.size(); k++) if (!cobc.get(k).equals(java.get(k)) && !compat(cobc.get(k)).equals(java.get(k))) return false;
            return true;
        } catch (java.io.IOException e) {
            return false;
        }
    }

    /** A cobc stdout line with each trailing-signed run of digits as DISPSIGN(COMPAT) shows it (D-005). */
    static String compat(String line) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+)([+-])").matcher(line);
        StringBuilder out = new StringBuilder();
        while (m.find()) {
            String digits = m.group(1);
            String last = m.group(2).equals("-") ? String.valueOf((char) (0x70 + digits.charAt(digits.length() - 1) - '0')) : digits.substring(digits.length() - 1);
            m.appendReplacement(out, java.util.regex.Matcher.quoteReplacement(digits.substring(0, digits.length() - 1) + last));
        }
        return m.appendTail(out).toString();
    }

    /** The golden's text for each fixture, by job. */
    private static Map<String, String> sections(String golden) {
        Map<String, String> out = new LinkedHashMap<>();
        String job = null;
        StringBuilder text = new StringBuilder();
        for (String line : golden.split("\n", -1)) {
            if (line.startsWith("# oracle/fixture/carddemo/")) {
                if (job != null) out.put(job, text.toString());
                job = line.replace("# oracle/fixture/carddemo/", "").replace(".fixture", "");
                text.setLength(0);
            } else text.append(line).append('\n');
        }
        if (job != null) out.put(job, text.toString());
        return out;
    }

    /** A report's lines but the java lane's banner and the cobc lane's program-source hashes, which only one lane has. */
    private static List<String> comparable(String report) {
        return report.lines().filter(l -> !l.isBlank() && !l.startsWith("lane java") && !l.startsWith("program ")).toList();
    }
}
