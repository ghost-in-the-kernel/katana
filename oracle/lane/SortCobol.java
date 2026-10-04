package katana.oracle.lane;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The other way to run a SORT step (R-010, measured in phase 1.5): a COBOL program generated from the
 * cards, sorted by GnuCOBOL (`SORT ... WITH DUPLICATES IN ORDER`, an INPUT PROCEDURE that RELEASEs
 * the records INCLUDE keeps). The study's `sortstep.cbl` pattern, made general as far as keys and
 * INCLUDE/OMIT; OUTREC and concatenated input are refused, which is part of what R-010 weighs.
 */
final class SortCobol {
    private SortCobol() {}

    /** The program for a spec over records of this length; Unsupported for what it does not generate. */
    static String source(SortCards.Spec spec, int lrecl) {
        if (spec.outrec() != null) throw new SortCards.Unsupported("the generated sort has no OUTREC");
        Map<SortCards.Field, String> keys = new LinkedHashMap<>(), fields = new LinkedHashMap<>();
        spec.keys().forEach(k -> keys.putIfAbsent(k.field(), "K" + (keys.size() + 1)));
        if (spec.select() != null) collect(spec.select(), fields);
        List<String> lines = new ArrayList<>(List.of("IDENTIFICATION DIVISION.", "PROGRAM-ID. KSORT.",
                "ENVIRONMENT DIVISION.", "INPUT-OUTPUT SECTION.", "FILE-CONTROL.",
                "    SELECT IN-FILE ASSIGN TO SORTIN", "        ORGANIZATION IS SEQUENTIAL FILE STATUS IS IN-ST.",
                "    SELECT OUT-FILE ASSIGN TO SORTOUT", "        ORGANIZATION IS SEQUENTIAL.",
                "    SELECT WORK-FILE ASSIGN TO SORTWK.", "DATA DIVISION.", "FILE SECTION.",
                "FD IN-FILE.", "01 IN-REC PIC X(" + lrecl + ")."));
        fields.forEach((f, name) -> lines.addAll(record("IN-" + name, name, f, lrecl)));
        lines.addAll(List.of("FD OUT-FILE.", "01 OUT-REC PIC X(" + lrecl + ").", "SD WORK-FILE.", "01 W-REC PIC X(" + lrecl + ")."));
        keys.forEach((f, name) -> lines.addAll(record("W-" + name, name, f, lrecl)));
        lines.addAll(List.of("WORKING-STORAGE SECTION.", "01 IN-ST PIC XX.", "01 EOF PIC X VALUE 'N'.", "PROCEDURE DIVISION."));
        lines.add("    SORT WORK-FILE");
        for (SortCards.Key k : spec.keys()) lines.add("        ON " + (k.ascending() ? "ASCENDING" : "DESCENDING") + " KEY " + keys.get(k.field()));
        lines.addAll(List.of("        WITH DUPLICATES IN ORDER", "        INPUT PROCEDURE IS PICK", "        GIVING OUT-FILE",
                "    STOP RUN.", "PICK.", "    OPEN INPUT IN-FILE", "    PERFORM UNTIL EOF = 'Y'", "      READ IN-FILE AT END MOVE 'Y' TO EOF",
                "      NOT AT END"));
        if (spec.select() == null) lines.add("        RELEASE W-REC FROM IN-REC");
        else {
            lines.add("        IF " + (spec.omit() ? "NOT " : "") + "(");
            condition(spec.select(), fields, lines, "          ");
            lines.addAll(List.of("        ) RELEASE W-REC FROM IN-REC END-IF"));
        }
        lines.addAll(List.of("      END-READ", "    END-PERFORM", "    CLOSE IN-FILE."));
        return Generated.fixed(lines);
    }

    /** A record of the file that places one field at its position, as long as the file's records (a shorter one would make the file variable-length): `PIC X` for CH, `PIC 9` for ZD (unsigned: a sign overpunch is not read as the Java sort reads it). */
    private static List<String> record(String record, String name, SortCards.Field f, int lrecl) {
        List<String> lines = new ArrayList<>(List.of("01 " + record + "."));
        if (f.position() > 1) lines.add("    05 FILLER PIC X(" + (f.position() - 1) + ").");
        lines.add("    05 " + name + (f.format().equals("ZD") ? " PIC 9(" : " PIC X(") + f.length() + ").");
        int rest = lrecl - (f.position() - 1 + f.length());
        if (rest < 0) throw new SortCards.Unsupported("field " + f.position() + "," + f.length() + " runs past a " + lrecl + "-byte record");
        if (rest > 0) lines.add("    05 FILLER PIC X(" + rest + ").");
        return lines;
    }

    private static void collect(SortConditions.Cond cond, Map<SortCards.Field, String> fields) {
        if (cond instanceof SortConditions.Group g) g.parts().forEach(p -> collect(p, fields));
        if (cond instanceof SortConditions.Compare c) {
            fields.putIfAbsent(c.left(), "C" + (fields.size() + 1));
            if (c.right() != null) fields.putIfAbsent(c.right(), "C" + (fields.size() + 1));
        }
    }

    /** The condition as COBOL lines: CH fields compared with literals or fields, as DFSORT compares them. */
    private static void condition(SortConditions.Cond cond, Map<SortCards.Field, String> fields, List<String> lines, String indent) {
        if (cond instanceof SortConditions.Compare c) {
            String op = switch (c.op()) {
                case "EQ" -> "=";
                case "NE" -> "NOT =";
                case "GT" -> ">";
                case "GE" -> ">=";
                case "LT" -> "<";
                default -> "<=";
            };
            String right = c.constant() == null ? fields.get(c.right())
                    : c.constant().startsWith("C'") ? "'" + c.constant().substring(2, c.constant().length() - 1) + "'" : c.constant();
            lines.add(indent + fields.get(c.left()) + " " + op + " " + right);
            return;
        }
        SortConditions.Group g = (SortConditions.Group) cond;
        for (int i = 0; i < g.parts().size(); i++) {
            if (i > 0) lines.add(indent + (g.and() ? "AND" : "OR"));
            lines.add(indent + "(");
            condition(g.parts().get(i), fields, lines, indent + "  ");
            lines.add(indent + ")");
        }
    }
}
