package katana.oracle.nist;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * One NIST module run under cobc, as the CCVS85 User Guide 4.2 says (3.6, and each module's notes in
 * chapter 4): its main programs in population order, each followed by its SUBPRG members, which run
 * after it on the files it left; its SUBRTN members compiled as modules for it to CALL; the copy
 * library members beside the programs, with ALTLB and ALTL1 in the libraries X-47 and X-48 name; a
 * data member given to the program it is named after on its standard input, which its ACCEPT
 * statements read (NC109M tests ACCEPT format 1); and what the guide says of particular programs (the
 * debugging switch, files that must be absent) taken from oracle/nist/run-notes. All of a module's
 * programs share one folder, because a program may read a file the one before it wrote.
 */
public final class ModuleRun {
    /**
     * What happened to one run of one program: it did not compile or was stopped (error), or it ran to
     * an end, normal or not, with the counts its report gave if it printed one; a note says how it
     * ended when that was not a report. As in the published summary, a program that ran to an end is
     * executed, and its tests are what it reported, none if it reported none.
     */
    public record Outcome(String program, String error, String note, Optional<Report.Counts> counts) {
        static Outcome compileError(String program, String output) {
            return new Outcome(program, "compile: " + firstLine(output), "", Optional.empty());
        }

        public boolean compiled() {
            return error == null || !error.startsWith("compile");
        }
    }

    static final String PRINT = "PRINTOUT";
    /**
     * Where the two alternate-library members go, as a folder and a name: SM207A copies ALTLB from the
     * library X-47 names and expects ALTLB's own text, then ALTLB from X-48's and expects ALTL1's text
     * (it passes only if that copy fails), so ALTL1 is placed in X-48's library under the name ALTLB.
     */
    private static final Map<String, String> ALTERNATE_LIBRARY = Map.of("ALTLB", "ALTLIB1/ALTLB", "ALTL1", "ALTLIB2/ALTLB");

    private ModuleRun() {}

    /** The module a member belongs to: the first two letters of its main program, all OB programs together. */
    public static String module(Population.Member member) {
        return member.owner().startsWith("OB") ? "OB" : member.owner().substring(0, 2);
    }

    /**
     * Runs a module's programs in a folder of their own under a runner (`Cobc`, or phase 3's Java lane;
     * LANE-09) and returns each run's outcome, in order. The environment it passes is cobc's runtime's
     * (COB_SET_DEBUG, COB_LIBRARY_PATH, the switches); another runner maps what it needs of it.
     */
    public static List<Outcome> run(String module, List<Population.Member> extracted, RunNotes notes, Map<String, String> switches,
                                    Path dir, Runner runner) {
        try {
            Files.createDirectories(dir);
            Map<String, Population.Member> data = new HashMap<>();
            for (Population.Member member : extracted) {
                if (member.kind().equals("CLBRY")) writeCopy(dir, member);
                if (member.kind().equals("DATA*")) data.put(member.name(), member);
            }
            List<Outcome> outcomes = new ArrayList<>();
            for (Population.Member main : extracted) {
                if (!main.kind().equals("COBOL") || !main.name().equals(main.owner()) || !module(main).equals(module)) continue;
                outcomes.addAll(runMain(main, extracted, data, notes, switches, dir, runner));
            }
            return outcomes;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** A main program with its SUBRTN modules, then each of its SUBPRG members in order. */
    private static List<Outcome> runMain(Population.Member main, List<Population.Member> extracted,
                                         Map<String, Population.Member> data, RunNotes notes, Map<String, String> switches, Path dir, Runner runner) throws IOException {
        List<Outcome> outcomes = new ArrayList<>();
        for (Population.Member sub : extracted) {
            if (sub.owner().equals(main.name()) && !sub.name().equals(main.name()) && isSubroutine(sub)) {
                write(dir, sub);
                Runner.Result compiled = runner.module(dir, sub.name() + ".CBL");
                if (!compiled.ok()) return List.of(Outcome.compileError(main.name(), sub.name() + ": " + compiled.output()));
            }
        }
        outcomes.addAll(runProgram(main, data, notes, switches, dir, runner));
        for (Population.Member sub : extracted) {
            if (sub.owner().equals(main.name()) && !sub.name().equals(main.name()) && !isSubroutine(sub)) {
                outcomes.addAll(runProgram(sub, data, notes, switches, dir, runner));
            }
        }
        return outcomes;
    }

    /** One program compiled and run once per debugging setting its run note gives, its absent files removed first. */
    private static List<Outcome> runProgram(Population.Member program, Map<String, Population.Member> data, RunNotes notes,
                                            Map<String, String> switches, Path dir, Runner runner) throws IOException {
        write(dir, program);
        Runner.Result compiled = runner.program(dir, program.name() + ".CBL");
        if (!compiled.ok()) return List.of(Outcome.compileError(program.name(), compiled.output()));
        RunNotes.Note note = notes.of(program.name(), module(program));
        byte[] input = data.containsKey(program.name())
                ? String.join("\n", data.get(program.name()).lines()).concat("\n").getBytes(StandardCharsets.ISO_8859_1)
                : new byte[0];
        List<Outcome> outcomes = new ArrayList<>();
        for (String debug : note.debug()) {
            for (String file : note.absent()) removeFile(dir, file);
            Files.deleteIfExists(dir.resolve(PRINT));
            Map<String, String> env = new HashMap<>();
            if (!debug.isEmpty()) env.put("COB_SET_DEBUG", debug.equals("on") ? "Y" : "N");
            env.put("COB_LIBRARY_PATH", ".");
            env.putAll(switches);
            Runner.Result ran = runner.run(dir, program.name(), env, input);
            Path print = dir.resolve(PRINT);
            String printed = Files.exists(print) ? Files.readString(print, StandardCharsets.ISO_8859_1) : "";
            Optional<Report.Counts> counts = Report.read(printed);
            String error = ran.exit() == -1 ? "timed out" : null;
            String ended = !ran.ok() && error == null ? "exit " + ran.exit() + ": " + firstLine(ran.output()) : counts.isEmpty() ? "no report" : "";
            outcomes.add(new Outcome(program.name(), error, ended, counts));
        }
        return outcomes;
    }

    /** A file and, for an indexed file, its alternate-key parts (NAME.1, NAME.2, ...). */
    private static void removeFile(Path dir, String name) throws IOException {
        try (var files = Files.list(dir)) {
            for (Path p : files.toList()) {
                String f = p.getFileName().toString();
                if (f.equals(name) || f.matches(java.util.regex.Pattern.quote(name) + "\\.\\d+")) Files.delete(p);
            }
        }
    }

    private static boolean isSubroutine(Population.Member member) {
        return member.role().equals("SUBRTN");
    }

    private static void write(Path dir, Population.Member program) throws IOException {
        Files.write(dir.resolve(program.name() + ".CBL"), program.lines(), StandardCharsets.ISO_8859_1);
    }

    /** A copy member beside the programs, or in the alternate library the guide names for it (3.6). */
    private static void writeCopy(Path dir, Population.Member member) throws IOException {
        Path copy = dir.resolve(ALTERNATE_LIBRARY.getOrDefault(member.name(), member.name()) + ".CPY");
        Files.createDirectories(copy.getParent());
        Files.write(copy, member.lines(), StandardCharsets.ISO_8859_1);
    }

    private static String firstLine(String output) {
        return output.lines().filter(l -> !l.contains("_FORTIFY_SOURCE") && !l.contains("previous definition") && !l.isBlank())
                .findFirst().orElse("").trim();
    }
}
