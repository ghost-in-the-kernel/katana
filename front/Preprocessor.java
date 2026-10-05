package katana.front;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import katana.front.grammar.CobolPreprocessorBaseListener;
import katana.front.grammar.CobolPreprocessorLexer;
import katana.front.grammar.CobolPreprocessorParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * The text the main grammar reads, from the normalized lines (Lines): each COPY replaced by its
 * copybook, itself preprocessed, under its REPLACING; each REPLACE applied to the text it governs; each
 * EXEC CICS, EXEC SQL and EXEC SQLIMS block written as tagged lines (`*>EXECCICS ...`, its END-EXEC
 * closed by `}`); EJECT, SKIP, TITLE and compiler options dropped. What cannot be done (a copybook not
 * found, a COPY nested too deep, a syntax error) is an error, never a silent gap.
 */
public final class Preprocessor extends CobolPreprocessorBaseListener {
    /** The text, each character with its origin, and what went wrong. */
    public record Result(Mapped mapped, List<String> errors) {
        public String text() {
            return mapped.toString();
        }
    }

    private static final int DEPTH = 20;
    private final Deque<Mapped> contexts = new ArrayDeque<>();
    private final Mapped input;
    private final CommonTokenStream tokens;
    private final Copybooks copybooks;
    private final List<String> errors;
    private final int depth;

    private Preprocessor(Mapped input, CommonTokenStream tokens, Copybooks copybooks, List<String> errors, int depth) {
        this.input = input;
        this.tokens = tokens;
        this.copybooks = copybooks;
        this.errors = errors;
        this.depth = depth;
        contexts.push(new Mapped());
    }

    /** Preprocesses a source's text (tabs expanded, not yet normalized). */
    public static Result run(String source, Copybooks copybooks) {
        return run(source, "", copybooks);
    }

    /** Preprocesses a file's text (tabs expanded, not yet normalized), its origins in that file. */
    public static Result run(String source, String file, Copybooks copybooks) {
        List<String> errors = new ArrayList<>();
        return new Result(text(source, file, copybooks, errors, 0, "the program"), errors);
    }

    private static Mapped text(String source, String file, Copybooks copybooks, List<String> errors, int depth, String where) {
        Mapped input = Lines.normalize(source, file);
        CobolPreprocessorLexer lexer = new CobolPreprocessorLexer(CharStreams.fromString(input.toString()));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CobolPreprocessorParser parser = new CobolPreprocessorParser(tokens);
        Errors.listen(lexer, parser, errors, "preprocessing " + where);
        Preprocessor listener = new Preprocessor(input, tokens, copybooks, errors, depth);
        ParseTreeWalker.DEFAULT.walk(listener, parser.startRule());
        return listener.contexts.peek();
    }

    /** A statement the preprocessor rewrites or drops gets its own text, so its words never reach the output. */
    @Override
    public void enterEveryRule(ParserRuleContext ctx) {
        if (ctx instanceof CobolPreprocessorParser.CompilerOptionsContext || ctx instanceof CobolPreprocessorParser.CopyStatementContext
                || ctx instanceof CobolPreprocessorParser.EjectStatementContext || ctx instanceof CobolPreprocessorParser.SkipStatementContext
                || ctx instanceof CobolPreprocessorParser.TitleStatementContext || ctx instanceof CobolPreprocessorParser.ExecCicsStatementContext
                || ctx instanceof CobolPreprocessorParser.ExecSqlStatementContext || ctx instanceof CobolPreprocessorParser.ExecSqlImsStatementContext
                || ctx instanceof CobolPreprocessorParser.ReplaceAreaContext || ctx instanceof CobolPreprocessorParser.ReplaceByStatementContext
                || ctx instanceof CobolPreprocessorParser.ReplaceOffStatementContext) {
            contexts.push(new Mapped());
        }
    }

    @Override
    public void exitCompilerOptions(CobolPreprocessorParser.CompilerOptionsContext ctx) {
        contexts.pop();
    }

    @Override
    public void exitEjectStatement(CobolPreprocessorParser.EjectStatementContext ctx) {
        contexts.pop();
    }

    @Override
    public void exitSkipStatement(CobolPreprocessorParser.SkipStatementContext ctx) {
        contexts.pop();
    }

    @Override
    public void exitTitleStatement(CobolPreprocessorParser.TitleStatementContext ctx) {
        contexts.pop();
    }

    @Override
    public void exitReplaceByStatement(CobolPreprocessorParser.ReplaceByStatementContext ctx) {
        contexts.pop();
    }

    @Override
    public void exitReplaceOffStatement(CobolPreprocessorParser.ReplaceOffStatementContext ctx) {
        contexts.pop();
    }

