package katana.rt;

import java.math.BigInteger;

/** Literals and figurative constants as sending items, in the program's codepage (the lowering writes each as one of these). */
public final class Literal {
    private Literal() {}

    /** A numeric literal: its unscaled value, signed, and its scale, as a signed zoned item of its digits. */
    public static Field number(String unscaled, int scale, Codepage cp) {
        BigInteger v = new BigInteger(unscaled);
        int digits = Math.max(1, Math.max(v.abs().toString().length(), scale));
        Field f = new Field(Record.of(digits, cp), 0, Attr.zoned(digits, scale, true));
        f.put(new Decimal(v, scale));
        return f;
    }

    /** An alphanumeric literal's characters. */
    public static Field text(String chars, Codepage cp) {
        byte[] b = chars.getBytes(cp.charset);
        return new Field(new Record(b, cp), 0, Attr.text(b.length, false));
    }

    /** A hexadecimal literal's bytes, as written (`C1F0`). */
    public static Field hex(String hex, Codepage cp) {
        byte[] b = java.util.HexFormat.of().parseHex(hex);
        return new Field(new Record(b, cp), 0, Attr.text(b.length, false));
    }

    /** A field's bytes, or a literal's (DISPLAY, comparison). */
    public static byte[] bytes(Field f) {
        return java.util.Arrays.copyOfRange(f.record().bytes(), f.offset(), f.offset() + f.attr().length());
    }

    /** A figurative constant's character in a codepage: SPACE, ZERO, QUOTE, HIGH-VALUE (the byte 0xFF), LOW-VALUE (0x00). */
    public static byte figurative(String name, Codepage cp) {
        return switch (name) {
            case "SPACE" -> cp.character(' ');
            case "ZERO" -> cp.character('0');
            case "QUOTE" -> cp.character('"');
            case "HIGH-VALUE" -> (byte) 0xFF;
            case "LOW-VALUE" -> 0;
            default -> throw new IllegalArgumentException("figurative constant " + name);
        };
    }
}
