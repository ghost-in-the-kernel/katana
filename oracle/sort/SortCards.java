package katana.oracle.sort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DFSORT's control cards as far as CardDemo uses them (docs/ROADMAP.md 1.5, R-010): SYMNAMES (fields
 * and constants), SORT FIELDS (with EQUALS), OPTION EQUALS, INCLUDE or OMIT COND, and OUTREC FIELDS
 * or BUILD. A statement, operand,
 * format or form outside this is refused with the card quoted (`Unsupported`), never guessed. SUM and
 * INREC are built when a fixture needs them (P-11).
 */
final class SortCards {
    /** A field by position (from 1), length and format. */
    record Field(int position, int length, String format) {}

    /** A sort key: a field, ascending or not. */
    record Key(Field field, boolean ascending) {}

    /** What a SORT step does: its keys, whether EQUALS was asked, its selection (null for all, `omit` to invert), and its OUTREC items (null for none). */
    record Spec(List<Key> keys, boolean equals, SortConditions.Cond select, boolean omit, List<SortOutrec.Item> outrec) {}

    /** A card, or a construction on it, the lane does not run; the message quotes it. */
    static final class Unsupported extends RuntimeException {
        Unsupported(String message) {
            super(message);
        }
    }

    private final Map<String, Field> fields = new HashMap<>();
    private final Map<String, String> constants = new HashMap<>();

    private SortCards() {}

    /** Reads SYMNAMES cards and SYSIN control statements into what the step does. */
    static Spec read(List<String> symnames, List<String> sysin) {
        SortCards cards = new SortCards();
        cards.symbols(symnames);
        List<Key> keys = null;
        SortConditions.Cond select = null;
        boolean omit = false;
        List<SortOutrec.Item> outrec = null;
        boolean equals = false;
        for (String[] statement : statements(sysin)) {
            String verb = statement[0], operand = statement[1];
            switch (verb) {
                case "SORT" -> {
                    String fields = operand.replaceAll(",(NO)?EQUALS$", "");
                    if (!fields.equals(operand)) equals = operand.endsWith(",EQUALS");
                    keys = cards.keys(operand(fields, "FIELDS"));
                }
                case "OPTION" -> {
                    if (!operand.matches("(NO)?EQUALS")) throw new Unsupported("OPTION " + operand + ": the lane reads OPTION EQUALS and NOEQUALS");
                    equals = operand.equals("EQUALS");
                }
                case "INCLUDE", "OMIT" -> {
                    if (select != null) throw new Unsupported("a second INCLUDE or OMIT: " + verb + " " + operand);
                    select = SortConditions.parse(operand(operand, "COND"), cards);
                    omit = verb.equals("OMIT");
                }
                case "OUTREC" -> outrec = SortOutrec.parse(operand(operand, operand.startsWith("BUILD=") ? "BUILD" : "FIELDS"), cards);
                default -> throw new Unsupported("statement " + verb + " is not in the lane's DFSORT: " + verb + " " + operand);
            }
        }
        if (keys == null) throw new Unsupported("no SORT FIELDS statement");
        return new Spec(keys, equals, select, omit, outrec);
    }

    /** SYMNAMES: `name,position,length,format` or `name,constant`; a blank ends the statement, `*` in column 1 is a comment. */
    private void symbols(List<String> cards) {
        for (String card : cards) {
            if (card.isBlank() || card.startsWith("*")) continue;
            String symbol = firstWord(card.substring(0, Math.min(card.length(), 80)));
            List<String> parts = split(symbol);
            if (parts.size() == 4 && parts.get(1).matches("\\d+") && parts.get(2).matches("\\d+")) {
                fields.put(parts.get(0), new Field(Integer.parseInt(parts.get(1)), Integer.parseInt(parts.get(2)), parts.get(3)));
            } else if (parts.size() == 2 && isConstant(parts.get(1))) {
                constants.put(parts.get(0), parts.get(1));
            } else {
                throw new Unsupported("a SYMNAMES statement the lane does not read: " + card.strip());
            }
        }
    }

