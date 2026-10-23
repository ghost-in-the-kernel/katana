# Workpath: process (a decision, the plan, the journal, the agent's environment, the supply chain)

Pointed to by CLAUDE.md (R-074). A new row for this path goes in this file's table, never in CLAUDE.md.

## Instructions

- A step runs in this order: a research section with its prediction, committed before the build; the build; a
  journal entry (`docs/JOURNAL.md`, R-046); the gate; the merge to `main` (R-040).
- Take every number from `gradle nextId` after `git fetch origin main`; never guess the next R-, J- or §.
- A decision is a row in `theGhost.md`, and the function that holds it cites it (`// Decision: R-xxx`).
- One commit in eight is a tithe (its message starts `Tithe`); the gate refuses a push past sixteen since the
  last (R-008). A tithe changes no behaviour and no golden.
- A long run writes to a file, and you read only its verdict and its failures (R-076): `scripts/prepush.sh --all
  > build/gate.log 2>&1`, then `grep "pre-push check:" build/gate.log`. A sub-agent does the same and reports
  only those lines.
- The generated indexes (`.gitattributes` marks them): `gradle decisions` (`docs/decisions.md`, after a decision),
  `gradle journalIndex` (`docs/journal/INDEX.md`, after an entry), `gradle researchIndex`
  (`docs/research/INDEX.md`, after a research section). A test fails when one is stale.
- `gradle metrics` warns early: a function at 16 to 20 paths, a file at 351 to 400 lines.
- A function added: `gradle kernelMap`. A function moved or removed: a closing row in `docs/KERNEL-MAP.md`
  (`superseded-by:` or `removed:`).

## Rows

| Task | Start here | Contract | Tests |
| --- | --- | --- | --- |
| A module: add one, or change its imports | `docs/modules.json` (its `adding` line) | `docs/ROADMAP.md` §3.3 | `test/ModulesTest.java` |
| A decision, a plan, what to work on | `docs/decisions.md` (the index), `theGhost.md`, `TODO.md` (each item's `Status:`), `docs/HANDOFF.md` | `theGhost.md` | `test/GhostTest.java`, `test/TodoTest.java` |
| Writing down what happened | `docs/JOURNAL.md` (the fields), the newest file in `docs/journal/`, `docs/journal/INDEX.md` | R-046 | `test/JournalTest.java` |
| A word | `docs/GLOSSARY.md` | | `test/GlossaryTest.java` |
| Why a rule is what it is (when it seems arbitrary) | `docs/GHOST-IN-THE-KERNEL.md` | | |
| The agent's environment: metrics, the kernel map | `docs/AGENT.md`, `docs/KERNEL-MAP.md`, `scripts/Metrics.java`, `scripts/KernelMap.java` | `docs/ROADMAP.md` §4.3, §5 | `test/MetricsTest.java`, `test/KernelMapTest.java` |
| The routing itself: CLAUDE.md and these files | `CLAUDE.md`, `docs/paths/` | R-074 | `test/DocsTest.java` |
| An escalation, and its log | `docs/ESCALATIONS.md` | R-073, R-075 | `test/EscalationsTest.java` |
| The corpus | `corpus/MANIFEST.md`, `corpus/fetch.sh`, `corpus/scan.sh` | licenses in the manifest | a file with no license fails |
| The supply chain: a vendored jar, the workflow, the image, an agent or editor config | `third_party/MANIFEST.md`, `.github/workflows/ci.yml`, `Dockerfile` | R-067, CLAUDE.md's binding rules | `test/SupplyChainTest.java` |
