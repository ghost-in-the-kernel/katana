package katana.lower;

import java.util.List;
import katana.sir.types.FileDescriptor;
import katana.sir.types.Statement;
import katana.sir.types.Value;

/** OPEN, CLOSE, WRITE, REWRITE and READ on sequential files, through `rt/SeqFile`; on relative and indexed files with START and DELETE, through `rt/RelFile` and `rt/IdxFile` (research §20, §21). */
final class FileVerbs {
    private final Flow f;

    FileVerbs(Flow f) {
        this.f = f;
    }

    static String name(String file) {
        return "file_" + file.replaceAll("[^A-Za-z0-9]", "_");
    }

    /** OPEN: each file in the mode written before it, NO REWIND kept (07), the status handled after each file (J-152). */
    // Decision: R-019
    void open(Statement s) {
        String mode = "OUTPUT";
        for (Statement.Operand o : s.operands()) {
            if (o.value() instanceof Value.Name n && n.kind().equals("keyword")) mode = switch (n.text()) {
                case "INPUT" -> "INPUT";
                case "EXTEND" -> "EXTEND";
                case "I-O" -> "IO";
                case "OUTPUT" -> "OUTPUT";
                default -> mode;
            };
            else if (o.value() instanceof Value.File file) {
                f.e.line(name(file.name()) + ".open(SeqFile.Mode." + mode + ");");
                if (noRewind(s, file)) f.e.line(name(file.name()) + ".noRewind();");
                after("OPEN", file.name(), false);
            }
        }
    }

    /** CLOSE file [WITH LOCK] ...: each file closed, locked when LOCK follows it before the next file. */
    void close(Statement s) {
        List<Statement.Operand> ops = s.operands();
        for (int k = 0; k < ops.size(); k++) {
            if (!(ops.get(k).value() instanceof Value.File file)) continue;
            java.util.Set<String> words = new java.util.HashSet<>();
            for (int j = k + 1; j < ops.size() && !(ops.get(j).value() instanceof Value.File); j++) if (ops.get(j).value() instanceof Value.Name n) words.add(n.text());
            boolean reel = words.contains("REEL") || words.contains("UNIT");
            f.e.line(name(file.name()) + ".close(" + words.contains("LOCK") + ", " + reel + ", " + (words.contains("REWIND") || words.contains("REMOVAL")) + ");");
            after("CLOSE", file.name(), false);
        }
    }

    static boolean relative(FileDescriptor d) {
        return d.organization() == FileDescriptor.Organization.RELATIVE;
    }

    static boolean indexed(FileDescriptor d) {
        return d.organization() == FileDescriptor.Organization.INDEXED;
    }

    static boolean keyed(FileDescriptor d) {
        return relative(d) || indexed(d);
    }

    /** A data-name the statement names after KEY (`role`), as a field, or null with none. */
    private String keyItem(Statement s, String role) {
        for (Statement.Operand o : s.operands()) if (o.role().equals(role) && o.value() instanceof Value.Field v) return f.x.field(v);
        return "null";
    }

    private FileDescriptor descriptor(String file) {
        return f.p.files().stream().filter(x -> x.name().equals(file)).findFirst().orElseThrow();
    }

    /** A relative file's RELATIVE KEY as a field, or null when it has none. */
    private String key(FileDescriptor d) {
        return d.relativeKey() == 0 ? "null" : f.x.field(new Value.Field(d.relativeKey(), List.of(), null, null));
    }

    /** Whether a statement has a phrase, even an empty one. */
    private static boolean phrase(Statement s, String label) {
        return s.branches().stream().anyMatch(b -> b.label().equals(label));
    }

    /**
     * After a statement on a relative file that can find an invalid key (LR pp. 303-304): the flags taken,
     * then the status and a declarative unless an INVALID KEY phrase handles it, then INVALID KEY, or NOT
     * INVALID KEY when the statement succeeded.
     */
    private void invalidKey(Statement s, String verb, String file) {
        String v = name(file);
        f.e.line("ik = " + v + ".invalid(); ok = " + v + ".successful();");
        after(verb, file, phrase(s, "invalid-key-phrase"));
        f.branch("ik", Verbs.branch(s, "invalid-key-phrase"), List.of());
        f.branch("ok", Verbs.branch(s, "not-invalid-key-phrase"), List.of());
    }

