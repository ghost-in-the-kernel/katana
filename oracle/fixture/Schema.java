package katana.oracle.fixture;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * The fixture's line schema (docs/FIXTURE.md): each line's keyword, how many positional values it
 * takes, the attributes and bare words it may carry, and what each attribute's value must look like.
 * One table, so the document and the reader cannot drift (FixtureDocTest holds both).
 */
public final class Schema {
    /** One keyword: its positional values, the attributes it may carry, those it must, and its bare words. */
    public record Spec(int positional, Set<String> attributes, Set<String> required, Set<String> words) {}

    /** One line of a fixture, read: its number, keyword, positional values, attributes and bare words. */
    public record Line(int number, String keyword, List<String> values, Map<String, String> attributes, Set<String> words) {}

    public static final Map<String, Spec> SPECS = specs();
    public static final Map<String, Pattern> VALUES = values();
    static final String DSN = "[A-Z0-9#@$]{1,8}(\\.[A-Z0-9#@$]{1,8})*(\\((\\+\\d+|-\\d+|0|[A-Z0-9#@$]{1,8})\\))?";

    private Schema() {}

    /** The keywords, in the order docs/FIXTURE.md's lines table gives them. */
    private static Map<String, Spec> specs() {
        Map<String, Spec> s = new LinkedHashMap<>();
        s.put("job", new Spec(1, Set.of(), Set.of(), Set.of()));
        s.put("source", new Spec(1, Set.of(), Set.of(), Set.of()));
        s.put("profile", new Spec(2, Set.of(), Set.of(), Set.of()));
        s.put("clock", new Spec(-1, Set.of(), Set.of(), Set.of()));
        s.put("tz", new Spec(1, Set.of(), Set.of(), Set.of()));
        s.put("module", new Spec(1, Set.of(), Set.of(), Set.of()));
        s.put("stub", new Spec(1, Set.of(), Set.of(), Set.of()));
        s.put("tabs", new Spec(2, Set.of(), Set.of(), Set.of()));
        s.put("dfsort.pad", new Spec(1, Set.of(), Set.of(), Set.of()));
        s.put("dfsort.trunc", new Spec(1, Set.of(), Set.of(), Set.of()));
        s.put("dataset", new Spec(1, ordered("org", "lrecl", "keys", "over", "unique", "from", "framing", "sha256", "signs", "defined-by"),
                Set.of("org"), Set.of("empty")));
        s.put("gold", new Spec(1, Set.of("sha256"), Set.of("sha256"), Set.of()));
        s.put("step", new Spec(1, ordered("pgm", "parm", "cond"), Set.of("pgm"), Set.of()));
        s.put("dd", new Spec(1, ordered("dsn", "disp", "recfm", "lrecl", "copybook", "encoding"), Set.of(),
                ordered("sysout", "dummy", "inline", "concat")));
        return s;
    }

    /** Each attribute's value, in the order docs/FIXTURE.md's attribute table gives them. */
    private static Map<String, Pattern> values() {
        Map<String, Pattern> v = new LinkedHashMap<>();
        v.put("org", Pattern.compile("PS|KSDS|PATH|GDG"));
        v.put("lrecl", Pattern.compile("[1-9]\\d{0,4}"));
        v.put("keys", Pattern.compile("[1-9]\\d*,\\d+"));
        v.put("over", Pattern.compile(DSN));
        v.put("unique", Pattern.compile("yes|no"));
        v.put("from", Pattern.compile("(carddemo|repo):[\\w./-]+|job:[A-Z0-9#@$-]{1,20}(:" + DSN + ")?"));
        v.put("framing", Pattern.compile("lines|fixed"));
        v.put("sha256", Pattern.compile("[0-9a-f]{64}"));
        v.put("signs", Pattern.compile("[A-Z0-9#@$]{1,8}"));
        v.put("defined-by", Pattern.compile("[A-Z0-9#@$.]{1,17}"));
        v.put("pgm", Pattern.compile("[A-Z0-9#@$]{1,8}"));
        v.put("parm", Pattern.compile(".{0,100}"));
        v.put("cond", Pattern.compile("\\d{1,4},(GT|GE|EQ|NE|LT|LE)"));
        v.put("dsn", Pattern.compile(DSN));
        v.put("disp", Pattern.compile("(NEW|OLD|SHR|MOD)(,(CATLG|KEEP|DELETE|PASS|UNCATLG)){0,2}"));
        v.put("recfm", Pattern.compile("F|FB|FBA|V|VB|VBA|U"));
        v.put("copybook", Pattern.compile("[A-Z0-9#@$]{1,8}|-"));
        v.put("encoding", Pattern.compile("ascii|cp037|cp1140"));
        return v;
    }

    private static Set<String> ordered(String... names) {
        return new LinkedHashSet<>(List.of(names));
    }

    /** Reads one non-card line against the schema; what does not fit is added to refusals, never dropped. */
    static Line line(int number, String text, List<String> refusals) {
        List<String> tokens = tokens(text, number, refusals);
        String keyword = tokens.get(0);
        Spec spec = SPECS.get(keyword);
        if (spec == null) {
            refusals.add("line " + number + ": unknown keyword " + keyword);
            return new Line(number, keyword, List.of(), Map.of(), Set.of());
        }
        if (spec.positional() < 0) return new Line(number, keyword, List.of(text.trim().substring(keyword.length()).trim()), Map.of(), Set.of());
        List<String> values = new ArrayList<>();
        Map<String, String> attributes = new LinkedHashMap<>();
        Set<String> words = new LinkedHashSet<>();
        for (String token : tokens.subList(1, tokens.size())) {
            if (values.size() < spec.positional()) values.add(token);
            else attribute(number, keyword, token, spec, attributes, words, refusals);
        }
        if (values.size() < spec.positional()) refusals.add("line " + number + ": " + keyword + " takes " + spec.positional() + " value(s)");
        for (String name : spec.required()) if (!attributes.containsKey(name)) refusals.add("line " + number + ": " + keyword + " needs " + name + "=");
        return new Line(number, keyword, values, attributes, words);
    }

    /** One token after the positional values: a bare word the keyword allows, or name=value with a well-formed value, once. */
    private static void attribute(int number, String keyword, String token, Spec spec, Map<String, String> attributes,
                                  Set<String> words, List<String> refusals) {
        int eq = token.indexOf('=');
        String name = eq < 0 ? token : token.substring(0, eq);
        if (eq < 0 && spec.words().contains(name)) words.add(name);
        else if (eq < 0 || !spec.attributes().contains(name)) refusals.add("line " + number + ": " + keyword + " takes no " + name);
        else if (!VALUES.get(name).matcher(token.substring(eq + 1)).matches()) refusals.add("line " + number + ": bad " + token);
        else if (attributes.put(name, token.substring(eq + 1)) != null) refusals.add("line " + number + ": " + name + " twice");
    }

    /** Splits on spaces outside single quotes; a quoted value loses its quotes, '' inside it is one quote. */
    static List<String> tokens(String text, int number, List<String> refusals) {
        List<String> tokens = new ArrayList<>();
        StringBuilder token = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\'' && quoted && i + 1 < text.length() && text.charAt(i + 1) == '\'') token.append(text.charAt(++i));
            else if (c == '\'') quoted = !quoted;
            else if (c == ' ' && !quoted) flush(token, tokens);
            else token.append(c);
        }
        if (quoted) refusals.add("line " + number + ": an unclosed quote");
        flush(token, tokens);
        return tokens;
    }

    private static void flush(StringBuilder token, List<String> tokens) {
        if (!token.isEmpty()) tokens.add(token.toString());
        token.setLength(0);
    }
}
