# Handoff: what to work on next, and what is waiting on whom

## What to work on next

`TODO.md`, from its first item, in order; what is done is in `docs/DONE.md`. Phase 0 and 1.1 to 1.4 are done.

Phases 1.1 to 1.4 are done. Next: phase 1.5, the DFSORT subset; its check now includes
TRANREPT-STUDY's frozen sort and report hashes and CREASTMT end to end (R-048), which needs the
`CUSTREC.cpy` tab policy (R-016) first.

## Waiting on Ghost

- The profile's IBM option fields (TRUNC, ARITH, NUMPROC, code page, collating sequence; J-029):
  Ghost is consulting Fable. R-009's other two choices are decided (R-009, R-048).
- Counsel on R-002's license texts and R-015 (EPL-2.0 grammars), before any copy leaves the repo.
- Counsel on JUnit 5 (EPL-2.0, vendored for tests only, unmodified, never shipped): a lighter
  question than R-015's che4z grammars, which would be modified and shipped inside the compiler. The
  two go to counsel together; neither blocks phase 1 (R-045 notes the distinction).
- The provisional patent application (R-037), before any public disclosure; realistically after
  phase 4, when there is a CardDemo proof to describe.
- R-010 (DFSORT as generated COBOL or Java) after phase 1.5 measures both.

## Waiting on a run

- IDCAMS's columns: the lane reads columns 2 to 72 and refuses a comment opened in column 1, which
  stops TRANREPT as written at CardDemo's REPROCT member (J-035). Two secondary sources agree; IBM's
  DFSMS Access Method Services manual itself could not be reached (blocked, like the COBOL
  manuals). Confirm from the manual, or a real IDCAMS run, before the lane is sold.
- NIST under `-std=ibm` (phase 1.2) is recorded (`oracle/nist/IBM-CHANGES.md`); three of its causes
  are what `rt/Decimal` and the profile must model before phase 3.1 freezes the numeric runtime:
  IBM's intermediate precision (`-farithmetic-osvs`, 61 failures), binary items not truncated
  (`-fno-binary-truncate`, 8) and segment numbers ignored (`-fsection-segments=ignore`, 31).
- The IBM Enterprise COBOL 6.x manuals (intermediate results appendix, TRUNC, NUMPROC, SYNC, CICS
  translator output): HTTP 403 from the study's container; must be read from an unblocked network
  before phase 3.1 freezes the numeric runtime.
- che4z's CICS and DB2 grammars driven over CardDemo and BankDemo (phase 2.2): generated, never run;
  about 30 minutes.
