package katana.sir.build;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import katana.front.Parse;
import katana.front.grammar.CobolParser;
import katana.sir.types.Origin;
import katana.sir.types.Call;
import katana.sir.types.Procedure;
import katana.sir.types.Statement;
import katana.sir.types.Value;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * The PROCEDURE DIVISION to the paragraph graph: sections (declaratives first, as written) and
 * paragraphs numbered in textual order, each paragraph's statements, and the PERFORM, GO TO and ALTER
 * edges, a section named standing for its paragraphs. Statements are built as the grammar shapes them:
 * the verb is the statement rule's name, each value an operand whose role is the rule that holds it,
 * each list of nested statements a branch named for its rule. A target that names nothing is a Name
 * and a finding, for sir/check.
 */
final class Paragraphs {
    private record Raw(String name, String section, List<CobolParser.SentenceContext> sentences, CobolParser.AlteredGoToContext altered, ParserRuleContext at) {}

    private final Parse.Result parse;
    private final Values values;
    private final List<String> findings;
    private final List<Raw> raw = new ArrayList<>();
    private final List<Procedure.Section> sections = new ArrayList<>();
    private final Map<String, int[]> sectionRanges = new LinkedHashMap<>();
    private final List<Procedure.Edge> edges = new ArrayList<>();
    final List<Call> calls = new ArrayList<>();
    private int current;

    Paragraphs(Parse.Result parse, Values values, List<String> findings) {
        this.parse = parse;
        this.values = values;
        this.findings = findings;
    }

    /** The paragraph graph of a PROCEDURE DIVISION, its USING and RETURNING items given. */
    Procedure build(CobolParser.ProcedureDivisionContext pd, List<Integer> using, int returning) {
        if (pd.procedureDeclaratives() != null) {
            for (var d : pd.procedureDeclaratives().procedureDeclarative()) section(d.procedureSectionHeader(), d.paragraphs(), true, use(d.useStatement()));
        }
        paragraphs("", pd.procedureDivisionBody().paragraphs(), pd);
        for (var s : pd.procedureDivisionBody().procedureSection()) section(s.procedureSectionHeader(), s.paragraphs(), false, null);
        List<Procedure.Paragraph> built = new ArrayList<>();
        for (int i = 0; i < raw.size(); i++) {
            current = i;
            Raw r = raw.get(i);
            List<Statement> statements = new ArrayList<>();
            List<Integer> starts = new ArrayList<>();
            // An altered GO TO, alone in its paragraph (LR 6.4 p. 347): a GO TO that names no procedure.
            if (r.altered() != null) {
                starts.add(0);
                statements.add(new Statement("GO TO", List.of(), List.of(), Origins.at(parse, r.altered())));
            }
            for (var sentence : r.sentences()) {
                if (!sentence.statement().isEmpty()) starts.add(statements.size());
                for (var st : sentence.statement()) statements.add(statement(st));
            }
            built.add(new Procedure.Paragraph(i, r.name(), r.section(), statements, starts, Origins.at(parse, r.at())));
        }
        return new Procedure(using, returning, sections, built, edges);
    }

    /** A USE statement as data (SIR-10): what it follows, GLOBAL, and the files or the mode, or what it debugs. */
    private Procedure.Use use(CobolParser.UseStatementContext u) {
        if (u.useDebugClause() != null) {
            List<String> on = u.useDebugClause().useDebugOn().stream().map(d -> Values.upper(DataMap.text(d))).toList();
            return new Procedure.Use(Procedure.Use.Kind.DEBUGGING, false, "", List.of(), List.of(), on);
        }
        CobolParser.UseAfterClauseContext a = u.useAfterClause();
        CobolParser.UseAfterOnContext on = a.useAfterOn();
        List<String> named = on.fileName().stream().map(f -> Values.upper(f.getText())).toList();
        List<String> files = named.stream().filter(f -> !(values.file(f) instanceof Value.Name)).toList();
        List<String> outer = named.stream().filter(f -> values.file(f) instanceof Value.Name).toList();
        String mode = named.isEmpty() ? Values.upper(on.getText()).replace("I_O", "I-O") : "";
        return new Procedure.Use(Procedure.Use.Kind.ERROR, a.GLOBAL() != null, mode, files, outer, List.of());
    }

