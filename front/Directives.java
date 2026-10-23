package katana.front;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The `>>` compiler directives that decide which text the program has, read before the lines are
 * (Lines): `>>IF`, `>>ELIF`, `>>ELSE` and `>>END-IF` over `name [IS] [NOT] DEFINED` or `name = literal`,
 * `>>DEFINE name AS literal`, and, where the dialect allows them (Settings.extensions), `>>ELIF` and `>>D` (a
 * debugging line, which becomes a `D` indicator for R-019's rule); IBM's own directives are not known to include
 * those two (research 59), so under IBM's dialect they are errors.
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
        int start;
        String reason = "";
        boolean taken;
        boolean live;
        boolean elsed;

        Frame(boolean outer) {
            this.outer = outer;
        }
    }

    /** The text with its directives applied, what stopped it, and the lines a false condition excluded. */
    record Result(String text, List<String> errors, List<Excluded> excluded) {}

    /** The pass's working state: the names defined so far, the open conditionals, and what it found. */
    private record State(Settings settings, Map<String, String> defined, Deque<Frame> open, List<String> errors, List<Excluded> excluded, String file) {}

    private Directives() {}

    /** The source (tabs already expanded) with its directives applied, or as it was when one is not read here or has an error. */
    // Decision: R-081
    static Result apply(String source, String file, Settings settings) {
        if (!source.contains(">>")) return new Result(source, List.of(), List.of());
        State st = new State(settings, new HashMap<>(settings.isDeclared() ? settings.defines() : Map.of()), new ArrayDeque<>(), new ArrayList<>(), new ArrayList<>(), file);
        String[] lines = source.split("\n", -1);
        StringBuilder out = new StringBuilder(source.length());
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            boolean live = st.open().isEmpty() || st.open().peek().live;
            Matcher m = DIRECTIVE.matcher(line.endsWith("\r") ? line.substring(0, line.length() - 1) : line);
            boolean directive = m.matches() && m.group(2).isBlank();
            if (!directive) {
                out.append(live ? line : blank(line));
            } else {
                String word = m.group(3).toUpperCase(Locale.ROOT), rest = m.group(4);
                if (word.equals("D") && settings.extensions()) {
                    out.append(live ? debugging(line) : blank(line));
                } else if (!directive(word, rest, live, st, i + 1)) {
                    return new Result(source, st.errors(), List.of());
                } else {
                    out.append(blank(line));
                }
            }
            if (i < lines.length - 1) out.append('\n');
        }
        return st.open().isEmpty() ? new Result(out.toString(), List.of(), st.excluded()) : new Result(source, List.of(), List.of());
    }

    /** One conditional or DEFINE directive applied at `line`; false when it is not one this reads, or is an error. */
    private static boolean directive(String word, String rest, boolean live, State st, int line) {
        if (word.equals("DEFINE")) {
            Matcher m = DEFINE.matcher(rest);
            if (!m.matches()) return false;
            String name = m.group(1).toUpperCase(Locale.ROOT), value = unquote(m.group(2));
            if (live && valueless(name, st, line)) return false;
            if (live && st.settings().isDeclared() && st.settings().defines().containsKey(name) && !st.settings().defines().get(name).equals(value)) {
                st.errors().add(where(st, line) + ">>DEFINE " + name + " AS " + value + " contradicts the job's DEFINE(" + name + "=" + st.settings().defines().get(name)
                        + "); which one governs is not known (R-081)");
                return false;
            }
            if (live) st.defined().put(name, value);
            return true;
        }
        String text = ">>" + word + (rest.isEmpty() ? "" : " " + rest);
        return switch (word) {
            case "IF" -> opened(rest, live, st, line, text);
            case "ELIF" -> st.settings().extensions() && next(rest, st, line, text);
            case "ELSE" -> otherwise(rest, st, line, text);
            case "END-IF" -> closed(rest, st, line);
            default -> false;
        };
    }

    private static boolean opened(String rest, boolean live, State st, int line, String text) {
        Frame f = new Frame(live);
        Boolean value = live ? condition(rest, st, line) : Boolean.FALSE;
        if (value == null) return false;
        f.taken = value;
        f.live = live && value;
        st.open().push(f);
        branch(f, st, line, text);
        return true;
    }

    /** An `>>ELIF` under the extensions switch: closes the open branch and opens the next, live only when the outer text is live and no earlier branch was taken (R-081); false when no `>>IF` is open, after an `>>ELSE`, or when its condition is unknown. */
    private static boolean next(String rest, State st, int line, String text) {
        if (st.open().isEmpty() || st.open().peek().elsed) return false;
        Frame f = st.open().peek();
        leave(f, st, line);
        Boolean value = f.outer && !f.taken ? condition(rest, st, line) : Boolean.FALSE;
        if (value == null) return false;
        f.live = f.outer && !f.taken && value;
        f.taken |= value;
        branch(f, st, line, text);
        return true;
    }

    private static boolean otherwise(String rest, State st, int line, String text) {
        if (st.open().isEmpty() || st.open().peek().elsed || !rest.isEmpty()) return false;
        Frame f = st.open().peek();
        leave(f, st, line);
        f.live = f.outer && !f.taken;
        f.taken = true;
        f.elsed = true;
        branch(f, st, line, text);
        return true;
    }

    private static boolean closed(String rest, State st, int line) {
        if (st.open().isEmpty() || !rest.isEmpty()) return false;
        leave(st.open().pop(), st, line);
        return true;
    }

    /** A branch begins after the directive on `line`: when its condition is false inside live text, its range starts. */
    private static void branch(Frame f, State st, int line, String text) {
        f.start = f.outer && !f.live ? line + 1 : 0;
        f.reason = text;
    }

    /** A directive on `line` ends the frame's branch: its excluded range, if it has lines, is recorded. */
    private static void leave(Frame f, State st, int line) {
        if (f.start > 0 && line - 1 >= f.start) st.excluded().add(new Excluded(st.file(), f.start, line - 1, f.reason));
        f.start = 0;
    }

    private static String where(State st, int line) {
        return (st.file().isEmpty() ? "" : st.file() + " ") + "line " + line + ": ";
    }

    /** A condition's value, null when it is not one of the two forms read, or when it tests a name the job may define from outside the source. */
    private static Boolean condition(String text, State st, int line) {
        Matcher d = DEFINED.matcher(text);
        Matcher e = EQUALS.matcher(text);
        boolean isDefined = d.matches();
        if (!isDefined && !e.matches()) return null;
        String name = (isDefined ? d.group(1) : e.group(1)).toUpperCase(Locale.ROOT);
        if (!st.settings().isDeclared() && !st.defined().containsKey(name)) {
            st.errors().add(where(st, line) + ">>IF tests " + name + ", which the source never >>DEFINEs and the job does not declare; the DEFINE compiler option may define it,"
                    + " so the branch is not chosen silently (R-081)");
            return null;
        }
        if (isDefined) return st.defined().containsKey(name) != (d.group(2) != null);
        if (valueless(name, st, line)) return null;
        String value = st.defined().get(name);
        boolean same = value != null && value.equals(unquote(e.group(4)));
        return same != (e.group(2) != null || e.group(3).equals("<>"));
    }

    /** True, with the error said, when the job defined `name` with no value (`-D NAME`) and the source asks or sets its value. */
    // Decision: R-084
    private static boolean valueless(String name, State st, int line) {
        if (!Settings.UNKNOWN.equals(st.defined().get(name))) return false;
        st.errors().add(where(st, line) + "-D" + name + " gives no value; which one cobc gives is not confirmed (R-084)");
        return true;
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
