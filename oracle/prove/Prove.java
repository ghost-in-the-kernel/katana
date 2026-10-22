package katana.oracle.prove;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import katana.oracle.compare.Compare;
import katana.oracle.compare.Layout;
import katana.oracle.fixture.Fixture;
import katana.oracle.lane.CobcLane;
import katana.oracle.lane.Gate;
import katana.oracle.lane.Report;

/**
 * 8A.1 (research §48, R-072): a foreign Java port held to its COBOL. The cobc lane runs the fixture twice, through
 * the gate; the java lane runs the port's command as the step's program, under the DD contract (docs/LANES.md); each
 * output dataset is compared by its copybook, and each step's stdout. A port that leaves an output dataset the cobc
 * lane wrote unwritten or empty did not honour the contract (LANE-01, LANE-04): it is refused with a finding, and its
 * datasets are not compared.
 */
public final class Prove {
    /** Where the lanes run: the fixture's root and sources, its programs and copybooks, a working folder. */
    public record Setup(Path root, Map<String, Path> sources, Path programs, List<Path> copybooks, Path work, Duration limit) {}

    /** One output dataset held to the cobc lane's: the same or not, and the comparator's findings. */
    public record Dataset(String dsn, boolean same, List<String> findings) {}

    /** What the port did: refused (and why), or each output dataset's and each step's stdout's verdict. */
    public record Result(List<String> refused, List<Dataset> datasets, Map<String, Boolean> stdout, String text) {
        public boolean same() {
            return refused.isEmpty() && datasets.stream().allMatch(Dataset::same) && !stdout.containsValue(false);
        }
    }

    private Prove() {}

    /** The port, by PGM name to its command, run through the lanes under the fixture. */
    // Decision: R-072
    public static Result run(Fixture fixture, Setup s, Map<String, List<String>> port) throws IOException {
        Path a = s.work().resolve("cobc-a"), b = s.work().resolve("cobc-b"), j = s.work().resolve("port");
        Report ra = CobcLane.run(fixture, lane(s, a, null)), rb = CobcLane.run(fixture, lane(s, b, null));
        Report rj = CobcLane.run(fixture, lane(s, j, port));
        List<String> refused = new ArrayList<>(rj.findings());
        Map<String, Dd> outputs = outputs(fixture);
        for (Map.Entry<String, Dd> o : outputs.entrySet()) {
            Path expected = a.resolve("unload").resolve(o.getKey()), actual = j.resolve("unload").resolve(o.getKey());
            if (Files.exists(expected) && Files.size(expected) > 0 && (!Files.exists(actual) || Files.size(actual) == 0))
                refused.add("the port did not write DD " + o.getValue().name() + " (" + o.getKey() + "), which the COBOL wrote: it does not honour the DD contract (LANE-01, LANE-04)");
        }
        List<Dataset> datasets = new ArrayList<>();
        Map<String, Boolean> stdout = new LinkedHashMap<>();
        if (refused.isEmpty()) {
            Gate.Result gate = Gate.compare(fixture, ra, a, rb, b);
            for (Map.Entry<String, Dd> o : outputs.entrySet()) datasets.add(dataset(gate, s, o.getKey(), o.getValue(), a, j));
            for (Fixture.Step step : fixture.steps()) stdout.put(step.name(), read(a, step).equals(read(j, step)));
        }
        return new Result(refused, datasets, stdout, text(fixture, refused, datasets, stdout));
    }

    /** An output DD: its name, record length, copybook and encoding. */
    record Dd(String name, int lrecl, String copybook, String encoding) {}

    /** Each dataset a step creates (disposition NEW), by its name. */
    static Map<String, Dd> outputs(Fixture fixture) {
        Map<String, Dd> out = new LinkedHashMap<>();
        for (Fixture.Step step : fixture.steps())
            for (Fixture.Dd dd : step.dds()) {
                Map<String, String> at = dd.attributes();
                if (at.getOrDefault("disp", "").startsWith("NEW") && at.containsKey("dsn") && at.containsKey("copybook"))
                    out.put(at.get("dsn"), new Dd(dd.name(), Integer.parseInt(at.get("lrecl")), at.get("copybook"), at.getOrDefault("encoding", "ascii")));
            }
        return out;
    }

    private static Dataset dataset(Gate.Result gate, Setup s, String dsn, Dd dd, Path a, Path j) throws IOException {
        Layout layout = Layout.read(Files.readString(copybook(s, dd.copybook())));
        Compare.Result r = Compare.compare(gate, Compare.Side.fixed(dsn, Files.readAllBytes(a.resolve("unload").resolve(dsn)), dd.lrecl(), dd.encoding()),
                Compare.Side.fixed(dsn, Files.readAllBytes(j.resolve("unload").resolve(dsn)), dd.lrecl(), dd.encoding()), layout, List.of(), Compare.Numproc.NOPFD);
        return new Dataset(dsn, r.same(), r.findings());
    }

    static Path copybook(Setup s, String name) {
        for (Path dir : s.copybooks()) for (String ext : List.of(".cpy", ".CPY", "")) if (Files.exists(dir.resolve(name + ext))) return dir.resolve(name + ext);
        throw new IllegalArgumentException("no copybook " + name + " in " + s.copybooks());
    }

    private static String read(Path work, Fixture.Step step) throws IOException {
        Path f = work.resolve("sysout").resolve(step.name() + ".stdout");
        return Files.exists(f) ? Files.readString(f) : "";
    }

    static CobcLane.Setup lane(Setup s, Path work, Map<String, List<String>> port) {
        return new CobcLane.Setup(s.root(), s.sources(), s.programs(), s.copybooks(), work, s.root().resolve("build/lane-cache"), s.limit(), (x, d) -> null, null, port, false);
    }

    /** The report: refused with why, or each dataset and stdout, SAME or DIFFER with the comparator's findings. */
    private static String text(Fixture fixture, List<String> refused, List<Dataset> datasets, Map<String, Boolean> stdout) {
        StringBuilder b = new StringBuilder("prove " + fixture.steps().get(0).name() + ": the port against cobc -std=ibm, the oracle (R-013)\n");
        refused.forEach(r -> b.append("REFUSED ").append(r).append('\n'));
        for (Dataset d : datasets) {
            b.append(d.same() ? "SAME   " : "DIFFER ").append(d.dsn()).append('\n');
            d.findings().forEach(f -> b.append("  ").append(f).append('\n'));
        }
        stdout.forEach((step, same) -> b.append(same ? "SAME   " : "DIFFER ").append("stdout of ").append(step).append('\n'));
        return b.toString();
    }

    /** `gradle prove --args="<fixture> <PGM>=<command...>"`: the report on stdout, exit 0 when the port is the same. */
    public static void main(String[] args) throws IOException {
        Path root = Path.of("").toAbsolutePath(), fixturePath = Path.of(args[0]);
        Fixture fixture = Fixture.read(Files.readString(fixturePath));
        Path dir = fixturePath.toAbsolutePath().getParent();
        String pgm = args[1].substring(0, args[1].indexOf('='));
        List<String> command = new ArrayList<>(List.of(args[1].substring(args[1].indexOf('=') + 1)));
        for (int k = 2; k < args.length; k++) command.add(args[k]);
        Setup s = new Setup(root, Map.of("repo", root), dir.resolve("cbl"), List.of(dir.resolve("cpy")), root.resolve("build/prove"), Duration.ofSeconds(120));
        Result r = run(fixture, s, Map.of(pgm, command));
        System.out.print(r.text());
        System.exit(r.same() ? 0 : 1);
    }
}
