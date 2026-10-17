package katana.intent.types;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import katana.sir.types.Item;
import katana.sir.types.Procedure;
import katana.sir.types.Program;

/** One program's Intent IR (R-007): its facts, under the profile its Semantic IR was built with. */
public record Intent(String program, String profileHash, List<Fact> facts) {
    public Intent {
        facts = List.copyOf(facts);
        if (program == null || program.isBlank() || profileHash == null || !profileHash.matches("sha256:[0-9a-f]{64}")) throw new Invalid("INT-06", "program '" + program + "' under '" + profileHash + "'");
        Set<String> ids = new HashSet<>();
        for (Fact f : facts) if (!ids.add(f.id())) throw new Invalid("INT-06", "two facts are " + f.id());
    }

    /** R-029: the facts a pass may act on, every one but the refuted; each still says whether a person confirmed it. */
    // Decision: R-029
    public List<Fact> actionable() {
        return facts.stream().filter(f -> f.state().status() != State.Status.REFUTED).toList();
    }

    /** INT-07: these facts are the program's: its name and profile hash, and every node they name one of its own. */
    public void against(Program p) {
        if (!p.header().program().equals(program) || !p.header().profileHash().equals(profileHash)) {
            throw new Invalid("INT-07", "facts of " + program + " under " + profileHash + " against " + p.header().program() + " under " + p.header().profileHash());
        }
        for (Fact f : facts) {
            List<String> wrong = new ArrayList<>(subject(p, f.subject()));
            for (Node n : f.evidence().nodes()) if (!node(p, n)) wrong.add("evidence " + n);
            if (!wrong.isEmpty()) throw new Invalid("INT-07", f.id() + ": " + wrong);
        }
    }

    /** What of a subject is not in the program: an item, a condition-name not the item's, an alternative that does not redefine it, a paragraph. */
    private static List<String> subject(Program p, Subject s) {
        List<String> wrong = new ArrayList<>();
        switch (s) {
            case Subject.Datum d -> { if (item(p, d.item()) == null) wrong.add("item " + d.item()); }
            case Subject.Choice c -> conditions(p, c, wrong);
            case Subject.Union u -> alternatives(p, u, wrong);
            case Subject.Range r -> { if (paragraph(p, r.first()) == null || paragraph(p, r.last()) == null) wrong.add(s.text()); }
            case Subject.Paragraphs g -> { for (int i : g.paragraphs()) if (paragraph(p, i) == null) wrong.add("paragraph " + i); }
            case Subject.Whole w -> { if (!w.program().equals(p.header().program())) wrong.add(s.text()); }
        }
        return wrong;
    }

    /** Each condition-name of a choice that is not one of its item's. */
    private static void conditions(Program p, Subject.Choice c, List<String> wrong) {
        Item i = item(p, c.item());
        Set<String> names = new HashSet<>();
        if (i != null) i.conditions().forEach(k -> names.add(k.name()));
        for (String name : c.conditions()) if (!names.contains(name)) wrong.add("condition " + name + " of item " + c.item());
    }

    /** A union's item, each alternative that does not REDEFINES it, and its tag, where the program has none of them. */
    private static void alternatives(Program p, Subject.Union u, List<String> wrong) {
        if (item(p, u.item()) == null) wrong.add("item " + u.item());
        for (int a : u.alternatives()) if (item(p, a) == null || item(p, a).redefines() != u.item()) wrong.add("alternative " + a + " of item " + u.item());
        if (u.tag() != 0 && item(p, u.tag()) == null) wrong.add("tag " + u.tag());
    }

    private static boolean node(Program p, Node n) {
        return switch (n) {
            case Node.ItemNode i -> item(p, i.item()) != null;
            case Node.ParagraphNode g -> paragraph(p, g.paragraph()) != null;
            case Node.StatementNode s -> paragraph(p, s.paragraph()) != null && s.n() < paragraph(p, s.paragraph()).statements().size();
            case Node.EdgeNode e -> e.edge() < p.procedure().edges().size();
        };
    }

    private static Item item(Program p, int id) {
        return id >= 1 && id <= p.items().size() ? p.items().get(id - 1) : null;
    }

    private static Procedure.Paragraph paragraph(Program p, int index) {
        for (Procedure.Paragraph g : p.procedure().paragraphs()) if (g.index() == index) return g;
        return null;
    }
}
