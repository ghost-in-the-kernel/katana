# Handoff: what to work on next, and what is waiting on whom

## What to work on next

`TODO.md`, from its first item, in order; what is done is in `docs/DONE.md`. Phase 0 and 1.1 to 1.4 are done.

Phases 1.1 to 1.4 are done. Phase 1.5, the DFSORT subset, is built and in part proven:
TRANREPT-STUDY reproduces the frozen sort and report (`a7d14a96…`, `19b7f04f…`), and COMBTRAN and
SORTTEST run every step. It stays open on the two questions below for Ghost (DFSORT record lengths;
the tab width CUSTREC needs), which CREASTMT and PRTCATBL wait on, and on R-010.

## Waiting on Ghost

- **DFSORT, an OUTREC record SORTOUT's LRECL does not hold** (J-043; blocks 1.5's CREASTMT and
  PRTCATBL). CREASTMT's OUTREC builds 328 bytes into a SORTOUT of LRECL 350; PRTCATBL's builds 41
  into 40. What does DFSORT do: pad the short record (with blanks or binary zeros?) and truncate the
  long one, with which return code, or stop? The lane stops both with a finding until IBM's text
  (the DFSORT Application Programming Guide, on SORTOUT LRECL and the PAD/TRUNC options) is read.
- **R-016's tab width against CUSTREC.cpy** (J-044; blocks 1.5's CREASTMT). At 8-column stops every
  one of CUSTREC's 17 tabbed lines reaches column 75, so its PICTURE clauses fall past column 72 and
  CBSTM03A does not compile (`unbalanced parentheses`, `invalid PICTURE character '2'`, as in the
  study); at 4-column stops it compiles. Keep 8 (CREASTMT never runs), take 4 for CardDemo as a
  stated hypothesis, or let a fixture declare its sources' tab width?
- **R-010, the DFSORT implementation** (J-045): measured on TRANREPT-STUDY, both reproduce the
  frozen hashes; the Java sort runs all five CardDemo SORT steps' cards (228 ms for the job), the
  generated COBOL sort two of five (334 ms, no OUTREC, no concatenation). Opus leans to the Java sort.
- **Appendix A of the Enterprise COBOL Programming Guide** ("Intermediate results and arithmetic
  precision"): the one IBM semantic still unverified (R-049, `docs/PROFILE.md`). Blocks phase 3.1.
  Closes when Ghost pastes the topic URL (path-style `ibm.com/docs/en/SS6SG3_6.4.0/pg/ref/<topic>.html`
  URLs are reachable when the id is known) or attaches the PDF (SC27-8714) for a full read.
- **Two more IBM texts to confirm when reachable**, neither blocking: IDCAMS reads columns 2–72
  (DFSMS Access Method Services Commands; the lane applies it, J-035, two secondary sources); the
  Enterprise COBOL rule for tab characters in source (R-016 assumes 8-column stops, as a finding).
- Counsel on R-002's license texts and R-015 (EPL-2.0 grammars), before any copy leaves the repo.
- Counsel on JUnit 5 (EPL-2.0, vendored for tests only, unmodified, never shipped): a lighter
  question than R-015's che4z grammars, which would be modified and shipped inside the compiler. The
  two go to counsel together; neither blocks phase 1 (R-045 notes the distinction).
- The provisional patent application (R-037), before any public disclosure; realistically after
  phase 4, when there is a CardDemo proof to describe.
- R-010 (DFSORT as generated COBOL or Java) after phase 1.5 measures both.

## Waiting on a run

- **CI is out of GitHub Actions minutes until 2026-11-02** (R-050). Until then a step's gate is the CI
  image and commands on a fresh clone before the push: `gradle build`, both NIST steps and the lane
  step, their results quoted in the commit message. The first commit only checked that way is
  `6701f6f` (1.5a). When minutes return, run the workflow by hand on `main` once.
- NIST under `-std=ibm` (phase 1.2) is recorded (`oracle/nist/IBM-CHANGES.md`); three of its causes
  are what `rt/Decimal` and the profile must model before phase 3.1 freezes the numeric runtime:
  IBM's intermediate precision (`-farithmetic-osvs`, 61 failures), binary items not truncated
  (`-fno-binary-truncate`, 8) and segment numbers ignored (`-fsection-segments=ignore`, 31).
- The IBM Enterprise COBOL 6.x manuals (intermediate results appendix, TRUNC, NUMPROC, SYNC, CICS
  translator output): HTTP 403 from the study's container; must be read from an unblocked network
  before phase 3.1 freezes the numeric runtime.
- che4z's CICS and DB2 grammars driven over CardDemo and BankDemo (phase 2.2): generated, never run;
  about 30 minutes.
