package katana.front;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import katana.oracle.nist.NistSources;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Phase 2.1's check: CardDemo, the OMP course and every NIST program cobc accepts under COBOL 85 parse
 * to a tree with no error nodes, and BankDemo when it is fetched (privately: its count only, no names).
 * Phase 2.3's: a program is clean only if its source map round-trips (RoundTrip) as well.
 * front/corpus.golden holds each count and each failure's first error; a count may only rise
 * (`-Dkatana.record=true` rewrites the golden and refuses a fall).
 */
@Tag("integration")
class FrontCorpusIntegrationTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));
    private static final Path GOLDEN = ROOT.resolve("front/corpus.golden");
    private static final String HEADER = """
            # Phase 2.1 (docs/ROADMAP.md): each corpus parsed by front/ (the forked ProLeap grammars), as
            # `corpus NAME clean/programs`, then each program that is not clean with its first error. A count may
            # only rise. Written by `gradle integration -Dkatana.record=true`; BankDemo's line is kept by hand.
            """;

    @Test
    void theCorpusParsesAndNoCountFalls() throws Exception {
        Map<String, String> lines = new LinkedHashMap<>();
        Path carddemo = ROOT.resolve("corpus/carddemo/app");
        lines.putAll(corpus("carddemo", carddemo.resolve("cbl"), programs(carddemo.resolve("cbl")),
                new Copybooks(List.of(carddemo.resolve("cpy"), carddemo.resolve("cpy-bms"), ROOT.resolve("front/ibm")), FrontCorpusIntegrationTest::tabs), true));
        Path omp = ROOT.resolve("corpus/omp-cobol-course");
        lines.putAll(corpus("omp", omp, programs(omp), new Copybooks(folders(omp), name -> 8), true));
        lines.putAll(nist());
        Path bankdemo = ROOT.resolve("corpus/bankdemo");
        String recorded = Files.exists(GOLDEN) ? Files.readString(GOLDEN) : HEADER;
        String bank = recorded.lines().filter(l -> l.startsWith("corpus bankdemo ")).findFirst().orElse(null);
        if (Files.isDirectory(bankdemo)) {
            Map<String, String> b = corpus("bankdemo", bankdemo, programs(bankdemo), new Copybooks(folders(bankdemo), name -> 8), false);
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
    private static int tabs(String name) {
        return name.equalsIgnoreCase("CUSTREC.cpy") ? 4 : 8;
    }

    /** `corpus NAME clean/total` and, when names may be shown, one line per program not clean. */
    private static Map<String, String> corpus(String name, Path root, Map<String, Path> programs, Copybooks books, boolean named) throws Exception {
        Map<String, List<String>> results = parseAll(programs, (path, text) -> Parse.text(text, path.toString(), books));
        return report(name, results, named);
    }

    private interface Parser {
        Parse.Result parse(Path path, String text);
    }

    /** Each program's errors only: the trees are dropped as they are made, or 560 of them fill the heap. */
    private static Map<String, List<String>> parseAll(Map<String, Path> programs, Parser parser) throws Exception {
        Map<String, Future<List<String>>> futures = new TreeMap<>();
        Map<String, List<String>> results = new TreeMap<>();
        try (ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())) {
            for (var p : programs.entrySet()) {
                futures.put(p.getKey(), pool.submit(() -> {
                    byte[] bytes = Tabs.expand(Files.readAllBytes(p.getValue()), 8);
                    Parse.Result result = parser.parse(p.getValue(), new String(bytes, StandardCharsets.ISO_8859_1));
                    List<String> errors = new ArrayList<>(result.errors());
                    List<String> map = new RoundTrip(FrontCorpusIntegrationTest::tabs).check(result);
                    if (!map.isEmpty()) errors.add("the source map does not round-trip: " + map.get(0));
                    return errors;
                }));
            }
            for (var f : futures.entrySet()) results.put(f.getKey(), f.getValue().get());
        }
        return results;
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

    /** NIST under COBOL 85 (NistSources), each program with its copy members beside it. */
    private static Map<String, String> nist() throws Exception {
        Map<String, Path> programs = NistSources.write(ROOT, Files.createDirectories(ROOT.resolve("build/front-nist")));
        Copybooks books = new Copybooks(List.of(programs.values().iterator().next().getParent()), name -> 8);
        return report("nist", parseAll(programs, (path, text) -> Parse.text(text, path.toString(), books)), true);
    }

    private static Map<String, Path> programs(Path root) throws Exception {
        try (Stream<Path> files = Files.walk(root)) {
            return files.filter(Files::isRegularFile).filter(p -> p.getFileName().toString().toLowerCase().matches(".*\\.(cbl|cob|cobol)"))
                    .collect(Collectors.toMap(p -> root.relativize(p).toString(), p -> p, (a, b) -> a, TreeMap::new));
        }
    }

    /** Every folder under the root that holds a copybook. */
    private static List<Path> folders(Path root) throws Exception {
        try (Stream<Path> files = Files.walk(root)) {
            List<Path> folders = new ArrayList<>(files.filter(p -> p.getFileName().toString().toLowerCase().matches(".*\\.(cpy|copy)")).map(Path::getParent).distinct().sorted().toList());
            folders.add(ROOT.resolve("front/ibm"));
            return folders;
        }
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
