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
    private record Raw(String name, String section, List<CobolParser.SentenceContext> sentences, ParserRuleContext at) {}

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
            for (var d : pd.procedureDeclaratives().procedureDeclarative()) section(d.procedureSectionHeader(), d.paragraphs(), true, DataMap.text(d.useStatement()));
        }
        paragraphs("", pd.procedureDivisionBody().paragraphs(), pd);
        for (var s : pd.procedureDivisionBody().procedureSection()) section(s.procedureSectionHeader(), s.paragraphs(), false, "");
        List<Procedure.Paragraph> built = new ArrayList<>();
        for (int i = 0; i < raw.size(); i++) {
            current = i;
            Raw r = raw.get(i);
            List<Statement> statements = new ArrayList<>();
            for (var sentence : r.sentences()) for (var st : sentence.statement()) statements.add(statement(st));
            built.add(new Procedure.Paragraph(i, r.name(), r.section(), statements, Origins.at(parse, r.at())));
        }
        return new Procedure(using, returning, sections, built, edges);
    }

    private void section(CobolParser.ProcedureSectionHeaderContext header, CobolParser.ParagraphsContext body, boolean declarative, String use) {
        String name = Values.upper(header.sectionName().getText());
        sections.add(new Procedure.Section(name, declarative, use, Origins.at(parse, header)));
        int first = raw.size();
        paragraphs(name, body, header);
        sectionRanges.put(name, new int[] {first, raw.size() - 1});
    }

    /** Statements under no paragraph name make a paragraph named ""; the body's always does, even empty. */
    private void paragraphs(String section, CobolParser.ParagraphsContext body, ParserRuleContext at) {
        if (!body.sentence().isEmpty() || (section.isEmpty() && raw.isEmpty())) raw.add(new Raw("", section, body.sentence(), at));
        for (var p : body.paragraph()) raw.add(new Raw(Values.upper(p.paragraphName().getText()), section, p.sentence(), p));
    }

    /** One statement: its verb, operands and branches; PERFORM, GO TO and ALTER also make edges, CALL a Call. */
    private Statement statement(CobolParser.StatementContext s) {
        ParserRuleContext verb = (ParserRuleContext) s.getChild(0);
        List<Statement.Operand> operands = new ArrayList<>();
        Map<String, List<Statement>> branches = new LinkedHashMap<>();
        walk(verb, verb, operands, branches);
        List<Statement.Branch> built = new ArrayList<>();
        branches.forEach((label, list) -> built.add(new Statement.Branch(label, null, list)));
        Origin origin = Origins.at(parse, s);
        edges(verb, origin);
        if (verb instanceof CobolParser.CallStatementContext c) calls.add(call(c, origin));
        return new Statement(verbName(verb), operands, built, origin);
    }

    /** A statement's subtree: values to operands, nested statements to branches, other rules walked into. */
    private void walk(ParserRuleContext node, ParserRuleContext verb, List<Statement.Operand> operands, Map<String, List<Statement>> branches) {
        for (ParseTree child : node.children == null ? List.<ParseTree>of() : node.children) {
            if (child instanceof TerminalNode) continue;
            if (child instanceof CobolParser.StatementContext nested) {
                branches.computeIfAbsent(role(node), k -> new ArrayList<>()).add(statement(nested));
            } else if (child instanceof CobolParser.ProcedureNameContext p) {
                operands.add(new Statement.Operand(role(node), target(p)));
            } else if (Values.isValue(child)) {
                operands.add(new Statement.Operand(role(node), values.of(child)));
            } else {
                walk((ParserRuleContext) child, verb, operands, branches);
            }
        }
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
                walk(phrase, phrase, found, new LinkedHashMap<>());
                for (var o : found) using.add(new Call.Argument(mode, o.value()));
            }
        }
        Value returning = c.callGivingPhrase() == null ? null : values.of(c.callGivingPhrase().identifier());
        return new Call(target, using, returning, origin);
    }

    /** `moveStatement` to MOVE, `goToStatement` to GO TO. */
    static String verbName(ParserRuleContext verb) {
        String rule = verb.getClass().getSimpleName().replace("StatementContext", "").replace("Context", "");
        return rule.replaceAll("([a-z])([A-Z])", "$1 $2").toUpperCase(Locale.ROOT);
    }

    /** A rule's name as a role: `moveToSendingArea` to `move-to-sending-area`. */
    static String role(ParserRuleContext rule) {
        String name = rule.getClass().getSimpleName().replace("Context", "");
        return name.replaceAll("([a-z0-9])([A-Z])", "$1-$2").toLowerCase(Locale.ROOT);
    }
}
