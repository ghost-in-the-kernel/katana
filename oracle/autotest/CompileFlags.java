package katana.oracle.autotest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import katana.front.Settings;

/**
 * A case's compile flags (`Autotests.Case.flags`) as the front end's settings (R-045 amended, R-081, J-255). The flags are the
 * test's own, extracted by code from its compile command; this says which of them Katana's compile step understands:
 * `-DNAME[=VALUE]` is a definition, `-fdebugging-line` reads debugging lines as code, `-fixed` is the reading the front end
 * has; `-o`, `-w`, `-W...`, `-g`, `-v`, `-O...` and `-save-temps` leave the program's output as it was. Any other flag
 * (`-free`, `-std=`, `-fbinary-size=`, ...) changes what the program means or how it is read in a way Katana does not
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
                // Unsure: a name given without a value (`-DACTIVATE`) is taken as defined with the value ""; what cobc's value for it is, and so how `NAME = literal` reads, is not known here. Stays: the cases that use it only ask `DEFINED`.
                defines.put(eq < 0 ? f.substring(2) : f.substring(2, eq), eq < 0 ? "" : f.substring(eq + 1));
            } else if (f.equals("-fdebugging-line")) {
                debugging = true;
            } else if (f.equals("-fixed") || noEffect(f)) {
                continue;
            } else {
                unmapped.add(f);
            }
        }
        Settings settings = Settings.declared(defines, extensions);
        return new Mapped(debugging ? settings.withDebugging() : settings, List.copyOf(unmapped));
    }

    /** Flags that name a file, ask for warnings or diagnostics, or tune the C compile: none changes what the program prints. */
    private static boolean noEffect(String f) {
        return f.equals("-o") || f.equals("-w") || f.equals("-g") || f.equals("-v") || f.startsWith("-W") || f.startsWith("-O") || f.equals("-save-temps") || f.startsWith("-save-temps=");
    }
}
