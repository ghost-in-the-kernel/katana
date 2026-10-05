package katana.oracle.autotest;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * GNU Autotest test cases (the `.at` format the Autoconf manual describes) to plain data (tithe pass 2,
 * B3): each `AT_SETUP` … `AT_CLEANUP` a case with its title, its `AT_KEYWORDS`, the files its
 * `AT_DATA` writes, and each `AT_CHECK`'s command, expected exit status, stdout and stderr. m4's
 * quoting is undone one level (`[`…`]`, nesting) and the quadrigraphs (`@<:@` `@:>@` `@S|@` `@%:@`
 * `@&t@`) are replaced. It reads GnuCOBOL's autotests as test input only (R-045): they are fetched in
 * phase 3, under corpus/, and what this writes from them stays there, never in the repository.
 */
public final class Autotests {
    /** One `AT_CHECK`: its shell command, expected status ("0" when not given, "ignore" allowed), stdout and stderr ("" when not given). */
    public record Check(String command, String status, String stdout, String stderr, int line) {}

    /** One test case: where it starts, its title and keywords, the files it writes, its checks in order, and whether `AT_SKIP_IF` or `AT_XFAIL_IF` marks it. */
    public record Case(String file, int line, String title, List<String> keywords, Map<String, String> files, List<Check> checks, boolean conditional) {}

    /** A file the reader cannot read as Autotest, with the line. */
    public static final class Malformed extends RuntimeException {
        Malformed(String file, int line, String what) {
            super(file + ":" + line + ": " + what);
        }
    }

    private final String file;
    private final String text;
    private int at;

    private Autotests(String file, String text) {
        this.file = file;
        this.text = text;
    }

    /** Every case of one `.at` file's text, in order. */
    public static List<Case> read(String file, String text) {
        return new Autotests(file, text).cases();
    }

    private List<Case> cases() {
        List<Case> cases = new ArrayList<>();
        Builder open = null;
        while (at < text.length()) {
            char c = text.charAt(at);
            if (c == '[') {
                quoted();
            } else if (c == '#' || text.startsWith("dnl", at) && word(at, 3)) {
                skipLine();
            } else if (text.startsWith("AT_", at) && (at == 0 || !wordChar(text.charAt(at - 1)))) {
                int line = line(at);
                String name = name();
                List<String> args = at < text.length() && text.charAt(at) == '(' ? arguments(line) : List.of();
                open = macro(name, args, line, open, cases);
            } else {
                at++;
            }
        }
        if (open != null) throw new Malformed(file, open.line, "AT_SETUP with no AT_CLEANUP");
        return cases;
    }

    /** One macro's effect on the case being read: SETUP opens, CLEANUP closes, the others add to it. */
    private Builder macro(String name, List<String> args, int line, Builder open, List<Case> cases) {
        switch (name) {
            case "AT_SETUP" -> {
                if (open != null) throw new Malformed(file, line, "AT_SETUP inside the case of line " + open.line);
                return new Builder(line, arg(args, 0));
            }
            case "AT_CLEANUP" -> {
                if (open == null) throw new Malformed(file, line, "AT_CLEANUP with no AT_SETUP");
                cases.add(new Case(file, open.line, open.title, open.keywords, open.files, open.checks, open.conditional));
                return null;
            }
            default -> {
                if (open == null) return null;
                switch (name) {
                    case "AT_KEYWORDS" -> open.keywords.addAll(List.of(arg(args, 0).trim().split("\\s+")));
                    case "AT_DATA" -> open.files.put(arg(args, 0).trim(), arg(args, 1));
                    case "AT_CHECK" -> open.checks.add(new Check(arg(args, 0), args.size() > 1 && !arg(args, 1).isBlank() ? arg(args, 1).trim() : "0",
                            arg(args, 2), arg(args, 3), line));
                    case "AT_SKIP_IF", "AT_XFAIL_IF" -> open.conditional = true;
                    default -> { }
                }
                return open;
            }
        }
    }

    private static final class Builder {
        final int line;
        final String title;
        final List<String> keywords = new ArrayList<>();
        final Map<String, String> files = new LinkedHashMap<>();
        final List<Check> checks = new ArrayList<>();
        boolean conditional;

