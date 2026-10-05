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
 * against it until the two runs are identical, on every dataset's logical unload, every SYSOUT DD, every
 * program step's stdout, every step's stderr with the run folder's path replaced by a token, and every
 * step's outcome (R-012). What differs is named: the dataset, the DDs that reach it, how many
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
        differences.addAll(sysouts(workA, workB));
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

    /**
     * Every file the steps wrote to the sysout folder, in either run: SYSOUT DDs, stdout, and stderr with
     * each run's folder path replaced by `<work>` (R-012); the first line that differs is named.
     */
    private static List<String> sysouts(Path workA, Path workB) {
        Set<String> names = new TreeSet<>(names(workA.resolve("sysout")));
        names.addAll(names(workB.resolve("sysout")));
        List<String> differences = new ArrayList<>();
        for (String name : names) {
            List<String> a = sysoutLines(workA, name), b = sysoutLines(workB, name);
            if (a == null || b == null) {
                differences.add(name + " is in one run only");
                continue;
            }
            for (int i = 0; i < Math.max(a.size(), b.size()); i++) {
                if (i >= a.size() || i >= b.size() || !a.get(i).equals(b.get(i))) {
                    differences.add(name + ": line " + (i + 1) + " differs");
                    break;
                }
            }
        }
        return differences;
    }

    private static List<String> names(Path folder) {
        if (!Files.isDirectory(folder)) return List.of();
        try (Stream<Path> files = Files.list(folder)) {
            return files.map(f -> f.getFileName().toString()).toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** A sysout file's lines, null when absent; a stderr file's with the run folder's path as `<work>`. */
    static List<String> sysoutLines(Path work, String name) {
        byte[] bytes = read(work.resolve("sysout").resolve(name));
        if (bytes == null) return null;
        String text = new String(bytes, StandardCharsets.ISO_8859_1);
        if (name.endsWith(".stderr")) text = normalize(text, work);
        return text.lines().toList();
    }

    /** The run folder's path, as given and as resolved, replaced by `<work>`. */
    static String normalize(String text, Path work) {
        try {
            text = text.replace(work.toRealPath().toString(), "<work>");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return text.replace(work.toAbsolutePath().toString(), "<work>");
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
