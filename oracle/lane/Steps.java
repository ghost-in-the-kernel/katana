package katana.oracle.lane;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import katana.oracle.fixture.Content;
import katana.oracle.fixture.Fixture;

/**
 * A fixture's steps in order, as JCL runs them: a step is bypassed when its COND holds against a
 * return code before it or an earlier step abended, and nothing runs after a finding. Each step's
 * DDs are allocated by disposition and bound, it runs, and its dispositions are applied (the
 * abnormal one after an abend).
 */
final class Steps {
    /** What a step's DDs are bound to: environment variables, each dataset DD's dataset, its stdin, or why it cannot run. */
    record Bound(Map<String, String> env, Map<String, String> dsns, Map<String, List<String>> inputs, List<String> created, Path stdin, String finding) {}

    /** How a step ended: its outcome line, its return code, whether it abended, and a finding that stops the job, or null. */
    record Outcome(String line, int rc, boolean abend, String finding) {}

    private static final Set<String> SORTS = Set.of("SORT", "DFSORT", "ICEMAN", "ICETOOL", "SYNCSORT");
    private final Fixture fixture;
    private final Datasets datasets;
    private final Programs programs;
    private final Map<String, String> paths;
    private final Path work;
    private final Report report;
    private final List<Integer> rcs = new ArrayList<>();

    Steps(Fixture fixture, Datasets datasets, Programs programs, Map<String, String> paths, Path work, Report report) {
        this.fixture = fixture;
        this.datasets = datasets;
        this.programs = programs;
        this.paths = paths;
        this.work = work;
        this.report = report;
    }

    /** Every step in order; `stopped` when the job cannot start, so each is reported as not run. */
    void run(boolean stopped) {
        boolean abended = false;
        for (Fixture.Step step : fixture.steps()) {
            String pgm = step.line().attributes().get("pgm"), cond = step.line().attributes().get("cond");
            if (stopped) report.step(step.name(), pgm + " not run: the job stopped before it");
            else if (abended) report.step(step.name(), pgm + " bypassed: an earlier step abended");
            else if (cond != null && bypassed(cond)) report.step(step.name(), pgm + " bypassed by COND=(" + cond + ")");
            else {
                Outcome outcome = execute(step, pgm);
                report.step(step.name(), outcome.line());
                if (outcome.finding() != null) report.finding("step " + step.name() + ": " + outcome.finding());
                stopped = outcome.finding() != null;
                abended = outcome.abend();
                rcs.add(outcome.rc());
            }
        }
    }

    /** The fixture's DFSORT PAD and TRUNC (R-051), RC0 where it states none. */
    private katana.oracle.sort.Dfsort.Rules rules() {
        return new katana.oracle.sort.Dfsort.Rules(header("dfsort.pad"), header("dfsort.trunc"));
    }

    private String header(String keyword) {
        var line = fixture.first(keyword);
        return line == null ? "RC0" : line.values().get(0);
    }

    /** COND=(code,op): the step is bypassed when `code op rc` holds for any earlier return code. */
    private boolean bypassed(String cond) {
        String[] c = cond.split(",");
        int code = Integer.parseInt(c[0]);
        return rcs.stream().anyMatch(rc -> switch (c[1]) {
            case "GT" -> code > rc;
            case "GE" -> code >= rc;
            case "EQ" -> code == rc;
            case "NE" -> code != rc;
            case "LT" -> code < rc;
            default -> code <= rc;
        });
    }

    /** Allocates, runs by kind (utility, SORT, program), and applies the dispositions. */
    private Outcome execute(Fixture.Step step, String pgm) {
        Bound bound = bind(step);
        if (bound.finding() != null) {
            bound.created().forEach(datasets::delete);
            return new Outcome(pgm + " not run", 0, false, bound.finding());
        }
        Outcome outcome;
        if (SORTS.contains(pgm)) outcome = Sorts.run(step, bound, datasets, report, rules());
        else if (pgm.equals("IEFBR14")) outcome = new Outcome("IEFBR14 rc=0", 0, false, null);
        else if (pgm.equals("IDCAMS")) outcome = idcams(step, bound);
        else if (pgm.equals("IEBGENER")) outcome = Utilities.iebgener(step, bound.dsns(), datasets);
        else outcome = program(step, pgm, bound);
        // A step the lane could not finish leaves no dataset it made: what it would have written is unknown.
        if (outcome.finding() != null) bound.created().forEach(datasets::delete);
        else dispose(step, outcome.abend());
        return outcome;
    }

    private Outcome idcams(Fixture.Step step, Bound bound) {
        Idcams.Result result = new Idcams(datasets, bound.dsns()).run(Utilities.sysin(step, bound.dsns(), datasets));
        result.lines().forEach(l -> report.line("idcams " + step.name() + " " + l));
        return new Outcome("IDCAMS rc=" + result.rc(), result.rc(), false, result.finding());
    }