    private void section(CobolParser.ProcedureSectionHeaderContext header, CobolParser.ParagraphsContext body, boolean declarative, Procedure.Use use) {
        String name = Values.upper(header.sectionName().getText());
        int priority = header.integerLiteral() == null ? -1 : Integer.parseInt(header.integerLiteral().getText().replace("+", ""));
        sections.add(new Procedure.Section(name, declarative, use, priority, Origins.at(parse, header)));
        int first = raw.size();
        paragraphs(name, body, header);
        sectionRanges.put(name, new int[] {first, raw.size() - 1});
    }

    /** Statements under no paragraph name make a paragraph named ""; the body's always does, even empty. */
    private void paragraphs(String section, CobolParser.ParagraphsContext body, ParserRuleContext at) {
        if (!body.sentence().isEmpty() || (section.isEmpty() && raw.isEmpty())) raw.add(new Raw("", section, body.sentence(), null, at));
        for (var p : body.paragraph()) raw.add(new Raw(Values.upper(p.paragraphName().getText()), section, p.sentence(), p.alteredGoTo(), p));
    }

    /** One statement: its verb, operands and branches; PERFORM, GO TO and ALTER also make edges, CALL a Call. */
    private Statement statement(CobolParser.StatementContext s) {
        ParserRuleContext verb = (ParserRuleContext) s.getChild(0);
        List<Statement.Operand> operands = new ArrayList<>();
        List<Statement.Branch> branches = new ArrayList<>();
        List<String> verbWords = new ArrayList<>(List.of(verbName(verb).split(" ")));
        walk(verb, verbWords, operands, branches);
        Origin origin = Origins.at(parse, s);
        edges(verb, origin);
        if (verb instanceof CobolParser.CallStatementContext c) calls.add(call(c, origin));
        return new Statement(verbName(verb), operands, branches, origin);
    }

    /**
     * A statement's subtree, no token left behind (tithe pass 2, A1): a value to an operand, a keyword to
     * a `Name("keyword", X)` operand (but noise, and the verb's own words), a rule holding statements to a
     * branch of its own, any other rule walked into; each operand's role the rule that holds it.
     */
    private void walk(ParserRuleContext node, List<String> verbWords, List<Statement.Operand> operands, List<Statement.Branch> branches) {
        for (ParseTree child : node.children == null ? List.<ParseTree>of() : node.children) {
            if (child instanceof TerminalNode t) {
                String word = Values.upper(t.getText());
                if (!verbWords.isEmpty() && verbWords.get(0).equals(word)) verbWords.remove(0);
                else if (!noise(t)) operands.add(new Statement.Operand(role(node), new Value.Name("keyword", word)));
                continue;
            }
            verbWords.clear();
            if (child instanceof CobolParser.ProcedureNameContext p) {
                operands.add(new Statement.Operand(role(node), target(p)));
            } else if (Values.isValue(child)) {
                operands.add(new Statement.Operand(role(node), values.of(child)));
            } else if (holdsStatements((ParserRuleContext) child)) {
                branches.add(branch((ParserRuleContext) child));
            } else {
                walk((ParserRuleContext) child, verbWords, operands, branches);
            }
        }
    }

