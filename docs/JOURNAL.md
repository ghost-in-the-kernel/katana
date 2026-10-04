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
