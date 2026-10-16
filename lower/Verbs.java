package katana.lower;

import java.util.ArrayList;
import java.util.List;
import katana.sir.types.Procedure;
import katana.sir.types.Statement;
import katana.sir.types.Value;

/** Each verb's Java, by its operands' roles (sir/roles.golden); a verb not yet lowered is a line that fails, and a finding. */
final class Verbs {
    private final Flow f;
    private final ArithVerbs arithmetic;
    private final Performs performs;
    private final TextVerbs text;
    private final FileVerbs files;
    private final SortVerbs sorts;
    private final Evaluate evaluate;
    private final Search search;

    Verbs(Flow f) {
        this.f = f;
        this.arithmetic = new ArithVerbs(f);
        this.performs = new Performs(f);
        this.text = new TextVerbs(f);
        this.files = new FileVerbs(f);
        this.sorts = new SortVerbs(f, files);
        this.evaluate = new Evaluate(f);
        this.search = new Search(f);
    }

    /** Each statement to its verb's lowering; a verb not lowered is a finding, and stops the program where it runs. */
    // Decision: R-021
    void statement(Statement s) {
        switch (s.verb()) {
            case "MOVE" -> move(s);
            case "DISPLAY" -> display(s);
            case "ACCEPT" -> accept(s);
            case "ADD", "SUBTRACT", "MULTIPLY", "DIVIDE", "COMPUTE" -> arithmetic.statement(s);
            case "SET" -> set(s);
            case "INSPECT" -> text.inspect(s);
            case "STRING" -> text.string(s);
            case "UNSTRING" -> text.unstring(s);
            case "INITIALIZE" -> Initialize.statement(f, s);
            case "OPEN", "CLOSE", "WRITE", "REWRITE", "READ", "START", "DELETE", "SORT", "MERGE", "RELEASE", "RETURN" -> io(s);
            default -> control(s);
        }
    }

    /** The input-output verbs: a file's (`FileVerbs`) or a sort file's (`SortVerbs`). */
    private void io(Statement s) {
        switch (s.verb()) {
            case "OPEN" -> files.open(s);
            case "CLOSE" -> files.close(s);
            case "WRITE" -> files.write(s);
            case "REWRITE" -> files.rewrite(s);
            case "READ" -> files.read(s);
            case "START" -> files.start(s);
            case "DELETE" -> files.delete(s);
            case "SORT" -> sorts.statement(s, "sort");
            case "MERGE" -> sorts.statement(s, "merge");
            case "RELEASE" -> sorts.release(s);
            default -> sorts.returnStatement(s);
        }
    }

    /** The verbs that decide where control goes. */
    private void control(Statement s) {
        switch (s.verb()) {
            case "IF" -> ifStatement(s);
            case "EVALUATE" -> evaluate.statement(s);
            case "SEARCH" -> search.statement(s);
            case "PERFORM" -> performs.statement(s);
            case "GO TO" -> goTo(s);
            case "STOP" -> stop(s);
            case "GOBACK" -> f.jump(String.valueOf(f.end()));
            case "EXIT" -> exit(s);
            case "ALTER" -> alter(s);
            case "CONTINUE" -> f.e.line("// CONTINUE");
            case "CALL" -> call(s);
            case "NEXT SENTENCE" -> f.nextSentence();
            default -> f.e.line(f.x.unsupported(s.verb() + " at " + s.origin() + " " + s.operands()) + ";");
        }
    }

    /**
     * CALL: of LE's CEE3ABD (abcode, clean-up), the run ended with an abend of abcode, with no return (LE 3.1
     * p. 111, research §22); every other CALL waits for phase 4.3 and is not lowered.
     */
    // Decision: R-027
    // Unsure: clean-up 0 performs no termination activity, so IBM may leave a QSAM file's last buffer unwritten, where the lane closes every file at an abend (LANE-08). Stays: no CardDemo or NIST run reaches CEE3ABD; a mainframe run with a QSAM file open at CEE3ABD settles it.
    private void call(Statement s) {
        katana.sir.types.Call c = f.p.calls().stream().filter(x -> x.origin().equals(s.origin())).findFirst().orElse(null);
        if (c != null && c.target() instanceof Value.Literal l && l.payload().equals("CEE3ABD") && !c.using().isEmpty()) {
            f.e.line("if (true) throw new Abend((int) " + f.x.index(c.using().get(0).value()) + ", \"CALL CEE3ABD\");");
            return;
        }
        f.e.line(f.x.unsupported(s.verb() + " at " + s.origin() + " " + s.operands()) + ";");
    }

