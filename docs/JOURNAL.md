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
`measurement`, `direction`, `trial` (a question settled by running something: name the trial, its
result, and the decision it produced), `deferred` (a question parked under R-053 with the default
taken; closed by a later `decision` entry naming it).

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

### J-024 The reconciliation is a computation, not a list
- **Who:** Opus. **Class:** decision.
- **Intent:** Phase 1.1e, R-047's "every difference explained".
- **Found:** listing each differing program with a reason can still leave a difference unaccounted
  for; the published summary gives per-program numbers only for main programs, so subprogram
  differences show only in module totals.
- **Options:** a reviewed list; reasons as rules applied to the golden, checked against the published
  rows.
- **Resolved:** eight reasons, four of them rules (`inspection-as-pass`, `deleted-as-pass`,
  `none-as-one`, `runs-summed`) and four that state the published outcome (`cobc-rejects`,
  `cobc-behaviour`, `source-altered`, `not-supported`); `ReconciliationTest` applies the 45 lines and
  must rebuild all twelve published rows exactly. Removing one line fails it, naming the module.
- **Touched:** `oracle/nist/Reconciliation.java`, `RECONCILIATION.md`, `programs.golden`,
  `published.summary`.

### J-025 The published run counted a test NIST's source does not run
- **Who:** Opus. **Class:** measurement.
- **Intent:** Phase 1.1e, reconciling NC217A.
- **Found:** NIST comments out STR-TEST-GF-4's STRING statement and deletes the test ("ANSC
  INTERPRETATION"); under the same cobc the published run counts 82 tests passed to the source's 81
  run and 1 deleted. The published harness ran altered source.
- **Resolved:** a reason of its own, `source-altered`, rather than folding it into cobc's behaviour.
  The tests NIST deletes and the published run counts as passes (NC174A, SM208A, SQ106A) may be the
  same alteration seen without a change of total. **Touched:** `RECONCILIATION.md`.

### J-026 Phase 1.1 closed: NIST in CI, every difference explained
- **Who:** Opus. **Class:** measurement.
- **Intent:** 1.1's check: the NIST run in CI under 3 minutes, every difference from the published
  summary explained (R-047).
- **Found:** CI run 43 (`cd56c8d`): the `nist` job's run step took 85 s (image build 44 s, fetch 2 s,
  not counted against the limit); the fast loop 17 s. Katana's total `426 413 12 1 9343 5 29 143 9520`;
  the published total rebuilt exactly from it by the 45 reconciliation lines.
- **Touched:** `DONE.md`, `TODO.md`, `HANDOFF.md`.
- **Cost:** six steps (a to f), about three and a quarter hours (1.1a at about 14:55 UTC, CI green at
  18:10), three Ghost decisions (R-044, R-045, R-047), and three places where NIST's documentation
  and its files disagreed (J-011, J-015, J-022).

### J-027 The IBM run's changes, each measured to one flag
- **Who:** Opus. **Class:** measurement.
- **Intent:** Phase 1.2, every change from the cobol85 run either a behaviour the profile must model
  or a test that depends on the dialect.
- **Found:** 21 runs change; 101 more failed tests and 7 more programs that do not compile. Adding one
  GnuCOBOL flag at a time to the cobol85 run reproduces every added failure: `-farithmetic-osvs` 61,
  `-fno-binary-truncate` 8, `-fsection-segments=ignore` 31 (NC106A needs two of them, 39 and 3); the
  seventh compile failure, NC211A, is `77 NOTHING`, a word reserved under ibm like the six ST
  programs' CHAR; ST117A loses its one test because ST115A no longer compiles. The flags were found
  from `cobc --help` and experiment only (R-045).
- **Options:** a reviewed list of changes; a record whose causes the test checks against the two
  goldens.
- **Resolved:** `IBM-CHANGES.md` with a fixed list of five causes, the measured failures beside each
  profile cause, held by `DialectChangesTest`. The roadmap names the baseline `nist-ibm.txt`; it is
  `programs-ibm.golden`, beside `programs.golden`, since the integration test holds both the same way.
  Flagged here rather than renamed silently.
