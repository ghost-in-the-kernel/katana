package katana.intent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import katana.front.Copybooks;
import katana.front.Parse;
import katana.intent.types.Evidence;
import katana.intent.types.Fact;
import katana.intent.types.Intent;
import katana.intent.types.Invalid;
import katana.intent.types.Kind;
import katana.intent.types.Node;
import katana.intent.types.State;
import katana.intent.types.Subject;
import katana.profile.Profile;
import katana.sir.build.Build;
import katana.sir.types.Item;
import katana.sir.types.Procedure;
import katana.sir.types.Program;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * The trial of the Intent IR's shapes on a real program (research §28): the facts a person writes for
 * CardDemo's CBTRN02C by reading it, by name, are accepted against its IR, and refused with a node moved.
 */
@Tag("integration")
class Cbtrn02cIntentIntegrationTest {
    private static final Path ROOT = Path.of(System.getProperty("katana.root", "."));
    private static final Path CARDDEMO = ROOT.resolve("corpus/carddemo/app");

    private static Program program() throws Exception {
        Profile ibm = Profile.read(Files.readString(ROOT.resolve("profile/ibm.profile")));
        Parse.Result parse = Parse.file(CARDDEMO.resolve("cbl/CBTRN02C.cbl"), new Copybooks(List.of(CARDDEMO.resolve("cpy")), n -> 8), 8);
        return Build.program(parse, ibm.hash()).program();
    }

    private static int item(Program p, String name) {
        List<Item> named = p.items().stream().filter(i -> i.name().equals(name)).toList();
        assertEquals(1, named.size(), name);
        return named.get(0).id();
    }

    private static int paragraph(Program p, String name) {
        return p.procedure().paragraphs().stream().filter(g -> g.name().equals(name)).mapToInt(Procedure.Paragraph::index).findFirst().orElseThrow();
    }

    /** A person's fact, its evidence the nodes it is about. */
    private static Fact fact(Kind kind, Subject subject, Node... read) {
        return new Fact(kind, subject, new Evidence("person", 1, List.of(read)), 1, State.INFERRED);
    }

    private static Fact datum(Program p, Kind kind, String name) {
        return fact(kind, new Subject.Datum(item(p, name)), new Node.ItemNode(item(p, name)));
    }

    private static Fact function(Program p, String name) {
        int g = paragraph(p, name);
        return fact(Kind.FUNCTION, new Subject.Range(g, g), new Node.ParagraphNode(g));
    }

    /** What a person reading CBTRN02C writes: the amounts it ADDs, its result code, its flags, counters and timestamps, two functions, the opens. */
    private static List<Fact> facts(Program p) {
        List<Fact> facts = new ArrayList<>();
        for (String money : List.of("DALYTRAN-AMT", "TRAN-CAT-BAL", "ACCT-CURR-BAL")) facts.add(datum(p, Kind.MONEY, money));
        facts.add(fact(Kind.ENUM, new Subject.Choice(item(p, "APPL-RESULT"), List.of("APPL-AOK", "APPL-EOF")), new Node.ItemNode(item(p, "APPL-RESULT"))));
        for (String flag : List.of("END-OF-FILE", "WS-CREATE-TRANCAT-REC")) facts.add(datum(p, Kind.FLAG, flag));
        for (String counter : List.of("WS-TRANSACTION-COUNT", "WS-REJECT-COUNT")) facts.add(datum(p, Kind.COUNTER, counter));
        for (String date : List.of("COBOL-TS", "DB2-FORMAT-TS")) facts.add(datum(p, Kind.DATE, date));
        facts.add(function(p, "1500-A-LOOKUP-XREF"));
        facts.add(function(p, "9910-DISPLAY-IO-STATUS"));
        List<Integer> opens = new ArrayList<>();
        for (String open : List.of("0000-DALYTRAN-OPEN", "0100-TRANFILE-OPEN", "0200-XREFFILE-OPEN", "0300-DALYREJS-OPEN", "0400-ACCTFILE-OPEN", "0500-TCATBALF-OPEN")) opens.add(paragraph(p, open));
        facts.add(fact(Kind.REDUCIBLE, new Subject.Paragraphs(opens), new Node.ParagraphNode(opens.get(0))));
        return facts;
    }

    /** INT-08's failing case: TWO-BYTES-ALPHA reads TWO-BYTES-BINARY's bytes with no tag, a view, not a union. */
    @Test
    void cbtrn02cUntaggedRedefinesIsNoTaggedUnion() throws Exception {
        Program p = program();
        int binary = item(p, "TWO-BYTES-BINARY"), alpha = item(p, "TWO-BYTES-ALPHA");
        assertEquals(binary, p.items().get(alpha - 1).redefines());
        Invalid e = assertThrows(Invalid.class, () -> new Subject.Union(binary, List.of(alpha), 0));
        assertEquals(true, e.getMessage().startsWith("INT-08: "), e.getMessage());
    }

    @Test
    void cbtrn02cFactsAreAcceptedAgainstItsIrAndRefusedWithANodeMoved() throws Exception {
        Program p = program();
        String hash = p.header().profileHash();
        List<Fact> facts = facts(p);
        new Intent("CBTRN02C", hash, facts).against(p);
        assertEquals(13, facts.size());
        int result = item(p, "APPL-RESULT"), binary = item(p, "TWO-BYTES-BINARY");
        for (Fact moved : List.of(fact(Kind.ENUM, new Subject.Choice(result, List.of("APPL-AOK", "APPL-OK")), new Node.ItemNode(result)),
                fact(Kind.MONEY, new Subject.Datum(p.items().size() + 1), new Node.ItemNode(result)),
                fact(Kind.TAGGED_UNION, new Subject.Union(binary, List.of(result), item(p, "TIMING")), new Node.ItemNode(binary)),
                fact(Kind.FUNCTION, new Subject.Range(0, p.procedure().paragraphs().size()), new Node.ParagraphNode(0)))) {
            List<Fact> with = new ArrayList<>(facts);
            with.add(moved);
            assertThrows(Invalid.class, () -> new Intent("CBTRN02C", hash, with).against(p), moved.id());
        }
    }
}
