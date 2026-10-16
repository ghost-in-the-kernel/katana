package katana.rt;

/**
 * Procedure debugging (research §17): the DEBUG-ITEM special register, laid out as LR 6.4 p. 19 gives
 * it, and what caused control to enter a procedure, so a USE FOR DEBUGGING section is told it (Table 2,
 * p. 20). A debugging section runs only when both switches are on (p. 759): the compile-time one, WITH
 * DEBUGGING MODE, which the lowering passes; the object-time one, IBM's DEBUG runtime option, the
 * property `katana.debug` (Y or N, N when absent, as NODEBUG is IBM's default, p. 760; LANE-13).
 */
public final class Debug {
    /** DEBUG-CONTENTS' length: Katana's n, the 30 characters of the longest procedure-name it can hold. */
    public static final int CONTENTS = 30;

    public final Record item;
    public final Field all, line, name, contents;
    private final Codepage cp;
    private final boolean on;
    private String cause = "START PROGRAM", seenCause = "";
    private int causeLine, seenLine;
    private boolean named, seenNamed;

    /** DEBUG-ITEM in a program's codepage; `compiled` is WITH DEBUGGING MODE. */
    public Debug(Codepage cp, boolean compiled) {
        this.cp = cp;
        this.on = compiled && "Y".equalsIgnoreCase(System.getProperty("katana.debug", "N"));
        item = Record.of(56 + CONTENTS, cp);
        all = new Field(item, 0, Attr.text(56 + CONTENTS, false));
        line = new Field(item, 0, Attr.text(6, false));
        name = new Field(item, 7, Attr.text(30, false));
        contents = new Field(item, 56, Attr.text(CONTENTS, false));
    }

    /** Whether debugging sections run: both switches on. */
    public boolean on() {
        return on;
    }

    /** The run begins: the first nondeclarative procedure is entered, "START PROGRAM", at its line. */
    public void start(int procedureLine) {
        cause = "START PROGRAM";
        causeLine = procedureLine;
        named = false;
    }

    /** A PERFORM at `statementLine` transfers control to `first`, once for each repetition: "PERFORM LOOP"; `section`, whether it named a section. */
    public int perform(int first, int statementLine, boolean section) {
        cause = "PERFORM LOOP";
        causeLine = statementLine;
        named = section;
        return first;
    }

    /** A GO TO at `statementLine` transfers control: DEBUG-CONTENTS spaces; `section`, whether it named a section. */
    public void goTo(int statementLine, boolean section) {
        cause = "";
        causeLine = statementLine;
        named = section;
    }

    /** A paragraph's end: control falling into `into` (not back to a PERFORM) is "FALL THROUGH", from `fromLine`. */
    public int fell(int next, int into, int fromLine) {
        if (next == into) {
            cause = "FALL THROUGH";
            causeLine = fromLine;
            named = false;
        }
        return next;
    }

    /** Control enters a paragraph at its start: the cause is kept for its debugging sections, whatever they do. */
    public void entered() {
        seenCause = cause;
        seenLine = causeLine;
        seenNamed = named;
    }

    /**
     * Whether entering a section's first paragraph executes the section: a PERFORM or GO TO that named
     * it, control falling through its header, or the run's start; not a transfer that named the
     * paragraph (NIST DB105A's PERFORM of a section's only paragraph).
     */
    public boolean section() {
        return seenNamed || seenCause.equals("FALL THROUGH") || seenCause.equals("START PROGRAM");
    }

    /** DEBUG-ITEM for the procedure entered: spaces, then its line, name and contents (LR 6.4 pp. 19-20). */
    public void fill(String procedure) {
        fill(seenLine, procedure, seenCause);
    }

    /** DEBUG-ITEM after an ALTER at `statementLine` of `procedure`: DEBUG-CONTENTS the TO PROCEED TO name. */
    public void altered(int statementLine, String procedure, String proceedTo) {
        fill(statementLine, procedure, proceedTo);
    }

    private void fill(int statementLine, String procedure, String text) {
        java.util.Arrays.fill(item.bytes(), cp.character(' '));
        put(0, 6, String.format("%06d", statementLine));
        put(7, 30, procedure);
        put(56, CONTENTS, text);
    }

    /** An alphanumeric MOVE of `text` into `length` bytes at `offset`: left-justified, cut, spaces after. */
    private void put(int offset, int length, String text) {
        for (int i = 0; i < length && i < text.length(); i++) item.bytes()[offset + i] = cp.character(text.charAt(i));
    }
}
