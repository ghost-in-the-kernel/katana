package katana.front;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Reference format to the text the forked grammars read (front/grammar/), one output line for each
 * source line so a line number stays a line number. Columns 1-6 are the sequence area, 7 the indicator,
 * 8-72 areas A and B; past 72 is not read. A comment line (`*`, `/`) becomes `*> text`; a `$` directive
 * line is blank; a debug line (`D`) is read as code. A continuation line (`-`) is joined to the line it
 * continues, which keeps its text to column 72 when a literal is open there, and is left blank. In the
 * IDENTIFICATION DIVISION the comment entries after AUTHOR., INSTALLATION., DATE-WRITTEN.,
 * DATE-COMPILED., SECURITY. and REMARKS. are marked `*>CE` until a line starts in area A. A tag is
 * always followed by a blank, as the grammars' comment tokens require.
 */
public final class Lines {
    static final String INDENT = "       ";
    private static final List<String> ENTRY_START = List.of("AUTHOR.", "INSTALLATION.", "DATE-WRITTEN.", "DATE-COMPILED.", "SECURITY.", "REMARKS.");

    private Lines() {}

    /** One source line's parts. */
    private record Line(char indicator, String areaA, String content) {
        static Line of(String text) {
            if (text.length() < 7) return new Line(' ', "", "");
            String content = text.length() > 72 ? text.substring(7, 72) : text.substring(7);
            return new Line(text.charAt(6), content.length() > 4 ? content.substring(0, 4) : content, content);
        }
    }

    /** The source (tabs already expanded) as the grammars read it. */
    public static String normalize(String source) {
        List<String> out = new ArrayList<>();
        int logical = -1;
        boolean entry = false;
        for (String text : source.split("\r?\n", -1)) {
            Line line = Line.of(text);
            switch (line.indicator()) {
                case '*', '/' -> out.add(INDENT + "*> " + line.content().stripTrailing());
                case '$' -> out.add("");
                case '-' -> {
                    if (logical < 0) {
                        out.add(INDENT + line.content().stripTrailing());
                    } else {
                        out.set(logical, join(out.get(logical), line.content()));
                        out.add("");
                    }
                }
                default -> {
                    String content = line.content();
                    String upper = content.trim().toUpperCase(Locale.ROOT);
                    boolean starts = ENTRY_START.stream().anyMatch(upper::startsWith);
                    if (starts) {
                        int dot = content.indexOf('.') + 1;
                        String rest = content.substring(dot);
                        content = content.substring(0, dot) + (rest.isBlank() ? "" : " *>CE" + rest);
                    } else if (entry && line.areaA().isBlank() && !content.isBlank()) {
                        content = "*>CE " + content.trim();
                    }
                    entry = starts || (entry && line.areaA().isBlank());
                    logical = out.size();
                    out.add(INDENT + content);
                }
            }
        }
        return String.join("\n", out.stream().map(Lines::trailing).toList());
    }

    /** A logical line continued: an open literal resumes after the continuation's quote, a word directly. */
    private static String join(String previous, String continuation) {
        String next = continuation.stripLeading();
        boolean quoted = next.startsWith("\"") || next.startsWith("'");
        // A literal closed in column 72 and resumed by a quote: the two quotes are one quote in the literal.
        if (quoted && !open(previous) && previous.length() >= INDENT.length() + 65 && (previous.endsWith("\"") || previous.endsWith("'"))) {
            return previous + next.substring(1);
        }
        if (open(previous)) {
            String padded = previous.length() < INDENT.length() + 65 ? previous + " ".repeat(INDENT.length() + 65 - previous.length()) : previous;
            return padded + (next.startsWith("\"") || next.startsWith("'") ? next.substring(1) : next);
        }
        return previous.stripTrailing() + next;
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

    /** Trailing blanks removed, unless a literal is open; a trailing comma or semicolon keeps one blank. */
    private static String trailing(String line) {
        if (line.startsWith(INDENT + "*> ") || open(line)) return line;
        String trimmed = line.stripTrailing();
        return trimmed.endsWith(",") || trimmed.endsWith(";") ? trimmed + " " : trimmed;
    }
}
