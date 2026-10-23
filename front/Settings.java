package katana.front;

import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * What the compile step says that the source does not (R-081): the names its DEFINE compiler option defines,
 * and whether the dialect reads the directives that are not IBM's (`>>ELIF`, `>>D`). `defines` is null when the
 * job declared nothing: a condition on a name the source never `>>DEFINE`s is then an error, because the name's
 * value may have come from outside the source. A declaration, empty or not, makes every name outside it undefined.
 * `debugging` says the compile step reads a debugging line (an indicator `D` in column 7) as code, whether or not the
 * program says WITH DEBUGGING MODE (a compiler option; the default is the language's rule). A name given with no value
 * (`-D NAME`) is defined with the value {@link #UNKNOWN}, distinct from `""`: a condition on its value is an error (R-084).
 */
public record Settings(Map<String, String> defines, boolean extensions, boolean debugging) {
    /** The job declared nothing and the dialect is IBM's: the default of every parse. */
    public static final Settings UNDECLARED = new Settings(null, false, false);

    /** The value of a name defined with no value (R-084): not `""`, which a source could compare against. */
    public static final String UNKNOWN = "\u0000no value";

    public Settings {
        if (defines != null) {
            Map<String, String> upper = new TreeMap<>();
            defines.forEach((k, v) -> upper.put(k.toUpperCase(Locale.ROOT), v));
            defines = Map.copyOf(upper);
        }
    }

    public Settings(Map<String, String> defines, boolean extensions) {
        this(defines, extensions, false);
    }

    /** The job's compile-time definitions as declared (an empty map says there are none), and the dialect's extensions. */
    public static Settings declared(Map<String, String> defines, boolean extensions) {
        return new Settings(defines, extensions);
    }

    /** The same with the compile step reading debugging lines as code. */
    public Settings withDebugging() {
        return new Settings(defines, extensions, true);
    }

    public boolean isDeclared() {
        return defines != null;
    }
}
