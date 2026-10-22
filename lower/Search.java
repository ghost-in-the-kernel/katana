package katana.lower;

import java.util.List;
import katana.sir.types.Item;
import katana.sir.types.Statement;
import katana.sir.types.Value;

/**
 * SEARCH and SEARCH ALL (LR 6.4 pp. 426-433), as stretches: a loop head that takes AT END once the
 * index passes the table's last occurrence, each WHEN tried in order, the first true one's statements
 * run and the SEARCH done, else the index (and a VARYING item that is not the table's own index) up
 * by one and back to the head. SEARCH ALL tries each occurrence from the first.
 */
final class Search {
    private final Flow f;

    Search(Flow f) {
        this.f = f;
    }

    /** A SEARCH as the loop head, its WHENs in order, and the step back to the head. */
    // Decision: R-019
    void statement(Statement s) {
        Value.Field table = (Value.Field) Verbs.operand(s, "search-statement");
        Item.Occurs occurs = f.x.item(table.item()).occurs();
        if (occurs == null || occurs.indexedBy().isEmpty()) {
            f.e.line(f.x.unsupported("SEARCH of a table without INDEXED BY at " + s.origin()) + ";");
            return;
        }
        boolean all = Verbs.keyword(s, "ALL");
        Value varying = Verbs.operand(s, "search-varying");
        int index = varying instanceof Value.Field v && occurs.indexedBy().contains(v.item()) ? v.item() : occurs.indexedBy().get(0);
        String ix = f.x.field(new Value.Field(index, List.of(), null, null));
        String count = occurs.dependingOn() == 0 ? occurs.max() + "L" : "Table.index(" + f.x.promoted.fieldName(occurs.dependingOn()) + ")";
        // Unsure: SEARCH ALL as a scan from the first occurrence; IBM leaves which of equal keys is found, and unordered keys, unpredictable (LR p. 431), and cobc halves the table, so only those cases can differ. Stays: IBM calls them unpredictable, so no run settles them; only a program's own data shows one.
        if (all) f.e.line("Table.set(" + ix + ", 1);");
        int head = f.label(), after = f.label();
        f.e.line("return " + head + ";");
        f.split(head);
        int next = f.label();
        f.e.line("if (Table.index(" + ix + ") <= " + count + ") return " + next + ";");
        f.statements(Verbs.branch(s, "at-end-phrase"));
        f.e.line("return " + after + ";");
        for (Statement.Branch b : s.branches()) {
            if (!b.label().equals("search-when")) continue;
            f.split(next);
            next = f.label();
            f.e.line("if (!(" + f.conditions.of(b.condition()) + ")) return " + next + ";");
            f.statements(b.statements());
            f.e.line("return " + after + ";");
        }
        f.split(next);
        f.e.line("Table.set(" + ix + ", Table.index(" + ix + ") + 1);");
        if (varying instanceof Value.Field v && v.item() != index) f.e.line("Table.set(" + f.x.field(v) + ", Table.index(" + f.x.field(v) + ") + 1);");
        f.e.line("return " + head + ";");
        f.split(after);
    }
}
