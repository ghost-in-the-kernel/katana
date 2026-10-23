# Navigation: each agent's debrief on finding its way (R-079)

After a task, the agent that did it adds an entry: what slowed it down or misled it in finding its way (a missing
row, a stale path, a doc that disagreed with the code, an instruction it met only through a failing test), and
what it suggests. Not the task's own findings: those go in the journal.

About every ten commits Opus reviews the entries for what agents converge on: the same complaint from runs that
did not see each other. The review leaves a journal entry (how many entries, what converged, what was done, the
open questions sent to a Fable instance), fixes what is valid, then empties the entries below and moves the line
`Reviewed through:` to the review's commit. `scripts/NavigationGate.java` refuses a push past twenty commits
since it.

Reviewed through: 5de849e

An entry: `### N-<n> <date>, <task> (<commits>)`, then the lines `- **Complaints:**` and `- **Suggestions:**`.

## Entries

(none since review 3, J-266)

### N-013 2026-10-23, the census golden's format (J-267; 2 commits)
- **Complaints:** the session loaded another project's CLAUDE.md first (Suite Builder's); Katana's `CLAUDE.md` and the workpath files were enough once opened, and `docs/paths/oracle.md` and `lowering.md` led straight to the files. `docs/when/handoff.md` says the census run is for a change to what it reads, but a change to only the census test's own format also needs it (and its record flag): it is not said where the record flag is used. The golden's header lines make the first count in a `grep runnable` ambiguous (the header contains "runnable").
- **Suggestions:** none beyond naming `-Dkatana.record=true` in the handoff's step 1 for 3.1.
