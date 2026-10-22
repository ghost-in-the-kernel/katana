package katana.oracle.prove;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import katana.findings.Divergence;
import katana.oracle.compare.Layout;
import katana.oracle.fixture.Fixture;
import katana.oracle.lane.CobcLane;

/**
 * 8A.5's attribution (research §52, R-072): each cell (dataset, record, field) where a foreign port's output differs
 * from the cobc lane's, and for each divergence kind whether its probe (Katana's literal Java with that one rule
 * broken, LANE-23) writes the port's bytes there. Kinds are ranked by the cells they reproduce; a cell no probe
 * reproduces is unattributed. The probes' commands come from the caller: oracle/ does not lower (MOD-05).
 */
public final class Attribute {
    /** A differing cell: the dataset, the record (from 1), the field's path, and the two lanes' bytes as text. */
    public record Cell(String dsn, int record, String field, String cobol, String port) {}

    /** A kind and the cells its probe reproduces, of all those to explain. */
    public record Rank(Divergence.Kind kind, int reproduced, int cells) {}

    /** The cells to explain, the kinds that explain any ranked first to last, the cells none explains, each cell's kinds, and the text. */
    public record Result(List<Cell> cells, List<Rank> ranks, List<Cell> unattributed, Map<Cell, List<Divergence.Kind>> kinds, String text) {
        public Divergence.Kind first() {
            return ranks.isEmpty() ? null : ranks.get(0).kind();
        }
    }

    private Attribute() {}

    /** The port, then each kind's probe, through the java lane; each differing cell put to every probe. */
    // Decision: R-072
    public static Result attribute(Fixture fixture, Prove.Setup s, Map<String, List<String>> port, Map<Divergence.Kind, Map<String, List<String>>> probes) throws IOException {
        Path cobol = run(fixture, s, "cobc", null), ported = run(fixture, s, "port", port);
        List<Cell> cells = new ArrayList<>();
        Map<Divergence.Kind, Path> runs = new LinkedHashMap<>();
        for (var p : probes.entrySet()) runs.put(p.getKey(), run(fixture, s, "probe-" + p.getKey().code, p.getValue()));
        Map<Divergence.Kind, Integer> reproduced = new LinkedHashMap<>();
        List<Cell> unattributed = new ArrayList<>();
        Map<Cell, List<Divergence.Kind>> kinds = new LinkedHashMap<>();
        for (var o : Prove.outputs(fixture).entrySet()) {
            Layout layout = Layout.read(Files.readString(Prove.copybook(s, o.getValue().copybook())));
            byte[] a = unload(cobol, o.getKey()), b = unload(ported, o.getKey());
            for (Cell c : differing(o.getKey(), o.getValue().lrecl(), layout, a, b)) {
                cells.add(c);
                kinds.put(c, new ArrayList<>());
                boolean any = false;
                for (var r : runs.entrySet()) {
                    if (!c.port().equals(cell(unload(r.getValue(), o.getKey()), o.getValue().lrecl(), layout, c))) continue;
                    reproduced.merge(r.getKey(), 1, Integer::sum);
                    kinds.get(c).add(r.getKey());
                    any = true;
                }
                if (!any) unattributed.add(c);
            }
        }
        List<Rank> ranks = new ArrayList<>();
        reproduced.forEach((k, n) -> ranks.add(new Rank(k, n, cells.size())));
        ranks.sort(Comparator.comparingInt(Rank::reproduced).reversed());
        return new Result(cells, ranks, unattributed, kinds, text(cells, ranks, unattributed));
    }

    /** A java-lane run of the commands (`null`, the cobc lane) under the fixture, in its own folder. */
    private static Path run(Fixture fixture, Prove.Setup s, String folder, Map<String, List<String>> commands) {
        Path work = s.work().resolve(folder);
        CobcLane.run(fixture, Prove.lane(s, work, commands));
        return work;
    }

    private static byte[] unload(Path work, String dsn) throws IOException {
        Path f = work.resolve("unload").resolve(dsn);
        return Files.exists(f) ? Files.readAllBytes(f) : new byte[0];
    }

    /** Every (record, field) whose bytes the two outputs hold otherwise. */
    private static List<Cell> differing(String dsn, int lrecl, Layout layout, byte[] a, byte[] b) {
        List<Cell> out = new ArrayList<>();
        for (int r = 0; r * lrecl < Math.max(a.length, b.length); r++)
            for (Layout.Field f : layout.fields()) {
                String x = text(a, r * lrecl + f.offset(), f.length()), y = text(b, r * lrecl + f.offset(), f.length());
                if (!x.equals(y)) out.add(new Cell(dsn, r + 1, f.path(), x, y));
            }
        return out;
    }

    private static String cell(byte[] records, int lrecl, Layout layout, Cell c) {
        Layout.Field f = layout.fields().stream().filter(x -> x.path().equals(c.field())).findFirst().orElseThrow();
        return text(records, (c.record() - 1) * lrecl + f.offset(), f.length());
    }

    private static String text(byte[] b, int at, int length) {
        return at + length <= b.length ? new String(Arrays.copyOfRange(b, at, at + length), java.nio.charset.StandardCharsets.ISO_8859_1) : "(none)";
    }

    private static String text(List<Cell> cells, List<Rank> ranks, List<Cell> unattributed) {
        StringBuilder b = new StringBuilder("attribution: " + cells.size() + " cells differ from the cobc lane (R-013)\n");
        for (Rank r : ranks) b.append("  ").append(r.kind().code).append(' ').append(r.kind().title).append(": reproduces ").append(r.reproduced()).append(" of ").append(r.cells()).append('\n');
        if (!unattributed.isEmpty()) b.append("  unattributed: ").append(unattributed.size()).append(" cells, first ").append(unattributed.get(0)).append('\n');
        return b.toString();
    }
}
