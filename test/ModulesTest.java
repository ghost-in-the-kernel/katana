import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Holds docs/modules.json (docs/ROADMAP.md 3.3, 4.2): every entry complete and naming only modules;
 * the may-import graph without a cycle; the entries agreeing with the rules MOD-01 to MOD-06; and
 * every import in the source within its module's may-import list, with no model client (R-004).
 */
final class ModulesTest {
    private static final List<String> FIELDS =
            List.of("purpose", "inputs", "outputs", "mayImport", "mustNotImport", "importsFrom", "phase", "entryPoints");
    private static final Pattern IMPORT = Pattern.compile("(?m)^import (?:static )?([\\w.]+)");
    private static final Pattern PACKAGE = Pattern.compile("(?m)^package ([\\w.]+);");
    /** A module named by its qualified name in code (J-232: `katana.lower.Lower.THROUGH` with no import line). */
    private static final Pattern QUALIFIED = Pattern.compile("(?<![\\w.])katana\\.([a-z]+)\\.");
    /** What a qualified name inside is not a reference: text blocks, strings, characters, comments. */
    private static final Pattern NOT_CODE = Pattern.compile("(?s)\"\"\".*?\"\"\"|\"(?:\\\\.|[^\"\\\\\n])*\"|'(?:\\\\.|[^'\\\\\n])+'|/\\*.*?\\*/|//[^\n]*|(?m)^(?:import|package) [^\n]*");
    private static final List<String> MODEL_CLIENTS =
            List.of("com.anthropic", "com.openai", "dev.langchain4j", "com.google.cloud.vertexai", "software.amazon.awssdk.services.bedrock");
    private static final Set<String> NOT_SOURCE = Set.of("test", "scripts", "build", "third_party", "corpus", "docs");

    public static void main(String[] args) {
        run().exit();
    }

    static Check run() {
        String json = Check.read("docs/modules.json");
        return new Check("modules")
                .repository(check(json, source()))
                .rejects("lower/ importing oracle/", () -> check(json, Map.of("lower/Emit.java", "package katana.lower;\nimport katana.oracle.Lane;\n")))
                .rejects("a model client", () -> check(json, Map.of("sir/Build.java", "package katana.sir;\nimport com.anthropic.Client;\n")))
                .rejects("source outside any module", () -> check(json, Map.of("misc/X.java", "package katana.misc;\n")))
                .rejects("rt/ allowed an import", () -> check(json.replaceFirst("(\"rt\": \\{[^}]*\"mayImport\": \\[)", "$1\"sir\""), Map.of()))
                .rejects("rt/ importing sir/", () -> check(json, Map.of("rt/Field.java", "package katana.rt;\nimport katana.sir.types.Item;\n", "rt/LICENSE", "")))
                .rejects("rt/ importing a library", () -> check(json, Map.of("rt/Field.java", "package katana.rt;\nimport org.antlr.v4.runtime.Token;\n", "rt/LICENSE", "")))
                .rejects("rt/ without its own LICENSE", () -> check(json, Map.of("rt/Field.java", "package katana.rt;\nimport java.math.BigDecimal;\n")))
                .rejects("lower/ importing front/", () -> check(json, Map.of("lower/Emit.java", "package katana.lower;\nimport katana.front.Parse;\n")))
                .rejects("lower/ naming oracle/ by its qualified name", () -> check(json, Map.of("lower/Emit.java", "package katana.lower;\nclass E { Object o = katana.oracle.Lane.run(); }\n")))
                .accepts("a qualified name in a string or a comment", () -> check(json, Map.of("lower/Emit.java", "package katana.lower;\n// katana.oracle.Lane\nclass E { String s = \"katana.rt.Field\"; }\n")))
                .accepts("rt/ on the JDK alone, licensed", () -> check(json, Map.of("rt/Field.java", "package katana.rt;\nimport java.math.BigDecimal;\n", "rt/LICENSE", "")))
                .accepts("an allowed import", () -> check(json, Map.of("sir/Build.java", "package katana.sir;\nimport katana.front.Tree;\nimport java.util.List;\n")));
    }

    @SuppressWarnings("unchecked")
    static List<String> check(String json, Map<String, String> source) {
        List<String> failures = new ArrayList<>();
        Map<String, Map<String, Object>> modules = (Map<String, Map<String, Object>>) ((Map<String, Object>) Json.parse(json)).get("modules");
        for (var entry : modules.entrySet()) entry(entry.getKey(), entry.getValue(), modules.keySet(), failures);
        cycles(modules, failures);
        rules(modules, failures);
        for (var file : source.entrySet()) if (file.getKey().endsWith(".java")) imports(file.getKey(), file.getValue(), modules, failures);
        boolean rt = source.keySet().stream().anyMatch(f -> f.startsWith("rt/") && f.endsWith(".java"));
        if (rt && !source.containsKey("rt/LICENSE")) failures.add("rt/ has code but no rt/LICENSE: it ships, under its own license (R-002)");
        return failures;
    }