    /** START file [KEY relation data-name] [INVALID KEY ...] (LR p. 456): EQUAL, GREATER or NOT LESS, by its words or symbols. */
    // Decision: R-019
    void start(Statement s) {
        Value.File file = (Value.File) Verbs.operand(s, "start-statement");
        java.util.Set<String> words = new java.util.HashSet<>();
        for (Statement.Operand o : s.operands()) if (o.role().equals("start-key") && o.value() instanceof Value.Name n) words.add(n.text().toUpperCase());
        boolean greater = words.contains("GREATER") || words.contains(">"), orEqual = words.contains("OR") || words.contains(">=");
        String relation = words.contains("NOT") || greater && orEqual || words.contains(">=") ? "NOT_LESS" : greater ? "GREATER" : "EQUAL";
        FileDescriptor d = descriptor(file.name());
        String item = indexed(d) ? keyItem(s, "start-key") : key(d);
        f.e.line(name(file.name()) + ".start(" + item + ", KeyedFile.Relation." + relation + ");");
        invalidKey(s, "START", file.name());
    }

    /** DELETE file [RECORD] [INVALID KEY ...] (LR p. 332). */
    void delete(Statement s) {
        Value.File file = (Value.File) Verbs.operand(s, "delete-statement");
        FileDescriptor d = descriptor(file.name());
        f.e.line(name(file.name()) + ".delete(" + (indexed(d) ? "" : key(d)) + ");");
        invalidKey(s, "DELETE", file.name());
    }

    /** Whether NO REWIND follows a file in an OPEN, before the next file. */
    private static boolean noRewind(Statement s, Value.File file) {
        List<Statement.Operand> ops = s.operands();
        int at = -1;
        for (int k = 0; k < ops.size() && at < 0; k++) if (ops.get(k).value().equals(file)) at = k;
        for (int j = at + 1; j < ops.size() && !(ops.get(j).value() instanceof Value.File); j++) if (ops.get(j).value() instanceof Value.Name n && n.text().equals("REWIND")) return true;
        return false;
    }

    /**
     * After an I-O statement (LR pp. 299, 705-706): the status into the FILE STATUS item, then, on an
     * error (3x, 4x, or at end with no AT END phrase), the declarative USE'd for the file by name, else
     * the one for the mode it is open in or being opened in, run as a PERFORM that comes back here; with
     * none and no FILE STATUS item, the run ends (PG p. 243); with a FILE STATUS item, it goes on.
     */
    // Decision: R-019
    void after(String verb, String fileName, boolean atEndPhrase) {
        FileDescriptor d = f.p.files().stream().filter(x -> x.name().equals(fileName)).findFirst().orElseThrow();
        String file = name(fileName);
        if (d.status() != 0) f.e.line("Move.move(Literal.text(" + file + ".status(), cp), " + f.x.field(new Value.Field(d.status(), List.of(), null, null)) + ");");
        String named = null;
        java.util.Map<String, String> modes = new java.util.LinkedHashMap<>();
        for (katana.sir.types.Procedure.Section sec : f.p.procedure().sections()) {
            if (sec.use() == null || sec.use().kind() != katana.sir.types.Procedure.Use.Kind.ERROR) continue;
            String range = range(sec.name());
            if (sec.use().files().contains(fileName) && named == null) named = range;
            if (!sec.use().mode().isEmpty()) modes.putIfAbsent(sec.use().mode().equals("I-O") ? "IO" : sec.use().mode(), range);
        }
        String fallback = d.status() != 0 ? "" : "throw " + file + ".abend(" + Emitter.quote(verb) + ");";
        if (named == null && modes.isEmpty() && fallback.isEmpty()) return;
        f.e.open("if (" + file + ".failed(" + atEndPhrase + "))");
        if (named != null) f.e.line("use(" + named + ");");
        else {
            StringBuilder chain = new StringBuilder();
            for (var m : modes.entrySet()) chain.append("if (" + file + ".mode() == SeqFile.Mode." + m.getKey() + ") use(" + m.getValue() + "); else ");
            f.e.line(chain + (fallback.isEmpty() ? "{ }" : "{ " + fallback + " }"));
        }
        f.e.close();
    }