    /** IF: its phrases hold statements only; a phrase with words left over (as NEXT SENTENCE once was, J-122) is not lowered, never dropped. */
    private void ifStatement(Statement s) {
        if (s.branches().stream().anyMatch(b -> b.condition() != null)) f.e.line(f.x.unsupported("IF with words in a phrase at " + s.origin() + " " + s.branches()) + ";");
        else f.branch(f.conditions.of(operand(s, "if-statement")), branch(s, "if-then"), branch(s, "if-else"));
    }

    /** The first operand of a role, null when none. */
    static Value operand(Statement s, String role) {
        for (Statement.Operand o : s.operands()) if (o.role().equals(role) && !(o.value() instanceof Value.Name n && n.kind().equals("keyword"))) return o.value();
        return null;
    }

    /** The operands of the given roles, keywords left out. */
    static List<Value> operands(Statement s, String... roles) {
        List<Value> out = new ArrayList<>();
        for (Statement.Operand o : s.operands()) if (List.of(roles).contains(o.role()) && !(o.value() instanceof Value.Name n && n.kind().equals("keyword"))) out.add(o.value());
        return out;
    }

    static boolean keyword(Statement s, String word) {
        for (Statement.Operand o : s.operands()) if (o.value() instanceof Value.Name n && n.kind().equals("keyword") && n.text().equals(word)) return true;
        return false;
    }

    static List<Statement> branch(Statement s, String label) {
        for (Statement.Branch b : s.branches()) if (b.label().equals(label)) return b.statements();
        return List.of();
    }

    private void move(Statement s) {
        Value from = operand(s, "move-to-sending-area");
        if (from == null) {
            Value.Field group = (Value.Field) operand(s, "move-corresponding-to-sending-area");
            for (Value to : operands(s, "move-corresponding-to-statement")) {
                for (Value.Field[] pair : Corresponding.pairs(f.x, group, (Value.Field) to, false)) f.e.line(f.x.moveInto(pair[0], f.x.field(pair[1]), f.x.item(pair[1].item())) + ";");
            }
            return;
        }
        for (Value to : operands(s, "move-to-statement")) {
            if (Exprs.returnCode(to)) {
                // RETURN-CODE, an elementary binary item (LR 6.4 p. 24).
                String pattern = f.x.pattern(from);
                boolean zero = from instanceof Value.Figurative z && Exprs.singular(z.name()).equals("ZERO") && z.all() == null;
                f.e.line((pattern != null ? "Move.fill(rc, " + pattern + ", " + zero + ")" : "Move.move(" + f.x.sending(from) + ", rc)") + ";");
                continue;
            }
            if (!(to instanceof Value.Field t)) {
                f.e.line(f.x.unsupported("MOVE to " + to) + ";");
                continue;
            }
            f.e.line(f.x.moveInto(from, f.x.receiving(t), f.x.item(t.item())) + ";");
        }
    }

    /** DISPLAY: each operand's bytes, a figurative constant's "single occurrence" (LR 6.4 p. 333), ALL "ABCD" its literal once. */
    // Unsure: a numeric literal is displayed as its sign and digits as written, no point, as cobc -std=ibm shows it (J-114, J-130). Stays: an IBM run of DISPLAY of a numeric literal settles it.
    private void display(Statement s) {
        List<String> parts = new ArrayList<>();
        for (Value v : operands(s, "display-operand")) parts.add(shown(v));
        Value upon = operand(s, "display-upon");
        boolean syserr = upon instanceof Value.Name n && n.text().equals("SYSERR");
        f.e.line((syserr ? "err" : "out") + ".display(" + !keyword(s, "ADVANCING") + (parts.isEmpty() ? "" : ", ") + String.join(", ", parts) + ");");
    }

    /**
     * STOP RUN ends the run. STOP literal: "the literal is communicated to the operator, and object
     * program execution is suspended", resumed "at the next executable statement" (LR 6.4 p. 457); a lane
     * has no operator, and reads the reply as given at once (LANE-14): the literal on the console, then on.
     */
    // Decision: R-019
    private void stop(Statement s) {
        if (keyword(s, "RUN")) {
            f.jump(String.valueOf(f.end()));
            return;
        }
        List<String> parts = new ArrayList<>();
        for (Statement.Operand o : s.operands()) if (!(o.value() instanceof Value.Name n && n.kind().equals("keyword"))) parts.add(shown(o.value()));
        f.e.line("err.display(true" + (parts.isEmpty() ? "" : ", ") + String.join(", ", parts) + ");");
    }

