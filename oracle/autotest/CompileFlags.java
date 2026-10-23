package katana.oracle.autotest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import katana.front.Settings;

/**
 * A case's compile flags (`Autotests.Case.flags`) as the front end's settings (R-045 amended, R-081, J-255). The flags are the
 * test's own, extracted by code from its compile command; this says which of them Katana's compile step understands:
 * `-DNAME[=VALUE]` is a definition (a name with no value has its value unknown, R-084), `-fdebugging-line` reads debugging lines as code, `-fixed` is the reading the front end
 * has; `-std=ibm` and `-std=ibm-strict` are the dialect the lane's oracle runs (R-083) and set nothing; `-o`, `-w`, `-W...`, `-g`, `-v`, `-O...` and `-save-temps` leave the program's output as it was. Any other flag
 * (`-free`, another `-std=`, `-fbinary-size=`, ...) changes what the program means or how it is read in a way Katana does not
 * yet follow, so it is returned unmapped, never dropped: a case run without it would prove nothing (P-03).
 */
public final class CompileFlags {
    private CompileFlags() {}

    /** The settings the understood flags make, and the flags not understood, in the order given. */
    public record Mapped(Settings settings, List<String> unmapped) {}

    /** The flags as settings; `extensions` is the caller's (R-081 amended): the census sets it, a flag never does. */
    // Decision: R-045
    public static Mapped map(List<String> flags, boolean extensions) {
        Map<String, String> defines = new LinkedHashMap<>();
        boolean debugging = false;
        List<String> unmapped = new ArrayList<>();
        for (String f : flags) {
            if (f.startsWith("-D") && f.length() > 2) {
                int eq = f.indexOf('=');
                // A name given without a value is defined with its value unknown (R-084), never "".
                defines.put(eq < 0 ? f.substring(2) : f.substring(2, eq), eq < 0 ? Settings.UNKNOWN : f.substring(eq + 1));
            } else if (f.equals("-fdebugging-line")) {
                debugging = true;
            } else if (f.equals("-fixed") || f.equals("-std=ibm") || f.equals("-std=ibm-strict") || noEffect(f)) {
                continue;
            } else {
                unmapped.add(f);
            }
        }
        Settings settings = Settings.declared(defines, extensions);
        return new Mapped(debugging ? settings.withDebugging() : settings, List.copyOf(unmapped));
    }

    /**
     * Why a flag the census does not map stays unmapped, or null when no reason is given yet (J-256): another `-std=` is another
     * dialect's expected output (R-083), and free form is a source format the dialect does not have (R-085).
     */
    // Decision: R-083
    public static String reason(String flag) {
        if (flag.startsWith("-std=")) return "another dialect's expected output";
        if (flag.equals("-free") || flag.equals("--free")) return "a source format the dialect does not have";
        return null;
    }

    /** Flags that name a file, ask for warnings or diagnostics, or tune the C compile: none changes what the program prints. */
    private static boolean noEffect(String f) {
        return f.equals("-o") || f.equals("-w") || f.equals("-g") || f.equals("-v") || f.startsWith("-W") || f.startsWith("-O") || f.equals("-save-temps") || f.startsWith("-save-temps=");
    }
}
