package katana.oracle.proof;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import katana.findings.Finding;
import katana.oracle.compare.Compare;
import katana.oracle.fixture.Fixture;
import katana.oracle.lane.Gate;
import katana.oracle.lane.Report;

/**
 * The proof report, v0 (docs/ROADMAP.md 1.9, docs/PROOF.md): what was compared, under which fixture and
 * profile, the gate's result, each dataset with its masks and findings, the programs' findings and what
 * is valid only under a hypothesis (R-049, R-057), each program's paragraphs the
 * fixture entered and did not, and docs/PROOF.md's "What it never covers" quoted verbatim (PROOF-01).
 */
public final class ProofReport {
    /** One dataset compared: its name, the masks declared for it, and the comparator's result. */
    public record Compared(String dataset, List<Compare.Mask> masks, Compare.Result result) {}

    private ProofReport() {}

    /** The report: expected is the original lane's first run, traced the coverage run, against names the other side. */
    public static String write(Fixture fixture, String fixturePath, Report expected, Gate.Result gate, String against,
                               List<Compared> compared, Report traced, String proofDocument, List<Finding> findings) {
        List<String> out = new ArrayList<>();
        out.add("# Proof report v0: " + fixture.first("job").values().get(0));
        out.add("");
        out.add("- Fixture: `" + fixturePath + "`, " + fixture.hash());
        out.add("- Profile: " + profileLine(expected));
        out.add("- Expected: the cobc lane. Actual: " + against + ".");
        out.add("- Initial values: an item without VALUE is assumed to start as INITIALIZE would set it, not proven (R-057).");
        out.add("");
        out.add("## Gate");
        out.add("");
        if (!gate.identical()) {
            out.add("Failed: the cobc lane's two runs differ, so nothing is compared (PROOF-03).");
            gate.differences().forEach(d -> out.add("- " + d));
        } else {
            out.add("Passed: the cobc lane's two runs are identical.");
            out.add("");
            compared(compared, out);
        }
        out.add("");
        findings(findings, out);
        out.add("");
        coverage(traced, out);
        out.add("");
        out.add("## What it never covers (docs/PROOF.md, verbatim)");
        out.add("");
        out.add(neverCovers(proofDocument));
        return String.join("\n", out) + "\n";
    }

    /**
     * The job's programs' findings, said apart: what cannot be made exact (`NE-`), and what is exact only
     * under a reading not yet proven on IBM (`HY-`, R-049), which one mainframe run clears.
     */
    private static void findings(List<Finding> findings, List<String> out) {
        List<Finding> notExact = findings.stream().filter(f -> !f.kind().hypothesis()).toList(), hypotheses = findings.stream().filter(f -> f.kind().hypothesis()).toList();
        out.add("## Findings");
        out.add("");
        out.add(notExact.isEmpty() ? "None." : Finding.report(notExact).stripTrailing());
        out.add("");
        out.add("## Valid under hypothesis");
        out.add("");
        out.add(hypotheses.isEmpty() ? "None." : Finding.report(hypotheses).stripTrailing());
    }

    private static String profileLine(Report expected) {
        return expected.text().lines().filter(l -> l.startsWith("profile ")).findFirst().map(l -> l.substring(8)).orElse("(not stated)");
    }

    /** The report's Compared section: a row a dataset with its masks and result, then every finding. */
    private static void compared(List<Compared> compared, List<String> out) {
        out.add("## Compared");
        out.add("");
        out.add("| Dataset | Masks | Result |");
        out.add("| --- | --- | --- |");
        for (Compared c : compared) {
            String masks = c.masks().isEmpty() ? "none" : String.join("; ", c.result().masks());
            out.add("| " + c.dataset() + " | " + masks + " | " + (c.result().same() ? "the same" : c.result().findings().size() + " findings") + " |");
        }
        for (Compared c : compared) c.result().findings().forEach(f -> out.add("- " + f));
        for (Compared c : compared) c.result().notes().forEach(n -> out.add("- note: " + n));
        boolean same = compared.stream().allMatch(c -> c.result().same());
        out.add("");
        out.add(same ? "Every dataset compared is the same." : "Datasets differ: the findings above.");
    }

    private static void coverage(Report traced, List<String> out) {
        out.add("## Coverage (from a cobc trace)");
        out.add("");
        out.add("| Program | Paragraphs entered | Not entered |");
        out.add("| --- | --- | --- |");
        for (Map.Entry<String, Report.Coverage> e : traced.coverage().entrySet()) {
            Report.Coverage c = e.getValue();
            String not = c.total() == 0 ? "(no named paragraphs)" : c.notEntered().isEmpty() ? "none" : String.join(", ", c.notEntered());
            out.add("| " + e.getKey() + " | " + c.entered() + " of " + c.total() + " | " + not + " |");
        }
    }

