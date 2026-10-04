package katana.oracle.lane;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * R-016 in the cobc lane: a tab in a source or copy line is expanded to spaces up to the next
 * 8-column stop (columns 9, 17, 25, ...) before cobc reads it, into a shadow copy, so both lanes
 * compile the same text. Each file that held a tab is reported, since the 8-column stop is a
 * hypothesis until IBM's text on tabs is read.
 */
final class Tabs {
    private Tabs() {}

    /** The text with every tab expanded to the next 8-column stop, line by line. */
    static byte[] expand(byte[] source) {
        String text = new String(source, StandardCharsets.ISO_8859_1);
        if (text.indexOf('\t') < 0) return source;
        StringBuilder out = new StringBuilder();
        int column = 0;
        for (char c : text.toCharArray()) {
            if (c == '\t') {
                do out.append(' '); while (++column % 8 != 0);
            } else {
                out.append(c);
                column = c == '\n' ? 0 : column + 1;
            }
        }
        return out.toString().getBytes(StandardCharsets.ISO_8859_1);
    }

    static boolean has(byte[] source) {
        for (byte b : source) if (b == '\t') return true;
        return false;
    }

    /** Copies each copybook folder to shadow/0, shadow/1, ... with tabs expanded; the folders, and the names of files that held tabs. */
    static List<Path> shadow(List<Path> folders, Path shadow, List<String> expanded) {
        List<Path> copies = new ArrayList<>();
        try {
            for (int i = 0; i < folders.size(); i++) {
                Path copy = Files.createDirectories(shadow.resolve(String.valueOf(i)));
                try (Stream<Path> files = Files.list(folders.get(i))) {
                    for (Path file : files.filter(Files::isRegularFile).sorted().toList()) {
                        byte[] bytes = Files.readAllBytes(file);
                        if (has(bytes)) expanded.add(file.getFileName().toString());
                        Files.write(copy.resolve(file.getFileName()), expand(bytes));
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
