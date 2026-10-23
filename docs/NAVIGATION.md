# Navigation: each agent's debrief on finding its way (R-079)

After a task, the agent that did it adds an entry: what slowed it down or misled it in finding its way (a missing
row, a stale path, a doc that disagreed with the code, an instruction it met only through a failing test), and
what it suggests. Not the task's own findings: those go in the journal.

About every ten commits Opus reviews the entries for what agents converge on: the same complaint from runs that
did not see each other. The review leaves a journal entry (how many entries, what converged, what was done, the
open questions sent to a Fable instance), fixes what is valid, then empties the entries below and moves the line
`Reviewed through:` to the review's commit. `scripts/NavigationGate.java` refuses a push past twenty commits
since it.

Reviewed through: 6bd8eeab6af6

An entry: `### N-<n> <date>, <task> (<commits>)`, then the lines `- **Complaints:**` and `- **Suggestions:**`.

## Entries

(none since review 2, J-260)

### N-009 2026-10-23, the reasons for the unmapped census flags (J-261)
- **Complaints:** Docker was down at the start (the procedure in `docs/paths/process.md` worked). TODO 3.1's text is the only
  place that lists the flags by count and is long; the golden is the better list (grep its FLAG lines). `docs/when/handoff.md`
  says to run "the item's own check" but 3.1's Check is the whole 359; the census plus the fast loop was the reading I took.
- **Suggestions:** say in 3.1's Next that the golden's FLAG lines are the current flag list; say in `docs/paths/lowering.md`
  what "the item's own check" is for a census change.

### N-010 2026-10-23, R-087 to R-089 in the census (J-263)
- **Complaints:** Docker was down again (the procedure worked: no dockerd, stale pid file). `corpus/gnucobol-autotests/` holds
  GnuCOBOL's whole source tree beside `cases/`; nothing says in the path files to open only `cases/<id>/flags.txt`, so I
  stayed off the rest by R-045 alone. The decisions' "Held by" lines say "once built: TODO 3.1" and are now stale.
  `docs/when/handoff.md` still does not say what "the item's own check" is for a census change.
- **Suggestions:** one line in `docs/paths/oracle.md` naming `cases/<id>/{prog.cob,flags.txt}` as the only readable part of that
  folder; update the "Held by" lines of R-087 to R-089.