    private Outcome program(Fixture.Step step, String pgm, Bound bound) {
        for (var input : bound.inputs().entrySet()) {
            if (input.getValue().size() > 1) return new Outcome(pgm + " not run", 0, false, "dd " + input.getKey() + " is concatenated; a program reading one is not in the lane yet");
        }
        Programs.Ended ended = programs.run(step.name(), pgm, step.line().attributes().get("parm"), bound.env(), bound.stdin());
        if (ended.finding() != null) return new Outcome(pgm + " not run", 0, false, ended.finding());
        String stdout = Programs.text(work.resolve("sysout/" + step.name() + ".stdout"));
        report.line("stdout " + step.name() + " lines=" + stdout.lines().count() + " " + Content.sha256(stdout.getBytes(StandardCharsets.ISO_8859_1)));
        String stderr = Programs.text(work.resolve("sysout/" + step.name() + ".stderr"));
        if (!stderr.isBlank()) report.line("stderr " + step.name() + " lines=" + stderr.lines().count() + " first: " + stderr.lines().findFirst().orElse("").replace(work.toString(), "<work>"));
        String line = pgm + (ended.abend() != null ? " abend " + ended.abend() + " rc=" + ended.rc() : " rc=" + ended.rc());
        return new Outcome(line, ended.rc(), ended.abend() != null, null);
    }

    /** Each DD bound: sysout to a file, dummy to /dev/null, inline cards to a file (and stdin for SYSIN), a dataset by its disposition. */
    private Bound bind(Fixture.Step step) {
        Map<String, String> env = new HashMap<>(Map.of("COB_CURRENT_DATE", fixture.first("clock").values().get(0).replaceAll("\\s+", " "),
                "TZ", fixture.first("tz").values().get(0)));
        Map<String, String> dsns = new HashMap<>();
        Map<String, List<String>> inputs = new HashMap<>();
        List<String> created = new ArrayList<>();
        Path stdin = Path.of("/dev/null");
        for (Fixture.Dd dd : step.dds()) {
            Path file = work.resolve((dd.line().words().contains("inline") ? "inline/" : "sysout/") + step.name() + "." + dd.name());
            if (dd.line().words().contains("dummy")) env.put("DD_" + dd.name(), "/dev/null");
            else if (dd.line().words().contains("sysout")) env.put("DD_" + dd.name(), write(file, new byte[0]).toString());
            else if (dd.line().words().contains("inline")) {
                env.put("DD_" + dd.name(), write(file, Utilities.cards(dd.cards())).toString());
                if (dd.name().equals("SYSIN")) stdin = write(Path.of(file + ".lines"), (String.join("\n", dd.cards()) + "\n").getBytes(StandardCharsets.ISO_8859_1));
            } else {
                String finding = allocate(dd, env, dsns, inputs, created);
                if (finding != null) return new Bound(env, dsns, inputs, created, stdin, finding);
            }
        }
        return new Bound(env, dsns, inputs, created, stdin, null);
    }

    /** A dataset DD: NEW makes it (and refuses one that exists), MOD makes it if absent, SHR and OLD need it; a concat one joins the DD's inputs. */
    private String allocate(Fixture.Dd dd, Map<String, String> env, Map<String, String> dsns, Map<String, List<String>> inputs, List<String> created) {
        String dsn = paths.getOrDefault(dd.attributes().get("dsn"), dd.attributes().get("dsn"));
        String status = dd.attributes().get("disp").split(",")[0];
        Datasets.Entry entry = datasets.get(dsn);
        if (entry != null && status.equals("NEW")) return "dd " + dd.name() + ": NEW, but " + entry.dsn() + " exists";
        if (entry == null && (status.equals("SHR") || status.equals("OLD"))) return "dd " + dd.name() + ": " + datasets.absolute(dsn) + " does not exist";
        if (entry == null) {
            entry = datasets.sequential(dsn, Integer.parseInt(dd.attributes().get("lrecl")), new byte[0]);
            created.add(entry.dsn());
        }
        inputs.computeIfAbsent(dd.name(), n -> new ArrayList<>()).add(entry.dsn());
        if (dd.line().words().contains("concat")) return null;
        env.put("DD_" + dd.name(), entry.path().toString());
        dsns.put(dd.name(), entry.dsn());
        return null;
    }

    /** The normal disposition, or the abnormal one after an abend; DELETE removes the dataset. NEW's default is DELETE. */
    private void dispose(Fixture.Step step, boolean abend) {
        for (Fixture.Dd dd : step.dds()) {
            if (!dd.attributes().containsKey("disp")) continue;
            String[] disp = dd.attributes().get("disp").split(",");
            String normal = disp.length > 1 ? disp[1] : disp[0].equals("NEW") ? "DELETE" : "KEEP";
            String which = abend && disp.length > 2 ? disp[2] : normal;
            if (which.equals("DELETE")) datasets.delete(paths.getOrDefault(dd.attributes().get("dsn"), dd.attributes().get("dsn")));
        }
    }

    private static Path write(Path file, byte[] bytes) {
        try {
            return Files.write(file, bytes);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
