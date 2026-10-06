package katana.rt.autotest;



/** run_misc.at:23, "Comma separator without space". */
final class RunMisc23 implements Lowered {
    public String id() {
        return "run_misc-23";
    }

    public void run(Out out) {
        // DISPLAY 1,1,1: three numeric literals (the commas separators), each shown as written.
        out.noAdvancing("1", "1", "1");
    }
}
