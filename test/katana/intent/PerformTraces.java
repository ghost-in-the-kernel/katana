package katana.intent;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import katana.intent.rules.Rules;
import katana.intent.types.Fact;
import katana.intent.types.Subject;
import katana.sir.types.Procedure;
import katana.sir.types.Program;

/**
 * T-F3 (research §33, §34): each `function` range of the review file, read on the java lane's PERFORM traces
 * (`rt/PerformTrace`) of the proven jobs' runs: how often a PERFORM of the whole range begins it, how often
 * control enters its first paragraph, and how: by that PERFORM, by falling in from the paragraph before, or
 * otherwise (a GO TO); how often it returns and how often its last paragraph falls through to the next.
 * Held in carddemo-batch.performs by ProofV1IntegrationTest, which runs the jobs; read by the review.
 */
public final class PerformTraces {
    static final Path FILE = BatchReview.ROOT.resolve("test/katana/intent/carddemo-batch.performs");
    static final String TEST = "T-F3";

    /** One range's counts. */
    static final class Counts {
        int performed, entered, returned, otherwise, fellIn, fellOut;

        String line() {
            return "performed " + performed + " | entered " + entered + " | returned " + returned + " | entered otherwise " + otherwise + " | fell in " + fellIn + " | fell out " + fellOut;
        }
    }

    private PerformTraces() {}

    /** Holds the summary of the traces, each run's lines in order, against carddemo-batch.performs; under `-Dkatana.record=true` writes it. */
    public static void hold(List<List<String>> runs, List<String> jobs) throws Exception {
        String text = summary(BatchReview.programs(), runs, jobs);
        if (Boolean.getBoolean("katana.record")) Files.writeString(FILE, text);
        assertEquals(Files.readString(FILE), text, "the PERFORM traces' summary (T-F3)");
    }

    static String summary(List<Program> programs, List<List<String>> runs, List<String> jobs) {
        StringBuilder out = new StringBuilder("# T-F3 (research §33, §34): each function range of carddemo-batch.review on the java lane's PERFORM traces of\n# " + String.join(", ", jobs)
                + ", written by ProofV1IntegrationTest under -Dkatana.record=true. PROGRAM | FIRST THRU LAST | counts.\n");
        for (Program p : programs) for (Map.Entry<Subject.Range, Counts> e : counts(p, runs).entrySet())
            out.append(p.header().program()).append(" | ").append(range(p, e.getKey())).append(" | ").append(e.getValue().line()).append('\n');
        return out.toString();
    }

    /** Each function range of the program, and its counts over the runs. */
    static Map<Subject.Range, Counts> counts(Program p, List<List<String>> runs) {
        Map<Subject.Range, Counts> out = new LinkedHashMap<>();
        for (Fact f : Rules.infer(p).facts()) if (f.subject() instanceof Subject.Range r) out.put(r, new Counts());
        String name = p.header().program();
        Map<String, Integer> index = new HashMap<>();
        for (Procedure.Paragraph g : p.procedure().paragraphs()) index.put(g.name(), g.index());
        for (List<String> run : runs) {
            String last = "";
            for (String line : run) {
                String[] w = line.split(" ");
                if (w.length < 3 || !w[1].equals(name)) continue;
                for (Map.Entry<Subject.Range, Counts> e : out.entrySet()) count(e.getKey(), e.getValue(), w, last.split(" "), index);
                last = line;
            }
        }
        return out;
    }

    private static void count(Subject.Range r, Counts c, String[] w, String[] before, Map<String, Integer> index) {
        switch (w[0]) {
            case "Perform:" -> c.performed += Integer.parseInt(w[2]) == r.first() && Integer.parseInt(w[3]) == r.last() ? 1 : 0;
            case "Return:" -> c.returned += Integer.parseInt(w[2]) == r.last() ? 1 : 0;
            case "Fall:" -> c.fellOut += Integer.parseInt(w[2]) == r.last() ? 1 : 0;
            case "Enter:" -> {
                if (index.getOrDefault(w[2], -1) != r.first()) return;
                c.entered++;
                boolean performed = before[0].equals("Perform:") && Integer.parseInt(before[2]) == r.first() && Integer.parseInt(before[3]) == r.last();
                boolean fell = before[0].equals("Fall:") && Integer.parseInt(before[3]) == r.first();
                if (fell) c.fellIn++;
                else if (!performed) c.otherwise++;
            }
            default -> {}
        }
    }

    private static String range(Program p, Subject.Range r) {
        String first = p.procedure().paragraphs().get(r.first()).name(), last = p.procedure().paragraphs().get(r.last()).name();
        return r.first() == r.last() ? first : first + " THRU " + last;
    }

    /** T-F3's word on a function fact, from the held summary. */
    static Verdict verdict(Program p, Subject.Range r) throws Exception {
        String key = p.header().program() + " | " + range(p, r) + " | ";
        String line = Files.readAllLines(FILE).stream().filter(l -> l.startsWith(key)).findFirst().orElse(null);
        if (line == null) return Verdict.cannotTell(TEST, "no PERFORM trace names it (carddemo-batch.performs)");
        Map<String, Integer> n = new HashMap<>();
        for (String cell : line.substring(key.length()).split(" \\| ")) n.put(cell.substring(0, cell.lastIndexOf(' ')), Integer.parseInt(cell.substring(cell.lastIndexOf(' ') + 1)));
        if (n.get("entered") == 0) return Verdict.cannotTell(TEST, "no proven job's run in the java lane enters it, so its traces say nothing of it");
        List<String> wrong = new ArrayList<>();
        if (n.get("fell in") > 0) wrong.add("control fell into it " + n.get("fell in") + " times");
        if (n.get("fell out") > 0) wrong.add("its end fell through to the next paragraph " + n.get("fell out") + " times");
        if (n.get("entered otherwise") > 0) wrong.add("it was entered " + n.get("entered otherwise") + " times by neither a PERFORM of the whole block nor a fall (a GO TO)");
        if (!wrong.isEmpty()) return Verdict.refutes(TEST, "in the java lane's runs of the proven jobs " + String.join("; ", wrong));
        return Verdict.holds(TEST, "the java lane's runs of the proven jobs enter it " + n.get("entered") + " times, each by a PERFORM of the whole block, and it returns " + n.get("returned") + " times; control never falls into or out of it");
    }
}
