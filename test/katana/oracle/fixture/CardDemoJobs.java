package katana.oracle.fixture;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * The CardDemo jobs a test runs, held to `oracle/fixture/carddemo/` (J-232): a test keeps its own list, in its own
 * order, and names each job it leaves out with why; together they are every fixture there, so a job added to the
 * folder that a test neither runs nor leaves out by name fails that test.
 */
public final class CardDemoJobs {
    private CardDemoJobs() {}

    /** Every job a `.fixture` in the folder names (a folder of its own, such as DATEPARM, is a study, not a job). */
    public static List<String> all(Path root) {
        try (var files = Files.list(root.resolve("oracle/fixture/carddemo"))) {
            return files.map(f -> f.getFileName().toString()).filter(n -> n.endsWith(".fixture")).map(n -> n.substring(0, n.length() - ".fixture".length())).sorted().toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** `jobs`, once each fixture is run or left out by name with why; a refusal naming what is neither, or both. */
    public static List<String> held(Path root, List<String> jobs, Map<String, String> leftOut) {
        List<String> wrong = new ArrayList<>();
        TreeSet<String> named = new TreeSet<>(jobs);
        for (String job : leftOut.keySet()) if (!named.add(job)) wrong.add(job + " is both run and left out");
        if (named.size() != jobs.size() + leftOut.size()) wrong.add("a job is listed twice");
        TreeSet<String> folder = new TreeSet<>(all(root));
        for (String job : folder) if (!named.contains(job)) wrong.add(job + " is in oracle/fixture/carddemo/ but neither run nor left out with why");
        for (String job : named) if (!folder.contains(job)) wrong.add(job + " has no fixture");
        if (!wrong.isEmpty()) throw new IllegalStateException(String.join("; ", wrong));
        return jobs;
    }
}
