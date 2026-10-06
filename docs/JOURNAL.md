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


### J-050 CBSTM03A reads the PSA at address 0
- **Who:** Opus. **Class:** contradiction.
- **Intent:** Phase 1.5, CREASTMT end to end, after R-051 and R-016's amendment let its SORT and
  CBSTM03A's compile through.
- **Found:** CBSTM03A ended on SIGSEGV with nothing written. Its source (lines 235 to 290, and its
  own header: the program exercises constructs "to help create exercise modernization tooling") sets
  a POINTER to the PSA at absolute address 0, follows it to the TCB and the TIOT, and DISPLAYs the job,
  the step and the DD names. On Linux address 0 is unmapped; on z/OS it is the PSA.
- **Options:** a z/OS storage facade in the java lane; one in the cobc lane at address 0; a source
  overlay (changes the program under test). Listed for Fable with a lean (HANDOFF).
- **Resolved:** class (c) under R-053: a signal libcob reports is now an abend (`abend SIGSEGV`, with
  the abnormal dispositions), the golden holds CREASTMT's run to it, and 1.5 closes with that part of
  its check deferred. The phase continues.
- **Touched:** `oracle/lane/Programs.java`, `oracle/lane/carddemo.golden`, `HANDOFF.md`, `DONE.md`.

### J-051 1.5 closed on Fable's three answers
- **Who:** Opus, from R-010, R-016 (amended), R-026 and R-051. **Class:** decision.
- **Intent:** Build what Fable's answers decided.
- **Resolved:** the sort moved to `oracle/sort` with one entry point (`Dfsort.run`); the generated COBOL
  sort and its measurement test removed, their functions closed in the kernel map; EQUALS read
  (`SORT ... ,EQUALS`, `OPTION EQUALS`), ties without it a note listing SORTOUT's record ranges
  (TRANREPT-STUDY: 255 records in 48 runs); short records padded with X'00', long ones truncated, with
  RC4 giving return code 4 and RC16 stopping the step; the fixture's `tabs`, `dfsort.pad` and
  `dfsort.trunc` lines (FIXTURE-09), every CardDemo fixture declaring `tabs 4 CUSTREC.cpy`. PRTCATBL-
  INLINE now runs end to end; CREASTMT runs to CBSTM03A (J-050).
- **Touched:** `oracle/sort/`, `oracle/lane/`, `oracle/fixture/`, `docs/FIXTURE.md`, the nine fixtures,
  `oracle/lane/carddemo.golden`, `docs/KERNEL-MAP.md`.

### J-052 The gate built; the hundredths are two columns, not one
- **Who:** Opus. **Class:** measurement.
- **Intent:** Phase 1.6's check: with the clock pinned to the second the gate fails naming TRANFILE
  column 326; with hundredths and zone it passes.
- **Found:** all nine CardDemo fixtures run identically twice under their own clocks. POSTTRAN with
  `2024-03-01 12:00:00` set outside the fixture (R-009) fails with one difference:
  `TRANSACT.VSAM.KSDS (DD TRANFILE in STEP15): 257 of 257 records differ, in columns 325-326`. F-02's
  "column 326" was one run's view: there the tens digit of the hundredths happened to agree.
- **Resolved:** the gate names the columns that differ rather than a fixed one; the check asserts that
  326 is among them. R-012's text is drafted for Fable (HANDOFF), with the gate as built as the default.
- **Touched:** `oracle/lane/Gate.java`, `CobcLane` (a clock set outside the fixture, reported),
  `Report` (record lengths), `HANDOFF.md`.

### J-053 iconv corrupts money silently
- **Who:** Opus. **Class:** measurement.
- **Intent:** Phase 1.7's check: a one-byte change in a COMP-3 field reported with its field and both
  values; a byte-blind `iconv` of the OMP data detected as corruption.
- **Found:** the one-byte change: `ACCT-FIELDS.ACCT-BALANCE: 1 of 45 records differ; first at record
  5, byte offset 693: X'003131313C' (31313.13) then X'003121313C' (31213.13)`. `iconv -f IBM037 -t
  ISO-8859-1` leaves every text field right and changes ACCT-BALANCE in 44 of 45 records and
  ACCT-LIMIT in 9; the changed bytes are still valid packed decimals (188.74 becomes 186.73,
  5000000.00 becomes 2600000.00), so a reader that decoded them would see plausible wrong money, not
  an error. The study's "byte-blind iconv would corrupt the packed fields" was right, and quieter than
  it said.
- **Resolved:** packed and binary fields are compared as bytes, never as decoded values; R-011's text
  and the copybook reader are listed for Fable (HANDOFF), each with its default. The OMP course is now
  fetched at a pinned commit.
- **Touched:** `oracle/compare/`, `corpus/fetch.sh`, `corpus/MANIFEST.md`, CI, `HANDOFF.md`.

### J-054 The java lane's stub, and a program that copied itself
- **Who:** Opus. **Class:** failure-caught.
- **Intent:** Phase 1.8's check: a hand-written Java copy and a COBOL copy through both lanes, empty diff.
- **Found:** the cobc lane refused Katana's own `COPYREC.cbl` with "recursive inclusion": the compile
  cache wrote each source as `NAME.cbl` in the folder cobc ran in, and cobc's COPY search found it, so
  a program named like a copybook it copies copied itself. No CardDemo program is named like its
  copybooks, which is why 1.4 to 1.7 never met it.
- **Resolved:** each source is compiled from a subfolder of its own. The check then passed from a
  cold cache: both lanes' outputs compare with no difference and the same stdout. The DD contract is
  `docs/LANES.md`, listed for Fable with the default taken (HANDOFF).
- **Caught by:** the first run of the check. **Cost:** one run.
- **Touched:** `oracle/lane/Compiler.java`, `JavaPrograms.java`, `ProgramRunner.java`, `CobcLane.java`,
  `docs/LANES.md`, `oracle/fixture/trivial/`.

### J-055 The seconds-only gate check was a coin with a hundred sides
- **Who:** Opus (fault and fix). **Class:** failure-caught.
- **Intent:** The fresh-clone gate before pushing 1.8.
- **Found:** GateIntegrationTest's seconds-only check failed: this pair of runs differed only in column
  325. The live hundredths land in TRANFILE columns 325 and 326, the same value on all 257 records of a
  run, so a pair differs in 325, in 326 or in both as the digits fall (F-02 saw 326, J-052 both), and
  about one pair in a hundred agrees and passes the gate. The test, and the roadmap's "column 326",
  assumed one outcome of a random draw.
- **Resolved:** the test asserts what is invariant: one difference, on TRANFILE, within columns 325 and
  326, over up to three pairs of runs (a chance of about one in a million that all three agree).
- **Caught by:** the gate run before a push, on a fresh clone. **Cost:** one gate run.

### J-056 The first proof reports
- **Who:** Opus. **Class:** measurement.
- **Intent:** Phase 1.9's check, written before the step (TODO.md): v0 reports for the trivial fixture
  and POSTTRAN, with the gate, the datasets compared, coverage from a cobc trace and PROOF.md quoted.
- **Found:** GnuCOBOL's `-ftrace` with COB_SET_TRACE writes one line per paragraph or section entered to
  COB_TRACE_FILE and leaves stdout alone; a traced run of each fixture is identical to the untraced one.
  POSTTRAN against the study's frozen TRANFILE and rejects: the same; CBTRN02C entered 24 of its 26
  paragraphs, not its abend and I/O-status paragraphs, which the fixture's data never reaches.
  COPYREC has no named paragraphs, and the report says so rather than "0 of 0 entered".
- **Resolved:** PROOF.md drafted from R-013's sentence as the reversible default and listed for Ghost
  (class (d): a buyer reads it); the paragraph reader listed for Fable. Both reports held as goldens.
- **Touched:** `oracle/proof/`, `oracle/lane/Paragraphs.java`, the lane's traced run, `docs/PROOF.md`,
  `HANDOFF.md`.

### J-057 Tithe pass 1: the trial found its files, the fast loop sits on its line
- **Who:** Opus; the trial by a fresh general-purpose agent. **Class:** measurement.
- **Intent:** Phase 1.10's check.
- **Found:** metrics: context 872 words, largest file 241 lines, routing 4 files, no function over
  20 paths, no duplicate helpers, every decision linked, nothing stale or skipped; the fast loop 20 s
  measured cold (18 s alone, 7 s warm, compile 2.6 s of it); docs per source 0.45. The trial, on a
  clean copy and told only to propose: `CLAUDE.md` second, then `oracle/sort/SortCards.java`,
  `Dfsort.java` and `oracle/lane/Sorts.java`; nothing changed (`git status` clean). Its gap: the DFSORT
  row's contract named R-010, R-026 and R-051 only; the rule that SUM waits for a fixture (P-11) was
  in a javadoc. No IBM text on which duplicate SUM keeps has been read; none is needed until a
  fixture uses SUM.
- **Resolved:** the DFSORT row names P-11's rule; the two metric questions listed for Ghost with their
  defaults; SUM's IBM text waits until a fixture needs SUM.
- **Touched:** `docs/AGENT.md`, `CLAUDE.md`, `DONE.md`, `HANDOFF.md`.

### J-058 The phase-1 boundary: nine deferred questions answered
- **Who:** Opus listed (J-050 to J-057); Fable answered six; Ghost three. **Class:** decision.
- **Found:** of nine defaults taken under R-053, **six held** (CBSTM03A outside the cobc lane; the
  comparator as built with the sign-variant rule added; the Layout and Paragraphs readers until 2.5;
  LANES.md as written) and **three were reversed** (the gate now holds SYSOUT DDs and normalized
  stderr; the fast loop is measured warm; the docs ratio arms at 0.45). PROOF.md gained two lines.
  Two questions became trials (RECFM=V format; stderr determinism) rather than decisions.
- **Resolved:** R-011, R-012, R-013, R-027, R-054; R-042 amended by R-054.
- **Touched:** `docs/PROOF.md`, `docs/HANDOFF.md` (Trials section), `docs/AGENT.md` (Opus), the two
  report goldens (Opus re-records).
- **Note:** the first R-053 count: 6 held, 3 reversed, 0 stopped. Ghost, on the fast loop: "isn't
  warm more realistic?" — yes; what is paid is what is measured.


### J-059 R-012 reversed: the gate holds SYSOUT DDs and stderr; the stderr trial passes
- **Who:** Opus. **Class:** reversal (R-053), trial (R-052).
- **Intent:** Switch the first reversed default of the phase-1 boundary, and run its trial.
- **Found:** the gate compared only stdout. It now compares every sysout file in either run, stderr
  with the run folder's path as `<work>`. The trial ran all nine CardDemo fixtures: no difference; the
  one stderr written is CREASTMT's SIGSEGV (R-027), with no path, so `GateTest` holds the token.
- **Resolved:** R-012 C-2 verified for CardDemo; the trial leaves HANDOFF.
- **Touched:** `oracle/lane/Gate.java`, `GateTest`, `theGhost.md`, `docs/HANDOFF.md`, KERNEL-MAP.

### J-060 R-011's sign-variant rule built; R-013's goldens re-recorded
- **Who:** Opus. **Class:** decision built (R-053 boundary).
- **Intent:** Build what the boundary decided for the comparator and the proof.
- **Found:** C against F in one encoding was a difference under every profile. Every call now states
  the consumer's NUMPROC (no default): under NOPFD an EBCDIC zoned value equal but for its sign nibble
  is a per-field `sign-variant` note in the report; under PFD a difference. The goldens gain PROOF.md's
  two lines and nothing else (no fixture dataset is EBCDIC).
- **Resolved:** R-011 and R-013 held by code and goldens.
- **Touched:** `Compare`, `ProofReport`, `CompareTest` and the callers, `CLAUDE.md`, the two goldens.

### J-061 R-054 built: the fast loop warm, the docs ratio armed and trimmed to fit
- **Who:** Opus. **Class:** reversal (R-053). **Intent:** build R-054.
- **Found:** `gradle metrics --args=--time` now runs the loop twice: cold 24 s, warm 9 s (held). The
  docs ratio was 0.46 at arming, the boundary's own lines on top of 0.45; answered HANDOFF text, a
  duplicate HANDOFF item and my own DONE and journal lines were cut to bring it to the 0.45 armed.
- **Touched:** `scripts/Metrics.java`, `MetricsTest`, `docs/AGENT.md`, HANDOFF, DONE, JOURNAL.

### J-062 The RECFM=V trial: no GnuCOBOL format is IBM's RDW
- **Who:** Opus. **Class:** trial (R-052). **Intent:** settle LANE-02's open point.
- **Found:** two records (5 and 10 bytes) written by `cobc -std=ibm` under each `COB_VARSEQ_FORMAT`:
  0 `00 05 00 00`, 1 `00 00 00 05`, 2 `05 00 00 00`, 3 `00 05`. Format 0 is IBM's shape, but its length
  leaves out the 4-byte prefix (IBM writes `00 09 00 00`). The lane ignored `recfm`, so a V DD would
  have run as fixed; it now stops both lanes with a finding until a fixture needs V (LANE-02).
- **Touched:** `CobcLane`, `TrivialLanesIntegrationTest`, `docs/LANES.md`, HANDOFF.

### J-063 Phase 2.1: the forked grammars parse the corpus; one roadmap count was wrong
- **Who:** Opus. **Class:** build. **Intent:** 2.1's check.
- **Found:** first run: CardDemo 31/31; BankDemo 79/82 and OMP 26/30, the study's numbers; NIST hung
  on NC210A and NC246A (22 nested IFs): SLL took `identifier`'s first alternative, a bare data name,
  and left `X (I)` unparsed, and the full-LL retry ran 20 s and more a program. With `tableCall` the
  only data-name alternative, SLL parses both cleanly (0.5 s, 4.6 s) and no corpus needs LL. OMP's
  CBL0007 is a lab whose syntax error is the lesson, and cobc refuses it, so "30/30" cannot hold. The
  first corpus run ran out of heap keeping 560 trees; the test keeps errors only (61 s in all).
  Gradle's `--rerun-tasks` now regenerates and compiles the parser, so the metric's warm loop went
  from 9 s to 16 s and cold to 30 s; an edit outside `front/grammar/` does not pay it.
