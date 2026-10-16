package katana.lower;

import java.util.ArrayList;
import java.util.List;
import katana.sir.types.Attributes;
import katana.sir.types.Item;
import katana.sir.types.Program;

/**
 * The literal lowering (phase 3.3): a program's Semantic IR to one Java class against `rt/` alone. One
 * `Record` per 01 and 77, one `Field` per item at its offset with its attributes (R-019), one method per
 * stretch of statements between dispatcher labels (R-021), one provenance comment per statement
 * (R-024). It decides nothing: what it cannot lower it says, as a finding, and leaves a line that fails.
 */
public final class Lower {
    private Lower() {}

    /** The Java source of a class `className`, and what could not be lowered. */
    public record Result(String java, List<String> findings, int statements) {}

    /** Items are set up in methods of this many, so no method passes the JVM's size limit. */
    static final int CHUNK = 150;

    /**
     * A program as one Java class through the one emitter (R-024): its records and fields, the constructor
     * that builds and initializes them, main, its procedure, and each program nested in it as a class
     * inside it (LANE-19).
     */
    // Decision: R-024
    public static Result java(Program p, String className) {
        Emitter e = new Emitter();
        List<String> findings = new ArrayList<>();
        e.line("// Lowered by Katana from " + p.header().source() + " (" + p.header().profileHash() + "). Literal: not for editing.");
        e.line("import katana.rt.*;");
        e.line("import katana.rt.Record;");
        e.line("import java.util.List;");
        program(e, p, className, List.of(), findings);
        return new Result(e.text(), findings, e.statements());
    }

    /**
     * One program's class: a top-level one (`containers` empty) public, with `main`; a nested one a static
     * class inside its container's, which it reaches as `up` (LANE-19).
     */
    // Decision: R-024
    private static void program(Emitter e, Program p, String className, List<Program> containers, List<String> findings) {
        Exprs x = new Exprs(p, findings);
        boolean top = containers.isEmpty();
        e.open((top ? "public final class " : "static final class ") + className + " implements Subprogram");
        e.line("private final RunUnit u;");
        if (!top) e.line("private final " + Lower.className(containers.get(containers.size() - 1)) + " up;");
        e.line("private boolean called;");
        e.line("private final Codepage cp;");
        e.line("private final Sysin in;");
        e.line("private final Upsi upsi;");
        e.line("private final Sysout out, err;");
        e.line("private final Field rc;");
        if (Debugging.held(p)) e.line("private final Debug dbg;");
        e.line("private final Dispatcher d = new Dispatcher(" + (Flow.segmented(p) ? Flow.segments(p) : String.valueOf(Math.max(1, p.procedure().paragraphs().size()))) + ");");
        declareItems(e, p);
        FileVerbs.declare(e, p);
        if (top) {
            e.open("public " + className + "(Codepage cp, Upsi upsi, Sysin in, Sysout out, Sysout err)");
            e.line("this(new RunUnit(cp, upsi, in, out, err, false));");
            e.close();
        }
        e.open("public " + className + "(RunUnit u" + (top ? "" : ", " + Lower.className(containers.get(containers.size() - 1)) + " up") + ")");
        e.line("this.u = u;");
        if (!top) e.line("this.up = up;");
        e.line("this.cp = u.cp;");
        if (Debugging.held(p)) e.line("this.dbg = new Debug(cp, true);");
        e.line("this.co = " + Collating.of(x) + ";");
        e.line("this.upsi = u.upsi;");
        e.line("this.in = u.in;");
        e.line("this.out = u.out;");
        e.line("this.err = u.err;");
        // RETURN-CODE, the run unit's (LR 6.4 p. 24).
        e.line("this.rc = u.rc;");
        FileVerbs.externals(e, p);
        for (int c = 0; c * CHUNK < p.items().size(); c++) e.line("items" + c + "();");
        Programs.externals(e, p);
        for (int c = 0; c * CHUNK < p.items().size(); c++) e.line("values" + c + "();");
        FileVerbs.linages(e, p, x);
        FileVerbs.varyings(e, p, x);
        FileVerbs.keys(e, p, x);
        e.close();
        defineItems(e, p, x);
        if (top) main(e, className, !p.procedure().using().isEmpty());
        if (top) parm(e, p);
        FileVerbs.closer(e, p);
        Programs.entry(e, p, x, containers);
        new Flow(e, p, x, containers).procedure();
        List<Program> inside = new ArrayList<>(containers);
        inside.add(p);
        for (Program n : p.nested()) program(e, n, Lower.className(n), inside, findings);
        e.close();
    }

