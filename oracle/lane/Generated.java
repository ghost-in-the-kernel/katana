package katana.oracle.lane;

import java.util.ArrayList;
import java.util.List;

/**
 * The COBOL the cobc lane writes for itself (the study's `load_*.cbl`, `unload.cbl` and `CEE3ABD.cbl`,
 * made general): a loader and an unloader for an indexed file of any shape, the PARM driver for a
 * program whose PROCEDURE DIVISION has USING, and the stubs. Each fails loudly: a file status other
 * than success is written to stderr as `KATANA <what> <status>` with return code 12.
 */
final class Generated {
    /** An indexed file's shape: its record length, its key, and its alternate keys. */
    record Shape(int lrecl, Key key, List<Key> alternates) {
        /** A name for the shape's programs, distinct per shape. */
        String tag() {
            StringBuilder tag = new StringBuilder(lrecl + "-" + key.length() + "-" + key.offset());
            alternates.forEach(a -> tag.append("-a").append(a.length()).append('-').append(a.offset()).append(a.unique() ? "u" : "d"));
            return tag.toString();
        }
    }

    /** A key as IDCAMS writes it (length, offset), and whether its values are unique. */
    record Key(int length, int offset, boolean unique) {
        static Key of(String keys, boolean unique) {
            String[] k = keys.split(",");
            return new Key(Integer.parseInt(k[0]), Integer.parseInt(k[1]), unique);
        }
    }

    private Generated() {}

    /** Reads SEQIN's fixed records and writes them to KSOUT in that order (ACCESS SEQUENTIAL: ascending keys). */
    static String loader(Shape shape) {
        return program("KLOAD", shape, List.of(
                "OPEN INPUT SEQ-FILE OUTPUT KS-FILE",
                "IF SEQ-ST NOT = '00' OR KS-ST NOT = '00'",
                "    DISPLAY 'KATANA OPEN ' SEQ-ST ' ' KS-ST UPON SYSERR",
                "    MOVE 12 TO RETURN-CODE STOP RUN END-IF",
                "PERFORM UNTIL EOF = 'Y'",
                "  READ SEQ-FILE AT END MOVE 'Y' TO EOF",
                "  NOT AT END",
                "    WRITE KS-REC FROM SEQ-REC",
                "    IF KS-ST NOT = '00' AND KS-ST NOT = '02'",
                "      DISPLAY 'KATANA WRITE ' KS-ST UPON SYSERR",
                "      CLOSE SEQ-FILE KS-FILE",
                "      MOVE 12 TO RETURN-CODE STOP RUN END-IF",
                "  END-READ",
                "END-PERFORM",
                "CLOSE SEQ-FILE KS-FILE",
                "STOP RUN."));
    }

    /** Reads KSOUT in key order and writes its records to SEQIN: the logical unload (F-02). */
    static String unloader(Shape shape) {
        return program("KUNLOAD", shape, List.of(
                "OPEN INPUT KS-FILE OUTPUT SEQ-FILE",
                "IF SEQ-ST NOT = '00' OR KS-ST NOT = '00'",
                "    DISPLAY 'KATANA OPEN ' SEQ-ST ' ' KS-ST UPON SYSERR",
                "    MOVE 12 TO RETURN-CODE STOP RUN END-IF",
                "PERFORM UNTIL EOF = 'Y'",
                "  READ KS-FILE NEXT AT END MOVE 'Y' TO EOF",
                "  NOT AT END WRITE SEQ-REC FROM KS-REC",
                "  END-READ",
                "END-PERFORM",
                "CLOSE SEQ-FILE KS-FILE",
                "STOP RUN."));
    }

