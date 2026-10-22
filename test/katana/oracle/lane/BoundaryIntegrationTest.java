package katana.oracle.lane;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.oracle.compare.Layout;
import katana.oracle.fixture.Content;
import katana.oracle.fixture.Fixture;
import katana.oracle.prove.Boundary;
import katana.sir.build.Build;
import katana.sir.types.Program;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * 8A.3's check (research §50): CardDemo's jobs with arithmetic on their data, run on the sample fixture and on
 * boundary inputs from the IR, with `rt/Events` counting each size error, truncation, dropped sign and dmax cut
 * by paragraph. The boundary run enters every paragraph the sample does and drives the events the sample does not;
 * on it, the java lane reports what the cobc lane does, and structured what literal does.
 */
@Tag("integration")
class BoundaryIntegrationTest {
    private static final Path ROOT = LaneRuns.ROOT, CARDDEMO = ROOT.resolve("corpus/carddemo");
    /** The jobs whose programs do arithmetic on their input's fields: CBTRN02C's postings, CBACT04C's interest, CBTRN03C's totals. */
    private static final List<String> JOBS = katana.oracle.fixture.CardDemoJobs.held(ROOT, List.of("POSTTRAN", "INTCALC", "TRANREPT-STUDY"), Map.of(
            "POSTTRAN-STUDY", "the study's reading of POSTTRAN (J-183)", "TRANREPT", "stops at its first step (J-035); TRANREPT-STUDY reaches CBTRN03C",
            "CREASTMT", "its arithmetic sums its own statement lines, not an input field (CBSTM03A)", "PRTCATBL", "no arithmetic on its data",
            "PRTCATBL-INLINE", "no arithmetic on its data", "COMBTRAN", "a sort, no program arithmetic", "SORTTEST", "a sort, no program arithmetic"));

    /** For each job: its boundary fixture written, the four runs, coverage and events compared, the lanes and the passes held alike. */
    @Test
    void boundaryInputsReachWhatTheSampleDoesAndDriveWhatItDoesNot() throws Exception {
        Path work = Files.createDirectories(ROOT.resolve("build/boundary"));
        Map<String, List<String>> problems = new LinkedHashMap<>();
        Map<String, Integer> news = new TreeMap<>();
        List<String> report = new ArrayList<>();
        for (String job : JOBS) {
            Fixture sample = CardDemoJavaLaneIntegrationTest.fixture(job);
            Path gen = work.resolve(job + "/carddemo");
            Fixture boundary = boundary(job, sample, gen, report);
            Run s = java(job, sample, CARDDEMO, work.resolve(job + "/sample"), false), b = java(job, boundary, gen, work.resolve(job + "/boundary"), false);
            Run st = java(job, boundary, gen, work.resolve(job + "/structured"), true);
            List<String> wrong = new ArrayList<>();
            Set<String> lost = new TreeSet<>(s.paragraphs);
            lost.removeAll(b.paragraphs);
            if (!lost.isEmpty()) wrong.add("paragraphs the sample enters and the boundary run does not: " + lost);
            for (String e : b.events.keySet()) if (!s.events.containsKey(e)) news.merge(e.split(" ")[0], 1, Integer::sum);
            report.add(job + " sample events " + s.events + "; boundary events " + b.events);
            if (!b.report.equals(st.report)) wrong.add("structured reports otherwise than literal on the boundary data");
            if (!b.traces.equals(st.traces)) wrong.add("structured traces otherwise than literal on the boundary data");
            wrong.addAll(lanes(job, boundary, gen, work.resolve(job + "/cobc"), b));
            if (!wrong.isEmpty()) problems.put(job, wrong);
        }
        report.forEach(System.out::println);
        System.out.println("events at sites the sample never reaches, by kind: " + news);
        assertEquals(Map.of(), problems);
        for (String kind : List.of("SIZE_ERROR", "TRUNCATED", "DMAX_CUT")) assertTrue(news.getOrDefault(kind, 0) > 0, kind + " driven where the sample does not: " + news);
    }

    /** A run's report, paragraphs entered, traces and events by `KIND PROGRAM PARAGRAPH` with their counts. */
    private record Run(Path dir, String report, Set<String> paragraphs, Map<String, String> traces, Map<String, Integer> events) {}

