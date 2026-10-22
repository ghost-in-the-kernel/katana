# TODO: the open items, in order

The roadmap as a chain: items are done in this order, never out of it, and an item that cannot be
done as written stops the chain and goes to Ghost. Each item's plan is its step in docs/ROADMAP.md
section 6, by the same number; its check is the roadmap's, copied here. A done item moves to
docs/DONE.md with its number kept. `test/TodoTest.java` holds this file: headings in numeric order, and
the first open item has a check.

## Phase 1: the oracle and the corpus

## Phase 2: the front end and the Semantic IR

### 2.2 Drive che4z's CICS and DB2 grammars

Check: every `EXEC CICS` and `EXEC SQL` in the corpus parses to a command tree or is a finding.

Stopped (R-053, no reversible default): the graft needs che4z's EPL-2.0 grammars in the tree, which
R-015 admits only after counsel. Measured outside the tree (J-064); waiting on Ghost (HANDOFF).

## Phase 3: literal lowering and the runtime, proven on NIST

### 3.1 `rt/` core

Check: the 359 GnuCOBOL autotests, run as unit tests of `rt/` through hand-lowered programs first,
then through the lowering; each tests one rule, which is why they come first.
Its first step (from tithe pass 2, B3): `corpus/fetch.sh gnucobol-autotests`, then
`oracle/autotest/Autotests` over every `.at` file into `corpus/gnucobol-autotests/cases/`, the count of
cases and of those with an expected stdout held by a test, and each runnable case run once under cobc
to settle the reader's one open hypothesis (whether `[[x]]` in an expected stdout means `[x]`).
*Done (J-093):* 1,446 cases in 37 files, 378 runnable, counts held by
`oracle/autotest/autotests.golden`; a first run under plain cobc passes 227 of 378, most failures an
exit status (compile flags the testsuite sets and Katana does not yet know, R-045 forbidding a look
at its configuration). The hypothesis stays open: the two cases it turns on stop before printing.
*Built so far, each held to IBM's text and to cobc by a differential:* arithmetic (J-101), field
byte images (J-102), MOVE (J-103), editing and de-editing (J-104), INSPECT (J-105), STRING and UNSTRING (J-106), comparison and collation (J-107), SIZE ERROR (J-108), DISPLAY (J-109).
*The check by hand (J-110):* `AutotestRtIntegrationTest`, its golden `test/katana/rt/autotest/results.golden`
(hand-lowered and passing counts); the cases `rt/` holds, then the rest through the lowering (HANDOFF).
*Reached by hand (J-112):* 24 of 378, 19 passing the testsuite, all equal to cobc -std=ibm or a finding.

### 3.2 `rt/Dispatcher`

Check: every NIST NC and IC program's PERFORM/GO TO behaviour reproduces its REPORT.
*Built (J-113):* `rt/Dispatcher`, held to cobc -std=ibm by 81 control-flow programs. The check itself
runs NIST through the lowering, so it is met at 3.3-3.4.

### 3.3 `lower/`

Check: the emitted data map equals the IR's; every statement has its comment; the Java compiles with
`javac` against `rt/` only.

