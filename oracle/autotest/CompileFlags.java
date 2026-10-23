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
 * has; `-std=ibm` and `-std=ibm-strict` are the dialect the lane's oracle runs (R-083) and set nothing; `-o`, `-w`, `-W...`, `-g`, `-v`, `-O...`, `-save-temps`, the listing (`-t`, `-fno-theaders`, `-fno-tmessages`) and `-fdiagnostics-show-option` leave the program's output as it was. Any other flag
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

    /** One group of unmapped flags and the reason they stay unmapped (J-261). */
    private record Why(java.util.function.Predicate<String> matches, String reason) {}

    private static Why why(String prefixes, String reason) {
        List<String> ps = List.of(prefixes.split(" "));
        return new Why(f -> ps.stream().anyMatch(f::startsWith), reason);
    }

    /** In order: the first group whose prefix a flag starts with names the reason. */
    private static final List<Why> WHY = List.of(
            why("-std=", "another dialect's expected output"),
            why("-free --free", "a source format the dialect does not have"),
            why("-fnotrunc -fno-trunc -fno-binary-truncate -fbinary-comp-1", "changes how binary items truncate or size, against the profile's binary-truncate=yes; a dialect decision"),
            why("-fsign=", "changes the DISPLAY sign form, which the profile fixes (ibm.dispsign); a dialect decision"),
            why("-fdefault-file-colseq= -farithmetic-osvs -fmove-ibm -fodoslide -fcomplex-odo -fconstant-folding -fnot-intrinsic= -fno-move-non-numeric-lit",
                    "changes what the program computes, orders or moves; the profile does not say it, so a dialect decision"),
            why("-frelax-syntax-checks -fcomment-paragraphs= -fmissing-statement= -fdefine-constant-directive= -fhp-octal-literals= -facu-literal -faccept-display-extensions= -fpartial-replace-when-literal-src= -fno-program-name-redefinition -freserved= -fformat= -fno-areacheck -fcontrol-division= -fdpc-in-data=",
                    "changes what the front end accepts or how it reads the source; the profile does not say it, so a dialect decision"),
            why("-fno-ec= -debug -fdump= -ftraceall -fprof -fremove-unreachable", "runtime checks, dumps, traces or profiling: output or checks Katana's runtime does not produce"),
            why("-fcallfh= -fsystem-name=", "names a file handler or a system-name mapping of the cobc runtime; Katana's runtime has neither"));

    /**
     * Why a flag the census does not map stays unmapped, or null when no reason is given (J-256, J-261): every flag left has one,
     * and `CompileFlagsTest` holds that the golden's flags all do.
     */
    // Decision: R-083
    public static String reason(String flag) {
        for (Why w : WHY) if (w.matches().test(flag)) return w.reason();
        return null;
    }

    /** Flags that name a file, ask for warnings or diagnostics, or tune the C compile: none changes what the program prints. */
    private static boolean noEffect(String f) {
        return f.equals("-o") || f.equals("-w") || f.equals("-g") || f.equals("-v") || f.startsWith("-W") || f.startsWith("-O") || f.equals("-save-temps") || f.startsWith("-save-temps=") || listing(f);
    }

    /** A listing file (`-t`, and the lines it leaves out) and the text of cobc's diagnostics: output of the compile, not of the program. */
    private static boolean listing(String f) {
        return f.equals("-t") || f.equals("-fno-theaders") || f.equals("-fno-tmessages") || f.equals("-fno-tsource") || f.equals("-ftsymbols")
                || f.equals("-fdiagnostics-show-option") || f.equals("-fno-diagnostics-show-option");
    }
}
