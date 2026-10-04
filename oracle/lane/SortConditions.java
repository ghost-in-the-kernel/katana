package katana.oracle.lane;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * INCLUDE and OMIT COND: comparisons (EQ, NE, GT, GE, LT, LE) of a CH field with a character
 * constant or a CH field of its length, or of a ZD field with a number or a ZD field, joined by AND or
 * by OR. A condition that mixes AND and OR without parentheses is refused rather than given a
 * precedence the lane has not read in IBM's text.
 */
final class SortConditions {
    /** A condition on a record: a comparison or a group, readable by the Java sort and by the generated COBOL one. */
    sealed interface Cond permits Compare, Group {
        boolean test(byte[] record);
    }

    /** `left op constant` or `left op right`. */
    record Compare(SortCards.Field left, String op, String constant, SortCards.Field right) implements Cond {
        public boolean test(byte[] record) {
            return left.format().equals("CH") ? character(this, record) : zoned(this, record);
        }
    }

    /** Conditions joined by AND (all) or OR (any). */
    record Group(List<Cond> parts, boolean and) implements Cond {
        public boolean test(byte[] record) {
            return and ? parts.stream().allMatch(p -> p.test(record)) : parts.stream().anyMatch(p -> p.test(record));
        }
    }

    private SortConditions() {}

    /** Parses COND's items: comparisons and parenthesized groups, joined by one connector. */
    static Cond parse(List<String> items, SortCards cards) {
        List<Cond> parts = new ArrayList<>();
        String connector = null;
        int i = 0;
        while (i < items.size()) {
            if (items.get(i).startsWith("(")) {
                String group = items.get(i++);
                parts.add(parse(SortCards.split(group.substring(1, group.length() - 1)), cards));
            } else {
                int[] at = {i};
                parts.add(comparison(items, at, cards));
                i = at[0];
            }
            if (i < items.size()) {
                String next = items.get(i++).replace("&", "AND").replace("|", "OR");
                if (!next.equals("AND") && !next.equals("OR")) throw new SortCards.Unsupported("COND expects AND or OR, found " + next);
                if (connector != null && !connector.equals(next)) throw new SortCards.Unsupported("COND mixes AND and OR without parentheses");
                connector = next;
            }
        }
        return new Group(parts, !"OR".equals(connector));
    }

    /** One `field,op,field-or-constant`, reading from at[0] and leaving it after. */
    private static Cond comparison(List<String> items, int[] at, SortCards cards) {
        SortCards.Field left = field(items, at, cards);
        if (at[0] >= items.size()) throw new SortCards.Unsupported("COND ends after a field");
        String op = items.get(at[0]++);
        if (!op.matches("EQ|NE|GT|GE|LT|LE")) throw new SortCards.Unsupported("COND operator " + op);
        String constant = at[0] < items.size() ? cards.constant(items.get(at[0])) : null;
        SortCards.Field right = constant == null ? field(items, at, cards) : null;
        if (constant != null) at[0]++;
        if (left.format().equals("CH")) {
            if (constant != null && !constant.startsWith("C'")) throw new SortCards.Unsupported("a CH field compared with " + constant);
            if (right != null && (!right.format().equals("CH") || right.length() != left.length())) throw new SortCards.Unsupported("CH fields of different lengths or formats");
            if (constant != null) text(constant, left.length());
        } else if (left.format().equals("ZD")) {
            if (constant != null && constant.startsWith("C'")) throw new SortCards.Unsupported("a ZD field compared with " + constant);
            if (right != null && !right.format().equals("ZD")) throw new SortCards.Unsupported("a ZD field compared with a " + right.format() + " field");
        } else {
            throw new SortCards.Unsupported("COND on format " + left.format());
        }
        return new Compare(left, op, constant, right);
    }

    /** A symbol naming a field, or `p,l,f`. */
    private static SortCards.Field field(List<String> items, int[] at, SortCards cards) {
        SortCards.Field named = cards.field(items.get(at[0]));
        if (named != null) {
            at[0]++;
            return named;
        }
        if (at[0] + 2 >= items.size() || !items.get(at[0]).matches("\\d+") || !items.get(at[0] + 1).matches("\\d+")) {
            throw new SortCards.Unsupported("COND expects a field, found " + items.get(at[0]));
        }
        SortCards.Field field = new SortCards.Field(Integer.parseInt(items.get(at[0])), Integer.parseInt(items.get(at[0] + 1)), items.get(at[0] + 2));
        at[0] += 3;
        return field;
    }

    /** A CH comparison: unsigned bytes, the constant padded with blanks to the field's length. */
    private static boolean character(Compare c, byte[] record) {
        int from = c.left().position() - 1, to = from + c.left().length();
        if (c.constant() != null) {
            byte[] value = text(c.constant(), c.left().length());
            return holds(c.op(), Arrays.compareUnsigned(record, from, to, value, 0, value.length));
        }
        int other = c.right().position() - 1;
        return holds(c.op(), Arrays.compareUnsigned(record, from, to, record, other, other + c.right().length()));
    }

    /** A ZD comparison: by value. */
    private static boolean zoned(Compare c, byte[] record) {
        BigInteger right = c.constant() != null ? new BigInteger(c.constant()) : Zoned.value(record, c.right());
        return holds(c.op(), Zoned.value(record, c.left()).compareTo(right));
    }

    /** A character constant's bytes, padded with blanks to the field's length; a longer one is refused. */
    static byte[] text(String constant, int length) {
        String body = constant.substring(2, constant.length() - 1).replace("''", "'");
        if (body.length() > length) throw new SortCards.Unsupported("constant " + constant + " is longer than its " + length + "-byte field");
        return String.format("%-" + length + "s", body).getBytes(StandardCharsets.ISO_8859_1);
    }

    private static boolean holds(String op, int c) {
        return switch (op) {
            case "EQ" -> c == 0;
            case "NE" -> c != 0;
            case "GT" -> c > 0;
            case "GE" -> c >= 0;
            case "LT" -> c < 0;
            default -> c <= 0;
        };
    }
}
