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
        program(e, p, className, null, List.of(), findings, plan);
        return new Result(e.text(), findings, e.statements(), e.symbols());
    }

    /**
     * One program's class: a top-level one (`containers` empty) public, with `main`; a nested one a static
     * class inside its container's, which it reaches as `up` (LANE-19). The order is load-bearing (J-235): the names
     * first (promoted items, then record classes, which take theirs after), then the members and the constructor,
     * the items' definitions, the entry, the procedure, and only then the predicates and the guard, which the
     * procedure's lowering asks for (`x.guarded`), then the nested programs. `outer` is the class a nested program is
     * written inside, under the name it was given (not its program-id's: the caller names the top-level class).
     */
    // Decision: R-024
    private static void program(Emitter e, Program p, String className, String outer, List<Program> containers, List<String> findings, Plan plan) {
        java.util.Set<String> types = new java.util.HashSet<>(java.util.Set.of(className));
        for (Program n : p.nested()) types.add(Lower.className(n));
        Exprs x = new Exprs(p, findings, plan, types);
        boolean top = containers.isEmpty();
        e.open((top ? "public final class " : "static final class ") + className + " implements Subprogram");
        e.enter(className);
        e.symbol("", "program " + p.header().program());
        e.line("private final RunUnit u;");
        if (!top) e.line("private final " + outer + " up;");
        e.line("private boolean called;");
        e.line("private final Codepage cp;");
        e.line("private final Sysin in;");
        e.line("private final Upsi upsi;");
        e.line("private final Sysout out, err;");
        e.line("private final Field rc;");
        if (Debugging.held(p)) e.line("private final Debug dbg;");
        e.line("private final Dispatcher d = new Dispatcher(" + (Flow.segmented(p) ? Flow.segments(p) : String.valueOf(Math.max(1, p.procedure().paragraphs().size()))) + ");");
        Storage.declareItems(e, p, x);
        FileDecls.declare(e, p);
        if (top) {
            e.open("public " + className + "(Codepage cp, Upsi upsi, Sysin in, Sysout out, Sysout err)");
            e.line("this(new RunUnit(cp, upsi, in, out, err, false));");
            e.close();
        }
        e.open("public " + className + "(RunUnit u" + (top ? "" : ", " + outer + " up") + ")");
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
        Storage.defineItems(e, p, x);
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
        for (Program n : p.nested()) program(e, n, Lower.className(n), className, inside, findings, Plan.LITERAL);
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







    /** FUNCTION WHEN-COMPILED's value: the moment of lowering, the program's compilation, as CURRENT-DATE writes one (21 characters). */
    static String compiled() {
        java.time.ZonedDateTime t = java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC);
        return String.format("%04d%02d%02d%02d%02d%02d%02d+0000", t.getYear(), t.getMonthValue(), t.getDayOfMonth(), t.getHour(), t.getMinute(), t.getSecond(), t.getNano() / 10_000_000);
    }



}
