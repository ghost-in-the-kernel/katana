package katana.oracle.lane;

import java.nio.file.Path;
import java.util.Map;

/** How a lane runs a program step: the cobc lane builds the COBOL program, the java lane runs a Java program (docs/LANES.md). */
interface ProgramRunner {
    /** Runs a program step with its DD bindings and clock in env and its SYSIN as stdin. */
    Programs.Ended run(String step, String name, String parm, Map<String, String> env, Path stdin);
}
