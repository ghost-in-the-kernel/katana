package katana.oracle.lane;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import katana.profile.Profile;

/**
 * `cobc` under a profile, with a cache: a source compiled once under the same arguments is not
 * compiled again, so a run that loads ten files of one shape compiles one loader. What it builds is
 * copied into the run's own bin folder under the program's name, which is the library path's view.
 */
final class Compiler {
    /** What cobc said when it refused a source; empty when it built it. */
    record Built(Path file, String refusal) {}

    private final Profile profile;
    private final List<Path> copybooks;
    private final Path cache;
    private final Duration limit;
    private final String copybooksHash;

    /** Under a profile, with copybook folders whose every file is part of each compile's cache key. */
    private final List<String> extra;

    Compiler(Profile profile, List<Path> copybooks, Path cache, Duration limit, List<String> extra) {
        this.extra = extra;
        this.profile = profile;
        this.copybooks = copybooks;
        this.cache = cache;
        this.limit = limit;
        StringBuilder all = new StringBuilder();
        try {
            for (Path dir : copybooks) {
                try (var files = Files.list(dir)) {
                    for (Path f : files.sorted().toList()) all.append(f.getFileName()).append(' ').append(hash(Files.readAllBytes(f), "")).append('\n');
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        this.copybooksHash = hash(all.toString().getBytes(StandardCharsets.UTF_8), "");
    }

    /** Builds a program (`-x`) or a module (`-m`, named NAME.so) into bin, from the source's bytes. */
    Built build(String name, byte[] source, boolean module, Path bin) {
        List<String> arguments = new ArrayList<>(profile.cobcArguments());
        arguments.add(module ? "-m" : "-x");
        arguments.addAll(extra);
        copybooks.forEach(c -> arguments.add("-I" + c));
        String key = hash(source, String.join(" ", arguments) + " " + copybooksHash);
        String file = module ? name + ".so" : name;
        try {
            Path dir = Files.createDirectories(cache.resolve(key));
            if (!Files.exists(dir.resolve(file))) {
                Path refusal = compile(dir, name, source, arguments, file);
                if (refusal != null) return new Built(null, firstError(Files.readString(refusal, StandardCharsets.ISO_8859_1)));
            }
            Files.createDirectories(bin);
            return new Built(Files.copy(dir.resolve(file), bin.resolve(file), StandardCopyOption.REPLACE_EXISTING), "");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Runs cobc in the cache folder, the source in a subfolder of its own (a program named like a copybook it copies would otherwise copy itself); the path of cobc's output when it failed, else null. */
    private Path compile(Path dir, String name, byte[] source, List<String> arguments, String file) throws IOException {
        Path src = Files.write(Files.createDirectories(dir.resolve("src")).resolve(name + ".cbl"), source);
        List<String> command = new ArrayList<>(List.of("cobc"));
        command.addAll(arguments);
        command.addAll(List.of("-o", file, "src/" + src.getFileName()));
        Path out = dir.resolve("cobc.out");
        int exit = Exec.run(command, dir, Map.of(), Path.of("/dev/null"), out, out, limit);
        return exit == 0 && Files.exists(dir.resolve(file)) ? null : out;
    }

    private static String firstError(String output) {
        return output.lines().filter(l -> l.contains("error")).findFirst().orElse(output.lines().findFirst().orElse("no output"));
    }

    private static String hash(byte[] source, String arguments) {
        try {
            var digest = java.security.MessageDigest.getInstance("SHA-256");
            digest.update(source);
            digest.update(arguments.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest.digest()).substring(0, 24);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
