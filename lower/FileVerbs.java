package katana.lower;

import katana.sir.types.FileDescriptor;
import katana.sir.types.Statement;
import katana.sir.types.Value;

/** OPEN, CLOSE, WRITE and READ on sequential files, through `rt/SeqFile`; relative and indexed files wait for phase 4. */
final class FileVerbs {
    private final Flow f;

    FileVerbs(Flow f) {
        this.f = f;
    }

    static String name(String file) {
        return "file_" + file.replaceAll("[^A-Za-z0-9]", "_");
    }

    /** Each file's object, by the name it is ASSIGNed to. */
    static void declare(Emitter e, katana.sir.types.Program p) {
        for (FileDescriptor d : p.files()) {
            boolean lines = d.organization() == FileDescriptor.Organization.LINE_SEQUENTIAL;
            e.line("private final SeqFile " + name(d.name()) + " = new SeqFile(" + Emitter.quote(d.assign()) + ", " + Emitter.quote(d.name()) + ", " + lines + ", " + (d.status() != 0) + ");");
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
        for (FileDescriptor d : p.files()) e.line(name(d.name()) + ".close();");
        e.close();
    }

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
            else if (o.value() instanceof Value.File file) f.e.line(name(file.name()) + ".open(SeqFile.Mode." + mode + ");");
        }
    }

    void close(Statement s) {
        for (Statement.Operand o : s.operands()) if (o.value() instanceof Value.File file) f.e.line(name(file.name()) + ".close();");
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

    private static String supplier(Value v, Exprs x) {
        return v == null ? "null" : "() -> (int) " + x.index(v);
    }

    /** WRITE record [FROM x] [BEFORE|AFTER ADVANCING n LINES|PAGE] [AT END-OF-PAGE ... NOT AT END-OF-PAGE ...]. */
    void write(Statement s) {
        Value.Field record = (Value.Field) Verbs.operand(s, "record-name");
        String file = owner(record.item());
        Value from = Verbs.operand(s, "write-from-phrase");
        if (from != null) f.e.line(f.x.moveInto(from, f.x.field(record), f.x.item(record.item())) + ";");
        boolean before = Verbs.keyword(s, "BEFORE");
        Value lines = Verbs.operand(s, "write-advancing-lines");
        if (paged(record.item())) {
            String n = Verbs.keyword(s, "PAGE") ? "-1" : lines != null ? "(int) " + f.x.index(lines) : "1";
            f.e.line("ae = " + file + ".page(" + f.x.field(record) + ", " + before + ", " + n + ");");
            f.branch("ae", Verbs.branch(s, "write-at-end-of-page-phrase"), Verbs.branch(s, "write-not-at-end-of-page-phrase"));
            return;
        }
        String count = Verbs.keyword(s, "PAGE") ? "-1" : lines != null ? "(int) " + f.x.index(lines) : (Verbs.keyword(s, "ADVANCING") || before || Verbs.keyword(s, "AFTER")) ? "1" : "0";
        f.e.line(file + ".write(" + f.x.field(record) + ", " + (before ? count : "0") + ", " + (before ? "0" : count) + ");");
    }

    /** READ file [INTO x] [AT END ... NOT AT END ...]. */
    void read(Statement s) {
        Value.File file = (Value.File) Verbs.operand(s, "read-statement");
        FileDescriptor d = f.p.files().stream().filter(x -> x.name().equals(file.name())).findFirst().orElseThrow();
        String record = "f" + d.records().get(0);
        f.e.line("ae = !" + name(file.name()) + ".read(" + record + ");");
        Value into = Verbs.operand(s, "read-into");
        if (into != null) f.e.line("if (!ae) " + f.x.moveInto(new Value.Field(d.records().get(0), java.util.List.of(), null, null), f.x.receiving((Value.Field) into), f.x.item(((Value.Field) into).item())) + ";");
        f.branch("ae", Verbs.branch(s, "at-end-phrase"), Verbs.branch(s, "not-at-end-phrase"));
    }

    /** Whether a record's file has a LINAGE clause. */
    private boolean paged(int item) {
        int record = f.x.item(item).record();
        return f.p.files().stream().anyMatch(d -> d.records().contains(record) && d.fd() != null && d.fd().linage() != null);
    }

    /** The file whose FD holds a record. */
    private String owner(int item) {
        int record = f.x.item(item).record();
        for (FileDescriptor d : f.p.files()) if (d.records().contains(record)) return name(d.name());
        return f.x.unsupported("WRITE of item " + item + ", in no file's FD");
    }
}
