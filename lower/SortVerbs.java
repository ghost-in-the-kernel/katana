package katana.lower;

import java.util.ArrayList;
import java.util.List;
import katana.sir.types.FileDescriptor;
import katana.sir.types.Statement;
import katana.sir.types.Value;

/**
 * SORT, MERGE, RELEASE and RETURN through `rt/SortFile` (LR 6.4 pp. 396, 429-430, 447-453; research §23):
 * the keys and the collating sequence set; USING files read into the SD's record area and released, or the
 * INPUT PROCEDURE run as a PERFORM; the records sorted; GIVING files written from them, or the OUTPUT
 * PROCEDURE run as a PERFORM. MERGE is a SORT of its USING files' records in the order the files are named,
 * which the stable sort keeps for equal keys.
 */
final class SortVerbs {
    private final Flow f;
    private final FileVerbs files;

    SortVerbs(Flow f, FileVerbs files) {
        this.f = f;
        this.files = files;
    }

    /** Whether a file is a sort-merge file (an SD). */
    static boolean sd(FileDescriptor d) {
        return d.fd() != null && d.fd().sort();
    }

    private FileDescriptor descriptor(String file) {
        return f.p.files().stream().filter(x -> x.name().equals(file)).findFirst().orElseThrow();
    }

    /** The SD's record area: its first 01. */
    private String area(FileDescriptor d) {
        return f.x.promoted.fieldName(d.records().get(0));
    }

    /** The SD's largest record: its record area's length. */
    private int length(FileDescriptor d) {
        return d.records().stream().mapToInt(r -> f.x.item(r).length()).max().orElseThrow();
    }

    /** SORT or MERGE (`verb`'s roles: `sort-...` or `merge-...`). */
    // Decision: R-026
    void statement(Statement s, String verb) {
        FileDescriptor sd = descriptor(((Value.File) Verbs.operand(s, verb + "-statement")).name());
        String file = FileVerbs.name(sd.name());
        List<String> keys = new ArrayList<>();
        boolean descending = false;
        String alphabet = "";
        for (Statement.Operand o : s.operands()) {
            if (o.role().equals(verb + "-on-key-clause") && o.value() instanceof Value.Name n) {
                if (n.text().equals("DESCENDING")) descending = true;
                if (n.text().equals("ASCENDING")) descending = false;
            }
            if (o.role().equals(verb + "-on-key-clause") && o.value() instanceof Value.Field k) keys.add("new SortFile.Key(" + f.x.field(k) + ", " + descending + ")");
            if (o.role().startsWith(verb + "-collating") && o.value() instanceof Value.Name n && n.kind().equals("alphabet")) alphabet = n.text();
            if (o.role().startsWith(verb + "-collating") && o.value() instanceof Value.Name n && !n.kind().equals("keyword")) alphabet = n.text();
        }
        String collation = alphabet.isEmpty() ? "co" : Collating.named(f.x, alphabet);
        f.e.line(file + ".begin(List.of(" + String.join(", ", keys) + "), " + collation + ");");
        List<FileDescriptor> using = files(s, verb + "-using");
        Value.Target input = target(s, verb + "-input-procedure-phrase", verb + "-input-through");
        if (input != null) {
            int back = f.label();
            f.e.line("return " + f.perform(input, back) + ";");
            f.split(back);
        }
        for (FileDescriptor u : using) read(u, sd);
        f.e.line(file + ".sort();");
        Value.Target output = target(s, verb + "-output-procedure-phrase", verb + "-output-through");
        if (output != null) {
            int back = f.label();
            f.e.line("return " + f.perform(output, back) + ";");
            f.split(back);
        }
        for (FileDescriptor g : files(s, verb + "-giving")) write(sd, g);
    }

    /** The files a phrase names, in order. */
    private List<FileDescriptor> files(Statement s, String role) {
        List<FileDescriptor> out = new ArrayList<>();
        for (Statement.Operand o : s.operands()) if (o.role().startsWith(role) && o.value() instanceof Value.File v) out.add(descriptor(v.name()));
        return out;
    }

    /** A procedure phrase's range: its first procedure through the THRU one, or its own last paragraph. */
    private static Value.Target target(Statement s, String phrase, String through) {
        Value.Target first = null, last = null;
        for (Statement.Operand o : s.operands()) {
            if (o.role().equals(phrase) && o.value() instanceof Value.Target t && first == null) first = t;
            if (o.role().equals(through) && o.value() instanceof Value.Target t) last = t;
        }
        if (first == null) return null;
        return new Value.Target(first.paragraph(), last == null ? first.thru() : last.thru(), first.section());
    }

