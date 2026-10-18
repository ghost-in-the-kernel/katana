package katana.intent;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.intent.review.Review;
import katana.intent.rules.Rules;
import katana.intent.types.Fact;
import katana.intent.types.Subject;
import katana.profile.Profile;
import katana.sir.build.Build;
import katana.sir.types.Item;
import katana.sir.types.Procedure;
import katana.sir.types.Program;
import katana.sir.types.Statement;
import katana.sir.types.Value;

/**
 * R-066's first review file: every fact the rules infer on CardDemo's batch programs, each with its evidence
 * in plain language, the rule's confidence, an empty column for Fable's proposal, and the authors' own
 * documentation quoted or "none", for Ghost to answer. Written once by `BatchReviewIntegrationTest` under
 * `-Dkatana.record=true`, never over a person's word.
 */
final class BatchReview {
    static final Path ROOT = Path.of(System.getProperty("katana.root", "."));
    static final Path CARDDEMO = ROOT.resolve("corpus/carddemo/app");
    static final Path FILE = ROOT.resolve("test/katana/intent/carddemo-batch.review");
    /** The batch programs, CBTRN02C first (the one the trial read), then as CardDemo names them. */
    static final List<String> PROGRAMS = List.of("CBTRN02C.cbl", "CBACT01C.cbl", "CBACT02C.cbl", "CBACT03C.cbl", "CBACT04C.cbl", "CBCUS01C.cbl", "CBEXPORT.cbl",
            "CBIMPORT.cbl", "CBSTM03A.CBL", "CBSTM03B.CBL", "CBTRN01C.cbl", "CBTRN03C.cbl", "COBSWAIT.cbl", "CSUTLDTC.cbl");

    private BatchReview() {}

    /** Each batch program's IR, under the ibm profile, CUSTREC.cpy at the 4 columns its fixture names. */
    static List<Program> programs() throws Exception {
        Profile ibm = Profile.read(Files.readString(ROOT.resolve("profile/ibm.profile")));
        Copybooks books = new Copybooks(List.of(CARDDEMO.resolve("cpy"), CARDDEMO.resolve("cpy-bms"), ROOT.resolve("front/ibm")), n -> n.toUpperCase().startsWith("CUSTREC") ? 4 : 8);
        List<Program> out = new ArrayList<>();
        for (String file : PROGRAMS) out.add(Build.program(Parse.file(CARDDEMO.resolve("cbl/" + file), books, 8), ibm.hash()).program());
        return out;
    }

    /** The whole file: how to read and answer it, then each program's part. */
    static String write(List<Program> programs) throws Exception {
        StringBuilder out = new StringBuilder(HEADER);
        for (Program p : programs) out.append(program(p));
        return out.toString();
    }

    private static final String HEADER = """
            # CardDemo's batch programs: what Katana's rules say each item and block of steps means (R-066).
            #
            # For Ghost. Each fact is one line that starts with `inferred`; under it, four readings:
            #   evidence  what the program does with the item, and where, in the program's own names
            #   rule      the rule that proposed it, and how sure the rule is (its measured precision on
            #             a hand-written key bounds it; the key is Opus's reading, not a person's)
            #   Fable     a proposal only (R-031): a probability and a reason, never a confirmation
            #   docs      the program's authors (AWS's CardDemo README, the copybook and program comments),
            #             quoted where they describe the item, "none" where they do not
            #
            # Your answer goes on the fact line itself, and the next build reads it:
            #   confirmed   change `inferred` to `confirmed` and fill the empty field after the confidence
            #               with your name and the date: `| Ghost 2026-10-18`
            #   refuted     change it to `refuted`, the same name and date, then `| ` and what it is instead
            #   could not tell  leave `inferred`, and after the confidence write `|  | could not tell: `
            #               and what you would have needed; this counts against the evidence line, not you
            # No reading, and no agreement among them, makes a fact confirmed; only your word does (R-029).
            #
            # Words. An item is a named piece of a record or of working storage. A paragraph is a named block
            # of steps; PERFORM runs one as a subroutine and comes back; GO TO jumps without coming back. A
            # condition-name (an 88 level) is a name for one or more values of an item. MOVE copies; ADD adds
            # into; COMPUTE sets from a formula; an edited field is one laid out for printing ($, commas).
            # Kinds: money, an amount of money; enum, an item holding one of several named values; flag, a
            # yes-or-no; function, a block of steps the program runs as a subroutine and that returns.

            """;

