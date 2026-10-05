package katana.sir.types;

import java.util.List;
import katana.front.Mapped;

/** A CALL: whom (a literal or a field), its USING arguments each BY REFERENCE, CONTENT or VALUE, and its RETURNING field (null for none). */
public record Call(Value target, List<Argument> using, Value returning, Mapped.Origin origin) {
    public enum Mode { REFERENCE, CONTENT, VALUE }

    public record Argument(Mode mode, Value value) {}

    public Call {
        using = List.copyOf(using);
        Invalid.origin(origin, "CALL");
    }
}
