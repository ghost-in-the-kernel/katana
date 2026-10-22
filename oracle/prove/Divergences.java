package katana.oracle.prove;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import katana.findings.Divergence;
import katana.oracle.fixture.Fixture;
import katana.sir.types.Procedure;
import katana.sir.types.Program;
import katana.sir.types.Statement;
import katana.sir.types.Value;

/**
 * 8A.6's entries (research §53): the cells attribution found, grouped by field; for each, the statement that stores
 * the field (a root, or following from another differing field it reads), its alignment, its cause, its trace on the
 * record's pass and Katana's Java for it; then the statements of a kind's shape the fixture never took across the
 * kind's boundary, possible. What a run gives is passed in: oracle/ lowers nothing (MOD-05).
 */
public final class Divergences {
    /** What the runs gave: Katana's literal Java, its paragraph trace, its events (`KIND PROGRAM PARAGRAPH` with counts), and the fixture's first input dataset's records. */
    public record Runs(String java, String trace, Map<String, Integer> events, String inputDsn, byte[] input, int lrecl) {}

    private static final Set<String> ARITHMETIC = Set.of("ADD", "SUBTRACT", "MULTIPLY", "DIVIDE", "COMPUTE");
    /** The event that shows a kind's boundary was crossed (LANE-22). */
    private static final Map<Divergence.Kind, String> EVENT = Map.of(Divergence.Kind.DV_01, "TRUNCATED", Divergence.Kind.DV_02, "SCALE_CUT", Divergence.Kind.DV_03, "DMAX_CUT");

    private Divergences() {}

    /** The entries: the proven, by field in the order the cells came, then the possible, in the program's order. */
    // Decision: R-072
    public static List<Report.Entry> entries(Program p, String fixturePath, Fixture fixture, Attribute.Result a, List<Align.Line> align, Runs runs) {
        Map<String, List<Attribute.Cell>> byField = new LinkedHashMap<>();
        for (Attribute.Cell c : a.cells()) byField.computeIfAbsent(leaf(c.field()), k -> new ArrayList<>()).add(c);
        Map<String, Object> nodes = Align.nodes(p);
        List<Report.Entry> out = new ArrayList<>();
        Set<Statement> proven = new java.util.HashSet<>();
        Map<String, Statement> storedBy = new LinkedHashMap<>();
        for (String f : byField.keySet()) {
            Map.Entry<String, Object> stored = storing(nodes, p, f);
            if (stored != null) storedBy.put(f, (Statement) stored.getValue());
        }
        for (var f : byField.entrySet()) {
            Map.Entry<String, Object> stored = storing(nodes, p, f.getKey());
            if (stored != null) proven.add((Statement) stored.getValue());
            out.add(proven(f.getKey(), f.getValue(), stored, storedBy, p, fixturePath, fixture, a, align, runs));
        }
        out.sort(java.util.Comparator.comparingInt(Divergences::line));
        for (var n : nodes.entrySet()) if (n.getValue() instanceof Statement s && !proven.contains(s)) {
            Divergence.Kind k = unexercised(p, s, n.getKey(), runs.events());
            if (k != null) out.add(new Report.Entry("possible", receiver(p, s), null, List.of(), null, n.getKey().substring("statement ".length()), origin(s), aligned(align, n.getKey()), k, 0, 0, null,
                    "not taken across the boundary by this fixture: no " + EVENT.get(k) + " at its paragraph", fix(runs.java(), s)));
        }
        return out;
    }

    /** An entry's place in the program: its statement's line, an entry with none last. */
    private static int line(Report.Entry e) {
        return e.origin().matches(".*:\\d+") ? Integer.parseInt(e.origin().replaceAll(".*:", "")) : Integer.MAX_VALUE;
    }

    private static Report.Entry proven(String field, List<Attribute.Cell> cells, Map.Entry<String, Object> stored, Map<String, Statement> storedBy, Program p, String fixturePath,
                                       Fixture fixture, Attribute.Result a, List<Align.Line> align, Runs runs) {
        List<Integer> records = cells.stream().map(Attribute.Cell::record).distinct().sorted().toList();
        int first = records.get(0);
        String proof = "fixture " + fixturePath + " " + fixture.hash() + "; input " + runs.inputDsn() + " record " + first + " " + record(runs, first);
        Divergence.Kind cause = cause(cells, a);
        int reproduced = cause == null ? 0 : (int) cells.stream().filter(c -> a.kinds().get(c).contains(cause)).count();
        if (stored == null) return new Report.Entry("proven", field, cells.get(0).dsn(), records, proof, "no statement stores it: a group or a MOVE of a record", "-", "unaligned", cause, reproduced, cells.size(), null, "-", "-");
        Statement s = (Statement) stored.getValue();
        // It follows from another differing field only if that field's own statement runs before it (J-237: the COMPUTE reads ACCT-BAL before the ADD changes it).
        String follows = reads(p, s).stream().filter(r -> !r.equals(field) && storedBy.containsKey(r) && storedBy.get(r).origin().line() < s.origin().line()).findFirst().orElse(null);
        return new Report.Entry("proven", field, cells.get(0).dsn(), records, proof, stored.getKey().substring("statement ".length()), origin(s), aligned(align, stored.getKey()), cause, reproduced, cells.size(), follows,
                trace(runs.trace(), paragraph(stored.getKey()), first), fix(runs.java(), s));
    }

    /** The kind most of the cells' probes name, null when none does. */
    private static Divergence.Kind cause(List<Attribute.Cell> cells, Attribute.Result a) {
        Map<Divergence.Kind, Integer> votes = new LinkedHashMap<>();
        for (Attribute.Cell c : cells) for (Divergence.Kind k : a.kinds().getOrDefault(c, List.of())) votes.merge(k, 1, Integer::sum);
        return votes.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(null);
    }

