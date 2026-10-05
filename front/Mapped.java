package katana.front;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Text that knows where each of its characters came from (docs/ROADMAP.md 2.3): a file, a line and a
 * column, counted after the file's tabs are expanded (R-016). A character is SOURCE when it is that
 * file's character at that place; REPLACED when a REPLACE or REPLACING put it there, at the place of
 * the text it replaced; ADDED when the front end wrote it (an indent, a tag, a line break, a blank)
 * with the place of the nearest source character. The preprocessed text the grammar reads is one of
 * these, so every token, and every tree node through its first token, has an origin.
 */
public final class Mapped {
    /** How a character came to be in the text. */
    public enum Kind { SOURCE, REPLACED, ADDED }

    /** Where a character came from. */
    public record Origin(String file, int line, int column, Kind kind) {}

    private final StringBuilder text = new StringBuilder();
    private String[] files = new String[64];
    private int[] lines = new int[64];
    private int[] columns = new int[64];
    private Kind[] kinds = new Kind[64];

    /** A file's text as written: every character SOURCE, at its line and column. */
    public static Mapped source(String text, String file) {
        Mapped m = new Mapped();
        int line = 1, column = 1;
        for (char c : text.toCharArray()) {
            m.add(c, file, line, column, Kind.SOURCE);
            if (c == '\n') {
                line++;
                column = 1;
            } else {
                column++;
            }
        }
        return m;
    }

    /** One character at an origin. */
    public Mapped add(char c, String file, int line, int column, Kind kind) {
        int at = text.length();
        if (at == lines.length) grow();
        text.append(c);
        files[at] = file;
        lines[at] = line;
        columns[at] = column;
        kinds[at] = kind;
        return this;
    }

    /** Characters the front end writes, at the origin of the character at `near` of `from` (or nowhere). */
    public Mapped added(String s, Mapped from, int near) {
        Origin o = from == null || from.length() == 0 ? new Origin("", 0, 0, Kind.ADDED) : from.origin(Math.max(0, Math.min(near, from.length() - 1)));
        for (char c : s.toCharArray()) add(c, o.file(), o.line(), o.column(), Kind.ADDED);
        return this;
    }

    /** Characters from..to of another text, each with its origin. */
    public Mapped append(Mapped other, int from, int to) {
        for (int i = from; i < to; i++) add(other.text.charAt(i), other.files[i], other.lines[i], other.columns[i], other.kinds[i]);
        return this;
    }

    public Mapped append(Mapped other) {
        return append(other, 0, other.length());
    }

    /** The text from..to, with its origins. */
    public Mapped slice(int from, int to) {
        return new Mapped().append(this, from, to);
    }

    /** The text cut to its first `length` characters. */
    public void truncate(int length) {
        text.setLength(length);
    }

    public int length() {
        return text.length();
    }

    public char charAt(int i) {
        return text.charAt(i);
    }

    public Origin origin(int i) {
        return new Origin(files[i], lines[i], columns[i], kinds[i]);
    }

    @Override
    public String toString() {
        return text.toString();
    }

    /** Every match of the pattern replaced by the text, each replacing character at the origin of its match's first. */
    public Mapped replaceAll(Pattern pattern, String replacement) {
        Mapped out = new Mapped();
        Matcher m = pattern.matcher(text);
        int at = 0;
        while (m.find()) {
            out.append(this, at, m.start());
            Origin o = origin(m.start());
            for (char c : replacement.toCharArray()) out.add(c, o.file(), o.line(), o.column(), Kind.REPLACED);
            at = m.end();
        }
        return out.append(this, at, length());
    }

    private void grow() {
        int size = lines.length * 2;
        files = Arrays.copyOf(files, size);
        lines = Arrays.copyOf(lines, size);
        columns = Arrays.copyOf(columns, size);
        kinds = Arrays.copyOf(kinds, size);
    }
}