    /**
     * The proof report, v1 (research §27): what Katana makes, the java lane, against the cobc lane. Each
     * dataset the job leaves compared; one the cobc lane leaves none of (a step it cannot run, R-027) said
     * with what holds it instead; each program's coverage from the cobc trace, or from the java lane's where
     * the cobc lane cannot run it; the java lane's trace held to cobc's elsewhere.
     */
    public static String v1(Fixture fixture, String fixturePath, Report expected, Gate.Result gate, List<Compared> compared, List<String> javaOnly,
                            Map<String, Report.Coverage> coverage, Map<String, String> coverageSource, String proofDocument, List<Finding> findings, List<String> notes) {
        List<String> out = new ArrayList<>();
        out.add("# Proof report v1: " + fixture.first("job").values().get(0));
        out.add("");
        out.add("- Fixture: `" + fixturePath + "`, " + fixture.hash());
        out.add("- Profile: " + profileLine(expected));
        out.add("- Expected: the cobc lane. Actual: the java lane (the program lowered by Katana, run on rt/).");
        out.add("- Initial values: an item without VALUE is assumed to start as INITIALIZE would set it, not proven (R-057).");
        out.add("");
        out.add("## Gate");
        out.add("");
        if (!gate.identical()) {
            out.add("Failed: the cobc lane's two runs differ, so nothing is compared (PROOF-03).");
            gate.differences().forEach(d -> out.add("- " + d));
        } else {
            out.add("Passed: the cobc lane's two runs are identical.");
            if (!expected.findings().isEmpty()) {
                out.add("");
                out.add("The job's own findings, in both lanes:");
                expected.findings().forEach(f -> out.add("- " + f));
            }
            out.add("");
            compared(compared, out);
            if (!javaOnly.isEmpty()) {
                out.add("");
                out.add("Not compared, the cobc lane leaving none of them (a step it cannot run, R-027): " + String.join("; ", javaOnly) + ".");
            }
        }
        out.add("");
        findings(findings, out);
        if (!notes.isEmpty()) {
            out.add("");
            out.add("## Notes");
            out.add("");
            notes.forEach(n -> out.add("- " + n));
        }
        out.add("");
        out.add("## Coverage");
        out.add("");
        out.add("| Program | Paragraphs entered | Not entered | From |");
        out.add("| --- | --- | --- | --- |");
        for (Map.Entry<String, Report.Coverage> e : coverage.entrySet()) {
            Report.Coverage c = e.getValue();
            String not = c.total() == 0 ? "(no named paragraphs)" : c.notEntered().isEmpty() ? "none" : String.join(", ", c.notEntered());
            out.add("| " + e.getKey() + " | " + c.entered() + " of " + c.total() + " | " + not + " | " + coverageSource.get(e.getKey()) + " |");
        }
        out.add("");
        out.add("## What it never covers (docs/PROOF.md, verbatim)");
        out.add("");
        out.add(neverCovers(proofDocument));
        return String.join("\n", out) + "\n";
    }

    /**
     * CardDemo's coverage by its fixtures (research §27): each program any of them runs, the paragraphs one
     * or more entered of all it has, the fixtures that ran it, and every paragraph none entered, which the
     * proofs leave proven by nothing ("the honest number a buyer needs", docs/ROADMAP.md 4.6).
     */
    public static String coverage(Map<String, Map<String, Report.Coverage>> byJob) {
        Map<String, Report.Coverage> merged = new java.util.TreeMap<>();
        Map<String, List<String>> jobs = new java.util.TreeMap<>();
        for (var job : byJob.entrySet()) {
            for (var c : job.getValue().entrySet()) {
                jobs.computeIfAbsent(c.getKey(), k -> new ArrayList<>()).add(job.getKey());
                Report.Coverage was = merged.get(c.getKey()), now = c.getValue();
                List<String> not = was == null ? now.notEntered() : was.notEntered().stream().filter(now.notEntered()::contains).toList();
                merged.put(c.getKey(), new Report.Coverage(now.total() - not.size(), now.total(), not));
            }
        }
        int entered = merged.values().stream().mapToInt(Report.Coverage::entered).sum(), total = merged.values().stream().mapToInt(Report.Coverage::total).sum();
        List<String> out = new ArrayList<>();
        out.add("# CardDemo's coverage by its fixtures");
        out.add("");
        out.add("The paragraphs of CardDemo's batch programs that " + String.join(", ", byJob.keySet()) + " entered (research §27): " + entered + " of " + total
                + ". A paragraph no fixture entered is listed, and proven by nothing (docs/PROOF.md).");
        out.add("");
        out.add("| Program | Paragraphs entered | Run by | Not entered by any |");
        out.add("| --- | --- | --- | --- |");
        for (var e : merged.entrySet()) {
            Report.Coverage c = e.getValue();
            out.add("| " + e.getKey() + " | " + c.entered() + " of " + c.total() + " | " + String.join(", ", jobs.get(e.getKey())) + " | " + (c.notEntered().isEmpty() ? "none" : String.join(", ", c.notEntered())) + " |");
        }
        return String.join("\n", out) + "\n";
    }

    /** docs/PROOF.md's "What it never covers" section: its body, up to the next heading. */
    public static String neverCovers(String proofDocument) {
        int start = proofDocument.indexOf("## What it never covers");
        if (start < 0) throw new IllegalArgumentException("docs/PROOF.md has no \"What it never covers\" section");
        int body = proofDocument.indexOf('\n', start) + 1;
        int end = proofDocument.indexOf("\n## ", body);
        return proofDocument.substring(body, end < 0 ? proofDocument.length() : end).strip();
    }
}
