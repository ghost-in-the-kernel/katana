package katana.intent.review;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import katana.intent.types.Evidence;
import katana.intent.types.Fact;
import katana.intent.types.Kind;
import katana.intent.types.Node;
import katana.intent.types.State;
import katana.intent.types.Subject;
import katana.sir.types.Program;

/**
 * One line of a review file (research §30): `state | kind | subject | rule vN | confidence | who date | why`,
 * the subject by the program's names. Read, a line is a fact of the program, or the reason it names nothing.
 */
record Line(Fact fact, String why, String problem) {
    static final String SEP = " | ";

    /** A fact as a line, or null where the program has no name for its subject. */
    static String write(Program p, Fact f, String why) {
        String subject = subject(p, f.subject());
        if (subject == null) return null;
        State s = f.state();
        String person = s.status() == State.Status.INFERRED ? "" : s.who() + " " + s.when();
        String line = String.join(SEP, s.status().name().toLowerCase(Locale.ROOT), f.kind().name().toLowerCase(Locale.ROOT), subject,
                f.evidence().rule() + " v" + f.evidence().version(), confidence(f.confidence()), person);
        return why == null || why.isBlank() ? line.stripTrailing().replaceAll("\\s*\\|$", "") : line + SEP + why;
    }

    private static String confidence(double c) {
        return java.math.BigDecimal.valueOf(c).stripTrailingZeros().toPlainString();
    }

    /** A line read against the program: its fact, or a problem saying why it names nothing. */
    static Line read(Program p, String text) {
        String[] f = text.split("\\s*\\|\\s*", -1);
        if (f.length < 5) return problem("not `state | kind | subject | rule vN | confidence | who date | why`");
        try {
            State.Status status = State.Status.valueOf(f[0].trim().toUpperCase(Locale.ROOT));
            Kind kind = Kind.valueOf(f[1].trim().toUpperCase(Locale.ROOT));
            Subject subject = subject(p, kind, f[2].trim());
            if (subject == null) return problem("the program holds no `" + f[2].trim() + "` as a " + kind.name().toLowerCase(Locale.ROOT) + " is about");
            String[] rule = f[3].trim().split("\\s+v");
            String[] person = f.length > 5 && !f[5].isBlank() ? f[5].trim().split("\\s+") : new String[0];
            State state = status == State.Status.INFERRED ? State.INFERRED
                    : new State(status, person.length == 2 ? person[0] : "", person.length == 2 ? person[1] : "");
            Fact fact = new Fact(kind, subject, new Evidence(rule[0], Integer.parseInt(rule[1]), nodes(subject)), Double.parseDouble(f[4].trim()), state);
            return new Line(fact, f.length > 6 ? String.join(SEP, List.of(f).subList(6, f.length)).trim() : "", null);
        } catch (RuntimeException e) {
            return problem(e.getMessage());
        }
    }

    private static Line problem(String why) {
        return new Line(null, "", why);
    }

    /** The subject as the program names it, or null where some part of it has no name. */
    static String subject(Program p, Subject s) {
        return switch (s) {
            case Subject.Datum d -> Names.item(p, d.item());
            case Subject.Choice c -> join(Names.item(p, c.item()), String.join(", ", c.conditions()));
            case Subject.Union u -> {
                List<String> alternatives = new ArrayList<>();
                for (int a : u.alternatives()) alternatives.add(Names.item(p, a));
                String tag = Names.item(p, u.tag());
                yield alternatives.contains(null) || tag == null ? null : join(Names.item(p, u.item()), String.join(", ", alternatives) + " tag " + tag);
            }
            case Subject.Range r -> r.first() == r.last() ? Names.paragraph(p, r.first())
                    : Names.paragraph(p, r.first()) == null || Names.paragraph(p, r.last()) == null ? null : Names.paragraph(p, r.first()) + " THRU " + Names.paragraph(p, r.last());
            case Subject.Paragraphs g -> {
                List<String> names = g.paragraphs().stream().map(i -> Names.paragraph(p, i)).toList();
                yield names.contains(null) ? null : String.join(", ", names);
            }
            case Subject.Whole w -> w.program();
        };
    }

    private static String join(String item, String rest) {
        return item == null ? null : item + ": " + rest;
    }

    /** A subject read by the program's names into its kind's shape, or null where a name names nothing. */
    private static Subject subject(Program p, Kind kind, String text) {
        String[] head = text.split(":", 2);
        if (kind.shape() == Subject.Datum.class) return Names.item(p, text) == 0 ? null : new Subject.Datum(Names.item(p, text));
        if (kind.shape() == Subject.Choice.class) return Names.item(p, head[0]) == 0 || head.length < 2 ? null : new Subject.Choice(Names.item(p, head[0]), list(head[1]));
        if (kind.shape() == Subject.Union.class) return union(p, head);
        if (kind.shape() == Subject.Range.class) {
            String[] ends = text.split("\\s+THRU\\s+");
            int first = Names.paragraph(p, ends[0]), last = Names.paragraph(p, ends[ends.length - 1]);
            return first < 0 || last < 0 ? null : new Subject.Range(first, last);
        }
        if (kind.shape() == Subject.Paragraphs.class) {
            List<Integer> indexes = list(text).stream().map(n -> Names.paragraph(p, n)).toList();
            return indexes.contains(-1) ? null : new Subject.Paragraphs(indexes);
        }
        return text.equals(p.header().program()) ? new Subject.Whole(text) : null;
    }

    private static Subject union(Program p, String[] head) {
        if (head.length < 2 || !head[1].contains(" tag ")) return null;
        String[] parts = head[1].split("\\s+tag\\s+");
        List<Integer> alternatives = list(parts[0]).stream().map(n -> Names.item(p, n)).toList();
        int item = Names.item(p, head[0]), tag = Names.item(p, parts[1]);
        return item == 0 || tag == 0 || alternatives.contains(0) ? null : new Subject.Union(item, alternatives, tag);
    }

    private static List<String> list(String text) {
        return List.of(text.trim().split("\\s*,\\s*"));
    }

    /** The nodes a fact read from a file cites: the item or the first paragraph it is about. */
    private static List<Node> nodes(Subject s) {
        return List.of(switch (s) {
            case Subject.Datum d -> new Node.ItemNode(d.item());
            case Subject.Choice c -> new Node.ItemNode(c.item());
            case Subject.Union u -> new Node.ItemNode(u.item());
            case Subject.Range r -> new Node.ParagraphNode(r.first());
            case Subject.Paragraphs g -> new Node.ParagraphNode(g.paragraphs().get(0));
            case Subject.Whole w -> new Node.ParagraphNode(0);
        });
    }
}