    /** A job in the java lane, literal or structured, traced, with `rt/Events` writing into the run's folder: its run. */
    private static Run java(String job, Fixture fixture, Path carddemo, Path dir, boolean structured) throws Exception {
        Files.createDirectories(dir);
        Path classes = Files.createDirectories(dir.resolve("classes")), run = Files.createDirectories(dir.resolve("run")), events = dir.resolve("events.txt");
        Files.deleteIfExists(events);
        Map<String, List<String>> commands = new LinkedHashMap<>();
        CardDemoJavaLaneIntegrationTest.commands(fixture, classes, structured).forEach((pgm, c) -> {
            List<String> with = new ArrayList<>(c);
            with.add(1, "-Dkatana.events=" + events);
            commands.put(pgm, with);
        });
        String text = CobcLane.run(fixture, setup(carddemo, run, commands).traced(run)).text();
        Set<String> paragraphs = new TreeSet<>();
        Map<String, String> traces = new TreeMap<>();
        Path trace = run.resolve("trace");
        if (Files.exists(trace)) try (var files = Files.list(trace)) {
            for (Path f : files.sorted().toList()) {
                String t = Files.readString(f, StandardCharsets.ISO_8859_1);
                traces.put(f.getFileName().toString(), t);
                t.lines().filter(l -> l.startsWith("Program-Id:")).forEach(l -> paragraphs.add(l.replaceAll("\\s+", " ")));
            }
        }
        Map<String, Integer> counted = new TreeMap<>();
        if (Files.exists(events)) for (String l : Files.readAllLines(events)) counted.merge(l.substring("Event: ".length()), 1, Integer::sum);
        return new Run(run, text, paragraphs, traces, counted);
    }

