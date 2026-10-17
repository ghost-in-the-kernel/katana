package katana.intent.types;

/** A node of the Semantic IR that evidence cites (INT-03): an item, a paragraph, a statement or an edge of the paragraph graph. */
public sealed interface Node {
    /** An item, by its id (SIR-02). */
    record ItemNode(int item) implements Node {
        public ItemNode {
            if (item < 1) throw new Invalid("INT-03", "item " + item);
        }
    }

    /** A paragraph, by its index. */
    record ParagraphNode(int paragraph) implements Node {
        public ParagraphNode {
            if (paragraph < 0) throw new Invalid("INT-03", "paragraph " + paragraph);
        }
    }

    /** A paragraph's `n`th statement, from 0, as the contract numbers it (`paragraph.n`). */
    record StatementNode(int paragraph, int n) implements Node {
        public StatementNode {
            if (paragraph < 0 || n < 0) throw new Invalid("INT-03", "statement " + paragraph + "." + n);
        }
    }

    /** An edge of the paragraph graph, by its place in the procedure's edges. */
    record EdgeNode(int edge) implements Node {
        public EdgeNode {
            if (edge < 0) throw new Invalid("INT-03", "edge " + edge);
        }
    }
}
