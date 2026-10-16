package katana.lower;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.tools.ToolProvider;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.rt.Codepage;
import katana.rt.Display;
import katana.rt.RunUnit;
import katana.rt.StopRun;
import katana.rt.Sysin;
import katana.rt.Sysout;
import katana.rt.Upsi;
import katana.sir.build.Build;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * CALL and CANCEL lowered and run (research §24, LANE-19): a separately compiled program reached by a
 * literal and by an identifier, its copies under NODYNAM and DYNAM, CANCEL, BY CONTENT, ON EXCEPTION, and
 * an INITIAL nested program that adds to its container's GLOBAL item, and an EXTERNAL record both programs
 * describe, the run unit's one (LR 6.4 p. 197).
 */
class CallVerbsTest {
    private static final String HASH = "sha256:cc4bbf084793b6f5280da870d1aa7b6fb58d535dcfaf091d8436a36a66c4dae1";
    private static final List<String> SOURCE = List.of("IDENTIFICATION DIVISION.", "PROGRAM-ID. MAINP.", "DATA DIVISION.", "WORKING-STORAGE SECTION.",
            "01 G GLOBAL.", "   02 G1 PIC 9 VALUE 0.", "01 N PIC 99.", "01 EF PIC X.", "01 ID1 PIC X(4) VALUE \"SUBP\".", "01 XT EXTERNAL.", "   02 XT1 PIC 9.", "PROCEDURE DIVISION.", "P1.",
            "    CALL \"SUBP\" USING N. DISPLAY N.", "    CALL ID1 USING N. DISPLAY N.", "    CALL \"SUBP\" USING N. DISPLAY N.", "    CANCEL \"SUBP\".",
            "    CALL \"SUBP\" USING N. DISPLAY N.", "    CALL ID1 USING N. DISPLAY N.", "    CALL \"INNER\". CALL \"INNER\". DISPLAY G1.",
            "    CALL \"NOPE\" ON EXCEPTION MOVE \"X\" TO EF", "      NOT ON EXCEPTION MOVE \"Y\" TO EF END-CALL. DISPLAY EF.", "    CALL \"SUBP\" USING BY CONTENT N. DISPLAY N.",
            "    DISPLAY XT1. STOP RUN.", "IDENTIFICATION DIVISION.", "PROGRAM-ID. INNER IS INITIAL.", "DATA DIVISION.", "WORKING-STORAGE SECTION.", "01 K PIC 9 VALUE 0.",
            "PROCEDURE DIVISION.", "Q1.", "    ADD 1 TO K. ADD K TO G1. EXIT PROGRAM.", "END PROGRAM INNER.", "END PROGRAM MAINP.",
            "IDENTIFICATION DIVISION.", "PROGRAM-ID. SUBP.", "DATA DIVISION.", "WORKING-STORAGE SECTION.", "01 C PIC 99 VALUE 0.", "01 XT EXTERNAL.", "   02 XT1 PIC 9.", "LINKAGE SECTION.",
            "01 L PIC 99.", "PROCEDURE DIVISION USING L.", "S1.", "    ADD 1 TO C XT1. MOVE C TO L. GOBACK.", "END PROGRAM SUBP.");

    @TempDir
    Path dir;

    /** Under NODYNAM the literal reaches SUBP's static copy and the identifier its dynamic one, which CANCEL alone resets (PG 6.4 p. 552, LR 6.4 p. 327). */
    @Test
    void underNodynamALiteralAndAnIdentifierReachTwoCopies() throws Exception {
        assertEquals(List.of("01", "01", "02", "03", "01", "2", "X", "01", "6"), run(false));
    }

    /** Under DYNAM every CALL reaches the one dynamic copy, which CANCEL resets. */
    @Test
    void underDynamEveryCallReachesOneCopy() throws Exception {
        assertEquals(List.of("01", "02", "03", "01", "02", "2", "X", "02", "6"), run(true));
    }

    /** A dynamic CALL finds the class the lowering named (`rt/RunUnit.className`, `Lower.className`). */
    @Test
    void theRunUnitNamesAProgramsClassAsTheLoweringDoes() {
        for (String name : List.of("IC222A-1", "SUBP", "9X", "A$B")) assertEquals(Lower.className(name), RunUnit.className(name), name);
    }

    /** The two programs lowered, compiled, and MAINP run until its STOP RUN; what it DISPLAYed. */
    private List<String> run(boolean dynam) throws Exception {
        Parse.Result parse = Parse.text(SOURCE.stream().map(l -> "       " + l).collect(Collectors.joining("\n", "", "\n")), new Copybooks(List.of(), n -> 8));
        assertEquals(List.of(), parse.errors());
        List<String> files = new ArrayList<>(List.of("-classpath", System.getProperty("java.class.path"), "-d", dir.toString()));
        for (Build.Result b : Build.programs(parse, HASH)) {
            Lower.Result r = Lower.java(b.program(), Lower.className(b.program()));
            assertEquals(List.of(), r.findings());
            Path java = dir.resolve(Lower.className(b.program()) + ".java");
            Files.writeString(java, r.java());
            files.add(java.toString());
        }
        ByteArrayOutputStream diagnostics = new ByteArrayOutputStream(), shown = new ByteArrayOutputStream();
        assertEquals(0, ToolProvider.getSystemJavaCompiler().run(null, null, diagnostics, files.toArray(String[]::new)), diagnostics.toString(StandardCharsets.UTF_8));
        ClassLoader before = Thread.currentThread().getContextClassLoader();
        try (URLClassLoader loader = new URLClassLoader(new URL[] {dir.toUri().toURL()}, getClass().getClassLoader())) {
            Thread.currentThread().setContextClassLoader(loader);
            Sysout out = new Sysout(new PrintStream(shown, true, StandardCharsets.ISO_8859_1), Display.Sign.SEP);
            RunUnit u = new RunUnit(Codepage.COBC_ASCII, Upsi.of("00000000"), new Sysin(new java.io.ByteArrayInputStream(new byte[0])), out, out, dynam);
            Object main = loader.loadClass("MAINP").getConstructor(RunUnit.class).newInstance(u);
            try {
                main.getClass().getMethod("run").invoke(main);
            } catch (java.lang.reflect.InvocationTargetException e) {
                if (!(e.getCause() instanceof StopRun)) throw e;
            }
        } finally {
            Thread.currentThread().setContextClassLoader(before);
        }
        return shown.toString(StandardCharsets.ISO_8859_1).lines().toList();
    }
}
