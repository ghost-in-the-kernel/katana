package katana.intent.types;

import java.util.HashSet;
import java.util.List;

/** What a fact is about, by the Semantic IR's numbers: items by id, paragraphs by index (INT-02). */
public sealed interface Subject {
    /** The subject as a fact's text writes it; with its kind, a fact's identity (INT-06). */
    String text();

    /** An item: money, a flag, a counter, a date, an item that never overflows. */
    record Datum(int item) implements Subject {
        public Datum {
            if (item < 1) throw new Invalid("INT-02", "item " + item);
        }

        public String text() {
            return "item " + item;
        }
    }

    /** An item and two or more of its condition-names: an enum, each name one of its values. */
    record Choice(int item, List<String> conditions) implements Subject {
        public Choice {
            conditions = List.copyOf(conditions);
            if (item < 1 || conditions.size() < 2 || new HashSet<>(conditions).size() != conditions.size() || conditions.stream().anyMatch(String::isBlank)) {
                throw new Invalid("INT-02", "item " + item + " with conditions " + conditions);
            }
        }

        public String text() {
            return "item " + item + " " + String.join(",", conditions);
        }
    }

    /** An item and the items that REDEFINES it, each one alternative; `tag` the item whose value says which (INT-08). */
    record Union(int item, List<Integer> alternatives, int tag) implements Subject {
        public Union {
            alternatives = List.copyOf(alternatives);
            if (tag < 1) throw new Invalid("INT-08", "item " + item + " with alternatives " + alternatives + " names no tag");
            if (item < 1 || tag == item || alternatives.isEmpty() || alternatives.contains(item) || alternatives.contains(tag)
                    || alternatives.stream().anyMatch(a -> a < 1) || new HashSet<>(alternatives).size() != alternatives.size()) {
                throw new Invalid("INT-02", "item " + item + " with alternatives " + alternatives + " and tag " + tag);
            }
        }

        public String text() {
            return "item " + item + " " + alternatives.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse("") + " tag " + tag;
        }
    }

    /** The paragraphs a PERFORM names, `first` THRU `last` (the same paragraph without THRU). */
    record Range(int first, int last) implements Subject {
        public Range {
            if (first < 0 || last < first) throw new Invalid("INT-02", "paragraphs " + first + " thru " + last);
        }

        public String text() {
            return "paragraphs " + first + " thru " + last;
        }
    }

    /** A set of paragraphs, in ascending order. */
    record Paragraphs(List<Integer> paragraphs) implements Subject {
        public Paragraphs {
            paragraphs = List.copyOf(paragraphs);
            boolean ascending = !paragraphs.isEmpty() && paragraphs.get(0) >= 0;
            for (int i = 1; i < paragraphs.size(); i++) ascending &= paragraphs.get(i) > paragraphs.get(i - 1);
            if (!ascending) throw new Invalid("INT-02", "paragraphs " + paragraphs);
        }

        public String text() {
            return "paragraphs " + paragraphs.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse("");
        }
    }

    /** A program, by its PROGRAM-ID. */
    record Whole(String program) implements Subject {
        public Whole {
            if (program == null || program.isBlank()) throw new Invalid("INT-02", "program '" + program + "'");
        }

        public String text() {
            return "program " + program;
        }
    }
}
