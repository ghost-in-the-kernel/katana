package katana.front;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import katana.oracle.nist.NistSources;
import katana.sir.DataMapCorpus;
import katana.sir.IrCorpus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * The corpora parsed once for three checks (J-169): the front end's (FrontCorpus, front/corpus.golden),
 * the data map beside the layout `cobc -C` gives (DataMapCorpus, sir/datamap.golden) and the IR built and
 * round-tripped (IrCorpus, sir/ir.golden). Each program is parsed once, each check's part of it taken,
 * and the tree dropped (560 trees fill the heap); the programs run in parallel and each check sums its
 * parts in its own order, so every golden is what the three classes wrote apart. They parsed NIST three
 * times in three JVMs, about two whole-corpus parses more than this.
 */
@Tag("integration")
class CorpusIntegrationTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));

    /** One program's parts, one per check; `map` null when the corpus has no data map check or cobc refused it. */
    private record Parts(List<String> errors, IrCorpus.One ir, DataMapCorpus.One map) {}

    /** One corpus's programs and their parts, in the programs' order. */
    private record Corpus(Map<String, Path> programs, Map<String, Parts> parts) {}

    private static Map<String, Corpus> corpora;

    @Test
    void theCorpusParsesAndNoCountFalls() throws Exception {
        Map<String, Corpus> c = corpora();
        FrontCorpus.hold(errors(c.get("carddemo")), errors(c.get("omp")), errors(c.get("nist")), c.containsKey("bankdemo") ? errors(c.get("bankdemo")) : null);
    }

    @Test
    void theDataMapIsCobcsLayout() throws Exception {
        Map<String, Corpus> c = corpora();
        DataMapCorpus.hold(DataMapCorpus.corpus("carddemo", maps(c.get("carddemo"))) + DataMapCorpus.corpus("nist", maps(c.get("nist"))));
    }

    @Test
    void everyProgramBuildsAndRoundTrips() throws Exception {
        Map<String, Corpus> c = corpora();
        if (c.containsKey("bankdemo")) System.out.print("privately: " + IrCorpus.corpus("bankdemo", irs(c.get("bankdemo"))).lines().findFirst().orElse("") + "\n");
        IrCorpus.hold(IrCorpus.header() + IrCorpus.corpus("carddemo", irs(c.get("carddemo"))) + IrCorpus.corpus("nist", irs(c.get("nist"))) + IrCorpus.corpus("omp", irs(c.get("omp"))));
    }

    /** SIR-08 on the corpora: cobc prints each literal as its payload, the literals gathered as the IR was built. */
    @Test
    void cobcPrintsEachLiteralOfTheCorporaAsItsPayload() throws Exception {
        corpora();
        IrCorpus.literals();
    }

    /** The corpora, parsed once per JVM: CardDemo, NIST and OMP, and BankDemo when it is fetched. */
    private static synchronized Map<String, Corpus> corpora() throws Exception {
        if (corpora != null) return corpora;
        Map<String, Corpus> out = new java.util.LinkedHashMap<>();
        Path carddemo = ROOT.resolve("corpus/carddemo/app");
        List<Path> cdBooks = List.of(carddemo.resolve("cpy"), carddemo.resolve("cpy-bms"), ROOT.resolve("front/ibm"));
        out.put("carddemo", corpus("carddemo", IrCorpus.programs(carddemo.resolve("cbl")), cdBooks, carddemo.resolve("cbl")));
        Map<String, Path> nist = NistSources.write(ROOT, Files.createDirectories(ROOT.resolve("build/corpus-nist")));
        out.put("nist", corpus("nist", nist, List.of(nist.values().iterator().next().getParent()), null));
        Path omp = ROOT.resolve("corpus/omp-cobol-course");
        out.put("omp", corpus("omp", IrCorpus.programs(omp), IrCorpus.folders(omp), null));
        Path bankdemo = ROOT.resolve("corpus/bankdemo");
        if (Files.isDirectory(bankdemo)) out.put("bankdemo", corpus("bankdemo", IrCorpus.programs(bankdemo), IrCorpus.folders(bankdemo), null));
        corpora = out;
        return out;
    }

    /**
     * Each program of a corpus parsed once and its three parts taken. CardDemo's data map covers the
     * `.cbl` files directly in `cbl/` (the data map's set), NIST's covers every program; OMP and BankDemo
     * have none.
     */
    private static Corpus corpus(String name, Map<String, Path> programs, List<Path> books, Path cbl) throws Exception {
        Copybooks copybooks = new Copybooks(books, FrontCorpus::tabs);
        boolean mapped = name.equals("carddemo") || name.equals("nist");
        DataMapCorpus.Setup setup = mapped ? DataMapCorpus.setup(name, books) : null;
        Map<String, Future<Parts>> futures = new TreeMap<>();
        Map<String, Parts> parts = new TreeMap<>();
        try (ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())) {
            for (var p : programs.entrySet()) {
                futures.put(p.getKey(), pool.submit(() -> {
                    Parse.Result parse = Parse.file(p.getValue(), copybooks, 8);
                    String key = mapKey(name, p.getKey(), p.getValue(), cbl);
                    DataMapCorpus.One map = mapped && key != null ? DataMapCorpus.one(key, p.getValue(), parse, setup) : null;
                    return new Parts(FrontCorpus.errors(parse), IrCorpus.one(p.getKey(), parse, name), map);
                }));
            }
            for (var f : futures.entrySet()) parts.put(f.getKey(), f.getValue().get());
        }
        return new Corpus(programs, parts);
    }

    /** A program's name in the data map: NIST's as it is; CardDemo's file name without `.cbl`, for a file directly in `cbl/`; else null. */
    private static String mapKey(String corpus, String key, Path source, Path cbl) {
        if (corpus.equals("nist")) return key;
        if (!corpus.equals("carddemo") || !source.getParent().equals(cbl) || !source.getFileName().toString().toLowerCase().endsWith(".cbl")) return null;
        return source.getFileName().toString().replaceAll("(?i)\\.cbl$", "");
    }

    private static Map<String, List<String>> errors(Corpus c) {
        Map<String, List<String>> out = new TreeMap<>();
        c.parts().forEach((k, p) -> out.put(k, p.errors()));
        return out;
    }

    private static List<IrCorpus.One> irs(Corpus c) {
        return c.parts().values().stream().map(Parts::ir).toList();
    }

    /** The data map's parts in its own order, by name without `.cbl` (cobc's refusals and the programs outside it count for nothing). */
    private static List<DataMapCorpus.One> maps(Corpus c) {
        Map<String, DataMapCorpus.One> byName = new TreeMap<>();
        c.parts().forEach((k, p) -> {
            if (p.map() != null) byName.put(k.replaceAll("(?i)\\.cbl$", ""), p.map());
        });
        return new ArrayList<>(byName.values());
    }
}
