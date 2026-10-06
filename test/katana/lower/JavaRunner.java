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

    JavaRunner(long seconds) {
        this.seconds = seconds;
    }

    @Override
    public Result module(Path dir, String source) {
        return new Result(1, "CALL is phase 4.3: a called module is not lowered yet");
    }

    @Override
    public Result program(Path dir, String source) {
        try {
            Parse.Result parse = Parse.file(dir.resolve(source), new Copybooks(List.of(dir), n -> 8), 8);
            if (!parse.clean()) return new Result(1, "does not parse: " + parse.errors().get(0));
            Program program = Build.programs(parse, "sha256:" + "0".repeat(64)).get(0).program();
            String name = Lower.className(program);
            Lower.Result lowered = Lower.java(program, name);
            Files.writeString(dir.resolve(name + ".java"), lowered.java());
            Files.writeString(dir.resolve(stem(source) + ".class-name"), name + "\n" + String.join("\n", lowered.findings()));
            ByteArrayOutputStream diagnostics = new ByteArrayOutputStream();
            JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
            int status = javac.run(null, null, diagnostics, "-classpath", rtClasses(), "-d", dir.toString(), dir.resolve(name + ".java").toString());
            return new Result(status, diagnostics.toString(StandardCharsets.UTF_8));
        } catch (Exception | StackOverflowError e) {
            return new Result(1, "lowering: " + e);
        }
    }

    @Override
    public Result run(Path dir, String program, Map<String, String> env, byte[] input) {
        try {
            String name = Files.readString(dir.resolve(stem(program) + ".class-name")).lines().findFirst().orElseThrow();
            List<String> command = new ArrayList<>(List.of(Path.of(System.getProperty("java.home"), "bin", "java").toString(), "-cp", rtClasses() + java.io.File.pathSeparator + dir, "-Dkatana.upsi=" + upsi(env), name));
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
