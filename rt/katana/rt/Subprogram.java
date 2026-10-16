package katana.rt;

/**
 * A lowered program as another program CALLs it (LANE-19): entered at the start of its procedure with
 * the CALL's arguments, back at EXIT PROGRAM or GOBACK (research §24).
 */
public interface Subprogram {
    /** One CALL: each argument a BY REFERENCE item or a BY CONTENT copy, null where none is passed; returns when the program does. */
    void call(Field[] using);

    /** Whether it has the INITIAL attribute, so that each CALL finds it in its initial state (LR 6.4 p. 103). */
    boolean initial();

    /** CANCEL: its files closed, as are those of the programs it contains (LR 6.4 p. 327); the caller then forgets it. */
    void cancel();

    /** The run's end: every file it and the programs it contains have closed (LANE-08). */
    void end();
}
