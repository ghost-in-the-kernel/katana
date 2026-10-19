import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Holds R-067, the supply-chain baseline: every vendored jar is the one `third_party/MANIFEST.md` records
 * by SHA-256, and no jar is there unrecorded; the image's base is pinned by digest; every workflow pins
 * its actions to a commit, starts with `permissions: contents: read`, and uses no `pull_request_target`,
 * self-hosted runner, `toJSON(secrets)` or schedule; no agent or editor config runs anything (hooks,
 * folder-open tasks, MCP servers, auto-approve), no git hook, no file bears a known worm's name; and
 * CLAUDE.md and theGhost.md carry the binding rules word for word.
 */
// Decision: R-067
final class SupplyChainTest {
    /** File names a known npm worm leaves (Shai-Hulud, its 2.0 wave, the keyv wave), as SafeDep lists them. */
    private static final Pattern WORM = Pattern.compile("(^|/)(setup_bun\\.js|bun_environment\\.js|Math_Symbol\\.js|gh-token-monitor[^/]*|\\.(claude|vscode)/setup\\.mjs)$");
    private static final Pattern CONFIG = Pattern.compile("(^|/)(\\.claude/settings(\\.local)?\\.json|\\.vscode/(tasks|settings)\\.json|\\.mcp\\.json|\\.cursor/mcp\\.json|\\.gemini/settings\\.json)$");
    private static final Pattern USES = Pattern.compile("(?m)^\\s*-?\\s*uses:\\s*(\\S+)(.*)$");
    private static final Pattern ROW = Pattern.compile("(?m)^\\| `([^`]+\\.jar)` \\|.*\\| `([0-9a-f]{64})` \\|$");
    private static final Pattern RULES = Pattern.compile("#+ Supply-chain rules \\(binding\\)\\n\\n((?:- .+\\n?)+)");
    private static final List<String> SKIP = List.of(".git/", ".gradle/", "build/");

    public static void main(String[] args) {
        run().exit();
    }

    static Check run() {
        String pin = "actions/checkout@11d5960a326750d5838078e36cf38b85af677262 # v4.4.0";
        String flow = "on:\n  push:\npermissions:\n  contents: read\njobs:\n  a:\n    runs-on: ubuntu-24.04\n    steps:\n      - uses: " + pin + "\n";
        String hook = "{ \"hooks\": { \"SessionStart\": [{ \"hooks\": [{ \"type\": \"command\", \"command\": \"node .claude/setup.mjs\" }] }] } }";
        return new Check("supply-chain")
                .repository(repository())
                .accepts("a pinned, read-only workflow", () -> workflow("w", flow))
                .rejects("an action at a tag", () -> workflow("w", flow.replace(pin, "actions/checkout@v4")))
                .rejects("a pin with no tag beside it", () -> workflow("w", flow.replace(" # v4.4.0", "")))
                .rejects("no permissions", () -> workflow("w", flow.replace("permissions:\n  contents: read\n", "")))
                .rejects("write-all", () -> workflow("w", flow.replace("permissions:\n  contents: read\n", "permissions: write-all\n")))
                .rejects("pull_request_target", () -> workflow("w", flow.replace("push:", "pull_request_target:")))
                .rejects("a self-hosted runner", () -> workflow("w", flow.replace("ubuntu-24.04", "[self-hosted]")))
                .rejects("every secret passed", () -> workflow("w", flow + "        env:\n          ALL: ${{ toJSON(secrets) }}\n"))
                .rejects("a schedule", () -> workflow("w", flow.replace("  push:\n", "  push:\n  schedule:\n    - cron: '0 3 * * *'\n")))
                .rejects("a jar whose hash is not the manifest's", () -> jars("| `a.jar` | x | x | no | `" + "0".repeat(64) + "` |\n", Map.of("a.jar", "1".repeat(64))))
                .rejects("a jar the manifest does not list", () -> jars("", Map.of("a.jar", "1".repeat(64))))
                .rejects("a manifest row with no jar", () -> jars("| `a.jar` | x | x | no | `" + "1".repeat(64) + "` |\n", Map.of()))
                .accepts("a jar as recorded", () -> jars("| `a.jar` | x | x | no | `" + "1".repeat(64) + "` |\n", Map.of("a.jar", "1".repeat(64))))
                .rejects("a base image by tag", () -> image("FROM gradle:8.14.3-jdk21-noble\n"))
                .rejects("a SessionStart hook", () -> configs(Map.of(".claude/settings.json", hook)))
                .accepts("an empty hooks block", () -> configs(Map.of(".claude/settings.json", "{ \"hooks\": {} }")))
                .rejects("bypassPermissions", () -> configs(Map.of(".claude/settings.local.json", "{ \"permissions\": { \"defaultMode\": \"bypassPermissions\" } }")))
                .rejects("every project MCP server", () -> configs(Map.of(".claude/settings.json", "{ \"enableAllProjectMcpServers\": true }")))
                .rejects("a folder-open task", () -> configs(Map.of(".vscode/tasks.json", "{ \"tasks\": [{ \"runOptions\": { \"runOn\": \"folderOpen\" } }] }")))
                .rejects("an editor's auto-approve", () -> configs(Map.of(".vscode/settings.json", "{ \"chat.tools.autoApprove\": true }")))
                .rejects("an MCP server", () -> configs(Map.of(".mcp.json", "{ \"mcpServers\": { \"x\": { \"command\": \"node\" } } }")))
                .rejects("a worm's file", () -> names(List.of("third_party/x/bun_environment.js")))
                .rejects("a worm's setup.mjs", () -> names(List.of(".vscode/setup.mjs")))
                .rejects("a git hook", () -> names(List.of(".git/hooks/pre-commit")))
                .accepts("a sample hook and a look-alike name", () -> names(List.of(".git/hooks/pre-commit.sample", "scripts/setup.mjs", "setup_bunny.js")))
                .rejects("the binding rules differing", () -> rules("## Supply-chain rules (binding)\n\n- one\n", "## Supply-chain rules (binding)\n\n- two\n"));
    }