- **Resolved:** the counts in `front/corpus.golden`; three questions under HANDOFF's "For Fable" and
  the vendored ANTLR tool under "For Ghost", each with its default.
- **Touched:** `front/`, `third_party/`, `build.gradle`, `oracle/lane/Tabs.java`, CI's lane step.

### J-064 Phase 2.2 measured, then stopped on R-015
- **Who:** Opus. **Class:** measurement; stop (R-053). **Intent:** the "30-minute check never done".
- **Found:** che4z (`66aa5a4`, shallow clone in scratch, never in the tree) generated by the vendored
  ANTLR tool, its `MessageServiceParser` stubbed so only syntax counts, driven over every EXEC block
  front/'s preprocessor tags: every CICS and SQL block in CardDemo and BankDemo parses; OMP's
  failures are the course's table-name placeholder. My first counts were wrong twice (a block ended
  only at a line ending `}`, so `END-EXEC.` ran on; then a stale compile); the counts above are
  from the corrected driver.
- **Resolved:** HANDOFF "For Ghost": 2.2 stopped; the numbers for R-014. **Touched:** TODO, HANDOFF.

### J-065 Phase 2.3: every character of the parsed text knows its origin
- **Who:** Opus. **Class:** build. **Intent:** 2.3's source map.
- **Found:** origins per character, not per line, because continuations join lines and REPLACE
  changes lengths. The corpus run caught one gap the unit tests had not: a blank line inside an EXEC
  SQL block (a BankDemo program) gave its tag line's break no file, since the break took its origin
  from the empty line before it; each break now takes its source line's end.
- **Resolved:** the round trip in the corpus test; the lowered half is 3.3's (HANDOFF "For Fable").
- **Touched:** `front/Mapped.java`, `Lines`, `Preprocessor`, `Copybooks`, `Parse`, the front tests.

### J-066 Phase 2.4: the Semantic IR's types, and its check written first
- **Who:** Opus. **Class:** build. **Intent:** 2.4, whose roadmap check was "none".
- **Found:** the check was written into TODO before the code, then sharpened once in the same commit
  (`Invalid`, the refusal, is the one class; files needed a `Value` of their own for OPEN and READ).
  The roadmap's list has no VALUE or 88s, but the lowering needs both, so `Item` carries them.
  Statements are one record, verb plus role-tagged operands and branches, not a type per verb: 2.5
  will say whether that holds. Twice a record's compact constructor read a field before it was set.
- **Touched:** `sir/types/`, `docs/SEMANTIC-IR.md`, `CLAUDE.md`, the sir tests, TODO, DONE.

### J-067 Phase 2.5a: the data map is cobc's layout on 13,108 fields
- **Who:** Opus. **Class:** build. **Intent:** 2.5's data map, held to `cobc -C`.
- **Found:** first comparison 208/215 on CardDemo and 12,397/12,531 on NIST; then 215/215 and 12,881/
  12,893 after: edited pictures with a sign symbol are signed; a floating string's symbols after the
  point are digits, a lone sign is not; an all-P picture is all scale; CURRENCY SIGN and
  DECIMAL-POINT IS COMMA; BLANK WHEN ZERO makes a DISPLAY number edited; a 66 of one item takes its
  attributes; nested programs each have their own layout file. The 12 left: 11 Report Writer groups
  and COMMUNICATION descriptions, features Enterprise COBOL lacks, now findings; 1 a cobc scale
  (HANDOFF). Several "differences" were my matcher's, pairing same-named fields of other records.
  The fresh-clone gate failed first: `.gitignore`'s `build/` also hid `sir/build/` (the roadmap's
  name), which compiled here and was never committed; the line is `/build/` now.
- **Touched:** `sir/build/`, `oracle/nist/Cobc.java` (`translate`), the sir tests, `NistSources`
  (shared by the front and sir corpus tests), CI's lane step.

