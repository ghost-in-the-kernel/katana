package katana.oracle.nist;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Katana's NIST golden against the published summary (R-047). The golden (programs.golden) is what
 * each program's own footer reports under Katana's profile; the published rows (published.summary)
 * are the study's run under GnuCOBOL's harness. RECONCILIATION.md names every program whose outcome
 * differs, with one reason from a fixed list. Each reason is a rule that turns Katana's outcome into
 * the published one, or, for a program cobc treats differently, states the published outcome; the
 * check applies them all and rebuilds the published rows. Any row that does not come out exact is a
 * difference nobody explained, and fails.
 */
public final class Reconciliation {
    /** One run of one program: how it ended (ok, compile, timeout) and its counts. */
    public record Run(String module, String program, int run, String outcome, int total, int pass, int fail, int deleted, int inspect) {
        Run with(String newOutcome, int t, int p, int f, int d, int i) {
            return new Run(module, program, run, newOutcome, t, p, f, d, i);
        }
    }

    /** One reconciliation line: a program's run, the reason, and the published outcome when a rule cannot derive it. */
    public record Line(String program, int run, String reason, Run published) {}

    /** The reasons a published outcome differs from Katana's; the first four are rules, the rest state the published outcome. */
    public static final Set<String> RULES = Set.of("inspection-as-pass", "deleted-as-pass", "none-as-one", "runs-summed");
    public static final Set<String> STATED = Set.of("cobc-rejects", "cobc-behaviour", "source-altered", "not-supported");

    private Reconciliation() {}

    /** A golden line: "MODULE PROGRAM RUN OUTCOME total pass fail deleted inspect". */
    public static Run run(String line) {
        String[] f = line.trim().split("\\s+");
        return new Run(f[0], f[1], Integer.parseInt(f[2]), f[3], Integer.parseInt(f[4]), Integer.parseInt(f[5]),
                Integer.parseInt(f[6]), Integer.parseInt(f[7]), Integer.parseInt(f[8]));
    }

    public static List<Run> golden(String text) {
        return text.lines().filter(l -> !l.isBlank() && !l.startsWith("#")).map(Reconciliation::run).toList();
    }

    /** The table rows of RECONCILIATION.md: | Program | Run | Reason | Published | Why |. */
    public static List<Line> lines(String markdown) {
        List<Line> lines = new ArrayList<>();
        for (String row : markdown.lines().toList()) {
            if (!row.startsWith("| ") || row.startsWith("| Program") || row.startsWith("| ---")) continue;
            String[] c = row.split("\\|");
            String program = c[1].trim();
            int run = Integer.parseInt(c[2].trim());
            String reason = c[3].trim().replace("`", "");
            String published = c[4].trim().replace("`", "");
            Run stated = published.equals("-") ? null : run("X " + program + " " + run + " " + published);
            lines.add(new Line(program, run, reason, stated));
        }
        return lines;
    }

    /** What is unexplained: empty when the reconciled golden gives every published row exactly. */
    public static List<String> check(List<Run> golden, List<Line> lines, String published) {
        List<String> problems = new ArrayList<>();
        Map<String, Line> byRun = new LinkedHashMap<>();
        for (Line line : lines) {
            if (!RULES.contains(line.reason()) && !STATED.contains(line.reason())) problems.add(line.program() + ": unknown reason " + line.reason());
            if (STATED.contains(line.reason()) && line.published() == null) problems.add(line.program() + ": " + line.reason() + " must state the published outcome");
            if (byRun.put(line.program() + " " + line.run(), line) != null) problems.add(line.program() + " run " + line.run() + " is reconciled twice");
        }
        List<Run> reconciled = new ArrayList<>();
        Map<String, Run> previous = new LinkedHashMap<>();
        for (Run r : golden) {
            Line line = byRun.remove(r.program() + " " + r.run());
            Run p = line == null ? r : apply(line, r, previous.get(r.program()));
            reconciled.add(p);
            previous.put(r.program(), r);
        }
        byRun.keySet().forEach(k -> problems.add(k + " is reconciled but is not in the golden"));
        Map<String, String> rows = rows(reconciled);
        for (String expected : published.lines().filter(l -> !l.isBlank() && !l.startsWith("#")).toList()) {
            String module = expected.split(" ")[0];
            String got = rows.getOrDefault(module, module + " (no runs)");
            if (!got.equals(expected)) problems.add("published " + expected + " but the reconciled golden gives " + got);
        }
        return problems;
    }

    /** One reason applied to one run: a rule's change, or the published outcome as stated. */
    static Run apply(Line line, Run r, Run before) {
        return switch (line.reason()) {
            case "inspection-as-pass" -> r.with(r.outcome(), r.total(), r.pass() + r.inspect(), r.fail(), r.deleted(), 0);
            case "deleted-as-pass" -> r.with(r.outcome(), r.total(), r.pass() + r.deleted(), r.fail(), 0, r.inspect());
            case "none-as-one" -> r.with("ok", 1, 1, 0, 0, 0);
            case "runs-summed" -> r.with(r.outcome(), r.total() + before.total(), r.pass() + before.pass(), r.fail() + before.fail(),
                    r.deleted() + before.deleted(), r.inspect() + before.inspect());
            default -> line.published() == null ? r : r.with(line.published().outcome(), line.published().total(), line.published().pass(),
                    line.published().fail(), line.published().deleted(), line.published().inspect());
        };
    }

    /** Each module's row, in the published summary's form: programs, executed, errors, crashes, then the counts. */
    public static Map<String, String> rows(List<Run> runs) {
        Map<String, int[]> sums = new LinkedHashMap<>();
        for (Run r : runs) {
            int[] s = sums.computeIfAbsent(r.module(), m -> new int[9]);
            s[0]++;
            switch (r.outcome()) {
                case "compile" -> s[2]++;
                case "timeout" -> s[3]++;
                default -> s[1]++;
            }
            s[4] += r.pass(); s[5] += r.fail(); s[6] += r.deleted(); s[7] += r.inspect(); s[8] += r.total();
        }
        Map<String, String> rows = new LinkedHashMap<>();
        sums.forEach((m, s) -> rows.put(m, m + " " + s[0] + " " + s[1] + " " + s[2] + " " + s[3] + " " + s[4] + " " + s[5] + " " + s[6] + " " + s[7] + " " + s[8]));
        return rows;
    }
}
