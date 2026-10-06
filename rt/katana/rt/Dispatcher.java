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

    /** A dispatcher for a program of `paragraphs` paragraphs. */
    public Dispatcher(int paragraphs) {
        altered = new int[paragraphs];
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
        return first;
    }

    /** The end of paragraph `paragraph`: back from the innermost active PERFORM whose range ends here, else on to `next`. */
    // Decision: R-021
    public int end(int paragraph, int next) {
        for (int i = depth - 1; i >= 0; i--) {
            if (ends[i] == paragraph) {
                depth = i;
                return backs[i];
            }
        }
        return next;
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
}