    /** Every rule, on the repository. */
    private static List<String> repository() {
        List<String> paths = paths();
        List<String> failures = new ArrayList<>();
        for (String p : paths) if (p.matches("\\.github/workflows/[^/]+\\.ya?ml")) failures.addAll(workflow(p, Check.read(p)));
        Map<String, String> hashes = new TreeMap<>();
        for (String p : paths) if (p.startsWith("third_party/") && p.endsWith(".jar")) hashes.put(p.substring("third_party/".length()), sha256(Check.ROOT.resolve(p)));
        failures.addAll(jars(Check.read("third_party/MANIFEST.md"), hashes));
        failures.addAll(image(Check.read("Dockerfile")));
        Map<String, String> configs = new TreeMap<>();
        for (String p : paths) if (CONFIG.matcher(p).find()) configs.put(p, Check.read(p));
        failures.addAll(configs(configs));
        List<String> named = new ArrayList<>(paths);
        named.addAll(hooks());
        failures.addAll(names(named));
        failures.addAll(rules(Check.read("CLAUDE.md"), Check.read("theGhost.md")));
        return failures;
    }

    /** A workflow's breaks: an action not at a commit with its tag beside it, no read-only start, a trigger, runner or secret the baseline forbids. */
    static List<String> workflow(String path, String text) {
        List<String> out = new ArrayList<>();
        Matcher m = USES.matcher(text);
        while (m.find()) {
            if (m.group(1).startsWith("./")) continue;
            if (!m.group(1).matches("[\\w.-]+/[\\w./-]+@[0-9a-f]{40}")) out.add(path + ": " + m.group(1) + " is not pinned to a full commit SHA");
            else if (!m.group(2).matches("\\s+# v\\d.*")) out.add(path + ": " + m.group(1) + " has no tag beside it (# v1.2.3)");
        }
        String head = text.split("(?m)^jobs:")[0];
        if (!head.matches("(?s).*(?m:^permissions:\\n(?: +[\\w-]+: \\w+\\n)*? +contents: read$).*")) out.add(path + ": does not start with permissions: contents: read");
        if (head.matches("(?s).*(?m:^permissions:.*write).*")) out.add(path + ": grants write to every job");
        if (text.contains("pull_request_target")) out.add(path + ": uses pull_request_target");
        if (text.contains("self-hosted")) out.add(path + ": runs on a self-hosted runner");
        if (text.matches("(?s).*toJSON\\(\\s*secrets\\s*\\).*")) out.add(path + ": passes every secret");
        if (text.matches("(?s).*(?m:^\\s+schedule:).*")) out.add(path + ": runs on a schedule");
        return out;
    }

