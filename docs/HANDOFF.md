# Handoff: what to work on next, and what is waiting on whom

## What to work on next

`TODO.md`, from its first item, in order; what is done is in `docs/DONE.md`. Phases 0 to 2 are done
(2.2 moved into 7.2, R-071); phase 3's items stand open in `TODO.md` with what each has built, each saying
whether it can be done now; phases 4 and 5 are done (4.1 to 4.7, J-172 to J-192; 5.1 to 5.5, J-193 to J-198).
Phase 6's passes 6.1 to 6.7 are built (J-210 to J-224, docs/PASSES.md), 6.8 and 6.9 open. Phase 8A, the
divergence report (R-072), runs after 7.1; Ghost had 8A.1 to 8A.7 built ahead of the chain (J-229 to J-238), each
with what it leaves open in TODO.md; the chain resumes at 3.1. The sections whose work is done, or whose questions were answered, are in
`docs/handoff-archive.md`; what still waits on someone is under "Waiting on Ghost" and "Waiting on a run" at the
end.

## For Opus: the first review file, for Ghost (R-066, 2026-10-17; before 6.1) (items 1 to 4 done: J-203)

(Fable's audit of the finished file, 2026-10-18, before Ghost reads it: 208 facts, 14 programs;
money 13, all 96–99%, every test holds, confirmable; function 175, 467 test verdicts hold, 7 "cannot
tell" (T-F3, paragraphs no fixture enters) and 5 abend paragraphs at 95%, the rest confirmable in bulk
on the tests; enum 20, of which 15 the tests refute: 8 `APPL-RESULT` (stores 8 and 12 unnamed), 6
file-status items (the file system writes 35), `HTML-FIXED-LN` (never read); 2 "could not tell"
(handed outside the corpus); 2 confirmable. Three findings: (1) **enum version 3 is the three tests**:
an item is an enum only if the program stores nothing into it but its named values (T-E1), reads it
(T-E2), and no file or caller outside the program writes it (T-E3); a FILE STATUS item is never one;
measured against the key under R-065's adoption rule. (2) The documentation reading is "none" on 181
of 208: CardDemo documents records and jobs, not items; recorded as R-066 C-1's result, and true of
customers' code. (3) A formatter lowercases the first letter of some reasons ("aDD", "cOMPUTE"); fix
before Ghost reads. The Fable column's authorship is to be stated in the file's header: which session
wrote it; if not Fable, relabel (R-031).)

1. **Every covered item, every batch program.** For each CardDemo batch program, every fact the rules
   infer (version 2 of money and flag per R-065 if measured by then, else version 1, said which), one
   line each in plain language: the item or range by the program's names, the kind, and the evidence
   as what the program does with it and where ("ADDed into ACCT-CURR-BAL in 1500-UPDATE-ACCOUNT; MOVEd
   to a field edited with $ and commas in 3000-PRINT"). No IR numbers, no COBOL terms without a gloss.
2. **Three reading columns**: the rule's confidence; a column left empty for Fable's probability and
   reason; the documentation's: AWS CardDemo's README and the copybook comments, quoted where they
   describe the item, "none" where they do not (the corpus is in `corpus/`; the README's license is in
   the manifest; quote, do not copy files in).
3. **Three answers** per line for Ghost: confirmed / refuted (with what instead) / could not tell.
4. **The file** under `test/katana/intent/`, in the review form `intent/review` reads (R-064), so Ghost's
   answers load as a review on the next build; a doc test holds every line to a fact the rules infer.
5. **Then stop and hand it to Fable** (done, J-203: test/katana/intent/carddemo-batch.review, 208 lines:
   175 function, 20 enum, 13 money, no flag, the batch programs holding none the rule finds) for the probability column; Fable returns it with tests or
   documents named for anything under 100%; Opus adds the structural tests for `Function` facts; then
   Ghost reads it. CBTRN02C first in the file, since the trial read it; 6.1 starts after Ghost's review
   or on his word to go ahead without it.

## For Ghost: the review file is ready to read (R-066; J-204, J-205)

test/katana/intent/carddemo-batch.review, CBTRN02C first: every fact with its evidence in plain words, the
rule's confidence, a Fable advisor's proposal (a proposal, never a confirmation), the tests or documents
named for it, and what CardDemo's authors wrote. Answer on the fact line: `confirmed`, `refuted` with what
it is instead, or `could not tell:` and what you would have needed. Every test the advisor named is built,
its verdict under the fact (holds, refutes, cannot tell, and why; evidence for your answer, never the
answer): 8 enum facts are refuted by T-E1 (APPL-RESULT), 6 by T-E3 (the file statuses), 1 by T-E2
(HTML-FIXED-LN); nothing else is refuted. Still open: D-F1, D-E1, D-E2 are IBM pages the network here
refuses, to quote when reachable; D-M1 and D-M2 to make.

## For Fable: the phase-4 boundary (R-053; the default taken is named)

- **Phase 3's items are open while phase 4 is done.** 3.1 to 3.7 each say what they built (the autotests
  by hand, the dispatcher, the lowering, NIST module by module by floors), but none meets its check as
  written, and 3.5 and 3.6 have no check. Phase 4 was worked on Ghost's word, step by step. Default
  taken: phase 3's items stay open in `TODO.md`, and 5.1 is not started until you say whether phase 3
  closes first (its checks written for 3.5 to 3.7) or carries on beside phase 5.
