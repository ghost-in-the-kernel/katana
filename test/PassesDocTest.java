import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Holds docs/PASSES.md to the code: every pass's ledger id and every field of `Lower.Plan` named in it, every path it names existing. */
final class PassesDocTest {
    private static final Pattern PASS = Pattern.compile("PASS = \"([a-z]+-v\\d+)\"");
    private static final Pattern PLAN = Pattern.compile("public record Plan\\((.*?)\\) \\{", Pattern.DOTALL);
    private static final Pattern PATH = Pattern.compile("`((?:passes|lower|test|docs)/[A-Za-z0-9_./-]+\\.(?:java|md))`");

    public static void main(String[] args) {
        run().exit();
    }

    static Check run() {
        String doc = Check.read("docs/PASSES.md");
        List<String> ids = ids(), fields = fields(Check.read("lower/Lower.java"));
        return new Check("passes")
                .repository(check(doc, ids, fields, Check::exists))
                .rejects("a pass the table leaves out", () -> check("`a-v1`", List.of("a-v1", "b-v1"), List.of(), p -> true))
                .rejects("a plan field the table leaves out", () -> check("`f`", List.of(), List.of("f", "g"), p -> true))
                .rejects("a path that does not exist", () -> check("`passes/Nowhere.java`", List.of(), List.of(), p -> false))
                .accepts("a sound table", () -> check("`a-v1` `f` `passes/A.java`", List.of("a-v1"), List.of("f"), p -> true));
    }

    static List<String> check(String doc, List<String> ids, List<String> fields, Predicate<String> exists) {
        List<String> failures = new ArrayList<>();
        for (String id : ids) if (!doc.contains("`" + id + "`")) failures.add("docs/PASSES.md leaves out the pass " + id);
        for (String f : fields) if (!doc.contains("`" + f + "`")) failures.add("docs/PASSES.md leaves out the plan field " + f);
        Matcher m = PATH.matcher(doc);
        while (m.find()) if (!exists.test(m.group(1))) failures.add("docs/PASSES.md names " + m.group(1) + ", which does not exist");
        return failures;
    }

    /** The ledger ids the passes declare. */
    private static List<String> ids() {
        List<String> out = new ArrayList<>();
        try (var files = Files.list(Path.of("passes"))) {
            for (Path f : files.sorted().toList()) {
                Matcher m = PASS.matcher(Files.readString(f));
                while (m.find()) out.add(m.group(1));
            }
        } catch (java.io.IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
        return out;
    }

    /** The names of `Lower.Plan`'s fields. */
    static List<String> fields(String lower) {
        Matcher m = PLAN.matcher(lower);
        if (!m.find()) return List.of("Plan (not found)");
        List<String> out = new ArrayList<>();
        for (String component : m.group(1).split(",(?![^<]*>)")) {
            String[] words = component.trim().split("\\s+");
            out.add(words[words.length - 1]);
        }
        return out;
    }
}