    /**
     * USING: the file opened, each record read into its own area, moved into the SD's area as a group
     * (spaces past a shorter record) and released at the length it was read, the file closed (LR p. 452).
     */
    private void read(FileDescriptor u, FileDescriptor sd) {
        String in = FileVerbs.name(u.name()), rec = f.x.promoted.fieldName(u.records().get(0));
        f.e.line(in + ".open(SeqFile.Mode.INPUT);");
        // "If EXCEPTION/ERROR procedures are specified for these files, the compiler makes the necessary linkage" (LR p. 452): an OPEN that fails is the OPEN's error.
        files.after("OPEN", u.name(), false);
        f.e.open("for (;;)");
        if (FileVerbs.keyed(u)) f.e.line(in + ".readNext(" + rec + (FileVerbs.indexed(u) ? "" : ", null") + ");");
        else f.e.line(in + ".read(" + rec + ");");
        f.e.line("if (!" + in + ".successful()) break;");
        // Into the whole record area, the SD's largest record, not its first 01, which may be shorter.
        f.e.line("Move.group(" + in + ".current(" + rec + "), new Field(" + area(sd) + ".record(), " + area(sd) + ".offset(), Attr.text(" + length(sd) + ", false)));");
        f.e.line(FileVerbs.name(sd.name()) + ".release(" + area(sd) + ", Math.min(" + in + ".current(" + rec + ").attr().length(), " + length(sd) + "));");
        f.e.close();
        f.e.line(in + ".close(false, false, false);");
    }

    /**
     * GIVING: the file opened as OUTPUT, each sorted record written as by a WRITE without phrases (LR p.
     * 453), at its own length to a file whose records vary, else moved as a group into the record (spaces
     * past a shorter one); the file closed.
     */
    private void write(FileDescriptor sd, FileDescriptor g) {
        String out = FileVerbs.name(g.name()), rec = f.x.promoted.fieldName(g.records().get(0)), file = FileVerbs.name(sd.name());
        f.e.line(file + ".rewind();");
        f.e.line(out + ".open(SeqFile.Mode.OUTPUT);");
        files.after("OPEN", g.name(), false);
        f.e.open("while (" + file + ".returnNext(" + area(sd) + "))");
        boolean varies = FileDecls.sizes(f.p, g, f.x) != null;
        String record = varies ? "new Field(" + rec + ".record(), " + rec + ".offset(), Attr.text(" + file + ".current(" + area(sd) + ").attr().length(), false))" : rec;
        f.e.line("Move.group(" + file + ".current(" + area(sd) + "), " + record + ");");
        if (FileVerbs.keyed(g)) f.e.line(out + ".write(" + record + (FileVerbs.indexed(g) ? "" : ", null") + ");");
        else f.e.line(out + ".write(" + record + ", 0, 0);");
        f.e.close();
        f.e.line(out + ".close(false, false, false);");
    }

    /** RELEASE record [FROM x] (LR p. 429): FROM MOVEd in first, then the record released. */
    void release(Statement s) {
        Value.Field record = (Value.Field) Verbs.operand(s, "record-name");
        FileDescriptor sd = f.p.files().stream().filter(d -> d.records().contains(f.x.item(record.item()).record())).findFirst().orElseThrow();
        Value from = null;
        boolean seen = false;
        for (Statement.Operand o : s.operands()) {
            if (o.role().equals("release-statement") && o.value() instanceof Value.Name n && n.text().equals("FROM")) seen = true;
            else if (seen && from == null && o.role().equals("release-statement")) from = o.value();
        }
        if (from != null) f.e.line(f.x.moveInto(from, f.x.field(record), f.x.item(record.item())) + ";");
        // The record named is released at its own length, the SD's records varying among its 01s.
        f.e.line(FileVerbs.name(sd.name()) + ".release(" + area(sd) + ", " + f.x.item(record.item()).length() + ");");
    }

    /** RETURN file [INTO x] [AT END ... NOT AT END ...] (LR p. 430). */
    void returnStatement(Statement s) {
        FileDescriptor sd = descriptor(((Value.File) Verbs.operand(s, "return-statement")).name());
        f.e.line("ae = !" + FileVerbs.name(sd.name()) + ".returnNext(" + area(sd) + ");");
        Value into = null;
        for (Statement.Operand o : s.operands()) if (o.role().equals("return-into") && o.value() instanceof Value.Field v) into = v;
        if (into != null) f.e.line("if (!ae) " + f.x.moveInto(new Value.Field(sd.records().get(0), List.of(), null, null), f.x.receiving((Value.Field) into), f.x.item(((Value.Field) into).item())) + ";");
        f.branch("ae", Verbs.branch(s, "at-end-phrase"), List.of());
        f.branch("!ae", Verbs.branch(s, "not-at-end-phrase"), List.of());
    }
}
