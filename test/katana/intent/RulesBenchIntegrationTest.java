package katana.intent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.ToIntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.intent.rules.Rule;
import katana.intent.rules.Rules;
import katana.intent.types.Fact;
import katana.intent.types.Subject;
import katana.profile.Profile;
import katana.sir.build.Build;
import katana.sir.types.Program;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * 5.2's bench (research §29): each rule's facts on every CardDemo program, against the hand-written key
 * (`carddemo.key`), within the key's scope for its kind; precision and recall recorded in
 * intent/rules/carddemo.scores, with each fact the key does not hold and each it holds that the rule missed.
 * A rule that lowers a recorded score is refused, and none claims more confidence than its precision.
 */
@Tag("integration")
class RulesBenchIntegrationTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));
    private static final Path CARDDEMO = ROOT.resolve("corpus/carddemo/app");
    private static final Path SCORES = ROOT.resolve("intent/rules/carddemo.scores");
    private static final Pattern SCORE = Pattern.compile("(?m)^rule (\\S+) .* precision (\\S+) recall (\\S+)$");

    /** The key's lines, `kind PROGRAM name`. */
    private static Set<String> key() throws Exception {
        Set<String> key = new TreeSet<>();
        for (String line : Files.readAllLines(ROOT.resolve("test/katana/intent/carddemo.key"))) if (!line.isBlank() && !line.startsWith("#")) key.add(line.strip());
        return key;
    }

    /** Whether a fact of this kind on this program is in the key's scope: enum and money everywhere, function in the batch programs. */
    private static boolean inScope(String kind, String program) {
        return !kind.equals("function") || program.startsWith("CB") || program.equals("CSUTLDTC") || program.equals("COBSWAIT");
    }

    /** The CREASTMT fixture's tab widths (CUSTREC.cpy at 4, R-016), 8 for every other copybook. */
    private static ToIntFunction<String> tabs() throws Exception {
        Map<String, Integer> widths = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (String line : Files.readAllLines(ROOT.resolve("oracle/fixture/carddemo/CREASTMT.fixture"))) {
            String[] w = line.strip().split("\\s+");
            if (w.length == 3 && w[0].equals("tabs")) widths.put(w[2].replaceFirst("(?i)\\.cpy$", ""), Integer.parseInt(w[1]));
        }
        return n -> widths.getOrDefault(n.replaceFirst("(?i)\\.cpy$", ""), 8);
    }

    /** A fact as the key writes it. */
    private static String line(Program p, Fact f) {
        String kind = f.kind().name().toLowerCase(java.util.Locale.ROOT), program = p.header().program();
        return switch (f.subject()) {
            case Subject.Datum d -> kind + " " + program + " " + p.items().get(d.item() - 1).name();
            case Subject.Choice c -> kind + " " + program + " " + p.items().get(c.item() - 1).name();
            case Subject.Range r -> kind + " " + program + " " + paragraph(p, r.first()) + (r.last() == r.first() ? "" : " THRU " + paragraph(p, r.last()));
            default -> kind + " " + program + " " + f.subject().text();
        };
    }

    private static String paragraph(Program p, int index) {
        return p.procedure().paragraphs().get(index).name();
    }

    @Test
    void eachRuleOnCardDemoAgainstTheKey() throws Exception {
        Profile ibm = Profile.read(Files.readString(ROOT.resolve("profile/ibm.profile")));
        Copybooks books = new Copybooks(List.of(CARDDEMO.resolve("cpy"), CARDDEMO.resolve("cpy-bms"), ROOT.resolve("front/ibm")), tabs());
        Map<String, Set<String>> inferred = new TreeMap<>();
        for (Rule r : Rules.ALL) inferred.put(r.name(), new TreeSet<>());
        for (Path source : katana.sir.IrCorpus.programs(CARDDEMO.resolve("cbl")).values()) {
            for (Build.Result built : Build.programs(Parse.file(source, books, 8), ibm.hash())) {
                Program p = built.program();
                for (Fact f : Rules.infer(p).facts()) {
                    String line = line(p, f);
                    if (inScope(line.split(" ")[0], p.header().program())) inferred.get(f.evidence().rule()).add(line);
                }
            }
        }
        Set<String> key = key();
        StringBuilder report = new StringBuilder("# 5.2's bench (research §29): each rule on CardDemo against test/katana/intent/carddemo.key, within its scope.\n"
                + "# `rule NAME vVERSION facts F true T key K precision P recall R`, then each fact the key does not hold (`not`) and\n"
                + "# each the key holds that the rule missed (`missed`). Written by `gradle integration -Dkatana.record=true`; a lower score is refused.\n");
        List<String> overclaimed = new ArrayList<>();
        for (Rule r : Rules.ALL) report.append(score(r, inferred.get(r.name()), key, overclaimed));
        hold(report.toString());
        assertEquals(List.of(), overclaimed, "a rule's confidence is at most its precision");
    }

    /** One rule's line and its errors. */
    private static String score(Rule r, Set<String> facts, Set<String> key, List<String> overclaimed) {
        String kind = kind(r);
        Set<String> wanted = new TreeSet<>(key.stream().filter(k -> k.startsWith(kind + " ")).toList());
        Set<String> right = new TreeSet<>(facts);
        right.retainAll(wanted);
        String precision = facts.isEmpty() ? "none" : String.format("%.3f", right.size() / (double) facts.size());
        String recall = wanted.isEmpty() ? "none" : String.format("%.3f", right.size() / (double) wanted.size());
        if (!facts.isEmpty() && r.confidence() > Double.parseDouble(precision)) overclaimed.add(r.name() + " claims " + r.confidence() + " over its precision " + precision);
        StringBuilder out = new StringBuilder("rule " + r.name() + " v" + r.version() + " facts " + facts.size() + " true " + right.size() + " key " + wanted.size() + " precision " + precision + " recall " + recall + "\n");
        for (String f : facts) if (!wanted.contains(f)) out.append("  not ").append(f).append('\n');
        for (String w : wanted) if (!facts.contains(w)) out.append("  missed ").append(w).append('\n');
        return out.toString();
    }

    private static String kind(Rule r) {
        return r.name().substring(0, r.name().indexOf('.'));
    }

    /** The scores held: equal to those recorded, and recorded only when no precision or recall falls. */
    private static void hold(String report) throws Exception {
        String recorded = Files.exists(SCORES) ? Files.readString(SCORES) : "";
        System.out.print(report);
        if (Boolean.getBoolean("katana.record")) {
            List<String> lower = lower(recorded, report);
            assertEquals(List.of(), lower, "a rule that lowers its recorded score is refused (docs/ROADMAP.md 5.2)");
            Files.writeString(SCORES, report);
            return;
        }
        assertEquals(recorded, report);
    }

    /** Each recorded precision or recall the report falls below, a rule's versions compared by the kind they infer, whatever their names (R-065). */
    static List<String> lower(String recorded, String report) {
        Map<String, String[]> before = new TreeMap<>();
        Matcher m = SCORE.matcher(recorded);
        while (m.find()) before.put(kindOf(m.group(1)), new String[] {m.group(2), m.group(3)});
        List<String> lower = new ArrayList<>();
        m = SCORE.matcher(report);
        while (m.find()) {
            String[] was = before.get(kindOf(m.group(1)));
            if (was == null) continue;
            if (below(m.group(2), was[0])) lower.add(m.group(1) + " precision " + was[0] + " to " + m.group(2));
            if (below(m.group(3), was[1])) lower.add(m.group(1) + " recall " + was[1] + " to " + m.group(3));
        }
        return lower;
    }

    private static String kindOf(String rule) {
        return rule.substring(0, rule.indexOf('.'));
    }

    private static boolean below(String now, String was) {
        if (was.equals("none")) return false;
        return now.equals("none") || Double.parseDouble(now) < Double.parseDouble(was);
    }
}
