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

### N-007 2026-10-23, 3.1 R-083 to R-085 (this commit)
- **Complaints:** (1) TODO 3.1's Next paragraph, R-083 to R-085 and `CompileFlags` were enough to build from; the only search was for
  `Settings` callers (as N-006 said). (2) The decisions' "Held by" cite `DirectivesTest` and the golden "once built", but no doc says
  the census golden is written before the floor assertion runs, so `-Dkatana.record=true` leaves a dirty golden when a floor
  fails; I reverted by hand. (3) A case mapped by a decision moves FLAGPASS to PASS (`run_fundamental-4746`, `-std=ibm-strict`),
  which lowers the FLAGPASS floor by one while the total passing stays 59; neither J-256's ruling nor the brief says what a floor
  does then, so I stopped. (4) The fast loop does not run the census, so a floor breach shows only in `gradle integration`.
- **Suggestions:** state in TODO 3.1 or `lowering.md` what a legitimate move between the two floored stages does (a floor on
  PASS + FLAGPASS together would not need lowering); have the census check floors before writing the golden.

### N-008 2026-10-23, 3.1 R-083 to R-085 under the new floors (this commit)
- **Complaints:** (1) CLAUDE.md's "When" lines, `process.md` and the handoff file led straight to the work; N-007's two
  suggestions (what a floor does on a move up; floors before the golden) were exactly the task, which worked. (2) The
  Docker daemon was down with a stale `/var/run/docker.pid`; `process.md` says how to restart it, which was enough, but
  nothing in CLAUDE.md says the daemon may need it. (3) The census (21 s) needs `corpus/gnucobol-autotests/cases` already
  written; here they were. (4) A floor named in four places (census javadoc, TODO 3.1, `lowering.md`, `checks.json`) and
  the "Held by" lines said "once built"; I found them by grepping "floor". (5) The golden's file mixes with a rule that it
  changes in its own commit (`oracle.md`), while a census-test change and its golden cannot be separated by a green state.
- **Suggestions:** one place for the floors' rule (`lowering.md`'s census row) and the others point to it; say in
  `oracle.md` whether a golden's own commit may follow a test change that leaves it red for one commit.
