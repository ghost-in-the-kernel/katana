package katana.lower;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.oracle.nist.Runner;
import katana.sir.build.Build;
import katana.sir.types.Program;

/**
 * Phase 3.4's Java lane for NIST (docs/LANES.md LANE-09): a program parsed, built to the Semantic IR,
 * lowered, compiled by javac against `rt/` alone into its folder, and run there in a JVM of its own, the
 * lane's environment added (the files it ASSIGNs through `DD_` names, as the cobc lane binds them) and
 * its switches (`COB_SWITCH_n`, n from 0 to 7) as LE's UPSI option, UPSI-n the n-th bit.
 */
final class JavaRunner implements Runner {
    private final long seconds;
    private final boolean dynam;

    /** A runner under the ibm profile's NODYNAM (research §24). */
    JavaRunner(long seconds) {
        this(seconds, false);
    }

    /** A runner under NODYNAM, or DYNAM (`dynam`), every CALL literal then dynamic as cobc's are. */
    JavaRunner(long seconds, boolean dynam) {
        this.seconds = seconds;
        this.dynam = dynam;
    }

    /** A called module: lowered as a main program is, the CALL finding its class by name (LANE-19). */
    @Override
    public Result module(Path dir, String source) {
        return program(dir, source);
    }

    /** A source's lowering as compiled: its Java and class files by name, its `.class-name` text, and javac's result. */
    private record Compiled(Map<String, byte[]> files, String names, Result result) {}

    /**
     * Each source compiled once a JVM, by its text and the copy members beside it: IC runs under NODYNAM and
     * DYNAM, the same 47 sources, and parsing is most of a NIST run's time (J-188).
     */
    private static final Map<String, Compiled> COMPILED = new java.util.concurrent.ConcurrentHashMap<>();

    /** Every program of the source lowered, one class each, the first the one that runs; compiled against `rt/` alone, once a JVM. */
    @Override
    public Result program(Path dir, String source) {
        try {
            String key = key(dir, source);
            Compiled done = COMPILED.get(key);
            if (done == null) {
                done = compile(dir, source);
                COMPILED.put(key, done);
            }
            for (var f : done.files().entrySet()) Files.write(dir.resolve(f.getKey()), f.getValue());
            if (!done.names().isEmpty()) Files.writeString(dir.resolve(stem(source) + ".class-name"), done.names());
            return done.result();
        } catch (java.io.IOException e) {
            return new Result(1, "lowering: " + e);
        }
    }

    /** A source's key: its text's hash and each copy member's beside it. */
    private static String key(Path dir, String source) throws java.io.IOException {
        java.security.MessageDigest sha;
        try {
            sha = java.security.MessageDigest.getInstance("SHA-256");
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
        sha.update(Files.readAllBytes(dir.resolve(source)));
        try (var list = Files.list(dir)) {
            for (Path c : list.filter(f -> f.toString().endsWith(".CPY")).sorted().toList()) {
                sha.update(c.getFileName().toString().getBytes(StandardCharsets.UTF_8));
                sha.update(Files.readAllBytes(c));
            }
        }
        return java.util.HexFormat.of().formatHex(sha.digest());
    }

    /** The source parsed, built, lowered and compiled in its folder; what it wrote, kept. */
    private Compiled compile(Path dir, String source) {
        try {
            Parse.Result parse = Parse.file(dir.resolve(source), new Copybooks(List.of(dir), n -> 8), 8);
            if (!parse.clean()) return new Compiled(Map.of(), "", new Result(1, "does not parse: " + parse.errors().get(0)));
            List<String> sources = new ArrayList<>(), findings = new ArrayList<>();
            String first = null;
            for (Build.Result built : Build.programs(parse, "sha256:" + "0".repeat(64))) {
                Program program = built.program();
                String name = Lower.className(program);
                if (first == null) first = name;
                Lower.Result lowered = Lower.java(program, name);
                Files.writeString(dir.resolve(name + ".java"), lowered.java());
                sources.add(dir.resolve(name + ".java").toString());
                // The build's findings (a name that names nothing, or two items) are reported with the lowering's (J-160).
                findings.addAll(built.findings());
                findings.addAll(lowered.findings());
            }
            String names = first + "\n" + String.join("\n", findings);
            Path out = Files.createTempDirectory(dir, "javac");
            ByteArrayOutputStream diagnostics = new ByteArrayOutputStream();
            JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
            List<String> options = new ArrayList<>(List.of("-classpath", rtClasses(), "-d", out.toString()));
            options.addAll(sources);
            int status = javac.run(null, null, diagnostics, options.toArray(String[]::new));
            Map<String, byte[]> files = new java.util.TreeMap<>();
            for (String java : sources) files.put(Path.of(java).getFileName().toString(), Files.readAllBytes(Path.of(java)));
            try (var list = Files.list(out)) {
                for (Path c : list.toList()) {
                    files.put(c.getFileName().toString(), Files.readAllBytes(c));
                    Files.delete(c);
                }
            }
            Files.delete(out);
            return new Compiled(files, names, new Result(status, diagnostics.toString(StandardCharsets.UTF_8)));
        } catch (Exception | StackOverflowError e) {
            return new Compiled(Map.of(), "", new Result(1, "lowering: " + e + " at " + java.util.Arrays.stream(e.getStackTrace()).filter(t -> t.getClassName().startsWith("katana.")).limit(4).map(String::valueOf).collect(java.util.stream.Collectors.joining(" < "))));
        }
    }

    @Override
    public Result run(Path dir, String program, Map<String, String> env, byte[] input) {
        try {
            String name = Files.readString(dir.resolve(stem(program) + ".class-name")).lines().findFirst().orElseThrow();
            List<String> command = new ArrayList<>(List.of(Path.of(System.getProperty("java.home"), "bin", "java").toString(), "-cp", rtClasses() + java.io.File.pathSeparator + dir, "-Dkatana.upsi=" + upsi(env), "-Dkatana.debug=" + env.getOrDefault("COB_SET_DEBUG", "N"), "-Dkatana.dynam=" + (dynam ? "DYNAM" : "NODYNAM"), name));
            Path log = dir.resolve(stem(program) + ".java-out");
            ProcessBuilder pb = new ProcessBuilder(command).directory(dir.toFile()).redirectErrorStream(true).redirectOutput(log.toFile());
            pb.environment().putAll(env);
            Process process = pb.start();
            process.getOutputStream().write(input);
            process.getOutputStream().close();
            if (!process.waitFor(seconds, TimeUnit.SECONDS)) {
                process.destroyForcibly().waitFor();
                return new Result(-1, "timeout");
            }
            return new Result(process.exitValue(), Files.readString(log, StandardCharsets.ISO_8859_1));
        } catch (Exception e) {
            return new Result(1, "run: " + e);
        }
    }

    private static String stem(String source) {
        int dot = source.lastIndexOf('.');
        return dot < 0 ? source : source.substring(0, dot);
    }

    /** `rt/`'s classes and nothing else. */
    static String rtClasses() {
        return Arrays.stream(System.getProperty("java.class.path").split(java.io.File.pathSeparator)).filter(e -> e.replace('\\', '/').endsWith("/classes/java/rt")).findFirst().orElseThrow();
    }

    /** The lane's switches as UPSI's eight bits. */
    static String upsi(Map<String, String> env) {
        StringBuilder bits = new StringBuilder();
        for (int n = 0; n < 8; n++) bits.append("ON".equals(env.get("COB_SWITCH_" + n)) ? '1' : '0');
        return bits.toString();
    }
}
