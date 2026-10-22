package katana.oracle.prove;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import katana.findings.Divergence;
import katana.findings.Finding;
import katana.oracle.fixture.Fixture;

/**
 * 8A.6's divergence report (research §53, R-072): a foreign Java port against its COBOL under one fixture. Each
 * entry `proven` (a run differed; the fixture and the input record cited by hash) or `possible` (a statement of a
 * kind's shape the fixture never took across the kind's boundary), with its statement, its aligned Java, its cause,
 * its trace and its fix; the findings no port can make exact apart; docs/PROOF.md's "What it never covers" quoted
 * (PROOF-01). Nothing in it varies between runs.
 */
public final class Report {
    /** The report's subject: the program, the fixture by path from the root and by hash, the port's command, and how many cells differ. */
    public record Header(String program, String fixture, String fixtureSha, String port, int cells) {}

    /**
     * One divergence: its status, the field, dataset and records, what proves it, the statement and its origin, the
     * aligned Java, the cause (null for unattributed) and the cells it reproduces, the field it follows from (null for
     * a root), the trace, and the fix.
     */
    public record Entry(String status, String field, String dsn, List<Integer> records, String proof, String statement, String origin, String java,
                        Divergence.Kind cause, int reproduced, int cells, String follows, String trace, String fix) {}

    private static final Pattern PROVEN = Pattern.compile("(?m)^- Proven by: fixture (\\S+) sha256:([0-9a-f]{64})");

    private Report() {}

    /** The report's text. */
    // Decision: R-072
    public static String write(Header h, List<Entry> entries, List<Finding> findings, String neverCovers) {
        long proven = entries.stream().filter(e -> e.status().equals("proven")).count();
        StringBuilder b = new StringBuilder("# Divergence report: " + h.program() + ", its Java port against its COBOL\n\n");
        b.append("Fixture: ").append(h.fixture()).append(" sha256:").append(h.fixtureSha()).append("\n");
        b.append("Port: `").append(h.port()).append("`\n");
        b.append("Oracle: cobc -std=ibm under the fixture's profile (R-013). ").append(h.cells()).append(" cells differ; ")
                .append(proven).append(" divergences proven, ").append(entries.size() - proven).append(" possible.\n\n");
        int n = 0;
        for (Entry e : entries) entry(b, ++n, e);
        b.append("## No port can be exact here\n\n");
        if (findings.isEmpty()) b.append("None: the program holds no NE- or HY- finding (docs/FINDINGS.md).\n\n");
        else b.append(Finding.report(findings)).append('\n');
        b.append("## What it never covers (docs/PROOF.md, verbatim)\n\n").append(neverCovers).append('\n');
        return b.toString();
    }

    /** One entry: its heading, its proof, statement and Java, then its cause (or what it follows from, or unattributed), its trace and its fix. */
    private static void entry(StringBuilder b, int n, Entry e) {
        b.append("## ").append(n).append(". ").append(e.status()).append(": ").append(e.field());
        if (e.dsn() != null) b.append(" in ").append(e.dsn()).append(", records ").append(e.records());
        b.append("\n\n");
        if (e.proof() != null) b.append("- Proven by: ").append(e.proof()).append('\n');
        b.append("- Statement: ").append(e.statement()).append(" (").append(e.origin()).append(")\n");
        b.append("- Aligned Java: ").append(e.java()).append('\n');
        if (e.follows() != null) b.append("- Follows from: the divergence of ").append(e.follows()).append(", which the statement reads\n");
        else if (e.cause() == null) b.append("- Cause: unattributed: no probe reproduces these cells\n");
        else b.append("- Cause: ").append(e.cause().code).append(' ').append(e.cause().title).append(e.cells() > 0 ? ", reproducing " + e.reproduced() + " of " + e.cells() + " cells" : "")
                .append(". The rule: ").append(e.cause().rule).append(". The wrong shape: ").append(e.cause().wrong).append('\n');
        b.append("- Trace: ").append(e.trace()).append('\n');
        if (e.cause() != null && e.follows() == null) b.append("- Fix: ").append(e.cause().exact).append("; Katana's Java for the statement, a reference, not a patch:\n\n```java\n").append(e.fix()).append("\n```\n");
        b.append('\n');
    }

    /** What a report claims that does not hold: a proven entry whose fixture is missing, or whose hash is not the fixture's address (FIXTURE-02). */
    public static List<String> refusals(String report, Path root) throws IOException {
        List<String> out = new ArrayList<>();
        Matcher m = PROVEN.matcher(report);
        while (m.find()) {
            Path f = root.resolve(m.group(1));
            if (!Files.exists(f)) out.add("a proven entry cites " + m.group(1) + ", which does not exist");
            else if (!Fixture.read(Files.readString(f)).hash().equals("sha256:" + m.group(2))) out.add("a proven entry cites " + m.group(1) + " at a hash it no longer has");
        }
        return out;
    }
}
