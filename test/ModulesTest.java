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
    private static final List<String> MODEL_CLIENTS =
            List.of("com.anthropic", "com.openai", "dev.langchain4j", "com.google.cloud.vertexai", "software.amazon.awssdk.services.bedrock");
    private static final Set<String> NOT_SOURCE = Set.of("test", "build", "third_party", "corpus", "docs");

    public static void main(String[] args) {
        String json = Check.read("docs/modules.json");
        new Check("modules")
                .repository(check(json, source()))
                .rejects("lower/ importing oracle/", () -> check(json, Map.of("lower/Emit.java", "package katana.lower;\nimport katana.oracle.Lane;\n")))
                .rejects("a model client", () -> check(json, Map.of("sir/Build.java", "package katana.sir;\nimport com.anthropic.Client;\n")))
                .rejects("source outside any module", () -> check(json, Map.of("misc/X.java", "package katana.misc;\n")))
                .rejects("rt/ allowed an import", () -> check(json.replaceFirst("(\"rt\": \\{[^}]*\"mayImport\": \\[)", "$1\"sir\""), Map.of()))
                .accepts("an allowed import", () -> check(json, Map.of("sir/Build.java", "package katana.sir;\nimport katana.front.Tree;\nimport java.util.List;\n")))
                .exit();
    }

    @SuppressWarnings("unchecked")
    static List<String> check(String json, Map<String, String> source) {
        List<String> failures = new ArrayList<>();
        Map<String, Map<String, Object>> modules = (Map<String, Map<String, Object>>) ((Map<String, Object>) Json.parse(json)).get("modules");
        for (var entry : modules.entrySet()) entry(entry.getKey(), entry.getValue(), modules.keySet(), failures);
        cycles(modules, failures);
        rules(modules, failures);
        for (var file : source.entrySet()) imports(file.getKey(), file.getValue(), modules, failures);
        return failures;
    }

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
            if (!name.startsWith("katana.")) continue;
            String target = name.split("\\.")[1];
            if (!target.equals(module) && !names(modules.get(module), "mayImport").contains(target)) {
                failures.add(path + " imports " + name + ", but " + module + " may not import " + target);
            }
        }
    }

    /** Every .java outside the prototypes, the tests and the vendored code, by path from the root. */
    private static Map<String, String> source() {
        Map<String, String> files = new TreeMap<>();
        try (Stream<Path> walk = Files.walk(Check.ROOT)) {
            for (Path p : walk.filter(p -> p.toString().endsWith(".java")).toList()) {
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
