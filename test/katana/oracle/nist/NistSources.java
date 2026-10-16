package katana.oracle.nist;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import katana.oracle.Runs;

/**
 * NIST's programs as files, for the tests that read them rather than run them (2.1's parse, 2.5's data
 * map): EXEC85 under COBOL 85 extracts them; each program of a module the NIST run compiles that cobc
 * did not refuse (both read from oracle/nist/programs.golden) is written, with the copy members beside
 * it and a library's members in the library's folder, as the NIST run lays them out.
 */
public final class NistSources {
    private NistSources() {}

    /**
     * The programs written into `copy` under the root's work folder, by name. The one caller reads them for
     * its whole class, so a run's folder is not removed when it passes; the earlier runs' are removed here
     * instead (`Runs`).
     */
    public static Map<String, Path> write(Path root, Path work) throws Exception {
        try (var earlier = Files.list(work)) {
            Runs.passed(earlier.filter(f -> f.getFileName().toString().startsWith("copy")).toArray(Path[]::new));
        }
        String cards = Files.readString(root.resolve("oracle/nist/exec85.cards"));
        Path exec85 = Files.createTempDirectory(work, "exec85");
        List<Population.Member> members = Exec85.run(root.resolve("corpus/nist-cobol85-test-suite/newcob.val"), Exec85.cards(cards),
                exec85, new Cobc(Duration.ofSeconds(30)));
        Runs.passed(exec85);
        Path copy = Files.createTempDirectory(work, "copy");
        Map<String, String> libraries = Map.of("ALTLB", "ALTLIB1/ALTLB", "ALTL1", "ALTLIB2/ALTLB");
        List<String[]> golden = Files.readAllLines(root.resolve("oracle/nist/programs.golden")).stream().filter(l -> !l.startsWith("#")).map(l -> l.split(" ")).toList();
        Set<String> modules = golden.stream().map(f -> f[0]).collect(Collectors.toSet());
        Set<String> refused = golden.stream().filter(f -> f[3].equals("compile")).map(f -> f[1]).collect(Collectors.toSet());
        Map<String, Path> programs = new TreeMap<>();
        for (Population.Member m : members) {
            if (m.kind().equals("CLBRY")) {
                Path file = copy.resolve(libraries.getOrDefault(m.name(), m.name()) + ".CPY");
                Files.createDirectories(file.getParent());
                Files.write(file, m.lines(), StandardCharsets.ISO_8859_1);
            } else if (m.kind().equals("COBOL") && modules.contains(ModuleRun.module(m)) && !refused.contains(m.name())) {
                Path file = copy.resolve(m.name() + ".CBL");
                Files.write(file, m.lines(), StandardCharsets.ISO_8859_1);
                programs.put(m.name(), file);
            }
        }
        return programs;
    }
}