    /** One module's entry: every field, a known importsFrom, import lists naming other modules that exist, none both allowed and refused. */
    private static void entry(String name, Map<String, Object> m, Set<String> known, List<String> failures) {
        for (String field : FIELDS) if (!m.containsKey(field)) failures.add(name + " has no " + field);
        if (!List.of("roadmap", "pipeline").contains(m.get("importsFrom"))) failures.add(name + ": importsFrom is roadmap or pipeline");
        for (String list : List.of("mayImport", "mustNotImport")) {
            for (String other : names(m, list)) {
                if (!known.contains(other)) failures.add(name + "." + list + " names " + other + ", not a module");
                if (other.equals(name)) failures.add(name + "." + list + " names itself");
            }
        }
        for (String other : names(m, "mayImport")) {
            if (names(m, "mustNotImport").contains(other)) failures.add(name + " both may and must not import " + other);
        }
    }

    /** No module reaches itself through mayImport. */
    private static void cycles(Map<String, Map<String, Object>> modules, List<String> failures) {
        for (String start : modules.keySet()) {
            List<String> stack = new ArrayList<>(List.of(start));
            Set<String> seen = new HashSet<>();
            while (!stack.isEmpty()) {
                String at = stack.remove(stack.size() - 1);
                for (String next : names(modules.get(at), "mayImport")) {
                    if (next.equals(start)) failures.add(start + " can reach itself through mayImport");
                    else if (seen.add(next) && modules.containsKey(next)) stack.add(next);
                }
            }
        }
    }

    /** The entries must agree with the rules they carry (MOD-02 to MOD-06; MOD-01 is checked on source). */
    private static void rules(Map<String, Map<String, Object>> modules, List<String> failures) {
        expect(names(modules.get("rt"), "mayImport").isEmpty(), "MOD-02: rt/ imports nothing", failures);
        expect(names(modules.get("lower"), "mayImport").equals(List.of("sir")), "MOD-03: lower/ reads sir/ only", failures);
        expect(names(modules.get("passes"), "mayImport").contains("lower"), "MOD-04: passes/ writes Java through lower/'s emitter", failures);
        expect(names(modules.get("oracle"), "mustNotImport").containsAll(List.of("lower", "passes")), "MOD-05: oracle/ never imports lower/ or passes/", failures);
        expect(names(modules.get("front"), "mayImport").isEmpty(), "MOD-06: front/ imports no module of ours", failures);
    }

    /** A source's imports of other modules, each one its module may make. */
    private static void imports(String path, String text, Map<String, Map<String, Object>> modules, List<String> failures) {
        String module = path.split("/")[0];
        if (!modules.containsKey(module)) {
            failures.add(path + " is not in any module of docs/modules.json");
            return;
        }
        Matcher pkg = PACKAGE.matcher(text);
        if (!pkg.find() || !(pkg.group(1) + ".").startsWith("katana." + module + ".")) failures.add(path + " is not in package katana." + module);
        Matcher imp = IMPORT.matcher(text);
        while (imp.find()) {
            String name = imp.group(1);
            for (String client : MODEL_CLIENTS) if (name.startsWith(client)) failures.add(path + " imports a model client, " + name + " (MOD-01, R-004)");
            if (module.equals("rt") && !name.startsWith("java.")) failures.add(path + " imports " + name + ": rt/ ships and imports the JDK alone (MOD-02)");
            if (!name.startsWith("katana.")) continue;
            String target = name.split("\\.")[1];
            if (!target.equals(module) && !names(modules.get(module), "mayImport").contains(target)) {
                failures.add(path + " imports " + name + ", but " + module + " may not import " + target);
            }
        }
        Matcher q = QUALIFIED.matcher(NOT_CODE.matcher(text).replaceAll(""));
        while (q.find()) {
            String target = q.group(1);
            if (!target.equals(module) && !names(modules.get(module), "mayImport").contains(target)) failures.add(path + " names katana." + target + " by its qualified name, but " + module + " may not import " + target);
        }
    }

    /** Every .java outside the prototypes, the tests and the vendored code, and rt/LICENSE, by path from the root. */
    private static Map<String, String> source() {
        Map<String, String> files = new TreeMap<>();
        try (Stream<Path> walk = Files.walk(Check.ROOT)) {
            for (Path p : walk.filter(p -> p.toString().endsWith(".java") || p.toString().endsWith("/rt/LICENSE")).toList()) {
                String rel = Check.ROOT.relativize(p).toString().replace('\\', '/');
                if (NOT_SOURCE.contains(rel.split("/")[0]) || rel.contains("/prototypes/") || rel.startsWith(".")) continue;
                files.put(rel, Files.readString(p));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return files;
    }

    @SuppressWarnings("unchecked")
    private static List<String> names(Map<String, Object> module, String list) {
        Object value = module == null ? null : module.get(list);
        return value instanceof List<?> l ? (List<String>) l : List.of();
    }

    private static void expect(boolean holds, String rule, List<String> failures) {
        if (!holds) failures.add("modules.json breaks " + rule);
    }
}
