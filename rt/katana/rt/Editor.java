package katana.rt;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * PICTURE editing, as IBM's Language Reference 6.4 gives it (pp. 220-225): simple insertion (B 0 / ,),
 * special insertion (the point), fixed insertion (the currency sign, + - CR DB), floating insertion
 * (a string of two or more `$`, `+` or `-`) and zero suppression (Z, or `*` check protection), and
 * BLANK WHEN ZERO; alphanumeric editing (X A 9 with B 0 /); and de-editing, a numeric-edited sender's
 * value read back. Works on the mask (`Edit`), one symbol a character position.
 */
public final class Editor {
    private Editor() {}

    /** A position's role once the mask is read: a digit, an insertion, a fixed symbol, the floating string's first symbol. */
    private record Layout(String mask, char floating, int[] digitAt, int suppressFrom, boolean allSuppressing) {}

    /** The mask read: its floating symbol (if any), its digit positions, where suppression or floating starts. */
    private static Layout layout(String mask) {
        boolean crdb = mask.endsWith("CR") || mask.endsWith("DB");
        int end = crdb ? mask.length() - 2 : mask.length();
        char floating = 0;
        for (char f : new char[] {'$', '+', '-'}) if (count(mask, f, end) >= 2) floating = f;
        int[] digitAt = new int[end];
        int n = 0, suppressFrom = -1;
        boolean firstFloating = true, allSuppressing = true;
        for (int i = 0; i < end; i++) {
            char c = mask.charAt(i);
            boolean isFloating = floating != 0 && c == floating;
            if (isFloating && firstFloating) {
                firstFloating = false;
                if (suppressFrom < 0) suppressFrom = i;
                continue;
            }
            if (c == '9' || c == 'Z' || c == '*' || isFloating) {
                digitAt[n++] = i;
                if (c == '9') allSuppressing = false;
                if ((c == 'Z' || c == '*') && suppressFrom < 0) suppressFrom = i;
            }
        }
        return new Layout(mask, floating, java.util.Arrays.copyOf(digitAt, n), suppressFrom, allSuppressing && n > 0);
    }

    private static int count(String s, char c, int end) {
        int n = 0;
        for (int i = 0; i < end; i++) if (s.charAt(i) == c) n++;
        return n;
    }

    /** A number edited by a mask: its digits, `scale` after the point, the rest by the symbols around them. */
    // Decision: R-019
    public static String number(Edit e, int scale, BigDecimal value) {
        Layout l = layout(e.mask());
        int nd = l.digitAt().length;
        BigInteger unscaled = value.setScale(scale, RoundingMode.DOWN).unscaledValue().abs().mod(BigInteger.TEN.pow(nd));
        String digits = nd == 0 ? "" : String.format("%0" + nd + "d", unscaled);
        // Unsure: a negative value cut to zero shows as positive, by the table's "positive or zero" column (LR p. 222); IBM's p. 404 "the sign of the sending item" could read the other way (J-104).
        boolean zero = unscaled.signum() == 0, negative = !zero && value.signum() < 0;
        int length = e.mask().length();
        if ((e.blankWhenZero() && zero) || (zero && l.allSuppressing() && suppressor(l) != '*')) return " ".repeat(length);
        if (zero && l.allSuppressing()) return e.mask().chars().mapToObj(c -> c == '.' ? point(e) : "*").reduce("", String::concat);
        int k0 = significance(l, digits, nd - scale);
        StringBuilder out = new StringBuilder();
        int d = 0;
        for (int i = 0; i < length; i++) {
            char c = e.mask().charAt(i);
            boolean digit = d < nd && l.digitAt()[d] == i;
            String shown = digit ? String.valueOf(digits.charAt(d++)) : symbol(e, l, i, c, negative);
            if (l.suppressFrom() >= 0 && i >= l.suppressFrom() && i < k0) shown = suppressed(e, l, i, k0, digit, c, shown, negative);
            out.append(shown);
        }
        return out.toString();
    }

    /** Where significance starts: the first digit position holding a nonzero digit, a 9, or a decimal digit, or the point if it comes first. */
    private static int significance(Layout l, String digits, int integerDigits) {
        int point = l.mask().indexOf('.');
        for (int d = 0; d < l.digitAt().length; d++) {
            int i = l.digitAt()[d];
            char c = l.mask().charAt(i);
            boolean significant = digits.charAt(d) != '0' || c == '9' || d >= integerDigits;
            if (significant) return point >= 0 && point < i ? point : i;
        }
        return point >= 0 ? point : l.mask().length();
    }

    /** What the suppression or floating string shows before significance: the floating symbol just left of it, else the replacement character. */
    private static String suppressed(Edit e, Layout l, int i, int k0, boolean digit, char c, String shown, boolean negative) {
        if (l.floating() != 0) return i == k0 - 1 ? floatingSymbol(e, l.floating(), negative) : " ";
        boolean replaced = digit || "B0/,".indexOf(c) >= 0;
        return replaced ? String.valueOf(suppressor(l)) : shown;
    }

    private static char suppressor(Layout l) {
        return l.mask().indexOf('*') >= 0 ? '*' : ' ';
    }

    /** A position that is not a digit: an insertion character, the point, a fixed currency or sign symbol, CR or DB. */
    private static String symbol(Edit e, Layout l, int i, char c, boolean negative) {
        boolean crdb = i >= e.mask().length() - 2 && (e.mask().endsWith("CR") || e.mask().endsWith("DB"));
        if (crdb) return negative ? String.valueOf(c) : " ";
        return switch (c) {
            case 'B' -> " ";
            case '0', '/' -> String.valueOf(c);
            case ',' -> e.decimalComma() ? "." : ",";
            case '.' -> point(e);
            case '$' -> String.valueOf(e.currency());
            case '+' -> negative ? "-" : "+";
            case '-' -> negative ? "-" : " ";
            default -> String.valueOf(c);
        };
    }

    private static String point(Edit e) {
        return e.decimalComma() ? "," : ".";
    }

    private static String floatingSymbol(Edit e, char f, boolean negative) {
        return f == '$' ? String.valueOf(e.currency()) : f == '+' ? (negative ? "-" : "+") : (negative ? "-" : " ");
    }

    /** Characters edited by an alphanumeric mask: each X, A or 9 takes the next character (a space when they run out); B 0 / insert. */
    // Decision: R-019
    public static String text(Edit e, String chars) {
        StringBuilder out = new StringBuilder();
        int k = 0;
        for (char c : e.mask().toCharArray()) {
            if (c == 'X' || c == 'A' || c == '9') out.append(k < chars.length() ? chars.charAt(k++) : ' ');
            else out.append(c == 'B' ? ' ' : c);
        }
        return out.toString();
    }

    /** A numeric-edited item's value read back (LR p. 404): its digit positions' digits (a space or `*` a zero), negative when a `-`, CR or DB shows. */
    // Decision: R-019
    public static BigDecimal deEdit(Edit e, int scale, String shown) {
        Layout l = layout(e.mask());
        StringBuilder digits = new StringBuilder();
        for (int i : l.digitAt()) {
            char c = shown.charAt(i);
            digits.append(Character.isDigit(c) ? c : '0');
        }
        boolean negative = shown.contains("-") || shown.endsWith("CR") || shown.endsWith("DB");
        BigInteger v = digits.length() == 0 ? BigInteger.ZERO : new BigInteger(digits.toString());
        return new BigDecimal(negative ? v.negate() : v, scale);
    }
}
