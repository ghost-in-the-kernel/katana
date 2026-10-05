package katana.front;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

/**
 * Where a COPY finds its text: the folders, in order, each file by its name with or without its
 * extension, case ignored (`COPY CVACT01Y`, `COPY 'CVACT01Y'` and `COPY cvact01y.cpy` find
 * `CVACT01Y.cpy`); a library named by OF or IN is a subfolder of that name, searched first when it
 * exists (`COPY ALTLB OF ALTLIB2` reads `ALTLIB2/ALTLB.CPY`). The text comes back
 * with its tabs expanded at the width given for the file's name (R-016).
 */
public final class Copybooks {
    private final Map<String, Path> files = new HashMap<>();
    private final ToIntFunction<String> tabWidth;

    /** The folders searched in order (the first file of a name wins), and the tab width of each file name. */
    public Copybooks(List<Path> folders, ToIntFunction<String> tabWidth) {
        this.tabWidth = tabWidth;
        for (Path folder : folders) {
            if (!Files.isDirectory(folder)) continue;
            index(folder, "");
            try (Stream<Path> list = Files.list(folder)) {
                for (Path library : list.filter(Files::isDirectory).sorted().toList()) index(library, library.getFileName().toString().toUpperCase(Locale.ROOT) + "/");
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    /** Each file of a folder under its name and its name without the extension, after the prefix. */
    private void index(Path folder, String prefix) {
        try (Stream<Path> list = Files.list(folder)) {
            for (Path file : list.filter(Files::isRegularFile).sorted().toList()) {
                String name = file.getFileName().toString().toUpperCase(Locale.ROOT);
                files.putIfAbsent(prefix + name, file);
                int dot = name.lastIndexOf('.');
                if (dot > 0) files.putIfAbsent(prefix + name.substring(0, dot), file);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** The copybook's text, tabs expanded, or null when no folder has it; the library may be null. */
    public String read(String name, String library) {
        String key = unquote(name);
        Path file = library == null ? null : files.get(unquote(library) + "/" + key);
        if (file == null) file = files.get(key);
        if (file == null) return null;
        try {
            byte[] bytes = Files.readAllBytes(file);
            return new String(Tabs.expand(bytes, tabWidth.applyAsInt(file.getFileName().toString())), StandardCharsets.ISO_8859_1);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String unquote(String word) {
        return word.replaceAll("^[\"']|[\"']$", "").toUpperCase(Locale.ROOT);
    }
}
