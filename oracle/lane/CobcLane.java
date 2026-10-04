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
 * The cobc lane (docs/ROADMAP.md 1.4): runs a fixture's job under GnuCOBOL. The catalog starts as the
 * fixture says, each source checked against its hash; each step's DDs are allocated by disposition
 * and bound; programs, IDCAMS, IEFBR14 and IEBGENER run; SORT stops the job with a finding naming
 * phase 1.5 (R-048); COND and an abend bypass later steps as JCL does; each dataset's records are
 * unloaded and hashed at the end. A refused fixture runs nothing.
 */
public final class CobcLane {
    /** Where a run finds what it needs: the repository, the `from=` roots by prefix, program sources, copybooks, its folders, and other jobs' datasets. */
    public record Setup(Path root, Map<String, Path> sources, Path programs, List<Path> copybooks, Path work, Path cache,
                        Duration limit, JobSource jobs, Sort sort) {}

    /** How SORT steps run (R-010 weighs the two): Katana's Java sort, or a COBOL program generated from the cards. */
    public enum Sort { JAVA, COBOL }

    /** A dataset as another job leaves it, unloaded (a `job:` source). */
    public interface JobSource {
        byte[] records(String job, String dsn);
    }

    private static final Map<String, String> STUBS = Map.of("CEE3ABD", Generated.cee3abd());
    private final Fixture fixture;
    private final Setup setup;
    private final Report report = new Report();
    private final Map<String, String> paths = new HashMap<>();
    private final List<String> tabbedCopybooks = new ArrayList<>();
    private Datasets datasets;
    private Programs programs;

    private CobcLane(Fixture fixture, Setup setup) {
        this.fixture = fixture;
        this.setup = setup;
    }

    public static Report run(Fixture fixture, Setup setup) {
        return new CobcLane(fixture, setup).run();
    }

    /** The fixture checked, the profile read, the catalog started, the libraries built, the steps run, the end unloaded. */
    private Report run() {
        report.line("job " + fixture.first("job").values().get(0) + " " + fixture.hash());
        List<String> refusals = FixtureCheck.refusals(fixture, FixtureCheck.profiles(setup.root()));
        if (!refusals.isEmpty()) {
            refusals.forEach(r -> report.finding("refused: " + r));
            return report;
        }
        String name = fixture.first("profile").values().get(0);
        Profile profile = Profile.read(Programs.text(setup.root().resolve("profile/" + name + ".profile")));
        report.line("profile " + name + " " + profile.hash());
        try {
            prepare(profile);
            String stopped = Start.catalog(fixture, datasets, setup, paths);
            if (stopped == null) stopped = libraries();
            if (stopped != null) report.finding(stopped);
            new Steps(fixture, datasets, programs, paths, setup.work(), report, setup.sort()).run(stopped != null);
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
        List<Path> shadows = Tabs.shadow(setup.copybooks(), setup.work().resolve("shadow"), tabbedCopybooks);
        Compiler compiler = new Compiler(profile, shadows, setup.cache(), setup.limit());
        datasets = new Datasets(setup.work(), compiler, setup.limit());
        programs = new Programs(setup.programs(), compiler, setup.work(), setup.limit());
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

    /** Every dataset's records at the end, unloaded to work/unload and hashed; then the programs by their sources. */
    private void end() throws IOException {
        Path unload = Files.createDirectories(setup.work().resolve("unload"));
        for (Datasets.Entry entry : new ArrayList<>(datasets.all())) {
            byte[] records = datasets.unload(entry);
            Files.write(unload.resolve(entry.dsn()), records);
            report.dataset(entry.dsn(), records.length / entry.lrecl(), Content.sha256(records));
        }
        List<String> tabbed = new ArrayList<>(programs.tabbed());
        for (String file : tabbedCopybooks) if (programs.copied().contains(file.replaceAll("\\.[^.]*$", "").toUpperCase())) tabbed.add(file);
        tabbed.forEach(f -> report.note("tab in " + f + ", expanded under the 8-column assumption (R-016; a hypothesis until IBM's text is read)"));
        programs.sourceHashes().entrySet().stream().sorted(Map.Entry.comparingByKey())
                .forEach(e -> report.line("program " + e.getKey() + " " + e.getValue()));
    }
}
