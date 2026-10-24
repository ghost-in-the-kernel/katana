# Navigation: each agent's debrief on finding its way (R-079)

After a task, the agent that did it adds an entry: what slowed it down or misled it in finding its way (a missing
row, a stale path, a doc that disagreed with the code, an instruction it met only through a failing test), and
what it suggests. Not the task's own findings: those go in the journal.

About every ten commits Opus reviews the entries for what agents converge on: the same complaint from runs that
did not see each other. The review leaves a journal entry (how many entries, what converged, what was done, the
open questions sent to a Fable instance), fixes what is valid, then empties the entries below and moves the line
`Reviewed through:` to the review's commit. `scripts/NavigationGate.java` refuses a push past twenty commits
since it.

Reviewed through: 9c8e22b

An entry: `### N-<n> <date>, <task> (<commits>)`, then the lines `- **Complaints:**` and `- **Suggestions:**`.

## Entries

### N-018 2026-10-24, R-090 steps 3 and 4 (J-277)
- **Complaints:** (1) The session loaded another project's instructions (Suite Builder's CLAUDE.md, named in the brief as not applying); Katana's CLAUDE.md was the right start and routed cleanly to `docs/paths/front.md`, but that file's rows say where the data map and the lowering are by directory, not that a throw in `sir/build` ends a census case at NOLOWER and a `Unsupported.fail` finding at NORUN; that took reading `AutotestsLoweredIntegrationTest.rawStageOf` and `stageOf` (a FLAG holds any raw stage that is not PASS), and research 63 had guessed NOLOWER for a finding. (2) `build/corpus-nist` is empty in a fresh tree until `CorpusIntegrationTest` has run; `process.md` says so, but a scan wants it before the first prediction, so the first step was a three-minute run. (3) A level-number scan by regex finds table values, REPORT COLUMNS and THRU ranges (`91`, `60`, `64 65`); only the files read show they are not levels. (4) TODO 3.1's numbered list repeats the sorting's counts and key names, so a step that removes a key leaves four stale phrases to find by grep.
- **Suggestions:** `docs/paths/front.md` gets a row sentence: "in the census a throw in the IR build or the lowering is NOLOWER, a finding is NORUN when its statement is reached". `docs/paths/process.md`'s scan instruction names the census golden's stage as readable straight from `lowered.golden` for every runnable case (done here by `grep`). TODO 3.1's sorting list could point at `grep FAIL lowered.golden` and carry no counts.
