package katana.sir.types;

import java.util.List;
import katana.front.Mapped;

/**
 * The paragraph graph: the PROCEDURE DIVISION's USING items and RETURNING item (0 for none), its
 * sections and paragraphs in textual order (statements under no paragraph name, at the start of the
 * body or of a section, are a paragraph named ""), and the edges PERFORM, GO TO and ALTER make.
 */
public record Procedure(List<Integer> using, int returning, List<Section> sections, List<Paragraph> paragraphs, List<Edge> edges) {
    /** A section; a declarative one says what it is USE'd for. */
    public record Section(String name, boolean declarative, String use, Mapped.Origin origin) {
        public Section {
            Invalid.origin(origin, "section " + name);
        }
    }

    /** A paragraph: its index in textual order, its name, its section ("" for none), its statements. */
    public record Paragraph(int index, String name, String section, List<Statement> statements, Mapped.Origin origin) {
        public Paragraph {
            statements = List.copyOf(statements);
            Invalid.origin(origin, "paragraph " + name);
        }
    }

    /** How control reaches a paragraph other than by falling into it. */
    public enum Kind { PERFORM, GO_TO, ALTER }

    /** An edge from a paragraph to another, through `thru` for a PERFORM range (the same paragraph when none). */
    public record Edge(Kind kind, int from, int to, int thru, Mapped.Origin origin) {
        public Edge {
            Invalid.origin(origin, kind + " edge");
        }
    }

    /** SIR-06: paragraphs are numbered 0, 1, ... in textual order, and every edge joins two of them. */
    public Procedure {
        using = List.copyOf(using);
        sections = List.copyOf(sections);
        paragraphs = List.copyOf(paragraphs);
        edges = List.copyOf(edges);
        for (int i = 0; i < paragraphs.size(); i++) {
            if (paragraphs.get(i).index() != i) throw new Invalid("SIR-06", "paragraph " + paragraphs.get(i).name() + " is numbered " + paragraphs.get(i).index() + ", not " + i);
        }
        int count = paragraphs.size();
        for (Edge e : edges) {
            if (!inside(e.from(), count) || !inside(e.to(), count) || !inside(e.thru(), count)) throw new Invalid("SIR-06", e.kind() + " edge " + e.from() + " -> " + e.to() + " thru " + e.thru() + " leaves the graph");
        }
    }

    private static boolean inside(int i, int count) {
        return i >= 0 && i < count;
    }
}