    private static String program(Program p) throws Exception {
        String name = p.header().program();
        StringBuilder out = new StringBuilder("# program " + name + "\n");
        out.append("#   docs: ").append(readme(name)).append("\n\n");
        String file = Review.merge(p, Rules.infer(p)).text();
        for (String line : file.lines().toList()) {
            if (line.isBlank() || line.startsWith("#")) continue;
            Fact f = fact(p, line);
            out.append(line).append('\n');
            out.append("#   evidence: ").append(evidence(p, f)).append('\n');
            out.append("#   rule: ").append(f.evidence().rule()).append(" v").append(f.evidence().version()).append(", confidence ").append(line.split("\\s*\\|\\s*")[4]).append('\n');
            out.append("#   Fable (proposal, R-031): ___% because ___\n");
            out.append("#   docs: ").append(docs(p, f)).append("\n\n");
        }
        return out.toString();
    }

    /** The fact a line of the merged file names, in the order the merge wrote them. */
    static Fact fact(Program p, String line) {
        Review.Result one = Review.merge(p, Rules.infer(p), line + "\n");
        String kind = line.split("\\s*\\|\\s*")[1];
        return one.intent().facts().stream().filter(f -> Review.merge(p, new katana.intent.types.Intent(p.header().program(), p.header().profileHash(), List.of(f))).text().contains(line))
                .filter(f -> f.kind().name().equalsIgnoreCase(kind)).findFirst().orElseThrow();
    }

    // ---- evidence, in plain language ----

    /** One statement with the paragraph it is in. */
    record At(String paragraph, Statement s) {}

    static List<At> statements(Program p) {
        List<At> out = new ArrayList<>();
        for (Procedure.Paragraph g : p.procedure().paragraphs()) for (Statement s : g.statements()) add(where(g), s, out);
        return out;
    }

    private static void add(String paragraph, Statement s, List<At> out) {
        out.add(new At(paragraph, s));
        for (Statement.Branch b : s.branches()) for (Statement t : b.statements()) add(paragraph, t, out);
    }

    private static String where(Procedure.Paragraph g) {
        return g.name().isBlank() ? "the program's opening steps" : g.name();
    }

    private static String evidence(Program p, Fact f) {
        return switch (f.subject()) {
            case Subject.Datum d -> f.kind().name().equals("MONEY") ? money(p, d.item()) : flag(p, d.item());
            case Subject.Choice c -> "holds one of " + c.conditions().size() + " named values: " + values(p.items().get(c.item() - 1)) + "; " + touched(p, c.item());
            case Subject.Range r -> function(p, r);
            default -> f.subject().text();
        };
    }

    private static String money(Program p, int item) {
        List<String> said = new ArrayList<>();
        for (At a : statements(p)) {
            Statement s = a.s();
            boolean stored = s.operands().stream().anyMatch(o -> Set.of("add-to", "add-giving", "subtract-minuend", "subtract-giving", "compute-store").contains(o.role()) && refers(o.value(), item));
            if (stored) {
                Set<String> from = names(p, s, item, Set.of("add-from", "subtract-subtrahend", "compute-statement")), also = names(p, s, item, Set.of("add-to", "add-giving", "subtract-minuend", "subtract-giving", "compute-store"));
                String verb = switch (s.verb()) {
                    case "ADD" -> "ADD adds " + String.join(", ", from) + " into it";
                    case "SUBTRACT" -> "SUBTRACT takes " + String.join(", ", from) + " from it";
                    default -> "COMPUTE sets it by a formula from " + String.join(", ", from);
                };
                if (!from.isEmpty() || !also.isEmpty()) said.add(verb + (also.isEmpty() ? "" : " (and into " + String.join(", ", also) + ")") + " in " + a.paragraph());
            }
            if (s.verb().equals("MOVE") && s.operands().stream().anyMatch(o -> o.role().equals("move-to-sending-area") && refers(o.value(), item))) {
                for (Statement.Operand o : s.operands()) {
                    if (!o.role().equals("move-to-statement") || !(o.value() instanceof Value.Field t)) continue;
                    Item to = p.items().get(t.item() - 1);
                    if (to.attributes().category() == katana.sir.types.Attributes.Category.NUMERIC_EDITED && (to.attributes().picture().contains(",") || to.attributes().picture().contains(p.header().currency()))) {
                        said.add("MOVE copies it to " + to.name() + ", a field printed as " + to.attributes().picture() + ", in " + a.paragraph());
                    }
                }
            }
        }
        return a2s(said) + " (" + kind(p.items().get(item - 1)) + ")";
    }

