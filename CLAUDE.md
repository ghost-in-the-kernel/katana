# Katana: for an agent working in this repository

A Java 21 compiler that reads COBOL batch programs and writes Java proven byte-identical to the
original on real batch runs, with a provenance map and the equivalence harness itself. Built as a
compiler: front end → Semantic IR (`sir/`) → literal lowering (`lower/`, a byte-image machine the
oracle holds as golden) → Intent IR (`intent/`) → proven passes (`passes/`) → proof ledger
(`ledger/`). No model runs at conversion time (theGhost.md R-004).

## Where to start: find the task's row

Open the two to five files the row starts at, nothing else yet; read the contract it names; run its
tests before and after. Read further only when the code points there. Rows fill in as the modules
are built (docs/ROADMAP.md says when); a row with no files yet points at the plan.

| Task | Start here | Contract | Tests |
| --- | --- | --- | --- |
| Phase 0: founding the repository | `docs/ROADMAP.md` §6 phase 0, `docs/HANDOFF.md` | `docs/ROADMAP.md` §3.3, §4 | `test/DocTests.java` (`gradle test`) |
| A module of Katana: add one, or change what it may import | `docs/modules.json` (its `adding` line) | `docs/ROADMAP.md` §3.3 | `test/ModulesTest.java` |
| A decision, a plan, what to work on | `theGhost.md`, `TODO.md`, `docs/HANDOFF.md` | `theGhost.md` | `test/GhostTest.java` |
| What happened while the work was done (a contradiction found, a failure caught, a decision taken): write it down | `docs/JOURNAL.md` (one entry, its fields) | R-046 | |
| A word | `docs/GLOSSARY.md` | | `test/GlossaryTest.java` |
| Why the rules are what they are (not read first; read when a rule seems arbitrary) | `docs/GHOST-IN-THE-KERNEL.md` | | |
| The oracle: fixtures, lanes, the gate, the comparator | `oracle/` (phase 1); until then `oracle/prototypes/` | `docs/research/oracle.md` | NIST summary diff empty; CardDemo hashes |
| A job fixture: what a lane runs; the CardDemo jobs | `oracle/fixture/Schema.java`, `oracle/fixture/FixtureCheck.java`, `oracle/fixture/carddemo/` | `docs/FIXTURE.md` | `test/katana/oracle/fixture/FixtureTest.java`, `test/katana/oracle/fixture/FixtureDocTest.java` |
| The cobc lane: a fixture's job run under GnuCOBOL, and the determinism gate | `oracle/lane/CobcLane.java`, `oracle/lane/Steps.java`, `oracle/lane/Gate.java`, `oracle/lane/carddemo.golden` | `docs/FIXTURE.md`, R-048 | `test/katana/oracle/lane/CardDemoLaneIntegrationTest.java`, `test/katana/oracle/lane/GateIntegrationTest.java` |
| Katana's DFSORT: a SORT step's cards over its records | `oracle/sort/Dfsort.java`, `oracle/sort/SortCards.java`, `oracle/lane/Sorts.java` | R-010, R-026, R-051; P-11 (SUM, INREC and other cards are built when a fixture needs them) | `test/katana/oracle/sort/SortTest.java` |
| The java lane and the DD contract both lanes keep | `docs/LANES.md`, `oracle/lane/JavaPrograms.java`, `oracle/lane/ProgramRunner.java`, `oracle/fixture/trivial/COPY.fixture` | `docs/LANES.md` | `test/katana/oracle/lane/TrivialLanesIntegrationTest.java` |
| The comparator: two datasets field by field under a copybook | `oracle/compare/Compare.java`, `oracle/compare/Layout.java`, `oracle/compare/Decode.java` | R-011 | `test/katana/oracle/compare/CompareTest.java`, `test/katana/oracle/compare/CompareOmpIntegrationTest.java` |
| The front end: a COBOL program to a parse tree (reference format, COPY, REPLACE, EXEC) | `front/Parse.java`, `front/Lines.java`, `front/Preprocessor.java`, `front/Copybooks.java`; the grammars in front/grammar (ProLeap's, forked, MIT; `gradle generateGrammar` writes the parser to build/) | docs/ROADMAP.md 2.1 | `test/katana/front/ParseTest.java`, `test/katana/front/FrontCorpusIntegrationTest.java` (`front/corpus.golden`) |
| The proof report, v0 | `oracle/proof/ProofReport.java`, `docs/PROOF.md`, `oracle/proof/POSTTRAN.report.md`, `oracle/lane/Paragraphs.java` | `docs/PROOF.md` (PROOF-01 to 03), R-013 | `test/katana/oracle/proof/ProofReportTest.java`, `test/katana/oracle/lane/ProofIntegrationTest.java` |
| The dialect profile | `profile/Profile.java`, `profile/ibm.profile` | `docs/PROFILE.md` | `test/katana/profile/ProfileTest.java` |
| The front end: grammar, preprocessor, source map | `front/` (phase 2); until then `front/prototypes/` | `docs/research/open_source_tools.md` | parse counts may only rise |
| The Semantic IR | `sir/types`, `sir/build`, `sir/check` (phase 2) | `docs/SEMANTIC-IR.md` | `sir-doc.test`; data map equals `cobc -C` |
| The literal lowering and the runtime | `lower/`, `rt/` (phase 3) | `docs/research/semantic_traps.md` until `docs/SEMANTIC-IR.md` covers it | the 359 autotests; NIST through the java lane |
| Intent, passes, the ledger | `intent/`, `passes/`, `ledger/` (phases 5–6) | `docs/PROOF.md` | every golden byte-identical after every pass |
| The agent's environment: metrics, the kernel map | `docs/AGENT.md`, `docs/KERNEL-MAP.md`, `scripts/Metrics.java`, `scripts/KernelMap.java` | `docs/ROADMAP.md` §4.3, §5 | `test/MetricsTest.java`, `test/KernelMapTest.java` |
| The corpus | `corpus/MANIFEST.md`, `corpus/fetch.sh`, `corpus/scan.sh` | licenses in the manifest | a file with no license fails |

## Commands

Everything runs in the pinned image (`Dockerfile`): `docker build -t katana .` once, then
`docker run --rm --network none -u "$(id -u)" -v "$PWD":/katana katana gradle test --offline`.
`gradle build --offline` builds; `gradle test --offline` is the fast loop (doc and unit tests, under
20 s). `gradle metrics` prints `docs/AGENT.md`'s numbers and the warnings
(`--args=--time` also times the fast loop); `gradle kernelMap` adds new functions to the kernel map.
`gradle integration` runs what needs `cobc` and the corpus (`corpus/fetch.sh nist-cobol85-test-suite`
first): NIST under `-std=cobol85` and `-std=ibm` (`-Dkatana.std=ibm` runs one), printing Katana's summary
(`oracle/nist/RECONCILIATION.md` ties it to the published one, `oracle/nist/IBM-CHANGES.md` says what
the IBM dialect changes), and the CardDemo jobs through the cobc lane (`--tests 'katana.oracle.lane.*'`;
`corpus/fetch.sh carddemo` first). `corpus/fetch.sh` fetches the corpus. The study's prototypes under `*/prototypes/` each
say how they ran.

## Before calling work done

The fast loop and the doc tests always; the integration target when `rt/`, `lower/`, `oracle/` or a
pass changed. A refactor changes no golden (docs/ROADMAP.md P-02). A construct
the lowering cannot make exact is a finding, never silent output (P-03). A function that holds a
decision cites it on the line above, `// Decision: R-021`. Every eighth commit is a tithe (R-008).
A finished step is merged to `main` at once, after fetching it and re-running the tests (R-040).

## Two rules

The maintainer is "Ghost" (GitHub: ghost-in-the-kernel) in every file, comment and commit; commits
are authored as Ghost (`git config user.name Ghost`, `git config user.email
118085043+ghost-in-the-kernel@users.noreply.github.com`; an agent keeps its Co-Authored-By line).
Nothing a customer gave us, nothing a run produced, and nothing under a license that forbids
redistribution ever enters this repository; the repository is private and stays so (R-037).
