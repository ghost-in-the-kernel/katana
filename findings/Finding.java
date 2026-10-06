package katana.findings;

import java.util.Comparator;
import java.util.List;

/**
 * Something the input program does that Katana cannot make exact (F-08), said before any Java: its
 * kind, where in the COBOL it is (file:line:column), and what in particular.
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
        NE_07("NE-07", "GO TO out of a PERFORM range", "the PERFORM stays active, and whether reaching its exit later returns is cobc's reading, unsettled on IBM (R-021 C-3)");

        public final String code, title, why;

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