    /**
     * A launcher: the lane's codepage, DISPLAY sign form and DYNAM from system properties (R-019; the
     * profile's, J-109, J-186), and LE's UPSI option; at the end, normal (STOP RUN in any program, or the main
     * program's last statement) or an abend (LANE-08), every program's files are closed. A normal
     * end returns RETURN-CODE "modulo 4096" to the system (LR 6.4 pp. 24, 457), 0 when it was never set.
     */
    private static void main(Emitter e, String className, boolean using) {
        e.open("public static void main(String[] args)");
        e.line("Codepage cp = Codepage.valueOf(System.getProperty(\"katana.codepage\", \"COBC_ASCII\"));");
        e.line("Display.Sign sign = Display.Sign.valueOf(System.getProperty(\"katana.dispsign\", \"SEP\"));");
        e.line("RunUnit u = new RunUnit(cp, Upsi.of(System.getProperty(\"katana.upsi\", \"00000000\")), new Sysin(System.in), new Sysout(System.out, sign), new Sysout(System.err, sign), \"DYNAM\".equals(System.getProperty(\"katana.dynam\", \"NODYNAM\")));");
        e.line(className + " program = new " + className + "(u);");
        if (using) e.line("program.parm(args.length > 0 ? args[0] : \"\");");
        e.open("try");
        e.line("program.run();");
        e.close();
        e.line("catch (StopRun s) { }");
        e.open("catch (Abend a)");
        e.line("program.end();");
        e.line("u.close();");
        e.line("System.out.flush();");
        e.line("System.err.println(a.line());");
        e.line("System.err.println(a.getMessage());");
        e.line("System.exit(a.code());");
        e.close();
        e.line("program.end();");
        e.line("u.close();");
        e.line("System.exit(Math.floorMod(u.rc.get().unscaled().intValue(), 4096));");
        e.close();
    }

    /**
     * A main program's PARM, as z/OS passes it to PROCEDURE DIVISION USING (LANE-03, the cobc lane's driver):
     * into the first USING item's storage, a halfword binary length, then the text, spaces to 100 bytes.
     */
    // Decision: R-019
    private static void parm(Emitter e, Program p) {
        if (p.procedure().using().isEmpty()) return;
        Item first = p.items().get(p.procedure().using().get(0) - 1);
        e.open("void parm(String text)");
        e.line("new Field(r" + first.record() + ", " + first.offset() + ", Attr.numeric(Attr.Usage.BINARY, 4, 0, true)).put(new Decimal(java.math.BigInteger.valueOf(text.length()), 0));");
        e.line("byte[] area = r" + first.record() + ".bytes(), bytes = text.getBytes(cp.charset);");
        e.line("for (int k = 0; k < 100 && " + first.offset() + " + 2 + k < area.length; k++) area[" + first.offset() + " + 2 + k] = k < bytes.length ? bytes[k] : cp.character(' ');");
        e.close();
    }

    /** A Java class name for a program: its PROGRAM-ID, each character Java does not allow made `_`. */
    public static String className(Program p) {
        return className(p.header().program());
    }

    /** A Java class name for a program-name, as `rt/RunUnit.className` makes it at a dynamic CALL. */
    static String className(String program) {
        String name = program.replaceAll("[^A-Za-z0-9_]", "_");
        return Character.isJavaIdentifierStart(name.charAt(0)) ? name : "P" + name;
    }

    private static void declareItems(Emitter e, Program p) {
        for (Item i : p.items()) {
            if (i.record() == i.id()) e.line("private Record r" + i.id() + ";");
            e.line("private Field f" + i.id() + "; // " + i.name());
        }
    }

    /** Each chunk of items: its records and fields, then the VALUEs (and, for an item without one, the initial value). */
    private static void defineItems(Emitter e, Program p, Exprs x) {
        List<Item> items = p.items();
        for (int c = 0; c * CHUNK < items.size(); c++) {
            List<Item> chunk = items.subList(c * CHUNK, Math.min(items.size(), (c + 1) * CHUNK));
            e.open("private void items" + c + "()");
            for (Item i : chunk) {
                if (i.record() == i.id()) e.line("r" + i.id() + " = " + area(p, i) + ";");
                e.line("f" + i.id() + " = new Field(r" + i.record() + ", " + i.offset() + ", " + attr(p, i, x) + ");");
            }
            e.close();
            e.open("private void values" + c + "()");
            for (Item i : chunk) {
                Item record = p.items().get(i.record() - 1);
                // A GLOBAL record is its container's, set there; an EXTERNAL one by the first program that describes it (LANE-19).
                if (Programs.global(p, record)) continue;
                if (Programs.external(p, record)) e.open("if (x" + record.id() + ")");
                Values.initial(e, p, i, x);
                if (Programs.external(p, record)) e.close();
            }
            e.close();
        }
    }

