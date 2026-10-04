package katana.oracle.lane;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import katana.oracle.fixture.Fixture;

/** CardDemo fixtures through the cobc lane, each run once per instance, a `job:` source run on demand. */
final class LaneRuns {
    static final Path ROOT = Path.of(System.getProperty("katana.root", "."));
    private final Map<String, Report> reports = new HashMap<>();
    private final CobcLane.Sort sort;
    private final String folder;

    LaneRuns(CobcLane.Sort sort, String folder) {
        this.sort = sort;
        this.folder = folder;
    }

    /** A fixture through the lane in build/FOLDER/JOB, once. */
    Report run(String job) {
        Report done = reports.get(job);
        if (done != null) return done;
        try {
            Fixture fixture = Fixture.read(Files.readString(ROOT.resolve("oracle/fixture/carddemo/" + job + ".fixture")));
            Path carddemo = ROOT.resolve("corpus/carddemo");
            CobcLane.Setup setup = new CobcLane.Setup(ROOT, Map.of("carddemo", carddemo, "repo", ROOT), carddemo.resolve("app/cbl"),
                    List.of(carddemo.resolve("app/cpy")), ROOT.resolve("build/" + folder + "/" + job), ROOT.resolve("build/lane-cache"),
                    Duration.ofSeconds(60), this::unloaded, sort);
            Report report = CobcLane.run(fixture, setup);
            reports.put(job, report);
            return report;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** A dataset as another fixture's job leaves it: that job run, its unload read. */
    private byte[] unloaded(String job, String dsn) {
        run(job);
        try {
            Path file = ROOT.resolve("build/" + folder + "/" + job + "/unload/" + dsn);
            return Files.exists(file) ? Files.readAllBytes(file) : null;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
