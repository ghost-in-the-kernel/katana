package katana.sir;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import katana.front.Parse;
import katana.front.grammar.CobolParser;
import katana.sir.types.FileDescriptor;
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
 * item, an ancestor of one, a condition, a file, a paragraph or a section it names. And every word of
 * each SELECT entry is in its file (SIR-09), of each FD or SD entry in its file's `Fd` (SIR-11), and of
 * each declarative's USE in its section (SIR-10).
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
        List<String> missing = new ArrayList<>(entries(parse, programs));
        missing.addAll(uses(parse, programs));
        missing.addAll(fds(parse, programs));
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

    /** Each SELECT entry's words its file lacks (SIR-09), entries and files paired in textual order. */
    private static List<String> entries(Parse.Result parse, List<Program> programs) {
        List<Program> owners = new ArrayList<>();
        List<FileDescriptor> files = new ArrayList<>();
        programs.forEach(p -> filesOf(p, files, owners));
        List<CobolParser.FileControlEntryContext> tree = nodes(parse.tree(), CobolParser.FileControlEntryContext.class);
        if (tree.size() != files.size()) return List.of("the tree has " + tree.size() + " SELECT entries, the IR " + files.size() + " files");
        List<String> missing = new ArrayList<>();
        for (int i = 0; i < tree.size(); i++) {
            Set<String> words = file(files.get(i), owners.get(i));
            var o = parse.origin(tree.get(i));
            for (String token : selectTokens(tree.get(i))) if (!words.contains(token)) missing.add(o.file() + ":" + o.line() + ": " + token + " in SELECT " + files.get(i).name());
        }
        return missing;
    }

    /**
     * A SELECT entry's tokens but those of the clauses that change nothing a program computes or writes:
     * PADDING CHARACTER and RECORD DELIMITER, each "syntax checked, but has no effect on the execution of
     * the program" (LR 6.4, FILE-CONTROL), and RESERVE, the number of I/O buffers.
     */
    private static List<String> selectTokens(ParseTree node) {
        if (node instanceof CobolParser.PaddingCharacterClauseContext || node instanceof CobolParser.RecordDelimiterClauseContext || node instanceof CobolParser.ReserveClauseContext) return List.of();
        if (node instanceof TerminalNode) return tokens(node);
        List<String> tokens = new ArrayList<>();
        for (int i = 0; i < node.getChildCount(); i++) tokens.addAll(selectTokens(node.getChild(i)));
        return tokens;
    }

    private static void filesOf(Program p, List<FileDescriptor> files, List<Program> owners) {
        for (FileDescriptor f : p.files()) {
            files.add(f);
            owners.add(p);
        }
        p.nested().forEach(n -> filesOf(n, files, owners));
    }

    /** The words a file carries: each clause's keywords only where the file holds what the clause says. */
    private static Set<String> file(FileDescriptor f, Program p) {
        Set<String> words = new HashSet<>(List.of("SELECT", "ASSIGN", "TO", "ORGANIZATION", "ACCESS", "MODE", f.name()));
        words.addAll(List.of(f.organization().name().split("_")));
        words.add(f.access().name());
        for (String w : f.assign().toUpperCase(Locale.ROOT).split("[^A-Z0-9$#@-]+")) words.add(w);
        words.add("\"" + f.assign() + "\"");
        words.add("'" + f.assign() + "'");
        if (f.optional()) words.add("OPTIONAL");
        if (f.recordKey() != 0) words.addAll(List.of("RECORD", "KEY"));
        if (!f.alternateKeys().isEmpty()) words.addAll(List.of("ALTERNATE", "RECORD", "KEY", "WITH"));
        if (f.alternateKeys().stream().anyMatch(FileDescriptor.AlternateKey::duplicates)) words.add("DUPLICATES");
        if (f.relativeKey() != 0) words.addAll(List.of("RELATIVE", "KEY"));
        if (f.status() != 0) words.addAll(List.of("FILE", "STATUS"));
        for (int item : new int[] {f.recordKey(), f.relativeKey(), f.status(), f.vsamStatus()}) if (item != 0) names(item, p, words);
        f.alternateKeys().forEach(k -> names(k.item(), p, words));
        if (!f.alternateKeys().isEmpty() || f.recordKey() != 0 || f.relativeKey() != 0 || f.status() != 0) words.addAll(List.of("OF", "IN"));
        return words;
    }

    /** Each FD or SD entry's words its file's `Fd` lacks (SIR-11), each entry paired with the first unpaired file of its name. */
    private static List<String> fds(Parse.Result parse, List<Program> programs) {
        List<Program> owners = new ArrayList<>();
        List<FileDescriptor> files = new ArrayList<>();
        programs.forEach(p -> filesOf(p, files, owners));
        List<String> missing = new ArrayList<>();
        for (CobolParser.FileDescriptionEntryContext e : nodes(parse.tree(), CobolParser.FileDescriptionEntryContext.class)) {
            String name = e.fileName().getText().toUpperCase(Locale.ROOT);
            var o = parse.origin(e);
            int at = -1;
            for (int i = 0; i < files.size() && at < 0; i++) if (files.get(i) != null && files.get(i).name().equals(name)) at = i;
            if (at < 0 || files.get(at).fd() == null) {
                missing.add(o.file() + ":" + o.line() + ": FD " + name + " has no file in the IR");
                continue;
            }
            Set<String> words = fd(files.get(at), owners.get(at));
            files.set(at, null);
            for (String token : fdTokens(e)) if (!words.contains(token)) missing.add(o.file() + ":" + o.line() + ": " + token + " in FD " + name);
        }
        return missing;
    }

    /** The words a file's `Fd` carries, each clause's keywords only where the `Fd` holds what the clause says. */
    private static Set<String> fd(FileDescriptor f, Program p) {
        FileDescriptor.Fd d = f.fd();
        Set<String> words = new HashSet<>(List.of(d.sort() ? "SD" : "FD", f.name(), "IS"));
        if (d.external()) words.add("EXTERNAL");
        if (d.global()) words.add("GLOBAL");
        if (d.recordFrom() != 0 || d.recordTo() != 0 || d.varying()) {
            words.addAll(List.of("RECORD", "CONTAINS", "CHARACTERS", String.valueOf(d.recordFrom()), String.valueOf(d.recordTo())));
            if (d.recordTo() != 0 && d.recordTo() != d.recordFrom()) words.add("TO");
            if (d.varying()) words.addAll(List.of("VARYING", "IN", "SIZE", "FROM"));
        }
        if (d.recordDepending() != 0) {
            words.addAll(List.of("DEPENDING", "ON", "OF", "IN"));
            names(d.recordDepending(), p, words);
        }
        FileDescriptor.Linage l = d.linage();
        if (l != null) {
            words.addAll(List.of("LINAGE", "LINES"));
            if (l.footing() != null) words.addAll(List.of("WITH", "FOOTING", "AT"));
            if (l.top() != null) words.addAll(List.of("AT", "TOP"));
            if (l.bottom() != null) words.addAll(List.of("AT", "BOTTOM"));
            for (Value v : java.util.Arrays.asList(l.body(), l.footing(), l.top(), l.bottom())) value(v, p, words);
        }
        if (!d.codeSet().isEmpty()) words.addAll(List.of("CODE-SET", d.codeSet()));
        if (!d.recordingMode().isEmpty()) words.addAll(List.of("RECORDING", "MODE", d.recordingMode()));
        return words;
    }

    /**
     * An FD entry's tokens but its record descriptions (the data map's) and the clauses that change
     * nothing a program does: LABEL RECORDS, VALUE OF, BLOCK CONTAINS ("syntax checked, but has no effect
     * on the execution of the program"), DATA RECORDS ("only as documentation"), and REPORT (Report
     * Writer, a build finding).
     */
    private static List<String> fdTokens(ParseTree node) {
        if (node instanceof CobolParser.DataDescriptionEntryContext || node instanceof CobolParser.LabelRecordsClauseContext || node instanceof CobolParser.ValueOfClauseContext
                || node instanceof CobolParser.BlockContainsClauseContext || node instanceof CobolParser.DataRecordsClauseContext || node instanceof CobolParser.ReportClauseContext) return List.of();
        if (node instanceof TerminalNode) return tokens(node);
        List<String> tokens = new ArrayList<>();
        for (int i = 0; i < node.getChildCount(); i++) tokens.addAll(fdTokens(node.getChild(i)));
        return tokens;
    }

    /** Each declarative's USE words its section lacks (SIR-10), USE statements and declarative sections paired in textual order. */
    private static List<String> uses(Parse.Result parse, List<Program> programs) {
        List<Procedure.Section> sections = new ArrayList<>();
        programs.forEach(p -> declaratives(p, sections));
        List<CobolParser.UseStatementContext> tree = nodes(parse.tree(), CobolParser.UseStatementContext.class);
        if (tree.size() != sections.size()) return List.of("the tree has " + tree.size() + " USE statements, the IR " + sections.size() + " declaratives");
        List<String> missing = new ArrayList<>();
        for (int i = 0; i < tree.size(); i++) {
            Procedure.Use u = sections.get(i).use();
            Set<String> words = new HashSet<>(List.of("USE"));
            if (u.kind() == Procedure.Use.Kind.ERROR) words.addAll(List.of("AFTER", "STANDARD", "ERROR", "EXCEPTION", "PROCEDURE", "ON"));
            else words.addAll(List.of("FOR", "DEBUGGING", "ON"));
            if (u.global()) words.add("GLOBAL");
            if (!u.mode().isEmpty()) words.add(u.mode());
            words.addAll(u.files());
            words.addAll(u.outerFiles());
            for (String d : u.debugging()) words.addAll(List.of(d.split(" ")));
            var o = parse.origin(tree.get(i));
            for (String token : tokens(tree.get(i))) if (!words.contains(token)) missing.add(o.file() + ":" + o.line() + ": " + token + " in USE of " + sections.get(i).name());
        }
        return missing;
    }

    private static void declaratives(Program p, List<Procedure.Section> sections) {
        p.procedure().sections().stream().filter(Procedure.Section::declarative).forEach(sections::add);
        p.nested().forEach(n -> declaratives(n, sections));
    }

    /** Every node of a type in the tree, in textual order. */
    private static <T extends ParseTree> List<T> nodes(ParseTree node, Class<T> type) {
        List<T> found = new ArrayList<>();
        if (type.isInstance(node)) found.add(type.cast(node));
        for (int i = 0; i < node.getChildCount(); i++) found.addAll(nodes(node.getChild(i), type));
        return found;
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
            String w = t.getText().trim().equals(".") ? "." : t.getText().toUpperCase(Locale.ROOT);
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
                for (String w : f.name().split(" ")) words.add(w);
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
