package katana.oracle.nist;

import java.nio.file.Path;
import java.util.Map;

/**
 * What NIST's programs run under (tithe pass 2, B2; docs/LANES.md LANE-09): build a module another
 * program CALLs, build a main program, run it in its folder. `Cobc` is the one today; phase 3's Java
 * lane is the other, so 3.4 runs NIST through both lanes by passing `ModuleRun` a different runner. The
 * lanes' job steps have their own seam, `oracle/lane/ProgramRunner`.
 */
public interface Runner {
    /** How a build or a run ended: its exit code (-1 when it was stopped at the time limit) and what it printed. */
    record Result(int exit, String output) {
        public boolean ok() {
            return exit == 0;
        }
    }

    /** Builds, from its source in a folder, a module another program CALLs. */
    Result module(Path dir, String source);

    /** Builds a main program from its source in a folder. */
    Result program(Path dir, String source);

    /** Runs a built main program in its folder, with these environment variables added and these bytes as its input. */
    Result run(Path dir, String program, Map<String, String> env, byte[] input);
}