    /** The two-place items other than this one a statement's operands of these roles name. */
    private static Set<String> names(Program p, Statement s, int item, Set<String> roles) {
        Set<String> out = new LinkedHashSet<>();
        for (Statement.Operand o : s.operands()) {
            if (!roles.contains(o.role())) continue;
            for (Item i : p.items()) if (i.id() != item && i.attributes().scale() == 2 && refers(o.value(), i.id())) out.add(i.name());
        }
        return out;
    }

    private static String flag(Program p, int item) {
        Item i = p.items().get(item - 1);
        return "a yes-or-no: " + values(i) + "; " + touched(p, item) + " (" + kind(i) + ")";
    }

    /** Where the program sets the item, where it checks it, and what it hands it to. */
    private static String touched(Program p, int item) {
        return String.join("; ", setting(p, item)) + "; checked in " + list(checked(p, item)) + handed(p, item);
    }

    /** The paragraphs that MOVE into the item or SET one of its condition-names, and who else sets it: the file system, a caller. */
    private static List<String> setting(Program p, int item) {
        Set<String> set = new LinkedHashSet<>();
        for (At a : statements(p)) {
            Statement s = a.s();
            boolean moves = s.verb().equals("MOVE") && s.operands().stream().anyMatch(o -> o.role().equals("move-to-statement") && refers(o.value(), item));
            boolean sets = s.verb().equals("SET") && s.operands().stream().anyMatch(o -> o.value() instanceof Value.ConditionName c && c.item() == item);
            if (moves || sets) set.add(a.paragraph());
        }
        List<String> setting = new ArrayList<>();
        if (!set.isEmpty()) setting.add("set in " + list(set));
        for (katana.sir.types.FileDescriptor d : p.files()) if (d.status() == item) setting.add("set by the file system after each operation on " + d.name() + " (its FILE STATUS)");
        if (p.items().get(item - 1).section() == Item.Section.LINKAGE) setting.add("set by the program that CALLs this one (it is in the LINKAGE SECTION)");
        if (setting.isEmpty()) setting.add("set nowhere in this program");
        return setting;
    }

    /** The paragraphs whose IF, EVALUATE, PERFORM UNTIL or SEARCH, or a branch's condition, reads the item. */
    private static Set<String> checked(Program p, int item) {
        Set<String> checked = new LinkedHashSet<>();
        for (At a : statements(p)) {
            Statement s = a.s();
            for (Statement.Branch b : s.branches()) if (b.condition() != null && refers(b.condition(), item)) checked.add(a.paragraph());
            if (Set.of("IF", "EVALUATE", "PERFORM", "SEARCH").contains(s.verb()) && s.operands().stream().anyMatch(o -> refers(o.value(), item))) checked.add(a.paragraph());
        }
        return checked;
    }

    /** Each CALL that hands the item, or a group holding it, to another program. */
    private static String handed(Program p, int item) {
        Set<String> called = new LinkedHashSet<>();
        for (At a : statements(p)) {
            if (!a.s().verb().equals("CALL")) continue;
            for (int at = item; at > 0; at = p.items().get(at - 1).parent()) {
                final int held = at;
                if (a.s().operands().stream().anyMatch(o -> refers(o.value(), held))) called.add(callee(a.s()) + " in " + a.paragraph() + (held == item ? "" : " (inside " + p.items().get(held - 1).name() + ")"));
            }
        }
        return called.isEmpty() ? "" : "; handed to another program by CALL " + list(called);
    }

    private static String callee(Statement s) {
        return s.operands().stream().filter(o -> o.value() instanceof Value.Literal).map(o -> ((Value.Literal) o.value()).payload()).findFirst().orElse("a program named at run time");
    }

    private static String function(Program p, Subject.Range r) {
        String first = p.procedure().paragraphs().get(r.first()).name(), last = p.procedure().paragraphs().get(r.last()).name();
        Set<String> callers = new LinkedHashSet<>();
        for (Procedure.Edge e : p.procedure().edges()) if (e.kind() == Procedure.Kind.PERFORM && e.to() == r.first() && e.thru() == r.last()) callers.add(where(p.procedure().paragraphs().get(e.from())));
        int steps = 0;
        Set<String> verbs = new LinkedHashSet<>();
        for (int g = r.first(); g <= r.last(); g++) for (Statement s : p.procedure().paragraphs().get(g).statements()) {
            steps++;
            verbs.add(s.verb());
        }
        return "the block " + first + (r.first() == r.last() ? "" : " through " + last) + " (" + steps + " steps: " + String.join(", ", verbs) + ") is run by PERFORM from " + list(callers)
                + " and returns; nothing jumps into it or out of it with GO TO";
    }

