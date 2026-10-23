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

### N-004 2026-10-23, TODO 3.1: the `>>` directives (research 58, R-081)
- **Complaints:** the Docker image is tagged `katana:dev`, not `katana` as CLAUDE.md's command says, and the daemon was
  not running (R-077's recipe worked; the tag I added by hand); TODO 3.1 names no command for the census and no workpath
  (the same as N-003); the Next list counted the `>>` cause by one error message ("mismatched input '>'"), so a case with
  "extraneous input '>'" was missed; R-045's reach is unclear for a case's `check-N.sh` (it holds the compile flags the
  census needs, and the expected stdout beside it), so I grepped for flags only; `docs/paths/front.md` has no row for
  `front/Lines.java`'s directives or for how to read a case's `case.txt`.
- **Suggestions:** one line in CLAUDE.md or `docs/paths/process.md` on the image name and how to build it; the census
  command in `docs/paths/lowering.md`; the census records each case's compile flags (from `check-0.sh`, flags only) so a
  FAIL caused by `-D` or `-fdebugging-line` is told apart from a real difference.

### N-005 2026-10-23, TODO 3.1: the `>>` directives amended (research 59, R-081)
- **Complaints:** I did not read `docs/when/handoff.md` on finishing, though CLAUDE.md's "When" line names it: I had
  the report format in my brief and went to the report, so the debrief step was missed until asked; the "When" lines sit
  in the middle of CLAUDE.md and nothing at the end of a run points back to them. Docker answered nothing at first and
  the image is `katana:dev` (as N-004); `nextId` says to fetch main first, but the sandbox has no route to it. A new
  finding code has no row in `docs/paths/front.md` (`Finding.Kind`, FINDINGS.md, `FindingsDocTest`'s regex, the proof
  report's sections were found by grepping); nor does a new fixture keyword (`Schema`, `FixtureCheck`, FIXTURE.md's
  table and invariant). IBM's manuals are unreachable from the sandbox and no copy is kept, so a research section on a
  directive can only say "unconfirmed".
- **Suggestions:** put "finishing: docs/when/handoff.md" in the brief's closing line, or last in CLAUDE.md; add rows to
  `docs/paths/front.md` and `oracle.md` for a new finding kind and a new fixture keyword, each listing the files that
  must change together; keep the few IBM pages a decision rests on as text under `docs/research/`.
