# Handoff: what to work on next, and what is waiting on whom

## What to work on next

`TODO.md`, from its first item, in order; what is done is in `docs/DONE.md`. Phases 0 and 1 are done
(1.5 with one part of its check deferred, below); the boundary's reversals are built. Next: 2.1.

## For Fable (R-053: answered at the phase boundary; the default taken is named)

(phase 2's six from steps 2.1–2.5 answered mid-phase 2026-10-05 while Fable was present: R-055; all
six defaults hold, three roadmap wordings corrected)

## For Ghost (R-053: answered at the phase boundary; a default is named, or "stopped")

- **2.2 reclassified from stopped to deferred** (R-055): che4z out of the tree is the reversible
  default; the graft lands when counsel clears R-015. Ghost's part is unchanged: R-015 to counsel.
- **The ANTLR tool is vendored** (from step 2.1): `third_party/antlr/antlr4-4.13.2-complete.jar`,
  BSD-3 (it bundles ANTLR 3's runtime, StringTemplate 4 and abego TreeLayout, all BSD), checked against
  Maven Central's SHA-1. It runs at build time to generate the parser into build/ and never ships.
  **Default taken:** vendored, listed in `third_party/MANIFEST.md`. Fable's reading: R-001 already
  vendors dependencies and BSD-3 is compatible with R-002; recommend accept.

## Trials (R-052: a question settled by running something; the implementing agent runs it)

- **NC124A's edited picture `$$,$$$.$$`** (R-055): MOVE 12.34 to it and DISPLAY under the cobc lane;
  the printed text shows whether cobc's scale 0 or Katana's 2 is what cobc actually does; the result
  goes in `oracle/nist/IBM-CHANGES.md`.
- **The recalled DFHAID values** (R-055 C-2): compare DFHPF13–24 and DFHPA1–3 against IBM's CICS
  documentation when reachable, or a second independent copybook; until then `hypothesis`.

(none open; RECFM=V J-062, stderr J-059)

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
- che4z's CICS and DB2 grammars driven over CardDemo and BankDemo (phase 2.2): generated, never run;
  about 30 minutes.