    private static String values(Item i) {
        List<String> out = new ArrayList<>();
        for (Item.Condition c : i.conditions()) out.add(c.name() + " = " + String.join(" or ", c.values().stream().map(BatchReview::value).toList()));
        return String.join(", ", out);
    }

    private static String value(Value v) {
        if (v instanceof Value.Literal l) return l.kind() == Value.Literal.Kind.NUMERIC ? l.payload() : "'" + l.payload() + "'";
        if (v instanceof Value.Figurative f) return f.name().toLowerCase().replace('-', ' ');
        if (v instanceof Value.Expression e && e.operator().equals("THRU")) return value(e.operands().get(0)) + " to " + value(e.operands().get(1));
        return String.valueOf(v);
    }

    private static String kind(Item i) {
        var a = i.attributes();
        return "it is " + (a.category() == katana.sir.types.Attributes.Category.NUMERIC ? "a number of " + a.digits() + " digits, " + a.scale() + " after the point" + (a.signed() ? ", signed" : "") : "text of " + i.length() + " characters")
                + ", picture " + a.picture() + ", declared in " + file(i.origin().file());
    }

    private static String list(Set<String> s) {
        return s.isEmpty() ? "none" : s.size() <= 4 ? String.join(", ", s) : String.join(", ", new ArrayList<>(s).subList(0, 4)) + " and " + (s.size() - 4) + " more";
    }

    private static String a2s(List<String> said) {
        return said.size() <= 3 ? String.join("; ", said) : String.join("; ", said.subList(0, 3)) + "; and " + (said.size() - 3) + " more such";
    }

    static boolean refers(Value v, int item) {
        if (v instanceof Value.Field f) return f.item() == item || f.subscripts().stream().anyMatch(s -> refers(s, item));
        if (v instanceof Value.ConditionName c) return c.item() == item;
        if (v instanceof Value.Expression e) return e.operands().stream().anyMatch(o -> refers(o, item));
        return false;
    }

    private static String file(String path) {
        return path.substring(path.lastIndexOf('/') + 1);
    }

    // ---- the authors' documentation ----

    /** The README's line for the program, quoted, or none. */
    private static String readme(String program) throws Exception {
        for (String line : Files.readAllLines(CARDDEMO.getParent().resolve("README.md"))) {
            if (line.contains("| " + program + " ")) return "README, its jobs table: \"" + String.join(", ", java.util.Arrays.stream(line.split("\\|")).map(String::strip).filter(c -> !c.isEmpty()).toList()) + "\"";
        }
        return "none";
    }

    /** The comment block right above the item's or paragraph's declaration, or above its record, quoted; none where there is none. */
    private static String docs(Program p, Fact f) throws Exception {
        katana.sir.types.Origin at;
        String about;
        switch (f.subject()) {
            case Subject.Datum d -> { at = p.items().get(d.item() - 1).origin(); about = p.items().get(d.item() - 1).record() == d.item() ? null : "its record, " + p.items().get(p.items().get(d.item() - 1).record() - 1).name(); }
            case Subject.Choice c -> { at = p.items().get(c.item() - 1).origin(); about = null; }
            case Subject.Range r -> { at = p.procedure().paragraphs().get(r.first()).origin(); about = null; }
            default -> { return "none"; }
        }
        String own = comment(at.file(), at.line());
        if (own != null) return own;
        if (about != null && f.subject() instanceof Subject.Datum d) {
            Item record = p.items().get(p.items().get(d.item() - 1).record() - 1);
            String theirs = comment(record.origin().file(), record.origin().line());
            if (theirs != null) return "of " + about + ": " + theirs;
        }
        return "none";
    }

    /** The comment lines (an asterisk in column 7) directly above a line, quoted with their file and line, or null. */
    private static String comment(String file, int line) throws Exception {
        Path path = Path.of(file);
        if (!Files.exists(path)) path = CARDDEMO.resolve("cpy").resolve(file(file));
        if (!Files.exists(path)) return null;
        List<String> lines = Files.readAllLines(path, java.nio.charset.StandardCharsets.ISO_8859_1);
        List<String> said = new ArrayList<>();
        for (int k = line - 2; k >= 0 && lines.get(k).length() > 6 && lines.get(k).charAt(6) == '*'; k--) {
            String text = lines.get(k).substring(7, Math.min(72, lines.get(k).length())).replaceAll("[*-]{3,}", "").replaceAll("\\s*\\*+\\s*$", "").replace("|", "/").strip();
            if (!text.isEmpty()) said.add(0, text);
        }
        return said.isEmpty() ? null : "\"" + String.join(" ", said) + "\" (" + file(file) + " line " + (line - said.size()) + ")";
    }
}
