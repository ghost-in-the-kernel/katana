package katana.intent.review;

import java.util.ArrayList;
import java.util.List;
import katana.sir.types.Item;
import katana.sir.types.Procedure;
import katana.sir.types.Program;

/**
 * The program's own names for what a fact is about (research §30): an item by its name, qualified `OF` its
 * parents only as far as makes it unique; a paragraph by its name, `IN` its section where two share it.
 * Names, unlike the IR's numbers, stay put when a line is added above them; a name the program does not
 * hold uniquely names nothing.
 */
final class Names {
    private Names() {}

    /** An item's name, as little qualified as makes it unique, or null where none does (FILLER). */
    static String item(Program p, int id) {
        Item i = p.items().get(id - 1);
        if (i.name().equals("FILLER") || i.name().isBlank()) return null;
        List<String> qualifiers = new ArrayList<>();
        List<Item> ancestors = ancestors(p, i);
        for (int k = 0; ; k++) {
            if (matches(p, i.name(), qualifiers).size() == 1) break;
            if (k == ancestors.size()) return null;
            if (!ancestors.get(k).name().equals("FILLER")) qualifiers.add(ancestors.get(k).name());
        }
        StringBuilder name = new StringBuilder(i.name());
        for (String q : qualifiers) name.append(" OF ").append(q);
        return name.toString();
    }

    /** The item a name, qualified or not, names in the program, or 0 where it names none or several. */
    static int item(Program p, String name) {
        String[] parts = name.trim().split("\\s+OF\\s+");
        List<Item> found = matches(p, parts[0], List.of(parts).subList(1, parts.length));
        return found.size() == 1 ? found.get(0).id() : 0;
    }

    /** The items of this name whose parents, nearest first, include the qualifiers in order. */
    private static List<Item> matches(Program p, String name, List<String> qualifiers) {
        List<Item> found = new ArrayList<>();
        for (Item i : p.items()) {
            if (!i.name().equals(name)) continue;
            int q = 0;
            for (Item a : ancestors(p, i)) if (q < qualifiers.size() && a.name().equals(qualifiers.get(q))) q++;
            if (q == qualifiers.size()) found.add(i);
        }
        return found;
    }

    private static List<Item> ancestors(Program p, Item i) {
        List<Item> out = new ArrayList<>();
        for (int at = i.parent(); at > 0; at = p.items().get(at - 1).parent()) out.add(p.items().get(at - 1));
        return out;
    }

    /** A paragraph's name, `IN` its section where another paragraph shares the name, or null for the unnamed start. */
    static String paragraph(Program p, int index) {
        Procedure.Paragraph g = p.procedure().paragraphs().get(index);
        if (g.name().isBlank()) return null;
        long same = p.procedure().paragraphs().stream().filter(o -> o.name().equals(g.name())).count();
        return same == 1 ? g.name() : g.section().isBlank() ? null : g.name() + " IN " + g.section();
    }

    /** The paragraph a name names, or -1 where it names none or several. */
    static int paragraph(Program p, String name) {
        String[] parts = name.trim().split("\\s+IN\\s+");
        List<Procedure.Paragraph> found = p.procedure().paragraphs().stream()
                .filter(g -> g.name().equals(parts[0]) && (parts.length == 1 || g.section().equals(parts[1]))).toList();
        return found.size() == 1 ? found.get(0).index() : -1;
    }
}
