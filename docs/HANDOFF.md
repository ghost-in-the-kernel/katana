# Handoff: what to work on next, and what is waiting on whom

## What to work on next

Phase 0 of `docs/ROADMAP.md`, in order. Done in the founding commit: 0.1 (repo, LICENSE, .gitignore),
the seed of 0.2 (`theGhost.md` R-001 to R-008 and R-037, `docs/GLOSSARY.md`, `CLAUDE.md`), 0.4 as a
fetch script and manifest, 0.5 (the prototypes imported under `*/prototypes/`).

Remaining, each its own commit:

1. **0.2, the rest:** `docs/modules.json` with the twelve modules of ROADMAP §3.3 and their import
   rules; `docs/AGENT.md` with the metrics table of §5 at phase-0 values; `TODO.md` (open items only,
   numeric order) and `docs/DONE.md`; `docs/SEMANTIC-IR.md`, `docs/PROFILE.md`, `docs/PROOF.md` as
   headings with their invariant numbering reserved; `docs/KERNEL-MAP.md` empty with its format.
2. **0.2, the doc tests** (§4.1): `ghost.test` (ids, supersession both ways, `subject:`/`conflicts-with:`),
   `glossary.test`, `docs.test` (every path named exists), `modules.test`, `todo.test`. Each must fail
   on a deliberately broken input before it passes.
3. **0.3, the container:** Dockerfile pinning GnuCOBOL 3.1.2 by digest, JDK 21, Gradle; `third_party/`
   with the ANTLR runtime, JUnit and a JSON library vendored with licenses; CI builds the image and
   runs the fast loop with the network off.
4. **0.6, scripts:** the kernel-map generator and the metrics script, ported from Suite Builder's
   `scripts/kernel-scan.mjs` and `kernel-metrics.mjs` to Java (or Gradle tasks).
5. **0.7, tithe pass 0:** the fresh-agent trial ("add a module") on the empty project.

Then phase 1.1: reproduce `Total 426 426 0 0 9610 0 20 118 9748` in CI from `oracle/prototypes/nist/`.

## Waiting on Ghost

- Counsel on R-002's license texts and R-015 (EPL-2.0 grammars), before any copy leaves the repo.
- The provisional patent application (R-037), before any public disclosure; realistically after
  phase 4, when there is a CardDemo proof to describe.
- R-010 (DFSORT as generated COBOL or Java) after phase 1.5 measures both.

## Waiting on a run

- NIST under `-std=ibm` (phase 1.2): never run; the baseline must be recorded before `rt/Decimal` is
  designed.
- The IBM Enterprise COBOL 6.x manuals (intermediate results appendix, TRUNC, NUMPROC, SYNC, CICS
  translator output): HTTP 403 from the study's container; must be read from an unblocked network
  before phase 3.1 freezes the numeric runtime.
- che4z's CICS and DB2 grammars driven over CardDemo and BankDemo (phase 2.2): generated, never run;
  about 30 minutes.
