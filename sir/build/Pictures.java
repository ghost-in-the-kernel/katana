package katana.sir.build;

import java.util.Locale;
import katana.sir.types.Attributes.Category;

/**
 * A PICTURE string read: its repetitions expanded (`9(3)V99` is `999V99`), its category, its digits
 * (each 9 and P; in an edited picture each digit position: 9, Z, * and every floating sign or currency
 * symbol but the first), its scale (positions right of V or of an edited picture's point; a P before
 * the first digit adds to it, a P after the last subtracts), whether it is signed (S; in an edited picture a +, -, CR or DB), and how many
 * character positions it takes in DISPLAY (S, V and P take none; CR and DB two; N two).
 */
record Pictures(String expanded, Category category, int digits, int scale, boolean signed, int positions) {
    private static final String INSERTION = "B0/,.";

    static Pictures read(String picture) {
        return read(picture, '$', false);
    }

    /**
     * A PICTURE under a CURRENCY SIGN other than `$` (the program's symbol stands where `$` would) or
     * DECIMAL-POINT IS COMMA (`,` is the point and `.` an insertion, read here swapped).
     */
    static Pictures read(String picture, char currency, boolean decimalComma) {
        String p = expand(picture.toUpperCase(Locale.ROOT));
        if (currency != '$') p = p.replace(Character.toUpperCase(currency), '$');
        if (decimalComma) p = p.replace('.', '\u0001').replace(',', '.').replace('\u0001', ',');
        Symbols symbols = Symbols.scan(p, picture);
        Category category = symbols.category();
        if (category != Category.NUMERIC && category != Category.NUMERIC_EDITED) return new Pictures(p, category, 0, 0, false, symbols.positions());
        boolean edited = category == Category.NUMERIC_EDITED;
        boolean signed = p.indexOf('S') >= 0 || (edited && (p.indexOf('+') >= 0 || p.indexOf('-') >= 0 || p.contains("CR") || p.contains("DB")));
        return new Pictures(p, category, digits(p, edited), scale(p), signed, symbols.positions());
    }

    /** Which kinds of symbol a picture holds, and the character positions they take. */
    private record Symbols(int positions, boolean x, boolean a, boolean n, boolean nine, boolean edited) {
        /** One pass over the expanded picture: which symbols, how many positions; an unknown symbol is refused. */
        static Symbols scan(String p, String picture) {
            int positions = 0;
            boolean x = false, a = false, n = false, nine = false, edited = false;
            for (int i = 0; i < p.length(); i++) {
                char c = p.charAt(i);
                if (p.startsWith("CR", i) || p.startsWith("DB", i)) {
                    edited = true;
                    positions += 2;
                    i++;
                    continue;
                }
                x |= c == 'X';
                a |= c == 'A';
                n |= c == 'N';
                nine |= c == '9';
                if ("SVP9XAN".indexOf(c) < 0) {
                    if (INSERTION.indexOf(c) < 0 && "Z*+-$".indexOf(c) < 0) throw new IllegalArgumentException("PICTURE " + picture + ": '" + c + "'");
                    edited = true;
                }
                if ("SVP".indexOf(c) < 0) positions += c == 'N' ? 2 : 1;
            }
            return new Symbols(positions, x, a, n, nine, edited);
        }

        /** The category the symbols make: national, edited, alphanumeric, alphabetic or numeric. */
        Category category() {
            return n ? Category.NATIONAL
                    : edited && (x || a) ? Category.ALPHANUMERIC_EDITED
                    : x || (a && nine) ? Category.ALPHANUMERIC
                    : a ? Category.ALPHABETIC
                    : edited ? Category.NUMERIC_EDITED
                    : Category.NUMERIC;
        }
    }

    /** Digit positions: 9 and P; in an edited picture also Z and *, and a floating string's symbols but its first. */
    private static int digits(String p, boolean edited) {
        int digits = 0;
        for (char c : p.toCharArray()) if (c == '9' || c == 'P' || (edited && (c == 'Z' || c == '*'))) digits++;
        if (edited) {
            for (char f : new char[] {'+', '-', '$'}) {
                int count = (int) p.chars().filter(c -> c == f).count();
                if (count > 1) digits += count - 1;
            }
        }
        return digits;
    }

    /** Digits right of the point (V, or `.` when edited, floating symbols counting); P before the first digit adds, P after the last subtracts; all P, all scale. */
    private static int scale(String p) {
        int point = p.indexOf('V') >= 0 ? p.indexOf('V') : p.indexOf('.');
        int scale = 0;
        if (point >= 0) {
            for (int i = point + 1; i < p.length(); i++) if ("9PZ*".indexOf(p.charAt(i)) >= 0 || floating(p, p.charAt(i))) scale++;
        }
        int firstDigit = firstOf(p, "9Z*"), lastDigit = lastOf(p, "9Z*");
        if (point < 0 && firstDigit < 0) scale = (int) p.chars().filter(c -> c == 'P').count();
        if (point < 0 && firstDigit >= 0) {
            long before = p.substring(0, firstDigit).chars().filter(c -> c == 'P').count();
            long after = p.substring(lastDigit + 1).chars().filter(c -> c == 'P').count();
            if (before > 0) scale = (int) (before + p.chars().filter(c -> c == '9').count());
            else scale = -(int) after;
        }
        return scale;
    }

    /** A +, - or $ written more than once is a floating string, whose symbols but the first hold digits. */
    private static boolean floating(String p, char c) {
        return "+-$".indexOf(c) >= 0 && p.chars().filter(x -> x == c).count() > 1;
    }

    private static int firstOf(String p, String chars) {
        for (int i = 0; i < p.length(); i++) if (chars.indexOf(p.charAt(i)) >= 0) return i;
        return -1;
    }

    private static int lastOf(String p, String chars) {
        for (int i = p.length() - 1; i >= 0; i--) if (chars.indexOf(p.charAt(i)) >= 0) return i;
        return -1;
    }

    /** `X(3)9(2)` as `XXX99`. */
    static String expand(String picture) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < picture.length(); i++) {
            char c = picture.charAt(i);
            if (c == '(') {
                int close = picture.indexOf(')', i);
                int n = Integer.parseInt(picture.substring(i + 1, close).trim());
                char last = out.charAt(out.length() - 1);
                out.append(String.valueOf(last).repeat(n - 1));
                i = close;
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }
}
