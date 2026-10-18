package katana.intent;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import katana.sir.types.Program;

/**
 * T-F2 (research §33, §34): a `function` fact read on cobc's own cross-reference listing (`-Xref`), not on
 * sir/'s graph: every reference to the range's first paragraph is a PERFORM of the whole range, no reference
 * from outside reaches a paragraph inside it but that PERFORM's THRU, no GO TO inside it names a paragraph
 * outside, no ALTER touches it, and the paragraphs cobc lists between its ends are the IR's.
 */
final class FunctionRangeXref {
    /** A label cobc lists: its name, the line it is defined on, and the lines that reference it. */
    record Label(String name, int defined, List<Integer> references) {}

    /** How a reference names its label. */
    enum Kind { PERFORM, THRU, GO_TO, ALTER, OTHER }

    /** A listing: the procedure division's numbered source lines, and the labels by name. */
    record Listing(TreeMap<Integer, String> lines, Map<String, Label> labels, int counted) {}

    private static final Pattern NUMBERED = Pattern.compile("^(\\d{6})(.*)$"), LABEL = Pattern.compile("^([PS]) (\\S+)\\s+(\\d+)((?:\\s+\\d+)*)(?:\\s+x(\\d+))?\\s*$"),
            MORE = Pattern.compile("^\\s{20,}((?:\\d+\\s+)*\\d*)\\s*(?:x(\\d+))?\\s*$");

    private FunctionRangeXref() {}

    /** cobc's listing of a CardDemo program, `-Xref` under `-std=ibm`, with CUSTREC.cpy's 4-column tabs expanded as its fixture says (R-016). */
    static String listing(Path source) throws Exception {
        Path work = Files.createTempDirectory("katana-xref"), books = Files.createDirectories(work.resolve("cpy"));
        try (var files = Files.list(BatchReview.CARDDEMO.resolve("cpy"))) {
            for (Path f : files.toList()) {
                String text = Files.readString(f, StandardCharsets.ISO_8859_1);
                Files.writeString(books.resolve(f.getFileName()), f.getFileName().toString().toUpperCase().startsWith("CUSTREC") ? untab(text, 4) : text, StandardCharsets.ISO_8859_1);
            }
        }
        Path out = work.resolve("x.lst");
        Process run = new ProcessBuilder("cobc", "-std=ibm", "-fsyntax-only", "-I", books.toString(), "-I", BatchReview.CARDDEMO.resolve("cpy-bms").toString(), "-t", out.toString(), "-Xref", source.toAbsolutePath().toString())
                .directory(work.toFile()).redirectErrorStream(true).redirectOutput(work.resolve("cobc.out").toFile()).start();
        if (!run.waitFor(60, java.util.concurrent.TimeUnit.SECONDS)) run.destroyForcibly();
        if (!Files.exists(out)) throw new IllegalStateException("cobc wrote no listing for " + source + ": " + Files.readString(work.resolve("cobc.out")));
        return Files.readString(out, StandardCharsets.ISO_8859_1);
    }

    private static String untab(String text, int width) {
        StringBuilder out = new StringBuilder();
        int column = 0;
        for (char c : text.toCharArray()) {
            if (c == '\t') {
                do out.append(' '); while (++column % width != 0);
            } else {
                out.append(c);
                column = c == '\n' ? 0 : column + 1;
            }
        }
        return out.toString();
    }

