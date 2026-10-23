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
        assertEquals(Map.of("ONE", "", "TWO", "2"), m.settings().defines());
        assertTrue(m.settings().debugging());
        assertTrue(m.settings().extensions(), "the caller's choice, not a flag's");
    }

    @Test
    void aFlagNotUnderstoodIsNamed() {
        var m = CompileFlags.map(List.of("-free", "-std=ibm", "-DA=1", "-fbinary-size=1-2-4-8"), true);
        assertEquals(List.of("-free", "-std=ibm", "-fbinary-size=1-2-4-8"), m.unmapped());
        assertEquals(Map.of("A", "1"), m.settings().defines());
    }

    @Test
    void noFlagsDeclaresNoDefinitionsAndReadsNoDebuggingLines() {
        var m = CompileFlags.map(List.of(), false);
        assertEquals(Settings.declared(Map.of(), false), m.settings());
        assertFalse(m.settings().debugging());
    }
}
