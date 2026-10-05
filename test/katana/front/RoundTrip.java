package katana.front;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * Phase 2.3's check, until lowering exists (3.3): from the parsed text back to the COBOL text is exact.
 * Every SOURCE character is its file's character at its line and column (tabs expanded at the file's
 * width); every REPLACED or ADDED one points at a place inside its file; every token of the tree has
 * an origin in a file.
 */
final class RoundTrip {
    private final Map<String, String[]> files = new HashMap<>();
    private final ToIntFunction<String> tabWidth;

    RoundTrip(ToIntFunction<String> tabWidth) {
        this.tabWidth = tabWidth;
    }

    /** What does not round-trip, at most a few; empty when the map is exact. */
    List<String> check(Parse.Result result) {
        List<String> wrong = new ArrayList<>();
        Mapped text = result.text();
        for (int i = 0; i < text.length() && wrong.size() < 3; i++) {
            Mapped.Origin o = text.origin(i);
            if (o.file().isEmpty()) continue;
            String[] lines = lines(o.file());
            boolean inside = o.line() >= 1 && o.line() <= lines.length && o.column() >= 1 && o.column() <= Math.max(1, lines[o.line() - 1].length() + 1);
            if (!inside) wrong.add("character " + i + " points outside " + o);
            else if (o.kind() == Mapped.Kind.SOURCE && (o.column() > lines[o.line() - 1].length() || lines[o.line() - 1].charAt(o.column() - 1) != text.charAt(i))) {
                wrong.add("character " + i + " '" + text.charAt(i) + "' is not the source's at " + o);
            }
        }
        tokens(result.tree(), result, wrong);
        return wrong;
    }

    /** Every token of the tree has a file, up to the first few that do not. */
    private void tokens(ParseTree node, Parse.Result result, List<String> wrong) {
        if (wrong.size() >= 3) return;
        if (node instanceof TerminalNode t) {
            Token token = t.getSymbol();
            if (token.getType() != Token.EOF && result.origin(token).file().isEmpty()) wrong.add("token " + token.getText() + " has no file");
            return;
        }
        if (node instanceof ParserRuleContext) for (int i = 0; i < node.getChildCount(); i++) tokens(node.getChild(i), result, wrong);
    }

    private String[] lines(String file) {
        return files.computeIfAbsent(file, f -> {
            try {
                byte[] bytes = Tabs.expand(Files.readAllBytes(Path.of(f)), tabWidth.applyAsInt(Path.of(f).getFileName().toString()));
                return new String(bytes, StandardCharsets.ISO_8859_1).split("\n", -1);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }
}
