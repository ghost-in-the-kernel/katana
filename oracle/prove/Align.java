package katana.oracle.prove;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import katana.sir.types.Item;
import katana.sir.types.Procedure;
import katana.sir.types.Program;
import katana.sir.types.Statement;
import katana.sir.types.Value;

/**
 * 8A.4's alignment (research §51, R-072): each COBOL node, by the program's own names (R-064), mapped to the place
 * in a foreign port's Java that does it, inferred by the strongest evidence found (`source`, a comment naming the
 * source line; `name`, a paragraph's or item's name in any spelling; `literal`, a statement's literal found once)
 * and `unconfirmed` until a person says otherwise (R-029). Read back, a line naming a node the IR does not hold is
 * refused.
 */
public final class Align {
    /** One line of `<PROGRAM>.align`. */
    public record Line(String node, String place, String state, String evidence) {
        String text() {
            return node + " | " + place + " | " + state + " | " + evidence;
        }
    }

    /** The lines inferred, and how many nodes of each kind there are and how many were aligned. */
    public record Result(List<Line> lines, Map<String, int[]> coverage) {}

    public static final Set<String> STATES = Set.of("unconfirmed", "confirmed", "refuted");
    private static final Pattern LITERAL = Pattern.compile("\"((?:\\\\.|[^\"\\\\])*)\"|(?<![\\w.])(\\d+(?:\\.\\d+)?)");

    private Align() {}

    /** Every node of the program by its name: paragraphs, statements, items. */
    public static Map<String, Object> nodes(Program p) {
        Map<String, Object> out = new LinkedHashMap<>();
        Map<String, Integer> names = new LinkedHashMap<>();
        for (Procedure.Paragraph g : p.procedure().paragraphs()) if (!g.name().isEmpty()) names.merge(g.name(), 1, Integer::sum);
        for (Procedure.Paragraph g : p.procedure().paragraphs()) {
            String name = paragraph(g, names);
            if (name != null) out.put("paragraph " + name, g);
            Map<String, Integer> k = new LinkedHashMap<>();
            for (Statement s : flat(g.statements())) out.put("statement " + s.verb() + " #" + k.merge(s.verb(), 1, Integer::sum) + " IN " + (name != null ? name : g.section() != null && g.index() > 0 ? "SECTION " + g.section() : "PROCEDURE DIVISION"), s);
        }
        for (Item i : p.items()) if (!i.name().equals("FILLER") && i.level() != 66) out.put("item " + item(p, i), i);
        return out;
    }

    private static String paragraph(Procedure.Paragraph g, Map<String, Integer> names) {
        if (g.name().isEmpty()) return null;
        return names.get(g.name()) > 1 && g.section() != null ? g.name() + " IN " + g.section() : g.name();
    }

    /** An item's name, qualified OF its parents as far as makes it unique (R-064). */
    private static String item(Program p, Item i) {
        String name = i.name();
        for (Item a = i; count(p, name) > 1 && a.parent() != 0; ) {
            a = p.items().get(a.parent() - 1);
            name = name + " OF " + a.name();
        }
        return name;
    }

    private static long count(Program p, String qualified) {
        return p.items().stream().filter(i -> qualifiedMatches(p, i, qualified)).count();
    }

    /** Whether an item is the one a qualified name (`ACCT-ID OF ACCOUNT-RECORD`) names: its own name, then each qualifier among its parents in order. */
    private static boolean qualifiedMatches(Program p, Item i, String qualified) {
        String[] parts = qualified.split(" OF ");
        if (!i.name().equals(parts[0])) return false;
        Item a = i;
        for (int k = 1; k < parts.length; k++) {
            do a = a.parent() == 0 ? null : p.items().get(a.parent() - 1); while (a != null && !a.name().equals(parts[k]));
            if (a == null) return false;
        }
        return true;
    }

    /** A paragraph's statements and their branches' statements, in order. */
    private static List<Statement> flat(List<Statement> statements) {
        List<Statement> out = new ArrayList<>();
        for (Statement s : statements) {
            out.add(s);
            for (Statement.Branch b : s.branches()) out.addAll(flat(b.statements()));
        }
        return out;
    }

