package katana.oracle.autotest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import katana.front.Settings;
import org.junit.jupiter.api.Test;

/** J-255: the flags the census understands become settings; any other is named, never dropped. */
class CompileFlagsTest {
    @Test
    void definitionsAndDebuggingLinesAreSettings() {
        var m = CompileFlags.map(List.of("-DONE", "-DTWO=2", "-fdebugging-line", "-fixed", "-o", "-Wno-x", "-w"), true);
        assertEquals(List.of(), m.unmapped());
        assertEquals(Map.of("ONE", Settings.UNKNOWN, "TWO", "2"), m.settings().defines());
        assertTrue(m.settings().debugging());
        assertTrue(m.settings().extensions(), "the caller's choice, not a flag's");
    }

    @Test
    void aFlagNotUnderstoodIsNamed() {
        var m = CompileFlags.map(List.of("-free", "-std=mf", "-DA=1", "-fbinary-size=1-2-4-8"), true);
        assertEquals(List.of("-free", "-std=mf", "-fbinary-size=1-2-4-8"), m.unmapped());
        assertEquals(Map.of("A", "1"), m.settings().defines());
    }

    /** R-083: the IBM dialects are the lane's own, so they set nothing and are not unmapped; every other -std= is named, with its reason. */
    @Test
    void stdIbmIsMappedAndEveryOtherStdIsNot() {
        var m = CompileFlags.map(List.of("-std=ibm", "-std=ibm-strict"), false);
        assertEquals(List.of(), m.unmapped());
        assertEquals(Settings.declared(Map.of(), false), m.settings());
        assertEquals(List.of("-std=mf", "-std=mvs", "-std=acu-strict", "-std=gcos-strict", "-std=cobol2002", "-std=cobol2014"),
                CompileFlags.map(List.of("-std=mf", "-std=mvs", "-std=acu-strict", "-std=gcos-strict", "-std=cobol2002", "-std=cobol2014"), false).unmapped());
        assertEquals("another dialect's expected output", CompileFlags.reason("-std=mf"));
        assertEquals("a source format the dialect does not have", CompileFlags.reason("-free"));
        assertEquals("a source format the dialect does not have", CompileFlags.reason("--free"));
    }

    /** J-261: a listing and the text of cobc's diagnostics are output of the compile, so they set nothing; the rest are named, each with a reason. */
    @Test
    void listingAndDiagnosticsAreMappedAndEveryOtherFlagHasAReason() {
        assertEquals(List.of(), CompileFlags.map(List.of("-t", "-fno-theaders", "-fno-tmessages", "-fdiagnostics-show-option"), false).unmapped());
        for (String f : List.of("-fnotrunc", "-fno-trunc", "-fno-binary-truncate", "-fsign=ebcdic", "-fodoslide", "-frelax-syntax-checks", "-fno-ec=bound",
                "-debug", "-fdump=ALL", "-fprof", "-fcallfh=EXTFH", "-fsystem-name=USW-31", "-std=mf", "-free"))
            assertTrue(CompileFlags.reason(f) != null, f);
        assertEquals(null, CompileFlags.reason("-fsomething-new"));
    }

    @Test
    void noFlagsDeclaresNoDefinitionsAndReadsNoDebuggingLines() {
        var m = CompileFlags.map(List.of(), false);
        assertEquals(Settings.declared(Map.of(), false), m.settings());
        assertFalse(m.settings().debugging());
    }
}
