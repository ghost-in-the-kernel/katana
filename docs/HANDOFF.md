# Handoff: what to work on next, and what is waiting on whom

## What to work on next

`TODO.md`, from its first item, in order; what is done is in `docs/DONE.md`. Phases 0 and 1 are done
(1.5 with one part of its check deferred, below). **This is the phase boundary (R-053):** Fable answers
"For Fable", Ghost "For Ghost"; each reversed default is switched in its own commit under goldens;
the tithe records how many held. Then phase 2.1.

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
- **R-011's text, what the comparator normalizes and refuses** (from step 1.7, J-053). Drafted as built:
  "The comparator normalizes text encoding (each side decoded by its declared encoding), the sign-zone
  scheme of zoned numbers, and framing (RDW, line terminators), always under the copybook, and masks
  only what is declared, listing each mask applied. It never normalizes packed or binary bytes, record
  counts, record order (collation), rounding or truncation. A difference is grouped by field." Open:
  whether a zoned field that decodes to the same value from different sign nibbles (C against F) is the
  same (built: yes, a sign-zone scheme) or a difference. **Default taken:** as built
  (`oracle/compare/Compare.java`).
- **Who reads copybooks for the comparator before phase 2** (from step 1.7). Built: a small reader in
  `oracle/compare/Layout.java`, refusing OCCURS DEPENDING ON, SIGN SEPARATE, floating point and
  pointers, to be replaced by the Semantic IR's data map when phase 2 has it. **Default taken:** that
  reader, marked `// Unsure: R-053 default`.
- **The DD contract** (from step 1.8, J-054): `docs/LANES.md`, LANE-01 to 05, as both lanes keep it now.
  Open: whether a RECFM=V dataset reaches a program with IBM's RDW (built: fixed records only, and the
  cobc lane's GnuCOBOL default for V is not IBM's RDW, `docs/research/oracle.md`), and how the java
  lane reaches an indexed dataset (phase 3). **Default taken:** the contract as written.
- **Who reads paragraph names before phase 2** (from step 1.9). Built: `oracle/lane/Paragraphs.java`
  reads names in area A after PROCEDURE DIVISION, to be replaced by the front end's paragraph graph.
  **Default taken:** that reader, marked `// Unsure: R-053 default`.

## For Ghost (R-053: answered at the phase boundary; a default is named, or "stopped")

- **`docs/PROOF.md`'s wording, and R-013** (from step 1.9, J-056): what a proof is, covers and never
  covers, the text every proof report quotes and a buyer reads. Drafted from R-013's sentence in the
  roadmap (environment configuration, data outside the fixtures, concurrency, I/O performance after a
  storage change, governance), with one line added: paragraphs the fixtures never entered. R-013
  itself is not recorded. **Default taken:** the draft as written; reversing it is a text change and a
  re-record of the two report goldens.
- **The fast-loop metric is measured cold** (from step 1.10, J-057). `gradle metrics --args=--time`
  times a nested `gradle test` that starts its own daemon: 20 s, against a target under 20 (R-008). The
  same loop is 18 s timed alone and 7 s with the daemon warm, which is what an agent pays after an edit;
  the tests themselves take under 4 s. Options: measure warm (the agent's cost); keep cold and trim
  compile and the metrics test; raise the target. **Default taken:** recorded both, the metric left as
  measured, nothing changed.
- **Docs lines per source line arms in phase 2 at 0.45** (from step 1.10). R-042 arms it "when the
  first module carries real code (phase 2)"; `oracle/` has carried real code since 1.1, and the ratio
  is 0.45 against 0.35, so arming it at 2.1 fails the fast loop at once. Options: arm it at 2.1 and cut
  documents to fit; arm it with pass 1's 0.45 as the recorded value to ratchet from; leave it unarmed.
  **Default taken:** unarmed, as R-042 reads, until Ghost says.

(the items below predate R-053)

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
