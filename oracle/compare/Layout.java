package katana.oracle.compare;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A record's layout read from copybook text (docs/ROADMAP.md 1.7): each elementary field's path,
 * offset, length, kind (text, zoned, packed, binary) and digits after the point, with OCCURS repeated
 * and a REDEFINES alternative left out (the first definition is the one compared). A clause outside
 * this (OCCURS DEPENDING ON, SIGN SEPARATE, floating point, pointers) is refused, never guessed.
 */
// Unsure: R-053 default: the comparator reads copybooks itself until phase 2's front end and Semantic IR give the data map (HANDOFF "For Fable").
public final class Layout {
    /** How a field holds its value. */
    public enum Kind { TEXT, ZONED, PACKED, BINARY }

    /** An elementary field: its path (`ACCT-FIELDS.CLIENT-ADDR.CITY-COUNTY`, `(n)` for an occurrence), where it is and how it holds its value. */
    public record Field(String path, int offset, int length, Kind kind, boolean signed, int scale) {}

    /** A clause the reader does not handle, quoted. */
    public static final class Unsupported extends RuntimeException {
        Unsupported(String message) {
            super(message);
        }
    }

    private record Item(int level, String name, String picture, String usage, int occurs, boolean redefines, String text) {}

    private static final Pattern ITEM = Pattern.compile("^(\\d{1,2})\\s+([A-Z0-9-]+)(.*)$");
    private final List<Field> fields;
    private final int length;

    private Layout(List<Field> fields, int length) {
        this.fields = fields;
        this.length = length;
    }

    public List<Field> fields() {
        return fields;
    }

    public int length() {
        return length;
    }

    /** Reads fixed-form copybook text (columns 8 to 72; `*` and `/` in column 7 are comments; 88 levels skipped). */
    public static Layout read(String copybook) {
        List<Item> items = items(copybook);
        List<Field> fields = new ArrayList<>();
        int[] at = {0};
        int end = place(items, 0, "", 0, fields, at);
        if (end != items.size()) throw new Unsupported("more than one record at level 01 or a level out of order near " + items.get(end).text());
        return new Layout(fields, at[0]);
    }

    /** The statements: the text of columns 8 to 72 joined, split on periods, each an item. */
    private static List<Item> items(String copybook) {
        StringBuilder text = new StringBuilder();
        for (String line : copybook.split("\r?\n")) {
            if (line.length() < 7 || line.charAt(6) == '*' || line.charAt(6) == '/') continue;
            text.append(' ').append(line, 7, Math.min(line.length(), 72));
        }
        List<Item> items = new ArrayList<>();
        for (String sentence : text.toString().split("\\.(\\s|$)")) {
            String s = sentence.trim().replaceAll("\\s+", " ");
            if (s.isEmpty()) continue;
            Matcher m = ITEM.matcher(s);
            if (!m.matches()) throw new Unsupported("not a data description: " + s);
            int level = Integer.parseInt(m.group(1));
            if (level == 88) continue;
            if (level == 66 || level == 77) throw new Unsupported("level " + level + ": " + s);
            items.add(item(level, m.group(2), m.group(3).trim(), s));
        }
        return items;
    }

    /** One data description's clauses: its PICTURE, its USAGE, OCCURS and REDEFINES; the ones not decoded refused. */
    private static Item item(int level, String name, String clauses, String text) {
        Matcher pic = Pattern.compile("\\bPIC(?:TURE)?\\s+(?:IS\\s+)?(\\S+)").matcher(clauses);
        Matcher occurs = Pattern.compile("\\bOCCURS\\s+(\\d+)(\\s+TIMES)?").matcher(clauses);
        if (clauses.matches(".*\\bDEPENDING\\b.*")) throw new Unsupported("OCCURS DEPENDING ON: " + text);
        if (clauses.matches(".*\\bSIGN\\b.*\\bSEPARATE\\b.*")) throw new Unsupported("SIGN SEPARATE: " + text);
        if (clauses.matches(".*\\b(COMP-1|COMP-2|POINTER|INDEX|NATIONAL)\\b.*")) throw new Unsupported("a usage the comparator does not decode: " + text);
        String usage = clauses.matches(".*\\b(COMP-3|PACKED-DECIMAL|COMPUTATIONAL-3)\\b.*") ? "PACKED"
                : clauses.matches(".*\\b(COMP|COMP-4|COMP-5|BINARY|COMPUTATIONAL|COMPUTATIONAL-4|COMPUTATIONAL-5)\\b.*") ? "BINARY" : "DISPLAY";
        return new Item(level, name, pic.find() ? pic.group(1) : null, usage, occurs.find() ? Integer.parseInt(occurs.group(1)) : 1,
                clauses.matches(".*\\bREDEFINES\\b.*"), text);
    }

    /** Places the items from i under a group at this level and path; returns the index after them, offsets advanced in at. */
    private static int place(List<Item> items, int i, String path, int parentLevel, List<Field> fields, int[] at) {
        while (i < items.size() && items.get(i).level() > parentLevel) {
            Item item = items.get(i);
            int end = i + 1;
            while (end < items.size() && items.get(end).level() > item.level()) end++;
            List<Item> children = items.subList(i + 1, end);
            if (item.redefines()) {
                i = end;
                continue;
            }
            for (int n = 1; n <= item.occurs(); n++) {
                String name = (path.isEmpty() ? "" : path + ".") + item.name() + (item.occurs() > 1 ? "(" + n + ")" : "");
                if (item.picture() == null) place(children, 0, name, item.level(), fields, at);
                else fields.add(elementary(item, name, at));
            }
            i = end;
        }
        return i;
    }

    /** One elementary field from its PICTURE and USAGE, at at[0]. */
    private static Field elementary(Item item, String path, int[] at) {
        String pic = expand(item.picture());
        boolean numeric = pic.matches("S?9*V?9*") && pic.contains("9");
        boolean signed = pic.startsWith("S");
        int digits = pic.replaceAll("[^9]", "").length();
        int scale = pic.contains("V") ? pic.substring(pic.indexOf('V')).replaceAll("[^9]", "").length() : 0;
        Kind kind;
        int length;
        if (!numeric) {
            if (!item.usage().equals("DISPLAY")) throw new Unsupported("a non-numeric PICTURE with a computational usage: " + item.text());
            kind = Kind.TEXT;
            length = pic.replace("V", "").length();
        } else if (item.usage().equals("PACKED")) {
            kind = Kind.PACKED;
            length = digits / 2 + 1;
        } else if (item.usage().equals("BINARY")) {
            kind = Kind.BINARY;
            length = digits <= 4 ? 2 : digits <= 9 ? 4 : 8;
        } else {
            kind = Kind.ZONED;
            length = digits;
        }
        Field field = new Field(path, at[0], length, kind, signed, scale);
        at[0] += length;
        return field;
    }

    /** A PICTURE with its repeats written out: `S9(7)V99` is `S9999999V99`. */
    static String expand(String picture) {
        Matcher m = Pattern.compile("(.)\\((\\d+)\\)").matcher(picture);
        StringBuilder out = new StringBuilder();
        while (m.find()) m.appendReplacement(out, m.group(1).repeat(Integer.parseInt(m.group(2))));
        m.appendTail(out);
        return out.toString();
    }
}
