package katana.intent;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.intent.rules.Rules;
import katana.intent.types.Fact;
import katana.intent.types.Subject;
import katana.profile.Profile;
import katana.sir.build.Build;
import katana.sir.types.Program;

/** The built review tests (research §33, §34), each by its catalogue id, and each one's verdict on a fact of the review file. */
final class ReviewTests {
    /** The tests built, and the test class whose killer mutation holds each. */
    static final Map<String, String> BUILT = Map.of("T-F1", "FunctionRangeStructureTest", "T-E1", "EnumValuesStoredTest", "T-E2", "EnumIsReadTest", "T-M1", "MoneyFlowTest", "T-M2", "MoneyEditedElsewhereTest",
            "T-E3", "FileStatusAbsentIntegrationTest", "T-F2", "FunctionRangeXrefTest", "T-F3", "PerformTracesTest");
    private static Corpus corpus;
    private static final Map<String, FunctionRangeXref.Listing> LISTINGS = new java.util.HashMap<>();
    /** Each program's function ranges, inferred once: every range's T-F1 verdict is read against them all. */
    private static final Map<String, List<FunctionRangeStructure.Range>> RANGES = new java.util.HashMap<>();

    private ReviewTests() {}

    /** CardDemo's programs, online and batch, each parsed the first time a test asks for it: where T-E1 and T-E2 follow a CALL and T-M2 looks for a copybook's amount printed. */
    static synchronized Corpus corpus() throws Exception {
        if (corpus != null) return corpus;
        Profile ibm = Profile.read(Files.readString(BatchReview.ROOT.resolve("profile/ibm.profile")));
        Copybooks books = new Copybooks(List.of(BatchReview.CARDDEMO.resolve("cpy"), BatchReview.CARDDEMO.resolve("cpy-bms"), BatchReview.ROOT.resolve("front/ibm")), n -> n.toUpperCase().startsWith("CUSTREC") ? 4 : 8);
        Map<String, Path> files = new LinkedHashMap<>();
        for (Path source : katana.sir.IrCorpus.programs(BatchReview.CARDDEMO.resolve("cbl")).values()) files.put(source.getFileName().toString().replaceFirst("\\.[^.]*$", "").toUpperCase(), source);
        Map<String, Program> parsed = new java.util.HashMap<>();
        java.util.function.Function<String, Program> parse = name -> parsed.computeIfAbsent(name, n -> {
            try {
                return Build.programs(Parse.file(files.get(n), books, 8), ibm.hash()).get(0).program();
            } catch (Exception e) {
                throw new IllegalStateException(n, e);
            }
        });
        return corpus = new Corpus() {
            public synchronized Program get(String program) {
                return files.containsKey(program) ? parse.apply(program) : null;
            }

            public synchronized List<Program> mentioning(String word) {
                java.util.regex.Pattern named = java.util.regex.Pattern.compile("(?<![A-Z0-9-])" + java.util.regex.Pattern.quote(word) + "(?![A-Z0-9-])");
                List<Program> out = new ArrayList<>();
                for (Map.Entry<String, Path> f : files.entrySet()) {
                    try {
                        if (named.matcher(Files.readString(f.getValue(), java.nio.charset.StandardCharsets.ISO_8859_1).toUpperCase()).find()) out.add(parse.apply(f.getKey()));
                    } catch (java.io.IOException e) {
                        throw new java.io.UncheckedIOException(e);
                    }
                }
                return out;
            }
        };
    }

    /** The verdict of each built test the proposal names on the fact, in the order it names them. */
    static List<Verdict> verdicts(Program p, Fact f, String named) throws Exception {
        List<Verdict> out = new ArrayList<>();
        for (String id : named.split(",\\s*")) {
            Verdict v = verdict(p, f, id.strip());
            if (v != null) out.add(v);
        }
        return out;
    }

    private static Verdict verdict(Program p, Fact f, String id) throws Exception {
        return switch (f.subject()) {
            case Subject.Choice c when id.equals(EnumValuesStored.TEST) -> EnumValuesStored.verdict(p, c.item(), corpus());
            case Subject.Choice c when id.equals(EnumIsRead.TEST) -> EnumIsRead.verdict(p, c.item(), corpus());
            case Subject.Choice c when id.equals(FileStatusAbsent.TEST) -> FileStatusAbsent.verdict(p, c.item());
            case Subject.Datum d when id.equals(MoneyFlow.TEST) -> MoneyFlow.verdict(p, d.item());
            case Subject.Datum d when id.equals(MoneyEditedElsewhere.TEST) -> MoneyEditedElsewhere.verdict(p, d.item(), corpus());
            case Subject.Range r when id.equals("T-F1") -> structure(p, r);
            case Subject.Range r when id.equals("T-F2") -> xref(p, r);
            case Subject.Range r when id.equals(PerformTraces.TEST) -> PerformTraces.verdict(p, r);
            default -> null;
        };
    }

    /** T-F2's word: the range on cobc's cross-reference listing of the program. */
    private static Verdict xref(Program p, Subject.Range r) throws Exception {
        String name = p.header().program();
        if (!LISTINGS.containsKey(name)) {
            String file = BatchReview.PROGRAMS.stream().filter(f -> f.toUpperCase().startsWith(name + ".")).findFirst().orElseThrow();
            LISTINGS.put(name, FunctionRangeXref.parse(FunctionRangeXref.listing(BatchReview.CARDDEMO.resolve("cbl/" + file))));
        }
        List<String> why = FunctionRangeXref.refusals(LISTINGS.get(name), p, new FunctionRangeStructure.Range(r.first(), r.last()));
        return why.isEmpty() ? Verdict.holds("T-F2", "on cobc's own cross-reference listing every reference to its first paragraph is a PERFORM of the whole block, nothing outside names a paragraph inside, and no GO TO inside names one outside") : Verdict.refutes("T-F2", String.join("; ", why));
    }

    /** T-F1's word: the range against every other function range the rules infer on the program. */
    private static Verdict structure(Program p, Subject.Range r) {
        List<FunctionRangeStructure.Range> others = RANGES.computeIfAbsent(p.header().program(), k -> {
            List<FunctionRangeStructure.Range> all = new ArrayList<>();
            for (Fact g : Rules.infer(p).facts()) if (g.subject() instanceof Subject.Range o) all.add(new FunctionRangeStructure.Range(o.first(), o.last()));
            return all;
        });
        List<String> why = FunctionRangeStructure.refusals(p, new FunctionRangeStructure.Range(r.first(), r.last()), others);
        return why.isEmpty() ? Verdict.holds("T-F1", "on sir/'s paragraph graph only a PERFORM of the whole block enters it, it leaves only at its end, and the main line does not fall into it") : Verdict.refutes("T-F1", String.join("; ", why));
    }
}