- **IBM's NODYNAM and cobc's (D-033, R-062 C-1).** The profile's `ibm.dynam NODYNAM` keeps a static copy
  for CALL literal and a dynamic one for CALL identifier, so IC 4 programs differ from cobc; a
  hypothesis until IC201A runs under Enterprise COBOL (Trials). Default taken: NODYNAM, as IBM's text reads.
  (Answered, Fable 2026-10-17: holds.)
- **The z/OS storage facade's layouts (R-063 C-1 to C-3).** The PSA, TCB and TIOT offsets, which DDs the
  TIOT lists, and a POINTER's starting NULL are from recollection of IBM's macros, not read; CREASTMT's
  STEP040 is held to the java lane's own golden. Default taken: kept as hypothesis; the proof report says
  CREASTMT's statements are not compared (R-027).
  (Answered, Fable 2026-10-17: holds; IBM's macro listings (IHAPSA, IKJTCB, IEFTIOT1) are in the z/OS
  Data Areas manuals on ibm.com, readable now, and verifying the three offsets is a tithe item.)
- **A REDEFINES that is a view, not a union (J-193, research §28).** CBTRN02C's TWO-BYTES-ALPHA over a
  binary, and its FILLER over DB2-FORMAT-TS, are the same bytes read two ways, with no tag; the roadmap's
  kinds have no `View`, and the types accept such a pair as a `TaggedUnion`. Default taken: no kind added
  (the kinds are the roadmap's); 5.2's union rule infers one only where a tag is found, and a person
  refutes the rest (R-029).
  (Answered, Fable 2026-10-17: the default holds, and goes one step further at the types. No pass in
  phase 6 consumes a `TaggedUnion` fact (6.3 excludes any item with a REDEFINES, so an untagged
  REDEFINES stays bytes, which is the safe shape), so a `View` kind would be a fact nobody acts on;
  a kind is added when a pass needs it, as a module is (R-043), and not before. Meanwhile a
  `TaggedUnion` with no tag is not merely untrue, it is ill-formed: the subject's tag becomes
  required for that kind (an INT invariant, with the CBTRN02C pair as its failing case), so the types
  refuse what 5.2's rule would never infer and a person never has to refute it. The "View" reading
  is noted in INTENT-IR.md under kinds not yet needed, with that pair as its example. Done: J-194, INT-08.)
- **The selected-suites ceiling (For Opus, J-186): closed by measurement.** Every suite now runs in 8.2 min
  (491 s at cf630c6, after J-189's lexer fix), under the 10 a selected push is held to, so no push can
  select past it. No ceiling raised; the 15-min `--all` ceiling could ratchet to 10 (yours to say).
  (Fable 2026-10-17: yes, 10; R-054 already says a ceiling ratchets down to the best recorded, so
  this needed no decision, only the number written.)
- **The cold fast loop is 31 s against "under 30"** (reported, not held, R-054); 26 at pass 3a. The warm
  loop the gate holds is 15. Default taken: recorded, not raised.

## For Fable: 5.2's first rules, measured (J-195, research §29)

- **Enum takes flags for enums.** enum.disjoint-88s, as the roadmap words it, is right on 110 of 187
  CardDemo items (precision 0.588, recall 0.973): 69 of its 77 errors are yes-or-no pairs (Y/N, 0/1,
  valid/invalid), as disjoint as any enum. A `FLAG` rule (two condition-names, or one and its LOW-VALUE)
  run before it, and enum's version 2 leaving what it takes, is the plain next step. Default taken:
  version 1 as written, its confidence 0.5; no version 2 until you say.
  (Answered, Fable 2026-10-17: version 2, as R-065 words it: a FLAG rule first, enum leaving what flag
  took; adopted only if precision and recall both rise on the key.)
- **Money's evidence finds one item in CardDemo.** money.packed-v99-added needs COMP-3; CardDemo's
  amounts are zoned (S9(10)V99 DISPLAY) and its other COMP-3 ones are export records only MOVEd into, so
  it finds CBSTM03A's WS-TOTAL-AMT and nothing else (recall 0.009). A version 2 taking any V99 item an ADD
  or SUBTRACT stores into would find most of the key's money; the rate DIS-INT-RATE is never ADDed to.
  Default taken: version 1 as written. Is COMP-3 the roadmap's evidence, or its example?
  (Answered, Fable 2026-10-17: its example. R-065: use is the evidence, representation a weight at
  most; a two-place item added to, or moved to a currency-edited picture, is money; a rate is not.
  Version 2 under the same adoption condition.)
- **The key is Opus's reading, for Ghost's review.** `test/katana/intent/carddemo.key` states its scopes
  and the readings it was judged by; one item was judged after a run (J-195 says which and why).
- **`Contract.falls` never matches EXIT PROGRAM.** The IR names it `EXIT` with PROGRAM as an operand, so
  the contract reads it as falling. In a program no CALL reached that is IBM's reading (EXIT PROGRAM is
  passed over); in a subprogram it is not. No golden changes until you say which the contract means.
  (Answered, Fable 2026-10-17: the contract means IBM's reading, which is both: EXIT PROGRAM returns
  when the program was CALLed and falls through when it is the run unit's main program. So
  `Contract.falls` reads EXIT with the PROGRAM operand as a conditional edge, "returns if called",
  and the lowering, which already does this per IBM, is what the contract must describe. The IR
  keeping PROGRAM as an operand is fine; it is not lost, R-017 holds. A golden that changes is a bug
  fix under the J-119 condition: every changed line attributable to an EXIT PROGRAM.)

## Trials (R-052: a question settled by running something; the implementing agent runs it)

- **The recalled DFHAID values** (R-055 C-2): compare DFHPF13–24 and DFHPA1–3 against IBM's CICS
  documentation when reachable, or a second independent copybook; until then `hypothesis`.

Each `hypothesis` claim's trial, as its claim names it (tithe 3a.2, `WayBackTest`; the claims are
unchanged, R-042):

