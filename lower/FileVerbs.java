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

    /** Each file's object, by the name it is ASSIGNed to; with a relative or indexed file, the flags its INVALID KEY phrases read. */
    static void declare(Emitter e, katana.sir.types.Program p) {
        if (p.files().stream().anyMatch(FileVerbs::keyed)) e.line("private boolean ik, ok;");
        for (FileDescriptor d : p.files()) {
            if (SortVerbs.sd(d)) {
                e.line("private final SortFile " + name(d.name()) + " = new SortFile();");
                continue;
            }
            if (keyed(d)) {
                String type = relative(d) ? "RelFile" : "IdxFile";
                e.line("private final " + type + " " + name(d.name()) + " = new " + type + "(" + Emitter.quote(d.assign()) + ", " + Emitter.quote(d.name()) + ", " + (d.status() != 0) + ", " + d.optional() + ", KeyedFile.Access." + d.access() + ");");
                continue;
            }
            boolean lines = d.organization() == FileDescriptor.Organization.LINE_SEQUENTIAL;
            e.line("private final SeqFile " + name(d.name()) + " = new SeqFile(" + Emitter.quote(d.assign()) + ", " + Emitter.quote(d.name()) + ", " + lines + ", " + (d.status() != 0) + ", " + d.optional() + ");");
        }
    }

    /**
     * Every file closed at the run unit's end, normal or abnormal: "When the run unit ends normally, the
     * run time closes all open files", and "If the run unit ends abnormally and the TRAP(ON) runtime
     * option is in effect, the run time closes all open files" (PG 6.4 p. 179), TRAP(ON,SPIE) being LE's
     * default for non-CICS applications; a record written before an abend reaches the file.
     */
    // Decision: R-019
    static void closer(Emitter e, katana.sir.types.Program p) {
        e.open("void closeFiles()");
        for (FileDescriptor d : p.files()) if (!SortVerbs.sd(d)) e.line(name(d.name()) + ".close();");
        e.close();
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

    /** Each indexed file given its record area, its largest record and its keys, prime first, each alternate with DUPLICATES or not (LR pp. 150-152). */
    static void keys(Emitter e, katana.sir.types.Program p, Exprs x) {
        for (FileDescriptor d : p.files()) {
            if (!indexed(d) || d.records().isEmpty()) continue;
            StringBuilder fields = new StringBuilder("f" + d.recordKey()), dups = new StringBuilder("false");
            for (FileDescriptor.AlternateKey a : d.alternateKeys()) {
                fields.append(", f").append(a.item());
                dups.append(", ").append(a.duplicates());
            }
            e.line(name(d.name()) + ".keys(f" + d.records().get(0) + ", " + d.maxRecord() + ", new Field[] {" + fields + "}, new boolean[] {" + dups + "});");
        }
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

    /**
     * Each LINAGE file given its LINAGE-COUNTER and its page values (SIR-11): the counter a binary item
     * of the page body's digits, or of its data-name's PICTURE and USAGE (LR 6.4 p. 23); each value read
     * when a page begins.
     */
    // Decision: R-019
    static void linages(Emitter e, katana.sir.types.Program p, Exprs x) {
        for (FileDescriptor d : p.files()) {
            if (d.fd() == null || d.fd().linage() == null) continue;
            FileDescriptor.Linage l = d.fd().linage();
            String counter;
            if (l.body() instanceof Value.Field b) {
                katana.sir.types.Item i = x.item(b.item());
                counter = "new Field(Record.of(" + i.length() + ", cp), 0, " + Lower.attr(p, i, x) + ")";
            } else {
                int digits = ((Value.Literal) l.body()).payload().length();
                counter = "new Field(Record.of(" + (digits <= 4 ? 2 : digits <= 9 ? 4 : 8) + ", cp), 0, Attr.numeric(Attr.Usage.BINARY, " + digits + ", 0, false))";
            }
            e.line(name(d.name()) + ".linage(" + counter + ", " + supplier(l.body(), x) + ", " + supplier(l.footing(), x) + ", " + supplier(l.top(), x) + ", " + supplier(l.bottom(), x) + ");");
        }
    }

    /** Each variable-length sequential, relative or indexed file given its record sizes and its DEPENDING ON item (research §13). */
    static void varyings(Emitter e, katana.sir.types.Program p, Exprs x) {
        for (FileDescriptor d : p.files()) {
            int[] s = sizes(p, d, x);
            if (s == null) continue;
            String depending = d.fd().recordDepending() == 0 ? "null" : "f" + d.fd().recordDepending();
            e.line(name(d.name()) + ".varying(" + s[0] + ", " + s[1] + ", " + d.fd().varying() + ", " + s[2] + ", " + s[3] + ", " + depending + ");");
        }
    }

    /**
     * A sequential, relative or indexed file's least and greatest 01 sizes and its bounds, or null for fixed-length records:
     * variable for RECORD IS VARYING, or, unless RECORD CONTAINS gives one size (format 1, PG 6.4 p. 169),
     * for 01 records of different sizes or one holding an ODO table (PG p. 170). The least size counts
     * an ODO table at its fewest occurrences (LR p. 188); VARYING's FROM and TO are its bounds when written.
     */
    // Decision: R-019
    static int[] sizes(katana.sir.types.Program p, FileDescriptor d, Exprs x) {
        if (d.organization() == FileDescriptor.Organization.LINE_SEQUENTIAL || d.fd() == null || d.fd().sort() || d.records().isEmpty()) return null;
        FileDescriptor.Fd fd = d.fd();
        int least = Integer.MAX_VALUE, greatest = 0;
        boolean odo = false;
        for (int r : d.records()) {
            katana.sir.types.Item record = x.item(r);
            int fewest = record.length();
            for (katana.sir.types.Item t : p.items()) {
                if (t.record() != r || t.occurs() == null || t.occurs().dependingOn() == 0) continue;
                odo = true;
                fewest = Math.min(fewest, t.offset() - record.offset() + t.occurs().min() * t.length());
            }
            least = Math.min(least, fewest);
            greatest = Math.max(greatest, record.length());
        }
        boolean format1 = !fd.varying() && fd.recordFrom() > 0 && fd.recordTo() == fd.recordFrom();
        if (!fd.varying() && (format1 || (least == greatest && !odo))) return null;
        return new int[] {least, greatest, fd.recordFrom() > 0 ? fd.recordFrom() : least, fd.recordTo() > 0 ? fd.recordTo() : greatest};
    }

    private static String supplier(Value v, Exprs x) {
        return v == null ? "null" : "() -> (int) " + x.index(v);
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
        String record = "f" + d.records().get(0);
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
        if (into != null && sizes(f.p, d, f.x) != null) f.e.line("if (" + fileVar + ".successful()) Move.group(" + fileVar + ".current(" + record + "), " + f.x.receiving((Value.Field) into) + ");");
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
