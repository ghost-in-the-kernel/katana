package katana.intent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import katana.sir.types.Attributes;
import katana.sir.types.Item;
import katana.sir.types.Program;
import katana.sir.types.Statement;
import katana.sir.types.Value;

/**
 * T-M2 (research §33, §34): a `money` fact on an item is borne out where some CardDemo program prints it,
 * a MOVE of it to a field edited with thousands commas or a currency sign; for an item a copybook declares,
 * any program that copies that copybook, online or batch; for the program's own, the program itself.
 */
final class MoneyEditedElsewhere {
    static final String TEST = "T-M2";

    private MoneyEditedElsewhere() {}

    static Verdict verdict(Program p, int item, Corpus corpus) {
        Item it = p.items().get(item - 1);
        boolean copybook = it.origin().file().toLowerCase().endsWith(".cpy");
        List<Program> where = copybook ? corpus.mentioning(it.name()) : List.of(p);
        List<String> printed = new ArrayList<>();
        for (Program q : where) {
            for (Item j : q.items()) {
                if (!j.name().equals(it.name()) || !j.origin().file().equals(it.origin().file())) continue;
                printed.addAll(printed(q, j.id()));
            }
        }
        String scope = copybook ? "no CardDemo program that copies " + BatchReview.file(it.origin().file()) : "this program, which declares it, never";
        if (printed.isEmpty()) return Verdict.refutes(TEST, scope + " moves it to a field edited with commas or a currency sign, where it would be printed");
        return Verdict.holds(TEST, printed.get(0) + (printed.size() == 1 ? "" : "; and " + (printed.size() - 1) + " more such"));
    }

    /** Each MOVE of the item to a numeric-edited field with a comma or the currency sign, as `PROGRAM moves it to FIELD (picture) in PARAGRAPH`. */
    static List<String> printed(Program q, int item) {
        List<String> out = new ArrayList<>();
        for (BatchReviewEvidence.At a : BatchReviewEvidence.statements(q)) {
            Statement s = a.s();
            if (!s.verb().equals("MOVE") || s.operands().stream().noneMatch(o -> o.role().equals("move-to-sending-area") && o.value() instanceof Value.Field f && f.item() == item && f.start() == null)) continue;
            for (Statement.Operand o : s.operands()) {
                if (!o.role().equals("move-to-statement") || !(o.value() instanceof Value.Field t)) continue;
                Attributes to = q.items().get(t.item() - 1).attributes();
                if (to.category() == Attributes.Category.NUMERIC_EDITED && (to.picture().contains(",") || to.picture().contains(q.header().currency())))
                    out.add(q.header().program() + " moves it to " + q.items().get(t.item() - 1).name() + " (" + to.picture() + ") in " + a.paragraph());
            }
        }
        return out;
    }
}