    /** The COPY's own words dropped; the copybook's text, preprocessed and replaced, written in their place. */
    @Override
    public void exitCopyStatement(CobolPreprocessorParser.CopyStatementContext ctx) {
        contexts.pop();
        String name = ctx.copySource().getChildCount() > 0 ? ctx.copySource().getChild(0).getText() : "";
        String library = ctx.copySource().copyLibrary() != null ? ctx.copySource().copyLibrary().getText()
                : ctx.directoryPhrase().isEmpty() ? null : ctx.directoryPhrase(0).getChild(ctx.directoryPhrase(0).getChildCount() - 1).getText();
        Copybooks.Book copybook = copybooks.read(name, library);
        if (copybook == null) {
            errors.add("copybook " + name + " not found (line " + ctx.getStart().getLine() + ")");
            return;
        }
        if (depth >= DEPTH) {
            errors.add("copybook " + name + " nested " + DEPTH + " deep (line " + ctx.getStart().getLine() + ")");
            return;
        }
        Mapped text = text(copybook.text(), copybook.file(), copybooks, errors, depth + 1, "copybook " + name);
        for (CobolPreprocessorParser.ReplacingPhraseContext replacing : ctx.replacingPhrase()) text = replace(text, replacing.replaceClause());
        contexts.peek().append(text).added("\n", text, text.length() - 1);
    }

    @Override
    public void exitExecCicsStatement(CobolPreprocessorParser.ExecCicsStatementContext ctx) {
        exec(ctx, "*>EXECCICS");
    }

    @Override
    public void exitExecSqlStatement(CobolPreprocessorParser.ExecSqlStatementContext ctx) {
        exec(ctx, "*>EXECSQL");
    }

    @Override
    public void exitExecSqlImsStatement(CobolPreprocessorParser.ExecSqlImsStatementContext ctx) {
        exec(ctx, "*>EXECSQLIMS");
    }

    /** An EXEC block as tagged lines, each one the main grammar reads as a single token. */
    private void exec(ParserRuleContext ctx, String tag) {
        contexts.pop();
        Mapped block = input.slice(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex() + 1), out = contexts.peek();
        String text = block.toString();
        int at = 0;
        while (at <= text.length()) {
            int end = text.indexOf('\n', at);
            if (end < 0) end = text.length();
            int from = at, to = end;
            while (from < to && Character.isWhitespace(text.charAt(from))) from++;
            while (to > from && Character.isWhitespace(text.charAt(to - 1))) to--;
            if (at > 0) out.added("\n", block, at - 1);
            out.added(Lines.INDENT + tag + " ", block, from);
            Matcher endExec = END_EXEC.matcher(text).region(from, to);
            int copied = from;
            while (endExec.find()) {
                out.append(block, copied, endExec.end()).added(" }", block, endExec.end() - 1);
                copied = endExec.end();
            }
            out.append(block, copied, to);
            at = end + 1;
        }
    }

    private static final Pattern END_EXEC = Pattern.compile("(?i)end-exec");

    /** The text a REPLACE governs, replaced, written to the enclosing text. */
    @Override
    public void exitReplaceArea(CobolPreprocessorParser.ReplaceAreaContext ctx) {
        Mapped text = replace(contexts.pop(), ctx.replaceByStatement().replaceClause());
        contexts.peek().append(text);
    }

    @Override
    public void visitTerminal(TerminalNode node) {
        Mapped out = contexts.peek();
        List<Token> hidden = tokens.getHiddenTokensToLeft(node.getSymbol().getTokenIndex());
        if (hidden != null) hidden.forEach(t -> out.append(input, t.getStartIndex(), t.getStopIndex() + 1));
        Token token = node.getSymbol();
        if (token.getType() != Token.EOF) out.append(input, token.getStartIndex(), token.getStopIndex() + 1);
    }

    /** Each clause applied, the longest replaceable first; a replaceable that starts or ends a word matches whole words only. */
    private Mapped replace(Mapped text, List<CobolPreprocessorParser.ReplaceClauseContext> clauses) {
        List<String[]> pairs = new ArrayList<>();
        for (var clause : clauses) pairs.add(new String[] {operand(clause.replaceable()), operand(clause.replacement())});
        pairs.sort(Comparator.comparingInt((String[] p) -> p[0].length()).reversed());
        for (String[] pair : pairs) {
            if (pair[0].isEmpty()) continue;
            List<String> parts = new ArrayList<>();
            for (String part : pair[0].split("\\s+")) parts.add(Pattern.quote(part));
            String regex = String.join("\\s+", parts);
            if (Character.isLetterOrDigit(pair[0].charAt(0))) regex = "(?<![\\w-])" + regex;
            if (Character.isLetterOrDigit(pair[0].charAt(pair[0].length() - 1))) regex = regex + "(?![\\w-])";
            text = text.replaceAll(Pattern.compile(regex, Pattern.CASE_INSENSITIVE), pair[1]);
        }
        return text;
    }

    /** A replaceable or replacement as text: pseudo-text without its `==`, else the operand as written. */
    private String operand(ParserRuleContext operand) {
        String text = tokens.getText(operand.getStart(), operand.getStop()).trim();
        return text.startsWith("==") && text.endsWith("==") && text.length() >= 4 ? text.substring(2, text.length() - 2).trim() : text;
    }
}