    /** ACCEPT: from the input device (format 1, any mnemonic), or a conceptual item, DATE to TIME (format 2). */
    // Decision: R-019
    // Unsure: a mnemonic for the console is read as the input device; the lane has one input (J-122). Stays: a fixture with a console mnemonic and an input of its own settles it.
    private void accept(Statement s) {
        Value.Field into = (Value.Field) operand(s, "accept-statement");
        List<String> conceptual = new ArrayList<>();
        for (Statement.Operand o : s.operands()) if (o.role().equals("accept-from-date-statement") && o.value() instanceof Value.Name n && !n.text().equals("FROM")) conceptual.add(n.text());
        boolean other = s.operands().stream().anyMatch(o -> o.role().equals("accept-message-count-statement") || o.role().equals("accept-from-escape-key-statement"));
        String from = String.join(" ", conceptual);
        if (!s.branches().isEmpty() || other || !(conceptual.isEmpty() || IBM_CONCEPTUAL.contains(from))) {
            f.e.line(f.x.unsupported("ACCEPT at " + s.origin() + " " + s.operands()) + ";");
        } else if (conceptual.isEmpty()) f.e.line("in.accept(" + f.x.receiving(into) + ");");
        else f.e.line("Accept.from(" + f.x.receiving(into) + ", " + Emitter.quote(from) + ");");
    }

    /** ACCEPT format 2's conceptual items (LR 6.4 p. 309); the grammar's others are not IBM's. */
    private static final List<String> IBM_CONCEPTUAL = List.of("DATE", "DATE YYYYMMDD", "DAY", "DAY YYYYDDD", "DAY-OF-WEEK", "TIME");

    private String shown(Value v) {
        return switch (v) {
            case Value.Field fld -> f.x.field(fld);
            case Value.Literal l when l.numeric() -> "Literal.bytes(Literal.text(" + Emitter.quote(Exprs.written(l)) + ", cp))";
            case Value.Figurative g -> f.x.pattern(g);
            default -> "Literal.bytes(" + f.x.sending(v) + ")";
        };
    }

    /** GO TO: one procedure (through the ALTER table when altered), none (an altered GO TO), or DEPENDING ON's list; each told to the debugging sections. */
    private void goTo(Statement s) {
        List<Value> targets = operands(s, "go-to-statement-simple", "go-to-depending-on-statement", "go-to-statement");
        Value depending = targets.stream().filter(v -> v instanceof Value.Field).findFirst().orElse(null);
        List<Value.Target> to = targets.stream().filter(v -> v instanceof Value.Target).map(v -> (Value.Target) v).toList();
        if (depending == null && to.isEmpty()) {
            f.debugging.goTo(f.e, f.line, false);
            alteredOnly();
            return;
        }
        if (depending == null) {
            f.debugging.goTo(f.e, f.line, to.get(0).section());
            f.jump(altered(to.get(0).paragraph()));
            return;
        }
        f.e.open("");
        f.e.line("long g = Table.index(" + f.x.field((Value.Field) depending) + ");");
        for (int k = 0; k < to.size(); k++) {
            f.e.open("if (g == " + (k + 1) + ")");
            f.debugging.goTo(f.e, f.line, to.get(k).section());
            f.e.line("return " + to.get(k).paragraph() + ";");
            f.e.close();
        }
        f.e.close();
    }

    /**
     * An altered GO TO, naming no procedure: where the last ALTER sent it; before any, "the GO TO
     * statement acts like a CONTINUE statement" (LR 6.4 p. 347).
     */
    private void alteredOnly() {
        String target = altered(-1);
        if (target.equals("-1")) return;
        f.e.open("");
        f.e.line("int g = " + target + ";");
        f.e.line("if (g >= 0) return g;");
        f.e.close();
    }

    /** A GO TO's target, through the dispatcher's table when an ALTER names this paragraph. */
    private String altered(int target) {
        int here = paragraphIndex();
        for (Procedure.Paragraph p : f.p.procedure().paragraphs()) for (Statement t : p.statements()) if (alters(t, here)) return "d.goTo(" + here + ", " + target + ")";
        return String.valueOf(target);
    }

    /** Whether a statement or one in its branches ALTERs a paragraph's GO TO, so that GO TO is read from the dispatcher's table (R-021). */
    private static boolean alters(Statement s, int paragraph) {
        if (s.verb().equals("ALTER")) {
            List<Value> targets = operands(s, "alter-statement", "alter-proceed-to");
            for (int k = 0; k < targets.size(); k += 2) if (targets.get(k) instanceof Value.Target t && t.paragraph() == paragraph) return true;
        }
        for (Statement.Branch b : s.branches()) for (Statement t : b.statements()) if (alters(t, paragraph)) return true;
        return false;
    }

