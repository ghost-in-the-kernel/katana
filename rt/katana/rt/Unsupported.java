package katana.rt;

/** What the lowering could not lower, where a program reaches it: it stops there, saying what (the finding says it too). */
public final class Unsupported extends RuntimeException {
    private Unsupported(String what) {
        super("not lowered: " + what);
    }

    /** Stops the program; typed as any value so the line it stands in compiles. */
    public static <T> T fail(String what) {
        throw new Unsupported(what);
    }
}
