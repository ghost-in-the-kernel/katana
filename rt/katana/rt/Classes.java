package katana.rt;

/** Class conditions (LR 6.4 pp. 268-270): NUMERIC, ALPHABETIC, ALPHABETIC-UPPER and -LOWER, over an item's bytes. */
public final class Classes {
    private Classes() {}

    /** NUMERIC: an alphanumeric item all digits; a zoned item digits with a valid sign where it has one; a packed item valid nibbles. */
    // Decision: R-019
    public static boolean numeric(Field f) {
        Attr a = f.attr();
        byte[] b = java.util.Arrays.copyOfRange(f.record().bytes(), f.offset(), f.offset() + a.length());
        Codepage cp = f.record().codepage();
        if (a.category() != Attr.Category.NUMERIC) return allDigits(b, 0, b.length, cp);
        return switch (a.usage()) {
            case DISPLAY -> zoned(a, b, cp);
            case PACKED -> packed(a, b);
            default -> true;
        };
    }

    private static boolean allDigits(byte[] b, int from, int to, Codepage cp) {
        for (int k = from; k < to; k++) if (b[k] != cp.digit(cp.digitOf(b[k]), 0) || cp.digitOf(b[k]) > 9) return false;
        return true;
    }

    private static boolean zoned(Attr a, byte[] b, Codepage cp) {
        if (!a.signed()) return allDigits(b, 0, b.length, cp);
        if (a.signSeparate()) {
            int sign = a.signLeading() ? 0 : b.length - 1;
            boolean ok = b[sign] == cp.character('+') || b[sign] == cp.character('-');
            return ok && allDigits(b, a.signLeading() ? 1 : 0, a.signLeading() ? b.length : b.length - 1, cp);
        }
        int over = a.signLeading() ? 0 : b.length - 1;
        int d = cp.digitOf(b[over]);
        boolean signOk = d <= 9 && (b[over] == cp.digit(d, 1) || b[over] == cp.digit(d, -1) || b[over] == cp.digit(d, 0));
        return signOk && allDigits(b, a.signLeading() ? 1 : 0, a.signLeading() ? b.length : b.length - 1, cp);
    }

    private static boolean packed(Attr a, byte[] b) {
        for (int k = 0; k < b.length; k++) {
            int hi = (b[k] >> 4) & 0xF, lo = b[k] & 0xF;
            if (hi > 9 || (k < b.length - 1 && lo > 9)) return false;
        }
        int sign = b[b.length - 1] & 0xF;
        return a.signed() ? sign == 0xC || sign == 0xD || sign == 0xF : sign == 0xF;
    }

    /** ALPHABETIC (letters of either case and spaces), -UPPER, -LOWER. */
    public static boolean alphabetic(Field f, boolean upper, boolean lower) {
        String s = new String(f.record().bytes(), f.offset(), f.attr().length(), f.record().codepage().charset);
        for (char c : s.toCharArray()) if (c != ' ' && !((upper && c >= 'A' && c <= 'Z') || (lower && c >= 'a' && c <= 'z'))) return false;
        return true;
    }
}
