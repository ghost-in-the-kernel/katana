package katana.rt.autotest;

import java.util.Map;
import java.util.TreeMap;

/**
 * The census's FAIL cases sorted (TODO 3.1, J-271): each case whose lowered output differs from the testsuite's expected stdout, by why. The
 * oracle for the sorting is cobc under the lane's arguments (`-std=ibm -fbinary-truncate` and the case's own flags), run on the case's own
 * program under the expected check's own command (R-045 as amended): where Katana equals it and the default dialect prints the expectation,
 * the expectation is the default dialect's (D-010); where Katana differs from it, a real difference, or a difference between cobc's ibm config
 * and IBM's text that DISCOVERIES lists. A sorting is a judgement, so it is written here by hand and the census holds it to the FAIL cases:
 * a new FAIL fails the census until it is sorted, and a sorted case that stops failing fails it until its line goes.
 */
final class FailSorting {
    private FailSorting() {}

    /** The keys and what each says; the golden's `# sorting` block writes them once. */
    static final Map<String, String> TEXTS = new TreeMap<>(Map.ofEntries(
            Map.entry("d010", "D-010: Katana's output equals cobc -std=ibm's; the expectation is GnuCOBOL's default dialect (DISPLAY shows a decimal point, default arithmetic), which cobc's default dialect prints; IBM shows none (LR 6.4 p. 333) and truncates intermediates (Appendix A)"),
            Map.entry("d010-cobc", "D-010, and cobc -std=ibm itself differs from IBM's text in the same output (D-004 binary DISPLAY digits, D-005 the sign beside the overpunch); Katana follows IBM's text, the expectation is the default dialect's"),
            Map.entry("setting", "the expectation was recorded with runtime checks on (cobc -debug: OCCURS DEPENDING ON bounds); IBM's default is NOSSRANGE, which Katana runs; cobc -std=ibm -debug prints the expectation"),
            Map.entry("harness", "not Katana: the census runs prog.cob from the repository root without the expected check's own command (working folder and date variable, a pipe, or another program of the case); under that command Katana equals cobc -std=ibm and the expectation, or the expectation is of another program"),
            Map.entry("debug-line", "USE FOR DEBUGGING on procedures: the census sets no katana.debug (the case's command sets COB_SET_DEBUG=1); with it Katana differs from cobc -std=ibm only in DEBUG-ITEM's line (six zero-padded digits, and a line for START PROGRAM, where cobc blank-pads and leaves START PROGRAM blank); IBM's form waits for the Language Reference (R-082)"),
            Map.entry("debug-data", "USE FOR DEBUGGING on a data name or a file (a GnuCOBOL extension its ibm config accepts): the lowering names it as not lowered, IBM debugs procedures only (LR 6.4 p. 708), so no debug line is written"),
            Map.entry("level78", "level 78 constants (GnuCOBOL's; its ibm config accepts them): Katana reads `78 X VALUE ...` as an item with no PICTURE, DISPLAYs nothing for it, and names no finding (P-03)"),
            Map.entry("files", "READ of a RECORD VARYING file over text (the case's DD_ file or ASSIGN EXTERNAL name): run under the case's command Katana ends in status 47 or an EOFException from SeqFile.readVarying where cobc -std=ibm reads; the census as run has neither the file nor the variable"),
            Map.entry("pscale", "DISPLAY of an item with P (assumed scaling) positions: Katana writes the stored digits (Display.of: the bytes), cobc -std=ibm writes the PICTURE's digits with the P positions, the expectation the value with a point; no D- row lists it; IBM's rule waits for the Language Reference (R-082)"),
            Map.entry("env-name", "DISPLAY ... UPON ENVIRONMENT-NAME / ENVIRONMENT-VALUE, ACCEPT FROM ENVIRONMENT-VALUE, ARGUMENT-VALUE and ARGUMENT-NUMBER (GnuCOBOL's): Katana writes the names to stdout and reads blanks; no finding (P-03)"),
            Map.entry("move-overlap", "MOVE between overlapping reference-modified parts of one item: Katana copies as if through a buffer, cobc -std=ibm with -fmove-ibm propagates byte by byte as the expectation does; R-088 said rt/Move is held to IBM's MOVE"),
            Map.entry("ssrange", "`$SET SSRANGE` (a directive cobc's ibm config honours): Katana ignores it, a subscript out of range is read as it stands and the run does not stop; the profile has no SSRANGE"),
            Map.entry("float-lit", "DISPLAY of numeric literals: a floating-point literal (1,23E0) is written as its mantissa digits and E0, where cobc -std=ibm writes its value; a plus-signed literal loses its sign, which cobc -std=ibm keeps (whether IBM keeps it waits for the Language Reference)"),
            Map.entry("invdata", "comparisons of numeric DISPLAY items holding spaces (invalid data, ibm.invdata NO): Katana, cobc -std=ibm and the expectation each give another result; IBM's is undefined without INVDATA, so none is the oracle")));

    /** The case to its key. */
    private static final Map<String, String> BY_CASE = Map.ofEntries(
            Map.entry("run_initialize-90", "d010"), Map.entry("run_misc-156", "d010"), Map.entry("run_misc-12873", "d010"),
            Map.entry("run_fundamental-146", "d010-cobc"), Map.entry("run_fundamental-193", "d010-cobc"), Map.entry("data_binary-1262", "d010-cobc"),
            Map.entry("run_subscripts-71", "setting"),
            Map.entry("run_file-5918", "harness"), Map.entry("used_binaries-1453", "harness"), Map.entry("run_file-975", "harness"),
            Map.entry("run_fundamental-7664", "debug-line"), Map.entry("run_fundamental-7715", "debug-line"),
            Map.entry("run_fundamental-7892", "debug-data"), Map.entry("run_fundamental-7933", "debug-data"),
            Map.entry("run_fundamental-7983", "debug-data"), Map.entry("run_fundamental-8071", "debug-data"),
            Map.entry("run_extensions-2831", "level78"), Map.entry("run_extensions-2854", "level78"), Map.entry("run_extensions-2880", "level78"),
            Map.entry("run_file-241", "files"), Map.entry("run_file-5646", "files"), Map.entry("run_file-5782", "files"),
            Map.entry("data_display-410", "pscale"), Map.entry("run_fundamental-8760", "pscale"), Map.entry("data_binary-2143", "pscale"),
            Map.entry("run_extensions-2785", "env-name"), Map.entry("run_fundamental-3100", "move-overlap"),
            Map.entry("run_subscripts-506", "ssrange"), Map.entry("run_fundamental-72", "float-lit"), Map.entry("run_misc-14909", "invdata"));

    /** A FAIL case's key; a case not sorted yet refuses, with what to do. */
    static String of(String id) {
        String key = BY_CASE.get(id);
        if (key == null) throw new AssertionError(id + " is a FAIL with no sorting: run it under cobc -std=ibm and add it to FailSorting (TODO 3.1, J-271)");
        return key;
    }

    /** The sorted cases, for the census to hold against its FAIL cases. */
    static java.util.Set<String> cases() {
        return BY_CASE.keySet();
    }
}
