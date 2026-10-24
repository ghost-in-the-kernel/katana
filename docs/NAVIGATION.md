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

### N-019 2026-10-24, the census runs each case's own command (J-278)
- **Complaints:** (1) The session loaded Suite Builder's CLAUDE.md first (named in the brief as not applying); Katana's CLAUDE.md routed cleanly. (2) Docker was down at the start (no `dockerd`, a stale `/var/run/docker.pid`); `process.md` says what to do and it worked, but the first `docker run` error is the only hint to look there. (3) `corpus/gnucobol-autotests/` holds GnuCOBOL's own source files beside `cases/` (ChangeLog, AUTHORS, INSTALL ...): R-045 keeps them off the screen, and nothing in `lowering.md` or `process.md` says to list only `cases/` and `runnable.txt`. (4) The research scan wanted "which cases run a non-plain command"; `process.md` says to scan the corpus but not how a command is found (the expected check, `AutotestRtIntegrationTest.expected`, picks it, and `build/scratch/` held an earlier agent's copy of that selection). (5) `lowering.md`'s last-but-one instruction (a test that runs lowered programs removes what they leave) described the root-folder run and was found stale only by reading the census test. (6) A test that names a case in its own mutation line (`FailSorting.of("run_file-241")`) failed after the case left the sorting; nothing says such lines exist.
- **Suggestions:** `docs/paths/lowering.md`'s census row gets "the expected check is `AutotestRtIntegrationTest.expectedCheck`" so a scan does not need a second reading of it. `CLAUDE.md`'s "Docker does not answer" line could name the symptom ("Cannot connect to the Docker daemon") so the cue is matched. A test helper that names the sorting's cases in a mutation should take them from the golden, not a literal case id.
