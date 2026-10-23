# Navigation: each agent's debrief on finding its way (R-079)

After a task, the agent that did it adds an entry: what slowed it down or misled it in finding its way (a missing
row, a stale path, a doc that disagreed with the code, an instruction it met only through a failing test), and
what it suggests. Not the task's own findings: those go in the journal.

About every ten commits Opus reviews the entries for what agents converge on: the same complaint from runs that
did not see each other. The review leaves a journal entry (how many entries, what converged, what was done, the
open questions sent to a Fable instance), fixes what is valid, then empties the entries below and moves the line
`Reviewed through:` to the review's commit. `scripts/NavigationGate.java` refuses a push past twenty commits
since it.

Reviewed through: 9cdf2cc

An entry: `### N-<n> <date>, <task> (<commits>)`, then the lines `- **Complaints:**` and `- **Suggestions:**`.

## Entries

### N-006 2026-10-23, 3.1 compile flags (this commit)
- **Complaints:** (1) the CLAUDE.md "When" lines and the task row led straight to the census and `Autotests`, which worked well;
  but `docs/paths/oracle.md` has no row for `oracle/autotest/Autotests.java`, though TODO's Next names it as a workpath, so I
  found the reader by grepping. (2) The census's inputs (`corpus/gnucobol-autotests/cases/`) are written by
  `AutotestCorpusIntegrationTest`, so a change to what `Autotests.write` writes needs that test run before the census; the
  lowering workpath says the list is written there, not that the cases are. (3) `gradle kernelMap` and `gradle journalIndex` are
  needed after a change that adds a method or an entry; I met `kernelMap` only through a failing `DocTests`. (4) A `Settings`
  record change: no doc says which callers construct one; I grepped.
  (5) A floor that lives in the golden is lowered by a re-record without a trace; `floors.txt` now holds the census's.
- **Suggestions:** an `Autotests` row in `docs/paths/oracle.md`; in `lowering.md`'s census row, "run `AutotestCorpusIntegrationTest`
  first when the reader's output changes"; list `kernelMap` in the "before calling work done" line of CLAUDE.md.
