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
        for (String f : List.of("-fnotrunc", "-fno-trunc", "-fno-binary-truncate", "-fsign=ebcdic", "-fcomplex-odo", "-frelax-syntax-checks", "-fno-ec=bound",
                "-debug", "-fdump=ALL", "-fprof", "-fcallfh=EXTFH", "-fsystem-name=USW-31", "-std=mf", "-free"))
            assertTrue(CompileFlags.reason(f) != null, f);
        assertEquals(null, CompileFlags.reason("-fsomething-new"));
    }

    /** R-087: the TRUNC(BIN) flags wait for their first job with the census's reason; -fbinary-comp-1 is another dialect's USAGE. */
    @Test
    void binaryTruncationFlagsStayUnmappedWithTheirReason() {
        var flags = List.of("-fnotrunc", "-fno-trunc", "-fno-binary-truncate", "-fbinary-comp-1");
        assertEquals(flags, CompileFlags.map(flags, false).unmapped());
        for (String f : flags.subList(0, 3)) assertEquals("recorded under TRUNC(BIN); the census runs ibm.profile (STD)", CompileFlags.reason(f), f);
        assertTrue(CompileFlags.reason("-fbinary-comp-1").startsWith("another dialect's USAGE"));
    }

    /** R-088: IBM's own behaviour sets nothing; the encodings and ODO forms wait, each with its reason. */
    @Test
    void ibmsOwnBehaviourSetsNothingAndTheEncodingsAndOdoFormsWait() {
        var m = CompileFlags.map(List.of("-farithmetic-osvs", "-fmove-ibm", "-fodoslide"), false);
        assertEquals(List.of(), m.unmapped());
        assertEquals(Settings.declared(Map.of(), false), m.settings());
        var waits = List.of("-fcomplex-odo", "-fsign=ebcdic", "-fsign=ascii", "-fdefault-file-colseq=EBCDIC");
        assertEquals(waits, CompileFlags.map(waits, false).unmapped());
        String lane = "the oracle lane's byte encoding; IBM has only EBCDIC (ibm.codepage)";
        assertEquals(lane, CompileFlags.reason("-fsign=ebcdic"));
        assertEquals(lane, CompileFlags.reason("-fdefault-file-colseq=EBCDIC"));
        assertEquals("not an IBM encoding", CompileFlags.reason("-fsign=ascii"));
        assertTrue(CompileFlags.reason("-fcomplex-odo").contains("Language Reference"));
    }

    /** R-089: a flag for a form IBM lacks stays unmapped and is a grammar leak if its case passes; the two IBM accepts or refuses alike set nothing. */
    @Test
    void acceptanceFlagsNeverMap() {
        var absent = List.of("-frelax-syntax-checks", "-fhp-octal-literals=ok", "-facu-literals=ok", "-facu-literal=ok", "-fcontrol-division=ok",
                "-fdefine-constant-directive=ok", "-fdpc-in-data=none", "-fpartial-replace-when-literal-src=skip", "-freserved=EXAMINE", "-fno-areacheck",
                "-fmissing-statement=ok", "-fno-program-name-redefinition", "-fformat=terminal");
        assertEquals(absent, CompileFlags.map(absent, false).unmapped());
        for (String f : absent) assertTrue(CompileFlags.grammarLeak(f), f);
        assertEquals("a form the dialect does not have", CompileFlags.reason("-frelax-syntax-checks"));
        assertEquals("a source format the dialect does not have", CompileFlags.reason("-fformat=xopen"));
        assertEquals(List.of(), CompileFlags.map(List.of("-fcomment-paragraphs=ok", "-faccept-display-extensions=error"), false).unmapped());
        for (String f : List.of("-fnotrunc", "-free", "-std=mf", "-fsign=ebcdic", "-fcomplex-odo")) assertFalse(CompileFlags.grammarLeak(f), f);
    }

    /** The census golden writes a reason once per key: a key names one text, the one `reason` gives every flag that has it. */
    @Test
    void aReasonKeyNamesTheFlagsReason() {
        for (String f : List.of("-std=mf", "-free", "-fformat=xopen", "-fnotrunc", "-fbinary-comp-1", "-fsign=ascii", "-fsign=ebcdic", "-fdefault-file-colseq=EBCDIC", "-fcomplex-odo",
                "-fconstant-folding", "-frelax-syntax-checks", "-fno-ec=bound", "-fcallfh=EXTFH", "-fsystem-name=USW-31")) {
            assertTrue(CompileFlags.reasonKey(f) != null, f);
            assertEquals(CompileFlags.reason(f), CompileFlags.reasonOfKey(CompileFlags.reasonKey(f)), f);
        }
        assertEquals(null, CompileFlags.reasonKey("-fsomething-new"));
        assertEquals(null, CompileFlags.reasonOfKey("nothing"));
    }

    @Test
    void noFlagsDeclaresNoDefinitionsAndReadsNoDebuggingLines() {
        var m = CompileFlags.map(List.of(), false);
        assertEquals(Settings.declared(Map.of(), false), m.settings());
        assertFalse(m.settings().debugging());
    }
}
