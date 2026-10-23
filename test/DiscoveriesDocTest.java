import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Holds docs/DISCOVERIES.md (R-058): its rows numbered D-001 onward, in order, never twice; each row's
 * journal entry real; each live row's test a test file that exists and cites the row, its mutation a
 * method of it. And the source held to the register: every list a test keeps of where cobc departs
 * (a constant named ...FINDINGS or ...READINGS, or the editing families) and every comment that says
 * "against cobc" cites a D-number, and every D-number cited is a row.
 */
// Decision: R-058
final class DiscoveriesDocTest {
    private static final Pattern ROW = Pattern.compile("(?m)^\\| (D-\\d{3}) \\|(.*)\\|\\s*$");
    private static final Pattern CITED = Pattern.compile("D-\\d{3}");
    private static final Pattern LIST = Pattern.compile("static final [^=]*\\b\\w*(FINDINGS|READINGS) =|enum Finding \\{");
    private static final Set<String> SKIPPED = Set.of("build", "corpus", ".git", ".gradle");

    /** A source file: its path from the root, its text. */
    record Source(String path, String text) {}

    public static void main(String[] args) {
        run().exit();
    }

    static Check run() {
        String journal = "### J-001 x\n";
        String row = "| D-001 | cobc | x | y | `ATest`; `#killer` | J-001 | 1 | open |\n";
        Source test = new Source("test/katana/ATest.java", "/** D-001. */\nclass ATest { void killer() {} }\n");
        return new Check("discoveries")
                .repository(check(Check.read("docs/DISCOVERIES.md"), JournalIndex.text(Check.ROOT), sources()))
                .accepts("a row its test cites", () -> check(row, journal, List.of(test)))
                .rejects("a row whose test does not exist", () -> check(row.replace("ATest", "BTest"), journal, List.of(test)))
                .rejects("a row naming a list its test does not hold", () -> check(row.replace("`ATest`", "`ATest` (`GONE`)"), journal, List.of(test)))
                .rejects("a row whose mutation is not a method", () -> check(row.replace("#killer", "#gone"), journal, List.of(test)))
                .rejects("a row whose journal entry does not exist", () -> check(row.replace("J-001", "J-002"), journal, List.of(test)))
                .rejects("a number out of order", () -> check(row.replace("D-001", "D-002"), journal, List.of(new Source(test.path(), test.text().replace("D-001", "D-002")))))
                .rejects("a test that does not cite its row", () -> check(row, journal, List.of(new Source(test.path(), test.text().replace("D-001. ", "")))))
                .rejects("a list of cobc's departures citing no row", () -> check(row, journal, List.of(test, new Source("test/katana/BTest.java", "    /** The cases. */\n    static final List<Integer> CAP_FINDINGS = List.of(1);\n"))))
                .rejects("a comment against cobc citing no row", () -> check(row, journal, List.of(test, new Source("rt/X.java", "    // A finding against cobc's dialect.\n    int x;\n"))))
                .rejects("a cited number that is not a row", () -> check(row, journal, List.of(test, new Source("rt/X.java", "    // A finding against cobc, D-009.\n"))));
    }

    /** Every problem in the register and in the source it holds. */
    static List<String> check(String register, String journal, List<Source> sources) {
        List<String> failures = new ArrayList<>();
        Map<String, String> rows = new LinkedHashMap<>();
        Matcher m = ROW.matcher(register);
        while (m.find()) {
            String id = m.group(1);
            if (rows.containsKey(id)) failures.add(id + " is used twice");
            rows.put(id, m.group(2));
            if (!id.equals("D-%03d".formatted(rows.size()))) failures.add(id + " is not D-%03d: rows are numbered in order".formatted(rows.size()));
        }
        rows.forEach((id, cells) -> failures.addAll(row(id, cells.split("\\|"), journal, sources)));
        for (Source s : sources) failures.addAll(cites(s, rows.keySet()));
        return failures;
    }

