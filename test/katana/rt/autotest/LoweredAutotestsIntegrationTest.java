package katana.rt.autotest;

import katana.lower.Plan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.lower.Lower;
import katana.rt.Codepage;
import katana.rt.Display;
import katana.rt.Field;
import katana.rt.Sysin;
import katana.rt.Sysout;
import katana.rt.Upsi;
import katana.sir.build.Build;
import katana.sir.types.Item;
import katana.sir.types.Procedure;
import katana.sir.types.Program;
import katana.sir.types.Statement;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Phase 3.3's check on the programs hand-lowered at 3.1: each `prog.cob` through the whole pipeline
 * (parse, Semantic IR, `lower/`), the Java compiled by javac against `rt/` alone and run. Its output
 * must be the hand-lowered program's byte for byte (the hand-lowering is the lowering's specification),
 * its data map the IR's (every item's field at the IR's offset and length), and every statement must
 * have its provenance comment. Its mutation: a lowering that drops the last statement differs.
 */
@Tag("integration")
class LoweredAutotestsIntegrationTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));
    private static final Path CASES = ROOT.resolve("corpus/gnucobol-autotests/cases");

    /** A program lowered and compiled: its IR, the lowering's result, and the loaded class. */
    record Built(Program program, Lower.Result lowered, Class<?> type) {}

    static Built build(String id) throws Exception {
        return build(id, false);
    }

    /** The same, lowered under 6.1's structuring pass when `structured` (research §35): the pass's unit corpus. */
    static Built build(String id, boolean structured) throws Exception {
        Parse.Result parse = Parse.file(CASES.resolve(id).resolve("prog.cob"), new Copybooks(List.of(), n -> 8), 8);
        if (!parse.clean()) return null;
        Program program = Build.program(parse, "sha256:" + "0".repeat(64)).program();
        Lower.Result lowered = Lower.java(program, "Prog", structured ? katana.passes.Passes.plan(program, katana.intent.rules.Rules.infer(program)).plan() : Plan.LITERAL);
        Path dir = Files.createDirectories(ROOT.resolve("build/lowered/" + id + (structured ? "-structured" : "")));
        Files.writeString(dir.resolve("Prog.java"), lowered.java());
        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        ByteArrayOutputStream diagnostics = new ByteArrayOutputStream();
        int status = javac.run(null, null, diagnostics, "-classpath", rtClasses(), "-d", dir.toString(), dir.resolve("Prog.java").toString());
        assertEquals(0, status, id + ": javac against rt/ alone\n" + diagnostics);
        URLClassLoader loader = new URLClassLoader(new URL[] {dir.toUri().toURL()}, LoweredAutotestsIntegrationTest.class.getClassLoader());
        return new Built(program, lowered, loader.loadClass("Prog"));
    }

    /** `rt/`'s classes and nothing else: the classpath entry of the rt source set. */
    static String rtClasses() {
        return Arrays.stream(System.getProperty("java.class.path").split(java.io.File.pathSeparator)).filter(e -> e.replace('\\', '/').endsWith("/classes/java/rt")).findFirst().orElseThrow();
    }

    /** A lowered program as its `main` makes one: the lane's codepage, every UPSI switch off, nothing on its input. */
    static Object construct(Built b, Sysout out, Sysout err) throws Exception {
        return b.type().getConstructor(Codepage.class, Upsi.class, Sysin.class, Sysout.class, Sysout.class)
                .newInstance(Out.CP, Upsi.of("00000000"), new Sysin(new java.io.ByteArrayInputStream(new byte[0])), out, err);
    }

    static byte[] run(Built b) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream(), err = new ByteArrayOutputStream();
        Object program = construct(b, new Sysout(out, Out.SIGN), new Sysout(err, Display.Sign.SEP));
        try {
            b.type().getMethod("run").invoke(program);
        } catch (java.lang.reflect.InvocationTargetException e) {
            // STOP RUN ends the run unit by throwing, as `main` catches it (LANE-19).
            if (!(e.getCause() instanceof katana.rt.StopRun)) throw e;
        }
        return out.toByteArray();
    }

    static int statements(List<Statement> list) {
        int n = 0;
        for (Statement s : list) {
            n++;
            for (Statement.Branch br : s.branches()) n += statements(br.statements());
        }
        return n;
    }

    @Test
    void eachHandLoweredProgramLowersToTheSameOutput() throws Exception {
        List<String> differ = new ArrayList<>();
        int lowered = 0, methods = 0;
        for (Lowered l : AutotestRtIntegrationTest.LOWERED) {
            Built b = build(l.id());
            if (b == null) continue;
            lowered++;
            int written = b.program().procedure().paragraphs().stream().mapToInt(p -> statements(p.statements())).sum();
            assertEquals(written, b.lowered().statements(), l.id() + ": one provenance comment a statement");
            dataMap(b);
            byte[] want = AutotestRtIntegrationTest.katana(l), got;
            try {
                got = run(b);
            } catch (java.lang.reflect.InvocationTargetException stopped) {
                differ.add(l.id() + ": " + stopped.getCause().getMessage());
                continue;
            }
            if (!Arrays.equals(want, got)) differ.add(l.id() + " findings " + b.lowered().findings() + "\nlowered:\n" + text(got) + "by hand:\n" + text(want));
            Built structured = build(l.id(), true);
            if (!Arrays.equals(want, run(structured))) differ.add(l.id() + " structured (6.1) differs");
            if (structured.lowered().java().contains("private void perform")) methods++;
        }
        System.out.println("structured: " + methods + " of the lowered programs hold a range made a method");
        System.out.println("lowered " + lowered + " of " + AutotestRtIntegrationTest.LOWERED.size());
        assertEquals(List.of(), differ);
    }

    /** Every item's field where the IR puts it: the same record offset and length. */
    private static void dataMap(Built b) throws Exception {
        Object program = construct(b, new Sysout(new ByteArrayOutputStream(), Out.SIGN), new Sysout(new ByteArrayOutputStream(), Out.SIGN));
        for (Item i : b.program().items()) {
            java.lang.reflect.Field member = b.type().getDeclaredField("f" + i.id());
            member.setAccessible(true);
            Field f = (Field) member.get(program);
            assertEquals(List.of(i.offset(), i.length()), List.of(f.offset(), f.attr().length()), i.name() + " in " + b.program().header().program());
        }
    }

    private static String text(byte[] b) {
        return new String(b, StandardCharsets.ISO_8859_1);
    }

    /** R-056's mutation: the lowered output without its last line differs from the hand-lowered. */
    @Test
    void aLoweringThatLosesTheLastStatementDiffers() throws Exception {
        Lowered l = AutotestRtIntegrationTest.LOWERED.get(1);
        byte[] got = run(build(l.id())), want = AutotestRtIntegrationTest.katana(l);
        int cut = got.length - 1;
        while (cut > 0 && got[cut - 1] != '\n') cut--;
        assertTrue(!Arrays.equals(Arrays.copyOf(got, cut), want));
    }
}