    /**
     * A record's storage: its own; for a second 01 under an FD, the first's (they share the record area); for
     * the records of files that share a record area (SIR-09, SAME RECORD AREA), the one of their first records
     * declared first, as long as the longest of them; for an 01 that REDEFINES another, that one's (LR 6.4 p. 226:
     * "redefinitions of the same storage area").
     */
    // Decision: R-019
    private static String area(Program p, Item record) {
        for (Program.Global g : p.globals()) if (g.record() == record.id()) return "up" + ".up".repeat(g.depth() - 1) + ".r" + g.outer();
        if (record.flags().contains(Item.Flag.EXTERNAL)) return "u.external(" + Emitter.quote(record.name()) + ", " + recordLength(p, record) + ")";
        if (record.redefines() != 0) return "r" + p.items().get(record.redefines() - 1).record();
        for (var d : p.files()) {
            if (!d.records().contains(record.id())) continue;
            if (d.fd() != null && d.fd().external()) return "u.external(" + Emitter.quote("FD " + d.name()) + ", " + d.records().stream().mapToInt(r -> recordLength(p, p.items().get(r - 1))).max().orElse(record.length()) + ")";
            List<katana.sir.types.FileDescriptor> group = p.files().stream().filter(o -> o == d || d.sharesRecordArea().contains(o.name())).filter(o -> !o.records().isEmpty()).toList();
            // The group's record declared first owns the area, so it exists before the others refer to it.
            int first = group.stream().mapToInt(o -> o.records().get(0)).min().orElseThrow();
            if (first != record.id()) return "r" + first;
            return "Record.of(" + group.stream().flatMap(o -> o.records().stream()).mapToInt(r -> recordLength(p, p.items().get(r - 1))).max().orElse(record.length()) + ", cp)";
        }
        return "Record.of(" + recordLength(p, record) + ", cp)";
    }

    /** A record's length: its own, or the longest item that REDEFINES it, an 01 that redefines it included. */
    private static int recordLength(Program p, Item record) {
        int length = record.length();
        for (Item i : p.items()) {
            if (i.record() == record.id()) length = Math.max(length, i.offset() + i.length());
            else if (i.redefines() == record.id() && i.record() == i.id()) length = Math.max(length, recordLength(p, i));
        }
        return length;
    }

    /** An item's `rt` attributes: a group as alphanumeric bytes; an item `rt/` does not hold, as bytes with a finding. */
    static String attr(Program p, Item i, Exprs x) {
        Attributes a = i.attributes();
        boolean justified = i.flags().contains(Item.Flag.JUSTIFIED), blank = i.flags().contains(Item.Flag.BLANK_WHEN_ZERO);
        String usage = switch (a.usage()) {
            case DISPLAY -> "DISPLAY";
            case BINARY -> "BINARY";
            case NATIVE_BINARY -> "NATIVE_BINARY";
            case PACKED -> "PACKED";
            default -> null;
        };
        return switch (a.category()) {
            case NUMERIC -> usage == null ? x.unsupported(i, "usage " + a.usage()) : "new Attr(Attr.Usage." + usage + ", Attr.Category.NUMERIC, " + stored(a) + ", " + a.scale() + ", " + a.signed() + ", " + a.signSeparate() + ", " + a.leading() + ", false, null)";
            case NUMERIC_EDITED -> "new Attr(Attr.Usage.DISPLAY, Attr.Category.NUMERIC_EDITED, " + a.digits() + ", " + a.scale() + ", false, false, false, false, " + edit(p, a, blank) + ")";
            case ALPHANUMERIC_EDITED -> "new Attr(Attr.Usage.DISPLAY, Attr.Category.ALPHANUMERIC_EDITED, 0, 0, false, false, false, false, " + edit(p, a, blank) + ")";
            case GROUP, ALPHANUMERIC, ALPHABETIC -> "Attr.text(" + i.length() + ", " + justified + ")";
            case INDEX -> index(i, x);
            // A POINTER is its four bytes, the address big-endian (research §25); rt/Storage reads and sets it.
            case POINTER -> "Attr.text(4, false)";
            default -> x.unsupported(i, "category " + a.category());
        };
    }

    /** An index item, or an index-name: a fullword holding the occurrence number. */
    // Unsure: IBM keeps an index as a displacement (LR p. 236); its occurrence number is what every use of it reads, so the lowering holds that (J-114). Stays: only a program that reads an index's own bytes can tell; an IBM run of one settles it.
    private static String index(Item i, Exprs x) {
        return i.length() == 4 ? "Attr.numeric(Attr.Usage.NATIVE_BINARY, 9, 0, true)" : x.unsupported(i, "an index of " + i.length() + " bytes");
    }

    /** FUNCTION WHEN-COMPILED's value: the moment of lowering, the program's compilation, as CURRENT-DATE writes one (21 characters). */
    static String compiled() {
        java.time.ZonedDateTime t = java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC);
        return String.format("%04d%02d%02d%02d%02d%02d%02d+0000", t.getYear(), t.getMonthValue(), t.getDayOfMonth(), t.getHour(), t.getMinute(), t.getSecond(), t.getNano() / 10_000_000);
    }

    /** The digits an item stores: the IR counts each P as a digit (sir/build/Pictures), `rt/` only those it holds. */
    static int stored(Attributes a) {
        return a.digits() - (int) a.picture().chars().filter(c -> c == 'P').count();
    }

    private static String edit(Program p, Attributes a, boolean blank) {
        return "new Edit(" + Emitter.quote(a.mask()) + ", " + Emitter.quote(p.header().currency()) + ".charAt(0), " + p.header().decimalComma() + ", " + blank + ")";
    }
}