    /** ALTER p TO PROCEED TO q: each pair, the paragraph whose GO TO changes and its new target. */
    private void alter(Statement s) {
        List<Value> targets = operands(s, "alter-statement", "alter-proceed-to");
        for (int k = 0; k + 1 < targets.size(); k += 2) {
            if (!(targets.get(k) instanceof Value.Target from && targets.get(k + 1) instanceof Value.Target to)) continue;
            f.e.line("d.alter(" + from.paragraph() + ", " + to.paragraph() + ");");
            f.debugging.alter(f.e, from.paragraph(), to.paragraph(), f.line, f.paragraph);
        }
    }

    /** The paragraph being lowered, by its index: a name can stand for two paragraphs in two sections (NIST OBNC2M's PARA-6B, J-177). */
    private int paragraphIndex() {
        return f.paragraphIndex();
    }

    /** SET: an index or item TO a value, UP or DOWN BY one, or a condition name TO TRUE (its first value into its item). */
    // Decision: R-019
    private void set(Statement s) {
        if (s.operands().stream().anyMatch(o -> o.value() instanceof Value.Name n && n.kind().equals("mnemonic"))) {
            setSwitches(s);
            return;
        }
        Value value = operand(s, "set-to-value"), by = operand(s, "set-by-value");
        for (Value t : operands(s, "set-to")) {
            if (t instanceof Value.ConditionName c && value instanceof Value.Literal l && l.payload().equals("TRUE")) setTrue(c);
            else if (t instanceof Value.Field target && by != null) {
                Value step = keyword(s, "DOWN") ? new Value.Expression("-", List.of(target, by)) : new Value.Expression("+", List.of(target, by));
                f.e.line("Arithmetic.store(" + f.x.field(target) + ", " + f.arith.result(step, 0, false) + ", false, false);");
            } else if (t instanceof Value.Field target && value != null && !(value instanceof Value.Literal l && l.kind() == Value.Literal.Kind.BOOLEAN)) {
                f.e.line(f.x.moveInto(value, f.x.field(target), f.x.item(target.item())) + ";");
            } else f.e.line(f.x.unsupported("SET at " + s.origin() + " " + s.operands()) + ";");
        }
    }

    /** SET mnemonic-name ... TO ON or OFF, in groups as written: each mnemonic's UPSI switch set. */
    // Decision: R-019
    private void setSwitches(Statement s) {
        List<String> pending = new ArrayList<>();
        for (Statement.Operand o : s.operands()) {
            if (o.value() instanceof Value.Name n && n.kind().equals("mnemonic")) pending.add(n.text());
            if (!(o.role().equals("set-to-value") && o.value() instanceof Value.Name v && (v.text().equals("ON") || v.text().equals("OFF")))) continue;
            for (String m : pending) {
                String env = f.p.header().mnemonics().stream().filter(k -> k.name().equals(m)).map(katana.sir.types.Header.Mnemonic::environment).filter(x -> x.matches("UPSI-[0-7]")).findFirst().orElse(null);
                f.e.line(env == null ? f.x.unsupported("SET of switch " + m + " at " + s.origin()) + ";" : "upsi.set(" + env.charAt(5) + ", " + v.text().equals("ON") + ");");
            }
            pending.clear();
        }
    }

    private void setTrue(Value.ConditionName c) {
        for (katana.sir.types.Item.Condition k : f.x.item(c.item()).conditions()) {
            if (!k.name().equals(c.name())) continue;
            Value first = k.values().get(0);
            if (first instanceof Value.Expression e && e.operator().equals("THRU")) first = e.operands().get(0);
            Value.Field item = new Value.Field(c.item(), c.subscripts(), null, null);
            f.e.line(f.x.moveInto(first, f.x.field(item), f.x.item(c.item())) + ";");
            return;
        }
    }

    private void exit(Statement s) {
        if (keyword(s, "PROGRAM")) f.jump(String.valueOf(f.end()));
        else if (keyword(s, "PARAGRAPH") || keyword(s, "SECTION")) {
            int last = keyword(s, "SECTION") ? lastOfSection() : paragraphIndex();
            f.jump(f.end(last));
        }
        else f.e.line("// EXIT");
    }

    private int lastOfSection() {
        String section = f.p.procedure().paragraphs().get(paragraphIndex()).section();
        int last = paragraphIndex();
        for (Procedure.Paragraph p : f.p.procedure().paragraphs()) if (p.section().equals(section)) last = p.index();
        return last;
    }
}
