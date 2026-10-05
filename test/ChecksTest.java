import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * R-056's meta-test (tithe pass 2, A7): holds docs/checks.json. Every integration test and every test
 * that reads a golden has an entry; each entry's oracle is of a known kind, and one whose path lies
 * inside the module under test says why it is independent anyway or is a finding; a self golden is a
 * finding; each mutation names a test that exists; each gate runs the test (the fast loop for a fast
 * test, a pre-push suite whose --tests pattern selects an integration test), and every suite selects
 * at least one, so none runs zero and passes. A check with no mutation is a finding, listed in the
 * registry with its reason.
 */
// Decision: R-056
final class ChecksTest {
    private static final Pattern GOLDEN = Pattern.compile("\\.golden\"|\\.contract\"|\"sir/golden");
    private static final Pattern PATH = Pattern.compile("\\b([a-z_]+(?:/[A-Za-z0-9_.*-]+)+)");
    private static final Pattern SUITE = Pattern.compile("(?m)^\\s*(\\w+)\\) tests=\"\\$tests((?: --tests [\\w.*]+)+)\"");

    public static void main(String[] args) {
        run().exit();
    }

    /** The registry held to the repository, and each rule shown to refuse its own broken input (R-056 applied to itself). */
    static Check run() {
        String json = Check.read("docs/checks.json");
        String prepush = Check.read("scripts/prepush.sh");
        Map<String, String> tests = tests();
        String entry = "{\"test\": \"katana.sir.IrGoldenTest\", \"module\": \"sir\", \"oracle\": {\"kind\": \"self\", \"what\": \"sir/golden\"}, \"mutation\": null, \"gates\": \"fast\"}";
        String sound = entry.replace("\"self\"", "\"tool\"").replace("\"sir/golden\"", "\"cobc\"").replace("null", "\"katana.sir.NoTokenLeftTest#theCheckFailsWhenAKeywordIsDropped\"");
        String tokens = "{\"test\": \"katana.sir.NoTokenLeftTest\", \"module\": \"sir\", \"oracle\": {\"kind\": \"tool\", \"what\": \"front's parse tree\"}, "
                + "\"mutation\": \"katana.sir.NoTokenLeftTest#theCheckFailsWhenAKeywordIsDropped\", \"gates\": \"fast\"}";
        Map<String, String> two = new TreeMap<>();
        for (String t : List.of("test/katana/sir/IrGoldenTest.java", "test/katana/sir/NoTokenLeftTest.java")) two.put(t, tests.get(t));
        return new Check("checks")
                .repository(check(json, tests, prepush))
                .repository(suites(tests, prepush))
                .rejects("an integration test with no entry", () -> check(json, with(tests, "test/katana/sir/NewIntegrationTest.java",
                        "package katana.sir;\n@Tag(\"integration\")\nclass NewIntegrationTest {\n    @Test\n    void x() {}\n}\n"), prepush))
                .rejects("a self golden with no finding", () -> check(registry(entry, tokens), two, prepush))
                .rejects("an oracle inside its module, unexplained", () -> check(registry(sound.replace("\"tool\"", "\"file\"").replace("\"cobc\"", "\"sir/golden\""), tokens), two, prepush))
                .rejects("a mutation that is not a test", () -> check(registry(sound.replace("theCheckFailsWhenAKeywordIsDropped", "noSuchTest"), tokens), two, prepush))
                .rejects("a gate that does not run it", () -> check(registry(sound.replace("\"fast\"", "\"nist\""), tokens), two, prepush))
                .rejects("a suite that selects nothing", () -> suites(tests, prepush.replace("--tests katana.oracle.nist.*", "--tests katana.oracle.nothing.*")))
                .accepts("an entry that holds", () -> check(registry(sound, tokens), two, prepush));
    }

    /** The registry against the test sources and the pre-push script. */
    @SuppressWarnings("unchecked")
    static List<String> check(String json, Map<String, String> tests, String prepush) {
        List<String> failures = new ArrayList<>();
        Map<String, Object> root = (Map<String, Object>) Json.parse(json);
        Map<String, Object> kinds = (Map<String, Object>) root.get("kinds");
        List<Map<String, Object>> checks = (List<Map<String, Object>>) root.get("checks");
        List<String> listed = checks.stream().map(c -> (String) c.get("test")).toList();
        for (var t : tests.entrySet()) {
            boolean integration = t.getValue().contains("@Tag(\"integration\")");
            if ((integration || GOLDEN.matcher(t.getValue()).find()) && t.getValue().contains("@Test") && !listed.contains(name(t.getKey()))) {
                failures.add(name(t.getKey()) + " is " + (integration ? "an integration test" : "a golden test") + " with no entry in docs/checks.json (R-056)");
            }
        }
        for (Map<String, Object> c : checks) entry(c, kinds, tests, prepush, failures);
        return failures;
    }

