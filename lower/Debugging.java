package katana.lower;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import katana.sir.types.Procedure;
import katana.sir.types.Program;
import katana.sir.types.Statement;

/**
 * USE FOR DEBUGGING on procedures (research §17): which procedures a debugging section watches, and the
 * code that tells `rt/Debug` what caused each entry (Table 2, LR 6.4 p. 20) and runs the section "Before
 * each execution of the named procedure" and "After the execution of an ALTER statement referring to
 * the named procedure" (Table 74, p. 708). Only in a program WITH DEBUGGING MODE that has one; elsewhere
 * every method gives back the code it was given.
 */
final class Debugging {
    private final Program p;
    private final boolean on;
    /** Each watched procedure-name (a paragraph or a section), with its debugging section's first and last paragraph. */
    private final Map<String, String> watched = new LinkedHashMap<>();
    private String all;

    /** The procedures each debugging section watches; a data, file or cd name refused, as IBM has none (p. 708). */
    Debugging(Program p, Exprs x) {
        this.p = p;
        for (Procedure.Section s : p.procedure().sections()) {
            if (s.use() == null || s.use().kind() != Procedure.Use.Kind.DEBUGGING) continue;
            String range = range(s.name());
            // A debugging section with no procedures runs nothing.
            if (range == null) continue;
            for (String on : s.use().debugging()) {
                String name = on.split(" (OF|IN) ")[0].trim();
                if (name.replace(" ", "").equals("ALLPROCEDURES")) all = range;
                else if (procedure(name)) watched.put(name, range);
                else x.unsupported("USE FOR DEBUGGING ON " + on + ": IBM debugs procedures only (LR 6.4 p. 708)");
            }
        }
        this.on = p.header().debuggingMode() && (all != null || !watched.isEmpty());
    }

    boolean on() {
        return on;
    }

    /** Whether a program holds DEBUG-ITEM: WITH DEBUGGING MODE and a debugging section (LR 6.4 p. 760). */
    static boolean held(Program p) {
        return p.header().debuggingMode() && p.procedure().sections().stream().anyMatch(s -> s.use() != null && s.use().kind() == Procedure.Use.Kind.DEBUGGING);
    }

    /** The run's start: "START PROGRAM" at the first nondeclarative procedure's line. */
    void start(Emitter e, int first) {
        if (on && first < p.procedure().paragraphs().size()) e.line("dbg.start(" + p.procedure().paragraphs().get(first).origin().line() + ");");
    }

    /** An out-of-line PERFORM's call, its entry a "PERFORM LOOP" from the statement at `line`; `section`, whether it named one. */
    String perform(String call, int line, boolean section) {
        return on ? "dbg.perform(" + call + ", " + line + ", " + section + ")" : call;
    }

    /** A GO TO at `line`, to a procedure that is a section or not. */
    void goTo(Emitter e, int line, boolean section) {
        if (on) e.line("dbg.goTo(" + line + ", " + section + ");");
    }

    /** A paragraph's end: falling into the next is "FALL THROUGH", from its last statement, or from the next section's header when it crosses one (Table 2's note 1). */
    String end(int paragraph, String call) {
        if (!on || paragraph + 1 >= p.procedure().paragraphs().size()) return call;
        Procedure.Paragraph here = p.procedure().paragraphs().get(paragraph), next = p.procedure().paragraphs().get(paragraph + 1);
        int line = here.statements().isEmpty() ? here.origin().line() : here.statements().get(here.statements().size() - 1).origin().line();
        if (!next.section().equals(here.section())) line = header(next.section(), line);
        return "dbg.fell(" + call + ", " + (paragraph + 1) + ", " + line + ")";
    }

    /**
     * The start of a nondeclarative paragraph: its section's debugging section when the entry executes
     * the section (`Debug.section`), then the paragraph's own (or ALL PROCEDURES', each "nondebugging
     * procedure", p. 708), each with DEBUG-ITEM filled for it.
     */
    // Decision: R-021
    void entry(Emitter e, Procedure.Paragraph g) {
        if (!on || declarative(g.section())) return;
        e.line("dbg.entered();");
        List<Procedure.Paragraph> section = p.procedure().paragraphs().stream().filter(o -> o.section().equals(g.section())).toList();
        String sectionRange = watched.getOrDefault(g.section(), all);
        if (!g.section().isEmpty() && section.get(0) == g && sectionRange != null) {
            e.line("if (dbg.on() && dbg.section()) { dbg.fill(" + Emitter.quote(g.section()) + "); use(" + sectionRange + "); }");
        }
        String own = watched.getOrDefault(g.name(), g.name().isEmpty() ? null : all);
        if (own != null) e.line("if (dbg.on()) { dbg.fill(" + Emitter.quote(g.name()) + "); use(" + own + "); }");
    }

    /** After ALTER `from` TO PROCEED TO `to` at `line`: the debugging section watching `from` (ALL PROCEDURES' too, outside the declaratives). */
    void alter(Emitter e, int from, int to, int line, String paragraphOfAlter) {
        if (!on) return;
        Procedure.Paragraph altered = p.procedure().paragraphs().get(from);
        String range = watched.getOrDefault(altered.name(), inDeclaratives(paragraphOfAlter) ? null : all);
        if (range == null) return;
        String target = p.procedure().paragraphs().get(to).name();
        e.line("if (dbg.on()) { dbg.altered(" + line + ", " + Emitter.quote(altered.name()) + ", " + Emitter.quote(target) + "); use(" + range + "); }");
    }

    /** DEBUG-ITEM's fields as sending items, by their names (LR 6.4 p. 19); DEBUG-SUB-n are not held. */
    static String register(String name) {
        return switch (name) {
            case "DEBUG-ITEM" -> "dbg.all";
            case "DEBUG-LINE" -> "dbg.line";
            case "DEBUG-NAME" -> "dbg.name";
            case "DEBUG-CONTENTS" -> "dbg.contents";
            default -> null;
        };
    }

    private boolean procedure(String name) {
        return p.procedure().paragraphs().stream().anyMatch(g -> g.name().equals(name) || g.section().equals(name));
    }

    private boolean declarative(String section) {
        return p.procedure().sections().stream().anyMatch(s -> s.name().equals(section) && s.declarative());
    }

    private boolean inDeclaratives(String paragraph) {
        return p.procedure().paragraphs().stream().anyMatch(g -> g.name().equals(paragraph) && declarative(g.section()));
    }

    private int header(String section, int otherwise) {
        return p.procedure().sections().stream().filter(s -> s.name().equals(section)).map(s -> s.origin().line()).findFirst().orElse(otherwise);
    }

    private String range(String section) {
        List<Integer> ps = new ArrayList<>();
        for (Procedure.Paragraph g : p.procedure().paragraphs()) if (g.section().equals(section)) ps.add(g.index());
        return ps.isEmpty() ? null : ps.get(0) + ", " + ps.get(ps.size() - 1);
    }

    /** The line of a statement, for DEBUG-LINE. */
    static int line(Statement s) {
        return s.origin().line();
    }
}
