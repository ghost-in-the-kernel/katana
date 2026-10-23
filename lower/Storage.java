package katana.lower;

import java.util.List;
import katana.sir.types.Attributes;
import katana.sir.types.Item;
import katana.sir.types.Program;

/**
 * A program's storage as the literal lowering writes it (J-241, out of `Lower`): each item's member, its `Record`
 * and `Field` (or a promoted item's variable, or a record class's member), their definitions in chunks of
 * `Lower.CHUNK`, each record's area, and each item's attributes and digits. `Lower.program` calls it; it decides
 * nothing (MOD-03).
 */
public final class Storage {
    private Storage() {}

    /** Each item's members: its record if it is one, and its Field, or for a promoted item its variable (6.3). */
    static void declareItems(Emitter e, Program p, Exprs x) {
        for (Item i : p.items()) {
            if (i.record() == i.id()) {
                e.line("private Record r" + i.id() + ";");
                e.symbol("r" + i.id(), "item " + i.id());
                if (x.promoted.views().record(i.id())) x.promoted.views().declare(e, i);
            }
            // A promoted item (6.3): a Java variable for its value, no Field over its bytes (R-068).
            if (x.promoted.has(i.id())) {
                e.line(x.promoted.declaration(i.id(), x));
                e.symbol(x.promoted.name(i.id()), "item " + i.id());
                for (String[] c : x.promoted.constants(i.id())) {
                    e.line("private static final long " + c[0] + " = " + c[1] + "; // 88 " + c[2]);
                    e.symbol(c[0], "condition " + i.id());
                }
                continue;
            }
            // A member of a record class (6.7) is the class's, not the program's.
            if (x.promoted.views().path(i.id()) != null) continue;
            e.line("private Field f" + i.id() + "; // " + i.name());
            e.symbol("f" + i.id(), "item " + i.id());
        }
    }
    /** Each chunk of items: its records and fields, then the VALUEs (and, for an item without one, the initial value). */
    static void defineItems(Emitter e, Program p, Exprs x) {
        List<Item> items = p.items();
        for (int c = 0; c * Lower.CHUNK < items.size(); c++) {
            List<Item> chunk = items.subList(c * Lower.CHUNK, Math.min(items.size(), (c + 1) * Lower.CHUNK));
            e.open("private void items" + c + "()");
            for (Item i : chunk) {
                if (i.record() == i.id()) e.line("r" + i.id() + " = " + area(p, i) + ";");
                if (i.record() == i.id() && x.promoted.views().record(i.id())) e.line(x.promoted.views().create(i.id()));
                if (!x.promoted.has(i.id()) && x.promoted.views().path(i.id()) == null) e.line("f" + i.id() + " = new Field(r" + i.record() + ", " + i.offset() + ", " + attr(p, i, x) + ");");
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
    /** The digits an item stores: the IR counts each P as a digit (sir/build/Pictures), `rt/` only those it holds. */
    public static int stored(Attributes a) {
        return a.digits() - (int) a.picture().chars().filter(c -> c == 'P').count();
    }
    private static String edit(Program p, Attributes a, boolean blank) {
        return "new Edit(" + Emitter.quote(a.mask()) + ", " + Emitter.quote(p.header().currency()) + ".charAt(0), " + p.header().decimalComma() + ", " + blank + ")";
    }
}
