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

    /** The Java source of a class `className`, what could not be lowered, and the manifest of the symbols it declares (research §31). */
    public record Result(String java, List<String> findings, int statements, List<Symbol> symbols) {}

    /**
     * One symbol the Java declares that stands on a COBOL node: a class on `program NAME`, a field `f<id>` or
     * record `r<id>` on `item <id>`, a stretch method `s<label>` on `paragraph <index>`. The machinery a class
     * holds besides (its constructor, `run`, `step`, `main`) stands on the class.
     */
    public record Symbol(String name, String node) {}

    /**
     * What a pass asks the lowering to write otherwise (MOD-04: passes/ writes Java only through this emitter):
     * each PERFORM range, by its first paragraph's index to its last's, called as a method `perform<first>`, and the
     * paragraphs whose last statement, a GO TO to the next, is left to fall, and the PERFORM VARYINGs written as
     * a `for` (research §36), and the methods, by their first paragraph, whose paragraphs are the method's own
     * code, out of the switch (6.2, research §37); the main line's paragraphs when they are `run`'s own code, and the
     * loops of either, each header paragraph to the last of its loop (research §38); the items promoted to Java
     * variables (6.3, research §39, R-068); the arithmetic statements written as exact arithmetic, by where each
     * starts (6.4, research §41); the items whose condition names an Enum or Flag fact names, and the promoted
     * items a Money fact makes `Money` (6.5, research §42, R-070); whether the program is stateless, lowered as
     * INITIAL (6.6, research §44); the exact statements whose value always fits their receiver, stored by plain
     * assignment (6.6b, research §45); the records written as classes over their bytes (6.7, research §46). Empty, the literal lowering; it decides nothing (passes/Structuring and
     * passes/Scalars discharge the obligations).
     */
    public record Plan(java.util.Map<Integer, Integer> methods, java.util.Set<Integer> falls, java.util.Set<String> loops, java.util.Set<Integer> inline,
                       java.util.Set<Integer> main, java.util.Map<Integer, Integer> cycles, java.util.Set<Integer> scalars, java.util.Set<String> exact,
                       java.util.Set<Integer> enums, java.util.Set<Integer> money, boolean stateless, java.util.Set<String> fits,
                       java.util.Set<Integer> records) {
        public static final Plan LITERAL = new Plan(java.util.Map.of(), java.util.Set.of(), java.util.Set.of(), java.util.Set.of(), java.util.Set.of(), java.util.Map.of(), java.util.Set.of(), java.util.Set.of(),
                java.util.Set.of(), java.util.Set.of(), false, java.util.Set.of(), java.util.Set.of());

        public Plan {
            methods = java.util.Map.copyOf(methods);
            falls = java.util.Set.copyOf(falls);
            loops = java.util.Set.copyOf(loops);
            inline = java.util.Set.copyOf(inline);
            main = java.util.Set.copyOf(main);
            cycles = java.util.Map.copyOf(cycles);
            scalars = java.util.Set.copyOf(scalars);
            exact = java.util.Set.copyOf(exact);
            enums = java.util.Set.copyOf(enums);
            money = java.util.Set.copyOf(money);
            fits = java.util.Set.copyOf(fits);
            records = java.util.Set.copyOf(records);
        }

        /** Whether the plan changes the procedure's flow (6.1, 6.2), apart from the items it promotes: a promotion alone leaves the dispatcher as it is. */
        public boolean structured() {
            return !methods.isEmpty() || !falls.isEmpty() || !loops.isEmpty() || !inline.isEmpty() || !main.isEmpty() || !cycles.isEmpty();
        }

        /** A PERFORM VARYING the plan writes as a `for`, by where its statement starts: `file:line:column`. */
        public static String loop(katana.sir.types.Origin o) {
            return o.file() + ":" + o.line() + ":" + o.column();
        }
    }

    /** A PERFORM VARYING's phrases as the lowering reads them (research §36): each identifier and its BY (null for none). */
    public record Varying(List<katana.sir.types.Value.Field> ids, List<katana.sir.types.Value> bys) {}

    /** The VARYING phrases of a PERFORM statement, VARYING then each AFTER; none for any other. */
    public static Varying varying(katana.sir.types.Statement s) {
        return Performs.phrases(s);
    }

    /** Items are set up in methods of this many, so no method passes the JVM's size limit. */
    static final int CHUNK = 150;

    /**
     * A program as one Java class through the one emitter (R-024): its records and fields, the constructor
     * that builds and initializes them, main, its procedure, and each program nested in it as a class
     * inside it (LANE-19).
     */
    // Decision: R-024
    public static Result java(Program p, String className) {
        return java(p, className, Plan.LITERAL);
    }

    /** The same under a pass's plan (6.1): the program's own procedure only, a nested program's literal. */
    // Decision: R-024
    public static Result java(Program p, String className, Plan plan) {
        Emitter e = new Emitter();
        List<String> findings = new ArrayList<>();
        e.line("// Lowered by Katana from " + p.header().source() + " (" + p.header().profileHash() + "). " + (plan.equals(Plan.LITERAL) ? "Literal" : "Structured (passes/, phase 6)") + ": not for editing.");
        e.line("import katana.rt.*;");
        e.line("import katana.rt.Record;");
        e.line("import java.util.List;");
        program(e, p, className, List.of(), findings, plan);
        return new Result(e.text(), findings, e.statements(), e.symbols());
    }

    /**
     * One program's class: a top-level one (`containers` empty) public, with `main`; a nested one a static
     * class inside its container's, which it reaches as `up` (LANE-19). The order is load-bearing (J-235): the names
     * first (promoted items, then record classes, which take theirs after), then the members and the constructor,
     * the items' definitions, the entry, the procedure, and only then the predicates and the guard, which the
     * procedure's lowering asks for (`x.guarded`), then the nested programs.
     */
    // Decision: R-024
    private static void program(Emitter e, Program p, String className, List<Program> containers, List<String> findings, Plan plan) {
        Exprs x = new Exprs(p, findings);
        x.promoted = new Promoted(p, plan.scalars(), plan.money(), plan.enums());
        java.util.Set<String> types = new java.util.HashSet<>(java.util.Set.of(className));
        for (Program n : p.nested()) types.add(Lower.className(n));
        x.promoted.views = new Views(p, plan.records(), x, types);
        boolean top = containers.isEmpty();
        e.open((top ? "public final class " : "static final class ") + className + " implements Subprogram");
        e.enter(className);
        e.symbol("", "program " + p.header().program());
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
        declareItems(e, p, x);
        FileDecls.declare(e, p);
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
        FileDecls.externals(e, p);
        for (int c = 0; c * CHUNK < p.items().size(); c++) e.line("items" + c + "();");
        Programs.externals(e, p);
        for (int c = 0; c * CHUNK < p.items().size(); c++) e.line("values" + c + "();");
        FileDecls.linages(e, p, x);
        FileDecls.varyings(e, p, x);
        FileDecls.keys(e, p, x);
        e.close();
        defineItems(e, p, x);
        if (top) main(e, className, !p.procedure().using().isEmpty());
        if (top) parm(e, p);
        FileDecls.closer(e, p);
        Programs.entry(e, p, x, containers, plan.stateless());
        Flow flow = new Flow(e, p, x, containers, plan);
        flow.procedure();
        x.promoted.predicates(e, flow.conditions);
        if (x.guarded) guard(e, p);
        List<Program> inside = new ArrayList<>(containers);
        inside.add(p);
        for (Program n : p.nested()) program(e, n, Lower.className(n), inside, findings, Plan.LITERAL);
        e.leave();
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

    /** 6.4b's guard (R-069): every operand a valid number within its PICTURE, so the exact form runs; else the step-by-step one. */
    // Decision: R-069
    private static void guard(Emitter e, Program p) {
        e.open("private static boolean valid(Field... operands)");
        e.line("for (Field f : operands) if (!Classes.exact(f)) return false;");
        e.line("return true;");
        e.close();
        e.symbol("valid", "guard " + p.header().program());
    }

    /** Each item's members: its record if it is one, and its Field, or for a promoted item its variable (6.3). */
    private static void declareItems(Emitter e, Program p, Exprs x) {
        for (Item i : p.items()) {
            if (i.record() == i.id()) {
                e.line("private Record r" + i.id() + ";");
                e.symbol("r" + i.id(), "item " + i.id());
                if (x.promoted.views.record(i.id())) x.promoted.views.declare(e, i);
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
            if (x.promoted.views.path(i.id()) != null) continue;
            e.line("private Field f" + i.id() + "; // " + i.name());
            e.symbol("f" + i.id(), "item " + i.id());
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
                if (i.record() == i.id() && x.promoted.views.record(i.id())) e.line(x.promoted.views.create(i.id()));
                if (!x.promoted.has(i.id()) && x.promoted.views.path(i.id()) == null) e.line("f" + i.id() + " = new Field(r" + i.record() + ", " + i.offset() + ", " + attr(p, i, x) + ");");
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

    /**
     * The verbs that write a promoted item's bytes through a field of the item's own (research §40): `Verbs` runs
     * them through `Promoted.through`, and passes/Scalars keeps out an item one of them writes with statements of
     * its own, since `through` holds no statement inside another. One set, read by both.
     */
    public static final java.util.Set<String> THROUGH = java.util.Set.of("INITIALIZE", "SET", "STRING", "UNSTRING", "ACCEPT");

    /** The digits an item stores: the IR counts each P as a digit (sir/build/Pictures), `rt/` only those it holds. */
    public static int stored(Attributes a) {
        return a.digits() - (int) a.picture().chars().filter(c -> c == 'P').count();
    }

    private static String edit(Program p, Attributes a, boolean blank) {
        return "new Edit(" + Emitter.quote(a.mask()) + ", " + Emitter.quote(p.header().currency()) + ".charAt(0), " + p.header().decimalComma() + ", " + blank + ")";
    }
}
