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

### N-014 2026-10-24, ROUNDED MODE (J-268; 2 commits)
- **Complaints:** the session loaded Suite Builder's CLAUDE.md first; Katana's routing worked once opened. `docker image ls` showed only `katana:dev` and the daemon was down with a stale `/var/run/docker.pid`: `process.md` has both fixes, but not under one heading an agent reaches before its first docker call (it is in the Instructions list, found only by reading the file whole). TODO 3.1 names the item ("`MODE` after ROUNDED (10+)") but not which cases, nor that "(10+)" is exactly ten; the golden's `extraneous input 'MODE'` lines were the quickest list. Nothing says what an agent does when the largest cause is a form whose IBM status is only recollection: the brief had to say it (R-089 covers acceptance flags, not a grammar form).
- **Suggestions:** a line in `docs/paths/front.md` for "a new grammar form": confirm in `corpus/ibm-manuals/` or leave an error and say so, with its meaning built before it parses; and the case lists by cause under 3.1 (a `grep` of the golden is enough, say so in `lowering.md`).
