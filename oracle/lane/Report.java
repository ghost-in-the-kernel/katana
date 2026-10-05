package katana.oracle.lane;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * What a lane run did, as lines a golden can hold: the fixture and profile by hash, each step's
 * outcome, what each program step printed, each dataset's records at the end by hash, the programs
 * by their sources' hashes, and the findings that stopped it.
 */
public final class Report {
    private final List<String> lines = new ArrayList<>();
    private final Map<String, String> steps = new LinkedHashMap<>();
    private final Map<String, String> datasets = new LinkedHashMap<>();
    private final Map<String, Integer> lrecls = new LinkedHashMap<>();
    private final List<String> findings = new ArrayList<>();
    private final List<String> notes = new ArrayList<>();
    private final Map<String, Coverage> coverage = new LinkedHashMap<>();

    /** A program's paragraphs (and sections): how many, how many a trace entered, and the ones it did not. */
    public record Coverage(int entered, int total, List<String> notEntered) {}

    void line(String line) {
        lines.add(line);
    }

    void step(String name, String outcome) {
        steps.put(name, outcome);
        lines.add("step " + name + " " + outcome);
    }

    void dataset(String dsn, int lrecl, int records, String sha256) {
        datasets.put(dsn, sha256);
        lrecls.put(dsn, lrecl);
        lines.add("dataset " + dsn + " records=" + records + " " + sha256);
    }

    void finding(String finding) {
        findings.add(finding);
        lines.add("finding " + finding);
    }

    /** A finding that does not stop the job: a tab expanded (R-016), equal sort keys kept in order (R-026). */
    void note(String note) {
        notes.add(note);
        lines.add("note " + note);
    }

    public List<String> notes() {
        return notes;
    }

    /** Each step's outcome by name: `rc=4`, `abend U0999 rc=231`, `bypassed ...`, `not run ...`. */
    public Map<String, String> steps() {
        return steps;
    }

    /** Each dataset's records at the end, by name, as `sha256:...`. */
    public Map<String, String> datasets() {
        return datasets;
    }

    void coverage(String program, int entered, int total, List<String> not) {
        coverage.put(program, new Coverage(entered, total, not));
        lines.add("coverage " + program + " entered " + entered + " of " + total + (not.isEmpty() ? "" : "; not entered: " + String.join(", ", not)));
    }

    /** Each program's coverage by name, when the run was traced. */
    public Map<String, Coverage> coverage() {
        return coverage;
    }

    /** Each dataset's record length, by name. */
    public Map<String, Integer> lrecls() {
        return lrecls;
    }

    public List<String> findings() {
        return findings;
    }

    public String text() {
        return String.join("\n", lines) + "\n";
    }
}
