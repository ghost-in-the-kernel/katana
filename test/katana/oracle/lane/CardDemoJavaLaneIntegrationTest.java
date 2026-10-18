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
 * Phases 4.1 and 4.5's CardDemo check (research §22, §26): each CardDemo fixture through the java lane, the
 * programs it runs lowered from it, its indexed datasets `rt/IdxFile`'s, loaded and unloaded by `rt/Ksds`
 * (LANE-16), reports what the cobc lane's golden (oracle/lane/carddemo.golden) holds: every step's return
 * code, every stdout's lines and hash, every dataset's records and hash, every finding. The java lane's
 * report says it is the java lane, and has no program sources to hash; nothing else may differ but D-005's
 * DISPLAY lines. CREASTMT's STEP040, which the cobc lane abends in at the PSA (R-027), runs through the z/OS
 * storage facade (LANE-20) and is held to the java lane's own golden and to its input (`Statements`).
 */
@Tag("integration")
class CardDemoJavaLaneIntegrationTest {
    private static final Path ROOT = LaneRuns.ROOT, CARDDEMO = ROOT.resolve("corpus/carddemo");
    private static final List<String> JOBS = List.of("POSTTRAN", "TRANREPT", "TRANREPT-STUDY", "INTCALC", "PRTCATBL", "PRTCATBL-INLINE", "COMBTRAN", "SORTTEST", "CREASTMT");
    /** The utilities the lanes run themselves; every other PGM, and every module, is a CardDemo program lowered for the job. */
    private static final java.util.Set<String> UTILITIES = java.util.Set.of("IDCAMS", "SORT", "IEFBR14", "IEBGENER");
    /** The java lane's own golden: CREASTMT, whose STEP040 the cobc lane cannot run (R-027), as this lane ran it (research §26). */
    private static final Path JAVA_GOLDEN = ROOT.resolve("oracle/lane/carddemo-java.golden");

    /**
     * Phase 4.5 (research §26): each job's programs lowered from its own fixture, its steps' PGMs and its
     * modules at its tab widths, no program named here; every job reports what the cobc lane's golden holds,
     * D-005's stdout lines aside, but CREASTMT from STEP040, which holds the java lane's golden and its input.
     */
    @Test
    void everyCardDemoJobTheJavaLaneRunsReportsWhatTheCobcLanesGoldenHolds() throws Exception {
        Path work = Files.createDirectories(ROOT.resolve("build/carddemo-java"));
        Map<String, String> golden = sections(Files.readString(ROOT.resolve("oracle/lane/carddemo.golden")));
        List<Path> runs = new ArrayList<>();
        Map<String, List<String>> differ = new LinkedHashMap<>();
        Map<String, String> javaGolden = new LinkedHashMap<>();
        for (String job : JOBS) {
            Path dir = Files.createTempDirectory(work, job), classes = Files.createTempDirectory(work, job + "-classes");
            runs.add(dir);
            runs.add(classes);
            String text = CobcLane.run(fixture(job), setup(dir, commands(fixture(job), classes))).text();
            List<String> cobc = comparable(golden.get(job)), java = comparable(text);
            int at = job.equals("CREASTMT") ? java.indexOf(java.stream().filter(l -> l.startsWith("stdout STEP040")).findFirst().orElseThrow()) : java.size();
            List<String> unexplained = java.subList(0, at).stream().filter(l -> !cobc.contains(l)).filter(l -> !d005(job, l, dir)).toList();
            if (!unexplained.isEmpty() || (at == java.size() && cobc.size() != java.size())) differ.put(job, unexplained);
            if (job.equals("CREASTMT")) {
                javaGolden.put(job, text + String.join("\n", Files.readAllLines(dir.resolve("sysout/STEP040.stdout"), StandardCharsets.ISO_8859_1)) + "\n");
                assertEquals("50 statements, 262 transactions, each once, every total its lines' sum", Statements.check(dataset(dir, "STATEMNT.PS"), dataset(dir, "TRXFL.SEQ")));
            }
        }
        assertEquals(Map.of(), differ, "the java lane's lines the cobc lane's golden does not hold, by job");
        String recorded = javaGolden.entrySet().stream().map(e -> "# oracle/fixture/carddemo/" + e.getKey() + ".fixture\n" + e.getValue()).collect(java.util.stream.Collectors.joining());
        if (Boolean.getBoolean("katana.record")) Files.writeString(JAVA_GOLDEN, recorded);
        assertEquals(Files.readString(JAVA_GOLDEN), recorded, "CREASTMT as the java lane ran it (`-Dkatana.record=true` rewrites it; its diff is the review)");
        Runs.passed(runs.toArray(Path[]::new));
    }

    /**
     * The java lane's setup for a job: each program its fixture runs (a step's PGM that is no utility, or a
     * module) lowered into `classes` at the fixture's tab widths, launched under the profile's DISPSIGN and
     * DYNAM (J-109, J-186), a CALL finding a module's class on the path (LANE-19); and `rt/Ksds` (LANE-16).
     */
    static Map<String, List<String>> commands(Fixture fixture, Path classes) throws Exception {
        Map<String, String> ibm = katana.profile.Profile.read(Files.readString(ROOT.resolve("profile/ibm.profile"))).ibm();
        String launcher = Path.of(System.getProperty("java.home"), "bin", "java").toString(), path = classes + java.io.File.pathSeparator + System.getProperty("java.class.path");
        java.util.Set<String> programs = new java.util.LinkedHashSet<>();
        for (Fixture.Step step : fixture.steps()) {
            String pgm = step.line().attributes().get("pgm");
            if (pgm != null && !UTILITIES.contains(pgm)) programs.add(pgm);
        }
        for (var line : fixture.header()) if (line.keyword().equals("module")) programs.add(line.values().get(0));
        Map<String, List<String>> commands = new LinkedHashMap<>();
        for (String name : programs) commands.put(name, List.of(launcher, "-cp", path, "-Dkatana.dispsign=" + ibm.get("ibm.dispsign"), "-Dkatana.dynam=" + ibm.get("ibm.dynam"), lowered(name, classes, fixture)));
        commands.put(CobcLane.KSDS, List.of(launcher, "-cp", path, "katana.rt.Ksds"));
        return commands;
    }

    static Fixture fixture(String job) throws Exception {
        return Fixture.read(Files.readString(ROOT.resolve("oracle/fixture/carddemo/" + job + ".fixture")));
    }

    static CobcLane.Setup setup(Path dir, Map<String, List<String>> java) {
        // The cobc runs a job's `job:` sources come from, in folders of the caller's own: ProofV1 and this class share
        // this setup, and run at once they removed each other's folders (J-206).
        LaneRuns cobc = new LaneRuns("carddemo-java-sources-" + dir.getParent().getFileName());
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
    static byte[] dataset(Path dir, String suffix) throws java.io.IOException {
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
    static List<String> comparable(String report) {
        return report.lines().filter(l -> !l.isBlank() && !l.startsWith("lane java") && !l.startsWith("program ")).toList();
    }
}