- R-069 C-1 (the guarded exact form equals the literal everywhere): the proof runs after 6.4b, every
  CardDemo job and NIST module. Run (J-219): every one as literal; the claim's state is Ghost's to change.
- R-069 C-2 (the guard takes the step-by-step form on invalid bytes, stores and size errors equal): 6.4's
  property test extended with invalid-byte operands, with the guard-removal mutation. Run (J-219,
  `ExactArithmeticTest`): the guard refuses a non-digit, a bad sign, a packed spare nibble and a binary past
  its digits, the guarded form storing the step-by-step value; without the guard the exact form stores
  otherwise. The claim's state is Ghost's to change.
- R-066 C-1 (the three readings agree on most of CardDemo's batch facts): the first review file, its
  three columns filled, the agreements counted in research §29.
- R-066 C-2 (a reviewer who does not read COBOL can judge most facts from the evidence line): Ghost's
  review of CBTRN02C, the "could not tell" count recorded as the review file's usability result.
- R-015 C-1 (IBM's CICS and SQL syntax documented well enough to parse from): phase 7.2's first step.
- R-017 C-3 (the no-token test suffices for completeness): phase 3.3's lowering of NIST without
  consulting the tree.
- R-020 C-1 (BigInteger and scale reproduce libcob on the autotests): the 378 runnable autotests through
  the java lane.
- R-020 C-6 (the cap rows past 30 digits): a mainframe run, which settles C-6 and the ROUNDED `// Unsure`
  together.
- R-060 C-2 (past a short variable-length record the area keeps what it held): VARTRIAL steps 7 and 8
  run under Enterprise COBOL, with the IBM-run `// Unsure` lines (Waiting on a run).
- R-062 C-1 (IBM's NODYNAM keeps two copies of a program a CALL literal and a CALL identifier both reach):
  NIST IC201A run under Enterprise COBOL with its default options; under the claim it reports 1 of 11, as
  the Java lane does (D-033).
- R-022 C-2 (STD is what most OPT programs mean): a customer's mainframe gold on an OPT program.
- R-023 C-2 (IEEE close enough for NIST's float tests): NIST's float cases through the java lane.
- R-024 C-2 (passes as transformations the emitter prints): phase 6's first pass.
- R-063 C-1 (the PSA, TCB and TIOT layouts): IBM's MVS Data Areas (IHAPSA, IKJTCB, IEFTIOT1) read where
  they can be reached, or CBSTM03A's STEP040 run on z/OS.
- R-063 C-2 (which DDs the TIOT lists, and with what UCB): the same z/OS run, whose stdout names the DDs as
  the TIOT holds them.
- R-063 C-3 (a POINTER without VALUE starts NULL): the same z/OS run, or LE's STORAGE option text.
- R-052 C-2 (a trial is cheaper than a debate): trials per phase against Fable minutes per phase, from
  the journal.
- R-053 C-1 (most deferred questions decided the way the agent leaned): the boundary tithe's count of
  held and reversed defaults.
- R-053 C-2 (a reversible default exists for most class (c) and (d) questions): the same count, with
  the stops recorded.
- R-053 C-3 (switching a default under goldens is cheap): commits and minutes per reversal, journaled.
- R-054 C-2 (the docs ratio falls as phase 2 lands code): measured at the phase-2 boundary (J-091).
- R-056 C-2 (one declared mutation per check is enough): the meta-test's first run over the checks
  (tithe pass 2, A7, J-088).
- R-057 C-2, its NORENT part (a NORENT program's WORKING-STORAGE): the claim names no trial; an IBM page
  covering NORENT storage, or a mainframe run of a NORENT program, would settle it (Opus, tithe 3a.2).
- R-058 C-2 (the register matters to an evaluator more than pass counts): the NSF study's reviewers.
- R-059 C-2 (for MULTIPLY and DIVIDE the temporary is the multiplier or divisor): a mainframe run, or a
  further IBM example.

(run: R-065 C-1 and C-2 J-202, RECFM=V J-062, stderr J-059, NC124A J-072)

## Waiting on Ghost

- **Phase 8A's two that are Ghost's (J-237, R-072):** opensourcecobol4j's runtime (`libcobj`, LGPL) as an approved
  dependency, run as a process only, so 8A.1's first real port, oc4j's CardDemo Java, can run; and the first divergence
  report read outside the project (8.6's rule), under NDA (R-037), since its fix shows the lowering's shape.
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

- **The `// Unsure:` lines an IBM run settles** (tithe 3a.4, J-158): each line says so after "Stays:".
  A mainframe session would take them together: ADD/MULTIPLY/DIVIDE's temporary (`lower/ArithVerbs`,
  R-059 C-2); CORRESPONDING under REDEFINES or OCCURS (`lower/Corresponding`); an index's bytes
  (`lower/Lower`); a numeric literal into a group MOVE and in DISPLAY (`lower/Exprs`, `lower/Verbs`);
  an operand inside a divisor and dmax (`lower/Arith`, `test/.../Calc`); UNSTRING over adjacent
  different delimiters (`rt/Strings`); a negative value edited to zero (`rt/Editor`); INSPECT with
  BEFORE and AFTER (`rt/Inspect`); a division by zero without SIZE ERROR (`rt/Arithmetic`); ROUNDED's
  place and the cap (`rt/ArithProfile`, R-020 C-2 and C-6); LOW-VALUE under a shared lowest position
  (`rt/Collation`). The others wait on a source or a fixture, as their lines say. The same session
  takes R-060 C-2: VARTRIAL steps 7 and 8, the record area past a short variable-length record.

- **CI is out of GitHub Actions minutes until 2026-11-02** (R-050). Until then a step's gate is the CI
  image and commands on a fresh clone before the push: `scripts/prepush.sh` (the suites the change
  touches, and why; `--all` at a phase boundary), its result and suites quoted in the commit message. The first commit only checked that way is
  `6701f6f` (1.5a). When minutes return, run the workflow by hand on `main` once.
- NIST under `-std=ibm` (phase 1.2) is recorded (`oracle/nist/IBM-CHANGES.md`); three of its causes
  are what `rt/Decimal` and the profile must model before phase 3.1 freezes the numeric runtime:
  IBM's intermediate precision (`-farithmetic-osvs`, 61 failures), binary items not truncated
  (`-fno-binary-truncate`, 8) and segment numbers ignored (`-fsection-segments=ignore`, 31).
- che4z's CICS and DB2 grammars driven over CardDemo and BankDemo (moved with 2.2 into 7.2, R-071; waits on
  counsel, R-015): generated, never run; about 30 minutes.
