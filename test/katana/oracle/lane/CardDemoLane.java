package katana.oracle.lane;

import katana.support.Javac;
import katana.lower.Plan;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.lower.Lower;
import katana.oracle.fixture.Fixture;
import katana.sir.build.Build;
import katana.sir.types.Program;

/**
 * The CardDemo lanes' test support (J-240): a job's fixture, its programs lowered and compiled for the java lane, the
 * lane's setup with each job's `job:` sources from the cobc lane, and the comparisons the java lane is held by. The
 * lane and structuring tests, the boundary and proof tests, use it; none of them reaches into another test.
 */
final class CardDemoLane {
    static final Path ROOT = LaneRuns.ROOT, CARDDEMO = ROOT.resolve("corpus/carddemo");
    /** The utilities the lanes run themselves; every other PGM, and every module, is a CardDemo program lowered for the job. */
    static final java.util.Set<String> UTILITIES = java.util.Set.of("IDCAMS", "SORT", "IEFBR14", "IEBGENER");

    private CardDemoLane() {}


    /**
     * The java lane's setup for a job: each program its fixture runs (a step's PGM that is no utility, or a
     * module) lowered into `classes` at the fixture's tab widths, launched under the profile's DISPSIGN and
     * DYNAM (J-109, J-186), a CALL finding a module's class on the path (LANE-19); and `rt/Ksds` (LANE-16).
     */
    static Map<String, List<String>> commands(Fixture fixture, Path classes) throws Exception {
        return commands(fixture, classes, false);
    }


    /** The same, each program lowered under 6.1's structuring pass when `structured` (research §35). */
    static Map<String, List<String>> commands(Fixture fixture, Path classes, boolean structured) throws Exception {
        Map<String, String> ibm = katana.profile.Profile.read(Files.readString(ROOT.resolve("profile/ibm.profile"))).ibm();
        String launcher = Path.of(System.getProperty("java.home"), "bin", "java").toString(), path = classes + java.io.File.pathSeparator + System.getProperty("java.class.path");
        java.util.Set<String> programs = new java.util.LinkedHashSet<>();
        for (Fixture.Step step : fixture.steps()) {
            String pgm = step.line().attributes().get("pgm");
            if (pgm != null && !UTILITIES.contains(pgm)) programs.add(pgm);
        }
        for (var line : fixture.header()) if (line.keyword().equals("module")) programs.add(line.values().get(0));
        Map<String, List<String>> commands = new LinkedHashMap<>();
        for (String name : programs) commands.put(name, List.of(launcher, "-XX:TieredStopAtLevel=1", "-XX:+UseSerialGC", "-cp", path, "-Dkatana.dispsign=" + ibm.get("ibm.dispsign"), "-Dkatana.dynam=" + ibm.get("ibm.dynam"), lowered(name, classes, fixture, structured)));
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
    private static String lowered(String name, Path classes, Fixture fixture, boolean structured) throws Exception {
        Path source = CARDDEMO.resolve("app/cbl/" + name + ".cbl");
        if (!Files.exists(source)) source = CARDDEMO.resolve("app/cbl/" + name + ".CBL");
        Parse.Result parse = Parse.file(source, new Copybooks(List.of(CARDDEMO.resolve("app/cpy")), Tabs.widths(fixture)), 8);
        assertEquals(List.of(), parse.errors(), name + " parses");
        Program program = Build.programs(parse, "sha256:" + "0".repeat(64)).get(0).program();
        String className = Lower.className(program);
        Plan plan = structured ? katana.passes.Passes.plan(program, katana.intent.rules.Rules.infer(program)).plan() : Plan.LITERAL;
        Files.writeString(classes.resolve(className + ".java"), Lower.java(program, className, plan).java());
        Javac.compile(name, classes, classes.resolve(className + ".java"));
        return className;
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


    /** A report's lines but the java lane's banner and the cobc lane's program-source hashes, which only one lane has. */
    static List<String> comparable(String report) {
        return report.lines().filter(l -> !l.isBlank() && !l.startsWith("lane java") && !l.startsWith("program ")).toList();
    }
}
