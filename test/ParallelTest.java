import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * The fast loop runs its test classes in parallel (R-054 amended, 2026-10-09; `build.gradle`), which is
 * safe while no two classes write the same file: a fast-loop test writes into a temporary directory of
 * its own (`@TempDir`, `createTempDirectory`), or a golden only under `-Dkatana.record`. A write
 * outside both fails here, before it can race another class.
 */
final class ParallelTest {
    private static final Pattern WRITE = Pattern.compile("Files\\.(write|writeString|newOutputStream|copy|move|delete|deleteIfExists)\\(|new FileOutputStream\\(");

    public static void main(String[] args) {
        run().exit();
    }

    static Check run() {
        return new Check("parallel fast loop")
                .repository(writes(fastTests()))
                .rejects("a fast-loop class that writes without the flag", () -> writes(Map.of("x/ATest.java", "    @Test\n    void t() throws Exception {\n        Files.writeString(Path.of(\"sir/roles.golden\"), \"x\");\n    }\n")))
                .accepts("a golden written under the flag", () -> writes(Map.of("x/ATest.java", "    @Test\n    void t() {\n        if (Boolean.getBoolean(\"katana.record\")) Files.writeString(GOLDEN, got);\n    }\n")))
                .accepts("a write into the test's own temporary directory", () -> writes(Map.of("x/ATest.java", "    @Test\n    void t(@TempDir Path dir) {\n        Files.writeString(dir.resolve(\"A\"), \"x\");\n    }\n")))
                .accepts("an integration class", () -> writes(Map.of()));
    }

    /** Each write in a fast-loop test class that is neither under `-Dkatana.record` nor in a temporary directory of its own. */
    static List<String> writes(Map<String, String> tests) {
        List<String> failures = new ArrayList<>();
        tests.forEach((path, text) -> {
            boolean temporary = text.contains("@TempDir") || text.contains("createTempDirectory");
            String[] lines = text.split("\n", -1);
            for (int i = 0; i < lines.length; i++) {
                if (WRITE.matcher(lines[i]).find() && !lines[i].contains("katana.record") && !temporary) {
                    failures.add(path + ":" + (i + 1) + ": a fast-loop class writes outside a temporary directory without -Dkatana.record; the classes run in parallel");
                }
            }
        });
        return failures;
    }

    /** The test classes the fast loop runs: a `@Test` and no `@Tag("integration")`. */
    private static Map<String, String> fastTests() {
        Map<String, String> tests = new LinkedHashMap<>();
        try (var walk = Files.walk(Check.ROOT.resolve("test"))) {
            for (Path p : walk.filter(x -> x.toString().endsWith(".java")).sorted().toList()) {
                String text = Files.readString(p);
                if (text.contains("@Test") && !text.contains("@Tag(\"integration\")")) tests.put(Check.ROOT.relativize(p).toString(), text);
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return tests;
    }
}
