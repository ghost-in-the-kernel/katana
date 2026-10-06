package katana.findings;

import java.util.Comparator;
import java.util.List;

/**
 * Something the input program does that Katana cannot make exact (F-08, `NE-`), or that is exact only
 * under a reading not yet proven on IBM (R-049's "valid under hypothesis", `HY-`), said before any Java:
 * its kind, where in the COBOL it is (file:line:column), and what in particular.
 */
public record Finding(Kind kind, String where, String detail) {
    /** The kinds, each a row: its code, its title, and why it cannot be made exact (docs/FINDINGS.md). */
    public enum Kind {
        NE_01("NE-01", "TRUNC(OPT)", "a binary item's value past its PICTURE depends on the code IBM's optimizer generated"),
        NE_02("NE-02", "overlapping PERFORM ranges", "which range's exit returns control depends on the order they were entered at run time"),
        NE_03("NE-03", "recursive PERFORM", "a range performed from inside itself has no defined return under IBM's rules"),
        NE_04("NE-04", "division without ON SIZE ERROR", "a division by zero leaves the receiving field as IBM's code left it, which is not defined"),
        NE_05("NE-05", "ODO data beyond the count", "bytes of a table past its OCCURS DEPENDING count are undefined, and a move of its record carries them"),
        NE_06("NE-06", "COMP-1 or COMP-2", "IBM's hexadecimal floating point has no exact Java equivalent"),
        NE_08("NE-08", "overlapping arithmetic operands", "IBM calls the result of an arithmetic statement whose operands share storage unpredictable; Katana takes cobc's reading"),
        HY_01("HY-01", "GO TO out of a PERFORM range", "exact under cobc's reading of PERFORM's return (R-021 C-1), which a mainframe run settles for every such program at once");

        public final String code, title, why;

        /** Whether the program is exact under a stated reading not yet proven on IBM (`HY-`), rather than not exact (`NE-`). */
        public boolean hypothesis() {
            return code.startsWith("HY-");
        }

        Kind(String code, String title, String why) {
            this.code = code;
            this.title = title;
            this.why = why;
        }
    }

    /** The findings report: one line each, by kind then place, said first in a build's output. */
    public static String report(List<Finding> findings) {
        StringBuilder out = new StringBuilder();
        findings.stream().sorted(Comparator.comparing((Finding f) -> f.kind()).thenComparing(Finding::where))
                .forEach(f -> out.append(f.kind().code).append(' ').append(f.kind().title).append(": ").append(f.detail()).append(" (").append(f.where()).append(")\n"));
        return out.toString();
    }
}
