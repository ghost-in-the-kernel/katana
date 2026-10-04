package katana.oracle.lane;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import katana.oracle.fixture.Content;
import katana.oracle.fixture.Fixture;
import katana.oracle.fixture.Schema;

/**
 * The catalog when a job starts (FIXTURE-06): each sequential dataset written and each KSDS loaded in
 * key order, with the alternate keys of the paths over it, from its source after the source's
 * records are checked against the fixture's hash. A GDG base and a dataset a step defines start as
 * nothing.
 */
final class Start {
    private Start() {}

    /** Builds the starting catalog; paths gets each PATH's base. Why the job cannot start, or null. */
    static String catalog(Fixture fixture, Datasets datasets, CobcLane.Setup setup, Map<String, String> paths) {
        Map<String, List<Generated.Key>> alternates = new HashMap<>();
        for (Schema.Line d : fixture.datasets()) {
            Map<String, String> a = d.attributes();
            if (!"PATH".equals(a.get("org"))) continue;
            paths.put(d.values().get(0), a.get("over"));
            alternates.computeIfAbsent(a.get("over"), k -> new ArrayList<>()).add(Generated.Key.of(a.get("keys"), "yes".equals(a.get("unique"))));
        }
        for (Schema.Line d : fixture.datasets()) {
            Map<String, String> a = d.attributes();
            boolean starts = d.keyword().equals("dataset") && !a.containsKey("defined-by") && !List.of("GDG", "PATH").contains(a.get("org"));
            if (!starts) continue;
            String stopped = dataset(d, datasets, setup, alternates.getOrDefault(d.values().get(0), List.of()));
            if (stopped != null) return stopped;
        }
        return null;
    }

    /** One dataset's starting records, checked and placed; why it cannot be, or null. */
    private static String dataset(Schema.Line d, Datasets datasets, CobcLane.Setup setup, List<Generated.Key> alternates) {
        Map<String, String> a = d.attributes();
        String dsn = d.values().get(0);
        int lrecl = Integer.parseInt(a.get("lrecl"));
        boolean ksds = a.get("org").equals("KSDS");
        byte[] records = new byte[0];
        if (a.containsKey("from")) {
            byte[] source = source(a.get("from"), dsn, setup);
            if (source == null) return "dataset " + dsn + ": no source " + a.get("from");
            records = Content.records(source, a.getOrDefault("framing", "fixed"), lrecl, ksds ? a.get("keys") : null);
            String actual = Content.sha256(records);
            if (!actual.equals("sha256:" + a.get("sha256"))) return "dataset " + dsn + ": its source's records are " + actual + ", the fixture says " + a.get("sha256");
        }
        if (!ksds) {
            datasets.sequential(dsn, lrecl, records);
            return null;
        }
        Generated.Shape shape = new Generated.Shape(lrecl, Generated.Key.of(a.get("keys"), true), alternates);
        String complaint = datasets.indexed(dsn, shape, records);
        return complaint.isEmpty() ? null : "dataset " + dsn + ": loading it: " + complaint;
    }

    /** A source's bytes: a file under a `from=` root, or another job's dataset; null when there is none. */
    private static byte[] source(String from, String dsn, CobcLane.Setup setup) {
        String[] where = from.split(":", 2);
        if (where[0].equals("job")) return setup.jobs().records(where[1], dsn);
        var root = setup.sources().get(where[0]);
        try {
            return root == null || !Files.exists(root.resolve(where[1])) ? null : Files.readAllBytes(root.resolve(where[1]));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
