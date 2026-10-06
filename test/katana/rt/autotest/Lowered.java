package katana.rt.autotest;

/**
 * An autotest program lowered by hand against `rt/` alone (phase 3.1's check): what the lowering (3.3)
 * will write for it, so `rt/` is tried on the testsuite's programs before the lowering exists. Each
 * implementation names its case (`file-line`, as `corpus/gnucobol-autotests/runnable.txt` lists it).
 */
interface Lowered {
    /** The case, `run_fundamental-2628`. */
    String id();

    /** Runs the program, its DISPLAY output into `out`. */
    void run(Out out);
}
