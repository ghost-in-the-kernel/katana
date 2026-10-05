package katana.front;

import java.util.List;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

/** A lexer's and a parser's syntax errors as lines in a list, never on the console. */
final class Errors {
    private Errors() {}

    static void listen(Lexer lexer, Parser parser, List<String> errors, String where) {
        BaseErrorListener listener = new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object symbol, int line, int column, String message, RecognitionException e) {
                errors.add(where + " " + line + ":" + column + ": " + message);
            }
        };
        lexer.removeErrorListeners();
        parser.removeErrorListeners();
        lexer.addErrorListener(listener);
        parser.addErrorListener(listener);
    }
}
