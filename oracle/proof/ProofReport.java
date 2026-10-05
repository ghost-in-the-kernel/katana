package katana.oracle.proof;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import katana.oracle.compare.Compare;
import katana.oracle.fixture.Fixture;
import katana.oracle.lane.Gate;
import katana.oracle.lane.Report;

/**
 * The proof report, v0 (docs/ROADMAP.md 1.9, docs/PROOF.md): what was compared, under which fixture and
 * profile, the gate's result, each dataset with its masks and findings, each program's paragraphs the
 * fixture entered and did not, and docs/PROOF.md's "What it never covers" quoted verbatim (PROOF-01).
 */
public final class ProofReport {
    /** One dataset compared: its name, the masks declared for it, and the comparator's result. */
    public record Compared(String dataset, List<Compare.Mask> masks, Compare.Result result) {}

    private ProofReport() {}

    /** The report: expected is the original lane's first run, traced the coverage run, against names the other side. */
    public static String write(Fixture fixture, String fixturePath, Report expected, Gate.Result gate, String against,
                               List<Compared> compared, Report traced, String proofDocument) {
        List<String> out = new ArrayList<>();
        out.add("# Proof report v0: " + fixture.first("job").values().get(0));
        out.add("");
        out.add("- Fixture: `" + fixturePath + "`, " + fixture.hash());
        out.add("- Profile: " + profileLine(expected));
        out.add("- Expected: the cobc lane. Actual: " + against + ".");
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
        coverage(traced, out);
        out.add("");
        out.add("## What it never covers (docs/PROOF.md, verbatim)");
        out.add("");
        out.add(neverCovers(proofDocument));
        return String.join("\n", out) + "\n";
    }

    private static String profileLine(Report expected) {
        return expected.text().lines().filter(l -> l.startsWith("profile ")).findFirst().map(l -> l.substring(8)).orElse("(not stated)");
    }

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

    /** docs/PROOF.md's "What it never covers" section: its body, up to the next heading. */
    public static String neverCovers(String proofDocument) {
        int start = proofDocument.indexOf("## What it never covers");
        if (start < 0) throw new IllegalArgumentException("docs/PROOF.md has no \"What it never covers\" section");
        int body = proofDocument.indexOf('\n', start) + 1;
        int end = proofDocument.indexOf("\n## ", body);
        return proofDocument.substring(body, end < 0 ? proofDocument.length() : end).strip();
    }
}
