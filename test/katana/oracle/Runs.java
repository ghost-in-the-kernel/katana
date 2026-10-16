package katana.oracle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * A test run's folders under build/: an integration test makes one per run (`createTempDirectory`), so
 * two runs never share one, and removes it when the test passes, as nothing reads it then; a failing
 * test leaves its folders to be read. Kept, they filled the disk in a day of lane runs (J-179).
 */
public final class Runs {
    private Runs() {}

    /** Removes each folder and what it holds; one already gone is nothing to remove. */
    public static void passed(Path... folders) throws IOException {
        for (Path folder : folders) {
            if (!Files.exists(folder)) continue;
            try (Stream<Path> files = Files.walk(folder)) {
                for (Path p : files.sorted(Comparator.reverseOrder()).toList()) Files.delete(p);
            }
        }
    }
}