*Begun (J-114):* `lower/`, its first increment; `LoweredAutotestsIntegrationTest` lowers the 21 hand-lowered
autotests that parse, each printing what its hand-lowering prints, javac against `rt/` alone, one
provenance comment a statement, the data map the IR's. *Then (J-115):* EVALUATE, sequential files, a Java lane for NIST (`JavaRunner`); NC: 22 of 95 runs (J-116) report
what cobc -std=ibm reports (`NistJavaLaneIntegrationTest`, a floor that only rises). *Next, by NC's
count of what was not lowered:* SEARCH, ACCEPT, switch conditions, MOVE CORRESPONDING. (The decimal comma: fixed, J-119.)
*Then (J-122):* SEARCH, NEXT SENTENCE (sentence starts in the IR), ACCEPT; NC 26 of 95. *Then (J-123):*
MOVE CORRESPONDING, switch-status conditions (UPSI); NC 29. *Then (J-124):* `**` with integral
exponents, abbreviated relations with a data-item object; NC holds at 29 (those programs stop on
something else). *Then (J-125):* NC's crashes, one cause at a time: relative subscripts (the grammar
split `I - 2` and joined `W-1 10`), ALL before a figurative constant, EVALUATE's NOT, REMAINDER after a
size error, ADD and SUBTRACT CORRESPONDING, subscripted condition-names; NC 47. *Then (J-127):* CLASS
names, SET of a switch, INITIALIZE REPLACING, VALUE on edited items; NC 50, every NC statement lowered
but one ALL literal. *Then (J-128):* an 01 that REDEFINES another shares its storage; NC 65. *Then (J-129):* GIVING and COMPUTE results stored once; NE-08; NC 67. *Then (J-130):* numeric literals as written, 66s not initialized, MOVE ALL and DISPLAY ALL; NC 73, every NC statement lowered. *Then (J-131):* the program collating sequence; NC 75. *Then (J-132):* D-019, cobc's ibm arithmetic short of Appendix A (NC177A by hand); the other runs IBM-CHANGES.md gives to arithmetic-osvs or binary-truncation, each worked by hand the same way. *Then (J-133):* numeric literals keep their written digits, a group MOVE into a JUSTIFIED item, OCCURS DEPENDING ON lengths; NC 79. *Then (J-134):* PERFORM VARYING in IBM's order, WITH TEST AFTER; no-report runs compared as the golden writes them; NC 83. *Next:*
NC205A (one arithmetic-osvs case to work by hand) and NC401M (TAPE1); then 3.4's next modules (SQ, IF), and the cobol85 profile's lane. J-139: NC 83 the same, 10 explained by D-019, D-020, D-021 or PROFILE-04 (where Katana fails more, a bug; where cobc fails
more, read against IBM's text).

### 3.4 NIST, module by module, through both lanes

Check: the Java lane reproduces the same PASS/FAIL lines as the 9,610 GnuCOBOL passes, under both
`-std=cobol85` and `-std=ibm` profiles; the per-module pass count is recorded and held by a test
that may only rise (the bench discipline, P-01).

Carried in (J-102): a COMP-5 of 1 or 2 digits is 1 byte under cobc and a halfword on IBM (`rt/`, the
data map), so a record holding one has a different length and layout in the two lanes; such a program is
compared by a finding naming it, until a profile flag or a layout shim reconciles them.
Carried in (R-020 C-5): an all-literal COMPUTE is excluded from the lane comparison by a finding, never
skipped silently: cobc folds it with more places than Appendix A, and a mainframe may yet show IBM folds
too; the lowering applies IBM's table to literals.

### 3.5 Findings, second set

Check: every finding class has a program that raises it (Fable, 2026-10-17, J-199).

### 3.6 Refactoring distance, v0

Check: the distance baseline recorded for CardDemo (Fable, 2026-10-17, J-199).

### 3.7 Tithe pass 3

Check: tithe pass 3's evaluation, as pass 2's was (Fable, 2026-10-17, J-199).

Carried in (Fable, 2026-10-06): a line in GHOST-IN-THE-KERNEL's "check that cannot fail" paragraph,
since J-082's pattern (an old helper still at work where no test looked; a check whose expected side
came from the code under test) has now been met four times (J-082, J-098 twice, J-099).

## Phase 4: CardDemo batch, end to end

### 4.7 Tithe pass 4

Check: none in the roadmap; written here before the item starts. No change of behaviour and no golden
changed (P-02): the largest file well under 400 lines, every main-code function the metrics name as long
and uncommented commented or split, 4.1 to 4.6 moved to docs/DONE.md, HANDOFF's next work and the
phase-4 boundary's open hypotheses current, pass 4 measured in AGENT.md, and the pre-push check over every
suite timed at the boundary (J-192).

## Phase 5: the Intent IR and the proof ledger

### 5.5 Tithe pass 5

