# Katana: for an agent working in this repository

A Java 21 compiler from COBOL batch programs to Java proven byte-identical to the original on real
batch runs, with a provenance map and the equivalence harness. Built as a
compiler: front end → Semantic IR (`sir/`) → literal lowering (`lower/`, a byte-image machine the
oracle holds as golden) → Intent IR (`intent/`) → proven passes (`passes/`) → proof ledger
(`ledger/`). No model runs at conversion time (theGhost.md R-004).

## Where to start: find the task's row

Open the two to five files the row starts at, nothing else yet; read the contract it names; run its
tests before and after. Read further only when the code points there. A row with no files yet points
at the plan.

| Task | Start here | Contract | Tests |
| --- | --- | --- | --- |
| A module of Katana: add one, or change what it may import | `docs/modules.json` (its `adding` line) | `docs/ROADMAP.md` §3.3 | `test/ModulesTest.java` |
| A decision, a plan, what to work on | `theGhost.md`, `TODO.md`, `docs/HANDOFF.md` | `theGhost.md` | `test/GhostTest.java` |
| Writing down what happened (a contradiction, a failure caught, a decision) | `docs/JOURNAL.md` (one entry, its fields) | R-046 | |
| A word | `docs/GLOSSARY.md` | | `test/GlossaryTest.java` |
| Why a rule is what it is (when it seems arbitrary) | `docs/GHOST-IN-THE-KERNEL.md` | | |
| A job fixture: what a lane runs; the CardDemo jobs | `oracle/fixture/Schema.java`, `oracle/fixture/FixtureCheck.java`, `oracle/fixture/carddemo/` | `docs/FIXTURE.md` | `test/katana/oracle/fixture/FixtureTest.java`, `test/katana/oracle/fixture/FixtureDocTest.java` |
| The cobc lane and the determinism gate | `oracle/lane/CobcLane.java`, `oracle/lane/Steps.java`, `oracle/lane/Gate.java`, `oracle/lane/carddemo.golden` | `docs/FIXTURE.md`, R-048 | `test/katana/oracle/lane/CardDemoLaneIntegrationTest.java`, `test/katana/oracle/lane/GateIntegrationTest.java` |
| Katana's DFSORT: a SORT step's cards over its records | `oracle/sort/Dfsort.java`, `oracle/sort/SortCards.java`, `oracle/lane/Sorts.java` | R-010, R-026, R-051; P-11 (SUM, INREC and other cards are built when a fixture needs them) | `test/katana/oracle/sort/SortTest.java` |
| The java lane and the DD contract both lanes keep, record formats (RECFM) included | `docs/LANES.md`, `oracle/lane/JavaPrograms.java`, `oracle/lane/ProgramRunner.java`, `oracle/fixture/trivial/COPY.fixture` | `docs/LANES.md` | `test/katana/oracle/lane/TrivialLanesIntegrationTest.java` |
| The comparator: two datasets field by field | `oracle/compare/Compare.java`, `oracle/compare/Layout.java`, `oracle/compare/Decode.java` | R-011 | `test/katana/oracle/compare/CompareTest.java`, `test/katana/oracle/compare/CompareOmpIntegrationTest.java` |
| The front end: COBOL to a parse tree, COPY and REPLACE, the source map | `front/Parse.java`, `front/Lines.java`, `front/Preprocessor.java`, `front/Mapped.java`; front/grammar (ProLeap's, forked, MIT) | docs/ROADMAP.md 2.1, 2.3 | `test/katana/front/ParseTest.java`, `test/katana/front/FrontCorpusIntegrationTest.java` (`front/corpus.golden`) |
| The Semantic IR: what a program does, as data | `sir/types/Program.java`, `sir/types/Item.java`, `sir/types/Statement.java`, `sir/types/Procedure.java` | `docs/SEMANTIC-IR.md` (SIR-01 onward), R-007 | `test/katana/sir/SirDocTest.java`, `test/katana/sir/SirTypesTest.java` |
| Tree to IR (`sir/build`), its findings (`sir/check`), JSON (`sir/json`) | `sir/build/Build.java`, `sir/build/DataMap.java`, `sir/build/Paragraphs.java`, `sir/build/Values.java` | `cobc -C`'s layout (`sir/datamap.golden`); SIR-01 onward | `test/katana/sir/BuildTest.java`, `test/katana/sir/DataMapIntegrationTest.java`, `test/katana/sir/IrIntegrationTest.java` (`sir/ir.golden`) |
| The proof report, v0 | `oracle/proof/ProofReport.java`, `docs/PROOF.md`, `oracle/proof/POSTTRAN.report.md`, `oracle/lane/Paragraphs.java` | `docs/PROOF.md` (PROOF-01 to 03), R-013 | `test/katana/oracle/proof/ProofReportTest.java`, `test/katana/oracle/lane/ProofIntegrationTest.java` |
| The dialect profile | `profile/Profile.java`, `profile/ibm.profile` | `docs/PROFILE.md` | `test/katana/profile/ProfileTest.java` |
| The literal lowering and the runtime | `lower/`, `rt/` (phase 3) | `docs/research/semantic_traps.md` until `docs/SEMANTIC-IR.md` covers it | the 359 autotests; NIST through the java lane |
| Intent, passes, the ledger | `intent/`, `passes/`, `ledger/` (phases 5–6) | `docs/PROOF.md` | every golden byte-identical after every pass |
| The agent's environment: metrics, the kernel map | `docs/AGENT.md`, `docs/KERNEL-MAP.md`, `scripts/Metrics.java`, `scripts/KernelMap.java` | `docs/ROADMAP.md` §4.3, §5 | `test/MetricsTest.java`, `test/KernelMapTest.java` |
| The corpus | `corpus/MANIFEST.md`, `corpus/fetch.sh`, `corpus/scan.sh` | licenses in the manifest | a file with no license fails |

## Commands

Everything runs in the pinned image (`Dockerfile`, built once): `docker run --rm --network none -u
"$(id -u)" -v "$PWD":/katana katana gradle test --offline` is the fast loop (doc and unit tests).
`gradle metrics` prints `docs/AGENT.md`'s numbers and warnings; `gradle kernelMap` adds new functions
to the kernel map. `gradle integration` runs what needs `cobc` and the corpus (`corpus/fetch.sh`
first): NIST, the CardDemo lane, the parser and the IR over the corpus, each held by a golden
(`-Dkatana.record=true` rewrites them). `scripts/prepush.sh` is the check before a push: a fresh
clone, the suites the change touches and why (`--all` at a phase boundary), quoted in the commit.

## Before calling work done

The fast loop always; `scripts/prepush.sh` before a push. A refactor changes no golden (P-02). A
construct the lowering cannot make exact is a finding, never silent output (P-03). A function that
holds a decision cites it, `// Decision: R-021`. Every eighth commit is a tithe (R-008). A finished
step is merged to `main` at once, after fetching it (R-040).

## Two rules

The maintainer is "Ghost" (GitHub: ghost-in-the-kernel) in every file, comment and commit; commits
are authored as Ghost (`git config user.name Ghost`, `git config user.email
118085043+ghost-in-the-kernel@users.noreply.github.com`; an agent keeps its Co-Authored-By line).
Nothing a customer gave us, nothing a run produced, and nothing under a license that forbids
redistribution ever enters this repository; the repository is private and stays so (R-037).
