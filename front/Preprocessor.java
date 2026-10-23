package katana.front;

import java.util.ArrayDeque;
import java.util.ArrayList;
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
    public record Result(Mapped mapped, List<String> errors, List<Excluded> excluded) {
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

    private final Run run;

    private Preprocessor(Mapped input, CommonTokenStream tokens, Copybooks copybooks, Run run, int depth) {
        this.run = run;
        this.input = input;
        this.tokens = tokens;
        this.copybooks = copybooks;
        this.errors = run.errors();
        this.depth = depth;
        contexts.push(new Mapped());
    }

    /** Preprocesses a source's text (tabs expanded, not yet normalized). */
    public static Result run(String source, Copybooks copybooks) {
        return run(source, "", copybooks);
    }

    /** Preprocesses a file's text (tabs expanded, not yet normalized), its origins in that file. */
    public static Result run(String source, String file, Copybooks copybooks) {
        return run(source, file, copybooks, Settings.UNDECLARED);
    }

    /** The same under the compile step's settings (R-081). */
    public static Result run(String source, String file, Copybooks copybooks, Settings settings) {
        List<String> errors = new ArrayList<>();
        List<Excluded> excluded = new ArrayList<>();
        Mapped text = text(source, file, copybooks, new Run(settings, errors, excluded), 0, "the program");
        if (!settings.debugging() && !debuggingMode(text.toString())) for (int i = 0; i < text.length(); i++) if (text.charAt(i) != '\n' && text.debugging(i)) text.blank(i);
        return new Result(text, errors, excluded);
    }

    /**
     * Whether a source unit says WITH DEBUGGING MODE, "logically determined after all COPY and REPLACE
     * statements have been processed"; without it, "all debugging sections and debugging lines are
     * treated as comments" (LR 6.4 p. 122, p. 760), so `run` blanks the debugging lines' text.
     */
    // Decision: R-019
    static boolean debuggingMode(String text) {
        for (String line : text.split("\n")) {
            if (line.stripLeading().startsWith("*>")) continue;
            if (DEBUGGING_MODE.matcher(line).find()) return true;
        }
        return false;
    }

    private static final java.util.regex.Pattern DEBUGGING_MODE = java.util.regex.Pattern.compile("(?i)(^|[\\s.])DEBUGGING\\s+MODE([\\s.]|$)");

    /** What one run shares across the program and its copybooks: the settings, the errors and the excluded ranges. */
    private record Run(Settings settings, List<String> errors, List<Excluded> excluded) {}

    private static Mapped text(String source, String file, Copybooks copybooks, Run run, int depth, String where) {
        List<String> errors = run.errors();
        Mapped input = Lines.normalize(source, file, run.settings(), errors, run.excluded());
        CobolPreprocessorLexer lexer = new CobolPreprocessorLexer(CharStreams.fromString(input.toString()));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CobolPreprocessorParser parser = new CobolPreprocessorParser(tokens);
        Errors.listen(lexer, parser, errors, "preprocessing " + where);
        Preprocessor listener = new Preprocessor(input, tokens, copybooks, run, depth);
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
        Mapped text = text(copybook.text(), copybook.file(), copybooks, run, depth + 1, "copybook " + name);
        for (CobolPreprocessorParser.ReplacingPhraseContext replacing : ctx.replacingPhrase()) text = replace(text, replacing.replaceClause());
        // "The compiler treats the copied text as the debugging line or lines" of a COPY on one (LR 6.4 p. 760).
        if (input.debugging(ctx.getStart().getStartIndex())) text.debugging();
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

    /**
     * Between two text words: blanks, a separator comma or semicolon ("each occurrence of a separator comma,
     * a separator semicolon, or a sequence of one or more separator spaces is considered to be a single
     * space", LR 6.4 p. 701), and comment lines and inline comments, which "are ignored for purposes of
     * matching" (p. 702).
     */
    private static final String SEPARATION = "(?:\\s|[,;](?=\\s|$)|\\*>[^\\n]*)+";
    private static final Pattern SEPARATOR = Pattern.compile(SEPARATION);
    /** One source text word: a literal, a parenthesis, or a run of anything else up to a blank, a parenthesis or a quote. */
    private static final Pattern WORD = Pattern.compile("\"[^\"\\n]*\"?|'[^'\\n]*'?|[()]|[^\\s()\"']+");

    /**
     * REPLACING and REPLACE as IBM's comparison rules give them (LR 6.4 p. 701): from the leftmost source
     * text word, each replaceable tried in the order written; on a match the replacement is put in and the
     * word after the matched text is the next leftmost, so a replacement is never matched again (NIST
     * SM206A's 1 BY 5, 5 BY 7 gives 5, not 7); on none, the next word is. A replaceable that starts or ends
     * with a letter or digit matches whole words only (J-173).
     */
    private Mapped replace(Mapped text, List<CobolPreprocessorParser.ReplaceClauseContext> clauses) {
        List<Pattern> patterns = new ArrayList<>();
        List<String> replacements = new ArrayList<>();
        for (var clause : clauses) {
            String written = operand(clause.replaceable()).replaceAll("\\*>[^\\n]*", " ").trim();
            // A replaceable that is only a separator comma or semicolon matches it as a text word (p. 701).
            String replaceable = written.matches("[,;]") ? written : written.replaceAll("[,;](?=\\s|$)", " ").trim();
            if (replaceable.isEmpty()) continue;
            List<String> parts = new ArrayList<>();
            for (String part : replaceable.split("\\s+")) parts.add(Pattern.quote(part));
            String regex = String.join(SEPARATION, parts);
            if (Character.isLetterOrDigit(replaceable.charAt(0))) regex = "(?<![\\w-])" + regex;
            if (Character.isLetterOrDigit(replaceable.charAt(replaceable.length() - 1))) regex = regex + "(?![\\w-])";
            patterns.add(Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
            replacements.add(operand(clause.replacement()));
        }
        String s = text.toString();
        Mapped out = new Mapped();
        int copied = 0, at = 0;
        while (at < s.length()) {
            Matcher gap = SEPARATOR.matcher(s).region(at, s.length());
            if (gap.lookingAt() && gap.end() > at) {
                at = gap.end();
                continue;
            }
            int matched = -1;
            for (int k = 0; k < patterns.size() && matched < 0; k++) {
                Matcher m = patterns.get(k).matcher(s).region(at, s.length()).useTransparentBounds(true);
                if (m.lookingAt()) {
                    out.append(text, copied, at);
                    out.replacement(text, at, replacements.get(k));
                    copied = matched = m.end();
                }
            }
            if (matched >= 0) {
                at = matched;
                continue;
            }
            Matcher word = WORD.matcher(s).region(at, s.length());
            at = word.lookingAt() && word.end() > at ? word.end() : at + 1;
        }
        return out.append(text, copied, s.length());
    }

    /** A replaceable or replacement as text: pseudo-text without its `==`, else the operand as written. */
    private String operand(ParserRuleContext operand) {
        String text = tokens.getText(operand.getStart(), operand.getStop()).trim();
        return text.startsWith("==") && text.endsWith("==") && text.length() >= 4 ? text.substring(2, text.length() - 2).trim() : text;
    }
}
