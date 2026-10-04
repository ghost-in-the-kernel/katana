# Handoff: what to work on next, and what is waiting on whom

## What to work on next

`TODO.md`, from its first item, in order; what is done is in `docs/DONE.md`. Phase 0 and 1.1 to 1.3 are done.

Phases 1.1 to 1.3 are done. Next: phase 1.4, the cobc lane; R-009 below should be settled first.

## Waiting on Ghost

- R-009, the fixture schema (docs/FIXTURE.md, built in 1.3; the roadmap records R-009 in phase 1).
  Three choices in it are Ghost's, and 1.4 builds on them: (1) a clock pinned to seconds alone is
  refused by the schema (FIXTURE-03, from 1.3's "with hundredths and zone"), so 1.6's check, a
  seconds-only run that the gate must fail, has to set its clock outside a fixture (J-030); (2)
  TRANREPT's fixture is the JCL as written, dates 2022-01-01 to 2022-07-06, which select none of
  POSTTRAN's transactions; the frozen hashes 1.4 must reproduce came from 2024-03-01 to 2024-03-01
  (J-031), so 1.4 needs either a fixture with the study's dates or new frozen hashes; (3) the
  profile carries no IBM option fields until the manuals are read (J-029).
- Counsel on R-002's license texts and R-015 (EPL-2.0 grammars), before any copy leaves the repo.
- Counsel on JUnit 5 (EPL-2.0, vendored for tests only, unmodified, never shipped): a lighter
  question than R-015's che4z grammars, which would be modified and shipped inside the compiler. The
  two go to counsel together; neither blocks phase 1 (R-045 notes the distinction).
- The provisional patent application (R-037), before any public disclosure; realistically after
  phase 4, when there is a CardDemo proof to describe.
- R-010 (DFSORT as generated COBOL or Java) after phase 1.5 measures both.

## Waiting on a run

- NIST under `-std=ibm` (phase 1.2) is recorded (`oracle/nist/IBM-CHANGES.md`); three of its causes
  are what `rt/Decimal` and the profile must model before phase 3.1 freezes the numeric runtime:
  IBM's intermediate precision (`-farithmetic-osvs`, 61 failures), binary items not truncated
  (`-fno-binary-truncate`, 8) and segment numbers ignored (`-fsection-segments=ignore`, 31).
- The IBM Enterprise COBOL 6.x manuals (intermediate results appendix, TRUNC, NUMPROC, SYNC, CICS
  translator output): HTTP 403 from the study's container; must be read from an unblocked network
  before phase 3.1 freezes the numeric runtime.
- che4z's CICS and DB2 grammars driven over CardDemo and BankDemo (phase 2.2): generated, never run;
  about 30 minutes.