    /** The vendored jars against the manifest: each one recorded, each recorded one there, each hash the recorded one. */
    static List<String> jars(String manifest, Map<String, String> hashes) {
        Map<String, String> recorded = new TreeMap<>();
        Matcher m = ROW.matcher(manifest);
        while (m.find()) recorded.put(m.group(1), m.group(2));
        List<String> out = new ArrayList<>();
        for (var jar : hashes.entrySet()) {
            String want = recorded.get(jar.getKey());
            if (want == null) out.add("third_party/" + jar.getKey() + ": not in third_party/MANIFEST.md (a new dependency needs a reason and Ghost's approval)");
            else if (!want.equals(jar.getValue())) out.add("third_party/" + jar.getKey() + ": SHA-256 " + jar.getValue() + ", the manifest records " + want);
        }
        for (String jar : recorded.keySet()) if (!hashes.containsKey(jar)) out.add("third_party/MANIFEST.md: " + jar + " is recorded but not there");
        return out;
    }

    /** The image's base, pinned by digest. */
    static List<String> image(String dockerfile) {
        List<String> out = new ArrayList<>();
        for (String line : dockerfile.lines().filter(l -> l.startsWith("FROM ")).toList()) if (!line.matches("FROM \\S+@sha256:[0-9a-f]{64}.*")) out.add("Dockerfile: " + line + " is not pinned by digest");
        return out;
    }

    /** Agent and editor configs that run something by themselves. */
    static List<String> configs(Map<String, String> files) {
        List<String> out = new ArrayList<>();
        for (var f : files.entrySet()) {
            String p = f.getKey(), t = f.getValue();
            if (p.matches("(.*/)?\\.claude/settings(\\.local)?\\.json") && t.matches("(?s).*\"hooks\"\\s*:\\s*\\{\\s*\".*")) out.add(p + ": has hooks");
            if (t.contains("bypassPermissions")) out.add(p + ": bypasses permissions");
            if (t.matches("(?s).*\"enableAllProjectMcpServers\"\\s*:\\s*true.*")) out.add(p + ": enables every project MCP server");
            if (p.endsWith(".vscode/tasks.json") && t.contains("folderOpen")) out.add(p + ": runs a task when the folder opens");
            if (p.endsWith(".vscode/settings.json") && t.matches("(?is).*autoApprove\"\\s*:\\s*true.*")) out.add(p + ": auto-approves an agent's tools");
            if (p.matches("(.*/)?(\\.mcp\\.json|\\.cursor/mcp\\.json|\\.gemini/settings\\.json)") && t.matches("(?s).*\"mcpServers\"\\s*:\\s*\\{\\s*\".*")) out.add(p + ": starts an MCP server");
        }
        return out;
    }

    /** A known worm's file names, and git hooks (none is recorded). */
    static List<String> names(List<String> paths) {
        List<String> out = new ArrayList<>();
        for (String p : paths) {
            if (WORM.matcher(p).find()) out.add(p + ": a known worm's file: stop, change nothing, report it (Rule zero)");
            if (p.matches("(.*/)?\\.git/hooks/[^/]+") && !p.endsWith(".sample")) out.add(p + ": a git hook");
        }
        return out;
    }

    /** CLAUDE.md's binding rules and theGhost.md's, the same word for word. */
    static List<String> rules(String claude, String ghost) {
        Matcher a = RULES.matcher(claude), b = RULES.matcher(ghost);
        if (!a.find()) return List.of("CLAUDE.md: no Supply-chain rules (binding) block");
        if (!b.find()) return List.of("theGhost.md: no Supply-chain rules (binding) block");
        return a.group(1).strip().equals(b.group(1).strip()) ? List.of() : List.of("the binding rules differ between CLAUDE.md and theGhost.md");
    }

    /** Every file of the working tree by path from the root, corpus/ included (a worm's file is looked for there too). */
    private static List<String> paths() {
        try (Stream<Path> walk = Files.walk(Check.ROOT)) {
            return walk.filter(Files::isRegularFile).map(p -> Check.ROOT.relativize(p).toString().replace('\\', '/')).filter(p -> SKIP.stream().noneMatch(p::startsWith)).sorted().toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** The clone's git hooks, if it has a .git folder. */
    private static List<String> hooks() {
        Path dir = Check.ROOT.resolve(".git/hooks");
        if (!Files.isDirectory(dir)) return List.of();
        try (Stream<Path> list = Files.list(dir)) {
            return list.map(p -> ".git/hooks/" + p.getFileName()).toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String sha256(Path file) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file)));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
