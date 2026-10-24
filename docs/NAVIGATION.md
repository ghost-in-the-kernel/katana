# Navigation: each agent's debrief on finding its way (R-079)

After a task, the agent that did it adds an entry: what slowed it down or misled it in finding its way (a missing
row, a stale path, a doc that disagreed with the code, an instruction it met only through a failing test), and
what it suggests. Not the task's own findings: those go in the journal.

About every ten commits Opus reviews the entries for what agents converge on: the same complaint from runs that
did not see each other. The review leaves a journal entry (how many entries, what converged, what was done, the
open questions sent to a Fable instance), fixes what is valid, then empties the entries below and moves the line
`Reviewed through:` to the review's commit. `scripts/NavigationGate.java` refuses a push past twenty commits
since it.

Reviewed through: 5c3a8e7

An entry: `### N-<n> <date>, <task> (<commits>)`, then the lines `- **Complaints:**` and `- **Suggestions:**`.

## Entries

### N-022 2026-10-24, navigation review 6 (J-283)
- **Complaints:** (1) The session loaded Suite Builder's CLAUDE.md first; the brief said it did not apply, and Katana's routed cleanly (CLAUDE.md, then `docs/when/handoff.md`). (2) The handoff section's review paragraph holds the procedure but not the journal entry's fields; I took them from J-279 (the last review) and J-266, as N-020 did, so the shape is still found by example. (3) Which commit `Reviewed through:` takes when the newest commits are a merge and a task's commit with its own debrief in the entries: the rule says "the last non-merge commit the review read", which I took as the task commit (5c3a8e7) because its debrief was read; it is a reading, not a stated case. (4) Two ways to count "since the last tithe": `git log --no-merges` by eye, or `TitheGate`'s loop; I counted by hand (eleven), and the gate's `verdict` is not runnable alone from the shell, so there is no one command that prints the count.
- **Suggestions:** `docs/when/handoff.md` carries the journal entry's field list (read, already fixed, converged and fixed, outside the repository, kept as evidence, open, resolved, touched) as N-020 asked. A command that prints the tithe gate's count (`git log --no-merges --format=%s | grep -n -m1 '^Tithe'` gives it, minus one) written beside the sentence on the tithe.