    /** The boundary fixture: each `carddemo:` data file whose copybook holds a field the job's arithmetic names, driven to its boundaries under `gen`. */
    private static Fixture boundary(String job, Fixture sample, Path gen, List<String> report) throws Exception {
        Files.createDirectories(gen.resolve("app"));
        copy(CARDDEMO.resolve("app/jcl"), gen.resolve("app/jcl"));
        Set<String> names = Boundary.names(programs(sample));
        String text = Files.readString(ROOT.resolve("oracle/fixture/carddemo/" + job + ".fixture"));
        for (var line : sample.datasets()) {
            String from = line.attributes().get("from");
            if (from == null || !from.startsWith("carddemo:")) continue;
            String path = from.substring("carddemo:".length()), copybook = copybook(sample, line.values().get(0));
            Path source = CARDDEMO.resolve(path), target = gen.resolve(path);
            Files.createDirectories(target.getParent());
            int lrecl = Integer.parseInt(line.attributes().get("lrecl"));
            List<Layout.Field> targets = copybook == null ? List.of() : Boundary.targets(layout(copybook), names);
            if (targets.isEmpty() || !"lines".equals(line.attributes().get("framing"))) {
                Files.copy(source, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                continue;
            }
            byte[] records = Content.records(Files.readAllBytes(source), "lines", lrecl, null);
            byte[] driven = Boundary.perturb(records, lrecl, targets);
            StringBuilder lines = new StringBuilder();
            for (int r = 0; r * lrecl < driven.length; r++) lines.append(new String(driven, r * lrecl, lrecl, StandardCharsets.ISO_8859_1)).append('\n');
            Files.write(target, lines.toString().getBytes(StandardCharsets.ISO_8859_1));
            String sha = Content.sha256(Content.records(Files.readAllBytes(target), "lines", lrecl, line.attributes().get("keys"))).replace("sha256:", "");
            text = text.replace("sha256=" + line.attributes().get("sha256"), "sha256=" + sha);
            report.add(job + " " + line.values().get(0) + ": " + targets.stream().map(Layout.Field::path).toList());
        }
        Files.writeString(gen.resolve(job + ".fixture"), text);
        return Fixture.read(text);
    }

    /** The cobc lane on the boundary fixture against the java lane's report: the same lines, D-005's DISPLAY form aside. */
    private static List<String> lanes(String job, Fixture fixture, Path gen, Path dir, Run java) throws Exception {
        Path run = Files.createDirectories(dir);
        String cobc = CobcLane.run(fixture, setup(gen, run, null).traced(run)).text();
        List<String> a = CardDemoJavaLaneIntegrationTest.comparable(cobc), b = CardDemoJavaLaneIntegrationTest.comparable(java.report);
        List<String> out = new ArrayList<>();
        for (String l : b) if (!a.contains(l) && !(l.startsWith("stdout ") && compat(run, java.dir, l.split(" ")[1]))) out.add("java lane, not cobc: " + l);
        for (String l : a) if (!b.contains(l) && !(l.startsWith("stdout ") && compat(run, java.dir, l.split(" ")[1]))) out.add("cobc lane, not java: " + l);
        return out;
    }

    /** Whether a step's two stdouts are the same line for line, a cobc line also read as DISPSIGN(COMPAT) shows it (D-005). */
    private static boolean compat(Path cobc, Path java, String step) {
        try {
            List<String> c = Files.readAllLines(cobc.resolve("sysout/" + step + ".stdout"), StandardCharsets.ISO_8859_1), j = Files.readAllLines(java.resolve("sysout/" + step + ".stdout"), StandardCharsets.ISO_8859_1);
            if (c.size() != j.size()) return false;
            for (int k = 0; k < c.size(); k++) if (!c.get(k).equals(j.get(k)) && !CardDemoJavaLaneIntegrationTest.compat(c.get(k)).equals(j.get(k))) return false;
            return true;
        } catch (java.io.IOException e) {
            return false;
        }
    }

    private static CobcLane.Setup setup(Path carddemo, Path dir, Map<String, List<String>> java) {
        LaneRuns cobc = new LaneRuns("boundary-sources");
        return new CobcLane.Setup(ROOT, Map.of("carddemo", carddemo, "repo", ROOT), CARDDEMO.resolve("app/cbl"), List.of(CARDDEMO.resolve("app/cpy")),
                dir, ROOT.resolve("build/lane-cache"), Duration.ofSeconds(120), (j, d) -> {
                    cobc.run(j);
                    try {
                        Path f = cobc.work(j).resolve("unload/" + d);
                        return Files.exists(f) ? Files.readAllBytes(f) : null;
                    } catch (java.io.IOException e) {
                        throw new java.io.UncheckedIOException(e);
                    }
                }, null, java);
    }

    /** The programs a job runs (its steps' PGMs that are no utility, and its modules), parsed and built. */
    private static List<Program> programs(Fixture fixture) throws Exception {
        List<Program> out = new ArrayList<>();
        Set<String> names = new TreeSet<>();
        for (Fixture.Step step : fixture.steps()) {
            String pgm = step.line().attributes().get("pgm");
            if (pgm != null && !Set.of("IDCAMS", "SORT", "IEFBR14", "IEBGENER").contains(pgm)) names.add(pgm);
        }
        for (var line : fixture.header()) if (line.keyword().equals("module")) names.add(line.values().get(0));
        for (String name : names) {
            Path source = CARDDEMO.resolve("app/cbl/" + name + ".cbl");
            if (!Files.exists(source)) source = CARDDEMO.resolve("app/cbl/" + name + ".CBL");
            out.add(Build.programs(Parse.file(source, new Copybooks(List.of(CARDDEMO.resolve("app/cpy")), Tabs.widths(fixture)), 8), "sha256:" + "0".repeat(64)).get(0).program());
        }
        return out;
    }

    /** The copybook a DD of any step names for the dataset. */
    private static String copybook(Fixture fixture, String dsn) {
        for (Fixture.Step step : fixture.steps()) for (Fixture.Dd dd : step.dds()) if (dsn.equals(dd.attributes().get("dsn")) && dd.attributes().get("copybook") != null && !dd.attributes().get("copybook").equals("-")) return dd.attributes().get("copybook");
        return null;
    }

    private static Layout layout(String copybook) throws Exception {
        Path f = CARDDEMO.resolve("app/cpy/" + copybook + ".cpy");
        if (!Files.exists(f)) f = CARDDEMO.resolve("app/cpy/" + copybook + ".CPY");
        return Layout.read(Files.readString(f, StandardCharsets.ISO_8859_1));
    }

    private static void copy(Path from, Path to) throws Exception {
        try (var walk = Files.walk(from)) {
            for (Path p : walk.toList()) {
                Path t = to.resolve(from.relativize(p).toString());
                if (Files.isDirectory(p)) Files.createDirectories(t);
                else Files.copy(p, t, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
}
