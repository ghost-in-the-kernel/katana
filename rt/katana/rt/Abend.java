package katana.rt;

/**
 * The run unit's abnormal end (LANE-08): its abend code and the reason, the condition the lanes
 * compare (the statement, the file status, the file), never the code, which only IBM has. The
 * generated program's `main` prints `CEE3ABD abend Unnnn` and the reason on stderr and exits with the
 * code, as the cobc lane's CEE3ABD stub does.
 */
public final class Abend extends RuntimeException {
    private final int code;

    public Abend(int code, String reason) {
        super(reason);
        this.code = code;
    }

    public int code() {
        return code;
    }

    /** The line the lanes read: `CEE3ABD abend U4038`. */
    public String line() {
        return "CEE3ABD abend U%04d".formatted(code);
    }
}