- **Touched:** `oracle/nist/` (`Cobc`, `Exec85`, `ModuleRun`, `DialectChanges`, `exec85-ibm.cards`,
  `programs-ibm.golden`, `IBM-CHANGES.md`), `build.gradle`, CI, `HANDOFF.md`.

### J-028 The paths metric refused a checker over 20 paths
- **Who:** Opus. **Class:** failure-caught.
- **Intent:** Phase 1.2, the change record's checker.
- **Found:** `DialectChanges.check` was written as one function; the fast loop failed with
  "Functions over 20 paths is 1, worse than the 0.0 recorded".
- **Resolved:** split into `check`, `causes` and `measured`; no limit raised.
- **Caught by:** `MetricsTest`, on the first run. **Cost:** one fast loop.

### J-029 The profile is built in 1.3, as far as a fixture needs
- **Who:** Opus. **Class:** ambiguity.
- **Intent:** Phase 1.3, the fixture's "profile hash".
- **Found:** the roadmap's 1.3 builds the fixture model only; `docs/PROFILE.md` says `profile/` is
  filled "in phase 1.3, where a fixture first carries the hash". A hash needs a profile to hash.
- **Options:** a placeholder hash until a later step builds `profile/`; the whole profile now; the
  profile as far as can be stated from what was measured.
- **Resolved:** the last: compiler, version, dialect and GnuCOBOL flags, their cobc arguments and the
  hash (PROFILE-01 to 03). TRUNC, ARITH, NUMPROC, code page and collating sequence are not fields
  yet: their IBM meaning is in the manuals the study could not reach, and a field added later is a
  new profile with a new hash. No new module or import: `profile` was in `modules.json`, and
  `oracle` may import it.
- **Touched:** `profile/`, `docs/PROFILE.md`, `CLAUDE.md`.

### J-030 Phase 1.3 refuses the clock phase 1.6 needs
- **Who:** Opus; for Ghost to settle as R-009. **Class:** contradiction.
- **Intent:** Phase 1.3's check, "a fixture with an unpinned clock is refused".
- **Found:** 1.3 defines the environment as `COB_CURRENT_DATE` "with hundredths and zone"; 1.6's
  check runs the gate "with the clock pinned to seconds only" and expects it to fail at TRANFILE
  column 326. If the schema refuses a seconds-only clock, 1.6 cannot express its run as a fixture;
  if it accepts one, the schema lets F-02's leak through to the gate.
