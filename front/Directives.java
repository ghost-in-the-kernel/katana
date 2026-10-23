package katana.front;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The `>>` compiler directives that decide which text the program has, read before the lines are
 * (Lines): `>>IF`, `>>ELIF`, `>>ELSE` and `>>END-IF` over `name [IS] [NOT] DEFINED` or `name = literal`,
 * `>>DEFINE name AS literal`, and `>>D` (a debugging line, which becomes a `D` indicator for R-019's rule).
 * The text keeps its length, every line and every column: a directive line and a line a false condition
 * excludes are blanked. No name is predefined. Any other `>>` directive (SET CONSTANT, DEFINE ... AS
 * PARAMETER, SOURCE, TURN), a condition not read here or a `>>IF` left open or closed twice gives the
 * source back as it was, so the parser stops on the first `>>`: an unread directive is an error, never a
 * silent gap (P-03).
 */
final class Directives {
    private static final Pattern DIRECTIVE = Pattern.compile("^(.{6})(.?)\\s*>>\\s*(\\S*)\\s*(.*?)\\s*$");
    private static final Pattern DEFINE = Pattern.compile("(?i)([A-Z0-9][A-Z0-9_-]*)\\s+AS\\s+(\"[^\"]*\"|'[^']*'|[0-9]+)(?!\\S)\\s*(\\*>.*)?");
    private static final Pattern DEFINED = Pattern.compile("(?i)([A-Z0-9][A-Z0-9_-]*)\\s+(?:IS\\s+)?(NOT\\s+)?DEFINED\\s*(\\*>.*)?");
    private static final Pattern EQUALS = Pattern.compile("(?i)([A-Z0-9][A-Z0-9_-]*)\\s+(?:IS\\s+)?(NOT\\s+)?(=|EQUAL\\s+TO|<>)\\s+(\"[^\"]*\"|'[^']*'|[0-9]+)\\s*(\\*>.*)?");

    /** One open `>>IF`: whether its enclosing text is live, whether a branch was taken, whether this branch is live. */
    private static final class Frame {
        final boolean outer;
        boolean taken;
        boolean live;
        boolean elsed;

        Frame(boolean outer) {
            this.outer = outer;
        }
    }

    private Directives() {}

    /** The source (tabs already expanded) with its directives applied, or as it was when one is not read here. */
    // Decision: R-081
    static String apply(String source) {
        if (!source.contains(">>")) return source;
        Map<String, String> defined = new HashMap<>();
        Deque<Frame> open = new ArrayDeque<>();
        String[] lines = source.split("\n", -1);
        StringBuilder out = new StringBuilder(source.length());
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            boolean live = open.isEmpty() || open.peek().live;
            Matcher m = DIRECTIVE.matcher(line.endsWith("\r") ? line.substring(0, line.length() - 1) : line);
            boolean directive = m.matches() && m.group(2).isBlank();
            if (!directive) {
                out.append(live ? line : blank(line));
            } else {
                String word = m.group(3).toUpperCase(Locale.ROOT), rest = m.group(4);
                if (word.equals("D")) {
                    out.append(live ? debugging(line) : blank(line));
                } else if (!directive(word, rest, live, open, defined)) {
                    return source;
                } else {
                    out.append(blank(line));
                }
            }
            if (i < lines.length - 1) out.append('\n');
        }
        return open.isEmpty() ? out.toString() : source;
    }

    /** One conditional or DEFINE directive applied; false when it is not one this reads. */
    private static boolean directive(String word, String rest, boolean live, Deque<Frame> open, Map<String, String> defined) {
        if (word.equals("DEFINE")) {
            Matcher m = DEFINE.matcher(rest);
            if (!m.matches()) return false;
            if (live) defined.put(m.group(1).toUpperCase(Locale.ROOT), unquote(m.group(2)));
            return true;
        }
        return switch (word) {
            case "IF" -> opened(rest, live, open, defined);
            case "ELIF" -> next(rest, open, defined);
            case "ELSE" -> otherwise(rest, open);
            case "END-IF" -> closed(rest, open);
            default -> false;
        };
    }

    private static boolean opened(String rest, boolean live, Deque<Frame> open, Map<String, String> defined) {
        Frame f = new Frame(live);
        Boolean value = live ? condition(rest, defined) : Boolean.FALSE;
        if (value == null) return false;
        f.taken = value;
        f.live = live && value;
        open.push(f);
        return true;
    }

    private static boolean next(String rest, Deque<Frame> open, Map<String, String> defined) {
        if (open.isEmpty() || open.peek().elsed) return false;
        Frame f = open.peek();
        Boolean value = f.outer && !f.taken ? condition(rest, defined) : Boolean.FALSE;
        if (value == null) return false;
        f.live = f.outer && !f.taken && value;
        f.taken |= value;
        return true;
    }

    private static boolean otherwise(String rest, Deque<Frame> open) {
        if (open.isEmpty() || open.peek().elsed || !rest.isEmpty()) return false;
        Frame f = open.peek();
        f.live = f.outer && !f.taken;
        f.taken = true;
        f.elsed = true;
        return true;
    }

    private static boolean closed(String rest, Deque<Frame> open) {
        if (open.isEmpty() || !rest.isEmpty()) return false;
        open.pop();
        return true;
    }

    /** A condition's value, or null when it is not one of the two forms read. */
    private static Boolean condition(String text, Map<String, String> defined) {
        Matcher d = DEFINED.matcher(text);
        if (d.matches()) return defined.containsKey(d.group(1).toUpperCase(Locale.ROOT)) != (d.group(2) != null);
        Matcher e = EQUALS.matcher(text);
        if (!e.matches()) return null;
        String value = defined.get(e.group(1).toUpperCase(Locale.ROOT));
        boolean same = value != null && value.equals(unquote(e.group(4)));
        return same != (e.group(2) != null || e.group(3).equals("<>"));
    }

    private static String unquote(String literal) {
        char c = literal.charAt(0);
        return c == '"' || c == '\'' ? literal.substring(1, literal.length() - 1) : literal;
    }

    private static String blank(String line) {
        return line.endsWith("\r") ? " ".repeat(line.length() - 1) + "\r" : " ".repeat(line.length());
    }

    /** `>>D text` as a `D` in column 7 and the same text in the same columns. */
    private static String debugging(String line) {
        int at = line.indexOf(">>");
        char[] chars = line.toCharArray();
        for (int k = at; k < at + 3 && k < chars.length; k++) chars[k] = ' ';
        chars[6] = 'D';
        return new String(chars);
    }
}
