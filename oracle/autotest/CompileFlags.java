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
 * has; `-std=ibm` and `-std=ibm-strict` are the dialect the lane's oracle runs (R-083) and set nothing; `-farithmetic-osvs`, `-fmove-ibm`, `-fodoslide` (IBM's own, R-088),
 * `-fcomment-paragraphs=ok` and `-faccept-display-extensions=error` (R-089) set nothing too; `-o`, `-w`, `-W...`, `-g`, `-v`, `-O...`, `-save-temps`, the listing (`-t`, `-fno-theaders`, `-fno-tmessages`)
 * and `-fdiagnostics-show-option` leave the program's output as it was. Any other flag
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
            } else if (f.equals("-fixed") || f.equals("-std=ibm") || f.equals("-std=ibm-strict") || ibmsOwn(f) || noEffect(f)) {
                continue;
            } else {
                unmapped.add(f);
            }
        }
        Settings settings = Settings.declared(defines, extensions);
        return new Mapped(debugging ? settings.withDebugging() : settings, List.copyOf(unmapped));
    }

    /** The reason of the flags for a form IBM does not have (R-089). */
    public static final String NO_SUCH_FORM = "a form the dialect does not have";

    /**
     * Whether a flag asks for a form IBM does not have (R-089): a case that passes under it is the front end accepting what IBM rejects, a
     * grammar leak to tighten, never a pass. `-fformat=` is on R-089's list with R-085's reason.
     */
    // Decision: R-089
    public static boolean grammarLeak(String flag) {
        return NO_SUCH_FORM.equals(reason(flag)) || flag.startsWith("-fformat=");
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
            why("-fformat=", "a source format the dialect does not have"),
            why("-fnotrunc -fno-trunc -fno-binary-truncate", "recorded under TRUNC(BIN); the census runs ibm.profile (STD)"),
            why("-fbinary-comp-1", "another dialect's USAGE: COMP-1 is a 16-bit integer there, short floating point in IBM's"),
            why("-fsign=ascii", "not an IBM encoding"),
            why("-fsign=ebcdic -fdefault-file-colseq=", "the oracle lane's byte encoding; IBM has only EBCDIC (ibm.codepage)"),
            why("-fcomplex-odo", "an ODO form not yet sorted against the Language Reference's rules"),
            why("-fconstant-folding -fnot-intrinsic= -fno-move-non-numeric-lit",
                    "changes what the program computes, orders or moves; the profile does not say it, so a dialect decision"),
            why("-frelax-syntax-checks -fcomment-paragraphs= -fmissing-statement= -fdefine-constant-directive= -fhp-octal-literals= -facu-literal -faccept-display-extensions= -fpartial-replace-when-literal-src= -fno-program-name-redefinition -freserved= -fno-areacheck -fcontrol-division= -fdpc-in-data=",
                    NO_SUCH_FORM),
            why("-fno-ec= -debug -fdump= -ftraceall -fprof -fremove-unreachable", "runtime checks, dumps, traces or profiling: output or checks Katana's runtime does not produce"),
            why("-fcallfh= -fsystem-name=", "names a file handler or a system-name mapping of the cobc runtime; Katana's runtime has neither"));

    /**
     * Why a flag the census does not map stays unmapped, or null when no reason is given (J-256, J-261): every flag left has one,
     * and `CompileFlagsTest` holds that the golden's flags all do.
     */
    // Decision: R-083
    // Decision: R-087
    // Decision: R-089
    public static String reason(String flag) {
        for (Why w : WHY) if (w.matches().test(flag)) return w.reason();
        return null;
    }

    /**
     * Flags that name IBM's own behaviour, or an acceptance IBM gives or refuses the same: the census already runs them, so they set nothing and
     * their cases run as is, a FAIL a real difference (R-088, R-089). `-fno-program-name-redefinition` waits for the Language Reference.
     */
    // Decision: R-088
    private static boolean ibmsOwn(String f) {
        return f.equals("-farithmetic-osvs") || f.equals("-fmove-ibm") || f.equals("-fodoslide")
                || f.equals("-fcomment-paragraphs=ok") || f.equals("-faccept-display-extensions=error");
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
