package katana.oracle.lane;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The catalog of one run, on disk: each dataset a file under the run's folder (an indexed one is
 * GnuCOBOL's BDB file and, with alternate keys, its numbered companions), the generations a job
 * creates, and the loading and unloading of indexed files in key order through Generated's programs.
 */
final class Datasets {
    /** A dataset in the catalog: its organization (PS or KSDS), its file, its record length, and a KSDS's shape. */
    record Entry(String dsn, String org, Path path, int lrecl, Generated.Shape shape) {}

    private static final Pattern RELATIVE = Pattern.compile("(.+)\\(\\+(\\d+)\\)");
    private final Map<String, Entry> entries = new TreeMap<>();
    private final Map<String, String> generations = new HashMap<>();
    private final Path dir;
    private final Path work;
    private final Compiler compiler;
    private final Duration limit;

    Datasets(Path work, Compiler compiler, Duration limit) throws IOException {
        this.work = work;
        this.dir = Files.createDirectories(work.resolve("catalog"));
        this.compiler = compiler;
        this.limit = limit;
    }

    /** A name as the catalog knows it: `BASE(+n)` is the job's n-th new generation, `BASE.Gnnnn V00`. */
    String absolute(String dsn) {
        Matcher m = RELATIVE.matcher(dsn);
        if (!m.matches()) return dsn;
        return generations.computeIfAbsent(dsn, d -> String.format("%s.G%04dV00", m.group(1), Integer.parseInt(m.group(2))));
    }

    Entry get(String dsn) {
        return entries.get(absolute(dsn));
    }

    Collection<Entry> all() {
        return entries.values();
    }

    /** A sequential dataset holding these records (none for a new one). */
    Entry sequential(String dsn, int lrecl, byte[] records) {
        Entry entry = new Entry(absolute(dsn), "PS", dir.resolve(absolute(dsn)), lrecl, null);
        write(entry.path(), records);
        entries.put(entry.dsn(), entry);
        return entry;
    }

    /** An indexed dataset loaded with these records, which must be in key order; the loader's complaint, or "" when loaded. */
    String indexed(String dsn, Generated.Shape shape, byte[] records) {
        Entry entry = new Entry(absolute(dsn), "KSDS", dir.resolve(absolute(dsn)), shape.lrecl(), shape);
        delete(entry.dsn());
        entries.put(entry.dsn(), entry);
        return load(entry, records);
    }

    /** Replaces an indexed dataset's records with these (in key order); the loader's complaint, or "". */
    String load(Entry entry, byte[] records) {
        Path seq = write(work.resolve("load.seq"), records);
        removeFiles(entry.path());
        return generated("kload-", Generated.loader(entry.shape()), entry, seq);
    }

    /** A dataset's records: a sequential file's bytes, or an indexed file's in key order. */
    byte[] unload(Entry entry) {
        try {
            if (entry.org().equals("PS")) return Files.exists(entry.path()) ? Files.readAllBytes(entry.path()) : new byte[0];
            Path seq = work.resolve("unload.seq");
            Files.deleteIfExists(seq);
            String complaint = generated("kunload-", Generated.unloader(entry.shape()), entry, seq);
            if (!complaint.isEmpty()) throw new IllegalStateException("unloading " + entry.dsn() + ": " + complaint);
            return Files.readAllBytes(seq);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Removes a dataset from the catalog and its files from the disk; false when there was none. */
    boolean delete(String dsn) {
        Entry entry = entries.remove(absolute(dsn));
        if (entry != null) removeFiles(entry.path());
        return entry != null;
    }

    /** Builds and runs a loader or unloader with SEQIN and KSOUT bound; its stderr when it failed, else "". */
    private String generated(String prefix, String source, Entry entry, Path seq) {
        Path bin = work.resolve("bin");
        Compiler.Built built = compiler.build(prefix + entry.shape().tag(), source.getBytes(), false, bin);
        if (!built.refusal().isEmpty()) throw new IllegalStateException("the lane's own " + prefix + " did not compile: " + built.refusal());
        Path out = work.resolve("generated.out"), err = work.resolve("generated.err");
        int exit = Exec.run(List.of(built.file().toString()), work, Map.of("DD_SEQIN", seq.toString(), "DD_KSOUT", entry.path().toString()),
                Path.of("/dev/null"), out, err, limit);
        try {
            return exit == 0 ? "" : ("exit " + exit + " " + Files.readString(err).trim()).trim();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Path write(Path path, byte[] bytes) {
        try {
            return Files.write(path, bytes);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** A file and the companions BDB keeps beside it for alternate keys (`name.1`, `name.2`, ...). */
    private static void removeFiles(Path path) {
        try {
            Files.deleteIfExists(path);
            for (int i = 1; Files.deleteIfExists(Path.of(path + "." + i)); i++) {
                // each alternate key's file
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
