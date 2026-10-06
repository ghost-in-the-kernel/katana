package katana.rt;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;

/** Where DISPLAY writes: the bytes of each operand (a field as `Display` writes it, or a literal's bytes), then a new line unless NO ADVANCING. */
public final class Sysout {
    private final OutputStream out;
    private final Display.Sign sign;

    public Sysout(OutputStream out, Display.Sign sign) {
        this.out = out;
        this.sign = sign;
    }

    /** DISPLAY of the operands, each a `Field` or a `byte[]`. */
    // Decision: R-019
    public void display(boolean advancing, Object... operands) {
        try {
            for (Object o : operands) out.write(o instanceof Field f ? Display.of(f, sign) : (byte[]) o);
            if (advancing) out.write('\n');
            out.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
