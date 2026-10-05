package katana.sir;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.oracle.nist.Cobc;
import katana.oracle.nist.NistSources;
import katana.sir.build.Build;
import katana.sir.types.Item;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Phase 2.5's check, its data map half: on every program `cobc -C -std=ibm` accepts, of CardDemo and of
 * NIST, each field cobc lays out is an item of ours with the same size, offset and attribute tuple
 * (CobcLayout). sir/datamap.golden holds the counts and every difference, each named
 * (`-Dkatana.record=true` rewrites it).
 */
@Tag("integration")
class DataMapIntegrationTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));
    private static final Path GOLDEN = ROOT.resolve("sir/datamap.golden");
    private static final String HEADER = """
            # Phase 2.5 (docs/ROADMAP.md): the data map against `cobc -C -std=ibm`'s layout, on every program cobc
            # accepts: `corpus NAME programs P fields F masks M flagged G editing E differ D` (M of the fields edited, G JUSTIFIED,
            # BLANK WHEN ZERO or EXTERNAL, each compared too; tithe pass 2, A4; E the program units whose DECIMAL-POINT or
            # CURRENCY SIGN is not the default, every unit's compared with cobc's module, J-098), then each difference as `PROGRAM FIELD: what`.
            # Written by `gradle integration -Dkatana.record=true`.
            """;

    @Test
    void theDataMapIsCobcsLayout() throws Exception {
        StringBuilder report = new StringBuilder(HEADER);
        Path carddemo = ROOT.resolve("corpus/carddemo/app");
        Map<String, Path> cd = new TreeMap<>();
        try (Stream<Path> files = Files.list(carddemo.resolve("cbl"))) {
            files.filter(f -> f.toString().toLowerCase().endsWith(".cbl")).forEach(f -> cd.put(f.getFileName().toString().replaceAll("(?i)\\.cbl$", ""), f));
        }
        report.append(corpus("carddemo", cd, List.of(carddemo.resolve("cpy"), carddemo.resolve("cpy-bms"), ROOT.resolve("front/ibm"))));
        Map<String, Path> nist = NistSources.write(ROOT, Files.createDirectories(ROOT.resolve("build/sir-nist")));
        Path copy = nist.values().iterator().next().getParent();
        report.append(corpus("nist", nist, List.of(copy)));
        String recorded = Files.exists(GOLDEN) ? Files.readString(GOLDEN) : HEADER;
        if (Boolean.getBoolean("katana.record")) Files.writeString(GOLDEN, report.toString());
        System.out.print(report);
        assertEquals(recorded, report.toString());
    }

    /** A corpus's programs translated by cobc in a folder of their own, each unit's fields compared. */
    private static String corpus(String name, Map<String, Path> programs, List<Path> copybooks) throws Exception {
        Path work = Files.createDirectories(ROOT.resolve("build/sir-cobc/" + name));
        Cobc cobc = new Cobc(Duration.ofSeconds(60), "ibm");
        Copybooks books = new Copybooks(copybooks, n -> n.equalsIgnoreCase("CUSTREC.cpy") ? 4 : 8);
        List<String> flags = new ArrayList<>();
        for (Path p : copybooks) flags.addAll(List.of("-I", p.toAbsolutePath().toString()));
        flags.addAll(List.of("-I", copybooks.get(0).resolve("ALTLIB1").toAbsolutePath().toString()));
        int translated = 0, fields = 0, masks = 0, flagged = 0, editing = 0;
        List<String> differences = new ArrayList<>();
        for (var program : programs.entrySet()) {
            Files.copy(program.getValue(), work.resolve(program.getKey() + ".cbl"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            if (!cobc.translate(work, program.getKey() + ".cbl", flags).ok()) continue;
            translated++;
            Parse.Result parse = Parse.file(program.getValue(), books, 8);
            List<Build.Data> units = Build.all(parse);
            List<List<CobcLayout.Field>> layout = CobcLayout.read(work, program.getKey());
            List<String> modules = CobcLayout.editing(work, program.getKey());
            for (int u = 0; u < Math.min(units.size(), modules.size()); u++) {
                String ours = (units.get(u).decimalComma() ? "," : ".") + units.get(u).currency();
                if (!ours.equals(modules.get(u))) differences.add(program.getKey() + " unit " + u + ": point and currency " + ours + " vs " + modules.get(u));
                if (!modules.get(u).equals(".$")) editing++;
            }
            for (int u = 0; u < Math.min(units.size(), layout.size()); u++) {
                for (CobcLayout.Field f : layout.get(u)) {
                    if (!CobcLayout.compared(f)) continue;
                    fields++;
                    if (f.picture() != null) masks++;
                    if ((f.flags() & 0x18) != 0 || f.external()) flagged++;
                    Item item = CobcLayout.match(f, units.get(u).items());
                    String finding = units.get(u).findings().stream().filter(x -> x.startsWith("REPORT SECTION") || x.startsWith("COMMUNICATION SECTION")).findFirst().orElse(null);
                    String why = item != null ? CobcLayout.difference(item, f, units.get(u)) : finding == null ? "no item of ours" : "no item of ours (a finding: " + finding.substring(0, finding.indexOf(" at ")) + ")";
                    if (why != null) differences.add(program.getKey() + " " + f.name() + ": " + why);
                }
            }
        }
        StringBuilder out = new StringBuilder("corpus " + name + " programs " + translated + " fields " + fields + " masks " + masks + " flagged " + flagged + " editing " + editing + " differ " + differences.size() + "\n");
        differences.forEach(d -> out.append(d).append('\n'));
        return out.toString();
    }
}