    /** SORT FIELDS=(p,l,f,o,...) or (symbol,o,...): CH and ZD keys, A or D. */
    private List<Key> keys(List<String> items) {
        List<Key> keys = new ArrayList<>();
        for (int i = 0; i < items.size(); ) {
            Field field;
            if (fields.containsKey(items.get(i))) {
                field = fields.get(items.get(i++));
            } else if (i + 2 < items.size() && items.get(i).matches("\\d+")) {
                field = new Field(Integer.parseInt(items.get(i)), Integer.parseInt(items.get(i + 1)), items.get(i + 2));
                i += 3;
            } else {
                throw new Unsupported("SORT FIELDS item " + items.get(i));
            }
            if (i >= items.size() || !items.get(i).matches("[AD]")) throw new Unsupported("SORT FIELDS needs A or D after each field");
            if (!field.format().matches("CH|ZD")) throw new Unsupported("SORT FIELDS format " + field.format() + ": the lane sorts CH and ZD");
            keys.add(new Key(field, items.get(i++).equals("A")));
        }
        return keys;
    }

    /** The field a symbol or `p,l,f` names, or null. */
    Field field(String name) {
        return fields.get(name);
    }

    /** The constant a symbol names (C'...' or a number), or the token itself when it is one, else null. */
    String constant(String token) {
        if (constants.containsKey(token)) return constants.get(token);
        return isConstant(token) ? token : null;
    }

    private static boolean isConstant(String token) {
        return token.matches("C'([^']|'')*'") || token.matches("[+-]?\\d+");
    }

    /** `KEYWORD=(a,b,(c,d))` as its top-level items. */
    static List<String> operand(String operand, String keyword) {
        if (!operand.startsWith(keyword + "=(") || !operand.endsWith(")")) throw new Unsupported("expected " + keyword + "=(...): " + operand);
        return split(operand.substring(keyword.length() + 2, operand.length() - 1));
    }

    /** Splits on commas outside quotes and parentheses. */
    static List<String> split(String text) {
        List<String> items = new ArrayList<>();
        StringBuilder item = new StringBuilder();
        int depth = 0;
        boolean quoted = false;
        for (char c : text.toCharArray()) {
            if (c == '\'') quoted = !quoted;
            if (!quoted && c == '(') depth++;
            if (!quoted && c == ')') depth--;
            if (c == ',' && depth == 0 && !quoted) {
                items.add(item.toString());
                item.setLength(0);
            } else {
                item.append(c);
            }
        }
        if (!item.isEmpty()) items.add(item.toString());
        return items;
    }

    /** SYSIN's statements as (verb, operand): columns 2 to 71, `*` comments, an operand ending in a comma continued on the next card. */
    static List<String[]> statements(List<String> cards) {
        List<String[]> statements = new ArrayList<>();
        String verb = null;
        StringBuilder operand = new StringBuilder();
        for (String card : cards) {
            if (card.startsWith("*") || card.isBlank()) continue;
            if (!card.startsWith(" ")) throw new Unsupported("a label in column 1: " + card.strip());
            String body = card.substring(1, Math.min(card.length(), 71)).strip();
            if (verb == null) {
                String[] parts = body.split("\\s+", 2);
                verb = parts[0];
                body = parts.length > 1 ? parts[1] : "";
            }
            operand.append(firstWord(body));
            if (!operand.toString().endsWith(",")) {
                statements.add(new String[] {verb, operand.toString()});
                verb = null;
                operand.setLength(0);
            }
        }
        if (verb != null) throw new Unsupported("a statement continued past the last card: " + verb + " " + operand);
        return statements;
    }

    /** The text up to the first blank outside quotes: DFSORT reads what follows as remarks. */
    private static String firstWord(String text) {
        boolean quoted = false;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\'') quoted = !quoted;
            if (text.charAt(i) == ' ' && !quoted) return text.substring(0, i);
        }
        return text;
    }
}
