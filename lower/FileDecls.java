package katana.lower;

import katana.sir.types.FileDescriptor;
import katana.sir.types.Value;

/** What a program's files are before its procedure runs: each file's object, its keys, LINAGE and record sizes, and the closing of all at the run unit's end. */
final class FileDecls {
    private FileDecls() {}

    /** Each file's object, by the name it is ASSIGNed to; with a relative or indexed file, the flags its INVALID KEY phrases read. */
    static void declare(Emitter e, katana.sir.types.Program p, Exprs x) {
        if (p.files().stream().anyMatch(FileVerbs::keyed)) e.line("private boolean ik, ok;");
        for (FileDescriptor d : p.files()) {
            String type = type(d);
            e.line("private final " + type + " " + FileVerbs.name(d.name()) + (external(d) ? ";" : " = " + made(d, x) + ";"));
        }
    }

    /** In the constructor: each EXTERNAL file the run unit's one connector of that name, the first program to describe it making it (LANE-19). */
    static void externals(Emitter e, katana.sir.types.Program p, Exprs x) {
        for (FileDescriptor d : p.files()) if (external(d)) e.line(FileVerbs.name(d.name()) + " = u.externalFile(" + Emitter.quote(d.name()) + ", () -> " + made(d, x) + ");");
    }

    static boolean external(FileDescriptor d) {
        return d.fd() != null && d.fd().external() && !SortVerbs.sd(d);
    }

    private static String type(FileDescriptor d) {
        if (SortVerbs.sd(d)) return "SortFile";
        if (FileVerbs.keyed(d)) return FileVerbs.relative(d) ? "RelFile" : "IdxFile";
        return "SeqFile";
    }

    /**
     * The word a file's ASSIGN clause begins with that IBM's does not have, EXTERNAL or DYNAMIC, else null: IBM's ASSIGN names a ddname (or USING a data item), and cobc -std=ibm's
     * "ASSIGN TO EXTERNAL name" takes name from the environment, which no lane passes and IBM lacks (R-090; recollection, R-082). The SIR keeps the clause's words, so the name here is "EXTERNAL name".
     */
    // Decision: R-090
    // Unsure: a literal ASSIGN whose payload begins "EXTERNAL " or "DYNAMIC " reads as the clause words, the SIR holding both as one string. Stays: a program with such a literal settles it; none is known, and the refusal names the file.
    static String foreignAssign(FileDescriptor d) {
        for (String w : new String[] {"EXTERNAL", "DYNAMIC"}) if (d.assign().startsWith(w + " ")) return w;
        return null;
    }

    /** A file's object as made; an ASSIGN IBM lacks is a finding, the object failing where the program is built. */
    private static String made(FileDescriptor d, Exprs x) {
        String foreign = foreignAssign(d);
        if (foreign != null && !SortVerbs.sd(d)) return x.unsupported("file " + d.name() + " ASSIGN " + foreign + " " + d.assign().substring(foreign.length() + 1) + ": IBM's ASSIGN names a ddname, not an environment name (R-090; recollection, R-082)");
        if (SortVerbs.sd(d)) return "new SortFile()";
        if (FileVerbs.keyed(d)) return "new " + type(d) + "(" + Emitter.quote(d.assign()) + ", " + Emitter.quote(d.name()) + ", " + (d.status() != 0) + ", " + d.optional() + ", KeyedFile.Access." + d.access() + ")";
        boolean lines = d.organization() == FileDescriptor.Organization.LINE_SEQUENTIAL;
        return "new SeqFile(" + Emitter.quote(d.assign()) + ", " + Emitter.quote(d.name()) + ", " + lines + ", " + (d.status() != 0) + ", " + d.optional() + ")";
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
        for (FileDescriptor d : p.files()) if (!SortVerbs.sd(d)) e.line(FileVerbs.name(d.name()) + ".close();");
        e.close();
    }

    /** Each indexed file given its record area, its largest record and its keys, prime first, each alternate with DUPLICATES or not (LR pp. 150-152). */
    static void keys(Emitter e, katana.sir.types.Program p, Exprs x) {
        for (FileDescriptor d : p.files()) {
            if (!FileVerbs.indexed(d) || d.records().isEmpty()) continue;
            StringBuilder fields = new StringBuilder(x.promoted.fieldName(d.recordKey())), dups = new StringBuilder("false");
            for (FileDescriptor.AlternateKey a : d.alternateKeys()) {
                fields.append(", ").append(x.promoted.fieldName(a.item()));
                dups.append(", ").append(a.duplicates());
            }
            e.line(FileVerbs.name(d.name()) + ".keys(" + x.promoted.fieldName(d.records().get(0)) + ", " + d.maxRecord() + ", new Field[] {" + fields + "}, new boolean[] {" + dups + "});");
        }
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
                counter = "new Field(Record.of(" + i.length() + ", cp), 0, " + Storage.attr(p, i, x) + ")";
            } else {
                int digits = ((Value.Literal) l.body()).payload().length();
                counter = "new Field(Record.of(" + (digits <= 4 ? 2 : digits <= 9 ? 4 : 8) + ", cp), 0, Attr.numeric(Attr.Usage.BINARY, " + digits + ", 0, false))";
            }
            e.line(FileVerbs.name(d.name()) + ".linage(" + counter + ", " + supplier(l.body(), x) + ", " + supplier(l.footing(), x) + ", " + supplier(l.top(), x) + ", " + supplier(l.bottom(), x) + ");");
        }
    }

    /** Each variable-length sequential, relative or indexed file given its record sizes and its DEPENDING ON item (research §13). */
    static void varyings(Emitter e, katana.sir.types.Program p, Exprs x) {
        for (FileDescriptor d : p.files()) {
            int[] s = sizes(p, d, x);
            if (s == null) continue;
            String depending = d.fd().recordDepending() == 0 ? "null" : x.promoted.fieldName(d.fd().recordDepending());
            e.line(FileVerbs.name(d.name()) + ".varying(" + s[0] + ", " + s[1] + ", " + d.fd().varying() + ", " + s[2] + ", " + s[3] + ", " + depending + ");");
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
}
