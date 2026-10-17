package katana.intent.review;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import katana.intent.types.Fact;
import katana.intent.types.Intent;
import katana.intent.types.State;
import katana.sir.types.Program;

/**
 * A program's review file (docs/ROADMAP.md 5.3, research §30): its facts as plain lines a person edits
 * beside the program, and the merge a rebuild makes of the rules' new facts with the person's words.
 */
public final class Review {
    static final String ORPHANED = "orphaned";

    /** The merged facts, the file to write, and what the merge reports: each line kept as orphaned with why, each fact it could not name. */
    public record Result(Intent intent, String text, List<String> reported) {}

    private Review() {}

    /** A first review file, with no person's word yet. */
    public static Result merge(Program p, Intent inferred) {
        return merge(p, inferred, "");
    }

    /**
     * The rules' facts merged with a review file: a confirmed fact stays confirmed; a refuted one stays refuted
     * while the rule version that refuted it, or an older one, infers it, and comes back inferred under a newer,
     * the refutation kept beside it (R-029); a person's fact, or one a person confirmed or refuted that no rule
     * infers any more, stays; a line that names nothing in the program is kept as orphaned, never rebound.
     */
    // Decision: R-064
    public static Result merge(Program p, Intent inferred, String previous) {
        Map<String, Line> reviewed = new LinkedHashMap<>();
        List<String> orphaned = new ArrayList<>(), why = new ArrayList<>();
        read(p, previous, reviewed, orphaned, why);
        Map<String, Fact> facts = new LinkedHashMap<>();
        Map<String, String> notes = new LinkedHashMap<>();
        for (Fact f : inferred.facts()) {
            Line said = reviewed.remove(f.id());
            facts.put(f.id(), said == null ? f : kept(f, said, notes));
            if (said != null && !said.why().isBlank()) notes.putIfAbsent(f.id(), said.why());
        }
        for (Line said : reviewed.values()) {
            if (said.fact().state().status() == State.Status.INFERRED && !said.fact().evidence().rule().equals("person")) continue;
            facts.put(said.fact().id(), said.fact());
            if (!said.why().isBlank()) notes.put(said.fact().id(), said.why());
        }
        Intent intent = new Intent(p.header().program(), p.header().profileHash(), new ArrayList<>(facts.values()));
        intent.against(p);
        return new Result(intent, write(p, intent, notes, orphaned, why), why);
    }

    /** The person's word on a fact the rules infer again. */
    private static Fact kept(Fact f, Line said, Map<String, String> notes) {
        State was = said.fact().state();
        if (was.status() == State.Status.CONFIRMED) return withState(f, was);
        if (was.status() != State.Status.REFUTED) return f;
        boolean sameRule = said.fact().evidence().rule().equals(f.evidence().rule());
        if (!sameRule || f.evidence().version() <= said.fact().evidence().version()) return withState(f, was);
        notes.put(f.id(), "was refuted by " + was.who() + " " + was.when() + " under " + said.fact().evidence().rule() + " v" + said.fact().evidence().version()
                + (said.why().isBlank() ? "" : ": " + said.why()));
        return f;
    }

    private static Fact withState(Fact f, State s) {
        return new Fact(f.kind(), f.subject(), f.evidence(), f.confidence(), s);
    }

    /** The file's lines: each fact it names, each orphaned line kept as it was, each line that names nothing orphaned with why. */
    private static void read(Program p, String text, Map<String, Line> reviewed, List<String> orphaned, List<String> why) {
        for (String raw : text.lines().toList()) {
            String line = raw.strip();
            if (line.isEmpty() || line.startsWith("#")) continue;
            if (line.startsWith(ORPHANED + Line.SEP.strip())) {
                orphaned.add(line);
                why.add(line + " (kept from an earlier review)");
                continue;
            }
            Line read = Line.read(p, line);
            String problem = read.problem() != null ? read.problem() : reviewed.containsKey(read.fact().id()) ? "a second line for " + read.fact().id() : null;
            if (problem == null) {
                reviewed.put(read.fact().id(), read);
                continue;
            }
            orphaned.add(ORPHANED + Line.SEP + line);
            why.add(line + ": " + problem);
        }
    }

    /** The file: a header saying how to edit it, the facts by kind and name, then the orphaned lines; a fact the program has no name for is said, not written. */
    private static String write(Program p, Intent intent, Map<String, String> notes, List<String> orphaned, List<String> why) {
        StringBuilder out = new StringBuilder("# The review of " + p.header().program() + " under " + p.header().profileHash() + " (docs/INTENT-IR.md).\n"
                + "# state | kind | subject | rule vN | confidence | who date | why. To confirm or refute a fact, change its first word\n"
                + "# to `confirmed` or `refuted` and write who and the date (2026-10-17); a fact of your own is `confirmed | ... | person v1 | 1 | ...`.\n"
                + "# Your word survives a rebuild; a line that names nothing in the program is kept below as orphaned.\n");
        List<String> lines = new ArrayList<>();
        for (Fact f : intent.facts()) {
            String line = Line.write(p, f, notes.get(f.id()));
            if (line != null) lines.add(line);
            else why.add(f.id() + ": the program has no unique name for its subject, so no line can hold a person's word on it");
        }
        lines.sort(Comparator.comparing((String l) -> l.split("\\s*\\|\\s*")[1]).thenComparing(l -> l.split("\\s*\\|\\s*")[2]));
        lines.forEach(l -> out.append(l).append('\n'));
        orphaned.forEach(l -> out.append(l).append('\n'));
        return out.toString();
    }
}
