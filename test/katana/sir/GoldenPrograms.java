package katana.sir;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.sir.build.Build;

/**
 * The golden programs (`sir/golden`, the trivial fixture's COPYREC) parsed and built once per JVM: the
 * IR, roles and token goldens each read the same thirteen, which the fast loop paid for four times
 * (tithe 3a.9). Parse trees and programs are only read, never changed, by the tests that share them.
 */
final class GoldenPrograms {
    static final String HASH = "sha256:25d21d9dc3d4db6554821c53cb0927a5bcc95f750d04631a50e43bb636866296";

    record Built(Parse.Result parse, List<Build.Result> results) {}

    private static final Map<Path, Built> BUILT = new ConcurrentHashMap<>();

    private GoldenPrograms() {}

    /** One program's parse and its programs, built under the ibm profile's hash, the copybooks beside it. */
    static Built of(Path program, Path copybooks) {
        return BUILT.computeIfAbsent(program.toAbsolutePath().normalize(), p -> {
            Parse.Result parse = Parse.file(p, new Copybooks(List.of(copybooks), n -> 8), 8);
            return new Built(parse, Build.programs(parse, HASH));
        });
    }
}
