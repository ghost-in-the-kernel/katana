# Handoff: what to work on next, and what is waiting on whom

## What to work on next

`TODO.md`, from its first item, in order; what is done is in `docs/DONE.md`. Done so far: 0.1, 0.2,
0.3, 0.4 (fetch script and manifest), 0.5, 0.6.

Remaining, each its own commit:

1. **0.7, tithe pass 0:** the fresh-agent trial ("add a module") on the empty project.

Then phase 1.1: reproduce `Total 426 426 0 0 9610 0 20 118 9748` in CI from `oracle/prototypes/nist/`.

## Waiting on Ghost

- Counsel on R-002's license texts and R-015 (EPL-2.0 grammars), before any copy leaves the repo.
- Whether JUnit 5 (EPL-2.0, vendored in `third_party/junit/` for tests only, never shipped) needs
  counsel under R-015 as the che4z grammars do.
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