    /**
     * A rule holding statements as a branch: its label the rule, its condition what else it holds (its own
     * words, but those its label says, such as AT END in `at-end-phrase`); a phrase's NEXT SENTENCE (IF,
     * SEARCH's WHEN) is a NEXT SENTENCE statement, as one written alone is.
     */
    private Statement.Branch branch(ParserRuleContext rule) {
        List<Statement> statements = new ArrayList<>();
        List<Value> parts = new ArrayList<>();
        List<String> label = List.of(role(rule).toUpperCase(java.util.Locale.ROOT).split("-"));
        for (ParseTree child : rule.children == null ? List.<ParseTree>of() : rule.children) {
            if (child instanceof TerminalNode t && t.getSymbol().getType() == CobolParser.NEXT) {
                statements.add(new Statement("NEXT SENTENCE", List.of(), List.of(), Origins.at(parse, t.getSymbol())));
                break;
            }
            if (child instanceof CobolParser.StatementContext nested) statements.add(statement(nested));
            else if (child instanceof ParserRuleContext r) parts.add(group(r));
            else if (child instanceof TerminalNode t && !noise(t) && !label.contains(Values.upper(t.getText()))) parts.add(new Value.Name("keyword", Values.upper(t.getText())));
        }
        Value condition = parts.isEmpty() ? null : parts.size() == 1 ? parts.get(0) : new Value.Expression(role(rule), parts);
        return new Statement.Branch(role(rule), condition, statements);
    }

    /** A rule inside a condition, whole: a value as itself, else its keywords, values and rules in order under its name. */
    private Value group(ParserRuleContext rule) {
        if (rule instanceof CobolParser.ProcedureNameContext p) return target(p);
        if (Values.isValue(rule)) return values.of(rule);
        List<Value> items = new ArrayList<>();
        for (ParseTree child : rule.children == null ? List.<ParseTree>of() : rule.children) {
            if (child instanceof TerminalNode t) {
                if (!noise(t)) items.add(new Value.Name("keyword", Values.upper(t.getText())));
            } else {
                items.add(group((ParserRuleContext) child));
            }
        }
        return items.size() == 1 && !(items.get(0) instanceof Value.Name) ? items.get(0) : new Value.Expression(role(rule), items);
    }

    /** Whether the grammar lets the rule hold statements: its generated context has a `statement()` list. */
    private static boolean holdsStatements(ParserRuleContext rule) {
        return holdsStatements(rule.getClass());
    }

    /** Whether a generated context class has a `statement()` list. */
    static boolean holdsStatements(Class<?> rule) {
        return HOLDERS.computeIfAbsent(rule, c -> {
            try {
                c.getMethod("statement");
                return true;
            } catch (NoSuchMethodException e) {
                return false;
            }
        });
    }

    private static final Map<Class<?>, Boolean> HOLDERS = new java.util.concurrent.ConcurrentHashMap<>();

    /** Words that carry nothing: IS, ARE, THEN, an END-x, punctuation. */
    static boolean noise(TerminalNode t) {
        String w = Values.upper(t.getText());
        return w.equals("IS") || w.equals("ARE") || w.equals("THEN") || w.startsWith("END-") || w.equals(".") || w.equals(",") || w.equals("(") || w.equals(")")
                || w.equals(":") || w.equals("<EOF>");
    }

    /** PERFORM a [THRU b], GO TO a ... and ALTER a TO b, as edges from the paragraph they are in. */
    private void edges(ParserRuleContext verb, Origin origin) {
        if (verb instanceof CobolParser.PerformStatementContext p && p.performProcedureStatement() != null) {
            var names = p.performProcedureStatement().procedureName();
            int[] from = range(names.get(0)), thru = range(names.get(names.size() - 1));
            if (from != null && thru != null) edges.add(new Procedure.Edge(Procedure.Kind.PERFORM, current, from[0], thru[1], origin));
        } else if (verb instanceof CobolParser.GoToStatementContext g) {
            List<CobolParser.ProcedureNameContext> names = g.goToStatementSimple() != null ? List.of(g.goToStatementSimple().procedureName()) : g.goToDependingOnStatement().procedureName();
            for (var n : names) {
                int[] to = range(n);
                if (to != null) edges.add(new Procedure.Edge(Procedure.Kind.GO_TO, current, to[0], to[0], origin));
            }
        } else if (verb instanceof CobolParser.AlterStatementContext a) {
            for (var pt : a.alterProceedTo()) {
                int[] from = range(pt.procedureName(0)), to = range(pt.procedureName(1));
                if (from != null && to != null) edges.add(new Procedure.Edge(Procedure.Kind.ALTER, from[0], to[0], to[0], origin));
            }
        }
    }

