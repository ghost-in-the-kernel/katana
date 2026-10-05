# Handoff: what to work on next, and what is waiting on whom

## What to work on next

`TODO.md`, from its first item, in order; what is done is in `docs/DONE.md`. Phases 0 and 1 are done
(1.5 with one part of its check deferred, below). **This is the phase boundary (R-053):** Fable answers
"For Fable", Ghost "For Ghost"; each reversed default is switched in its own commit under goldens;
the tithe records how many held. Then phase 2.1.

## For Fable (R-053: answered at the phase boundary; the default taken is named)

(phase 1's six answered 2026-10-04: R-011, R-012, R-013, R-027; the Layout and Paragraphs readers
hold as defaults until `sir/` replaces them in 2.5, with a test that the replacement agrees with each
on every CardDemo copybook and program before the reader is deleted; LANES.md as written, and RECFM=V
by trial: see "Trials" below)

## For Ghost (R-053: answered at the phase boundary; a default is named, or "stopped")

(phase 1's three answered 2026-10-04: R-013 with two added lines; R-054 warm fast loop and the docs
ratio armed at 0.45)

## Trials (R-052: a question settled by running something; the implementing agent runs it)

- **RECFM=V reaches a program with IBM's RDW** (from LANES.md's open point). Trial: write a V
  dataset through `cobc` under each `COB_VARSEQ_FORMAT` (0..3) and hexdump; the format whose prefix
  is a 2-byte big-endian length (including the 4-byte prefix) and two zero bytes is IBM's RDW; LANE-02
  then gains the V rule and both lanes use that format. If none matches, the cobc lane wraps records
  itself and LANE-02 says so.
- **stderr is deterministic after path normalization** (R-012 C-2). Trial: the four CardDemo fixtures
  through the gate with stderr held; any difference is named and masked by declaration.

## Waiting on Ghost

- **Appendix A of the Enterprise COBOL Programming Guide** ("Intermediate results and arithmetic
  precision"): the one IBM semantic still unverified (R-049, `docs/PROFILE.md`). Blocks phase 3.1.
  Closes when Ghost pastes the topic URL (path-style `ibm.com/docs/en/SS6SG3_6.4.0/pg/ref/<topic>.html`
  URLs are reachable when the id is known) or attaches the PDF (SC27-8714) for a full read.
- **Two more IBM texts to confirm when reachable**, neither blocking: IDCAMS reads columns 2–72
  (DFSMS Access Method Services Commands; the lane applies it, J-035, two secondary sources); the
  Enterprise COBOL rule for tab characters in source (R-016: fixture-declared width, default 8, as a
  finding); DFSORT's IBM-supplied PAD/TRUNC defaults and whether its padding rule applies to the
  OUTREC-reformatted length (R-051 assumes RC0 and yes).
- Counsel on R-002's license texts and R-015 (EPL-2.0 grammars), before any copy leaves the repo.
- Counsel on JUnit 5 (EPL-2.0, vendored for tests only, unmodified, never shipped): a lighter
  question than R-015's che4z grammars, which would be modified and shipped inside the compiler. The
  two go to counsel together; neither blocks phase 1 (R-045 notes the distinction).
- The provisional patent application (R-037), before any public disclosure; realistically after
  phase 4, when there is a CardDemo proof to describe.

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
