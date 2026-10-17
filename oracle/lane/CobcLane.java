package katana.oracle.lane;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import katana.oracle.fixture.Content;
import katana.oracle.fixture.Fixture;
import katana.oracle.fixture.FixtureCheck;
import katana.oracle.fixture.Schema;
import katana.profile.Profile;

/**
 * The cobc lane (docs/ROADMAP.md 1.4): runs a fixture's job under GnuCOBOL; with `Setup.java`, the java
 * lane's stub (1.8), the same job with each program step a Java program, under the same DD contract
 * (docs/LANES.md). The catalog starts as the
 * fixture says, each source checked against its hash; each step's DDs are allocated by disposition
 * and bound; programs, IDCAMS, IEFBR14 and IEBGENER run; SORT stops the job with a finding naming
 * phase 1.5 (R-048); COND and an abend bypass later steps as JCL does; each dataset's records are
 * unloaded and hashed at the end. A refused fixture runs nothing.
 */
public final class CobcLane {
    /** Where a run finds what it needs: the repository, the `from=` roots by prefix, program sources, copybooks, its folders, other jobs' datasets, and a clock set outside the fixture (null for the fixture's own, R-009). */
    public record Setup(Path root, Map<String, Path> sources, Path programs, List<Path> copybooks, Path work, Path cache,
                        Duration limit, JobSource jobs, String clock, Map<String, List<String>> java, boolean trace) {
        /** The cobc lane's setup: no Java programs, no trace. */
        public Setup(Path root, Map<String, Path> sources, Path programs, List<Path> copybooks, Path work, Path cache,
                     Duration limit, JobSource jobs, String clock) {
            this(root, sources, programs, copybooks, work, cache, limit, jobs, clock, null, false);
        }

        /** The java lane's setup: these Java programs by PGM, no trace. */
        public Setup(Path root, Map<String, Path> sources, Path programs, List<Path> copybooks, Path work, Path cache,
                     Duration limit, JobSource jobs, String clock, Map<String, List<String>> java) {
            this(root, sources, programs, copybooks, work, cache, limit, jobs, clock, java, false);
        }

        /** This setup in another folder with the programs compiled with `-ftrace` and traced, for coverage (1.9). */
        public Setup traced(Path work) {
            return new Setup(root, sources, programs, copybooks, work, cache, limit, jobs, clock, java, true);
        }
    }

    /** A dataset as another job leaves it, unloaded (a `job:` source). */
    public interface JobSource {
        byte[] records(String job, String dsn);
    }

    private static final Map<String, String> STUBS = Map.of("CEE3ABD", Generated.cee3abd());
    /** The key of the java lane's setup whose command runs `rt/Ksds`, which loads and unloads its indexed datasets (LANE-16). */
    public static final String KSDS = "*KSDS";
    /** The stubs the lowering calls as the services they stand for (CEE3ABD as LE's, research §22), so the java lane runs a fixture that names them. */
    private static final java.util.Set<String> LOWERED_STUBS = java.util.Set.of("CEE3ABD");
    private final Fixture fixture;
    private final Setup setup;
    private final Report report = new Report();
    private final Map<String, String> paths = new HashMap<>();
    private final List<String> tabbedCopybooks = new ArrayList<>();
    private final java.util.function.ToIntFunction<String> widths;
    private Datasets datasets;
    private Programs programs;
    /** The programs the java lane ran, for its coverage. */
    private java.util.Set<String> javaRan = java.util.Set.of();

    private CobcLane(Fixture fixture, Setup setup) {
        this.widths = Tabs.widths(fixture);
        this.fixture = fixture;
        this.setup = setup;
    }

    public static Report run(Fixture fixture, Setup setup) {
        return new CobcLane(fixture, setup).run();
    }