    /** Every pre-push suite selects at least one integration test, so none runs zero and passes. */
    static List<String> suites(Map<String, String> tests, String prepush) {
        List<String> failures = new ArrayList<>();
        Matcher suite = SUITE.matcher(prepush);
        while (suite.find()) {
            String name = suite.group(1);
            boolean any = tests.entrySet().stream().anyMatch(t -> t.getValue().contains("@Tag(\"integration\")") && selects(prepush, name, name(t.getKey())));
            if (!any) failures.add("the pre-push suite " + name + " selects no integration test: it would run zero and pass (R-056, J-006)");
        }
        return failures;
    }

    /** One entry: its test exists; its oracle, mutation and gate hold, or it is a finding that says why not. */
    @SuppressWarnings("unchecked")
    private static void entry(Map<String, Object> c, Map<String, Object> kinds, Map<String, String> tests, String prepush, List<String> failures) {
        String test = (String) c.get("test"), module = (String) c.get("module"), gates = (String) c.get("gates");
        boolean finding = c.get("finding") instanceof String f && !f.isBlank();
        String source = tests.get(path(test));
        if (source == null) {
            failures.add(test + ": no such test");
            return;
        }
        Map<String, Object> oracle = (Map<String, Object>) c.get("oracle");
        String kind = oracle == null ? null : (String) oracle.get("kind"), what = oracle == null ? "" : String.valueOf(oracle.get("what"));
        if (kind == null || !kinds.containsKey(kind)) failures.add(test + ": its oracle's kind is not one of " + kinds.keySet());
        if ("self".equals(kind) && !finding) failures.add(test + ": a self golden catches change, not error: a finding says what refutes its facts");
        Matcher p = PATH.matcher(what);
        while (p.find()) {
            if ((p.group(1) + "/").startsWith(module + "/") && !"self".equals(kind) && !(c.get("independent") instanceof String) && !finding) {
                failures.add(test + ": its oracle " + p.group(1) + " lies inside " + module + "/, the module under test; say why it is independent, or list it as a finding");
            }
        }
        if (c.get("mutation") instanceof String m) mutation(test, m, tests, failures);
        else if (!finding) failures.add(test + ": no mutation, and no finding saying so");
        boolean integration = source.contains("@Tag(\"integration\")");
        if ("fast".equals(gates)) {
            if (integration) failures.add(test + ": an integration test does not run in the fast loop");
        } else if (!integration || !selects(prepush, gates, test)) {
            failures.add(test + ": the pre-push suite " + gates + " does not run it, so its verdict stops nothing");
        }
    }

    /** A mutation is a test method that exists: `Class#method`, annotated @Test. */
    private static void mutation(String test, String mutation, Map<String, String> tests, List<String> failures) {
        String[] parts = mutation.split("#");
        String source = parts.length == 2 ? tests.get(path(parts[0])) : null;
        if (source == null || !Pattern.compile("@Test\\s+void " + Pattern.quote(parts[1]) + "\\(").matcher(source).find()) {
            failures.add(test + ": its mutation " + mutation + " is not a test");
        }
    }

    /** Whether the pre-push script's suite selects a test class by one of its --tests patterns. */
    private static boolean selects(String prepush, String suite, String test) {
        Matcher m = SUITE.matcher(prepush);
        while (m.find()) {
            if (!m.group(1).equals(suite)) continue;
            for (String pattern : m.group(2).trim().split("\\s*--tests\\s+")) {
                if (!pattern.isBlank() && test.matches(pattern.trim().replace(".", "\\.").replace("*", ".*"))) return true;
            }
        }
        return false;
    }

    private static String name(String path) {
        return path.replaceFirst("^test/", "").replaceFirst("\\.java$", "").replace('/', '.');
    }

    private static String path(String name) {
        return "test/" + name.replace('.', '/') + ".java";
    }

    private static Map<String, String> with(Map<String, String> tests, String path, String text) {
        Map<String, String> out = new TreeMap<>(tests);
        out.put(path, text);
        return out;
    }

    private static String registry(String... entries) {
        return "{\"kinds\": {\"tool\": \"\", \"file\": \"\", \"spec\": \"\", \"self\": \"\"}, \"checks\": [" + String.join(", ", entries) + "]}";
    }

    /** Every test source under test/katana, by path from the root. */
    private static Map<String, String> tests() {
        Map<String, String> files = new TreeMap<>();
        try (Stream<Path> walk = Files.walk(Check.ROOT.resolve("test/katana"))) {
            for (Path p : walk.filter(p -> p.toString().endsWith(".java")).toList()) {
                files.put(Check.ROOT.relativize(p).toString().replace('\\', '/'), Files.readString(p));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return files;
    }
}
