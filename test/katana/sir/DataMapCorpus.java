package katana.sir;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import katana.front.Parse;
import katana.oracle.nist.Cobc;
import katana.sir.build.Build;
import katana.sir.types.Item;

/**
 * Phase 2.5's check, its data map half: on every program `cobc -C -std=ibm` accepts, of CardDemo and of
 * NIST, each field cobc lays out is an item of ours with the same size, offset and attribute tuple
 * (CobcLayout). sir/datamap.golden holds the counts and every difference, each named
 * (`-Dkatana.record=true` rewrites it). Run by `katana.front.CorpusIntegrationTest`, which parses each
 * program once for this, the IR and the front end's checks (J-169).
 */
public final class DataMapCorpus {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));
    private static final Path GOLDEN = ROOT.resolve("sir/datamap.golden");
    private static final String HEADER = """
            # Phase 2.5 (docs/ROADMAP.md): the data map against `cobc -C -std=ibm`'s layout, on every program cobc
            # accepts: `corpus NAME programs P fields F masks M flagged G editing E differ D` (M of the fields edited, G JUSTIFIED,
            # BLANK WHEN ZERO or EXTERNAL, each compared too; tithe pass 2, A4; E the program units whose DECIMAL-POINT or
            # CURRENCY SIGN is not the default, every unit's compared with cobc's module, J-098), then each difference as `PROGRAM FIELD: what`.
            # Written by `gradle integration -Dkatana.record=true`.
            """;

    /** One program's part: the units' fields compared, or null when cobc does not translate it. */
    public record One(int fields, int masks, int flagged, int editing, List<String> differences) {}

    /** A corpus's folder for cobc's translations, and the flags that find its copybooks. */
    public record Setup(Path work, List<String> flags) {}

    /** The work folder and `-I` flags for a corpus whose copybooks are these folders. */
    public static Setup setup(String name, List<Path> copybooks) throws Exception {
        Path work = Files.createDirectories(ROOT.resolve("build/sir-cobc/" + name));
        List<String> flags = new ArrayList<>();
        for (Path p : copybooks) flags.addAll(List.of("-I", p.toAbsolutePath().toString()));
        flags.addAll(List.of("-I", copybooks.get(0).resolve("ALTLIB1").toAbsolutePath().toString()));
        return new Setup(work, flags);
    }

    private static final Cobc COBC = new Cobc(Duration.ofSeconds(60), "ibm");

    /** One program translated by cobc in its corpus's folder and its units' fields compared with ours, from its parse; null when cobc refuses it. */
    public static One one(String key, Path source, Parse.Result parse, Setup setup) throws Exception {
        Files.copy(source, setup.work().resolve(key + ".cbl"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        if (!COBC.translate(setup.work(), key + ".cbl", setup.flags()).ok()) return null;
        int fields = 0, masks = 0, flagged = 0, editing = 0;
        List<String> differences = new ArrayList<>();
        List<Build.Data> units = Build.all(parse);
        List<List<CobcLayout.Field>> layout = CobcLayout.read(setup.work(), key);
        List<String> modules = CobcLayout.editing(setup.work(), key);
        for (int u = 0; u < Math.min(units.size(), modules.size()); u++) {
            String ours = (units.get(u).decimalComma() ? "," : ".") + units.get(u).currency();
            if (!ours.equals(modules.get(u))) differences.add(key + " unit " + u + ": point and currency " + ours + " vs " + modules.get(u));
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
                if (why != null) differences.add(key + " " + f.name() + ": " + why);
            }
        }
        return new One(fields, masks, flagged, editing, differences);
    }

    /** One corpus's lines of the golden from its programs' parts in their order (null for those cobc refused). */
    public static String corpus(String name, List<One> ones) {
        int translated = 0, fields = 0, masks = 0, flagged = 0, editing = 0;
        List<String> differences = new ArrayList<>();
        for (One o : ones) {
            if (o == null) continue;
            translated++;
            fields += o.fields();
            masks += o.masks();
            flagged += o.flagged();
            editing += o.editing();
            differences.addAll(o.differences());
        }
        StringBuilder out = new StringBuilder("corpus " + name + " programs " + translated + " fields " + fields + " masks " + masks + " flagged " + flagged + " editing " + editing + " differ " + differences.size() + "\n");
        differences.forEach(d -> out.append(d).append('\n'));
        return out.toString();
    }

    /** The data map golden held against the corpora's lines. */
    public static void hold(String corpora) throws Exception {
        String report = HEADER + corpora;
        String recorded = Files.exists(GOLDEN) ? Files.readString(GOLDEN) : HEADER;
        if (Boolean.getBoolean("katana.record")) Files.writeString(GOLDEN, report);
        System.out.print(report);
        assertEquals(recorded, report);
    }
}