    /** A declarative section's paragraphs, first and last, for `use`. */
    private String range(String section) {
        List<katana.sir.types.Procedure.Paragraph> ps = f.p.procedure().paragraphs().stream().filter(g -> g.section().equals(section)).toList();
        return ps.get(0).index() + ", " + ps.get(ps.size() - 1).index();
    }

    /** WRITE record [FROM x] [BEFORE|AFTER ADVANCING n LINES|PAGE] [AT END-OF-PAGE ... NOT AT END-OF-PAGE ...]. */
    void write(Statement s) {
        Value.Field record = (Value.Field) Verbs.operand(s, "record-name");
        String file = owner(record.item());
        Value from = Verbs.operand(s, "write-from-phrase");
        if (from != null) f.e.line(f.x.moveInto(from, f.x.field(record), f.x.item(record.item())) + ";");
        FileDescriptor owner = descriptor(fileOf(record.item()));
        if (keyed(owner)) {
            f.e.line(file + ".write(" + f.x.field(record) + (indexed(owner) ? "" : ", " + key(owner)) + ");");
            invalidKey(s, "WRITE", owner.name());
            return;
        }
        boolean before = Verbs.keyword(s, "BEFORE");
        Value lines = Verbs.operand(s, "write-advancing-lines");
        String channel = channel(s);
        if (paged(record.item())) {
            String n = Verbs.keyword(s, "PAGE") ? "-1" : channel != null ? channel : lines != null ? "(int) " + f.x.index(lines) : "1";
            f.e.line("ae = " + file + ".page(" + f.x.field(record) + ", " + before + ", " + n + ");");
            after("WRITE", fileOf(record.item()), false);
            f.branch("ae", Verbs.branch(s, "write-at-end-of-page-phrase"), List.of());
            f.branch(file + ".successful() && !ae", Verbs.branch(s, "write-not-at-end-of-page-phrase"), List.of());
            return;
        }
        String count = Verbs.keyword(s, "PAGE") ? "-1" : channel != null ? channel : lines != null ? "(int) " + f.x.index(lines) : (Verbs.keyword(s, "ADVANCING") || before || Verbs.keyword(s, "AFTER")) ? "1" : "0";
        f.e.line(file + ".write(" + f.x.field(record) + ", " + (before ? count : "0") + ", " + (before ? "0" : count) + ");");
        after("WRITE", fileOf(record.item()), false);
    }

    /**
     * WRITE ... ADVANCING mnemonic-name (LR 6.4 p. 473: "a skip to channels 1 through 12, or space
     * suppression"), by its environment-name in SPECIAL-NAMES: C01, channel 1, a new page (-1); CSP, no
     * line advanced (0), as the lane reads them (LANE-12). Where channels 2 to 12 stop is the printer's
     * carriage-control tape, and stacker selection a card punch's, so they are not lowered. Null with no
     * mnemonic-name.
     */
    // Decision: R-019
    private String channel(Statement s) {
        for (Statement.Operand o : s.operands()) {
            if (!(o.value() instanceof Value.Name n && n.kind().equals("mnemonic"))) continue;
            String env = f.p.header().mnemonics().stream().filter(m -> m.name().equals(n.text())).map(katana.sir.types.Header.Mnemonic::environment).findFirst().orElse("");
            return switch (env) {
                case "C01" -> "-1";
                case "CSP" -> "0";
                default -> f.x.unsupported("WRITE ADVANCING " + n.text() + (env.isEmpty() ? "" : " (" + env + ")") + ": where it stops is the device's");
            };
        }
        return null;
    }