    /** The alignment of a port's Java file onto the program: every node it finds evidence for, `unconfirmed`. */
    // Decision: R-072
    public static Result infer(Program p, String javaFile, String java) {
        String[] lines = java.split("\n", -1);
        List<Line> out = new ArrayList<>();
        Map<String, int[]> coverage = new LinkedHashMap<>(Map.of("paragraph", new int[2], "statement", new int[2], "item", new int[2], "referenced item", new int[2]));
        Set<Integer> referenced = referenced(p);
        String[] code = new String[lines.length];
        // A name inside a string literal is text the port prints, not the port's name for the item (J-234: ACCRUED).
        for (int k = 0; k < lines.length; k++) code[k] = lines[k].replaceAll("\"(?:\\\\.|[^\"\\\\])*\"", "\"\"");
        Map<String, Integer> literals = literals(lines);
        for (Map.Entry<String, Object> n : nodes(p).entrySet()) {
            String kind = n.getKey().substring(0, n.getKey().indexOf(' '));
            coverage.get(kind)[0]++;
            Line l = switch (n.getValue()) {
                case Procedure.Paragraph g -> byName(n.getKey(), g.name(), javaFile, code);
                case Statement s -> statement(n.getKey(), s, javaFile, lines, literals);
                case Item i -> byName(n.getKey(), i.name(), javaFile, code);
                default -> null;
            };
            boolean used = n.getValue() instanceof Item i && referenced.contains(i.id());
            if (used) coverage.get("referenced item")[0]++;
            if (l == null) continue;
            out.add(l);
            coverage.get(kind)[1]++;
            if (used) coverage.get("referenced item")[1]++;
        }
        return new Result(paragraphRanges(out, lines.length, javaFile), coverage);
    }

    /** The items a statement names, its subscripts and expressions' too: what a port must hold; an item nothing names may have no Java at all. */
    private static Set<Integer> referenced(Program p) {
        Set<Integer> out = new java.util.HashSet<>();
        for (Procedure.Paragraph g : p.procedure().paragraphs()) for (Statement s : flat(g.statements())) {
            for (Statement.Operand o : s.operands()) items(o.value(), out);
            for (Statement.Branch b : s.branches()) items(b.condition(), out);
        }
        return out;
    }

    private static void items(Value v, Set<Integer> out) {
        switch (v) {
            case Value.Field f -> {
                out.add(f.item());
                f.subscripts().forEach(x -> items(x, out));
            }
            case Value.ConditionName c -> out.add(c.item());
            case Value.Expression e -> e.operands().forEach(x -> items(x, out));
            case null, default -> {}
        }
    }

    /** A statement by the comment naming its source line, else by a literal of its own found at one place only. */
    private static Line statement(String node, Statement s, String javaFile, String[] lines, Map<String, Integer> literals) {
        String source = s.origin().file().replaceAll(".*/", "") + ":" + s.origin().line() + ":";
        for (int k = 0; k < lines.length; k++) if (lines[k].contains(source)) return new Line(node, javaFile + ":" + (k + 2), "unconfirmed", "source " + source.substring(0, source.length() - 1));
        List<Value.Literal> own = new ArrayList<>();
        for (Statement.Operand o : s.operands()) literalsOf(o.value(), own);
        for (Value.Literal l : own) {
            String text = l.kind() == Value.Literal.Kind.NUMERIC ? digits(l.payload()) : l.payload();
            if (text.isBlank() || text.length() < 2) continue;
            Integer at = literals.get(text);
            if (at != null && at >= 0) return new Line(node, javaFile + ":" + (at + 1), "unconfirmed", "literal " + text);
        }
        return null;
    }

    /** The literals a value holds, an expression's operands' too. */
    private static void literalsOf(Value v, List<Value.Literal> out) {
        if (v instanceof Value.Literal l) out.add(l);
        if (v instanceof Value.Expression e) for (Value x : e.operands()) literalsOf(x, out);
    }

