import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The way back (tithe pass 3a, item 2; Ghost, 2026-10-06: "a solid track of truth back to the
 * surface"): every open discovery to its IBM quote, a page or table cited and found where the row says
 * it is quoted (DiscoveriesDocTest holds its test and journal entry); every `hypothesis` claim to its
 * trial, listed in HANDOFF's Trials or a journal entry of class `trial`; every `// Decision: R-nnn` to a
 * decision in force. It fails on each missing link.
 */
// Decision: R-058
final class WayBackTest {
    private static final Pattern ROW = Pattern.compile("(?m)^\\| (D-\\d{3}) \\|(.*)\\|\\s*$");
    private static final Pattern DECISION = Pattern.compile("(?ms)^### (R-\\d{3}):.*?(?=^### |\\z)");
    private static final Pattern CITED = Pattern.compile("// Decision: (R-\\d{3})");

    public static void main(String[] args) {
        run().exit();
    }

    static Check run() {
        String ghost = Check.read("theGhost.md");
        String row = "| D-001 | cobc | x | LR 6.4 p. 12 (§1) | `ATest` | J-001 | 1 | open |\n";
        String claims = "### R-001: x\n- **Decision:** x.\n- **Claims:** C-1 a thing: `hypothesis`; a trial.\n- **Status:** in force\n";
        return new Check("way back")
                .repository(discoveries(Check.read("docs/DISCOVERIES.md"), sections(Check.read("docs/research/ibm-options.md")), WayBackTest::source))
                .repository(hypotheses(ghost, trials(Check.read("docs/HANDOFF.md")), Check.read("docs/JOURNAL.md")))
                .repository(decisions(ghost, sources()))
                .accepts("a discovery whose page is quoted where it says", () -> discoveries(row, Map.of("1", "- LR p. 12: \"x\". D-001."), f -> null))
                .rejects("a discovery whose section does not hold its page", () -> discoveries(row, Map.of("1", "- LR p. 13: \"x\"."), f -> null))
                .rejects("a discovery that names no quote", () -> discoveries(row.replace(" (§1)", ""), Map.of("1", "p. 12"), f -> null))
                .accepts("a hypothesis with its trial listed", () -> hypotheses(claims, "- R-001 C-1: the trial.", ""))
                .rejects("a hypothesis with no trial", () -> hypotheses(claims, "", ""))
                .accepts("a hypothesis with a trial in the journal", () -> hypotheses(claims, "", "### J-002 x\n- **Class:** trial.\n- R-001 C-1 settled.\n"))
                .rejects("a citation of a decision not in force", () -> decisions(claims.replace("in force", "withdrawn"), Map.of("x.java", "// Decision: R-001\n")))
                .rejects("a citation of a decision that does not exist", () -> decisions(claims, Map.of("x.java", "// Decision: R-002\n")));
    }

    /** Each open discovery: a quote location (`§N` or a source file), and a cited page or table found there. */
    static List<String> discoveries(String register, Map<String, String> sections, Function<String, String> files) {
        List<String> failures = new ArrayList<>();
        Matcher m = ROW.matcher(register);
        while (m.find()) {
            String[] cells = m.group(2).split("\\|");
            if (cells[cells.length - 1].trim().equals("withdrawn")) continue;
            String authority = cells[2], id = m.group(1);
            List<String> pages = pages(authority), quoted = new ArrayList<>();
            Matcher sec = Pattern.compile("§(\\d+)").matcher(authority);
            while (sec.find()) {
                if (!sections.containsKey(sec.group(1))) failures.add(id + " cites research §" + sec.group(1) + ", which does not exist");
                else quoted.add(sections.get(sec.group(1)));
            }
            Matcher file = Pattern.compile("`((?:rt|lower|sir)/\\w+)`").matcher(authority);
            while (file.find()) {
                String text = files.apply(file.group(1));
                if (text == null) failures.add(id + " cites " + file.group(1) + ", which does not exist");
                else quoted.add(text);
            }
            if (quoted.isEmpty()) failures.add(id + " names no place its IBM text is quoted (a research § or a source file)");
            else if (pages.isEmpty()) failures.add(id + " cites no page or table");
            else if (pages.stream().noneMatch(p -> quoted.stream().anyMatch(q -> q.matches("(?s).*\\b" + p + "\\b.*")))) failures.add(id + " cites " + pages + ", found in none of the places it names");
        }
        return failures;
    }

    /** Every page a citation names (each page of a range) and every table. */
    private static List<String> pages(String authority) {
        List<String> pages = new ArrayList<>();
        Matcher m = Pattern.compile("pp?\\. (\\d+)(?:-(\\d+))?|Table (\\d+)").matcher(authority);
        while (m.find()) {
            if (m.group(3) != null) pages.add(m.group(3));
            else for (int p = Integer.parseInt(m.group(1)); p <= Integer.parseInt(m.group(2) == null ? m.group(1) : m.group(2)); p++) pages.add(String.valueOf(p));
        }
        return pages;
    }

    /** Every `hypothesis` claim (R-nnn C-n) listed in Trials, or named by a journal entry of class trial. */
    static List<String> hypotheses(String ghost, String trials, String journal) {
        List<String> trialEntries = new ArrayList<>();
        for (String e : journal.split("(?m)^### ")) if (e.matches("(?s).*\\*\\*Class:\\*\\*[^\\n]*\\btrial\\b.*")) trialEntries.add(e);
        List<String> failures = new ArrayList<>();
        Matcher d = DECISION.matcher(ghost);
        while (d.find()) {
            Matcher c = Pattern.compile("(?ms)^- \\*\\*Claims:\\*\\*(.*?)(?=^- \\*\\*|\\z)").matcher(d.group());
            if (!c.find()) continue;
            String[] parts = c.group(1).split("\\b(?=C-\\d+\\b)");
            for (String part : parts) {
                Matcher id = Pattern.compile("^C-(\\d+)").matcher(part);
                if (!id.find() || !part.contains("hypothesis")) continue;
                String key = d.group(1) + " C-" + id.group(1);
                Pattern named = Pattern.compile(Pattern.quote(d.group(1)) + "\\W{0,3}(?:'s )?C-" + id.group(1) + "\\b");
                if (!trials.contains(key) && trialEntries.stream().noneMatch(e -> named.matcher(e).find())) failures.add(key + " is a hypothesis with no trial listed in HANDOFF's Trials or journaled as one");
            }
        }
        return failures;
    }

    /** Every `// Decision: R-nnn` in the source names a decision in force. */
    static List<String> decisions(String ghost, Map<String, String> sources) {
        Map<String, String> status = new LinkedHashMap<>();
        Matcher d = DECISION.matcher(ghost);
        while (d.find()) {
            Matcher s = Pattern.compile("- \\*\\*Status:\\*\\* ([^\\n]*)").matcher(d.group());
            status.put(d.group(1), s.find() ? s.group(1).trim() : "");
        }
        List<String> failures = new ArrayList<>();
        sources.forEach((path, text) -> {
            Matcher c = CITED.matcher(text);
            while (c.find()) {
                String st = status.get(c.group(1));
                if (st == null) failures.add(path + " cites " + c.group(1) + ", which is not in theGhost.md");
                else if (!st.startsWith("in force")) failures.add(path + " cites " + c.group(1) + ", which is " + st);
            }
        });
        return failures;
    }

    private static Map<String, String> sections(String research) {
        Map<String, String> sections = new LinkedHashMap<>();
        Matcher m = Pattern.compile("(?ms)^## (\\d+)\\..*?(?=^## |\\z)").matcher(research);
        while (m.find()) sections.put(m.group(1), m.group());
        return sections;
    }

    private static String trials(String handoff) {
        int at = handoff.indexOf("\n## Trials");
        if (at < 0) return "";
        int end = handoff.indexOf("\n## ", at + 5);
        return handoff.substring(at, end < 0 ? handoff.length() : end);
    }

    private static String source(String module) {
        Path p = Check.ROOT.resolve(module.startsWith("rt/") ? "rt/katana/" + module + ".java" : module.replaceFirst("/", "/") + ".java");
        try {
            return Files.exists(p) ? Files.readString(p) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static Map<String, String> sources() {
        Map<String, String> sources = new LinkedHashMap<>();
        for (String dir : List.of("front", "sir", "lower", "rt", "oracle", "scripts", "test")) {
            try (var walk = Files.walk(Check.ROOT.resolve(dir))) {
                for (Path p : walk.filter(x -> x.toString().endsWith(".java")).sorted().toList()) sources.put(Check.ROOT.relativize(p).toString(), Files.readString(p));
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
        return sources;
    }
}
