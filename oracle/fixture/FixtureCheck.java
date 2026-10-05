package katana.oracle.fixture;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import katana.profile.Profile;

/**
 * What makes a fixture refused (docs/FIXTURE.md, FIXTURE-03 to FIXTURE-08): an environment that is
 * not pinned, a dataset whose starting state is not stated, a DD that is not one of its four forms,
 * or a step that reads a dataset that does not exist when it starts. An empty list is a fixture a
 * lane may run.
 */
public final class FixtureCheck {
    static final Pattern CLOCK = Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{2} [+-]\\d{2}:\\d{2}");
    private static final Pattern GENERATION = Pattern.compile("(.+)\\(([+-]?\\d+)\\)");
    private static final List<String> ONCE = List.of("job", "profile", "clock", "tz", "dfsort.pad", "dfsort.trunc");
    private static final Set<String> CONTENT = Set.of("from", "sha256", "framing", "over", "unique");

    private FixtureCheck() {}

    /** Every refusal, the schema's first; `profiles` gives a profile's hash by name, or null. */
    public static List<String> refusals(Fixture fixture, Function<String, String> profiles) {
        List<String> refusals = new ArrayList<>(fixture.refusals());
        environment(fixture, profiles, refusals);
        Set<String> steps = new HashSet<>();
        for (Fixture.Step step : fixture.steps()) {
            if (!steps.add(step.name())) refusals.add("step " + step.name() + " twice");
            step.dds().forEach(dd -> form(step, dd, refusals));
        }
        for (Schema.Line dataset : fixture.datasets()) if (dataset.keyword().equals("dataset")) start(dataset, fixture, steps, refusals);
        Catalog.walk(fixture, refusals);
        return refusals;
    }

