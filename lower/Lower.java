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

    // Decision: R-024
    public static Result java(Program p, String className) {
        Emitter e = new Emitter();
        List<String> findings = new ArrayList<>();
        Exprs x = new Exprs(p, findings);
        e.line("// Lowered by Katana from " + p.header().source() + " (" + p.header().profileHash() + "). Literal: not for editing.");
        e.line("import katana.rt.*;");
        e.line("import katana.rt.Record;");
        e.line("import java.util.List;");
        e.open("public final class " + className);
        e.line("private final Codepage cp;");
        e.line("private final Sysout out, err;");
        e.line("private final Dispatcher d = new Dispatcher(" + Math.max(1, p.procedure().paragraphs().size()) + ");");
        declareItems(e, p);
        FileVerbs.declare(e, p);
        e.open("public " + className + "(Codepage cp, Sysout out, Sysout err)");
        e.line("this.cp = cp;");
        e.line("this.out = out;");
        e.line("this.err = err;");
        for (int c = 0; c * CHUNK < p.items().size(); c++) e.line("items" + c + "();");
        for (int c = 0; c * CHUNK < p.items().size(); c++) e.line("values" + c + "();");
        e.close();
        defineItems(e, p, x);
        main(e, className);
        new Flow(e, p, x).procedure();
        e.close();
        return new Result(e.text(), findings, e.statements());
    }

    /** A launcher: the lane's codepage and DISPLAY sign form from system properties (R-019; the profile's, J-109). */
    private static void main(Emitter e, String className) {
        e.open("public static void main(String[] args)");
        e.line("Codepage cp = Codepage.valueOf(System.getProperty(\"katana.codepage\", \"COBC_ASCII\"));");
        e.line("Display.Sign sign = Display.Sign.valueOf(System.getProperty(\"katana.dispsign\", \"SEP\"));");
        e.line("new " + className + "(cp, new Sysout(System.out, sign), new Sysout(System.err, sign)).run();");
        e.close();
    }

    /** A Java class name for a program: its PROGRAM-ID, each character Java does not allow made `_`. */
    public static String className(Program p) {
        String name = p.header().program().replaceAll("[^A-Za-z0-9_]", "_");
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
            for (Item i : chunk) Values.initial(e, p, i, x);
            e.close();
        }
    }

    /** A record's storage: its own, or, for a second 01 under an FD, the first's (they share the record area). */
    private static String area(Program p, Item record) {
        for (var d : p.files()) {
            if (d.records().contains(record.id()) && d.records().get(0) != record.id()) return "r" + d.records().get(0);
            if (d.records().contains(record.id())) return "Record.of(" + d.records().stream().mapToInt(r -> recordLength(p, p.items().get(r - 1))).max().orElse(record.length()) + ", cp)";
        }
        return "Record.of(" + recordLength(p, record) + ", cp)";
    }

    /** A record's length: its own, or the longest item that REDEFINES it. */
    private static int recordLength(Program p, Item record) {
        int length = record.length();
        for (Item i : p.items()) if (i.record() == record.id()) length = Math.max(length, i.offset() + i.length());
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
            default -> x.unsupported(i, "category " + a.category());
        };
    }

    /** An index item, or an index-name: a fullword holding the occurrence number. */
    // Unsure: IBM keeps an index as a displacement (LR p. 236); its occurrence number is what every use of it reads, so the lowering holds that (J-114).
    private static String index(Item i, Exprs x) {
        return i.length() == 4 ? "Attr.numeric(Attr.Usage.NATIVE_BINARY, 9, 0, true)" : x.unsupported(i, "an index of " + i.length() + " bytes");
    }

    /** The digits an item stores: the IR counts each P as a digit (sir/build/Pictures), `rt/` only those it holds. */
    static int stored(Attributes a) {
        return a.digits() - (int) a.picture().chars().filter(c -> c == 'P').count();
    }

    private static String edit(Program p, Attributes a, boolean blank) {
        return "new Edit(" + Emitter.quote(a.mask()) + ", " + Emitter.quote(p.header().currency()) + ".charAt(0), " + p.header().decimalComma() + ", " + blank + ")";
    }
}