    /** Each literal in the Java, by its text, at the one line it is on (-1 when it is on more than one). */
    private static Map<String, Integer> literals(String[] lines) {
        Map<String, Integer> out = new LinkedHashMap<>();
        for (int k = 0; k < lines.length; k++) {
            Matcher m = LITERAL.matcher(lines[k].replaceAll("//.*|/\\*.*?\\*/", ""));
            while (m.find()) {
                String text = m.group(1) != null ? m.group(1) : digits(m.group(2));
                int line = k;
                out.merge(text, line, (a, b) -> a == b ? a : -1);
                if (m.group(1) != null) out.merge(text.strip(), line, (a, b) -> a == b ? a : -1);
                // A number written as a string, `new BigDecimal("0.0375")`, by its digits too.
                if (m.group(1) != null && m.group(1).matches("-?\\d+(\\.\\d+)?")) out.merge(digits(m.group(1)), line, (a, b) -> a == b ? a : -1);
            }
        }
        return out;
    }

    /** A number's digits, its point and leading zeros dropped: `0.0375` and the COBOL literal's payload alike. */
    private static String digits(String number) {
        String d = number.replace(".", "").replace("-", "").replaceFirst("^0+(?=\\d)", "");
        return d;
    }

    /** A paragraph or an item by its name in a comment or an identifier, in any spelling; its first such line. */
    private static Line byName(String node, String name, String javaFile, String[] lines) {
        Pattern spelled = spellings(name);
        for (int k = 0; k < lines.length; k++) if (spelled.matcher(lines[k]).find()) return new Line(node, javaFile + ":" + (k + 1), "unconfirmed", "name " + name);
        return null;
    }

    /** `ACCT-CURR-BAL` as the COBOL name in a comment, `ACCT_CURR_BAL` with a prefix or qualifier, or `acctCurrBal`. */
    static Pattern spellings(String name) {
        String[] words = name.toLowerCase().split("-");
        StringBuilder camel = new StringBuilder(words[0]);
        for (int k = 1; k < words.length; k++) camel.append(words[k].isEmpty() ? "" : Character.toUpperCase(words[k].charAt(0)) + words[k].substring(1));
        String upper = Pattern.quote(name.toUpperCase().replace('-', '_'));
        return Pattern.compile("/\\*\\s*" + Pattern.quote(name) + "\\s*\\*/|(?<![A-Za-z0-9])(?:[a-z]_)?(?:\\w+__)?" + upper + "(?:__\\w+)?(?![A-Za-z0-9])|(?<![A-Za-z0-9_])" + Pattern.quote(camel.toString()) + "(?![A-Za-z0-9_])");
    }

    /** Each paragraph's place from its line to the line before the next aligned paragraph's. */
    private static List<Line> paragraphRanges(List<Line> lines, int last, String javaFile) {
        List<Integer> starts = new ArrayList<>();
        for (Line l : lines) if (l.node().startsWith("paragraph ")) starts.add(line(l));
        starts.sort(null);
        List<Line> out = new ArrayList<>();
        for (Line l : lines) {
            if (!l.node().startsWith("paragraph ")) {
                out.add(l);
                continue;
            }
            int from = line(l), next = starts.stream().filter(x -> x > from).findFirst().orElse(last + 1);
            out.add(new Line(l.node(), javaFile + ":" + from + "-" + (next - 1), l.state(), l.evidence()));
        }
        return out;
    }

    private static int line(Line l) {
        return Integer.parseInt(l.place().replaceAll(".*:(\\d+).*", "$1"));
    }

    /** The file: a header, then a line each. */
    public static String write(String program, List<Line> lines) {
        StringBuilder b = new StringBuilder("# " + program + ".align: COBOL node | Java place | state | evidence (research §51; a person sets the state)\n");
        for (Line l : lines) b.append(l.text()).append('\n');
        return b.toString();
    }

    /** The file read against the program: each line refused with why when its node is not the program's or its state is not one of the three. */
    public static List<Line> read(String text, Program p, List<String> refused) {
        Set<String> known = nodes(p).keySet();
        List<Line> out = new ArrayList<>();
        for (String raw : text.split("\n")) {
            if (raw.isBlank() || raw.startsWith("#")) continue;
            String[] f = raw.split(" \\| ", -1);
            if (f.length != 4) refused.add("not four fields: " + raw);
            else if (!known.contains(f[0])) refused.add("names " + f[0] + ", which the program does not hold");
            else if (!STATES.contains(f[2])) refused.add("state " + f[2] + " of " + f[0] + " is not unconfirmed, confirmed or refuted");
            else out.add(new Line(f[0], f[1], f[2], f[3]));
        }
        return out;
    }
}
