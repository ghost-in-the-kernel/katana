package katana.rt;

import java.util.Arrays;

/**
 * Control flow in the literal lowering (R-021): a program runs as `for (;;) switch (pc)`, each case a
 * paragraph or the part of one after a PERFORM, falling through to the next as written, GO TO an
 * assignment to `pc`. This holds the active PERFORMs: each the paragraph that ends its range and where
 * control goes back to. At the end of every paragraph the lowering asks `end`: reaching the exit of any
 * active PERFORM returns from it, the innermost first, and the PERFORMs begun inside it are dropped
 * (IBM's text leaves control passing through another active PERFORM's exit undefined, LR p. 414; this
 * is cobc -std=ibm's behaviour, J-113). ALTER changes a GO TO's target, kept here by paragraph.
 */
public final class Dispatcher {
    private int[] ends = new int[16], backs = new int[16];
    private int depth;
    private final int[] altered;
    private final int[] segments;
    private int current = -1;

    /** A dispatcher for a program of `paragraphs` paragraphs. */
    public Dispatcher(int paragraphs) {
        this(new int[paragraphs]);
        Arrays.fill(segments, -1);
    }

    /** A dispatcher for a segmented program: each paragraph's section's priority-number, -1 for none. */
    public Dispatcher(int[] segments) {
        this.segments = segments;
        altered = new int[segments.length];
        Arrays.fill(altered, -1);
    }

    /** PERFORM: remembers the range's last paragraph and where to come back to; returns where to go, the range's first label. */
    // Decision: R-021
    public int perform(int first, int lastParagraph, int back) {
        if (depth == ends.length) {
            ends = Arrays.copyOf(ends, depth * 2);
            backs = Arrays.copyOf(backs, depth * 2);
        }
        ends[depth] = lastParagraph;
        backs[depth++] = back;
        if (PerformTrace.ON) PerformTrace.perform(first, lastParagraph);
        return first;
    }

    /** The end of paragraph `paragraph`: back from the innermost active PERFORM whose range ends here, else on to `next`. */
    // Decision: R-021
    public int end(int paragraph, int next) {
        for (int i = depth - 1; i >= 0; i--) {
            if (ends[i] == paragraph) {
                if (PerformTrace.ON) PerformTrace.returned(paragraph, depth - 1 - i);
                depth = i;
                return backs[i];
            }
        }
        if (PerformTrace.ON) PerformTrace.fell(paragraph, next);
        return next;
    }

    /** EXIT PROGRAM or GOBACK back to the caller: "the ends of the ranges of all executed PERFORM statements are considered to have been reached" (LR 6.4 p. 343). */
    // Decision: R-021
    public void exited() {
        if (PerformTrace.ON) PerformTrace.exited(depth);
        depth = 0;
    }

    /** How many PERFORMs are active. */
    public int depth() {
        return depth;
    }

    /** ALTER paragraph TO PROCEED TO target: the paragraph's GO TO now goes to `target`. */
    // Decision: R-021
    public void alter(int paragraph, int target) {
        altered[paragraph] = target;
    }

    /** Where the GO TO in an alterable paragraph goes: its altered target, or the one written. */
    public int goTo(int paragraph, int written) {
        return altered[paragraph] < 0 ? written : altered[paragraph];
    }

    /**
     * Control enters paragraph `paragraph` at its start (falling into it, GO TO, PERFORM): an independent
     * segment, 50 through 99, entered from a segment with a different priority-number is in its initial
     * state, its altered GO TOs as written (LR 6.4 p. 265, read as R1 in research §16).
     */
    // Decision: R-021
    public void enter(int paragraph) {
        int segment = segments[paragraph];
        if (segment >= 50 && segment != current) {
            for (int i = 0; i < segments.length; i++) if (segments[i] == segment) altered[i] = -1;
        }
        current = segment;
    }

    /** Control comes back to paragraph `paragraph` after a PERFORM or within it: its segment is current, unchanged in state (P2, research §16). */
    // Unsure: P2 is a reading; NIST SG passes under P1 too (J-175). Stays: an independent section that alters its own GO TO, PERFORMs a range in another segment and then runs that GO TO, on a mainframe.
    public void resume(int paragraph) {
        current = segments[paragraph];
    }
}
