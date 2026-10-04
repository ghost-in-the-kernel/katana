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
    private final List<String> findings = new ArrayList<>();

    void line(String line) {
        lines.add(line);
    }

    void step(String name, String outcome) {
        steps.put(name, outcome);
        lines.add("step " + name + " " + outcome);
    }

    void dataset(String dsn, int records, String sha256) {
        datasets.put(dsn, sha256);
        lines.add("dataset " + dsn + " records=" + records + " " + sha256);
    }

    void finding(String finding) {
        findings.add(finding);
        lines.add("finding " + finding);
    }

    /** Each step's outcome by name: `rc=4`, `abend U0999 rc=231`, `bypassed ...`, `not run ...`. */
    public Map<String, String> steps() {
        return steps;
    }

    /** Each dataset's records at the end, by name, as `sha256:...`. */
    public Map<String, String> datasets() {
        return datasets;
    }

    public List<String> findings() {
        return findings;
    }

    public String text() {
        return String.join("\n", lines) + "\n";
    }
}