    /** The fixture checked, the profile read, the catalog started, the libraries built, the steps run, the end unloaded. */
    private Report run() {
        report.line("job " + fixture.first("job").values().get(0) + " " + fixture.hash());
        if (setup.java() != null) report.line("lane java (the stub of phase 1.8)");
        List<String> refusals = FixtureCheck.refusals(fixture, FixtureCheck.profiles(setup.root()));
        if (!refusals.isEmpty()) {
            refusals.forEach(r -> report.finding("refused: " + r));
            return report;
        }
        String name = fixture.first("profile").values().get(0);
        Profile profile = Profile.read(Programs.text(setup.root().resolve("profile/" + name + ".profile")));
        report.line("profile " + name + " " + profile.hash());
        if (setup.clock() != null) report.line("clock set outside the fixture: " + setup.clock() + " (R-009; for the gate's own check)");
        try {
            prepare(profile);
            String stopped = variable();
            if (stopped == null && setup.java() != null) stopped = javaLimits();
            if (stopped == null) stopped = Start.catalog(fixture, datasets, setup, paths);
            if (stopped == null && setup.java() == null) stopped = libraries();
            if (stopped != null) report.finding(stopped);
            JavaPrograms lowered = setup.java() == null ? null : new JavaPrograms(setup.java(), setup.work(), setup.limit(), setup.trace());
            ProgramRunner runner = lowered == null ? programs : lowered;
            if (lowered != null) javaRan = lowered.ran();
            new Steps(fixture, datasets, runner, paths, setup.work(), report, setup.clock()).run(stopped != null);
            end();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return report;
    }

    /** A clean work folder, the compiler under the profile, the catalog and the program runner. */
    private void prepare(Profile profile) throws IOException {
        if (Files.exists(setup.work())) {
            try (Stream<Path> files = Files.walk(setup.work())) {
                for (Path p : files.sorted(Comparator.reverseOrder()).toList()) Files.delete(p);
            }
        }
        Files.createDirectories(setup.work().resolve("sysout"));
        Files.createDirectories(setup.work().resolve("inline"));
        List<Path> shadows = Tabs.shadow(setup.copybooks(), setup.work().resolve("shadow"), widths, tabbedCopybooks);
        Compiler compiler = new Compiler(profile, shadows, setup.cache(), setup.limit(), setup.trace() ? List.of("-ftrace") : List.of());
        datasets = new Datasets(setup.work(), compiler, setup.limit(), setup.java() == null ? null : setup.java().get(KSDS));
        programs = new Programs(setup.programs(), compiler, setup.work(), setup.limit(), widths, setup.trace());
    }

    /**
     * What the java lane does not run yet (LANE-05): a called module its setup has no lowered class for (LANE-19), a stub the lowering does
     * not call as its service, and an indexed dataset with no `rt/Ksds` command. A PATH needs nothing of its
     * own: its key is an alternate key of the KSDS it is over, which `rt/Ksds` loads with it (`Start.catalog`).
     */
    private String javaLimits() {
        for (Schema.Line line : fixture.header()) {
            String name = line.values().get(0);
            if (line.keyword().equals("module") && !setup.java().containsKey(name)) return "the java lane's setup has no lowered class for the called module " + name + " (LANE-19)";
            if (line.keyword().equals("stub") && !LOWERED_STUBS.contains(name)) return "the java lane has no stub " + name + " yet (the stub facade, phase 4.4)";
        }
        for (Schema.Line d : fixture.datasets()) {
            String org = d.attributes().get("org");
            if ("KSDS".equals(org) && !setup.java().containsKey(KSDS)) return "the java lane's setup has no " + KSDS + " command for the indexed dataset " + d.values().get(0) + " (LANE-16)";
        }
        return null;
    }

    /** A RECFM=V DD stops both lanes until a fixture needs it: no `COB_VARSEQ_FORMAT` is IBM's RDW (LANE-02). */
    private String variable() {
        for (Fixture.Step step : fixture.steps()) {
            for (Fixture.Dd dd : step.dds()) {
                String recfm = dd.attributes().get("recfm");
                if (recfm != null && recfm.startsWith("V")) return "dd " + dd.name() + " in " + step.name() + " is RECFM=" + recfm + ", not in the lanes yet (LANE-02)";
            }
        }
        return null;
    }

    /** The stubs and called modules on the library path; why the job cannot run, or null. */
    private String libraries() {
        for (Schema.Line line : fixture.header()) {
            String name = line.values().isEmpty() ? "" : line.values().get(0);
            if (line.keyword().equals("stub") && !STUBS.containsKey(name)) return "no stub for " + name + " in the lane";
            if (line.keyword().equals("stub")) programs.stub(name, STUBS.get(name));
            if (line.keyword().equals("module")) {
                String refusal = programs.module(name);
                if (!refusal.isEmpty()) return "module " + name + ": " + refusal;
            }
        }
        return null;
    }

    /** Each program's paragraphs against the ones its trace entered, every step's trace read (1.9). */
    private void coverage() throws IOException {
        StringBuilder traces = new StringBuilder();
        Path dir = setup.work().resolve("trace");
        if (Files.exists(dir)) {
            try (Stream<Path> files = Files.list(dir)) {
                for (Path f : files.sorted().toList()) traces.append(Files.readString(f, java.nio.charset.StandardCharsets.ISO_8859_1)).append('\n');
            }
        }
        // The java lane runs the programs its setup names, a module through its CALL (research §27).
        java.util.Set<String> ran = new java.util.TreeSet<>(programs.sourceHashes().keySet());
        if (setup.java() != null) {
            ran.addAll(javaRan);
            for (Schema.Line line : fixture.header()) if (line.keyword().equals("module")) ran.add(line.values().get(0));
        }
        for (String name : ran) {
            List<String> all = Paragraphs.of(programs.source(name), setup.copybooks(), widths);
            java.util.Set<String> entered = Paragraphs.entered(traces.toString(), name);
            List<String> not = all.stream().filter(p -> !entered.contains(p)).toList();
            report.coverage(name, all.size() - not.size(), all.size(), not);
        }
    }

    /** Every dataset's records at the end, unloaded to work/unload and hashed; then the programs by their sources. */
    private void end() throws IOException {
        Path unload = Files.createDirectories(setup.work().resolve("unload"));
        for (Datasets.Entry entry : new ArrayList<>(datasets.all())) {
            byte[] records = datasets.unload(entry);
            Files.write(unload.resolve(entry.dsn()), records);
            report.dataset(entry.dsn(), entry.lrecl(), records.length / entry.lrecl(), Content.sha256(records));
        }
        List<String> tabbed = new ArrayList<>(programs.tabbed());
        for (String file : tabbedCopybooks) if (programs.copied().contains(file.replaceAll("\\.[^.]*$", "").toUpperCase())) tabbed.add(file);
        tabbed.forEach(f -> report.note("tab in " + f + ", expanded at " + widths.applyAsInt(f) + " columns (R-016; a hypothesis until the Enterprise COBOL rule is read)"));
        programs.sourceHashes().entrySet().stream().sorted(Map.Entry.comparingByKey())
                .forEach(e -> report.line("program " + e.getKey() + " " + e.getValue()));
        if (setup.trace()) coverage();
    }
}