Check: none in the roadmap; written here before the item starts. No change of behaviour and no golden
changed (P-02): every main-code function the metrics name as long and uncommented commented; phase 5's new
words in the glossary; R-031 recorded (the roadmap's last phase-5 decision); 5.1 to 5.4 moved to
docs/DONE.md; HANDOFF's next work and the phase-5 boundary's questions current; pass 5 measured in AGENT.md;
the pre-push check over every suite timed at the boundary (J-198).

## Phase 6: proven refactoring passes

Phase 3 runs beside phase 6 and closes on one condition: 3.1's check as the roadmap wrote it, the runnable
autotests through the lowering at scale, which is also R-020 C-1's trial (Fable, 2026-10-17, J-199).

Every pass's check (ROADMAP 6): all phase-3 and phase-4 goldens byte-identical; the
paragraph-entry trace equal; the ledger has a line per changed symbol; the pass's own unit corpus
(the GnuCOBOL autotests that exercise its construct) passes.

### 6.1 Pattern structuring

Check: as above. On the batch programs first: every CardDemo golden byte-identical after the pass, and a
ledger line per range naming its `Function` fact and its state, `unconfirmed` until a review file a person
wrote says otherwise; no figure counts Opus's key or the trial's review edits as confirmed (J-199).

Built (J-210, research §35): `passes/Structuring` (a `Function` range a method its callers call; a GO TO to the
next paragraph left to fall), `passes/StructuringLedger`; on the batch programs all 175 function facts
become methods, every CardDemo job's report and paragraph trace the literal Java's, every NIST module's runs
the literal Java's, the ledger covering the structured build with each method's fact `unconfirmed`. The
third pattern built too (J-211, research §36): a PERFORM VARYING of one phrase, its body structured, its
counter monotone, a `for` (CBSTM03A's two; NIST's 23, 57 kept as stretches, each with why). Open: the pass's
unit corpus, the GnuCOBOL autotests that exercise PERFORM, which waits on phase 3's runnable autotests
through the lowering (the 20 hand-lowered ones pass structured, none holding a range).

### 6.2 Method extraction under the dispatcher

Check: as above.

Built (J-212, research §37): a method range whose paragraphs need no stretch of their own, whose GO TOs go
forward inside it, and which nothing enters but its callers' calls, becomes its method's own code, out of the
switch (a forward GO TO a labeled `break`). CardDemo's batch: all 175 methods, the switches from 986 cases to
30 (the main lines'); NIST: 2,501 of 3,037 methods, 106,713 cases to 50,667; every run as literal. Open: a
backward GO TO (a loop) as a labeled `while`, and the main line itself, both residual with their findings.

Built (J-213, research §38): the main line as `run`'s own code and a GO TO back as a labeled `while`, where the
loops nest and nothing enters the line from elsewhere; a PERFORM of a range no method is `dispatch`. CardDemo's
batch: 11 of 14 programs with no switch, 986 cases to 17 (CBSTM03A's ALTERs, CSUTLDTC's PERFORM over its main
line, CBTRN03C's NEXT SENTENCE); NIST: 46 of 436 main lines, 106,661 cases to 47,896; every run as literal.
Open, Ghost's to decide: a main-line paragraph entered from elsewhere kept in both forms (232 NIST main lines
blocked only by entries, theirs or a loop's), or the paragraphs reordered so such loops nest. Ghost moved on to 6.3
(2026-10-19) with both left open.

### 6.3 Scalar promotion

Check: as above.

Built (J-215, research §39, R-068): a numeric item whose bytes nothing else reads or writes (no REDEFINES,
OCCURS, reference modification, RENAMES, group used whole or with a VALUE, EXTERNAL or GLOBAL record, file,
table or CALL naming it; used only by MOVE, the arithmetic verbs, DISPLAY and conditions; not varied by a
PERFORM) becomes a `long`, or a `BigDecimal` at its scale, every store keeping COBOL's truncation and size error
(Ghost's choice, "exact always"). CardDemo's batch: 40 of 292 numeric items; NIST: 3,844 of 10,470; every
CardDemo job and NIST module as literal, the autotests as before. Moved by Ghost (2026-10-19): the interval rule
for `NeverOverflows` to 6.6 (it needs loop bounds, 6.6's dataflow); tables and items in records used whole to 6.7
(record classes). Built (J-216, research §40): INITIALIZE, SET, STRING, UNSTRING and ACCEPT FROM DATE or TIME
through a field of the item's own, its value taken back, and CALL BY CONTENT; CardDemo 41 promoted, NIST 3,900,
every run as literal. Kept out, each with why: ACCEPT from the input device and CALL BY REFERENCE (their bytes may
be no number), and a statement with ON OVERFLOW or ON EXCEPTION statements.

### 6.4 Exact-arithmetic substitution

Check: as above.

Built (J-217, research §41): ADD, SUBTRACT, MULTIPLY and COMPUTE whose expression is `+ - *` over promoted items,
numeric literals and ZERO, every step within the 30-digit cap, written as plain arithmetic (`long`, or `BigDecimal`
with a decimal operand) into the unchanged store. CardDemo's batch: 64 of 79 arithmetic statements; NIST: 5,659,
3,240 kept step-wise; every CardDemo job and NIST module as literal. Open: an operand that is a record field (2,492
of NIST's kept, 10 of CardDemo's), whose bytes may decode past its PICTURE, which a check that its value fits could
admit; ADD TO, SUBTRACT FROM and MULTIPLY BY with several receivers (R-059's temporary).

Built (J-219, research §43, R-069): 6.4b, a record field in exact arithmetic under a guard, `valid(...)` (the
NUMERIC test and the value within its PICTURE's digits), the exact form when every field passes and the
step-by-step one otherwise. CardDemo's batch: 72 of 79 exact, 8 under the guard; NIST 8,177 exact (2,509 under
the guard), 789 kept; every CardDemo job and NIST module as literal. A native binary or reference-modified field
stays step-by-step.

### 6.5 Enum and flag types

Check: as above.

Built (J-218, research §42, R-070): an Enum or Flag fact gives its item names, not a closed type: each condition
name a predicate method the condition tests call, a promoted item's single values constants a MOVE or SET ... TO
TRUE assigns by name; a Money fact makes a promoted item an `rt` `Money`, every store still COBOL's. CardDemo's
batch: 20 items named (87 predicates, 16 constants), 7 Money, 6 money record fields refused (6.7); NIST: 29 named
(62 predicates), 20 Money; every CardDemo job and NIST module as literal, each new name in the ledger with its
fact's state (all `unconfirmed`). Open: a closed enum, where a pass proves an item holds only its 88 values (PIC X
items promoted first); money in records with 6.7.

### 6.6 Stateless subprograms

Check: as above.

With it (moved from 6.3): the interval rule for `NeverOverflows`, on 6.6's dataflow with loops' bounds, which lets
a promoted item's store be plain arithmetic (R-068).

### 6.7 Record classes

Check: as above.

With it (moved from 6.3): the items 6.3 leaves in bytes because a record holding them is read, written or moved
whole (NIST's largest group, 793 by a group MOVE), and tables (OCCURS) as arrays.

### 6.8 Refactoring distance, per pass and cumulative

Check: as above.

### 6.9 Tithe pass 6

Check: none in the roadmap; written here before the item starts.

Carried in (Fable, 2026-10-17, J-199), phase 6's tithe list in order: parse-once (the 3a list), so the
held times have room; the runnable autotests through the lowering at scale (3.1's check, R-020 C-1); R-063's
three offsets read against IBM's z/OS Data Areas (IHAPSA, IKJTCB, IEFTIOT1). The proof report says in one
line that the ledger is held by the repository's history, not a signature, until 8.3.

Carried in (J-207, R-054 amended 2026-10-18): the Java lane's JVM flags, done (J-208: C1 only and the serial
collector kept, 128 s to 114 s for NIST's Java lane, every report the same; the class-data-sharing archive
refused, its runs failing and slower). Feature work, not tithe, each with its own design note first: the NIST Java
lane in one JVM (rt/'s STOP RUN, files under the run's folder, DD names and UPSI each read from the run,
not the process), and the EVALUATE decision that sends CardDemo's online programs to full LL (CORPT00C,
1,296 tokens of lookahead), the parse trees held byte-identical.

## Phase 7: scale and the facades

### 7.1 CNAF CGMX02

Check: parses with no error nodes; data map built; Java compiles; time and memory recorded.

### 7.2 CICS facade

Check: none in the roadmap; written here before the item starts.

### 7.3 DB2 facade

Check: none in the roadmap; written here before the item starts.

### 7.4 JCL beyond CardDemo

Check: none in the roadmap; written here before the item starts.

### 7.5 The Hercules lane

Check: none in the roadmap; written here before the item starts.

### 7.6 Tithe pass 7

Check: none in the roadmap; written here before the item starts.

## Phase 8: the product surface

### 8.1 CLI

Check: none in the roadmap; written here before the item starts.

### 8.2 The proof report, shipped

Check: none in the roadmap; written here before the item starts.

### 8.3 The provenance map, shipped

Check: none in the roadmap; written here before the item starts.

### 8.4 `pack`

Check: none in the roadmap; written here before the item starts.

### 8.5 The harness as a standalone product

Check: none in the roadmap; written here before the item starts.

### 8.6 Tithe pass 8

Check: none in the roadmap; written here before the item starts.

## Phase 9: credibility and the radar

### 9.0 The provisional patent application is on file

Check: none in the roadmap; written here before the item starts.

### 9.1 Publish the NIST and CardDemo results

Check: none in the roadmap; written here before the item starts.

### 9.2 The regulatory translation

Check: none in the roadmap; written here before the item starts.

### 9.3 Approach the hyperscalers and IBM

Check: none in the roadmap; written here before the item starts.

### 9.4 The first paying shop names its JCL and DFSORT subset

Check: none in the roadmap; written here before the item starts.

### 9.5 A customer's z/OS output reconciles the lanes

Check: none in the roadmap; written here before the item starts.