    /** The listing's procedure-division lines (from PROCEDURE DIVISION on, where a copybook's own numbering no longer interleaves) and its LABEL table. */
    static Listing parse(String listing) {
        TreeMap<Integer, String> lines = new TreeMap<>();
        Map<String, Label> labels = new LinkedHashMap<>();
        boolean procedure = false, table = false;
        Label last = null;
        int counted = 0;
        for (String line : listing.split("\r?\n")) {
            Matcher n = NUMBERED.matcher(line);
            if (n.matches() && !table) {
                procedure |= code(n.group(2)).toUpperCase().matches("\\s*PROCEDURE\\s+DIVISION.*");
                if (procedure) lines.put(Integer.parseInt(n.group(1)), n.group(2));
                continue;
            }
            if (line.startsWith("LABEL ") && line.contains("DEFINED")) table = true;
            if (!table) continue;
            Matcher l = LABEL.matcher(line), m = MORE.matcher(line);
            if (l.matches()) {
                last = new Label(l.group(2), Integer.parseInt(l.group(3)), new ArrayList<>(numbers(l.group(4))));
                labels.put(last.name(), last);
                if (l.group(5) != null) counted += Integer.parseInt(l.group(5));
            } else if (!line.isBlank() && m.matches() && last != null) {
                last.references().addAll(numbers(m.group(1)));
                if (m.group(2) != null) counted += Integer.parseInt(m.group(2));
            } else if (line.startsWith("FUNCTION ")) {
                table = false;
            }
        }
        return new Listing(lines, labels, counted);
    }

    private static List<Integer> numbers(String s) {
        List<Integer> out = new ArrayList<>();
        for (String w : s.trim().split("\\s+")) if (!w.isEmpty()) out.add(Integer.parseInt(w));
        return out;
    }

    /** How the reference at a line names the label: the word before it, across a line break, over a GO TO DEPENDING's other labels. */
    static Kind kind(Listing l, String label, int line) {
        String text = code(l, line);
        int at = word(text, label);
        String before = at < 0 ? "" : text.substring(0, at);
        List<String> words = words(l, before);
        for (Integer up = l.lines().lowerKey(line); words.isEmpty() && up != null && up > line - 4; up = l.lines().lowerKey(up)) {
            before = code(l, up) + " " + before;
            words = words(l, before);
        }
        String w = words.isEmpty() ? "" : words.get(words.size() - 1), v = words.size() < 2 ? "" : words.get(words.size() - 2);
        if (w.equals("PERFORM")) return Kind.PERFORM;
        if (w.equals("THRU") || w.equals("THROUGH")) return Kind.THRU;
        if (w.equals("TO") && v.equals("GO") || w.equals("GO")) return Kind.GO_TO;
        if (w.equals("ALTER") || w.equals("TO") && v.equals("PROCEED")) return Kind.ALTER;
        return Kind.OTHER;
    }

    /** A text's words, upper case, without the labels at its end (a GO TO DEPENDING's list). */
    private static List<String> words(Listing l, String text) {
        List<String> words = new ArrayList<>();
        for (String w : text.trim().toUpperCase().split("\\s+")) if (!w.isEmpty()) words.add(w.replaceAll("[.,]$", ""));
        while (!words.isEmpty() && label(l, words.get(words.size() - 1)) != null) words.remove(words.size() - 1);
        return words;
    }

    /** What follows a PERFORM's first target: the THRU's label, or "" for none. */
    static String thru(Listing l, String first, int line) {
        String text = code(l, line);
        int at = word(text, first);
        String after = (at < 0 ? "" : text.substring(at + first.length())) + " " + (l.lines().higherKey(line) == null ? "" : code(l, l.lines().higherKey(line)));
        Matcher m = Pattern.compile("^\\s*(?:THRU|THROUGH)\\s+([A-Z0-9-]+)", Pattern.CASE_INSENSITIVE).matcher(after);
        return m.find() ? m.group(1).toUpperCase() : "";
    }

    /** A line's code: columns 8 to 72, and nothing of a comment line. */
    private static String code(Listing l, int line) {
        return code(l.lines().getOrDefault(line, ""));
    }

    private static String code(String t) {
        String src = t.length() > 2 ? t.substring(2) : "";
        if (src.length() > 6 && (src.charAt(6) == '*' || src.charAt(6) == '/')) return "";
        return src.length() > 7 ? src.substring(7, Math.min(src.length(), 72)) : "";
    }

    /** cobc's table cuts a name at its column's width; a name of that width may go on in the source. */
    private static final int WIDTH = 28;

    private static int word(String text, String label) {
        Matcher m = Pattern.compile("(?<![A-Z0-9-])" + Pattern.quote(label) + (label.length() >= WIDTH ? "[A-Z0-9-]*" : "") + "(?![A-Z0-9-])", Pattern.CASE_INSENSITIVE).matcher(text);
        return m.find() ? m.start() : -1;
    }

