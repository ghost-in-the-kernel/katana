# Handoff: what to work on next, and what is waiting on whom

## What to work on next

`TODO.md`, from its first item, in order; what is done is in `docs/DONE.md`. Phase 0 and 1.1 to 1.6 are done
(1.5 with one part of its check deferred, below). Next: phase 1.7, the comparator; the phase runs to
its end under R-053.

## For Fable (R-053: answered at the phase boundary; the default taken is named)

- **CBSTM03A reads z/OS control blocks** (from step 1.5, J-050). CREASTMT's last step runs CardDemo's
  CBSTM03A, which sets a pointer to the PSA at address 0, follows it to the TCB and the TIOT, and
  DISPLAYs the job, the step and every DD name before writing a statement; under GnuCOBOL on Linux
  address 0 is unmapped and it ends on SIGSEGV. No cobc run can give it a PSA. Options: (1) CBSTM03A
  is outside the cobc lane, a finding, and its oracle waits for the java lane, whose runtime can stand
  in for the PSA, TCB and TIOT from the fixture's job and DDs (a z/OS storage facade beside the LE stub
  facade); (2) the same facade in the cobc lane, through a shared-memory shim mapped at address 0
  (needs `vm.mmap_min_addr` 0: root, not portable); (3) a fixture-declared source overlay replacing
  lines 266 to 290, which changes the program under test (Opus leans against: the two lanes would
  prove different programs). Lean: (1). **Default taken:** the lane reports CBSTM03A's abend as the
  outcome (`abend SIGSEGV`), the golden holds it, and 1.5's "CREASTMT end to end" waits on this.
- **R-012's text, the double-run gate** (from step 1.6, J-052; the roadmap reserves it for phase 1).
  Drafted as built: "The cobc lane runs a fixture's job twice, each in its own folder, and nothing is
  compared against the original until the two runs are identical on every dataset's logical unload,
  every program step's stdout and every step's outcome; a difference is reported by dataset, the DDs
  that reach it, records and columns. A clock that is not pinned to hundredths and zone is refused
  before the run (R-009); the gate is what catches what a fixture cannot state." Open: whether the gate
  also holds stderr (libcob's messages can carry paths) and SYSOUT DDs. **Default taken:** the gate
  as built (`oracle/lane/Gate.java`), stderr and SYSOUT DDs not held.

## For Ghost (R-053: answered at the phase boundary; a default is named, or "stopped")

(none yet; the items below predate R-053)

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
