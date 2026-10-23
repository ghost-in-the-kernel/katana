# Navigation: each agent's debrief on finding its way (R-079)

After a task, the agent that did it adds an entry: what slowed it down or misled it in finding its way (a missing
row, a stale path, a doc that disagreed with the code, an instruction it met only through a failing test), and
what it suggests. Not the task's own findings: those go in the journal.

About every ten commits Opus reviews the entries for what agents converge on: the same complaint from runs that
did not see each other. The review leaves a journal entry (how many entries, what converged, what was done, the
open questions sent to a Fable instance), fixes what is valid, then empties the entries below and moves the line
`Reviewed through:` to the review's commit. `scripts/NavigationGate.java` refuses a push past twenty commits
since it.

Reviewed through: 6da9e5405966

An entry: `### N-<n> <date>, <task> (<commits>)`, then the lines `- **Complaints:**` and `- **Suggestions:**`.

## Entries

### N-001 2026-10-23, TODO 3.1: the autotest census (ba19a11, dd7067b)
- **Complaints:** "runnable" is defined only in a package-private test method in another package, its list only
  in a gitignored generated file; `docs/checks.json` and R-056 named in no workpath file, met only through a
  failing DocTests; TODO 3.1 mixed history with what is open, and said 359 where the count is 378.
- **Suggestions:** name the list and its writer in TODO 3.1 and `docs/paths/lowering.md`; put checks.json in the
  workpath rows; split TODO items into done, open and next.

### N-002 2026-10-23, TODO 3.1: parse causes, the nine javac failures (cad2ab7 to 0968949)
- **Complaints:** an `// Unsure:` line needs a "Stays:" clause, said only by the failing test; a prediction was
  written before the cases were read; nothing lowers a nested program under a class name other than its
  program-id.
- **Suggestions:** "read the cases before predicting" in the research step; the "Stays:" rule in
  `docs/paths/process.md`; a unit test of a nested program lowered under another name.

### N-003 2026-10-23, TODO 3.1: each case's copybook path (f330318, 52e7a24)
- **Complaints:** TODO 3.1 names no workpath, so the agent guessed `docs/paths/lowering.md`; no row for the census
  tests, and `front/Copybooks.java`'s place guessed wrong (`front/katana/front/`); the census runs only through
  `gradle integration --tests`, said nowhere; 3.1's Next list said 13 cases lacked their directory where 6 did.
- **Suggestions:** a `Workpath:` line on each TODO item; a row for the census tests with its command in
  `docs/paths/lowering.md`.
