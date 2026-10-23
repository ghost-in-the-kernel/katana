package katana.oracle.lane;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import katana.front.Settings;
import katana.oracle.fixture.Fixture;
import katana.oracle.fixture.Schema;

/**
 * The compile-time definitions a fixture declares with `define` (R-081), the one reading both lanes take: the
 * cobc lane passes them to cobc as `-D NAME=VALUE`; the java lane's compile step is the front end, which takes
 * them as `Settings`. A fixture with no `define` line declares nothing (`Settings.UNDECLARED`'s null), `define -`
 * declares that there are none.
 */
public final class Definitions {
    private Definitions() {}

    /** The names and values the fixture declares, in file order; null when it has no `define` line. */
    public static Map<String, String> of(Fixture fixture) {
        Map<String, String> names = null;
        for (Schema.Line line : fixture.header()) {
            if (!line.keyword().equals("define")) continue;
            if (names == null) names = new LinkedHashMap<>();
            String v = line.values().get(0);
            if (!v.equals("-")) names.put(v.substring(0, v.indexOf('=')), v.substring(v.indexOf('=') + 1));
        }
        return names;
    }

    /** The front end's settings for this fixture: its definitions, and IBM's dialect (no extensions). */
    // Decision: R-081
    public static Settings settings(Fixture fixture) {
        Map<String, String> names = of(fixture);
        return names == null ? Settings.UNDECLARED : Settings.declared(names, false);
    }

    /** cobc's arguments for the declared names: `-D NAME=VALUE` each. */
    static List<String> cobcArguments(Fixture fixture) {
        List<String> out = new ArrayList<>();
        Map<String, String> names = of(fixture);
        if (names != null) names.forEach((k, v) -> out.addAll(List.of("-D", k + "=" + v)));
        return out;
    }
}
