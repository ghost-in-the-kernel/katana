package katana.front;

import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * What the compile step says that the source does not (R-081): the names its DEFINE compiler option defines,
 * and whether the dialect reads the directives that are not IBM's (`>>ELIF`, `>>D`). `defines` is null when the
 * job declared nothing: a condition on a name the source never `>>DEFINE`s is then an error, because the name's
 * value may have come from outside the source. A declaration, empty or not, makes every name outside it undefined.
 */
public record Settings(Map<String, String> defines, boolean extensions) {
    /** The job declared nothing and the dialect is IBM's: the default of every parse. */
    public static final Settings UNDECLARED = new Settings(null, false);

    public Settings {
        if (defines != null) {
            Map<String, String> upper = new TreeMap<>();
            defines.forEach((k, v) -> upper.put(k.toUpperCase(Locale.ROOT), v));
            defines = Map.copyOf(upper);
        }
    }

    /** The job's compile-time definitions as declared (an empty map says there are none), and the dialect's extensions. */
    public static Settings declared(Map<String, String> defines, boolean extensions) {
        return new Settings(defines, extensions);
    }

    public boolean isDeclared() {
        return defines != null;
    }
}
