package katana.lower;

import java.util.ArrayList;
import java.util.List;
import katana.sir.types.Header;
import katana.sir.types.Value;

/**
 * The program collating sequence as `rt/Collation` (LR 6.4 pp. 127-129): the native order when none is
 * named; an alphabet's: STANDARD-1, STANDARD-2 and ASCII the ASCII order, EBCDIC the EBCDIC order, NATIVE
 * the bytes' own; a literal alphabet each member a position (an ALSO group one, a literal's characters
 * and a THRU range one each), a numeric literal the character at that ordinal from 1. LOW-VALUE and
 * HIGH-VALUE then come from it.
 */
final class Collating {
    private Collating() {}

    /** The program collating sequence as an rt Collation: native order unless the alphabet it names says otherwise (LR 6.4 p. 123). */
    // Decision: R-019
    static String of(Exprs x) {
        return named(x, x.p.header().collating());
    }

    /** An alphabet's collating sequence by its name, as `of` reads the program's; native order for no name (SORT's COLLATING SEQUENCE phrase, LR 6.4 p. 452). */
    // Decision: R-019
    static String named(Exprs x, String name) {
        if (name.isEmpty()) return "Collation.nativeOrder()";
        for (Header.Alphabet a : x.p.header().alphabets()) {
            if (!a.name().equals(name)) continue;
            return switch (a.kind()) {
                case "NATIVE" -> "Collation.nativeOrder()";
                case "STANDARD-1", "STANDARD-2", "ASCII" -> "Collation.of(cp, java.nio.charset.StandardCharsets.US_ASCII)";
                case "EBCDIC" -> "Collation.of(cp, Codepage.EBCDIC_1140.charset)";
                case "LITERAL" -> literal(x, a);
                default -> x.unsupported("alphabet " + a.name() + " " + a.kind());
            };
        }
        return x.unsupported("PROGRAM COLLATING SEQUENCE " + name + ", no such alphabet");
    }

    /** An alphabet written as literals: its members in the order written, a THRU range or ALSO characters of one rank each as they appear. */
    private static String literal(Exprs x, Header.Alphabet a) {
        List<String> parts = new ArrayList<>();
        for (Value m : a.members()) {
            if (m instanceof Value.Expression e && e.operator().equals("THRU")) parts.add("Collation.thru(" + code(x, e.operands().get(0)) + ", " + code(x, e.operands().get(1)) + ")");
            else if (m instanceof Value.Expression e && e.operator().equals("ALSO")) parts.add("new int[] {" + String.join(", ", e.operands().stream().map(o -> code(x, o)).toList()) + "}");
            else if (m instanceof Value.Literal l && l.kind() == Value.Literal.Kind.ALPHANUMERIC) for (char c : l.payload().toCharArray()) parts.add("new int[] {" + charCode(String.valueOf(c)) + "}");
            else parts.add("new int[] {" + code(x, m) + "}");
        }
        return "Collation.literal(" + String.join(", ", parts) + ")";
    }

    /** One character's byte: an alphanumeric literal's (one character), or a numeric literal's ordinal less one. */
    private static String code(Exprs x, Value v) {
        if (v instanceof Value.Literal l && l.numeric()) return String.valueOf(Integer.parseInt(l.payload()) - 1);
        if (v instanceof Value.Literal l && l.kind() == Value.Literal.Kind.ALPHANUMERIC && l.payload().length() == 1) return charCode(l.payload());
        if (v instanceof Value.Figurative f && f.all() == null) return "(Literal.figurative(" + Emitter.quote(Exprs.singular(f.name())) + ", cp) & 0xFF)";
        return "(int) " + x.unsupported("alphabet member " + v);
    }

    private static String charCode(String c) {
        return "Classes.code(Literal.text(" + Emitter.quote(c) + ", cp))";
    }
}