    /** The range on the listing: its first and last labels, the last line it holds, and the labels cobc lists inside it. */
    private record Span(Label first, Label last, int end, List<String> inside) {
        boolean holds(int line) {
            return line >= first.defined() && line <= end;
        }
    }

    /** Why the range is not a function on cobc's listing; empty when it is. */
    static List<String> refusals(Listing l, Program p, FunctionRangeStructure.Range r) {
        String first = FunctionRangeStructure.name(p, r.first()), last = FunctionRangeStructure.name(p, r.last());
        Label f = label(l, first), z = label(l, last);
        if (f == null || z == null) return List.of("cobc lists no label " + (f == null ? first : last));
        int end = l.labels().values().stream().mapToInt(Label::defined).filter(d -> d > z.defined()).min().orElse(Integer.MAX_VALUE) - 1;
        Span span = new Span(f, z, end, l.labels().values().stream().filter(b -> b.defined() >= f.defined() && b.defined() <= end).map(Label::name).toList());
        List<String> why = new ArrayList<>();
        List<String> ir = new ArrayList<>();
        for (int g = r.first(); g <= r.last(); g++) ir.add(FunctionRangeStructure.name(p, g));
        boolean same = span.inside().size() == ir.size();
        for (int i = 0; same && i < ir.size(); i++) same = same(span.inside().get(i), ir.get(i));
        if (!same) why.add("cobc lists " + span.inside() + " between its ends, the IR " + ir);
        for (Label b : l.labels().values()) for (int ref : b.references()) reference(l, span, first, last, b, ref, why);
        return why;
    }

    /** One reference to a label against the range: an ALTER of it, an entry to it but by PERFORM of the whole, a GO TO out of it. */
    private static void reference(Listing l, Span span, String first, String last, Label b, int ref, List<String> why) {
        boolean in = span.inside().contains(b.name()), from = span.holds(ref);
        Kind k = kind(l, b.name(), ref);
        if (k == Kind.ALTER && in) why.add("an ALTER at line " + ref + " names " + b.name());
        if (in && b == span.first() && !from && !whole(l, k, first, last, ref)) why.add("line " + ref + " names " + first + " otherwise than PERFORM " + first + (first.equals(last) ? "" : " THRU " + last) + " (" + k + ")");
        if (in && b != span.first() && !from && !(k == Kind.THRU && b == span.last() && closes(l, first, ref))) why.add("line " + ref + ", outside it, names " + b.name() + " inside it (" + k + ")");
        if (!in && from && k == Kind.GO_TO) why.add("a GO TO at line " + ref + ", inside it, names " + b.name() + " outside it");
    }

    /** The label cobc lists for a paragraph of the IR, its name cut at the table's width. */
    private static Label label(Listing l, String name) {
        for (Label b : l.labels().values()) if (same(b.name(), name)) return b;
        return null;
    }

    private static boolean same(String table, String name) {
        return table.equals(name) || table.length() >= WIDTH && name.startsWith(table);
    }

    /** A PERFORM of the whole range: its first, then THRU its last, or no THRU (or THRU itself) for a range of one. */
    private static boolean whole(Listing l, Kind k, String first, String last, int ref) {
        String thru = thru(l, first, ref);
        return k == Kind.PERFORM && (first.equals(last) ? thru.isEmpty() || thru.equals(first) : thru.equals(last));
    }

    /** A THRU of the range's last that closes a PERFORM of its first, on the same line or the one before. */
    private static boolean closes(Listing l, String first, int ref) {
        Integer previous = l.lines().lowerKey(ref);
        return word(code(l, ref), first) >= 0 || previous != null && word(code(l, previous), first) >= 0;
    }

    /** The PERFORM and GO TO references cobc lists: the guard that the test read the listing at all, held to the IR's edges. */
    static int jumps(Listing l) {
        int n = 0;
        for (Label b : l.labels().values()) for (int ref : b.references()) if (kind(l, b.name(), ref) == Kind.PERFORM || kind(l, b.name(), ref) == Kind.GO_TO) n++;
        return n;
    }
}
