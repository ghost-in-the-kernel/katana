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
| Phase 0: founding the repository | `docs/ROADMAP.md` §6 phase 0, `docs/HANDOFF.md` | `docs/ROADMAP.md` §3.3, §4 | the doc tests, once written |
| A module of Katana: its purpose, what it may import | `docs/modules.json` | `docs/ROADMAP.md` §3.3 | `test/modules.test` |
| A decision, a plan, what to work on | `theGhost.md`, `TODO.md`, `docs/HANDOFF.md` | `theGhost.md` | `test/ghost.test` |
| A word | `docs/GLOSSARY.md` | | `test/glossary.test` |
| The oracle: fixtures, lanes, the gate, the comparator | `oracle/` (phase 1); until then `oracle/prototypes/` | `docs/research/oracle.md` | NIST summary diff empty; CardDemo hashes |
| The front end: grammar, preprocessor, source map | `front/` (phase 2); until then `front/prototypes/` | `docs/research/open_source_tools.md` | parse counts may only rise |
| The Semantic IR | `sir/types`, `sir/build`, `sir/check` (phase 2) | `docs/SEMANTIC-IR.md` | `sir-doc.test`; data map equals `cobc -C` |
| The literal lowering and the runtime | `lower/`, `rt/` (phase 3) | `docs/research/semantic_traps.md` until `docs/SEMANTIC-IR.md` covers it | the 359 autotests; NIST through the java lane |
| Intent, passes, the ledger | `intent/`, `passes/`, `ledger/` (phases 5–6) | `docs/PROOF.md` | every golden byte-identical after every pass |
| The agent's environment | `docs/AGENT.md`, `docs/KERNEL-MAP.md` | `docs/ROADMAP.md` §5 | `test/metrics.test` |
| The corpus | `corpus/MANIFEST.md`, `corpus/fetch.sh`, `corpus/scan.sh` | licenses in the manifest | a file with no license fails |

## Commands

To be written in phase 0.3 and 0.6: `./gradlew build` (offline), `./gradlew test` (the fast loop,
under 20 s), `./gradlew integration` (cobc lane, NIST, CardDemo), `./gradlew metrics`,
`corpus/fetch.sh`. Until then, the study's prototypes under `*/prototypes/` each say how they ran.

## Before calling work done

The fast loop and the doc tests always; the integration target when `rt/`, `lower/`, `oracle/` or a
pass changed. A refactor changes no golden (docs/ROADMAP.md P-02). A construct
the lowering cannot make exact is a finding, never silent output (P-03). A function that holds a
decision cites it on the line above, `// Decision: R-021`. Every eighth commit is a tithe (R-008).

## Two rules

The maintainer is "Ghost" (GitHub: ghost-in-the-kernel) in every file, comment and commit; commits
are authored as Ghost (`git config user.name Ghost`, `git config user.email
118085043+ghost-in-the-kernel@users.noreply.github.com`; an agent keeps its Co-Authored-By line).
Nothing a customer gave us, nothing a run produced, and nothing under a license that forbids
redistribution ever enters this repository; the repository is private and stays so (R-037).