    private Value target(CobolParser.ProcedureNameContext p) {
        int[] r = range(p);
        if (r != null) return new Value.Target(r[0], r[1]);
        findings.add("procedure " + Values.upper(p.getText()) + " at " + Origins.at(parse, p) + " names no paragraph or section");
        return new Value.Name("unresolved procedure", Values.upper(p.getText()));
    }

    /** A procedure name's first and last paragraph: a paragraph (in its section when qualified, else the current one's first), or a section's paragraphs. */
    private int[] range(CobolParser.ProcedureNameContext p) {
        String name = Values.upper(p.paragraphName() != null ? p.paragraphName().getText() : p.sectionName().getText());
        String in = p.inSection() != null ? Values.upper(p.inSection().sectionName().getText()) : null;
        String here = current < raw.size() ? raw.get(current).section() : "";
        int found = -1;
        for (int i = 0; i < raw.size(); i++) {
            Raw r = raw.get(i);
            if (r.name().equals(name) && (in == null || r.section().equals(in))) {
                if (found < 0 || (in == null && r.section().equals(here))) found = i;
            }
        }
        if (found >= 0) return new int[] {found, found};
        int[] section = sectionRanges.get(name);
        return section != null && section[1] >= section[0] ? section : null;
    }

    /** A CALL's target, its USING arguments each with its mode, and its RETURNING field. */
    private Call call(CobolParser.CallStatementContext c, Origin origin) {
        Value target = values.of(c.identifier() != null ? c.identifier() : c.literal());
        List<Call.Argument> using = new ArrayList<>();
        if (c.callUsingPhrase() != null) {
            for (var parameter : c.callUsingPhrase().callUsingParameter()) {
                ParserRuleContext phrase = (ParserRuleContext) parameter.getChild(0);
                Call.Mode mode = phrase instanceof CobolParser.CallByContentPhraseContext ? Call.Mode.CONTENT
                        : phrase instanceof CobolParser.CallByValuePhraseContext ? Call.Mode.VALUE : Call.Mode.REFERENCE;
                List<Statement.Operand> found = new ArrayList<>();
                walk(phrase, new ArrayList<>(), found, new ArrayList<>());
                for (var o : found) {
                    if (o.value() instanceof Value.Name n && n.kind().equals("keyword") && List.of("BY", "REFERENCE", "CONTENT", "VALUE").contains(n.text())) continue;
                    using.add(new Call.Argument(mode, o.value()));
                }
            }
        }
        Value returning = c.callGivingPhrase() == null ? null : values.of(c.callGivingPhrase().identifier());
        return new Call(target, using, returning, origin);
    }

    /** `moveStatement` to MOVE, `goToStatement` to GO TO. */
    static String verbName(ParserRuleContext verb) {
        return verbName(verb.getClass());
    }

    /** A statement context class's verb. */
    static String verbName(Class<?> verb) {
        String rule = verb.getSimpleName().replace("StatementContext", "").replace("Context", "");
        return rule.replaceAll("([a-z])([A-Z])", "$1 $2").toUpperCase(Locale.ROOT);
    }

    /** A rule's name as a role: `moveToSendingArea` to `move-to-sending-area`. */
    static String role(ParserRuleContext rule) {
        return role(rule.getClass());
    }

    /** A context class's role. */
    static String role(Class<?> rule) {
        String name = rule.getSimpleName().replace("Context", "");
        return name.replaceAll("([a-z0-9])([A-Z])", "$1-$2").toLowerCase(Locale.ROOT);
    }
}
