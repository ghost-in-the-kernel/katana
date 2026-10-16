package katana.rt;

/** STOP RUN in any program of the run unit: the run ends, normally, the main program's `main` closing every file (research §24). */
public final class StopRun extends RuntimeException {
    public StopRun() {
        super(null, null, false, false);
    }
}
