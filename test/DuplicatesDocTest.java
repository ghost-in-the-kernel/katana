import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds the documents an agent reads first against a section pasted twice (J-231: HANDOFF.md carried a 100-line
 * section twice, found by an advisor's md5): no paragraph of 200 characters or more, and no `## ` heading, appears
 * twice in one of them.
 */
final class DuplicatesDocTest {
    private static final List<String> DOCS = List.of("CLAUDE.md", "TODO.md", "docs/HANDOFF.md", "docs/AGENT.md", "docs/PASSES.md");
    private static final int LONG = 200;

    public static void main(String[] args) {
        run().exit();
    }

    static Check run() {
        List<String> failures = new ArrayList<>();
        for (String doc : DOCS) failures.addAll(check(doc, Check.read(doc)));
        String paragraph = "x".repeat(LONG);
        return new Check("duplicates")
                .repository(failures)
                .rejects("a long paragraph twice", () -> check("d", paragraph + "\n\n" + paragraph + "\n"))
                .rejects("a heading twice", () -> check("d", "## A\n\ntext\n\n## A\n"))
                .accepts("short lines repeated", () -> check("d", "Check: as above.\n\nCheck: as above.\n"));
    }

    /** Each long paragraph and each `## ` heading that appears a second time in the document. */
    static List<String> check(String name, String text) {
        List<String> failures = new ArrayList<>();
        Map<String, Integer> seen = new HashMap<>();
        for (String paragraph : text.split("\n\\s*\n")) {
            String p = paragraph.strip();
            boolean heading = p.startsWith("## ") && !p.contains("\n");
            if (!heading && p.length() < LONG) continue;
            if (seen.merge(p, 1, Integer::sum) == 2)
                failures.add(name + " has twice: " + (p.length() > 80 ? p.substring(0, 80) + "..." : p));
        }
        return failures;
    }
}