- **Options:** refuse seconds alone (the gate's test sets its clock outside a fixture); accept it and
  leave it to the gate.
- **Resolved:** refused (FIXTURE-03), since 1.3's own words ask for hundredths and zone and a
  refusal before the run is cheaper than a gate failure after two; written in `HANDOFF.md` as one of
  R-009's choices for Ghost.
- **Touched:** `docs/FIXTURE.md`, `oracle/fixture/FixtureCheck.java`, `HANDOFF.md`.

### J-031 The study's TRANREPT run did not use the JCL's dates
- **Who:** Opus. **Class:** contradiction.
- **Intent:** Phase 1.3, expressing TRANREPT.
- **Found:** TRANREPT's SORT keeps transactions processed from 2022-01-01 to 2022-07-06. POSTTRAN
  under the pinned clock dates all 257 of its transactions 2024-03-01, so that range keeps none. The
  study's frozen sort output holds all 257 (every TRAN-PROC-DT 2024-03-01) and its report says
  "Reporting from 2024-03-01 to 2024-03-01": its SORT emulation and DATEPARM used that date. The
  hashes 1.4 must reproduce (`a7d14a96…`, `19b7f04f…`) are of that run, not the JCL's. DATEPARM
  itself is not in CardDemo.
- **Options:** write the fixture as the JCL says; write it as the study ran it.
- **Resolved:** as the JCL says, with DATEPARM stating the JCL's range and a comment saying both;
  which run 1.4 reproduces is Ghost's (HANDOFF, R-009).
- **Touched:** `oracle/fixture/carddemo/TRANREPT.fixture`, `DATEPARM`, `HANDOFF.md`.

### J-032 Four things the study said about CardDemo, checked
- **Who:** Opus. **Class:** measurement.
- **Intent:** Phase 1.3, the CardDemo fixtures.
- **Found:** (1) `cardxref.txt` is fifty 36-byte lines, not one short line among 50-byte records;
  padded to 50 its records are the study's `cardxref.seq` exactly, as are the eight other data
  files under the fixture's framing. (2) CREASTMT's STMTFILE DD has stray text after its SPACE
  operand; after a comma and a blank JCL reads it as a comment, so it changes nothing. (3) INTCALC's
  XREFFIL1 is the alternate index's path, named by IBM's convention after XREFFILE; CBACT04C never
  assigns it. (4) CardDemo was fetched at whatever its head was; the fixtures hash its data, so it
  is now pinned (`59cc6c2f`).
- **Touched:** `corpus/fetch.sh`, `corpus/MANIFEST.md`, the four fixtures.

### J-033 The first test run caught two of mine
- **Who:** Opus. **Class:** failure-caught.
- **Intent:** Phase 1.3, the fixture tests.
- **Found:** `java.time` parsed `2024-02-30` as February 29 under its default resolver, so an
  impossible clock was accepted; and a KSDS defined by a step was refused for having keys, which
  are its structure, not its records. Also, JUnit's parameterized-test jar is not vendored.
- **Resolved:** the strict resolver; keys moved out of the content attributes; a loop for the four
  jobs. **Caught by:** `FixtureTest`, on its first run. **Cost:** one run.

### J-034 Ghost settled R-009 and moved the SORT steps to 1.5
- **Who:** Opus asked; Ghost decided. **Class:** decision.
- **Intent:** Phase 1.4's start: its check named TRANREPT's frozen hashes, which came from dates the
  JCL does not use (J-031) and include the SORT step's own output, though the DFSORT subset is 1.5.
- **Options:** for the dates, a second fixture with the study's dates, refreezing from the JCL, or
  changing the one fixture; for SORT, moving those checks to 1.5, or a stopgap sort in 1.4.
- **Resolved:** Ghost chose the options Opus leaned to: `TRANREPT-STUDY.fixture` beside the JCL's,
  and SORT steps in 1.5 (R-048); and confirmed that a seconds-only clock stays refused, with 1.6
  setting its clock outside a fixture. R-009 recorded. The profile's IBM fields go to Fable.
- **Touched:** `theGhost.md` (R-009, R-048), docs/ROADMAP.md 1.4 and 1.5, `TODO.md`, `HANDOFF.md`.

### J-035 CardDemo's REPROCT opens a comment where IDCAMS does not read
- **Who:** Opus. **Class:** contradiction.
- **Intent:** Phase 1.4, TRANREPT's backup step (the REPROC procedure, SYSIN from member REPROCT).
- **Found:** REPROCT's licence comment starts `/*` in column 1. IDCAMS reads columns 2 to 72 (two
  secondary sources; IBM's manual is blocked from here), so its first card reads `* Copyright …`,
  which is not a command. The study never read REPROCT: it copied the KSDS with a COBOL stand-in.
- **Options:** read column 1 (a guess against the documented rule); refuse the card with a finding.
- **Resolved:** a finding that names the card and the rule; TRANREPT as written stops there, and
  TRANREPT-STUDY carries the one command inline as a third commented departure (R-009). Confirming
  the rule from the manual is in `HANDOFF.md`.
- **Touched:** `oracle/lane/Idcams.java`, the two TRANREPT fixtures, `HANDOFF.md`.

### J-036 POSTTRAN through the lane matches every frozen file, not just the one checked
- **Who:** Opus. **Class:** measurement.
- **Intent:** Phase 1.4's check, POSTTRAN's TRANFILE unload `aed61744…`.
- **Found:** the lane's first run reproduced it, and also the study's reject file (`86f3b341…`, 43
  records) and CBTRN02C's stdout (`a33ef3a7…`, 48 lines), return code 4, from the shipped data
  loaded in key order with TCATBALF from `tcatbal.txt`. INTCALC through the PARM driver wrote 50
  interest transactions, return code 0. Left out because no fixture needs them yet: temporary
  datasets (the fixture schema has none) and the COBDATFT stub (none of the four jobs calls it).
- **Touched:** `oracle/lane/`, `oracle/lane/carddemo.golden`, CI (a lane step of its own).

### J-037 A SORT step allocated its outputs before the lane stopped at it
- **Who:** Opus. **Class:** failure-caught.
- **Intent:** Phase 1.4, stopping at SORT (R-048).
- **Found:** the step's DDs were bound before the lane looked at its program, so CREASTMT's catalog
  ended with an empty TRXFL.SEQ the job never wrote.
- **Resolved:** a SORT step stops before anything is allocated. **Caught by:** reading the first
  report before recording the golden. **Cost:** one run.

### J-038 The IBM manuals, reached in part
- **Who:** Fable, with a research agent; decided by Ghost. **Class:** measurement → decision.
- **Intent:** R-009's third choice: what the profile's IBM fields are and mean (J-029).
- **Found:** from this session's network, IBM's performance-tuning papers, compiler-option tables,
  Customization Guide and eleven APARs were reachable and quotable where the study's container got
  403 on everything. TRUNC, NUMPROC, ARITH, CODEPAGE and ZONEDATA/INVDATA semantics and defaults are
  now verified from primary text. The Programming Guide's Appendix A (intermediate results) is still
  not: its PDF is reachable but 700 pages defeat the fetch tool; its HTML topic id is unknown and
  guesses return 403.
- **Options:** wait for the full appendix before adding any field; add the fields with what is
  verified and mark the rest hypothesis.
- **Resolved:** R-049: fields added with IBM defaults; every semantic carries verified/hypothesis;
  a lowering on a hypothesis says so in its proof; Appendix A blocks 3.1, not 1.x.
- **Touched:** R-049, `docs/PROFILE.md`, `docs/research/ibm-options.md`, `HANDOFF.md`.
- **Note:** the study's F-10 risk ("the manuals were unreachable") was partly an artefact of one
  container's proxy, not of the manuals. Worth remembering when a research finding says "unreachable".

### J-039 Four open choices from 1.3–1.4, decided
- **Who:** Opus listed, with a lean on each; Ghost decided with Fable. **Class:** decision.
- **Found:** (1) IBM fields: already R-049, with TRUNC(STD) mapped to `binary-truncate=yes` over
  `-std=ibm`, the conflict Opus measured in 1.2. (2) IDCAMS columns 2–72: accepted; the lane's stop
  on REPROCT is a finding on CardDemo's JCL; manual to confirm. (3) Temp datasets and COBDATFT: built
  when a fixture needs them (P-11); roadmap 1.4 text amended. (4) R-016 decided: permissive column 7,
  tabs expanded to 8-column stops upstream of both lanes, as a finding under a stated hypothesis.
  (5) R-010 after 1.5's measurement, as planned.
- **Touched:** R-016, ROADMAP 1.4, HANDOFF.

### J-040 Fable pushed a red main a second time
- **Who:** Fable (fault and fix). **Class:** failure-caught.
- **Intent:** recording R-016 (J-039).
- **Found:** the entry was appended after R-049 and left in the reserved paragraph; `GhostTest`
  refused both. The commit script ran the tests but did not gate the commit on their exit code, so the
  failure was printed and the push went ahead anyway (cee02b3). Main red for about three minutes.
- **Caught by:** the test's own output, read after the push. **Resolved:** cb46066. The script now
  commits only when the fast loop exits 0.
- **Note:** J-007 was "did not run the tests"; this is "ran them and did not look". Both are the same
  failure: a check that does not stop the action is not a check. The same lesson R-040 and the Gradle
  bug (J-006) already carried.


### J-041 R-049's fields built; the IBM profile now truncates binary items
- **Who:** Opus, from Fable's R-049 and J-039. **Class:** decision.
- **Intent:** Implement R-049 in `profile/`: the six `ibm.*` fields, stated by every profile.
- **Found:** `ibm.trunc STD` truncates binary items to their PICTURE; `-std=ibm` alone does not (1.2,
  `binary-truncation`), so `ibm.profile` carries `binary-truncate=yes`, as J-039 says.
- **Resolved:** the fields read, checked against their value sets and hashed after the flags;
  NUMPROC(MIG) raises a finding; PROFILE-04 refuses a `binary-truncate` flag that disagrees with
  `ibm.trunc`, and an `ibm`-dialect profile that does not state it. The IBM profile's hash changed,
  so the five CardDemo fixtures name the new one; run under the flag, every CardDemo outcome is the
  same and only the golden's hash lines changed. `cobol85.profile` states IBM's defaults too
  (PROFILE-02), though NIST was never compiled on z/OS; its comment says so.
- **Touched:** `profile/`, `docs/PROFILE.md` (the hash's field order, PROFILE-02, PROFILE-04), the
  five fixtures, `oracle/lane/carddemo.golden`.

### J-042 Opus committed on a main that had moved, and saw it before pushing
- **Who:** Opus (fault and fix). **Class:** failure-caught.
- **Intent:** Commit J-041's work per R-040: fetch `main`, rebase if it moved, test, push.
- **Found:** the fetch printed 3 (Fable's R-016, its fix and J-040), and the same command line went
  on to commit; the fresh-clone check then tested a commit without them. The commit duplicated R-016
  and the journal numbers Fable had used.
- **Resolved:** the commit was kept aside, `main` taken as Fable left it, and only this work
  re-applied (Fable's R-016, J-039, J-040, roadmap and HANDOFF text kept). **Caught by:** reading the
  count after the commit, before the push. **Cost:** one rebuilt commit. The same lesson as J-021 and
  J-040: a check whose answer does not stop the next step is not a check; the fetch now runs alone.

### J-043 Two CardDemo OUTRECs build records their SORTOUT's length does not hold
- **Who:** Opus; for Ghost. **Class:** ambiguity.
- **Intent:** Phase 1.5, CREASTMT's and PRTCATBL's SORT steps.
- **Found:** CREASTMT's `OUTREC FIELDS=(1:263,16,17:1,262,279:279,50)` builds 328 bytes into a SORTOUT
  of LRECL 350; PRTCATBL's builds 41 (11+1+2+1+4+1, a 12-byte EDIT mask, 9X) into LRECL 40. Whether
  DFSORT pads, truncates or stops, and with what byte and return code, is not in any text reached
  from here; a web search found the PAD/TRUNC options but not their defaults for reformatted records.
- **Options:** pad with blanks and truncate (a guess); stop with a finding.
- **Resolved:** a finding naming both lengths, and the question to Ghost. A step stopped by a finding
  now also removes the datasets it made, since what it would have written is unknown.
- **Touched:** `oracle/lane/Sorts.java`, `oracle/lane/Steps.java`, `HANDOFF.md`.

### J-044 R-016's 8-column tabs do not compile CUSTREC; 4-column ones do
- **Who:** Opus; for Ghost. **Class:** measurement.
- **Intent:** Phase 1.5, CREASTMT end to end (R-048), which needs CBSTM03A and so CUSTREC.cpy.
- **Found:** CUSTREC's 17 tabbed lines begin with two tabs. Expanded to 8-column stops each line is 75
  columns, its PICTURE past column 72, and cobc refuses it exactly as the study found; expanded to
  4-column stops CBSTM03A compiles. CSLKPCDY.cpy also holds tabs; no program the fixtures run copies
  it. The lane implements R-016 as decided and reports a tab only in a copybook a compiled program
  copies.
- **Resolved:** not changed by Opus: R-016 is Ghost's, and the 8-column stop is its stated hypothesis.
  CREASTMT stops at its SORT (J-043) before CBSTM03A is reached, so the measurement is from a direct
  compile. The question is in `HANDOFF.md`.
- **Touched:** `oracle/lane/Tabs.java`, `HANDOFF.md`.

### J-045 R-010 measured: both sorts reproduce TRANREPT-STUDY
- **Who:** Opus. **Class:** measurement.
- **Intent:** Phase 1.5, R-010's measurement on TRANREPT.
- **Found:** the Java sort and a generated COBOL SORT (`WITH DUPLICATES IN ORDER`, an INPUT PROCEDURE
  for INCLUDE) both reproduce `a7d14a96…` and `19b7f04f…`. One run each, warm cache: 228 ms and 334 ms
  for the whole job. The Java sort reads all five CardDemo SORT steps' cards; the generated one two
  (no OUTREC, no concatenated SORTIN). CBTRN03C's stdout matches the study's too (`e8f3ca3f…`).
- **Resolved:** the lane runs the Java sort; R-010 is Ghost's, with Opus's lean in `HANDOFF.md`.
  Equal keys keep input order in both; the lane notes it where it happens, since DFSORT promises that
  only with EQUALS (R-026).
- **Touched:** `oracle/lane/SortCards.java`, `SortConditions.java`, `SortOutrec.java`, `Zoned.java`,
  `Sorts.java`, `SortCobol.java`; four new fixtures (PRTCATBL, PRTCATBL-INLINE, COMBTRAN, SORTTEST);
  the schema's `concat`, existing generations and `job:NAME:DSN`.

### J-046 Three faults of Opus's own, caught on the way
- **Who:** Opus (fault and fix). **Class:** failure-caught.
- **Intent:** Phase 1.5.
- **Found:** (1) the generated sort's file had a second, shorter record, which made GnuCOBOL read it as
  variable-length (status 04, nothing released); (2) the compile cache's key, since 1.4, held the
  source and the arguments but not the copybooks, so a changed copybook could reuse a stale build;
  (3) every job reported the library's two tabbed copybooks, though none of its programs copied them.
- **Resolved:** records padded to full length; the copybooks' hashes in the key; a tab reported only
  for a copybook a program copies. **Caught by:** the measurement's empty output, a read of the cache
  code while adding shadow copybooks, and reading the first report. **Cost:** three runs.

### J-047 Every change ran CI twice, until the minutes ran out
- **Who:** Opus found it; Ghost decided. **Class:** decision.
- **Intent:** Checking CI after 1.5a (`6701f6f`).
- **Found:** GitHub Actions minutes are spent until 2026-11-02; both runs of `6701f6f` sit unfinished.
  Each step since 0.x was pushed to `main` and then to the working branch, and the workflow ran on
  every push, so every commit ran the whole workflow twice (the NIST job alone about four minutes a run).
- **Resolved:** R-050, Ghost's directive: one run per change, on a push to `main` or by hand. Until
  the minutes return, the fresh-clone run in the CI image is the gate, quoted in each commit message.
- **Note:** the commit's own gate line was written before the build ran, and Docker was down; it
  was run before the push and passed, but a result is written after the run, never before.
- **Touched:** `.github/workflows/ci.yml`, `theGhost.md` (R-050), `HANDOFF.md`.

### J-048 The three 1.5 questions, decided; DFSORT's padding read from IBM
- **Who:** Opus raised (J-043, J-044, J-045); Fable read IBM's text; Ghost decided. **Class:**
  decision.
- **Found:** IBM's DFSORT guide was reachable from Fable's network: fixed-length records are padded
  with binary zeros on the right and truncated on the right, ICE171I and return code per PAD/TRUNC
  (RC0/RC4/RC16). CUSTREC.cpy compiles at 4-column tabs and not at 8. The Java sort ran all five
  CardDemo SORT cards; the generated-COBOL sort two.
- **Resolved:** R-051 (pad X'00' / truncate, RC per fixture setting, default RC0 as a stated
  hypothesis); R-016 amended (tab width declared per source in the fixture, default 8; CardDemo
  declares 4 with its evidence); R-010 (Java sort) and R-026 (stable, with a tie finding).
- **Touched:** R-010, R-016, R-026, R-051; `docs/research/ibm-options.md` §8; HANDOFF.

### J-049 How questions are answered, and when the chain stops
- **Who:** Ghost; recorded by Fable. **Class:** decision.
- **Found:** Ghost proposed adversarial Fable pairs debating every Opus question; Fable argued that
  debate does not manufacture facts and that the budget is weekly. Ghost reframed: Fable as a source
  of answers and trial designs when uncertain; trials implemented by Opus and their results the
  answer; and the implementing agent running a phase to its end, deferring human decisions to the
  boundary where a reversible default exists.
- **Resolved:** R-052 (claims with status; trials before arguments) and R-053 (phase-scope autonomy
  with a four-class rule and the irreversible as the only stop). Journal gains classes `trial` and
  `deferred`; HANDOFF gains "For Fable" and "For Ghost" lists with the step each came from.
- **Touched:** R-052, R-053, ROADMAP §4.6, this file's schema.
- **Note:** the framework is itself an experiment: R-052's and R-053's Claims name what the
  boundary tithe measures (trials per phase against Fable minutes; defaults held against reversed).

