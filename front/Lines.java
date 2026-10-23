package katana.front;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Reference format to the text the forked grammars read (front/grammar/), one output line for each
 * source line so a line number stays a line number, each character with its origin (Mapped). Columns
 * 1-6 are the sequence area, 7 the indicator, 8-72 areas A and B; past 72 is not read. A comment line
 * (`*`, `/`) becomes `*> text`; a `$` directive line is blank; a `>>` directive is applied first (Directives); a debug line (`D`) is read as code. A
 * continuation line (`-`) is joined to the line it continues, which keeps its text to column 72 when
 * a literal is open there, and is left blank. In the IDENTIFICATION DIVISION the comment entries after
 * AUTHOR., INSTALLATION., DATE-WRITTEN., DATE-COMPILED., SECURITY. and REMARKS. are marked `*>CE` until
 * a line starts in area A. A tag is always followed by a blank, as the grammars' comment tokens require.
 */
public final class Lines {
    static final String INDENT = "       ";
    private static final List<String> ENTRY_START = List.of("AUTHOR.", "INSTALLATION.", "DATE-WRITTEN.", "DATE-COMPILED.", "SECURITY.", "REMARKS.");

    private Lines() {}

    /** The source (tabs already expanded) as the grammars read it. */
    public static String normalize(String source) {
        return normalize(source, "").toString();
    }

    /** The source (tabs already expanded) as the grammars read it, each character with its place in the file. */
    public static Mapped normalize(String raw, String file) {
        String source = Directives.apply(raw);
        Mapped all = Mapped.source(source, file);
        List<Mapped> out = new ArrayList<>();
        List<Integer> ends = new ArrayList<>();
        int logical = -1, start = 0;
        boolean entry = false;
        for (String text : source.split("\n", -1)) {
            Mapped line = all.slice(start, start + text.length() - (text.endsWith("\r") ? 1 : 0));
            start += text.length() + 1;
            ends.add(Math.min(start - 1, all.length() - 1));
            Mapped content = line.length() < 8 ? new Mapped() : line.slice(7, Math.min(72, line.length()));
            char indicator = line.length() < 7 ? ' ' : line.charAt(6);
            Mapped built = new Mapped();
            switch (indicator) {
                case '*', '/' -> out.add(built.added(INDENT + "*> ", line, 6).append(strip(content)));
                case '$' -> out.add(built);
                case '-' -> {
                    if (logical < 0) {
                        out.add(built.added(INDENT, line, 0).append(strip(content)));
                    } else {
                        out.set(logical, join(out.get(logical), content));
                        out.add(built);
                    }
                }
                default -> {
                    String upper = content.toString().trim().toUpperCase(Locale.ROOT);
                    boolean starts = ENTRY_START.stream().anyMatch(upper::startsWith);
                    String areaA = content.length() > 4 ? content.toString().substring(0, 4) : content.toString();
                    built.added(INDENT, line, 0);
                    if (starts) {
                        int dot = content.toString().indexOf('.') + 1;
                        built.append(content, 0, dot);
                        if (!content.toString().substring(dot).isBlank()) built.added(" *>CE", content, dot).append(content, dot, content.length());
                    } else if (entry && areaA.isBlank() && !content.toString().isBlank()) {
                        int first = content.length() - content.toString().stripLeading().length();
                        built.added("*>CE ", content, first).append(strip(content.slice(first, content.length())));
                    } else {
                        built.append(content);
                    }
                    entry = starts || (entry && areaA.isBlank());
                    logical = out.size();
                    out.add(marked(built, indicator));
                }
            }
        }
        Mapped result = new Mapped();
        for (int i = 0; i < out.size(); i++) {
            if (i > 0) result.added("\n", all, ends.get(i - 1));
            result.append(trailing(out.get(i)));
        }
        return result;
    }

    /** A debugging line (LR 6.4 p. 122) marked: code only WITH DEBUGGING MODE, which `Preprocessor.run` decides. */
    private static Mapped marked(Mapped line, char indicator) {
        return indicator == 'D' || indicator == 'd' ? line.debugging() : line;
    }

    /** A logical line continued: an open literal resumes after the continuation's quote, a word directly. */
    private static Mapped join(Mapped previous, Mapped continuation) {
        int first = continuation.length() - continuation.toString().stripLeading().length();
        Mapped next = continuation.slice(first, continuation.length());
        String p = previous.toString(), n = next.toString();
        boolean quoted = n.startsWith("\"") || n.startsWith("'");
        Mapped joined = previous.slice(0, previous.length());
        // A literal closed in column 72 and resumed by a quote: the two quotes are one quote in the literal.
        if (quoted && !open(p) && p.length() >= INDENT.length() + 65 && (p.endsWith("\"") || p.endsWith("'"))) {
            return joined.append(next, 1, next.length());
        }
        if (open(p)) {
            if (p.length() < INDENT.length() + 65) joined.added(" ".repeat(INDENT.length() + 65 - p.length()), previous, p.length() - 1);
            return joined.append(next, quoted ? 1 : 0, next.length());
        }
        joined.truncate(p.stripTrailing().length());
        return joined.append(next);
    }

    /** Whether a quote is still open at the end of the text. */
    static boolean open(String text) {
        char quote = 0;
        for (char c : text.toCharArray()) {
            if (quote == 0 && (c == '"' || c == '\'')) quote = c;
            else if (c == quote) quote = 0;
        }
        return quote != 0;
    }

    private static Mapped strip(Mapped m) {
        m.truncate(m.toString().stripTrailing().length());
        return m;
    }

    /** Trailing blanks removed, unless a literal is open or it is a comment; a trailing comma or semicolon keeps one blank. */
    private static Mapped trailing(Mapped line) {
        String s = line.toString();
        if (s.startsWith(INDENT + "*> ") || open(s)) return line;
        strip(line);
        if (s.stripTrailing().endsWith(",") || s.stripTrailing().endsWith(";")) line.added(" ", line, line.length() - 1);
        return line;
    }
}
