package katana.oracle.lane;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * R-016 in the cobc lane, expanding as the front end does (katana.front.Tabs): a tab in a source or copy line is expanded to spaces up to the next tab stop,
 * every 8 columns or the width the fixture declares for the file (`tabs`), before cobc reads it, into a
 * shadow copy, so both lanes compile the same text. Each file that held a tab is reported with the
 * width used, a hypothesis until the Enterprise COBOL rule is read.
 */
final class Tabs {
    private Tabs() {}

    /** The text with every tab expanded to the next stop of this width (katana.front.Tabs, R-016). */
    static byte[] expand(byte[] source, int width) {
        return katana.front.Tabs.expand(source, width);
    }

    /** The width a fixture's `tabs` lines give a file name (the first whose pattern matches), else 8. */
    static java.util.function.ToIntFunction<String> widths(katana.oracle.fixture.Fixture fixture) {
        List<String[]> rules = new ArrayList<>();
        for (var line : fixture.header()) if (line.keyword().equals("tabs")) rules.add(new String[] {line.values().get(0), line.values().get(1)});
        return name -> {
            for (String[] rule : rules) {
                if (java.nio.file.FileSystems.getDefault().getPathMatcher("glob:" + rule[1]).matches(Path.of(name))) return Integer.parseInt(rule[0]);
            }
            return 8;
        };
    }

    static boolean has(byte[] source) {
        return katana.front.Tabs.has(source);
    }

    /** Copies each copybook folder to shadow/0, shadow/1, ... with tabs expanded at each file's width; the folders, and the names of files that held tabs. */
    static List<Path> shadow(List<Path> folders, Path shadow, java.util.function.ToIntFunction<String> width, List<String> expanded) {
        List<Path> copies = new ArrayList<>();
        try {
            for (int i = 0; i < folders.size(); i++) {
                Path copy = Files.createDirectories(shadow.resolve(String.valueOf(i)));
                try (Stream<Path> files = Files.list(folders.get(i))) {
                    for (Path file : files.filter(Files::isRegularFile).sorted().toList()) {
                        byte[] bytes = Files.readAllBytes(file);
                        if (has(bytes)) expanded.add(file.getFileName().toString());
                        Files.write(copy.resolve(file.getFileName()), expand(bytes, width.applyAsInt(file.getFileName().toString())));
                    }
                }
                copies.add(copy);
            }
            return copies;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