        Builder(int line, String title) {
            this.line = line;
            this.title = title;
        }
    }

    /** The arguments of a macro call at `(`: split on commas at depth 0 outside quotes, each unquoted one level, leading unquoted blanks dropped. */
    private List<String> arguments(int line) {
        List<String> args = new ArrayList<>();
        StringBuilder arg = new StringBuilder();
        int depth = 0;
        at++;
        boolean leading = true;
        while (true) {
            if (at >= text.length()) throw new Malformed(file, line, "a macro call that does not close");
            char c = text.charAt(at);
            if (leading && Character.isWhitespace(c)) {
                at++;
                continue;
            }
            leading = false;
            if (c == '[') {
                arg.append(quoted());
                continue;
            }
            at++;
            if (c == '(') depth++;
            if (c == ')' && depth-- == 0) {
                args.add(quadrigraphs(arg.toString()));
                return args;
            }
            if (c == ',' && depth == 0) {
                args.add(quadrigraphs(arg.toString()));
                arg.setLength(0);
                leading = true;
                continue;
            }
            arg.append(c);
        }
    }

    /** A quoted string at `[`: its text with the outer quotes removed and inner ones kept. */
    // Unsure: whether Autotest strips a second level from an expected stdout written `[[x]]` is a hypothesis; phase 3's first run of the extracted cases under cobc settles it (J-087).
    private String quoted() {
        int line = line(at), depth = 0;
        StringBuilder out = new StringBuilder();
        do {
            if (at >= text.length()) throw new Malformed(file, line, "a quote that does not close");
            char c = text.charAt(at++);
            if (c == '[' && depth++ == 0) continue;
            if (c == ']' && --depth == 0) break;
            out.append(c);
        } while (true);
        return out.toString();
    }

    private static String quadrigraphs(String s) {
        return s.replace("@<:@", "[").replace("@:>@", "]").replace("@S|@", "$").replace("@%:@", "#").replace("@&t@", "");
    }

    private static String arg(List<String> args, int i) {
        return i < args.size() ? args.get(i) : "";
    }

    private String name() {
        int start = at;
        while (at < text.length() && wordChar(text.charAt(at))) at++;
        return text.substring(start, at);
    }

    private boolean word(int start, int length) {
        return start + length >= text.length() || !wordChar(text.charAt(start + length));
    }

    private static boolean wordChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    private void skipLine() {
        while (at < text.length() && text.charAt(at) != '\n') at++;
    }

    private int line(int offset) {
        int n = 1;
        for (int i = 0; i < offset; i++) if (text.charAt(i) == '\n') n++;
        return n;
    }

    /**
     * Each case to a folder of its own under `out` (`<file>-<line>/`): the files it writes, and
     * `case.txt` with its title, keywords and checks (each check's stdout and stderr beside it as
     * `check-<n>.stdout` and `.stderr`). For phase 3's runner; under corpus/, never committed (R-045).
     */
    public static void write(List<Case> cases, Path out) {
        try {
            for (Case c : cases) {
                Path dir = Files.createDirectories(out.resolve(c.file().replaceAll("\\.at$", "") + "-" + c.line()));
                for (var f : c.files().entrySet()) Files.writeString(dir.resolve(f.getKey()), f.getValue(), StandardCharsets.ISO_8859_1);
                StringBuilder about = new StringBuilder("title " + c.title() + "\nkeywords " + String.join(" ", c.keywords()) + "\nconditional " + c.conditional() + "\n");
                for (int i = 0; i < c.checks().size(); i++) {
                    Check k = c.checks().get(i);
                    about.append("check ").append(i).append(" status ").append(k.status()).append(" line ").append(k.line()).append('\n').append(k.command().strip()).append('\n');
                    Files.writeString(dir.resolve("check-" + i + ".stdout"), k.stdout(), StandardCharsets.ISO_8859_1);
                    Files.writeString(dir.resolve("check-" + i + ".stderr"), k.stderr(), StandardCharsets.ISO_8859_1);
                }
                Files.writeString(dir.resolve("case.txt"), about, StandardCharsets.ISO_8859_1);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