    /** The arithmetic or MOVE statement that stores into an item of the field's name, by its node. */
    private static Map.Entry<String, Object> storing(Map<String, Object> nodes, Program p, String field) {
        for (var n : nodes.entrySet()) if (n.getValue() instanceof Statement s && (ARITHMETIC.contains(s.verb()) || s.verb().equals("MOVE")) && field.equals(receiver(p, s))) return n;
        return null;
    }

    /** The field a statement stores into: its receiving operand's item name. */
    private static String receiver(Program p, Statement s) {
        for (Statement.Operand o : s.operands())
            if (o.value() instanceof Value.Field f && o.role().matches(".*(store|-to|giving|minuend|regular-operand|into|result|remainder).*")) return p.items().get(f.item() - 1).name();
        return "-";
    }

    /** The names of the items a statement reads (every field it names but its receiver). */
    private static Set<String> reads(Program p, Statement s) {
        Set<String> out = new TreeSet<>();
        for (Statement.Operand o : s.operands()) fields(p, o.value(), out);
        return out;
    }

    private static void fields(Program p, Value v, Set<String> out) {
        if (v instanceof Value.Field f) out.add(p.items().get(f.item() - 1).name());
        if (v instanceof Value.Expression e) for (Value x : e.operands()) fields(p, x, out);
    }

    /** A kind whose shape the statement has and whose boundary this fixture's run never crossed at its paragraph; null otherwise. */
    private static Divergence.Kind unexercised(Program p, Statement s, String node, Map<String, Integer> events) {
        if (!ARITHMETIC.contains(s.verb())) return null;
        String site = " " + paragraph(node);
        boolean rounded = s.operands().stream().anyMatch(o -> o.value() instanceof Value.Name n && n.text().equals("ROUNDED"));
        boolean divides = s.verb().equals("DIVIDE") || s.operands().stream().anyMatch(o -> divides(o.value()));
        List<Divergence.Kind> shapes = new ArrayList<>(List.of(Divergence.Kind.DV_01));
        if (!rounded && Set.of("COMPUTE", "MULTIPLY", "DIVIDE").contains(s.verb())) shapes.add(Divergence.Kind.DV_02);
        if (divides) shapes.add(Divergence.Kind.DV_03);
        for (Divergence.Kind k : shapes) if (events.keySet().stream().noneMatch(e -> e.startsWith(EVENT.get(k) + " ") && e.endsWith(site))) return k;
        return null;
    }

    private static boolean divides(Value v) {
        return v instanceof Value.Expression e && (e.operator().equals("/") || e.operands().stream().anyMatch(Divergences::divides));
    }

    /** The paragraph a statement's node names: `- -`'s last part for the unnamed lead paragraph, which the trace never names. */
    private static String paragraph(String node) {
        String in = node.substring(node.indexOf(" IN ") + 4);
        return in.equals("PROCEDURE DIVISION") ? "-" : in.replaceFirst("^SECTION ", "");
    }

    private static String aligned(List<Align.Line> align, String node) {
        return align.stream().filter(l -> l.node().equals(node)).findFirst().map(l -> l.place() + " (" + l.state() + ", " + l.evidence() + ")").orElse("unaligned");
    }

    private static String origin(Statement s) {
        return s.origin().file().replaceAll(".*/", "") + ":" + s.origin().line();
    }

    /** An input record's hash. */
    private static String record(Runs runs, int r) {
        int at = (r - 1) * runs.lrecl();
        if (at + runs.lrecl() > runs.input().length) return "(no such input record)";
        return katana.oracle.fixture.Content.sha256(java.util.Arrays.copyOfRange(runs.input(), at, at + runs.lrecl()));
    }

    /** The paragraphs Katana's run entered on the record's pass: from the paragraph's (r-1)-th entry to its r-th; the program's whole path when it is entered other than once a record. */
    static String trace(String trace, String paragraph, int r) {
        if (paragraph.equals("-")) return "the statement is in the program's unnamed lead paragraph, which the trace does not name";
        List<String> entered = trace.lines().filter(l -> l.startsWith("Program-Id:")).map(l -> l.replaceAll(".*(?:Paragraph|Section):\\s+", "")).toList();
        List<Integer> at = new ArrayList<>();
        for (int k = 0; k < entered.size(); k++) if (entered.get(k).equals(paragraph)) at.add(k);
        if (at.size() < r) return "entered " + at.size() + " times, not once a record: the run's path " + entered.subList(0, Math.min(entered.size(), 12));
        int from = r == 1 ? 0 : at.get(r - 2) + 1;
        return String.join(" > ", entered.subList(from, at.get(r - 1) + 1));
    }

    /** Katana's literal Java for the statement: from its provenance comment to the next statement's. */
    static String fix(String java, Statement s) {
        String mark = "// " + origin(s) + " ";
        String[] lines = java.split("\n");
        for (int k = 0; k < lines.length; k++) {
            if (!lines[k].strip().startsWith(mark)) continue;
            int indent = lines[k].length() - lines[k].stripLeading().length();
            StringBuilder b = new StringBuilder(lines[k].strip());
            for (int j = k + 1; j < lines.length && !lines[j].strip().matches("// \\S+\\.(?i:cbl):\\d+ .*"); j++) {
                String l = lines[j].stripTrailing();
                b.append('\n').append(l.length() >= indent && l.substring(0, indent).isBlank() ? l.substring(indent) : l.strip());
            }
            return b.toString();
        }
        return "(no provenance comment " + mark.strip() + " in the lowering)";
    }

    private static String leaf(String path) {
        return path.substring(path.lastIndexOf('.') + 1).replaceAll("\\(\\d+\\)", "");
    }
}