    /** One row: its journal entry, and unless withdrawn its test (citing the row) and its mutation. */
    private static List<String> row(String id, String[] cells, String journal, List<Source> sources) {
        List<String> failures = new ArrayList<>();
        String entry = cells.length > 4 ? cells[4].trim() : "";
        if (!entry.matches("J-\\d{3}") || !journal.contains("### " + entry + " ")) failures.add(id + "'s journal entry " + entry + " is not in the journal (docs/journal/)");
        if (!cells[cells.length - 1].trim().equals("withdrawn")) failures.addAll(proof(id, cells[3], sources));
        return failures;
    }

    /** A row's proof: its first test exists and cites the row, every list it names is in that test, every mutation a method. */
    private static List<String> proof(String id, String cell, List<Source> sources) {
        List<String> failures = new ArrayList<>();
        Matcher names = Pattern.compile("`(\\w*)(?:#(\\w+))?`").matcher(cell);
        Source test = null;
        while (names.find()) {
            String name = names.group(1), method = names.group(2);
            Source owner = name.endsWith("Test") ? find(sources, name) : test;
            if (name.endsWith("Test") && owner == null) failures.add(id + "'s test " + name + " is not a test file");
            if (name.endsWith("Test") && test == null && owner != null) {
                test = owner;
                if (!test.text().contains(id)) failures.add(id + "'s test " + name + " does not cite " + id);
            }
            if (!name.isEmpty() && !name.endsWith("Test") && (test == null || !test.text().contains(name))) failures.add(id + "'s " + name + " is not in its test");
            if (method != null && (owner == null || !owner.text().contains("void " + method + "("))) failures.add(id + "'s mutation " + method + " is not a method of its test");
        }
        if (test == null && failures.isEmpty()) failures.add(id + " names no test");
        return failures;
    }

    private static Source find(List<Source> sources, String name) {
        return sources.stream().filter(s -> s.path().startsWith("test/") && s.path().endsWith("/" + name + ".java")).findFirst().orElse(null);
    }

    /** A source's lists of cobc's departures and its comments against cobc each cite a row; each number it cites is a row. */
    private static List<String> cites(Source s, Set<String> rows) {
        List<String> failures = new ArrayList<>();
        String[] lines = s.text().split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            boolean list = s.path().startsWith("test/") && LIST.matcher(lines[i]).find();
            boolean against = lines[i].toLowerCase(java.util.Locale.ROOT).contains("against cobc") && comment(lines[i]);
            if ((list || against) && !CITED.matcher(block(lines, i)).find()) {
                failures.add(s.path() + ":" + (i + 1) + (list ? ": a list of cobc's departures" : ": a comment against cobc") + " cites no discovery (R-058)");
            }
            Matcher m = CITED.matcher(lines[i]);
            while (m.find()) if (comment(lines[i]) && !rows.contains(m.group())) failures.add(s.path() + ":" + (i + 1) + ": " + m.group() + " is not a row of docs/DISCOVERIES.md");
        }
        return failures;
    }

    private static boolean comment(String line) {
        String t = line.trim();
        return t.startsWith("//") || t.startsWith("*") || t.startsWith("/**");
    }

    /** The comment lines around a line (the line itself, and the comment block above or around it). */
    private static String block(String[] lines, int at) {
        int from = at, to = at;
        while (from > 0 && comment(lines[from - 1])) from--;
        while (to + 1 < lines.length && comment(lines[to]) && comment(lines[to + 1])) to++;
        return String.join("\n", java.util.Arrays.asList(lines).subList(from, to + 1));
    }

    /** Every Java file of the repository but the build's, the corpora's, git's, and this one (its inputs are made to fail). */
    static List<Source> sources() {
        try (Stream<Path> walk = Files.walk(Check.ROOT)) {
            List<Source> out = new ArrayList<>();
            for (Path p : walk.filter(p -> p.toString().endsWith(".java")).sorted().toList()) {
                Path rel = Check.ROOT.relativize(p);
                if (SKIPPED.contains(rel.getName(0).toString()) || rel.toString().equals("test/DiscoveriesDocTest.java")) continue;
                out.add(new Source(rel.toString().replace('\\', '/'), Files.readString(p)));
            }
            return out;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