    /** The profiles under a repository's profile/ folder: a name's hash, or null when there is no such file. */
    public static Function<String, String> profiles(Path root) {
        return name -> {
            Path file = root.resolve("profile").resolve(name + ".profile");
            try {
                return Files.exists(file) ? Profile.read(Files.readString(file)).hash() : null;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }

    /** FIXTURE-03 and FIXTURE-04: the job, its profile by hash, a clock pinned to hundredths and zone, TZ. */
    private static void environment(Fixture fixture, Function<String, String> profiles, List<String> refusals) {
        for (String keyword : ONCE) {
            long count = fixture.header().stream().filter(l -> l.keyword().equals(keyword)).count();
            if (count > 1) refusals.add(keyword + " given " + count + " times");
        }
        if (fixture.first("job") == null) refusals.add("no job line");
        if (fixture.first("tz") == null) refusals.add("no tz line: TZ is the machine's");
        clock(fixture.first("clock"), refusals);
        options(fixture, refusals);
        Schema.Line profile = fixture.first("profile");
        if (profile == null || profile.values().size() < 2) {
            refusals.add("no profile line: the fixture is valid for no profile (P-12)");
            return;
        }
        String name = profile.values().get(0), hash = profile.values().get(1), actual = profiles.apply(name);
        if (actual == null) refusals.add("profile " + name + " does not exist");
        else if (!actual.equals(hash)) refusals.add("profile " + name + " is " + actual + ", the fixture says " + hash);
    }

    /** FIXTURE-09: `dfsort.pad` and `dfsort.trunc` are RC0, RC4 or RC16 (R-051); `tabs` is a width of 1 to 16 and a file-name pattern (R-016). */
    private static void options(Fixture fixture, List<String> refusals) {
        for (Schema.Line line : fixture.header()) {
            List<String> v = line.values();
            boolean dfsort = line.keyword().startsWith("dfsort.");
            if (dfsort && !v.isEmpty() && !v.get(0).matches("RC0|RC4|RC16")) refusals.add("line " + line.number() + ": " + line.keyword() + " is RC0, RC4 or RC16");
            if (line.keyword().equals("tabs") && v.size() == 2 && (!v.get(0).matches("[1-9]|1[0-6]") || !v.get(1).matches("[\\w.*?-]+"))) {
                refusals.add("line " + line.number() + ": tabs is a width of 1 to 16 and a file-name pattern");
            }
        }
    }

    /** FIXTURE-03: an unpinned clock is refused; seconds alone are unpinned, since hundredths leak (F-02). */
    static void clock(Schema.Line clock, List<String> refusals) {
        if (clock == null) {
            refusals.add("unpinned clock: no clock line, so COB_CURRENT_DATE is the machine's");
            return;
        }
        String value = clock.values().get(0).replaceAll("\\s+", " ");
        if (!CLOCK.matcher(value).matches()) {
            refusals.add("unpinned clock: " + value + " is not YYYY-MM-DD HH:MM:SS.hh +HH:MM; without hundredths and"
                    + " zone the live ones leak through (F-02: TRANFILE column 326)");
            return;
        }
        try {
            LocalDateTime.parse(value.substring(0, 22), DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss.SS").withResolverStyle(ResolverStyle.STRICT));
        } catch (DateTimeParseException e) {
            refusals.add("clock " + value + " is not a date");
        }
    }

    /** FIXTURE-05: a DD is sysout, dummy, inline with its cards, or a dataset with all six attributes. */
    private static void form(Fixture.Step step, Fixture.Dd dd, List<String> refusals) {
        String where = "step " + step.name() + " dd " + dd.name();
        Set<String> words = new HashSet<>(dd.line().words());
        if (words.remove("concat") && (dd.attributes().isEmpty() || !concatenates(step, dd))) {
            refusals.add(where + ": concat is a dataset DD after one of the same name");
        } else if (words.size() + (dd.attributes().isEmpty() ? 0 : 1) != 1) {
            refusals.add(where + ": one of sysout, dummy, inline or a dataset");
        } else if (words.contains("inline") && dd.cards().isEmpty()) {
            refusals.add(where + ": inline with no cards");
        } else if (words.isEmpty()) {
            for (String name : Schema.SPECS.get("dd").attributes()) {
                if (!dd.attributes().containsKey(name)) refusals.add(where + ": a dataset DD needs " + name + "= (copybook=- for none)");
            }
        }
    }

    /** Whether the DD before this one in its step has the same name (JCL's unnamed DD, FIXTURE-05). */
    private static boolean concatenates(Fixture.Step step, Fixture.Dd dd) {
        int at = step.dds().indexOf(dd);
        return at > 0 && step.dds().get(at - 1).name().equals(dd.name());
    }

    /** FIXTURE-06: each dataset says how it starts: a GDG base, a path over a KSDS, defined by a step, empty, or content by hash. */
    private static void start(Schema.Line dataset, Fixture fixture, Set<String> steps, List<String> refusals) {
        Map<String, String> a = dataset.attributes();
        String where = "dataset " + dataset.values().get(0);
        String org = a.get("org");
        boolean content = a.keySet().stream().anyMatch(CONTENT::contains) || dataset.words().contains("empty");
        if (org.equals("GDG") && (content || a.containsKey("lrecl") || a.containsKey("keys"))) refusals.add(where + ": a GDG base holds no records; its generations are datasets");
        else if (org.equals("PATH")) path(where, a, fixture, refusals);
        else if (a.containsKey("defined-by") && !steps.contains(a.get("defined-by"))) refusals.add(where + ": no step " + a.get("defined-by"));
        else if (a.containsKey("defined-by") && content) refusals.add(where + ": defined by a step, so it starts with nothing");
        else if (!org.equals("GDG") && !a.containsKey("defined-by")) records(where, dataset, refusals);
        if (org.equals("KSDS") && !keysFit(a)) refusals.add(where + ": a KSDS needs keys=length,offset inside its lrecl");
        existingGeneration(where, dataset, fixture, refusals);
    }

    /** A dataset named as a generation exists at the start: (0) or (-n), sequential, of a declared GDG base. */
    private static void existingGeneration(String where, Schema.Line dataset, Fixture fixture, List<String> refusals) {
        Matcher g = GENERATION.matcher(dataset.values().get(0));
        if (!g.matches()) return;
        if (g.group(2).startsWith("+") || !"PS".equals(dataset.attributes().get("org"))) refusals.add(where + ": an existing generation is (0) or (-n), sequential");
        if (fixture.datasets().stream().noneMatch(d -> d.values().get(0).equals(g.group(1)) && "GDG".equals(d.attributes().get("org")))) {
            refusals.add(where + ": " + g.group(1) + " is not a declared GDG base");
        }
    }

    private static void path(String where, Map<String, String> a, Fixture fixture, List<String> refusals) {
        boolean base = fixture.datasets().stream().anyMatch(d -> d.values().get(0).equals(a.get("over")) && "KSDS".equals(d.attributes().get("org")));
        if (!base || !a.containsKey("keys") || !a.containsKey("unique")) refusals.add(where + ": a PATH needs over= a KSDS, keys= and unique=");
    }

    /** A PS or KSDS that exists at the start: its lrecl, and either empty or a source with its hash and framing. */
    private static void records(String where, Schema.Line dataset, List<String> refusals) {
        Map<String, String> a = dataset.attributes();
        boolean empty = dataset.words().contains("empty"), from = a.containsKey("from");
        if (!a.containsKey("lrecl")) refusals.add(where + ": needs lrecl=");
        if (empty == from) refusals.add(where + ": starts either empty or from= a source with its sha256=");
        if (from && !a.containsKey("sha256")) refusals.add(where + ": from= needs the sha256= of its records");
        if (from && !a.get("from").startsWith("job:") && !a.containsKey("framing")) refusals.add(where + ": a file source needs framing=");
    }

    private static boolean keysFit(Map<String, String> a) {
        if (!a.containsKey("keys") || !a.containsKey("lrecl")) return false;
        String[] k = a.get("keys").split(",");
        return Integer.parseInt(k[0]) + Integer.parseInt(k[1]) <= Integer.parseInt(a.get("lrecl"));
    }

    /** The catalog as the job runs (FIXTURE-07, FIXTURE-08): which datasets exist before each step, and their lrecl. */
    static final class Catalog {
        private final Map<String, String> lrecl = new HashMap<>();
        private final Set<String> generations = new HashSet<>();

        /** The catalog at the start (generation bases, paths at their base's lrecl, datasets not defined by a step), then each step. */
        static void walk(Fixture fixture, List<String> refusals) {
            Catalog c = new Catalog();
            Map<String, String> paths = new HashMap<>();
            for (Schema.Line d : fixture.datasets()) {
                Map<String, String> a = d.attributes();
                if (d.keyword().equals("gold")) continue;
                if ("GDG".equals(a.get("org"))) c.generations.add(d.values().get(0));
                else if ("PATH".equals(a.get("org"))) paths.put(d.values().get(0), a.get("over"));
                else if (!a.containsKey("defined-by")) c.lrecl.put(d.values().get(0), a.getOrDefault("lrecl", "?"));
            }
            paths.forEach((path, over) -> c.lrecl.put(path, c.lrecl.getOrDefault(over, "?")));
            for (Fixture.Step step : fixture.steps()) c.step(step, fixture, refusals);
        }

        /** One step: what it defines, then each dataset DD in order, then what its dispositions delete. */
        private void step(Fixture.Step step, Fixture fixture, List<String> refusals) {
            for (Schema.Line d : fixture.datasets()) {
                if (step.name().equals(d.attributes().get("defined-by"))) lrecl.put(d.values().get(0), d.attributes().getOrDefault("lrecl", "?"));
            }
            List<String> deleted = new ArrayList<>();
            for (Fixture.Dd dd : step.dds()) {
                if (!dd.attributes().containsKey("dsn") || !dd.attributes().containsKey("disp")) continue;
                String where = "step " + step.name() + " dd " + dd.name();
                String dsn = dd.attributes().get("dsn");
                String[] disp = dd.attributes().get("disp").split(",");
                if (generation(where, dsn, refusals)) use(where, dsn, disp[0], dd.attributes().get("lrecl"), refusals);
                if (disp.length > 1 && disp[1].equals("DELETE")) deleted.add(dsn);
            }
            deleted.forEach(lrecl::remove);
        }

        /** A relative generation is of a declared GDG base: (+n) a new one, (0) or (-n) one the fixture declares as existing. */
        private boolean generation(String where, String dsn, List<String> refusals) {
            Matcher m = GENERATION.matcher(dsn);
            if (!m.matches()) return true;
            if (!generations.contains(m.group(1))) refusals.add(where + ": " + m.group(1) + " is not a declared GDG base");
            else if (!m.group(2).startsWith("+") && !lrecl.containsKey(dsn)) refusals.add(where + ": generation " + m.group(2) + " of " + m.group(1) + " is not declared as a dataset");
            else return true;
            return false;
        }

        /** One dataset DD against the catalog: its disposition's status needs or makes the dataset, and its lrecl must agree. */
        private void use(String where, String dsn, String status, String ddLrecl, List<String> refusals) {
            boolean exists = lrecl.containsKey(dsn);
            if (status.equals("NEW") && exists) refusals.add(where + ": NEW, but " + dsn + " already exists");
            if ((status.equals("SHR") || status.equals("OLD")) && !exists) refusals.add(where + ": " + dsn + " does not exist when the step starts");
            String known = lrecl.get(dsn);
            if (exists && !known.equals("?") && ddLrecl != null && !known.equals(ddLrecl)) {
                refusals.add(where + ": lrecl=" + ddLrecl + ", but " + dsn + " has " + known);
            }
            if (!exists) lrecl.put(dsn, ddLrecl == null ? "?" : ddLrecl);
        }
    }
}
