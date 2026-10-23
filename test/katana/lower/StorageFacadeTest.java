package katana.lower;

import katana.support.Javac;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.rt.Codepage;
import katana.rt.Display;
import katana.rt.RunUnit;
import katana.rt.StopRun;
import katana.rt.Storage;
import katana.rt.Sysin;
import katana.rt.Sysout;
import katana.rt.Upsi;
import katana.sir.build.Build;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * POINTER, SET ADDRESS OF and LENGTH OF lowered and run over the lane's z/OS storage (research §25, R-063):
 * CBSTM03A's walk from the PSA at a NULL pointer to the TCB and the TIOT, a binary item that REDEFINES a
 * pointer stepping it, the loop ending a DD early at the next entry's zero fullword.
 */
class StorageFacadeTest {
    private static final String HASH = "sha256:cc4bbf084793b6f5280da870d1aa7b6fb58d535dcfaf091d8436a36a66c4dae1";
    private static final List<String> SOURCE = List.of("IDENTIFICATION DIVISION.", "PROGRAM-ID. TIOTP.", "DATA DIVISION.", "WORKING-STORAGE SECTION.",
            "01 PSAPTR POINTER.", "01 BUMP PIC S9(8) BINARY VALUE ZERO.", "01 IDX REDEFINES BUMP POINTER.", "LINKAGE SECTION.",
            "01 PSA.", "   05 FILLER PIC X(536).", "   05 TCBP POINTER.", "01 TCB.", "   05 FILLER PIC X(12).", "   05 TIOTP POINTER.",
            "01 TIOTH.", "   05 JOBN PIC X(8).", "   05 STEPN PIC X(8).", "   05 PSTEP PIC X(8).",
            "01 ENT1.", "   05 SEG.", "      10 TLEN PIC X.", "      10 FILLER PIC X(3).", "      10 DDN PIC X(8).", "      10 FILLER PIC X(5).",
            "      10 UCB PIC X(3).", "         88 NOUCB VALUE LOW-VALUES.", "   05 NXT PIC X(4).", "      88 ENDT VALUE LOW-VALUES.",
            "PROCEDURE DIVISION.", "P1.", "    SET ADDRESS OF PSA TO PSAPTR.", "    SET ADDRESS OF TCB TO TCBP.", "    SET ADDRESS OF TIOTH TO TIOTP.",
            "    SET IDX TO TIOTP.", "    DISPLAY JOBN STEPN.", "    COMPUTE BUMP = BUMP + LENGTH OF TIOTH.", "    SET ADDRESS OF ENT1 TO IDX.",
            "    PERFORM UNTIL ENDT", "        IF NOUCB DISPLAY DDN \" N\" ELSE DISPLAY DDN \" U\" END-IF", "        COMPUTE BUMP = BUMP + LENGTH OF SEG",
            "        SET ADDRESS OF ENT1 TO IDX", "    END-PERFORM.", "    IF NOUCB DISPLAY DDN \" N\" ELSE DISPLAY DDN \" U\".", "    STOP RUN.");

    @TempDir
    Path dir;

    @Test
    void aProgramWalksThePsaTcbAndTiotOfItsStep() throws Exception {
        Parse.Result parse = Parse.text(SOURCE.stream().map(l -> "       " + l).collect(Collectors.joining("\n", "", "\n")), new Copybooks(List.of(), n -> 8));
        assertEquals(List.of(), parse.errors());
        Lower.Result r = Lower.java(Build.programs(parse, HASH).get(0).program(), "TIOTP");
        assertEquals(List.of(), r.findings());
        Files.writeString(dir.resolve("TIOTP.java"), r.java());
        Javac.compile("TIOTP", dir, dir.resolve("TIOTP.java"));
        ByteArrayOutputStream shown = new ByteArrayOutputStream();
        try (URLClassLoader loader = new URLClassLoader(new URL[] {dir.toUri().toURL()}, getClass().getClassLoader())) {
            Sysout out = new Sysout(new PrintStream(shown, true, StandardCharsets.ISO_8859_1), Display.Sign.SEP);
            RunUnit u = new RunUnit(Codepage.COBC_ASCII, Upsi.of("00000000"), new Sysin(new java.io.ByteArrayInputStream(new byte[0])), out, out, false);
            u.storage(Storage.of("JOBX STEPY INFILE:u SYSOUT:n OUTFILE:u", Codepage.COBC_ASCII));
            Object main = loader.loadClass("TIOTP").getConstructor(RunUnit.class).newInstance(u);
            try {
                main.getClass().getMethod("run").invoke(main);
            } catch (java.lang.reflect.InvocationTargetException e) {
                if (!(e.getCause() instanceof StopRun)) throw e;
            }
        }
        assertEquals(List.of("JOBX    STEPY   ", "INFILE   U", "SYSOUT   N", "OUTFILE  U"), shown.toString(StandardCharsets.ISO_8859_1).lines().toList());
    }
}
