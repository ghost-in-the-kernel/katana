# Handoff: what to work on next, and what is waiting on whom

## What to work on next

`TODO.md`, from its first item, in order; what is done is in `docs/DONE.md`. Phases 0 and 1 are done
(1.5 with one part of its check deferred, below); the boundary's reversals are built. Next: 2.1.

## For Fable (R-053: answered at the phase boundary; the default taken is named)

- **OMP is 29/30, not 30/30** (from step 2.1, J-063). CBL0007 is the course's lab with a deliberate
  syntax error (an END-IF after the IF's period; the course has the student fix IGYPS2113-E), and cobc
  refuses it too. A clean tree for it would be a parser bug. **Default taken:** the golden holds 29/30
  and names CBL0007's error at its END-IF; the check's 30 should read 29 plus that one.
- **2.3's check names lowered statements, which come in 3.3** (from step 2.3, J-065). **Default
  taken:** the round trip is held now from the parsed text back to the COBOL text, character for
  character, over the whole corpus; 3.3's provenance comment cites an origin, and its check extends
  the same round trip to lowered statements.
- **"An IBM dialect switch mirroring `ibm-strict.conf`"** (2.1's text) would mean reading GnuCOBOL's
  configuration, which R-045 keeps off screen. **Default taken:** no switch in 2.1; the grammar accepts
  the union, and the dialect is the profile's (R-049) when `sir/` needs it.
- **EIB and SQLCA are not synthesized in 2.1** (2.1's text lists them with DFHAID and DFHBMSCA). No
  program COPYs them: CICS's translator inserts DFHEIBLK and SQLCA comes by EXEC SQL INCLUDE, which the
  parser reads as a tagged line. Writing IBM's layouts from memory is risky. **Default taken:** they
  come when `sir/` reads a field of them (2.4), from IBM's published layouts.

## For Ghost (R-053: answered at the phase boundary; a default is named, or "stopped")

- **2.2 is stopped on R-015** (from step 2.2, J-064). Its graft puts che4z's CICS and DB2 grammars
  (EPL-2.0, modified, shipped in the compiler) in `front/`, which R-015 admits only after counsel; no
  reversible default keeps that rule. The measurement 2.2 asks for, run from a clone outside the tree
  with che4z's semantic checks stubbed: CardDemo 220/220 EXEC CICS and 32/32 EXEC SQL, BankDemo
  351/351 and 120/120, the OMP labs' SQL 23/31, the 8 left being the course's `Z#####T` placeholder.
  That is the evidence for R-014's graft. **Stopped:** 2.2 waits on counsel; 2.3 onward continue (none
  needs CICS or SQL parsed: R-005 is batch first).
- **The ANTLR tool is vendored** (from step 2.1): `third_party/antlr/antlr4-4.13.2-complete.jar`,
  BSD-3 (it bundles ANTLR 3's runtime, StringTemplate 4 and abego TreeLayout, all BSD), checked against
  Maven Central's SHA-1. It runs at build time to generate the parser into build/ and never ships.
  docs/ROADMAP.md names the runtime only. **Default taken:** vendored, listed in `third_party/MANIFEST.md`.

## Trials (R-052: a question settled by running something; the implementing agent runs it)

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