### J-068 Phase 2.5b: every program of the corpus builds to the IR and round-trips
- **Who:** Opus. **Class:** build. **Intent:** the rest of 2.5.
- **Found:** first build: CardDemo 31/31, NIST 434/437; refusals were real gaps (a nested program
  opening its parent's GLOBAL file; RENAMES names qualified and split wrongly), and the "names no
  item" findings were mostly mine: 88s after an INDEXED BY table went to the index item; a qualified
  name split at the `IN` inside `VSTRING`; switch status names and DFHRESP's argument read as data.
  First round trip: 11/31 and 406/437, because `Value.Name` has a `kind` and the codec's
  discriminator was `kind` too; it is `is` now, and a unit test holds every Value kind.
- **Resolved:** `sir/ir.golden`; the CICS-names default in HANDOFF. **Touched:** `sir/`, the sir
  tests, `front/ibm/DFHAID.cpy`, CLAUDE.md, TODO, DONE.

### J-069 Phase 2's deferred questions answered mid-phase; 2.2 was deferred, not stopped
- **Who:** Opus listed (J-063 to J-068); Fable answered while present; Ghost's two items noted.
  **Class:** decision.
- **Found:** all six "For Fable" defaults were right; three were the roadmap's wording being wrong
  (30/30 OMP; "mirror ibm-strict.conf" against R-045; 2.3's check naming a step not yet built). The
  2.2 stop had a reversible default the agent did not take: keep che4z out, keep the measurement,
  keep EXEC blocks opaque, which is all R-005 needs until phase 7.
- **Resolved:** R-055; two new trials (NC124A's scale; the DFHAID values).
- **Touched:** R-055, ROADMAP 2.1–2.3, HANDOFF.
- **Note:** for the R-053 count this phase: answered early, not at the boundary, because Fable was
  present; six held, zero reversed, one stop downgraded to a deferral. The 8–10-minute pre-push
  check Opus runs in CI's absence caught a `.gitignore` line hiding `sir/build/` — a `main` that
  would not compile — which is the check paying for itself (J-062).

### J-070 Ghost's two phase-2 answers, and the tithe called
- **Who:** Ghost; recorded by Fable. **Class:** decision.
- **Found:** Ghost: no lawyer is available or expected, so the che4z question is not answered by
  guessing; the ANTLR tool jar is fine. Fable: 14 commits since the phase-1 tithe and none a tithe
  (R-008 says one in eight); mandatory context 631 → 982 words with two stale `CLAUDE.md` rows;
  13 functions past the comment threshold; the 8–10-minute pre-push check is the largest cost in the
  environment and not a metric.
- **Resolved:** R-015 (che4z stays out for good; phase 7 writes its own command grammar from IBM's
  published syntax, clean room); the jar accepted under R-001; tithe pass 2a listed in HANDOFF with
  six items, the pre-push check first.
- **Touched:** R-015, ROADMAP 2.2 and 7.2, HANDOFF.
- **Note:** the tithe was missed for 14 commits because nothing enforces R-008's cadence; the
  metrics test should count commits since the last tithe and warn past eight (an item for 2a).


### J-071 Tithe 2a.1: the pre-push check from 6.2 minutes to 3.4, and to 1.8 for one module
- **Who:** Opus. **Class:** measurement; build. **Intent:** HANDOFF's tithe 2a, item 1.
- **Found:** measured stage by stage, not 8-10 minutes but 371 s: clone 0, corpus fetch 4, build 47,
  NIST cobol85 68, ibm 64, the rest 188. The levers, each alone: (a) the corpus from this checkout by
  hard link, then `fetch.sh` verifies: worth the 4 s there were; (b) the image is already cached
  here: worth nothing; one container and one Gradle start instead of four: 308 s; integration classes
  in up to four JVMs: 201 s, which needed the lane's compile cache made atomic (a compile renamed into
  place) so two JVMs never share half a program; splitting NIST's dialects into two classes: 237 s,
  slower on four cores, reverted. (c) run what the change touches (`scripts/prepush.sh`, with `--all`):
  a docs change 18 s, a `sir/` change 105 s. The metric is armed at 3.4 against a target of 3.
- **Resolved:** `scripts/prepush.sh`; AGENT.md's metric; HANDOFF's R-050 line. **Touched:** those,
  `build.gradle`, `oracle/lane/Compiler.java`, Metrics.

### J-072 Tithe 2a.4-5: fresh-agent trial 2 passes; every trial has a result or "not yet run"
- **Who:** Opus; the trial by a fresh general-purpose agent. **Class:** trial (R-052); measurement.
- **Found:** trial 2 ("propose where a RECFM=V dataset's RDW handling would go ... then stop"), on a
  clean clone: the directory, `CLAUDE.md`, a grep for `recfm|RDW`, then `docs/LANES.md` third; LANE-02
  and J-062 named; nothing changed. Pass on all four counts. Its one detour: it found LANES.md by grep,
  since the routing row said "the DD contract" and not record formats; the row says RECFM now. Its
  proposal converts only at load and unload (`Datasets`), so hashes stay over IBM's bytes.
- **The trials:** stderr determinism: run, passes (J-059). RECFM=V: run, no GnuCOBOL format is IBM's
  RDW (J-062). NC124A's scale: run, cobc prints the cents, so it behaves with Katana's scale 2
  (`oracle/nist/IBM-CHANGES.md`). The recalled DFHAID values: not yet run; IBM's text is unreachable
  here and the corpus has no copy that defines them, only programs that use them.
- **Note (Ghost, on 2.5's report):** two stops this phase were mine with no blocker: 2.2 stopped
  though a reversible default existed (che4z out, EXEC kept as tagged text), and after answering
  Ghost's question mid-turn I ended the turn instead of going on. The rule: a question answered is not
  a stopping point; a step stops only when nothing reversible can be done.
- **Touched:** `CLAUDE.md` (one row), `oracle/nist/IBM-CHANGES.md`, `docs/HANDOFF.md`.

### J-073 Tithe pass 2a recorded: every metric within its target
- **Who:** Opus. **Class:** measurement. **Intent:** HANDOFF's tithe 2a, item 6.
- **Found:** context 795 words (872 at pass 1), fast loop 11 s warm and 20 s cold, largest file 344
  lines, routing 4, nothing over 20 paths, no duplicate, every decision linked, docs per source 0.33
  (0.45 armed: now within its 0.35), nothing stale or skipped; the pre-push check 3.4 minutes for
  every suite, the one metric past its target. `gradle metrics` now counts commits since the last
  tithe and says when one is due, so R-008's cadence is no longer held by habit (J-070).
- **Touched:** `docs/AGENT.md` (pass 1's numbers kept beside), `scripts/Metrics.java`, HANDOFF.

### J-074 Phase 2.6: the contract, a table of thirteen fact kinds
- **Who:** Opus. **Class:** build. **Intent:** 2.6, its check written first.
- **Found:** written as rows from the first line (T-13): adding a kind is one `new Kind(...)` line and
  one row of the document's table, which `ContractTest` holds equal. Statements are numbered by their
  place (`0.1.perform-inline-statement.0`), so a lowered statement can name the fact it honors; values
  are written in one token each (`#7`, `file:IN-FILE`, `(= #6 'Y')`). The fast loop's test task now
  passes `katana.record`, so a fast-loop golden can be rewritten like an integration one.
- **Touched:** `sir/contract/`, `docs/SEMANTIC-IR.md`, the sir tests, `build.gradle`, CLAUDE.md.

### J-075 Phase 2.7: F-08's findings from the IR, and how often the corpus has them
- **Who:** Opus. **Class:** build. **Intent:** 2.7's check.
- **Found:** the `findings` module may import nothing, so it holds the kinds and the report and
  `sir/check` finds them; `NonExact` refuses a profile other than the one the IR was built under. Over
  the corpus: CardDemo 2 unguarded divisions and 2 ODO tables; NIST 1 overlapping range, 1 recursive
  PERFORM, 233 unguarded divisions, 13 ODO tables; OMP 2 divisions. Division without ON SIZE ERROR is
  the common one: phase 3's lowering must say what it does with each. F-08's last class,
  uninitialized reads, needs data flow and is listed in FINDINGS.md as not yet found.
- **Touched:** `findings/`, `sir/check/NonExact.java`, `docs/FINDINGS.md`, tests, CLAUDE.md.

### J-073 The phase-2 boundary review: the IR's statement side was not complete, and every check was green
- **Who:** Fable asked a reviewer who had not seen the plan; Fable verified; Ghost's tithe call.
  **Class:** failure-caught.
- **Intent:** 2.8, the tithe evaluation Ghost asked for before phase 3.
- **Found:** `sir/build/Paragraphs.walk` skips every terminal node; `ROUNDED` is nowhere in `sir/`;
  EVALUATE's WHEN groups collapse and ANY is lost; INSPECT ALL and LEADING are one role; STOP RUN and
  STOP literal lose the word; arithmetic and conditions are stored as ProLeap rule shapes. Every IR
  type imports `katana.front.Mapped.Origin`, which `lower/` may not import. `ir.golden` holds ten
  lines of counts and the 415,863 contract lines are discarded. The data map side is sound.
- **Caught by:** a fresh reviewer with a probe program through `Build.program` and `IrJson.write`.
  Not caught by: the round-trip test (IR to JSON to IR, compared with itself) or the contract-facts
  count. The metrics were all green; the environment was fine; the artifact was not.
- **Resolved:** R-017 recorded with the "no token left behind" test as what holds it; tithe pass 2
  written as A (the IR), B (phase-3 seams), C (ordinary) in HANDOFF; 2.8 closes on A and B.
- **Note:** third member of the always-green family (J-006 the Gradle up-to-date task, the CI that
  would be always red). The rule they share: a check that compares a thing with itself, or that
  cannot fail, is not a check. The study should count these; the fix each time was a test that reads
  the *other* side (the tree, the file system, the run). Also: the reviewer's estimate — six weeks
  of phase 3 saved by 60 hours of tithe — is the tithe's claim (R-008) with a number on it.


### J-076 Tithe pass 2, A5: per-program contract goldens
- **Who:** Opus. **Class:** build. **Intent:** make A1-A4's changes land as reviewable diffs.
- **Found:** the smallest NIST program of each module is a monitor program of 7 to 55 facts, which would
  show nothing; the twelve kept are each module's smallest with 400 facts or more (DB204A IC237A IF142A
  IX111A NC127A OBIC3A RL101A RW102A SG202A SM106A SQ210M ST104A), 4,577 lines of COBOL and 6,132
  contract lines with COPYREC's. Only SM106A copies (K6SCA). The goldens live in `sir/golden/` and run in
  the fast loop, no corpus needed.
- **Touched:** `sir/golden/`, `IrGoldenTest`, `ContractTest`, `docs/SEMANTIC-IR.md`.

### J-077 Tithe pass 2, A2: the IR's own Origin
- **Who:** Opus. **Class:** build. **Intent:** `lower/` may import `sir` only.
- **Found:** nine `sir/types` files and `sir/check` used the front end's `Mapped.Origin`. `sir/types/Origin`
  (file, line, column, kind) replaces it, and `sir/build/Origins` is the one conversion. `sir/types` no
  longer names `katana.front`; no golden changed.
- **Touched:** `sir/types/`, `sir/build/`, `sir/check/NonExact.java`, `SirTypesTest`, SEMANTIC-IR.md.

### J-078 A check that cannot fail: the generalization, drafted for Fable
- **Who:** Opus, at Ghost's question. **Class:** decision (proposed). **Intent:** turn three instances
  into one rule a test can hold.
- **Found:** the always-green Gradle task, the always-red CI and the self-comparing round trip share
  one property: none could fail on the defect it guarded. Two tests capture it: independence (the
  expected side comes from outside the code under test) and falsifiability (a known-bad case is shown
  rejected). Both can be enforced (a zero-test suite is red; a registry of checks with mutations a
  meta-test applies); the judgment of independence cannot, and is a principle, not a decision.
- **Resolved:** the draft in HANDOFF "For Fable"; Ghost takes it to Fable. **Touched:** HANDOFF.

### J-079 The always-green family becomes a decision: R-056
- **Who:** Ghost asked whether the insight could be a directive; Opus analysed; Fable added the
  fourth case and recorded. **Class:** decision.
- **Found:** Opus's generalization — a check is worth what it can refute; it must compare against
  something the code under test did not produce and must be shown to fail once — splits into an
  enforceable half (oracle named, killer mutation declared, zero-run is red; a meta-test) and a
  judgment half (is the oracle really independent). Fable added the consequence clause from J-040:
  a verdict that stops no action is the same failure.
- **Resolved:** R-056 (the enforceable half, with Claims), a paragraph in GHOST-IN-THE-KERNEL.md
  (the judgment half), tithe item A7 (the meta-test).
- **Touched:** R-056, `docs/GHOST-IN-THE-KERNEL.md`, HANDOFF A7.
- **Note:** Opus drafted the decision in HANDOFF (J-078) and left the recording to Ghost's word, as
  R-043 asks; Fable recorded it with the consequence clause added. The first decision whose Why
  cites four journal entries as its evidence.

### J-080 Tithe pass 2, A1: no token left behind, and what reading the tree found
- **Who:** Opus. **Class:** build. **Intent:** R-017's test, and the IR it requires.
- **Found:** the walk now keeps every word (a keyword operand; the verb's own words and IS, ARE, THEN,
  END-x and punctuation aside), gives each rule holding statements its own branch with its other
  content as the condition, and writes arithmetic and conditions as operator trees (`Expressions`).
  The test reads the tree and pairs its statements with the IR's by textual order. The first corpus
  run left 695 tokens; reading them found five IR gaps (NEXT SENTENCE, unary plus, relative
  subscripts `I + 1`, reference modification on a function, a file named as a qualifier) and one
  missing program: a source with two top-level programs (NIST IC237A) built only its first, so
  `Build.programs` builds each. Three were the checker's (a copybook copied three times gives three
  statements of one origin; `+1` is one token; a qualifier naming a paragraph or file). It ends at 0 on
  CardDemo, NIST and OMP, and a mutation that drops keywords turns it red (kept as a test).
- **Resolved:** A1; R-017's Held by names the tests. **Touched:** `sir/build/`, the sir tests,
  `sir/golden/` (PROBE added; every contract's statements re-recorded), `sir/ir.golden`, theGhost.md.

### J-081 Tithe pass 2, A6: the role vocabulary, from the grammar
- **Who:** Opus. **Class:** build. **Intent:** the vocabulary phase 3's lowering switches on, pinned.
- **Found:** reading it from programs would pin only what a corpus happens to use, so `Roles` reads it
  from the grammar's ATN with the walk's own three rules (a value or procedure name is an operand of
  the rule calling it, a rule with `statement()` is a branch, any other rule is walked into): 50
  verbs, `sir/roles.golden`. Shaped as R-056 asks: its oracle is the programs (every verb, role and
  label the IR of the golden programs carries, and in the integration run of CardDemo, NIST and OMP,
  is inside it; all are), its mutation drops MOVE's roles and the check turns red (kept as a test),
  and the fast loop gates the commit. The vocabulary over-approximates: a role is any rule that can
  hold a word or value, so a verb's own rule and `cobol-word` appear; the lowering may map those to
  nothing, but must name them.
- **Resolved:** A6. **Touched:** `sir/build/Roles.java`, `Paragraphs` and `Values` (class-based
  forms of their predicates, so the walk and the vocabulary share them), the sir tests.

### J-082 Tithe pass 2, A3: literals that say what they mean, and cobc as their oracle
- **Who:** Opus. **Class:** build. **Intent:** the lowering decodes no literal (SIR-08).
- **Found:** `Value.Literal` is now text, `Kind` (alphanumeric, hex, null-terminated, national, DBCS,
  numeric, float, boolean, CICS) and payload (characters, hex bytes, or a signed unscaled integer and
  a scale); `Item.value` and `Condition.values` are `Value`s (a range a `THRU` expression), not text.
  Three things surfaced. (1) Every VALUE in the IR with a run of spaces inside its literal was wrong:
  the text was rebuilt from tokens and collapsed them (`"       COMPUTED="` was `" COMPUTED="`), in
  CardDemo's and NIST's report lines alike, and A5's goldens had pinned the wrong value, a golden
  written by the code it checks (R-056's case). (2) `ALL QUOTE` and the like reached the number
  decoder; they are now `Figurative("ALL QUOTE")`. (3) The oracle: `LiteralIntegrationTest` has cobc
  `-std=ibm` DISPLAY every literal of CardDemo, NIST and OMP (14,601 distinct; numbers through an
  `S9(18)V9(18)` field, a decimal comma in a program of its own) and compares the bytes with the
  payload. It refuted one thing: `-0` is a negative zero to cobc, and the payload had lost the sign;
  it keeps `-0` now. Its mutations (a wrong byte, a lost space, a lost sign, of each compared kind)
  each turn it red. National, DBCS, boolean and CICS literals are counted, not compared (one, zero,
  two and zero in the corpora).
- **Resolved:** A3; SIR-08. **Touched:** `sir/types/Value.java`, `Item.java`, `sir/build/Literals.java`,
  `Values`, `DataMap`, `Contract` (a literal of another kind prints its payload in braces), the
  goldens (the VALUE lines; PROBE gains each kind), `docs/SEMANTIC-IR.md`, the sir tests.

### J-083 Tithe pass 2, A4: PICTURE as data, and the clauses the IR dropped
- **Who:** Opus. **Class:** build. **Intent:** the lowering and `rt/` read no PICTURE string.
- **Found:** `Attributes.mask`, one symbol a character position for an edited item (S, V, P dropped;
  the currency sign written `$`, the point `.`, which the `Header` names: CURRENCY SIGN and
  DECIMAL-POINT IS COMMA, both read before and then lost); `Item.flags` JUSTIFIED, BLANK WHEN ZERO
  (a flag now, the category fold kept, as cobc folds it too), EXTERNAL, GLOBAL; `FileDescriptor`'s
  RELATIVE KEY. The oracle is cobc's own output, read only: each edited field's picture symbols
  (run-length, CR and DB written C and D), its flag bits (0x08 and 0x10, observed with a probe), and
  the names it takes EXTERNAL addresses of. On CardDemo and NIST all 750 masks and 36 flagged fields
  agree; `CobcLayoutTest` shows each comparison failing when ours differs. GLOBAL has no oracle (cobc
  writes nothing for it that a reader of its output can see); it is read, not compared.
- **Resolved:** A4. **Touched:** `sir/types/` (Attributes, Item, Header, FileDescriptor, Program's
  key check), `sir/build/` (DataMap, Tuples, Pictures, Files, Build), `Contract` (an `editing` kind;
  mask and flags on item lines; `relative` on file lines), `docs/SEMANTIC-IR.md`, the goldens, the
  data map oracle.

### J-084 Tithe pass 2, B4: rt/ and lower/ held before their first file
- **Who:** Opus. **Class:** build. **Intent:** phase 3's two new modules start inside their rules.
- **Found:** both were already entries of `modules.json`; what was missing was the holding. MOD-02
  said rt/ imports no library, but the source check looked only at imports of ours, and rt/ compiled
  in the main source set with ANTLR on its classpath. rt/ is now its own source set, compiled against
  the JDK alone (a probe file importing ANTLR failed to compile, then was removed), and `ModulesTest`
  refuses any rt/ import outside `java.`, rt/ code without `rt/LICENSE` (R-002; its text asked of
  Ghost, HANDOFF), and lower/ importing anything but sir/; each a rejects case on the empty modules.
- **Resolved:** B4. **Touched:** `build.gradle`, `test/ModulesTest.java`, HANDOFF "For Ghost".

### J-085 Tithe pass 2, B1: the oracle's two temporary readers retired, after an agreement trial
- **Who:** Opus. **Class:** build. **Intent:** one reader of COBOL, the front end and sir/; one
  PICTURE expander; one zoned decoder.
- **Found:** before deleting, each old reader ran against its replacement. `Layout` on all 31
  copybooks of the fixtures, CardDemo and OMP: 21 agree, 3 refused by both, and every one of the 7
  differences is the old reader's fault. Two lengths ignored a REDEFINES table longer than what it
  redefines (the data map's extent is cobc's, held by datamap.golden). Two copybooks it could not split
  on a quoted period. Three copybooks of several 01s it silently joined into one record, though its own
  comment said it refused them (its one-record check could not fire). The paragraph names on 39
  programs: 34 agree; the 5 others gain the paragraphs a procedure copybook COPYs in, which a trace
  enters and the area-A regex never saw. A test copybook had `05 ID`, a reserved word only the old
  reader accepted; renamed. `Layout` now flattens sir/'s data map (refusing what its four kinds cannot
  decode); `Paragraphs.of` reads the paragraph graph; `Overpunch` is the one ASCII zoned decoder, the
  comparator's and the sort's. Both `// Unsure: R-053 default` lines are gone with their readers.
- **Resolved:** B1. **Touched:** `oracle/compare/` (Layout, Decode, Overpunch), `oracle/sort/Zoned`,
  `oracle/lane/` (Paragraphs, CobcLane), their tests, the kernel map's closing rows.

### J-086 Tithe pass 2, B2: the generated program's contract, and NIST's runner seam
- **Who:** Opus. **Class:** build. **Intent:** phase 3's `lower/` emits to a written contract, and 3.4
  ("NIST through both lanes") has a seam to stand on.
- **Found:** LANES.md now says what a generated program is (LANE-06 to LANE-09): a static `run(dd,
  parm, sysin, sysout)` returning the return code, a `main` adapter over the `DD_` variables; an
  ASSIGN resolved through the DDs, then the working directory (NIST's `XXXXX001` names); an abend
  thrown and printed as LANE-04 says; NIST run through `oracle/nist/Runner`, which `Cobc` implements,
  so `ModuleRun` no longer names cobc. The in-process entry is chosen for time: 437 programs under two
  profiles as a JVM each is about five minutes the pre-push check lacks. The lanes' job steps already
  had a seam (`oracle/lane/ProgramRunner`); NIST was the one without. `CopyRecords`, the one Java
  program the java lane runs, now has the contract's shape, so the trivial lanes' check holds it.
- **Resolved:** B2. **Touched:** `docs/LANES.md`, `oracle/nist/` (Runner, Cobc, ModuleRun, Exec85),
  `CopyRecords`, the literal oracle's import.

### J-087 Tithe pass 2, B3: the autotest reader written, the fetch left to phase 3 as R-045 says
- **Who:** Opus. **Class:** decision. **Intent:** phase 3.1's check has its reader before its first
  step.
- **Found:** the plan's B3 (fetch now; extract into `oracle/fixture/autotest/*.fixture`) meets R-045
  twice: the autotests are fetched "only in phase 3", and GnuCOBOL's expected results are "never in
  this repository", which extracted cases committed under `oracle/` would be. Classed (d) under
  R-053, Ghost's, with a reversible default: `oracle/autotest/Autotests` reads the Autotest format
  (cases, keywords, `AT_DATA` files, each `AT_CHECK`'s command, status, stdout and stderr; m4 quoting
  one level, quadrigraphs) and writes each case to a folder under `corpus/`; its test is a sample
  written here from the Autoconf manual's description, so no GnuCOBOL file was fetched or opened.
  `fetch.sh` fetches the autotests only when named. One hypothesis stays open, marked `// Unsure`:
  whether an expected stdout written `[[x]]` means `[x]`; phase 3's first run under cobc settles it.
- **Options:** (1) as planned, fetching and committing now: against R-045's letter. (2) the reader
  now, the fetch and the count in phase 3's first step (taken). (3) nothing until phase 3: the seam
  would not exist when 3.1 starts.
- **Resolved:** B3 as far as R-045 allows; HANDOFF "For Ghost"; TODO 3.1 names the first step.
- **Touched:** `oracle/autotest/`, its test, `corpus/fetch.sh`, TODO.md, HANDOFF.

### J-088 Tithe pass 2, A7: R-056's meta-test, and its first run as the trial of C-2 and C-3
- **Who:** Opus. **Class:** trial. **Intent:** R-056 held by a test, and its two hypotheses measured.
- **Found:** `docs/checks.json` registers 18 checks (every integration test, every test reading a
  golden, and two more) with oracle kind and source, mutation test, and gate; `test/ChecksTest.java`
  holds it: an unregistered integration or golden test, a self golden with no finding, an oracle in
  the module's folder unexplained, a mutation that is not a test, a gate that does not run its test,
  and a pre-push suite that selects no test are each refused, with a rejects case each. `build.gradle`
  makes a test task that ran zero tests red (proved by pointing integration at a tag no test carries:
  "integration ran no tests"). The first run's own hit: IrIntegrationTest's vocabulary oracle,
  `sir/roles.golden`, lies in sir/; it is read from the grammar, so the entry says so. A mutation for
  the data map's offsets was missing and is added (`CobcLayoutTest#anOffsetOffByOneFindsNoItem`).
  Trial results: C-2, 6 of 18 checks have no killing test (findings, listed); C-3, the path rule's 4
  hits were all independent oracles in the module's folder, and the 3 real mirrors were found by the
  `self` declaration, one outside the module's folder (HANDOFF "For Fable").
- **Resolved:** A7. **Touched:** `docs/checks.json`, `test/ChecksTest.java`, `test/DocTests.java`,
  `build.gradle`, `CobcLayoutTest`, R-056's Held by, HANDOFF.

### J-089 Tithe pass 2, C1: the kernel map's body was stale by construction
- **Who:** Opus. **Class:** build. **Intent:** a ledger whose columns are true.
- **Found:** 640 of the map's rows said only "called by X" (or "no caller"): written once at a
  function's first sight and never changed, since a row is never changed, and found by a name-only
  scan that made `Placement.resolve` "called by FixtureCheck.profiles and 29 more". No function in
  a module cites a `// Decision:` yet, so the plan's other column would be empty too. The script now
  writes the cited decision or nothing; the open rows were rewritten once to match (the one change to
  existing rows the map has had, said in its header); the superseded and removed ledger, which is
  the map's job, is untouched. 90 KB to 59 KB.
- **Resolved:** C1. **Touched:** `scripts/KernelMap.java`, `docs/KERNEL-MAP.md`.

### J-090 Tithe pass 2, C2: a quick NIST for the pre-push check
- **Who:** Opus. **Class:** build. **Intent:** a change to the IR, the lowering or the runtime meets
  NIST before it is pushed, inside the check's budget once javac and a run are added in phase 3.
- **Found:** `-Dkatana.nist.modules=NC,IF` runs those modules only and holds them to their lines of
  both goldens (a record from a subset is refused, so a quick run cannot shrink a golden). NC and IF
  are 140 programs, 9,815 tests under the two dialects, in 56 s. `prepush.sh` runs it for a change to
  `sir/`, `lower/` or `rt/`, and drops it when the whole of NIST runs (a change to `oracle/nist/` or
  the profile); `--all` at a boundary runs everything. The suite is held by ChecksTest like the others
  (it must select a test).
- **Resolved:** C2. **Touched:** `ModuleRunIntegrationTest`, `build.gradle`, `scripts/prepush.sh`.

### J-091 Tithe pass 2 recorded: the phase-2 boundary, and the R-053 count
- **Who:** Opus. **Class:** measurement. **Intent:** C4: re-measure, record, count; close 2.8.
- **Found:** every metric within its target but one. The warm fast loop is 14 s (11), cold 22 s;
  the largest file 258 lines (344 before C3); the docs ratio 0.32. The pre-push check over every
  suite is 3.5 minutes against 3.4 armed and a 3-minute target: A3's literal oracle parsed the corpora
  a fourth time and took it to 4.1; letting one corpus pass serve both the IR report and the literal
  check brought it back; the rest is three heavy classes (NIST, the data map, the front corpus) each
  parsing NIST in its own JVM, the next tithe's to merge. Two decisions named tests that did not
  exist: R-046's JournalTest and R-045's LicenseTest, both written now (a sweep of every Held-by
  found no third). JournalTest lists the one duplicate id the journal has (the boundary review's
  J-073, which R-056 cites) and refuses another. The R-053 count for phase 2: six "For Fable"
  defaults held, none reversed, one stop downgraded to a deferral (J-069); this tithe adds three
  open items under its own defaults (B3 against R-045 and rt/LICENSE for Ghost; R-056's C-3 trial
  for Fable), to be counted when they are answered.
- **Resolved:** C4; 2.8 (A1-A7, B1-B4 and C1-C4 on main; R-017 in force, held by A1's tests).
- **Touched:** `docs/AGENT.md`, `docs/DONE.md`, TODO.md, HANDOFF, the journal and license tests,
  R-045's and R-046's Held by, `IrIntegrationTest`, `LiteralIntegrationTest`.

### J-092 The phase-2 boundary: three deferred items answered; the plan contradicted a decision
- **Who:** Opus listed; Fable answered two and drafted one; Ghost's word on the draft. **Class:**
  decision.
- **Found:** Fable's tithe item B3 told Opus to fetch the GnuCOBOL autotests now and commit their
  extracted cases under `oracle/fixture/`, which R-045 (Fable's own decision of the day before)
  forbids twice over; Opus took the reversible default and reported. R-056's first trial refuted its
  claim C-3 as stated (the oracle-path rule: 4 hits, all false; the 3 real mirrors caught only by
  their own `self` declaration). `rt/` could not take its first file for want of a license text.
- **Resolved:** B3's default holds, R-045 stands, the plan was wrong. C-3 restated: independence is
  declared, not detected. `rt/LICENSE` drafted from R-002 as the per-customer template, DRAFT until
  Ghost's word, counsel before the first delivery.
- **Touched:** R-056 Claims; `rt/LICENSE`; HANDOFF.
- **Note:** the R-053 count for phase 2, final: six defaults held, none reversed, one stop downgraded
  to a deferral, plus at the boundary one default held against the plan that contradicted it. A
  decision outranked a plan, which is the order the documents say. Also the pass's own numbers: the
  new checks caught squeezed spaces in 14,601 literals, a lost sign on -0, a copybook reader gluing
  01 records together, and two decisions whose named tests never existed.


### J-093 Phase 3.1, first step: the autotests fetched, read and run once
- **Who:** Opus. **Class:** trial. **Intent:** 3.1's check has its cases, counted, and the reader's
  open hypothesis is put to a run.
- **Found:** `fetch.sh gnucobol-autotests`, pinned at 868143c, gives 37 `.at` files; every one
  reads: 1,446 cases, 378 that compile a program and run it against an expected stdout, 169 marked
  `AT_SKIP_IF` or `AT_XFAIL_IF` (counts in `oracle/autotest/autotests.golden`, cases under
  `corpus/`, nothing of their text on screen or in the tree, R-045). One case wrote a file into a
  subfolder; the writer makes the folder and refuses a path out of its case. Each runnable case ran
  once under cobc with the Autotest variables mapped to cobc's own command line (`$COMPILE` as
  `cobc -x`, and so on): 227 of 378 pass; 116 of the failures are an exit status, likely flags the
  testsuite's configuration sets, which R-045 keeps off the screen. The hypothesis: my first scoring
  said no expected stdout held a bracket; the integration test I then wrote said two did. The script
  had stopped at each case's first failing check and never looked at later ones. The two cases
  (run_misc.at:1762, run_ml.at:856) both stop with exit 127 before printing, so the question stays
  open, named, and the test lists the cases it turns on.
- **Caught by:** the assertion written to hold the trial's result, on its first run (R-056 again).
- **Resolved:** 3.1's first step. **Touched:** `oracle/autotest/`, its tests, `corpus/fetch.sh`
  (pinned), `scripts/prepush.sh` (an `autotest` suite), `docs/checks.json`, TODO.md.

### J-094 Phase 3.1: Appendix A located, and refused by the network
- **Who:** Opus. **Class:** stop (R-053, one step). **Intent:** read IBM's intermediate-results rules
  before `rt/Decimal`, as the roadmap and R-049 require.
- **Found:** a search finds the topic and the 6.4 Programming Guide PDF; `www.ibm.com` and
  `publibfp.dhe.ibm.com` are both refused by this environment's egress proxy (403), so the notes'
  "[unverified] recollection" stays unverified. No reversible default exists for freezing `Decimal`
  on a recollection R-049 calls a hypothesis, so that step stops; the parts of 3.1 that do not
  compute intermediates continue.
- **Resolved:** HANDOFF "For Ghost" (allow the two hosts, or attach the PDF). **Touched:** HANDOFF.

### J-095 Phase 3.1: Appendix A read, and the recollection was wrong twice
- **Who:** Opus; Ghost allowed the hosts; Fable set the terms (IBM's text as quotes with pages;
  GnuCOBOL's implementation never a second opinion, only cobc's output). **Class:** decision built
  (a reading of a source, R-052 (1)).
- **Found:** the 6.4 Programming Guide, pp. 793-801, read in full and quoted into
  `docs/research/ibm-options.md` §4. The recollection that stood there since phase 1 was wrong in
  two places: past the 30/31 cap with d no more than dmax, IBM keeps the decimals and loses integer
  places (the recollection reduced decimals); and in division op1 is the divisor, so the places are
  i2 + d1 and max(d2 - d1, dmax). The first `rt/` file, `ArithProfile`, holds the tables (each
  function citing R-049); its test holds each row and the measured point, `1 / 3 * 300` into
  `999V99` = 99.00 under `cobc -std=ibm`, which the table gives (0.33 then 99.00). PROFILE.md's
  Appendix A row is verified. What the text leaves open (truncation or rounding of decimals an
  intermediate cannot carry; when ROUNDED's extra place is carried) is `rt/Decimal`'s, to settle
  against cobc's output.
- **Resolved:** R-049's Appendix A row verified (its Decision text, which calls it a hypothesis, is
  Ghost's to amend); the HANDOFF item closed. **Touched:** the research notes, PROFILE.md,
  `rt/katana/rt/ArithProfile.java`, its test, the kernel map.

### J-096 Appendix A read: the hypothesis was wrong in two rules, and nothing stood on it
- **Who:** Ghost opened the hosts; Opus read and transcribed; Fable amended R-020 and R-049.
  **Class:** measurement → decision.
- **Found:** IBM's Appendix A (6.4 PG pp. 793–801) against the recollection carried since phase 1:
  past the 30/31-digit cap IBM keeps decimal places and drops high-order integer places when
  d ≤ dmax (the recollection said the opposite); in division op1 is the divisor and the carried
  places are i2 + d1 and max(d2 − d1, dmax) (the recollection had the operands swapped). The rest
  held. `rt/ArithProfile` is built from the tables; the reproducer gives 99.00 under them.
- **Resolved:** R-020 amended (IBM profiles are results, not findings); R-049's Appendix A row
  verified; the one remaining question (truncate or round the uncarried places; ROUNDED's extra
  place) is a trial against `cobc`.
- **Touched:** R-020, R-049, `docs/research/ibm-options.md` §4, `docs/PROFILE.md`, `rt/ArithProfile`.
- **Note:** the status discipline did its job: two wrong rules sat in the notes for two days marked
  `hypothesis`, and no code was built on them. Had R-049 recorded them as fact, `rt/Decimal` would
  have shipped the wrong overflow rule and the wrong division. R-052's cost of a hypothesis was one
  label; the cost of the alternative would have been a penny in a bank's ledger.

### J-097 Phase 3.1: the intermediate-results trial; truncation holds, ROUNDED's place does not enter dmax
- **Who:** Fable designed the trial and set the defaults; Opus ran it. **Class:** trial.
- **Found:** first IBM's own text was checked for the ROUNDED question: the Programming Guide says
  only that one more place "might be carried"; the Language Reference (p. 296) verifies the final
  store (truncate unless ROUNDED, half up on the excess's first digit) and says nothing of dmax. Then
  cobc `-std=ibm`. The first fixture used literals and its control printed 1.9, not 1.8: cobc folds an
  all-literal expression at compile time with more places, so the fixture was wrong, as the control
  was there to say. With data items: `A / B * B` into 9V99 is 1.98 (truncation; rounding gives 2.01),
  into 9V9 ROUNDED 1.8 (no extra place in dmax; with one, 2.0), the control 1.8, and 1 / 3 * 300 into
  999V99 99.00. The truncation default holds; the ROUNDED default is refuted on cobc and becomes
  `ArithProfile.dmax` with no added place, an `// Unsure` naming IBM's "might". The literal folding is
  a finding against cobc's dialect (HANDOFF).
- **Caught by:** the trial's control case, on its first run.
- **Resolved:** `ArithTrialIntegrationTest` (cobc its oracle; `ArithProfileTest#roundingThe...` its
  mutation; the new `rt` pre-push suite its gate). **Touched:** `rt/ArithProfile`, its tests,
  PROFILE.md, the research notes, `scripts/prepush.sh`, `docs/checks.json`, HANDOFF.

### J-098 A double check of the chain from phase 2 to 3.1, on Ghost's hunch
- **Who:** Ghost ("my qa spidey senses are tingling"); Opus audited. **Class:** trial.
- **Found:** a clean baseline first (every suite, fresh clone, 240 s). Then each claim of the chain
  asked three things: what holds it, whether that comes from outside what it checks, whether it could
  fail. Three suspects. (1) A4's mask oracle normalized cobc's pictures with *our* reading of
  CURRENCY SIGN and DECIMAL-POINT IS COMMA, so a misreading would have shifted both sides alike and
  agreed: a mirror. cobc writes each unit's `decimal_point` and `currency_symbol` into its C; the
  data map test now compares them, and every unit agrees (2 NIST units set them): no bug, a hole
  closed. (2) J-093's "227 of 378" compared stdout only where one was expected; strictly re-scored it
  is still 227 (stderr was never compared, and is not claimed). (3) The text helper that squeezed
  VALUE spaces (J-082) still built the ASSIGN name, which was also upper-cased with its quotes
  stripped: `ASSIGN TO "my  data.txt"` became `MY DATA.TXT`, a wrong file once LANE-07 resolves it.
  No corpus program has such a literal, which is why no check saw it. Fixed: a literal keeps its
  payload exactly, a word is upper-cased; the new test fails on the old code ("MY DATA.TXT"). A sweep
  for the same class (literal text upper-cased or squeezed into the IR) found CALL targets already
  exact and every other upper-casing applied to COBOL words.
- **Caught by:** the hunch, then reading each check for where its expected side came from.
- **Resolved:** `Files.assign`; `BuildTest#anAssignLiteralIsKeptAsWritten`; the data map test's
  `editing` column (sir/datamap.golden). **Touched:** `sir/build/Files.java`, the sir tests, the golden.

### J-099 J-097 was wrong on ROUNDED: the extra place is the last operation's
- **Who:** Ghost asked whether the ROUNDED claim could be asserted through a full evidence chain;
  Opus found it could not, and ran the case that showed why. **Class:** trial; correction.
- **Found:** walking Ghost's chain (IBM text, formal rule, independent model, oracle, differential
  test, mutation, regression, implementation): the claim "ROUNDED adds no place" did not come from
  IBM's text (which says a place "might be carried"), had no independent model (the trial's
  evaluator called `ArithProfile`, the code under test), and rested on one case that could not tell
  "no place" from "a place on the last operation". The case that could: `COMPUTE R ROUNDED = A / B`
  into 9V9, 2 and 3, prints 0.7 on cobc `-std=ibm` (without ROUNDED 0.6; `DIVIDE ... GIVING ROUNDED`
  0.7). With J-097's `A / B * B` ROUNDED at 1.8, only one reading fits: ROUNDED's place on the last
  operation, none on the earlier ones. `ArithProfile.last` carries it; the trial has seven cases and
  takes the reading as a parameter, and a test shows each wrong reading (rounded intermediates; no
  place; the place in dmax) turning it red. On IBM itself the question stays open, with PH41526
  pointing toward ROUNDED reaching intermediates.
- **Caught by:** Ghost's "spidey sense", then the evidence chain applied to the newest claim.
- **Resolved:** `ArithProfile.last`; `ArithTrialIntegrationTest`; `ArithProfileTest#eachWrong...`;
  PROFILE.md, the research notes; HANDOFF (the chain for Fable). **Touched:** those.

### J-100 A merge pushed to main before its fast loop passed
- **Who:** Opus. **Class:** failure caught. **Intent:** merge Fable's R-020 claims and push.
- **Found:** one shell line chained the fast loop and the push with `;`, not `&&`; Docker had died,
  the loop exited 1, and the push to `main` (9a5ea03, a clean merge of docs) went ahead. The verdict
  stopped nothing: R-056's fourth case, by the agent that drafted it. The merge was then gated over
  every suite on a fresh clone (passed, 291 s, e04fb91). From here a push is its own command, run
  only after the gate's line says passed.
- **Caught by:** reading the exit code after the push. **Cost:** none in code; one ungated push.

### J-101 Phase 3.1: rt/Decimal, an independent model, and the three-way test
- **Who:** Opus, on the evidence chain Ghost proposed (J-099). **Class:** build; trial.
- **Found:** `rt/Decimal` (R-020: unscaled `BigInteger` and scale, each operation cut to
  `ArithProfile`'s places, `store` with ROUNDED half away from zero, high-order loss as a size error,
  division by zero a status). `AppendixAModel`, written from the quoted text and sharing nothing with
  `rt/`: exact fractions, the cap table as data rows. `ArithDifferentialIntegrationTest`: 400 COMPUTEs
  from a fixed seed, one cobc `-std=ibm` program, each case computed three ways. The coverage guard
  caught the first generator: only 1 case passed the cap, so the cap rows were barely tried; with
  larger operands 40 do. Model and `rt` agree on all 400. cobc agrees on all 360 below the cap and 33
  of the 40 above it; in the other 7 it equals the model with no cap, so cobc's ibm dialect does not
  apply the 30-digit cap. A finding against cobc (Fable's rule), the 7 listed by case so a change in
  either shows; the cap rows' only check is IBM's text and the independent model (HANDOFF). Its
  mutation: the model without ROUNDED's place disagrees with cobc. `RtSourceTest` holds R-020's "no
  MathContext"; it first caught Decimal's own comment naming it, and now reads code lines only.
- **Resolved:** the chain filled for Appendix A's fixed-point rules: text, rule (`ArithProfile`),
  independent model, oracle, differential, mutation, regression, implementation.
- **Touched:** `rt/katana/rt/Decimal.java`, `test/katana/rt/`, `docs/checks.json`, PROFILE.md,
  HANDOFF, TODO.md (Fable's two carried notes).

### J-102 Phase 3.1: Record, Field and the codepages, held to cobc's bytes
- **Who:** Opus. **Class:** build; trial.
- **Found:** R-019's byte image in `rt/`: `Record` (a `byte[]` and its codepage), `Field` (a view
  with `rt/`'s own attribute tuple, `Attr`, since `rt/` imports nothing), and `Codepage`. The cobc
  lane's encoding was observed, not assumed: a negative zoned sign is the digit plus 0x40 (`s` for
  3), a positive one the plain digit, a separate sign `+` or `-`, packed C/D/F, COMP big-endian and
  COMP-5 little-endian (x86's order). `CodecDifferentialIntegrationTest`: 300 generated fields MOVEd
  a value and DISPLAYed raw under cobc -std=ibm; `rt/Field` writes the same bytes and reads them
  back to the same value on all 289 it compares. The other 11 are COMP-5 of 1 or 2 digits, which
  cobc stores in 1 byte where IBM gives a halfword: found because the test's byte walk lost its
  place, a finding against cobc (HANDOFF, TODO 3.4). The EBCDIC codepage has no cobc oracle; IBM's
  preferred signs hold it in `FieldTest`. Its mutation: the EBCDIC codepage's bytes differ from
  cobc's on more than 50 cases.
- **Resolved:** `rt/` Record, Field, Attr, Codepage. **Touched:** `rt/katana/rt/`, `test/katana/rt/`,
  `docs/checks.json`, HANDOFF, TODO.

### J-103 Phase 3.1: MOVE, against IBM's rules and cobc's bytes; one rt/ bug, one cobc finding
- **Who:** Opus. **Class:** build; trial.
- **Found:** IBM's MOVE rules read from the Language Reference (pp. 174, 198, 239, 402-405):
  numeric alignment on the decimal point, alphanumeric left alignment with space fill, JUSTIFIED,
  a numeric integer into text as its unsigned digits, text into a number as an unsigned integer, a
  group move a byte copy. `rt/Move` does those; `MoveDifferentialIntegrationTest` moves 400
  generated senders into generated receivers under cobc -std=ibm with binary truncation (the lane
  profile). First run: 27 differ. 22 were `rt/`'s: a MOVE into COMP-5 was cut to its PICTURE, where
  IBM says "truncation occurs at the binary field size rather than at the COBOL picture size limit"
  (p. 239) and cobc agrees; fixed. 5 are cobc's: a numeric sender longer than a JUSTIFIED receiver
  keeps its leftmost digits, where IBM truncates the leftmost; each checked to equal the move with
  JUSTIFIED ignored, and listed. Its mutation: JUSTIFIED ignored, more than 10 cases differ. Not yet:
  edited receivers and de-editing, and a differential of its own for group moves (they share the
  alphanumeric placement the test holds).
- **Resolved:** `rt/Move`; `Attr` gains JUSTIFIED. **Touched:** `rt/`, `test/katana/rt/`,
  `docs/checks.json`, HANDOFF.

### J-104 Phase 3.1: editing, held to IBM's printed examples and to cobc; five cobc findings
- **Who:** Opus. **Class:** build; trial.
- **Found:** IBM's editing rules read from the Language Reference (pp. 220-225, 404): simple, special,
  fixed and floating insertion, zero suppression with Z and `*`, BLANK WHEN ZERO, alphanumeric
  editing, de-editing. `rt/Editor` does them on the expanded picture (`rt/Edit`); `Move` edits into a
  numeric-edited or alphanumeric-edited receiver and de-edits a numeric-edited sender. Two checks:
  `EditorTest`, every example IBM prints for editing, which passed first time, and
  `EditDifferentialIntegrationTest`, 400 generated edited moves (and each moved back, de-edited) under
  cobc -std=ibm. First run: 59 differ. One rule was `rt/`'s to choose: a negative value cut to zero;
  the sign of the sender was tried and cobc showed positive for +, CR, DB and floating signs, so
  zero shows positive, as IBM's "positive or zero" column reads (an `// Unsure`; HANDOFF). The other
  37 fall into five families, each checked by what cobc's bytes show, not just named: a sign next to
  the currency sign (cobc gets IBM's own `-$999.99` example wrong), suppression judged before
  truncation, DB not de-edited as negative, an inserted 0 de-edited as a digit, a trailing - on a
  value cut to zero. Listed as findings. Its mutation: suppression read as 9s, more than 20 differ.
- **Resolved:** `rt/Editor`, `rt/Edit`; `Attr` gains the edited categories and its `Edit`.
  **Touched:** `rt/`, `test/katana/rt/`, `docs/checks.json`, HANDOFF.

### J-105 Phase 3.1: INSPECT, held to IBM's examples and to cobc; cobc runs phrases one at a time
- **Who:** Opus. **Class:** build; trial.
- **Found:** IBM's INSPECT rules read from the Language Reference (pp. 353-360): comparison cycles
  from the leftmost position, the phrases compared in written order at each; CHARACTERS, ALL,
  LEADING, FIRST; BEFORE and AFTER INITIAL; CONVERTING as ALL phrases. `rt/Inspect` does them over an
  item's bytes. `InspectTest` holds it to IBM's four printed examples (passed first time);
  `InspectDifferentialIntegrationTest` to cobc over 400 generated statements. First run: 8 differ.
  7 were a reading IBM leaves open: with both BEFORE and AFTER, `rt/` searched BEFORE's delimiter
  from AFTER's end, cobc from the item's start; `rt/` now does as cobc (an `// Unsure`; HANDOFF). The
  8th was a probe's worth: cobc runs a REPLACING's phrases one at a time, positions claimed by an
  earlier phrase barred to later ones, where IBM's text is cycles. Probed by hand (`BA-ABA`), then two
  such cases added to the test; a phrase-at-a-time model written in the test fits cobc on all 402 and
  explains exactly the 3 listed findings. Its mutation: LEADING compared as ALL, more than 10 differ.
- **Resolved:** `rt/Inspect`. **Touched:** `rt/`, `test/katana/rt/`, `docs/checks.json`, HANDOFF, TODO.

### J-106 Phase 3.1: STRING and UNSTRING, held to IBM's text and to cobc
- **Who:** Opus. **Class:** build; trial.
- **Found:** IBM's STRING and UNSTRING rules read from the Language Reference (pp. 457-471): the
  pointer, transfer up to a delimiter or whole, no space filling, overflow; UNSTRING's examine, move
  and iterate stages, ALL, OR, DELIMITER IN, COUNT IN, TALLYING IN, the examined size without a
  delimiter (Table 54). `rt/Strings` does them, each UNSTRING part MOVEd into its receiver by
  `rt/Move`. IBM's examples are figures, so `StringsTest` holds what its text states in words ("an
  occurrence of ABC is considered an occurrence of AB"). `StringsDifferentialIntegrationTest`: 400
  generated statements under cobc. First run: 29 differ, all the test's fault: numeric receivers
  given `,` or `;`, a MOVE IBM leaves undefined; the generator now makes every non-digit a delimiter
  for them. Two readings IBM leaves open were probed by hand and agree with `rt/`: ALL takes repeats
  of the same delimiter as one, not of any (an `// Unsure` naming it cobc's reading), and STRING
  filling its receiver exactly does not overflow; both added as fixed cases. Its mutation: delimiters
  ignored, more than 20 differ.
- **Resolved:** `rt/Strings`. **Touched:** `rt/`, `test/katana/rt/`, `docs/checks.json`, TODO.

### J-107 Phase 3.1: comparison and collation, held to IBM's text and to cobc
- **Who:** Opus. **Class:** build; trial.
- **Found:** IBM's relation rules read from the Language Reference (pp. 274-279): numbers by algebraic
  value; text character by character in the collating sequence, the shorter extended with spaces; a
  zoned integer against text as its digits, unsigned under ZWB (IBM's default); a group by its bytes;
  the program collating sequence from the ALPHABET clause, HIGH-VALUE and LOW-VALUE its ends. `rt/Compare`
  and `rt/Collation` do them. `CompareDifferentialIntegrationTest`: 400 generated comparisons under
  cobc, the 300 non-numeric ones again under an EBCDIC program collating sequence. First run: 7
  differ, all the test's: an operand given a value its PICTURE cuts was stored uncut; it now gets its
  value through `rt/Move`. Then all agree, in both sequences. Its mutation: the native order under the
  EBCDIC sequence, more than 10 differ.
- **Resolved:** `rt/Compare`, `rt/Collation`. **Touched:** `rt/`, `test/katana/rt/`, `docs/checks.json`, TODO.

### J-108 Phase 3.1: SIZE ERROR, held to IBM's text and to cobc; cobc judges COMP-5 by capacity
- **Who:** Opus. **Class:** build; trial.
- **Found:** IBM's size error rules read from the Language Reference (p. 296): after alignment and
  rounding, a result that exceeds what the PICTURE holds (a binary item's PICTURE, whatever TRUNC says),
  or a division by zero; with ON SIZE ERROR the identifier is not altered, without it truncation rules
  apply; each resultant identifier on its own. `rt/Arithmetic.store` does it, a native binary cut at
  its binary size when the phrase is absent (p. 239, through `Move.number`).
  `SizeErrorDifferentialIntegrationTest`: 300 generated ADD (two receivers), COMPUTE and DIVIDE
  statements under cobc. 294 agree first time. The other 6 are one reading: cobc judges a COMP-5
  receiver's size error by its binary capacity and stores the value, where IBM's text names COMP-5
  and the PICTURE; a model of that reading in the test explains exactly those 6, listed as findings.
  Division by zero without the phrase is not generated: `rt/` leaves the receiver as it was (an
  `// Unsure`; HANDOFF). Its mutation: storing despite the phrase, more than 10 differ.
- **Resolved:** `rt/Arithmetic`. **Touched:** `rt/`, `test/katana/rt/`, `docs/checks.json`, HANDOFF, TODO.

### J-109 Phase 3.1: DISPLAY of numbers, held to IBM's Table 48 and to cobc
- **Who:** Opus. **Class:** build; trial; question.
- **Found:** Starting 3.1's check (the autotests as `rt/` tests), every program DISPLAYs, and `rt/` had no
  DISPLAY. IBM's rule (LR 6.4 p. 333; Programming Guide 6.4, DISPSIGN and its Table 48): binary and
  packed items become zoned decimal of their PICTURE's digits, no point shown; under DISPSIGN(COMPAT),
  the default, a negative value's last digit overpunched and zoned items as their bytes; under SEP a
  leading separate sign. `rt/Display` does both; `DisplayTest` holds every row of Table 48 in EBCDIC
  (passed first time). Probed by hand, cobc -std=ibm is neither form; under SEP, `rt` equals cobc on
  300 generated items but for two families, each checked against a model of cobc's display: binary
  items with the digits of their binary field (150), overpunched zoned items with the sign where the
  overpunch was (5). Which DISPSIGN a program has is a compile option, so a profile field is proposed
  (HANDOFF), not added. The autotest check's scope was taken as a default (HANDOFF): the cases `rt/`
  holds are hand-lowered now, the rest wait for the lowering. Its mutation: COMPAT, more than 50 differ.
- **Resolved:** `rt/Display`. **Open:** `ibm.dispsign` (Fable). **Touched:** `rt/`, `test/katana/rt/`,
  `docs/checks.json`, HANDOFF.

### J-110 Phase 3.1's check by hand: the first nine autotests on `rt/`
- **Who:** Opus. **Class:** build; trial.
- **Found:** `AutotestRtIntegrationTest` runs each hand-lowered autotest program (Java against `rt/`
  alone, `test/katana/rt/autotest/`) and holds its output two ways: to the testsuite's expected stdout,
  pass or fail only (R-045), and to cobc -std=ibm running the same `prog.cob`, in full, a difference a
  listed finding with its reason; the results are a golden. Nine hand-lowered (moves into JUSTIFIED and
  edited items, P-scaled senders, separate signs, DISPLAY of zoned items, STRING and INSPECT with
  sources that must not change, MULTIPLY GIVING into an edited item): 8 pass the testsuite; 7 equal
  cobc -std=ibm, the other 2 J-109's zoned sign in place. Three things `rt/` lacked, found by the
  programs: P scaling (a P-scaled integer moved as its digits, each P a zero, LR p. 404), INSPECT's
  view of a signed zoned item (Table 39: its digits unsigned, copied back with its sign), and an
  arithmetic result stored into a numeric-edited item (edited). Its mutation: a case held to another
  case's expected output fails.
- **Resolved:** the harness and nine cases. **Touched:** `rt/` (Move, Compare, Inspect, Arithmetic),
  `test/katana/rt/autotest/`, `docs/checks.json`, TODO.

### J-111 Phase 3.1's check by hand: eighteen autotests; Appendix A against a real program
- **Who:** Opus. **Class:** build; trial.
- **Found:** Nine more autotests hand-lowered (edited moves with floating and simple insertion, sign
  editing, abbreviated conditions, sign conditions, DECIMAL-POINT IS COMMA arithmetic, CURRENCY SIGN,
  STRING and UNSTRING overflow, and "Default Arithmetic (1)"): 18 in all, 14 passing the testsuite. Every
  difference from cobc -std=ibm is a finding already recorded: J-109's zoned sign (3 cases), J-104's
  suppression judged before truncation (run_fundamental 2433), and one new: cobc's ibm dialect does not
  take B 0 / , inside a floating string as part of it (run_fundamental 2513, `-//00BB,,-`), where IBM's
  text does (LR p. 223) and the testsuite's own expected output agrees with Katana. "Default Arithmetic
  (1)" is the first real program through `rt/Decimal`: its integer divisions truncated by Appendix A
  (`((399 / 100) - (211 / 100)) * 100` into `9(3)` is 100), dmax raised by an edited receiver (188),
  literal-only subexpressions, a condition's dmax: every line equals cobc -std=ibm; the testsuite,
  written for GnuCOBOL's default arithmetic, fails it, as it should. One slip, mine: `MULTIPLY A BY B`
  stores into B; caught by cobc's output before it was recorded. One reading taken (an `// Unsure` in
  the test's `Calc`): dmax counts operands inside a divisor expression, only a direct divisor left out.
- **Resolved:** 18 cases. **Touched:** `test/katana/rt/autotest/`.

### J-112 Phase 3.1's check by hand reaches what `rt/` holds: 24 autotests, 19 passing
- **Who:** Opus. **Class:** build; trial.
- **Found:** Six more hand-lowered (static and dynamic reference modification, a literal DISPLAY, an
  index condition, INITIALIZE of tables with leading and trailing separate signs and of packed items):
  24 in all, 19 passing the testsuite, every one equal to cobc -std=ibm or a recorded finding. The 5
  that fail the testsuite fail where its default dialect is not IBM's (a DISPLAY showing a decimal point
  or a sign IBM's DISPSIGN does not), and equal cobc -std=ibm or J-109's finding there. That is the
  reach by hand for now: of the runnable cases left, the rest need control flow (3.2), intrinsic
  functions, files, CALL, GnuCOBOL's own items (78-level constants, BINARY-LONG, ROUNDED MODE, VALUE in
  a REDEFINES) or compile flags the harness does not pass; they go through the lowering (3.3), as the
  HANDOFF default says.
- **Resolved:** 24 cases. **Touched:** `test/katana/rt/autotest/`.

### J-113 Phase 3.2: `rt/Dispatcher`, PERFORM's return held to cobc's ibm dialect
- **Who:** Opus. **Class:** build; trial; question.
- **Found:** R-021's design built: the lowering's `for (;;) switch (pc)` asks `Dispatcher.end` at every
  paragraph's end and `perform` at every PERFORM; ALTER is a table. IBM's rule for control reaching an
  active PERFORM's exit from inside another is "must not" (LR p. 414), so the return reading was probed
  by hand on one program that separates three: cobc -std=ibm returns from the innermost matching
  PERFORM and drops those begun inside it, cobc's default checks only the innermost (ANSI). `rt/`
  follows the ibm dialect. `DispatcherDifferentialIntegrationTest`: 60 generated programs (PERFORM of
  later ranges, GO TO anywhere, a step limit), 20 generated in the separating shape, and the probe: all
  81 equal to cobc -std=ibm. The first mutation run showed the generated programs alone never separated
  the readings (only the probe did), so the family was added; the ANSI reading now differs on 21. A
  check that could not have failed, caught by its own mutation (R-056 working as meant).
- **Resolved:** `rt/Dispatcher`. **Open:** R-021 C-1's status (Fable). **Touched:** `rt/`,
  `test/katana/rt/`, `docs/checks.json`, HANDOFF, TODO.

### J-114 Phase 3.3 begins: `lower/`, its first increment, held to the hand-lowerings
- **Who:** Opus. **Class:** build; trial; question.
- **Found:** `lower/` writes one Java class per program against `rt/` alone (R-024's one emitter, R-021's
  dispatcher): a `Record` per 01 and 77, a `Field` per item, VALUEs, each stretch of statements a method
  returning the next label, one provenance comment per statement. The check that matters most is the
  one 3.1 made possible: each hand-lowered autotest's own `prog.cob` through the whole pipeline (parse,
  IR, lower, javac, run) must print what its hand-lowering prints. 21 of the 24 parse; all 21 do,
  byte for byte, after three fixes the comparison forced: the IR counts each P as a digit and `rt/` only
  the stored ones; dmax counts the operands inside a divisor expression (the hand-lowering's reading,
  which cobc -std=ibm agreed with in J-111); an index item was not held. Five readings taken, each an
  `// Unsure` and a HANDOFF item. Found on the way: the front end reads `1,1` as a decimal in a program
  without DECIMAL-POINT IS COMMA (HANDOFF; not fixed here). Build's NPE on an unclean parse was mine:
  callers check `clean()` first, as the lowering's test now does.
- **Resolved:** `lower/` (Lower, Emitter, Exprs, Values, Arith, Conditions, Flow, Verbs, ArithVerbs,
  Performs, TextVerbs, Initialize); `rt/` gains Sysout, Literal, Table, Classes, Unsupported.
  **Touched:** `lower/`, `rt/`, `test/katana/rt/autotest/`, `docs/checks.json`, HANDOFF, TODO.

### J-115 Phase 3.3-3.4: NIST NC through the Java lane, 15 of 95 runs equal to cobc's
- **Who:** Opus. **Class:** build; trial.
- **Found:** `lower/` gains EVALUATE and sequential files (`rt/SeqFile`: OPEN, WRITE with ADVANCING as
  cobc writes a print line, READ, CLOSE, names bound as the cobc lane binds them); each program gets a
  `main`. `JavaRunner` (test code: `oracle/` may not import `lower/`) is ModuleRun's other runner, as
  LANE-09 planned: parse, IR, lower, javac against `rt/`, run in the program's folder. NC, all 95 runs:
  15 report exactly what cobc -std=ibm reports, held as a floor. Two bugs found on the way, both mine:
  `PERFORM A THRU B` arrives as two targets and the lowering read only the first, so NIST's
  `PERFORM BAIL-OUT THRU BAIL-OUT-EX` leaked control into the main line (found by tracing `pc`); and the
  runner read a program's output before its time limit started, so a looping program never timed out.
  The lowering's findings rank what is left: SET (886 uses), SEARCH (141), unary signs in subscripts,
  ALSO in EVALUATE, DIVIDE REMAINDER, ACCEPT, switches. Three programs pass more tests than cobc does
  (NC106A, NC118A, NC119A): to examine, not to celebrate.
- **Resolved:** EVALUATE, files, the Java lane. **Touched:** `lower/`, `rt/`, `test/katana/lower/`,
  `docs/checks.json`, TODO.

### J-116 Phase 3.3: SET, REMAINDER, VARYING AFTER, inline loops; NC 22 of 95
- **Who:** Opus. **Class:** build; trial.
- **Found:** A differential of the lowering's own (`LowerDifferentialIntegrationTest`: small programs
  written for it, each run by cobc -std=ibm and through the Java lane) found what NC's counts hid. An
  inline PERFORM keeps its loop control (UNTIL, TIMES, VARYING, AFTER) in the inline branch's
  condition, not in the operands, so every inline loop had been running its body once; `Performs` now
  reads both places, and VARYING with AFTER is nested loops (LR pp. 420-423). EVALUATE's single WHEN
  is not wrapped in a phrase, and a THRU follows its value as a separate part; both read now. An
  ALTER's operands all carry `alter-proceed-to`, so an altered GO TO went where it was written. SET
  (TO, UP BY, DOWN BY, a condition name TO TRUE), DIVIDE REMAINDER (the dividend less the truncated
  quotient times the divisor, LR p. 336) and unary signs lowered. NC: 22 of 95 runs equal cobc's, the
  new floor.
- **Resolved:** the above. **Touched:** `lower/`, `test/katana/lower/`, `docs/checks.json`, TODO.

### J-117 Phase 3's five waiting decisions, answered at Opus's report
- **Who:** Opus's defaults (J-109, J-113, J-114); Fable decided. **Class:** decision.
- **Found:** five defaults taken under R-053 in 3.1–3.3, each reversible, each marked, none stopping
  the phase: a DISPSIGN profile field, the initial value of an item without VALUE, PERFORM return's
  status, R-021's case granularity, and the scope of 3.1's hand-lowered check.
- **Resolved:** all five held as taken. `ibm.dispsign` and `ibm.wsclear` worded in R-049; R-057
  recorded (INITIALIZE's values, the proof report's header honest until phase 5's finding); R-021
  amended (stretches; C-1 verified on cobc, hypothesis on IBM; C-3 the exit-pass finding). The
  decimal-comma lexer bug may change NIST goldens under a named condition.
- **Note:** R-053's count for phase 3 so far: 5 held, 0 reversed, 0 stopped. The defaults were
  right because each was taken to keep the oracle agreeing and the disagreement written down,
  which is the rule the phase runs on.

### J-118 Fable's five answers carried out: profile fields, R-057's quotes, NE-07, 3.1's findings
- **Who:** Opus. **Class:** build; decision carried out.
- **Found:** `ibm.dispsign` and `ibm.wsclear` added to `profile/` (both profiles state them, PROFILE-02)
  and to `docs/PROFILE.md` in R-049's wording; the profiles' hashes change (ibm `feca2565…` to
  `a5298bfd…`, cobol85 `f9e218d2…` to `e13d3580…`), so the ten fixtures pinning ibm's are re-pinned and
  the lane golden and proof reports re-recorded: every changed line is a hash. R-057's C-1 quoted with
  its pages and `verified` (LR 6.4 pp. 103, 245); C-2 is not borne out for z/OS (WSCLEAR is AIX's, PG
  p. 864; HANDOFF). The same LR page gives a rule the lowering broke: a VALUE is placed independent of
  JUSTIFIED; `lower/Values` now places it left. R-021's C-3 is `NE-07`, "GO TO out of a PERFORM range",
  raised from the paragraph graph (`sir/check/NonExact`; 76 in NIST, 4 in CardDemo). 3.1's five
  testsuite differences are findings against its expectation, by case number, in
  `AutotestRtIntegrationTest`, which now fails on any other case that fails the testsuite. The
  lowering's `// Unsure` for initial values is `// Decision: R-057`.
- **Resolved:** the five answers. **Open:** R-057 C-2 (Fable). **Touched:** `profile/`, fixtures and
  goldens (hashes), `findings/`, `sir/check/`, `lower/`, `test/`, PROFILE, FINDINGS, research, HANDOFF.

### J-119 The decimal comma: a bug fix, no golden line changed
- **Who:** Opus. **Class:** fix.
- **Found:** The lexer took a comma as a numeric literal's decimal point in every program (J-114).
  Now only under DECIMAL-POINT IS COMMA (`front/Parse` sets a lexer flag from the program's text; the
  grammar's NUMERICLITERAL guards the comma with it). Without the clause, `DISPLAY 1,1,1` no longer
  parses: IBM's separator comma is followed by a space, so the program is not conforming and is refused
  rather than read as 1.1 (run_misc-23, a GnuCOBOL extension, leaves the lowered set; its hand-lowering
  and 3.1's 24/19 stand). Fable's condition (J-117) was that every changed golden line be a comma
  literal: none changed. The front, SIR and NIST goldens are as they were, and NC through the Java lane
  stays 22 of 95. `DecimalCommaTest` holds both readings.
- **Resolved:** the bug. **Touched:** `front/`, `test/katana/sir/`.

### J-120 ibm.wsinit, LE's STORAGE quoted, and HY-01 said apart from the findings
- **Who:** Opus. **Class:** decision carried out.
- **Found:** The field is renamed as Fable worded it (8a5d6de): `ibm.wsinit`, `NONE` or two hex digits,
  in `profile/`, both profiles, the tests and PROFILE's rows; the hashes change again (ibm `a5298bfd…`
  to `25d21d9d…`, cobol85 `e13d3580…` to `8f97cfc0…`), fixtures re-pinned, the lane golden and proof
  reports re-recorded. R-057's restated C-2 is quoted: LE Programming Reference z/OS 3.1 (SA38-0683-60)
  pp. 69-70, STORAGE's heap_alloc_value initializes "every byte of the allocated heap storage", NONE the
  default; and the COBOL Programming Guide 6.4 p. 40, a 6.1 or later RENT program's WORKING-STORAGE is
  "allocated from heap storage". So C-2 is `verified` for RENT programs; NORENT is not covered by those
  pages and stays `hypothesis`. NE-07 is now HY-01, a class of its own (`Finding.Kind.hypothesis()`):
  FINDINGS.md has a "Valid under hypothesis" table, the IR golden a `valid-under-hypothesis` line (76 in
  NIST, 4 in CardDemo, unchanged), and the proof report two sections, Findings and Valid under
  hypothesis, from its job's programs' IR, plus R-057's line in its header.
- **Resolved:** Fable's 8a5d6de items. **Touched:** `profile/`, `findings/`, `sir/check/`, `oracle/proof/`,
  fixtures and goldens, FINDINGS, PROFILE, research, `theGhost.md` (R-057 C-2's status, with its quotes).

### J-121 The discoveries register: a defect proven in another tool gets a number and its proof
- **Who:** Ghost asked; Fable recorded R-058. **Class:** decision / direction.
- **Found:** phase 3's differential tests had proven over a dozen departures of GnuCOBOL's ibm
  dialect from IBM's text, each recorded in a journal entry and excused in a test's comment, none
  with a number, none in one place an evaluator could read.
- **Resolved:** R-058; `docs/DISCOVERIES.md` seeded with eleven rows (one withdrawn: the decimal
  comma was Katana's own bug); the glossary's "discovery"; Opus backfills and adds the doc test.
- **Note:** the same facts as anecdotes are nothing; with fixed fields and a test, they are a result
  of the research, which is what Ghost asked for: "this is what the research project has already
  discovered, and this is why this existing tool sometimes has bad output."

### J-122 SEARCH, NEXT SENTENCE, ACCEPT; NC 22 to 26; two findings against cobc
- **Who:** Opus. **Class:** build (3.3/3.4), with an IR change and two findings against cobc.
- **Found:** SEARCH and SEARCH ALL lowered (`lower/Search`) as stretches: the loop head takes AT END
  once the index passes the last occurrence (the ODO object's value when there is one), each WHEN tried
  in order, else the index and a VARYING item that is not the table's own index up by one (LR 6.4
  pp. 436-437); SEARCH ALL as a scan from the first occurrence, which IBM's text allows (its results
  are "predictable only" for ordered, unique keys, p. 438; an `Unsure` names the temporary of the key's
  USAGE the argument is moved to). `Flow.splits` counts SEARCH, so a construct holding one is
  flattened. NC's SEARCH (141) then stopped at `WHEN c NEXT SENTENCE`, and the same probe showed
  `IF c NEXT SENTENCE ELSE` lowered silently as nothing: the IR folded a phrase's NEXT SENTENCE words
  into the branch condition, which IF's lowering never read. Fixed in the IR: a `Paragraph` carries
  where each sentence starts (SIR-06, SEMANTIC-IR), and a phrase's NEXT SENTENCE is a NEXT SENTENCE
  statement, as one written alone is; the lowering ends a sentence holding one at a label, the last
  sentence's target the paragraph's end. IF now refuses a phrase with words left over, so such a fold
  is a finding, never dropped. Contract facts: CardDemo +1, NIST +120, each a phrase's NEXT SENTENCE
  now a statement (`sir/ir.golden`). ACCEPT lowered: format 1 from the input device (`rt/Sysin`,
  records read into the item's bytes until filled, concatenated, padded only when EOF follows data,
  pp. 307-308), format 2's six conceptual items (`rt/Accept`, held to IBM's examples, p. 310).
  NC through the Java lane: 22 to 26 of 95 (floor raised).
- **Findings against cobc** (Katana follows IBM; each held in `LowerDifferentialIntegrationTest` to
  output worked from the page, cobc still differing): SEARCH VARYING an item that is not the table's
  index, which IBM steps "by the same amount as the search index-name and at the same time" (p. 437)
  and cobc sets to the search index's occurrence number (SEARCHVARY); ACCEPT at end of input, where
  IBM leaves the item "unchanged" (p. 308) and cobc fills it with spaces (ACCEPTEOF).
- **Next, by NC's count:** MOVE CORRESPONDING (16), switch conditions (32), `**` (22), class
  conditions (14), conditions on a bare field.

### J-123 MOVE CORRESPONDING and UPSI switches; NC 26 to 29
- **Who:** Opus. **Class:** build (3.3/3.4), with an IR change and a grammar fix.
- **Found:** MOVE CORRESPONDING lowered from the data map (`lower/Corresponding`, LR 6.4 p. 295): both
  groups' children walked in step, matched by name, looked into while both are groups, paired once
  one is elementary; FILLER, 66s, indexes, pointers and items described with REDEFINES or OCCURS left
  out (an `Unsure`: their subordinates too, which IBM does not name); each pair subscripted as the
  groups named are. cobc agrees on every pair of CORRS. Switch-status conditions (p. 283): the IR's
  header now lists each SPECIAL-NAMES condition-name with its switch and status (`Header.Switch`, a
  contract kind `switch`, SEMANTIC-IR), and the lowering tests its UPSI bit; `rt/Upsi` takes LE's UPSI
  run-time option as written (z/OS 3.1 LE Programming Reference SA38-0683-60 p. 87: eight bits, "the
  leftmost n representing the first switch", 00000000 the default), the generated `main` reads it from
  `katana.upsi`, and `JavaRunner` gives the lane's COB_SWITCH_n as those bits. A switch not UPSI-0 to
  UPSI-7 is not lowered. The grammar refused IBM's own `UPSI-0 ON STATUS IS ...` (the mnemonic-name is
  optional, p. 124); it now takes it. An unsupported condition is `(boolean) Unsupported.fail(...)`,
  so the line it stands in compiles under `!` too. NC through the Java lane: 26 to 29 (floor raised).
- **Gate:** J-122's own gate failed on `LoweredAutotestsIntegrationTest`, which built each lowered
  class through the old constructor; J-122 was not pushed, and goes in this commit, the test building
  the class as `main` does, through one helper.

### J-124 Exponentiation, and an abbreviated relation's data-item object
- **Who:** Opus. **Class:** build (3.3/3.4), with a builder fix and a finding against cobc.
- **Found:** `**` lowered for an integral exponent (`Decimal.power`, `ArithProfile.power`, PG 6.4
  pp. 796-797): an integral literal carries d1 * |n| decimal and i1 * |n| integer places (a literal
  base, the integers of its power) while under the cap, or at a 30-digit cap with an odd-digit base;
  a data-name or variable exponent carries cap - dmax and dmax; a zero exponent gives 1, a negative
  one divides 1 by the preliminary result; exponents left out of dmax (p. 794). An exponent with
  decimal places, or an expression holding ** or / when dmax is above zero, is floating point
  (p. 800) and not lowered. `A = B OR C` with C a data item: the grammar reads C as a condition-name,
  and the builder left it a bare Field; it is now the last relation's subject and operator against C,
  as a literal object already was, the remembered relation reset at each statement's condition. cobc
  agrees on every case of ABBREVS. NC holds at 29: `**` (22) and the bare fields (21) are gone from
  its list, and the programs holding them stop on something else.
- **Finding against cobc:** exponentiation results no reading of IBM's text gives, each exact and
  within its receiver: `10 ** 2 + A * 2` with A 12 is 100 under -std=ibm (124 under the default
  dialect), `(A + 1) ** (N - 1)` with N 3 is 0 (169), and `3 ** -1` into S9(5)V9(6) is 27 under both;
  the same `3 ** -1` ROUNDED printed 5.196152 in one program and 0.333333 in another. Held in POWERIBM
  to the values worked from IBM's rules, cobc still differing. A signed item DISPLAYed raw differs by
  the sign's place (J-109), so the programs show signed results through a numeric-edited item.

### J-125 NC's crashes, one cause at a time; NC 29 to 47
- **Who:** Opus. **Class:** build (3.4), with a grammar fix.
- **Found:** Of NC's runs with no report, most crashed; each crash traced to its statement. Relative
  subscripts: the grammar let `TABLE1-NUM (INDEX1 - 2)` be two subscripts, `INDEX1` and `- 2`, and
  `YEAR-1 (W-1  10)` one, `W-1` offset by 10. IBM's form is a name, + or - "preceded and followed by a
  space", and an integer (LR 6.4 p. 74): the grammar now takes that form, and the builder splits a name
  followed by a bare literal (`W-1 10`, `I -1`) into two subscripts. A first reading took `I -1` as
  relative too; cobc refused the program, and IBM's page settled it for cobc. ALL before a figurative
  constant (`ALL QUOTE`) is the constant. EVALUATE wrote `!Compare...(...) == 0` for NOT, so javac
  refused it; each negation is now parenthesized. DIVIDE ... REMAINDER read the stored quotient after a
  division by zero; IBM leaves quotient and remainder unchanged after a size error in the quotient
  (p. 336), and the remainder is now computed only when the quotient stored cleanly (always after a
  zero divisor). ADD and SUBTRACT CORRESPONDING lowered through `lower/Corresponding`'s numeric pairs.
  A subscripted condition-name kept only each subscript's first word, so `EQUALS-A OF T (IN1 - 5)` lost
  its offset (the old grammar's split had hidden it as a second subscript); the token check caught it
  (NC246A, 10 tokens left) and it now reads subscripts as a data item does.
  `JavaRunner` now names the lowering's own frames when it fails. NC through the Java lane: 29 to 47
  (floor raised); cobc agrees on every case of SUBSCRIPTS and the CORRS additions.
- **Open:** NC238A loops past its table (a step before it differs), NC243A, NC201A; SET of a switch;
  CLASS conditions; the 48 runs that report but differ from cobc's counts.

### J-126 The discoveries register filled, and its doc test (R-058)
- **Who:** Opus. **Class:** decision carried out.
- **Found:** `docs/DISCOVERIES.md` backfilled from J-097 to J-124 and the tests' own case lists: every
  cell filled, sixteen rows (D-011 withdrawn as seeded). Where Fable's seeds and the tests disagreed,
  the rows say what the tests prove: DISPLAY's are three, cobc not overpunching as COMPAT does (D-003,
  the test's mutation), binary items by their field's digits (D-004, 150 of 300, not 5), the zoned sign
  in place (D-005, 5 of 300 and 3 autotests); editing is 41 of 400 cases in five families (D-006).
  Five rows are new: the 1- and 2-digit COMP-5 layout (D-012, J-102), JUSTIFIED truncation (D-013,
  J-103), and this phase's SEARCH VARYING (D-014), ACCEPT at end of input (D-015) and exponentiation
  (D-016). Each authority is quoted, checked word for word against the PDF's text with its page from
  the PDF's footers, in `docs/research/ibm-options.md` §10; one test comment cited p. 296 where the
  sentence is on p. 297, corrected. Every test that lists cobc's departures cites its rows.
  `DiscoveriesDocTest` (in `DocTests`) holds the register to the code: rows in order, each journal
  entry real, each live row's test a file that cites it, the lists it names in that test, its mutation
  a method; every `...FINDINGS` or `...READINGS` list and every comment that says "against cobc" cites
  a D-number that is a row. It rejects nine broken inputs, one per rule.
- **Resolved:** HANDOFF's R-058 item. **Touched:** `docs/DISCOVERIES.md`, `docs/research/ibm-options.md`,
  `test/DiscoveriesDocTest.java`, `test/DocTests.java`, the differential tests' comments.

### J-127 CLASS names, SET of a switch, INITIALIZE REPLACING; NC 47 to 50
- **Who:** Opus. **Class:** build (3.4), with two IR facts and an rt fix.
- **Found:** A CLASS name (LR 6.4 p. 129) is a header fact (`Header.CharClass`, contract kind `class`):
  its literals and THRU ranges, an alphanumeric literal each of its characters, a numeric one the
  character at that ordinal position from 1 in the native collating sequence, a range either way
  round; `Classes.member` tests every byte of the item. SET mnemonic-name TO ON or OFF (format 3) sets
  the switch the header's `Mnemonic` names, `rt/Upsi` now settable. INITIALIZE ... REPLACING (pp.
  351-352): with the phrase, only the items of its categories, each given its category's sender; the
  grammar holds only COBOL 85's phrase, so WITH FILLER, VALUE and DEFAULT do not parse rather than
  being dropped. Its program found two rt bugs, each by cobc and IBM's text agreeing: a VALUE on an
  edited item was MOVEd through the editing, where IBM aligns the literal "according to the
  alphanumeric alignment rules" (p. 245), now `Move.place`; and a figurative constant MOVEd into an
  alphanumeric-edited item filled it raw, where a MOVE edits (` / ` for SPACE into `X/X`). cobc agrees
  on every case of CLASSES, SWITCHES and INITS. NC through the Java lane: 47 to 50 (floor raised);
  of NC's statements only one ALL literal is not lowered. NIST contract facts +14, the new header facts.

### J-128 An 01 that REDEFINES another shares its storage; NC 50 to 65
- **Who:** Opus. **Class:** build (3.4); a lowering bug since J-114.
- **Found:** Reading why NC125A's edited moves failed (`run1`, a scratch runner of one program's
  lowered class): the editing equals cobc's on every value (FLOATS), but its table, an 01 of FILLERs
  with VALUEs and an 01 that REDEFINES it with an OCCURS, read zeros and spaces. The lowering gave a
  record its own storage unless it was a second 01 under an FD, so an 01-level REDEFINES in
  WORKING-STORAGE had bytes of its own: writes through one name never reached the other. IBM: "One or
  more redefinitions of the same storage area are permitted", the redefining item possibly longer (LR
  6.4 p. 226). Such a record now takes the redefined record's storage, sized to the longest. Fifteen
  NC programs build their test tables that way: NC through the Java lane, 50 to 65 (floor raised).
  Neither the differential programs nor the autotests had an 01-level REDEFINES; FLOATS now does.

### J-129 A result stored once in each receiver; overlapping operands are a finding (NE-08); NC 65 to 67
- **Who:** Opus. **Class:** build (3.4); a finding kind; two questions.
- **Found:** NC172A's `DIVIDE D INTO X GIVING Y X ROUNDED Z` failed from its third receiver: the
  lowering recomputed the quotient for each receiver, so once X was stored the rest divided the new X.
  A GIVING form's result, and COMPUTE's, is now computed once, with ROUNDED's place when any receiver
  is ROUNDED (truncating again gives the others the same digits), and stored into each. A probe
  (SHARED) showed cobc's other forms take each receiver in turn with the operands as they then are
  (`ADD A TO A B`, A 2 and B 3, gives B 7), so those stay as they were. IBM calls operands that "share
  part of their storage" unpredictable (LR 6.4 p. 297): two different items over the same bytes are now
  NE-08 (`sir/check/NonExact`, following an 01's REDEFINES to the storage it shares; FINDINGS.md, with
  NE-07's retirement said); one item named twice is a question for Fable with cobc's reading as the
  default (HANDOFF). The same probe found cobc carrying ROUNDED's place into a division feeding an
  addition, where J-099 found it not reaching one feeding a multiplication: a second question
  (HANDOFF), SHARED's COMPUTE written without ROUNDED meanwhile. NC through the Java lane: 65 to 67.

### J-130 Four lowering bugs from NC's reports; NC 67 to 73, every NC statement lowered
- **Who:** Opus. **Class:** build (3.4), with a grammar fix.
- **Found:** Each from one NC program's first failure, read with a scratch runner of its lowered class
  in its lane folder (UPSI-0 on, as the ibm cards ask; the newest folder, which a first version did
  not pick and so disagreed with the lane). NC202A: `MOVE 060820000200 TO` a group moved the literal's
  value, `60820000200`, one place short; a numeric literal now gives a group MOVE, and DISPLAY, its
  characters as written (an `// Unsure`: IBM's text speaks of its value; cobc keeps the zeros). NC252A:
  a 66 RENAMES item without VALUE was given INITIALIZE's zero, over the bytes it renames; a 66 now
  takes no initial value of its own. NC252A again: `MOVE ALL "A"` moved "A" once, because the grammar
  (ProLeap's) read `MOVE ALL?` as a word of the statement, so the literal rule never made `ALL "A"`
  the figurative constant; the statement takes no ALL now. NC109M: `DISPLAY ALL "ABCD"` was not
  lowered; IBM displays "only a single occurrence of that figurative constant" (LR 6.4 p. 333).
  CORRS and REDEFS hold each against cobc. NC through the Java lane: 67 to 73 (floor raised), and the
  list of what NC needs and the lowering lacks is empty. Two NC programs need the program collating
  sequence (NC215A, NC219A), next.

### J-131 The program collating sequence; NC 73 to 75; two findings against cobc
- **Who:** Opus. **Class:** build (3.4), with IR facts and discoveries.
- **Found:** The ALPHABET clause and PROGRAM COLLATING SEQUENCE (LR 6.4 pp. 123, 127-129) are header
  facts (`Header.Alphabet`, the collating alphabet's name; contract kind `alphabet`). The lowering builds
  the class's `co` from them (`lower/Collating`), in the constructor after the codepage, so the VALUEs
  see it: STANDARD-1, STANDARD-2 and ASCII the ASCII order, EBCDIC the EBCDIC order, NATIVE the bytes'
  own, a literal alphabet each literal's characters a position, THRU each character of the range in the
  native set either way round, ALSO one shared position, a numeric literal the ordinal's character,
  figurative members their character, and every unlisted byte after, in native order
  (`Collation.literal`). LOW-VALUE and HIGH-VALUE come from it (`co.low()`, `co.high()`). J-114's
  `// Unsure` that the native order stood in for it is gone. cobc agrees on every comparison of
  ALPHABETS but two, each a finding against cobc held in ALPHAIBM to IBM's reading: a VALUE of
  LOW-VALUE is X'00' under cobc, where IBM's LOW-VALUE is the sequence's lowest character (p. 16; a MOVE
  of LOW-VALUE follows the sequence under cobc too), D-017; and cobc compares two literals in native
  order, where the sequence decides every relation condition (p. 123), D-018. Open: a contained program
  inherits its container's sequence (p. 123); the IR gives each program its own. NC through the Java
  lane: 73 to 75 (floor raised). NIST contract facts +26, the alphabet facts.

### J-132 Where NC passes and cobc does not: cobc's ibm arithmetic drops a place Appendix A keeps (D-019)
- **Who:** Opus. **Class:** discovery.
- **Found:** Nine of NC's runs pass every test through the Java lane where cobc -std=ibm fails some;
  `oracle/nist/IBM-CHANGES.md` puts each down to `-farithmetic-osvs` or `-fno-binary-truncate`, the flags
  the dialect sets to model IBM. Whether IBM fails them too is a question of IBM's text, not of cobc, so
  the first was worked by hand: NC177A's ADD-TEST-F2-11 adds S99, S99V9, S999V999, S99V9(6), -1.1111111,
  +.11111111 and SP(8)9 into S999V9(10); Appendix A carries "(i1 or i2) + 1, whichever is greater"
  integer and "d1 or d2, whichever is greater" decimal places for + (PG 6.4 p. 795), every intermediate
  far under 30 digits, so the sum is exact, 242.4332220110, NIST's expected value. cobc -std=ibm gives
  242.4332220010, its default dialect the exact sum: D-019, held in ADDIBM. The other arithmetic-osvs
  and binary-truncation runs are to be read one by one the same way (TODO); until each is, the
  register says "to be read", not a count.

### J-133 Where NC failed more than cobc: written digits, JUSTIFIED group moves, ODO lengths; NC 75 to 79
- **Who:** Opus. **Class:** build (3.4).
- **Found:** Each from one program's first failure. NC103A and NC237A compared a group with a numeric
  literal written with a leading zero (`012345678912`, `090109`): the comparison used the literal's
  value, J-130's fault in another place; an alphanumeric comparison now takes the literal's characters
  as written. NC105A moved `019823` into an alphanumeric-edited item one digit short: `rt/Literal.number`
  sized the literal by its value; it now keeps the digits as written, and the lowering passes them.
  NC107A moved a group into a JUSTIFIED RIGHT item left-aligned; IBM treats a group move "as though it
  were an alphanumeric-to-alphanumeric elementary move" (LR 6.4 p. 405), so `Move.group` right-aligns
  into a JUSTIFIED receiver. NC247A's seven: a group holding an OCCURS DEPENDING ON table took its
  maximum length everywhere; IBM uses "only that part of the table area that is specified by the
  object", except for a receiver whose object is in the group, which takes the maximum (pp. 204-205).
  `Exprs.field` now gives such a group its current length (`Table.current`), and `Exprs.receiving` the
  maximum when the object is inside, used for MOVE TO, STRING INTO, UNSTRING INTO and DELIMITER IN,
  ACCEPT and READ INTO; IBM's complex ODO shapes keep the maximum, an `// Unsure`. CORRS and ODOS hold
  each against cobc. NC through the Java lane: 75 to 79 (floor raised). NC105A now passes every test,
  where cobc fails two: to be read against IBM's text with the other cobc-only failures (J-132).

### J-134 PERFORM VARYING in IBM's order, and WITH TEST AFTER; NC 79 to 83
- **Who:** Opus. **Class:** build (3.4); a lane fix.
- **Found:** NC201A crashed on a subscript of 0: its `PERFORM ... WITH TEST AFTER VARYING ... AFTER` ran
  as TEST BEFORE, because the lowering of VARYING ignored TEST AFTER. Lowered now: every identifier set
  FROM, the procedures run "at least once, before any condition is tested" (LR 6.4 p. 419), then the
  innermost condition; while false it steps and the procedures run again; once true the next one out is
  tested, stepped when false, and every identifier inside it set FROM again. With the crash gone, two of
  NC201A's TEST BEFORE tests failed on the end state: IBM's inner identifier ends holding its FROM value
  (p. 422), and the lowering reset it only when the inner loop began again; and a third, `AFTER B FROM
  A`, showed the outer identifier steps before the inner one is set FROM. Both orders fixed; VARYAFTER
  holds the TEST AFTER, end-state and FROM-the-outer cases against cobc. `Performs.start` folded into
  the loops (a closing row). The lane compared a run with no report as `1 ok` against the golden's
  `1 ok 0 0 0 0 0`, so three runs neither lane reports on (NC110M, NC302M, NC303M) counted as differing;
  it now writes the golden's form, a run that ended in an exception still differing (NC401M). NC
  through the Java lane: 79 to 83 (floor raised). Twelve differ: ten where only cobc fails, NC401M (a
  TAPE1 the lane does not give it), and NC105A, where cobc fails two.

### J-135 D-019 across NC: three more programs are the same short arithmetic
- **Who:** Opus. **Class:** discovery.
- **Found:** Reading the runs where only cobc fails, by hand from Appendix A (PG 6.4 p. 795), each case
  run under cobc -std=ibm alone (its failing test and values) and reduced to ADDIBM: NC118A's
  SIG-TEST-GF-16 to -18 are NC177A's three ADDs, the same seven addends in three orders, which cobc
  sums three ways (242.4332220010, 242.4000000000, 242.4332211100) where every order is exact,
  242.4332220110; NC250A's IF--TEST-37 compares two exact sums of 123.45, `100 + (TWENTY + 3.4) + .05`
  and `.05 + (100 + TWENTY) + 3.4`, which cobc finds unequal; NC175A's SUB-TEST-F2-6 (NC119A's
  SUB-TEST-GF-14) subtracts 11111, .11111, 333333.333333 and 333.333 from zero into S9(6)V9(6), which
  cobc gives as -333666.777443, dropping the 11111, where the exact -344777.777443 fits. All are
  D-019, the register's row now naming them; ADDIBM holds each to IBM's value, cobc still differing.
  Left to read: NC106A, NC119A's and NC175A's other cases, NC176A (binary truncation, which is the
  profile's TRUNC, not a reading of Appendix A), NC204M, NC205A, NC105A's two, NC211A (a reserved word).

### J-136 cobc's ACCEPT reads one record where IBM fills the item (D-020); the differential test takes input
- **Who:** Opus. **Class:** discovery; a test fix.
- **Found:** NC204M passes through the Java lane where cobc fails three tests in both its dialects. Run
  with its data member, cobc's ACC-TEST-F1-13 ACCEPTs a 200-character item from 80-column cards and
  leaves its last 120 characters as they were, then every later ACCEPT reads the rest of that card.
  IBM reads the system input device "until the receiving area is filled", "each input record ...
  concatenated with the previous", the last cut (LR 6.4 pp. 307-308), which `rt/Sysin` does (J-122) and
  NIST expects: D-020, held in ACCEPTREC. The differential test gave no program any input; a `NAME.in`
  beside a program is now its standard input in both lanes.
