package katana.lower;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import katana.sir.types.FileDescriptor;
import katana.sir.types.Item;
import katana.sir.types.Program;

/**
 * A program as other programs reach it (LANE-19, research §24): `call`, which binds its LINKAGE records
 * to the CALL's arguments and runs it; `initial`; `cancel` and `end`, which close its files; and, for
 * each program nested in it, the instance it holds, made at the first CALL and dropped by CANCEL.
 */
final class Programs {
    private Programs() {}

    /** Whether a record is a containing program's GLOBAL one (SIR-12). */
    static boolean global(Program p, Item record) {
        return p.globals().stream().anyMatch(g -> g.record() == record.id());
    }

    /** Whether a record is the run unit's: an EXTERNAL record, or a record of an EXTERNAL file. */
    static boolean external(Program p, Item record) {
        if (global(p, record)) return false;
        if (record.flags().contains(Item.Flag.EXTERNAL)) return true;
        return p.files().stream().anyMatch(d -> d.fd() != null && d.fd().external() && d.records().contains(record.id()));
    }

    /** In the constructor, after the records: whether this program is the first to describe each EXTERNAL record, and so gives it its initial values. */
    static void externals(Emitter e, Program p) {
        for (Item i : p.items()) if (i.record() == i.id() && external(p, i)) e.line("x" + i.id() + " = u.first(" + Emitter.quote(name(p, i)) + ");");
    }

    /** The run unit's name for an EXTERNAL record: its own, or its file's. */
    static String name(Program p, Item record) {
        for (FileDescriptor d : p.files()) if (d.records().contains(record.id())) return "FD " + d.name();
        return record.name();
    }

    /**
     * `call`: each USING record bound to its argument, an omitted one left as it was; run; back with every
     * PERFORM range ended (LR 6.4 p. 343). `initial`, `cancel` (the program's internal files closed, LR 6.4
     * p. 327, and the programs it contains cancelled) and `end` (every file closed, at the run's end).
     */
    // Decision: R-019
    static void entry(Emitter e, Program p, Exprs x, List<Program> containers) {
        for (Item i : p.items()) if (i.record() == i.id() && external(p, i)) e.line("private boolean x" + i.id() + ";");
        List<Integer> using = p.procedure().using();
        e.open("public void call(Field[] using)");
        for (int k = 0; k < using.size(); k++) e.line("if (using.length > " + k + " && using[" + k + "] != null) bind" + using.get(k) + "(using[" + k + "]);");
        e.line("called = true;");
        e.open("try");
        e.line("run();");
        e.close();
        e.open("finally");
        e.line("called = false;");
        e.line("d.exited();");
        e.close();
        e.close();
        for (Item r : p.items()) if (linkage(r)) bind(e, p, x, r.id());
        e.line("public boolean initial() { return " + p.header().initial() + "; }");
        e.open("public void cancel()");
        for (FileDescriptor d : p.files()) if (!SortVerbs.sd(d) && !FileVerbs.external(d)) e.line(FileVerbs.name(d.name()) + ".close();");
        for (Program n : p.nested()) e.line("k_" + Lower.className(n) + "();");
        e.close();
        e.open("public void end()");
        e.line("closeFiles();");
        for (Program n : p.nested()) e.line("if (n_" + Lower.className(n) + " != null) n_" + Lower.className(n) + ".end();");
        e.close();
        for (Program n : p.nested()) nested(e, n);
        byName(e, p, containers);
    }

    /** A LINKAGE 01 or 77 that redefines none: a record a CALL or SET ADDRESS OF binds to storage. */
    static boolean linkage(Item r) {
        return r.record() == r.id() && r.section() == Item.Section.LINKAGE && r.redefines() == 0;
    }

    /**
     * A LINKAGE record bound to storage: it and every item in it, and each 01 that redefines it, now that
     * storage, at its place: a CALL's argument, "the same storage area" (LR 6.4 p. 320), or the address SET
     * ADDRESS OF gives it (p. 443).
     */
    // Decision: R-019
    private static void bind(Emitter e, Program p, Exprs x, int record) {
        List<Item> bound = new ArrayList<>();
        for (Item i : p.items()) {
            Item r = p.items().get(i.record() - 1);
            if (i.record() == record || r.redefines() == record) bound.add(i);
        }
        for (int c = 0; c == 0 || c * Lower.CHUNK < bound.size(); c++) {
            e.open("private void bind" + record + (c == 0 ? "" : "_" + c) + "(Field a)");
            for (Item i : bound.subList(c * Lower.CHUNK, Math.min(bound.size(), (c + 1) * Lower.CHUNK))) e.line("f" + i.id() + " = new Field(a.record(), a.offset() + " + i.offset() + ", " + Lower.attr(p, i, x) + ");");
            if ((c + 1) * Lower.CHUNK < bound.size()) e.line("bind" + record + "_" + (c + 1) + "(a);");
            e.close();
        }
    }

    /** A program nested in this one: its instance, made at its first CALL; its CALL, an INITIAL one dropped after it (LR 6.4 p. 103); its CANCEL. */
    private static void nested(Emitter e, Program n) {
        String c = Lower.className(n);
        e.line("private " + c + " n_" + c + ";");
        e.line("private " + c + " n_" + c + "() { if (n_" + c + " == null) n_" + c + " = new " + c + "(u, this); return n_" + c + "; }");
        e.open("private boolean c_" + c + "(Field[] a)");
        e.line("n_" + c + "().call(a);");
        e.line("if (n_" + c + ".initial()) k_" + c + "();");
        e.line("return true;");
        e.close();
        e.line("private void k_" + c + "() { if (n_" + c + " != null) { n_" + c + ".cancel(); n_" + c + " = null; } }");
    }

    /**
     * The programs a CALL or CANCEL here can name, each with the program that holds it (`up.` for the
     * container): those directly contained in this one, then each COMMON program directly contained in a
     * program that contains this one, but this one and those containing it (LR 6.4 pp. 86-87).
     */
    // Decision: R-062
    static Map<String, String> visible(Program p, List<Program> containers) {
        Map<String, String> out = new LinkedHashMap<>();
        for (Program n : p.nested()) out.putIfAbsent(n.header().program(), "");
        List<Program> chain = new ArrayList<>(containers);
        chain.add(p);
        for (int j = containers.size() - 1; j >= 0; j--) {
            String owner = "up." + "up.".repeat(containers.size() - 1 - j);
            for (Program n : containers.get(j).nested()) {
                if (n.header().common() && !chain.contains(n)) out.putIfAbsent(n.header().program(), owner);
            }
        }
        return out;
    }

    /** A CALL or CANCEL by an identifier: a program it can see by that name, else a separately compiled one, always dynamically (PG 6.4 p. 366). */
    private static void byName(Emitter e, Program p, List<Program> containers) {
        Map<String, String> visible = visible(p, containers);
        e.open("private boolean byName(String n, Field[] a)");
        e.open("return switch (n)");
        for (var v : visible.entrySet()) e.line("case " + Emitter.quote(v.getKey()) + " -> " + v.getValue() + "c_" + Lower.className(v.getKey()) + "(a);");
        e.line("default -> u.call(n, false, a);");
        e.close();
        e.line(";");
        e.close();
        e.open("private void cancelName(String n)");
        e.open("switch (n)");
        for (var v : visible.entrySet()) e.line("case " + Emitter.quote(v.getKey()) + " -> " + v.getValue() + "k_" + Lower.className(v.getKey()) + "();");
        e.line("default -> u.cancel(n);");
        e.close();
        e.close();
    }
}