    /** The SELECTs, FDs and storage both programs share, with their procedure lines. */
    private static String program(String id, Shape shape, List<String> procedure) {
        List<String> lines = new ArrayList<>(List.of("IDENTIFICATION DIVISION.", "PROGRAM-ID. " + id + ".",
                "ENVIRONMENT DIVISION.", "INPUT-OUTPUT SECTION.", "FILE-CONTROL.",
                "    SELECT SEQ-FILE ASSIGN TO SEQIN ORGANIZATION IS SEQUENTIAL",
                "        FILE STATUS IS SEQ-ST.",
                "    SELECT KS-FILE ASSIGN TO KSOUT ORGANIZATION IS INDEXED",
                "        ACCESS MODE IS SEQUENTIAL RECORD KEY IS KS-KEY"));
        for (int i = 0; i < shape.alternates().size(); i++) {
            lines.add("        ALTERNATE RECORD KEY IS KS-ALT" + (i + 1) + (shape.alternates().get(i).unique() ? "" : " WITH DUPLICATES"));
        }
        lines.addAll(List.of("        FILE STATUS IS KS-ST.", "DATA DIVISION.", "FILE SECTION.",
                "FD SEQ-FILE.", "01 SEQ-REC PIC X(" + shape.lrecl() + ").",
                "FD KS-FILE.", "01 KS-REC PIC X(" + shape.lrecl() + ")."));
        lines.addAll(keyRecord("KS-R0", "KS-KEY", shape.key()));
        for (int i = 0; i < shape.alternates().size(); i++) lines.addAll(keyRecord("KS-R" + (i + 1), "KS-ALT" + (i + 1), shape.alternates().get(i)));
        lines.addAll(List.of("WORKING-STORAGE SECTION.", "01 SEQ-ST PIC XX.", "01 KS-ST PIC XX.",
                "01 EOF PIC X VALUE 'N'.", "PROCEDURE DIVISION."));
        lines.addAll(procedure);
        return fixed(lines);
    }

    private static List<String> keyRecord(String record, String name, Key key) {
        List<String> lines = new ArrayList<>(List.of("01 " + record + "."));
        if (key.offset() > 0) lines.add("    05 FILLER PIC X(" + key.offset() + ").");
        lines.add("    05 " + name + " PIC X(" + key.length() + ").");
        return lines;
    }

    /** A main program that passes the PARM as z/OS does (a halfword length, then the text) to a program with USING. */
    static String parmDriver(String program, String parm) {
        if (parm.length() > 100) throw new IllegalArgumentException("a PARM is at most 100 bytes: " + parm);
        return fixed(List.of("IDENTIFICATION DIVISION.", "PROGRAM-ID. KPARM.", "DATA DIVISION.",
                "WORKING-STORAGE SECTION.", "01 PARM-AREA.", "    05 PARM-LEN PIC S9(4) COMP.", "    05 PARM-TEXT PIC X(100).",
                "PROCEDURE DIVISION.", "MOVE " + parm.length() + " TO PARM-LEN",
                "MOVE '" + parm.replace("'", "''") + "' TO PARM-TEXT",
                "CALL '" + program + "' USING PARM-AREA",
                "STOP RUN."));
    }

    /** LE's CEE3ABD: the abend code to stderr as `CEE3ABD abend Unnnn` (the exit status keeps only 8 bits). */
    static String cee3abd() {
        return fixed(List.of("IDENTIFICATION DIVISION.", "PROGRAM-ID. CEE3ABD.", "DATA DIVISION.",
                "WORKING-STORAGE SECTION.", "01 SHOWN PIC 9(4).", "LINKAGE SECTION.",
                "01 ABCODE PIC S9(9) BINARY.", "01 TIMING PIC S9(9) BINARY.",
                "PROCEDURE DIVISION USING ABCODE TIMING.",
                "IF ADDRESS OF ABCODE = NULL",
                "    DISPLAY 'CEE3ABD abend with no code' UPON SYSERR",
                "    MOVE 16 TO RETURN-CODE",
                "ELSE",
                "    MOVE ABCODE TO SHOWN",
                "    DISPLAY 'CEE3ABD abend U' SHOWN UPON SYSERR",
                "    MOVE ABCODE TO RETURN-CODE",
                "END-IF",
                "STOP RUN."));
    }

    /** Fixed-form source: each line from column 8, none past column 72. */
    private static String fixed(List<String> lines) {
        StringBuilder text = new StringBuilder();
        for (String line : lines) {
            if (line.length() > 65) throw new IllegalArgumentException("past column 72: " + line);
            text.append("       ").append(line).append('\n');
        }
        return text.toString();
    }
}
