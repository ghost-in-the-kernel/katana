package katana.oracle.lane;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;
import katana.oracle.fixture.Fixture;

/**
 * The determinism gate (docs/ROADMAP.md 1.6): the cobc lane runs a job twice, and nothing is compared
 * against it until the two runs are identical, on every dataset's logical unload, every program step's
 * stdout and every step's outcome. What differs is named: the dataset, the DDs that reach it, how many
 * records, and in which columns (F-02: a clock pinned to the second leaves the hundredths in TRANFILE
 * column 326).
 */
public final class Gate {
    /** Whether the two runs are identical, and each difference named. */
    public record Result(boolean identical, List<String> differences) {}

    private Gate() {}

    /** Compares two runs of one fixture, each its report and its work folder. */
    public static Result compare(Fixture fixture, Report a, Path workA, Report b, Path workB) {
        List<String> differences = new ArrayList<>();
        Set<String> steps = new TreeSet<>(a.steps().keySet());
        steps.addAll(b.steps().keySet());
        for (String step : steps) {
            if (!String.valueOf(a.steps().get(step)).equals(String.valueOf(b.steps().get(step)))) {
                differences.add("step " + step + ": " + a.steps().get(step) + " then " + b.steps().get(step));
            }
        }
        Set<String> dsns = new TreeSet<>(a.datasets().keySet());
        dsns.addAll(b.datasets().keySet());
        for (String dsn : dsns) {
            String difference = dataset(fixture, dsn, a.lrecls().getOrDefault(dsn, b.lrecls().getOrDefault(dsn, 1)),
                    read(workA.resolve("unload").resolve(dsn)), read(workB.resolve("unload").resolve(dsn)));
            if (difference != null) differences.add(difference);
        }
        differences.addAll(stdouts(workA, workB));
        return new Result(differences.isEmpty(), differences);
    }

    /** What the comparator calls first: the two runs, or a refusal naming what differed. */
    public static void require(Result result) {
        if (!result.identical()) throw new IllegalStateException("the cobc lane is not deterministic on this fixture: " + String.join("; ", result.differences()));
    }

    /** A dataset's two unloads: null when identical, else the DDs, the records that differ and the columns. */
    static String dataset(Fixture fixture, String dsn, int lrecl, byte[] a, byte[] b) {
        if (a == null || b == null) return a == b ? null : "dataset " + dsn + " is in one run only";
        if (java.util.Arrays.equals(a, b)) return null;
        String where = "dataset " + dsn + dds(fixture, dsn);
        if (a.length != b.length) return where + ": " + a.length / lrecl + " records, then " + b.length / lrecl;
        Set<Integer> records = new TreeSet<>(), columns = new TreeSet<>();
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) {
                records.add(i / lrecl + 1);
                columns.add(i % lrecl + 1);
            }
        }
        return where + ": " + records.size() + " of " + a.length / lrecl + " records differ, in column" + (columns.size() > 1 ? "s " : " ") + ranges(columns);
    }

    /** " (DD TRANFILE in STEP15, ...)": the DDs of the fixture that name the dataset. */
    private static String dds(Fixture fixture, String dsn) {
        List<String> dds = new ArrayList<>();
        for (Fixture.Step step : fixture.steps()) {
            for (Fixture.Dd dd : step.dds()) if (dsn.equals(dd.attributes().get("dsn"))) dds.add("DD " + dd.name() + " in " + step.name());
        }
        return dds.isEmpty() ? "" : " (" + String.join(", ", dds) + ")";
    }

    /** Each program step's stdout, line by line: the first line that differs. */
    private static List<String> stdouts(Path workA, Path workB) {
        List<String> differences = new ArrayList<>();
        try (Stream<Path> files = Files.list(workA.resolve("sysout"))) {
            for (Path file : files.filter(f -> f.toString().endsWith(".stdout")).sorted().toList()) {
                List<String> a = Files.readAllLines(file, StandardCharsets.ISO_8859_1);
                Path other = workB.resolve("sysout").resolve(file.getFileName());
                List<String> b = Files.exists(other) ? Files.readAllLines(other, StandardCharsets.ISO_8859_1) : List.of();
                for (int i = 0; i < Math.max(a.size(), b.size()); i++) {
                    if (i >= a.size() || i >= b.size() || !a.get(i).equals(b.get(i))) {
                        differences.add(file.getFileName() + ": line " + (i + 1) + " differs");
                        break;
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return differences;
    }

    /** Sorted numbers as ranges: `1-2, 4`. */
    private static String ranges(Set<Integer> numbers) {
        List<String> ranges = new ArrayList<>();
        Integer start = null, last = null;
        for (int n : numbers) {
            if (last != null && n == last + 1) {
                last = n;
                continue;
            }
            if (start != null) ranges.add(start.equals(last) ? String.valueOf(start) : start + "-" + last);
            start = last = n;
        }
        if (start != null) ranges.add(start.equals(last) ? String.valueOf(start) : start + "-" + last);
        return String.join(", ", ranges);
    }

    private static byte[] read(Path file) {
        try {
            return Files.exists(file) ? Files.readAllBytes(file) : null;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
