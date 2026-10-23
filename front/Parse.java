package katana.front;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import katana.front.grammar.CobolLexer;
import katana.front.grammar.CobolParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * A COBOL program to a parse tree (docs/ROADMAP.md 2.1): its tabs expanded, preprocessed (COPY,
 * REPLACE, EXEC), then parsed by the forked grammar. The tree is good only when nothing went wrong:
 * no preprocessing error, no syntax error, no error node in the tree. Every token, and so every tree
 * node through its first token, has an origin in the program or a copybook (Mapped, 2.3).
 */
public final class Parse {
    /** The tree, the text it was parsed from with each character's origin, and every error on the way to it. */
    public record Result(ParserRuleContext tree, Mapped text, List<String> errors, List<Excluded> excluded) {
        public boolean clean() {
            return errors.isEmpty();
        }

        /** Where a token's first character came from. */
        public Mapped.Origin origin(Token token) {
            return text.origin(token.getStartIndex());
        }

        /** Where a node's first token came from. */
        public Mapped.Origin origin(ParserRuleContext node) {
            return origin(node.getStart());
        }
    }

    private Parse() {}

    /** A program file, its tabs expanded at the width its copybooks are given. */
    public static Result file(Path program, Copybooks copybooks, int tabWidth) {
        return file(program, copybooks, tabWidth, Settings.UNDECLARED);
    }

    /** The same under the compile step's settings: the job's DEFINE names and the dialect's extensions (R-081). */
    public static Result file(Path program, Copybooks copybooks, int tabWidth, Settings settings) {
        try {
            byte[] bytes = Tabs.expand(Files.readAllBytes(program), tabWidth);
            return text(new String(bytes, StandardCharsets.ISO_8859_1), program.toString(), copybooks, settings);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** A program's text (tabs expanded): SLL prediction first, full LL only when SLL fails. */
    public static Result text(String source, Copybooks copybooks) {
        return text(source, "", copybooks);
    }

    /** A program's text (tabs expanded) and the file it came from, for the origins. */
    public static Result text(String source, String file, Copybooks copybooks) {
        return text(source, file, copybooks, Settings.UNDECLARED);
    }

    /** The same under the compile step's settings (R-081). */
    public static Result text(String source, String file, Copybooks copybooks, Settings settings) {
        Preprocessor.Result pre = Preprocessor.run(source, file, copybooks, settings);
        List<String> errors = new ArrayList<>(pre.errors());
        List<String> sll = new ArrayList<>();
        ParserRuleContext tree = parse(pre.text(), PredictionMode.SLL, sll);
        if (!sll.isEmpty()) tree = parse(pre.text(), PredictionMode.LL, errors);
        int nodes = errorNodes(tree);
        if (nodes > 0 && errors.size() == pre.errors().size()) errors.add(nodes + " error nodes in the tree");
        return new Result(tree, pre.mapped(), errors, pre.excluded());
    }

    /** SPECIAL-NAMES' DECIMAL-POINT IS COMMA, which makes a comma in a numeric literal its decimal point. */
    private static final java.util.regex.Pattern DECIMAL_COMMA = java.util.regex.Pattern.compile("(?i)DECIMAL-POINT\\s+(IS\\s+)?COMMA");

    private static ParserRuleContext parse(String text, PredictionMode mode, List<String> errors) {
        CobolLexer lexer = new CobolLexer(CharStreams.fromString(text));
        lexer.decimalComma = DECIMAL_COMMA.matcher(text).find();
        CobolParser parser = new CobolParser(new CommonTokenStream(lexer));
        parser.getInterpreter().setPredictionMode(mode);
        Errors.listen(lexer, parser, errors, "parsing");
        return parser.startRule();
    }

    private static int errorNodes(ParseTree tree) {
        if (tree instanceof ErrorNode) return 1;
        int n = 0;
        for (int i = 0; i < tree.getChildCount(); i++) n += errorNodes(tree.getChild(i));
        return n;
    }
}
