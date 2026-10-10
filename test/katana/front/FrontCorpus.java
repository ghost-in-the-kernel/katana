package katana.front;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Phase 2.1's check: CardDemo, the OMP course and every NIST program cobc accepts under COBOL 85 parse
 * to a tree with no error nodes, and BankDemo when it is fetched (privately: its count only, no names).
 * Phase 2.3's: a program is clean only if its source map round-trips (RoundTrip) as well.
 * front/corpus.golden holds each count and each failure's first error; a count may only rise
 * (`-Dkatana.record=true` rewrites the golden and refuses a fall). Run by `CorpusIntegrationTest`, which
 * parses each program once for this, the data map and the IR (J-169).
 */
public final class FrontCorpus {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));
    private static final Path GOLDEN = ROOT.resolve("front/corpus.golden");
    private static final String HEADER = """
            # Phase 2.1 (docs/ROADMAP.md): each corpus parsed by front/ (the forked ProLeap grammars), as
            # `corpus NAME clean/programs`, then each program that is not clean with its first error. A count may
            # only rise. Written by `gradle integration -Dkatana.record=true`; BankDemo's line is kept by hand.
            """;

    /**
     * The golden held: each corpus's programs and their errors (`errors`), CardDemo, OMP and NIST named,
     * BankDemo, when fetched, counted privately; a count may only rise.
     */
    public static void hold(Map<String, List<String>> carddemo, Map<String, List<String>> omp, Map<String, List<String>> nist, Map<String, List<String>> bankdemo) throws Exception {
        Map<String, String> lines = new LinkedHashMap<>();
        lines.putAll(report("carddemo", carddemo, true));
        lines.putAll(report("omp", omp, true));
        lines.putAll(report("nist", nist, true));
        String recorded = Files.exists(GOLDEN) ? Files.readString(GOLDEN) : HEADER;
        String bank = recorded.lines().filter(l -> l.startsWith("corpus bankdemo ")).findFirst().orElse(null);
        if (bankdemo != null) {
            Map<String, String> b = report("bankdemo", bankdemo, false);
            String now = b.keySet().iterator().next();
            System.out.println("front: " + now + " (privately)");
            if (bank != null) assertTrue(clean(now) >= clean(bank), bank + " then " + now);
            bank = now;
        }
        if (bank != null) lines.put(bank.strip() + "\n", "");
        String report = HEADER + lines.entrySet().stream().map(e -> e.getKey() + e.getValue()).collect(Collectors.joining()) ;
        for (String line : recorded.lines().filter(l -> l.startsWith("corpus ")).toList()) {
            String now = lines.keySet().stream().filter(k -> k.startsWith(line.substring(0, line.lastIndexOf(' ')))).findFirst().orElse(line);
            assertTrue(clean(now) >= clean(line), "a count fell: " + line + " then " + now);
        }
        if (Boolean.getBoolean("katana.record")) Files.writeString(GOLDEN, report);
        System.out.print(report);
        assertEquals(recorded, report);
    }

    /** CUSTREC.cpy was written with 4-column tabs (R-016), as the CardDemo fixtures declare. */
    public static int tabs(String name) {
        return name.equalsIgnoreCase("CUSTREC.cpy") ? 4 : 8;
    }

    /** A program's errors: its parse's, and the source map's if it does not round-trip. */
    public static List<String> errors(Parse.Result result) {
        List<String> errors = new ArrayList<>(result.errors());
        List<String> map = new RoundTrip(FrontCorpus::tabs).check(result);
        if (!map.isEmpty()) errors.add("the source map does not round-trip: " + map.get(0));
        return errors;
    }

    private static Map<String, String> report(String name, Map<String, List<String>> results, boolean named) {
        long clean = results.values().stream().filter(List::isEmpty).count();
        StringBuilder failures = new StringBuilder();
        if (named) {
            results.forEach((program, r) -> {
                if (!r.isEmpty()) failures.append("fail ").append(name).append(' ').append(program).append(": ").append(first(r.get(0))).append('\n');
            });
        }
        Map<String, String> out = new LinkedHashMap<>();
        out.put("corpus " + name + " " + clean + "/" + results.size() + "\n", failures.toString());
        return out;
    }

    private static int clean(String countLine) {
        return Integer.parseInt(countLine.trim().split(" ")[2].split("/")[0]);
    }

    /** An error up to ANTLR's list of what it expected. */
    private static String first(String error) {
        int expecting = error.indexOf(" expecting");
        return expecting < 0 ? error : error.substring(0, expecting);
    }
}
