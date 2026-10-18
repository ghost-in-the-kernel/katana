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
    static final Path NOTES = ROOT.resolve("test/katana/intent/carddemo-batch.notes");
    static final Path PROPOSALS = ROOT.resolve("test/katana/intent/carddemo-batch.proposals");
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
            # For Ghost. Each fact is one line that starts with `inferred`; under it, its readings:
            #   evidence  what the program does with the item, and where, in the program's own names
            #   rule      the rule that proposed it, and how sure the rule is (its measured precision on
            #             a hand-written key bounds it; the key is Opus's reading, not a person's)
            #   Fable     a proposal only (R-031): a probability and a reason, never a confirmation. A Fable
            #             advisor's (J-204), a session separate from the Fable that wrote R-066; the percent
            #             is the advisor's on every line; where the line says "reason in Opus's words" the
            #             reason is Opus's, written from the advisor's report (J-209)
            #   T-xx      each built test's verdict (research section 34): holds, refutes or cannot tell, and why;
            #             a test's word is evidence for your answer, never the answer (R-029)
            #   docs      the program's authors (AWS's CardDemo README, the copybook and program comments),
            #             quoted where they describe the item, "none" where they do not
            #
            # Your answer goes on the fact line itself, and the next build reads it. Leave the subject (the
            # names after the kind) as it is; each line is answered on its own, APPL-RESULT once a program.
            #   confirmed   change `inferred` to `confirmed`, and after the confidence append your name and
            #               the date: `| Ghost 2026-10-18`
            #   refuted     change `inferred` to `refuted`, append the same, then `| ` and what it is instead
            #   could not tell  leave `inferred`, and after the confidence append `|  | could not tell: ` (the
            #               name field left empty) and what you would have needed; it counts against the
            #               evidence line, not you
            # No reading, and no agreement among them, makes a fact confirmed; only your word does (R-029).
            #
            # Words. An item is a named piece of a record or of working storage. A paragraph is a named block
            # of steps; PERFORM runs one as a subroutine and comes back; GO TO jumps without coming back. A
            # condition-name (an 88 level) is a name for one or more values of an item. MOVE copies; ADD adds
            # into; COMPUTE sets from a formula; an edited field is one laid out for printing ($, commas).
            # Kinds, as the facts mean them (docs/INTENT-IR.md): money, an amount of money; enum, an item that,
            # whenever the program reads it, holds one of the values its condition-names name (a status code
            # with other values besides is not one); flag, a yes-or-no; function, a block of steps that only a
            # PERFORM enters and that leaves only at its end, with no GO TO in or out.

            """;

    private static String program(Program p) throws Exception {
        String name = p.header().program();
        StringBuilder out = new StringBuilder("# program " + name + "\n");
        for (String said : BatchReviewDocs.programDocs(p)) out.append("#   docs: ").append(said).append('\n');
        String programNote = notes().get(name + " | program | ");
        if (programNote != null) out.append("#   note (document research, J-204): ").append(programNote).append('\n');
        out.append('\n');
        String file = Review.merge(p, Rules.infer(p)).text();
        java.util.Map<String, Fact> byLine = byLine(p);
        for (String line : file.lines().toList()) {
            if (line.isBlank() || line.startsWith("#")) continue;
            Fact f = byLine.get(line);
            out.append(line).append('\n');
            out.append("#   evidence: ").append(BatchReviewEvidence.evidence(p, f)).append('\n');
            out.append("#   rule: ").append(f.evidence().rule()).append(" v").append(f.evidence().version()).append(", confidence ").append(line.split("\\s*\\|\\s*")[4]).append('\n');
            String[] proposal = proposals().get(name + " | " + line.split("\\s*\\|\\s*")[1] + " | " + line.split("\\s*\\|\\s*")[2]);
            out.append("#   Fable (proposal, R-031").append(proposal != null && proposal[3].equals("Opus") ? "; reason in Opus's words" : "").append("): ")
                    .append(proposal == null ? "___% because ___" : proposal[0] + "% because " + sentence(proposal[1])).append('\n');
            if (proposal != null) out.append("#   tests: ").append(tests(proposal[2])).append('\n');
            if (proposal != null) for (Verdict v : ReviewTests.verdicts(p, f, proposal[2])) out.append("#   ").append(v.line()).append('\n');
            out.append("#   docs: ").append(BatchReviewDocs.docs(p, f)).append('\n');
            String[] cells = line.split("\\s*\\|\\s*");
            String note = notes().get(name + " | " + cells[1] + " | " + cells[2]);
            if (note != null) out.append("#   note (document research, J-204): ").append(note).append('\n');
            out.append('\n');
        }
        return out.toString();
    }

    /** A reason after "because": its first letter lowered, unless its first word is a name in capitals (ADD, CYC-CREDIT). */
    private static String sentence(String reason) {
        String first = reason.split("[\\s,:;(]", 2)[0];
        return first.length() > 1 && first.equals(first.toUpperCase()) ? reason : Character.toLowerCase(reason.charAt(0)) + reason.substring(1);
    }

    /** The proposals file, by `PROGRAM | kind | subject`: the percent, the reason, the tests and documents named, and whose words the reason is. */
    static java.util.Map<String, String[]> proposals() {
        java.util.Map<String, String[]> out = new java.util.LinkedHashMap<>();
        try {
            for (String line : Files.readAllLines(PROPOSALS)) {
                if (line.isBlank() || line.startsWith("#")) continue;
                String[] f = line.split(" \\| ", -1);
                out.put(f[0] + " | " + f[1] + " | " + f[2], new String[] {f[3], f[4], f[5], f[6]});
            }
        } catch (java.io.IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
        return out;
    }

    /** Each test or document a proposal names, and whether it is built: a test that holds the fact, one named and not yet built, a document to find. */
    static String tests(String named) {
        List<String> out = new ArrayList<>();
        for (String id : named.split(",\\s*")) {
            if (id.isBlank()) continue;
            out.add(id + (ReviewTests.BUILT.containsKey(id) ? " (built, " + ReviewTests.BUILT.get(id) + ": its verdict below)" : id.startsWith("D-") ? " (a document to find)" : " (named, not yet built)"));
        }
        return out.isEmpty() ? "none named" : String.join(", ", out) + "; each in docs/research/ibm-options.md sections 33 and 34";
    }

    /** The notes file, by `PROGRAM | kind | subject`. */
    static java.util.Map<String, String> notes() {
        java.util.Map<String, String> out = new java.util.LinkedHashMap<>();
        try {
            for (String line : Files.readAllLines(NOTES)) {
                if (line.isBlank() || line.startsWith("#")) continue;
                String[] f = line.split("\\s*\\|\\s*", 4);
                out.put(f[0].strip() + " | " + f[1].strip() + " | " + f[2].strip(), f[3].strip());
            }
        } catch (java.io.IOException e) {
            throw new java.io.UncheckedIOException(e);
        }
        return out;
    }

    /** Each fact the rules infer on the program, by the line the merge writes for it alone: one merge a fact. */
    static java.util.Map<String, Fact> byLine(Program p) {
        java.util.Map<String, Fact> out = new java.util.HashMap<>();
        for (Fact f : Rules.infer(p).facts())
            for (String line : Review.merge(p, new katana.intent.types.Intent(p.header().program(), p.header().profileHash(), List.of(f))).text().lines().toList())
                if (!line.isBlank() && !line.startsWith("#")) out.put(line, f);
        return out;
    }

    static String file(String path) {
        return path.substring(path.lastIndexOf('/') + 1);
    }
}
