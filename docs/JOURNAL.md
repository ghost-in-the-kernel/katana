# Journal: every event in the work, as a record a researcher can count

One entry per event: a contradiction found, a decision taken, a failure caught or missed, a change of
direction, a measurement. Not a restatement of the commit log (`git log` has what changed) and not
a status list (`DONE.md`, `TODO.md`) and not the design (`theGhost.md`): the journal records *what
happened while the work was done* — what was intended, what was found, what was on the table, how it
was resolved, and what it touched. Fixed fields, so entries can be counted and compared across
projects (Suite Builder keeps the same journal under its G-162). Newest last. Written by whoever was
there, in the same commit as the event when possible; an agent writes its own entries (R-046).

Field meanings. **Who**: Ghost, or the agent by its model name, or both. **Intent**: what the work
was trying to do when the event happened. **Found**: the fact the event turned on. **Options**: what
was considered, including the one taken. **Resolved**: what was done, by whom. **Touched**: decisions,
metrics, documents or modules the event changed or created. **Caught by**: for a failure, what
noticed it (a test, a run, a person, an agent's habit, luck). **Cost**: wall time or commits, where
known. **Class**: one of `contradiction` (the plan disagreed with itself or with reality),
`ambiguity` (two readings, both reasonable), `failure-caught`, `failure-missed`, `decision`,
`measurement`, `direction`.

---

## 2026-10-04

### J-001 Glossary banned the word the plan used
- **Who:** Opus; decided by Ghost with Fable. **Class:** contradiction.
- **Intent:** Phase 0.2, writing `modules.json` and the glossary test.
- **Found:** `GLOSSARY.md` listed "module" as a forbidden alias of "program"; the roadmap used
  "module" for Katana's own parts throughout; the planned test would have failed on the plan.
- **Options:** rename Katana's parts; drop the alias; make "module" a term.
- **Resolved:** "module" a term of its own; "NIST module" left to NIST. Smallest change, by Opus;
  confirmed in the plan by Fable.
- **Touched:** `GLOSSARY.md`, ROADMAP §3.3, §9.

### J-002 The plan said twelve modules; it listed thirteen
- **Who:** Opus; fixed by Fable. **Class:** contradiction.
- **Found:** a miscount in the plan's prose. **Resolved:** the count removed from prose; it lives only
  in `modules.json`. **Touched:** ROADMAP §3.3, §6 0.2, §9; R-042 text.

### J-003 "A layer imports only downward" contradicted the plan's own listing
- **Who:** Opus; fixed by Fable. **Class:** contradiction.
- **Found:** `passes/` imports `ledger/`, listed after it. **Options:** reorder the listing; drop the
  rule; make the import lists the law. **Resolved:** the import lists in `modules.json` are the rule;
  the order is prose. **Touched:** ROADMAP §3.3.

### J-004 The glossary test could not work as specified
- **Who:** Opus proposed; Ghost decided; Fable recorded. **Class:** ambiguity → decision.
- **Found:** "a listed alias outside the glossary fails" gave 60+ failures on correct prose: "run",
  "source", "error", "map" are wrong for one concept and right for another; "golden" was both a term
  and an alias.
- **Options:** enforce all (switch the test off in practice); enforce none; enforce only marked
  aliases.
- **Resolved:** aliases in backticks are enforced, others are a review guide; `docs/research/`
  exempt; "golden" a term only. **Touched:** R-039, `GLOSSARY.md` header, `GlossaryTest`.
- **Note:** cites Suite Builder G-107 (rules that fired on good code became warnings).

### J-005 R-038's wording would have forced duplicated helpers
- **Who:** Opus; Fable reworded. **Class:** contradiction.
- **Found:** "single-file Java programs run with `java test/X.java`" cannot share a helper file; each
  test would carry its own `Check`/`Json`, against the duplicate-helpers metric.
- **Resolved:** plain Java 21 classes compiled together by `test/run.sh`, no dependency; the decision
  text says why the first wording was wrong. **Touched:** R-038.

### J-006 `gradle test` silently skipped the doc tests after a doc-only change
- **Who:** Opus. **Class:** failure-caught (would have been failure-missed in daily use).
- **Intent:** Phase 0.3, the Gradle build.
- **Found:** Gradle saw the test classes unchanged, marked the task up to date, and a broken
  `TODO.md` or `theGhost.md` passed. CI never affected (fresh checkout).
- **Caught by:** an agent's habit — Opus deleted a test file in a scratch clone and watched the build
  stay green. **Resolved:** the tests always run (c5d548f, its own commit). **Cost:** one commit.
- **Note:** the first instance in Katana of "prefer a test to a belief" catching something; cited in
  `docs/GHOST-IN-THE-KERNEL.md`.

### J-007 Fable pushed to main without running the tests; main red ten minutes
- **Who:** Fable (fault); Opus (fix). **Class:** failure-caught.
- **Intent:** recording R-042 (a doc-only change).
- **Found:** the metrics test did not yet understand "not armed"; the added lines moved the docs
  ratio 1.13 → 1.14; CI failed on 62ccd05.
- **Caught by:** CI. **Resolved:** Opus pushed the "not armed" handling alone (46be673). Fable runs
  the fast loop before every push from then on. **Touched:** R-040 applied to both agents.
- **Note:** the same "I only changed documents" reasoning as J-006, one hour after J-006 showed it
  wrong.

### J-008 A ratchet that could only ever say "worse"
- **Who:** Opus raised; Ghost decided; Fable recorded. **Class:** ambiguity → decision.
- **Found:** "docs lines per source line" (target < 0.35) was 1.13 in a phase with no code and no
  path to the target until phase 2; ratcheting it trains agents to ignore it.
- **Options:** phase-specific target; exempt the metric; arm metrics on first meeting their target.
- **Resolved:** a metric arms the first time it is met; until then measured, not enforced (general
  rule, not a phase-0 exception). **Touched:** R-042, `AGENT.md`, `MetricsTest`.

### J-009 Who may edit the decision log
- **Who:** Opus asked; Ghost decided. **Class:** ambiguity → decision.
- **Found:** Opus wanted to add seven missing `See also` links but treated `theGhost.md` as Fable's.
- **Resolved:** it is Ghost's; any agent may append a decision Ghost made and keep cross-references
  true; `Decision`/`Why`/`Status` text is Ghost's. **Touched:** R-042, `theGhost.md` header.

### J-010 Two fresh-agent trials both "added a module" when the task meant "propose one"
- **Who:** Opus ran the trials; Ghost decided; Fable recorded. **Class:** ambiguity → decision.
- **Intent:** Phase 0.7, the tithe pass's navigation trial.
- **Found:** both agents opened `CLAUDE.md` first (right), reached `modules.json` 4th–5th after the
  Gradle files (detour), and neither stopped; one edited ROADMAP §3.3, flagged as Ghost's, and said it
  could not tell whether the asker was Ghost.
- **Options:** score only navigation; forbid edits in the trial; make a new module a decision agents
  propose.
- **Resolved:** a new module or import is an architecture decision an agent proposes and stops
  (R-043); the trial task is phrased "say what you would change, then stop"; the agent that stopped
  was judged to have made the best call of the day. `settings.gradle` now says modules are not Gradle
  subprojects (the detour's fix, unproven until a third trial). **Touched:** R-043, `modules.json`
  adding line, `CLAUDE.md` row, ROADMAP 0.7 and §5.

### J-011 NIST's documentation disagreed with its file in two places
- **Who:** Opus. **Class:** contradiction (documentation vs. reality).
- **Intent:** Phase 1.1b, the population reader.
- **Found:** a subprogram's header names the main program first and itself last (68 members); two
  headers carry sequence text past column 72; neither in the corpus README or the NIST guide.
- **Resolved:** cards read only within the COBOL text area; the reader holds the manifest's exact
  count (512 members: 459 programs, 51 copy members, 2 data files), by test. **Touched:** `oracle/nist`,
  the first rows of `KERNEL-MAP.md`, `DONE.md`.

### J-012 The clean room had the reference on the shelf
- **Who:** Opus raised; Ghost decided; Fable recorded and removed. **Class:** contradiction →
  decision.
- **Intent:** Phase 1.1, the NIST harness as a clean-room rewrite (R-044).
- **Found:** Opus deleted its local GnuCOBOL clone so nothing GPL would be read while writing the
  harness; the same GPL scripts (`expand.pl`, `report.pl`, `summary.pl`), per-module `EXEC85.conf`
  and GnuCOBOL's expected-result files sat in `oracle/prototypes/nist/`, one directory over.
- **Options:** leave them (nobody opens them); move them out of the tree; remove and keep only
  NIST's material and the study's run outputs.
- **Resolved:** removed; GPL code never in the repo or on screen while Katana's equivalent is written;
  `fetch.sh` no longer clones GnuCOBOL whole (autotests by sparse checkout in phase 3); JUnit
  (EPL-2.0, unmodified, tests only) distinguished from che4z grammars (modified, shipped) for
  counsel. **Touched:** R-045, `fetch.sh`, `oracle/prototypes/README.md`, `HANDOFF.md`.

### J-013 Observation began
- **Who:** Ghost. **Class:** direction.
- **Found:** Ghost told the Suite Builder session (≈11:30 CDT) that its methodology is being observed
  and formalized into standards, and that a journal of intent and problems would help; Katana's
  session was told the same through this file's creation. Behaviour before and after this point is
  under different conditions and should be compared with that in mind.
- **Touched:** this journal (R-046); Suite Builder G-162.

### J-014 What the NIST golden means
- **Who:** Opus raised, with both options and a lean; Ghost decided with Fable. **Class:** ambiguity →
  decision.
- **Intent:** Phase 1.1e, the module runs; all twelve modules running under cobc in 68 s.
- **Found:** four modules matched the study's golden exactly (IC, RL, SG, RW); the rest differed in
  counts; four conventions of GnuCOBOL's harness were visible only in the study's outputs and stated
  nowhere (IF's `ALL` subscript credited as passing; inspection as pass; zero tests as one pass; DB103M
  summed). Also: SM207A shows X-48's library must hold ALTL1's text under the name ALTLB (the two
  alternate libraries had been the wrong way round).
- **Options:** copy the conventions and reproduce the published total exactly; follow NIST's own
  semantics and reconcile every difference.
- **Resolved:** NIST's semantics (R-047); the 1.1 check becomes "every difference explained".
  Opus's branch (cc99173) could not merge under R-040 until this was decided; the environment fixes
  (DB switches, card-input programs, IX216A–218A, SQ) continue regardless.
- **Touched:** R-047, ROADMAP 1.1, `RECONCILIATION.md` (to be written).
- **Note:** the implementing agent stopped at a decision that was not its own, kept the work off main,
  and kept working on the part that was independent of the decision. That is R-043's behaviour
  generalized, and the pattern the journal exists to count.

### J-015 An X-card's text starts in column 9, which the guide's own sample hides
- **Who:** Opus. **Class:** contradiction (documentation vs. reality), failure-caught.
- **Intent:** Phase 1.1c, EXEC85's control cards for the cobc lane.
- **Found:** the User Guide's Appendix E, as scanned, shows `X-001 TAPE1.`; EXEC85 reads the number
  in columns 3–5, a program-name position in 6–7 and the text from column 9, and a card written as
  the sample shows is misread without an error.
- **Options:** trust the sample; read EXEC85's record layout and write to it.
- **Resolved:** cards rewritten with text in column 9; the card file's header says so; a unit test
  holds every X-card to it. **Touched:** `oracle/nist/exec85.cards`, `Exec85Test`.
- **Caught by:** EXEC85's own print file, which lists every undefined card: the undefined list did
  not shrink after the cards were "fixed", which made the misreading visible.
- **Cost:** two runs (minutes).

### J-016 A program's counts come from its footer, not its lines
- **Who:** Opus. **Class:** decision.
- **Intent:** Phase 1.1d, the report reader.
- **Found:** counting `PASS` lines in NC101A's report gives 96 for 93 tests: the column heading
  repeats the word. Every NIST routine ends with a fixed four-line footer from its own END-ROUTINE.
- **Options:** count per-test lines; read the footer.
- **Resolved:** the footer, written from NIST's report code; a report without a whole footer gives
  no counts rather than zeros. Real runs of NC101A, NC105A and SQ201M give the study's counts.
  **Touched:** `oracle/nist/Report.java`, three report excerpts as fixtures.

### J-017 A reader that drops a field invites a guess where the field was
- **Who:** Opus. **Class:** failure-caught.
- **Intent:** Phase 1.1e, telling a SUBRTN (CALLed) from a SUBPRG (run after its main).
- **Found:** `Population.Member` (1.1b) kept the owner but not the role word from the header; the
  first runner guessed the role from the name prefix ("IC", "OBIC").
- **Resolved:** the member keeps its role; the guess is gone. **Touched:** `Population.java`,
  `ModuleRun.java`. **Caught by:** the author's own reading of the code before running it.

### J-018 The alternate copy libraries were the wrong way round
- **Who:** Opus. **Class:** failure-caught.
- **Intent:** Phase 1.1e, placing ALTL1 and ALTLB in the libraries X-47 and X-48 name.
- **Found:** the guide (3.6) names both members and both cards without saying which goes where;
  SM207A failed to compile looking for `ALTLIB2/ALTLB`. SM207A's own code settles it: its second
  test copies ALTLB from X-48's library and passes only if that text *fails*, which is ALTL1's text.
- **Resolved:** ALTL1 placed in X-48's library under the name ALTLB, with SM207A's logic written
  beside the mapping. **Touched:** `ModuleRun.java`. **Caught by:** the first full run.

### J-019 What "executed" means in the published summary, read from its outputs
- **Who:** Opus. **Class:** decision.
- **Intent:** Phase 1.1e, a module's row: programs, executed, compile errors, crashes.
- **Found:** the study's outputs record programs that ended on a runtime error (NC401M, file status
  35) or printed no report (NC302M, a flagging test) as run with zero tests; the guide does not say.
- **Resolved:** a program that ran to an end, normally or not, is executed with the tests its report
  gave; only a run stopped at the time limit is a crash; how it ended is kept as a note. With it, the
  program and executed columns match the golden in ten of twelve modules. **Touched:** `ModuleRun`.

### J-020 The harness refused a removal it could not resolve
- **Who:** Opus. **Class:** failure-caught (by the environment).
- **Intent:** cleaning a scratch folder before a hand run (`rm -f *` after a `cd`), and a print file
  inside a container script.
- **Found:** Claude Code's safety check blocks a removal whose target it cannot resolve statically,
  and an `rm` inside a `sh -c` script.
- **Resolved:** a fresh folder per run instead of cleaning one, and a subfolder per program; no
  removal needed. **Caught by:** the harness. **Cost:** two retries.

### J-021 A push failed and the command chain carried on
- **Who:** Opus. **Class:** failure-caught.
- **Intent:** landing J-015 onward on `main` alone, then rebasing the unmergeable 1.1e work on top.
- **Found:** Fable pushed R-047 between Opus's fetch and its push, so the push to `main` was refused;
  the rest of the command was joined partly by `;`, so it went on: a rebase that conflicted in this
  file, then a forced push (with a lease) that set the branch to `main`'s commit. `main` was never
  touched; the 1.1e commit stayed in the local branch.
- **Options:** chain every step with `&&`; or push, check, and only then go on, one step per command.
- **Resolved:** the conflict resolved by hand (Fable's J-014 kept, Opus's J-019 on the same event
  dropped, the rest renumbered), the branch restored from the local copy; from here a push that can
  be refused is its own command, checked before anything follows it.
- **Caught by:** the output of the next command (`git status`, the reflog). **Cost:** minutes.

### J-022 X-91 is the ordinal of 'D', not of '0'
- **Who:** Opus. **Class:** contradiction (documentation vs. reality), failure-caught.
- **Intent:** Phase 1.1e, Katana's own environment fixes.
- **Found:** the scanned Appendix D reads X-91 as "ordinal number of character '0'"; NC174A defines
  `ORDINAL-A-THROUGH-D` as X-90 THROUGH X-91, so X-91 is 'D' (the scan read D as 0). With 49 ('0'),
  two CLASS tests failed.
- **Resolved:** X-91 = 69, with NC174A's definition cited beside the card. **Touched:**
  `oracle/nist/exec85.cards`. **Caught by:** the per-program comparison with the study's run.
- **Note:** the second time the scan hid the truth (after J-015); the program text decides.

### J-023 What NIST says about one program is data, beside its page
- **Who:** Opus. **Class:** decision.
- **Intent:** Phase 1.1e, the DB debugging switch and files that must be absent.
- **Found:** the guide gives per-program run conditions: DB programs with the object-time debugging
  switch on except DB102A (off) and DB103M (on, then off); SQ130A, SQ141A, SQ142A, SQ225A and
  IX216A–IX218A expect a file not to exist when they start, while other programs rely on files the
  one before left (3.6).
- **Options:** special cases in Java; one rule for all (fresh folder per program, which breaks the
  chains); a data file of conditions, each with its page.
- **Resolved:** `oracle/nist/run-notes`, read by `RunNotes`; DB's failed tests went from 274 to 0,
  and the SQ and IX failures to 0. Remaining: NC204M (ACCEPT into a 200-character field from two
  card lines), SM206A (a debug line in COPY text), OBNC1M (STOP literal with no operator), the IF
  `ALL` subscript, DB205A (communications): cobc's own behaviour, for the reconciliation (R-047).
- **Touched:** `oracle/nist/run-notes`, `RunNotes.java`, `ModuleRun.java`, `Cobc.java` (input).