    /** REWRITE record [FROM x] (LR 6.4 p. 432): the FROM item MOVEd into the record first. */
    void rewrite(Statement s) {
        Value.Field record = (Value.Field) Verbs.operand(s, "record-name");
        Value from = Verbs.operand(s, "rewrite-from");
        if (from != null) f.e.line(f.x.moveInto(from, f.x.field(record), f.x.item(record.item())) + ";");
        FileDescriptor owner = descriptor(fileOf(record.item()));
        if (keyed(owner)) {
            f.e.line(name(owner.name()) + ".rewrite(" + f.x.field(record) + (indexed(owner) ? "" : ", " + key(owner)) + ");");
            invalidKey(s, "REWRITE", owner.name());
            return;
        }
        f.e.line(owner(record.item()) + ".rewrite(" + f.x.field(record) + ");");
        after("REWRITE", fileOf(record.item()), false);
    }

    /** READ file [INTO x] [AT END ... NOT AT END ...]. */
    void read(Statement s) {
        Value.File file = (Value.File) Verbs.operand(s, "read-statement");
        FileDescriptor d = f.p.files().stream().filter(x -> x.name().equals(file.name())).findFirst().orElseThrow();
        String record = f.x.promoted.fieldName(d.records().get(0));
        String fileVar = name(file.name());
        // A relative or indexed file is read by its key with random access, and with dynamic access without NEXT (LR pp. 427-428).
        boolean random = keyed(d) && (d.access() == FileDescriptor.Access.RANDOM || d.access() == FileDescriptor.Access.DYNAMIC && !Verbs.keyword(s, "NEXT"));
        if (random) f.e.line(fileVar + ".read(" + record + ", " + (indexed(d) ? keyItem(s, "read-key") : key(d)) + ");");
        else if (keyed(d)) f.e.line(fileVar + ".readNext(" + record + (indexed(d) ? "" : ", " + key(d)) + ");");
        else f.e.line(fileVar + ".read(" + record + ");");
        if (random) f.e.line("ik = " + fileVar + ".invalid(); ok = " + fileVar + ".successful();");
        f.e.line("ae = " + fileVar + (keyed(d) ? ".atEnd();" : ".status().equals(\"10\");"));
        Value into = Verbs.operand(s, "read-into");
        // A variable file's record moves at the length read, a group move (LR 6.4 pp. 188, 425).
        if (into != null && FileDecls.sizes(f.p, d, f.x) != null) f.e.line("if (" + fileVar + ".successful()) Move.group(" + fileVar + ".current(" + record + "), " + f.x.receiving((Value.Field) into) + ");");
        else if (into != null) f.e.line("if (" + fileVar + ".successful()) " + f.x.moveInto(new Value.Field(d.records().get(0), java.util.List.of(), null, null), f.x.receiving((Value.Field) into), f.x.item(((Value.Field) into).item())) + ";");
        List<Statement> atEnd = Verbs.branch(s, "at-end-phrase");
        if (random) {
            after("READ", file.name(), phrase(s, "invalid-key-phrase"));
            f.branch("ik", Verbs.branch(s, "invalid-key-phrase"), List.of());
            f.branch("ok", Verbs.branch(s, "not-invalid-key-phrase"), List.of());
            return;
        }
        after("READ", file.name(), !atEnd.isEmpty() || phrase(s, "at-end-phrase"));
        f.branch("ae", atEnd, List.of());
        f.branch(fileVar + ".successful()", Verbs.branch(s, "not-at-end-phrase"), List.of());
    }

    /** Whether a record's file has a LINAGE clause. */
    private boolean paged(int item) {
        int record = f.x.item(item).record();
        return f.p.files().stream().anyMatch(d -> d.records().contains(record) && d.fd() != null && d.fd().linage() != null);
    }

    /** The name of the file whose FD holds a record. */
    private String fileOf(int item) {
        int record = f.x.item(item).record();
        return f.p.files().stream().filter(d -> d.records().contains(record)).findFirst().orElseThrow().name();
    }

    /** The file whose FD holds a record. */
    private String owner(int item) {
        int record = f.x.item(item).record();
        for (FileDescriptor d : f.p.files()) if (d.records().contains(record)) return name(d.name());
        return f.x.unsupported("WRITE of item " + item + ", in no file's FD");
    }
}
