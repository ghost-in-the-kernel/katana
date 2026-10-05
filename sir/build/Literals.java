package katana.sir.build;

import java.math.BigDecimal;
import java.util.Locale;
import katana.sir.types.Value;
import katana.sir.types.Value.Literal.Kind;

/** A literal's text to its kind and payload (tithe pass 2, A3; SIR-08), the text alone deciding. */
final class Literals {
    private Literals() {}

    /** The literal a text is: by its prefix and quote, else by its digits, else a word. */
    static Value.Literal of(String text) {
        String upper = text.toUpperCase(Locale.ROOT);
        if (upper.startsWith("DFHRESP") || upper.startsWith("DFHVALUE")) return new Value.Literal(text, Kind.CICS, text, 0);
        if (upper.equals("TRUE") || upper.equals("FALSE")) return new Value.Literal(text, Kind.BOOLEAN, upper, 0);
        int quote = quote(text);
        if (quote < 0) return number(text);
        String chars = unquote(text.substring(quote));
        return switch (quote == 0 ? ' ' : upper.charAt(0)) {
            case 'X' -> new Value.Literal(text, Kind.HEX, chars.toUpperCase(Locale.ROOT), 0);
            case 'Z' -> new Value.Literal(text, Kind.NULL_TERMINATED, chars, 0);
            case 'N' -> new Value.Literal(text, Kind.NATIONAL, chars, 0);
            case 'G' -> new Value.Literal(text, Kind.DBCS, chars, 0);
            default -> new Value.Literal(text, Kind.ALPHANUMERIC, chars, 0);
        };
    }

    /** Where the quoted part starts: 0 for a plain literal, 1 after X, Z, N or G; -1 for none. */
    private static int quote(String text) {
        if (text.startsWith("'") || text.startsWith("\"")) return 0;
        if (text.length() > 1 && (text.charAt(1) == '\'' || text.charAt(1) == '"') && "XZNGxzng".indexOf(text.charAt(0)) >= 0) return 1;
        return -1;
    }

    /** A quoted string's characters: its delimiters dropped, a doubled delimiter one. */
    private static String unquote(String quoted) {
        String q = quoted.substring(0, 1);
        return quoted.substring(1, quoted.length() - 1).replace(q + q, q);
    }

    /** A numeric literal's unscaled value and scale; with an exponent, FLOAT, its scale less the exponent. */
    private static Value.Literal number(String text) {
        String[] parts = text.toUpperCase(Locale.ROOT).split("E", 2);
        BigDecimal mantissa = new BigDecimal(parts[0].replace(',', '.'));
        if (parts.length == 1) return new Value.Literal(text, Kind.NUMERIC, unscaled(mantissa, parts[0]), mantissa.scale());
        int exponent = Integer.parseInt(parts[1].replace("+", ""));
        return new Value.Literal(text, Kind.FLOAT, unscaled(mantissa, parts[0]), mantissa.scale() - exponent);
    }

    /** The unscaled value, a minus zero kept as `-0`: cobc moves it as a negative zero (LiteralIntegrationTest). */
    private static String unscaled(BigDecimal mantissa, String written) {
        return mantissa.signum() == 0 && written.startsWith("-") ? "-0" : mantissa.unscaledValue().toString();
    }
}
