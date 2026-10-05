package katana.sir.build;

import java.util.ArrayList;
import java.util.List;
import katana.front.Parse;
import katana.sir.build.DataMap.Node;
import katana.sir.types.Attributes;
import katana.sir.types.Attributes.Category;
import katana.sir.types.Attributes.Usage;
import katana.sir.types.Item;

/**
 * The data map's nodes, placed, to items (tithe pass 2, C3, from DataMap): each with its attribute
 * tuple, OCCURS keys and indexes. A 66 that renames one item has that item's attributes; one that
 * renames a range is a group. BLANK WHEN ZERO makes a numeric DISPLAY item numeric-edited.
 */
final class Tuples {
    private final Parse.Result parse;
    private final List<Node> nodes;
    private final Placement placement;

    Tuples(Parse.Result parse, List<Node> nodes, Placement placement) {
        this.parse = parse;
        this.nodes = nodes;
        this.placement = placement;
    }

    /** The built items, in textual order. */
    List<Item> items() {
        List<Item> items = new ArrayList<>();
        for (Node n : nodes) {
            Item.Occurs occurs = n.occursMax == 0 ? null : new Item.Occurs(n.occursMin, n.occursMax, n.dependingOn, keys(n), indexes(n));
            items.add(new Item(n.id, n.name, n.level, n.parent, n.record, n.section, n.offset, n.length, attributes(n), n.redefines, occurs, n.syncSlack, n.value,
                    n.conditions, Origins.at(parse, n.ctx)));
        }
        return items;
    }

    private List<Item.Key> keys(Node n) {
        List<Item.Key> keys = new ArrayList<>();
        for (String[] k : n.keyNames) {
            int id = placement.named(k[0]);
            if (id != 0) keys.add(new Item.Key(id, k[1].equals("A")));
        }
        return keys;
    }

    private List<Integer> indexes(Node n) {
        List<Integer> ids = new ArrayList<>();
        for (String name : n.indexNames) {
            for (int i = n.id; i < nodes.size(); i++) {
                if (nodes.get(i).name.equals(name) && nodes.get(i).usage == Usage.INDEX) {
                    ids.add(nodes.get(i).id);
                    break;
                }
            }
        }
        return ids;
    }

    /** The attribute tuple: a group's is GROUP; an elementary item's from its usage and PICTURE. */
    static Attributes attributes(Node n) {
        if (n.renamesOne != null) return attributes(n.renamesOne);
        if (!n.children.isEmpty() || n.level == 66) return new Attributes(n.usage, Category.GROUP, 0, 0, false, false, false, "");
        Pictures p = n.pic;
        Category category = switch (n.usage) {
            case FLOAT_SHORT, FLOAT_LONG -> Category.NUMERIC;
            case INDEX -> Category.INDEX;
            case POINTER, PROCEDURE_POINTER, FUNCTION_POINTER -> Category.POINTER;
            default -> p == null ? Category.ALPHANUMERIC : p.category() == Category.NUMERIC && n.blankWhenZero && n.usage == Usage.DISPLAY ? Category.NUMERIC_EDITED : p.category();
        };
        boolean numeric = category == Category.NUMERIC || category == Category.NUMERIC_EDITED;
        if (!numeric || p == null) return new Attributes(n.usage, category, 0, 0, false, false, false, p == null ? "" : p.expanded());
        boolean signed = p.signed();
        return new Attributes(n.usage, category, Math.max(1, p.digits()), p.scale(), signed, signed && n.signSeparate, signed && n.leading, p.expanded());
    }
}
