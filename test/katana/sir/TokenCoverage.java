package katana.sir;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import katana.front.Parse;
import katana.front.grammar.CobolParser;
import katana.sir.types.Item;
import katana.sir.types.Origin;
import katana.sir.types.Procedure;
import katana.sir.types.Program;
import katana.sir.types.Statement;
import katana.sir.types.Value;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * Tithe pass 2, A1, "no token left behind", reading the other side: every token of each statement in
 * the parse tree (IS, ARE, THEN, END-x and punctuation aside), paired with the IR's by textual order (a
 * copybook copied twice gives two statements of one origin), is found in the IR's statement, as a verb
 * or role word, a keyword, a literal, an operator (a relation's words by its symbol), or the name of an
 * item, an ancestor of one, a condition, a file, a paragraph or a section it names.
 */
final class TokenCoverage {
    private static final Set<String> NOISE = Set.of("IS", "ARE", "THEN", ".", ",", "(", ")", ":", "<EOF>");
    private static final Set<String> RELATION = Set.of("GREATER", "LESS", "EQUAL", "THAN", "TO", "OR", "NOT", "=", "<", ">", "<=", ">=", "<>");

    private TokenCoverage() {}

    /** Each token of the tree's statements the IR lacks, as `file:line: WORD in VERB`. */
    static List<String> missing(Parse.Result parse, Program program) {
        return missing(parse, List.of(program));
    }

    /** As above, for every top-level program of the source, in order. */
    static List<String> missing(Parse.Result parse, List<Program> programs) {
        List<Statement> ir = new ArrayList<>();
        List<Program> owners = new ArrayList<>();
        programs.forEach(p -> index(p, ir, owners));
        List<CobolParser.StatementContext> tree = statements(parse.tree());
        if (tree.size() != ir.size()) return List.of("the tree has " + tree.size() + " statements, the IR " + ir.size());
        List<String> missing = new ArrayList<>();
        for (int i = 0; i < tree.size(); i++) {
            var o = parse.origin(tree.get(i));
            Origin at = ir.get(i).origin();
            if (!o.file().equals(at.file()) || o.line() != at.line() || o.column() != at.column()) return List.of("statement " + i + " is at " + at + " in the IR, not " + o);
            Set<String> words = new HashSet<>();
            words(ir.get(i), owners.get(i), words);
            for (String token : tokens(tree.get(i))) if (!covered(token, words)) missing.add(o.file() + ":" + o.line() + ": " + token + " in " + ir.get(i).verb());
        }
        return missing;
    }

    /** The IR's statements in textual order (a program's, then its nested programs'), each with the program it is in. */
    private static void index(Program p, List<Statement> ir, List<Program> owners) {
        for (Procedure.Paragraph g : p.procedure().paragraphs()) for (Statement s : g.statements()) {
            ir.add(s);
            owners.add(p);
        }
        p.nested().forEach(n -> index(n, ir, owners));
    }

    /** The statements a sentence holds directly (nested ones are inside their parent's tokens). */
    private static List<CobolParser.StatementContext> statements(ParseTree node) {
        List<CobolParser.StatementContext> found = new ArrayList<>();
        if (node instanceof CobolParser.SentenceContext sentence) return new ArrayList<>(sentence.statement());
        for (int i = 0; i < node.getChildCount(); i++) found.addAll(statements(node.getChild(i)));
        return found;
    }

    private static List<String> tokens(ParseTree node) {
        List<String> tokens = new ArrayList<>();
        if (node instanceof TerminalNode t) {
            String w = t.getText().toUpperCase(Locale.ROOT);
            if (!NOISE.contains(w) && !w.startsWith("END-")) tokens.add(w);
        }
        for (int i = 0; i < node.getChildCount(); i++) tokens.addAll(tokens(node.getChild(i)));
        return tokens;
    }

    private static boolean covered(String token, Set<String> words) {
        if (words.contains(token)) return true;
        if (RELATION.contains(token) && words.contains("<relation>")) return true;
        if ((token.equals("OF") || token.equals("IN")) && (words.contains("<field>") || words.contains("<target>") || words.contains("<file>"))) return true;
        if (token.matches("[-+]\\d+") && words.contains(token.substring(0, 1)) && words.contains(token.substring(1))) return true;
        String singular = token.replaceAll("(ES|S)$", "");
        return words.contains(singular) || words.contains(token.replaceAll("S$", ""));
    }

    private static void words(Statement s, Program p, Set<String> words) {
        for (String w : s.verb().split(" ")) words.add(w);
        for (Statement.Operand o : s.operands()) {
            for (String w : o.role().toUpperCase(Locale.ROOT).split("-")) words.add(w);
            value(o.value(), p, words);
        }
        for (Statement.Branch b : s.branches()) {
            for (String w : b.label().toUpperCase(Locale.ROOT).split("-")) words.add(w);
            value(b.condition(), p, words);
            for (Statement t : b.statements()) words(t, p, words);
        }
    }

    private static void value(Value v, Program p, Set<String> words) {
        switch (v) {
            case null -> { }
            case Value.Field f -> {
                words.add("<field>");
                names(f.item(), p, words);
                f.subscripts().forEach(x -> value(x, p, words));
                value(f.start(), p, words);
                value(f.length(), p, words);
            }
            case Value.ConditionName c -> {
                words.add(c.name());
                words.add("<field>");
                names(c.item(), p, words);
                c.subscripts().forEach(x -> value(x, p, words));
            }
            case Value.File f -> {
                words.add(f.name());
                words.add("<file>");
            }
            case Value.Target t -> {
                words.add("<target>");
                for (int i = t.paragraph(); i <= t.thru(); i++) {
                    Procedure.Paragraph g = p.procedure().paragraphs().get(i);
                    words.add(g.name());
                    words.add(g.section());
                }
            }
            case Value.Name n -> {
                words.add(n.text());
                for (String w : n.text().split("[^A-Z0-9-]+")) words.add(w);
            }
            case Value.Literal l -> words.add(l.text().toUpperCase(Locale.ROOT));
            case Value.Figurative f -> {
                words.add(f.name());
                if (f.all() != null) words.add(f.all().text().toUpperCase(Locale.ROOT));
            }
            case Value.Expression e -> {
                for (String w : e.operator().split("[ -]")) words.add(w);
                for (String w : e.operator().split(" ")) words.add(w);
                words.add(e.operator());
                if (List.of("=", "<>", "<", "<=", ">", ">=").contains(e.operator())) words.add("<relation>");
                if (e.operator().equals("neg")) words.add("-");
                if (e.operator().equals("pos")) words.add("+");
                e.operands().forEach(x -> value(x, p, words));
            }
        }
    }

    /** An item's name, its ancestors', and its record's file (a qualifier names one of them). */
    private static void names(int item, Program p, Set<String> words) {
        for (int id = item; id != 0; ) {
            Item i = p.items().get(id - 1);
            words.add(i.name());
            for (var f : p.files()) if (f.records().contains(i.record())) words.add(f.name());
            id = i.parent();
        }
    }
}
