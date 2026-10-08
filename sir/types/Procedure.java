package katana.sir.types;

import java.util.List;

/**
 * The paragraph graph: the PROCEDURE DIVISION's USING items and RETURNING item (0 for none), its
 * sections and paragraphs in textual order (statements under no paragraph name, at the start of the
 * body or of a section, are a paragraph named ""), and the edges PERFORM, GO TO and ALTER make.
 */
public record Procedure(List<Integer> using, int returning, List<Section> sections, List<Paragraph> paragraphs, List<Edge> edges) {
    /** A section; a declarative one, and only one, says what it is USE'd for (SIR-10). */
    public record Section(String name, boolean declarative, Use use, Origin origin) {
        public Section {
            Invalid.origin(origin, "section " + name);
            if (declarative != (use != null)) throw new Invalid("SIR-10", "section " + name + (declarative ? " is declarative with no USE" : " has a USE and is not declarative"));
        }
    }

    /**
     * A declarative's USE as data (SIR-10): after an ERROR (or EXCEPTION, the same) on the files it names
     * (the program's own, and apart from them a GLOBAL file of a program it is nested in, as a statement
     * keeps one apart) or on every file opened in a mode (INPUT, OUTPUT, I-O, EXTEND; "" when it names
     * files), GLOBAL or not; or FOR DEBUGGING, with what it debugs, each as written (`ALL PROCEDURES`, a
     * procedure name, `ALL REFERENCES OF X`, a file).
     */
    public record Use(Kind kind, boolean global, String mode, List<String> files, List<String> outerFiles, List<String> debugging) {
        public enum Kind { ERROR, DEBUGGING }

        public Use {
            files = List.copyOf(files);
            outerFiles = List.copyOf(outerFiles);
            debugging = List.copyOf(debugging);
            boolean named = !files.isEmpty() || !outerFiles.isEmpty();
            if (kind == Kind.ERROR && mode.isEmpty() != named) throw new Invalid("SIR-10", "a USE AFTER ERROR names files or a mode, one of the two");
        }
    }

    /**
     * A paragraph: its index in textual order, its name, its section ("" for none), its statements, and
     * where each of its sentences starts (the index of its first statement, in increasing order; a
     * sentence of no statements has none), which is where NEXT SENTENCE goes.
     */
    public record Paragraph(int index, String name, String section, List<Statement> statements, List<Integer> sentences, Origin origin) {
        public Paragraph {
            statements = List.copyOf(statements);
            sentences = List.copyOf(sentences);
            Invalid.origin(origin, "paragraph " + name);
            for (int k = 0; k < sentences.size(); k++) {
                boolean ordered = sentences.get(k) >= 0 && sentences.get(k) < statements.size() && (k == 0 || sentences.get(k) > sentences.get(k - 1));
                if (!ordered) throw new Invalid("SIR-06", "paragraph " + name + "'s sentence " + k + " starts at statement " + sentences.get(k));
            }
        }
    }

    /** How control reaches a paragraph other than by falling into it. */
    public enum Kind { PERFORM, GO_TO, ALTER }

    /** An edge from a paragraph to another, through `thru` for a PERFORM range (the same paragraph when none). */
    public record Edge(Kind kind, int from, int to, int thru, Origin origin) {
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
