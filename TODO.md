# TODO: the open items, in order

The roadmap as a chain: items are done in this order, never out of it, and an item that cannot be
done as written stops the chain and goes to Ghost. Each item's plan is its step in docs/ROADMAP.md
section 6, by the same number; its check is the roadmap's, copied here. A done item moves to
docs/DONE.md with its number kept. `test/TodoTest.java` holds this file: headings in numeric order, and
the first open item has a check.

## Phase 1: the oracle and the corpus

## Phase 2: the front end and the Semantic IR

## Phase 3: literal lowering and the runtime, proven on NIST

### 3.1 `rt/` core

Status: partly built (J-093 to J-112: 24 of 378 autotests by hand; J-246 to J-256: the census through the lowering, J-259: 45 pass under their own flags, 14 more without a flag it does not map); open: the
rest of the runnable autotests through the lowering at scale; can be done now.
Workpath: `docs/paths/lowering.md` (the census); each Next item names its own when it differs.

Check: the 359 GnuCOBOL autotests, run as unit tests of `rt/` through hand-lowered programs first,
then through the lowering; each tests one rule, which is why they come first.
The counts: the check's 359 is the roadmap's count before the cases were extracted. The extraction found 1,446
cases, 378 of them runnable, and 378 is the number every test holds. "Runnable" is defined by
`AutotestCorpusIntegrationTest.runnable` (`test/katana/oracle/autotest/`): a case that compiles a program with
`$COMPILE` and later runs something against a non-empty expected stdout. That test writes the list,
`corpus/gnucobol-autotests/runnable.txt` (gitignored, like the `cases/` beside it), and
`AutotestsLoweredIntegrationTest` reads that list rather than a second reading of the word. The definition stays
in its test: moving it into `oracle/autotest/Autotests` is more than small (the two tests are in different
packages, and `AutotestRtIntegrationTest.expected` is a third reading of the checks), so it is written down here.

Done:
- The reader (J-093): 1,446 cases in 37 files, 378 runnable, counts held by `oracle/autotest/autotests.golden`; a
  first run under plain cobc passes 227 of 378 (most failures an exit status: compile flags the testsuite sets,
  R-045 forbidding a look at its configuration). The `[[x]]` hypothesis was settled by J-269: an argument written out whole
  as `[[x]]` is `x`.
- `rt/`, each held to IBM's text and to cobc by a differential: arithmetic (J-101), field byte images (J-102),
  MOVE (J-103), editing and de-editing (J-104), INSPECT (J-105), STRING and UNSTRING (J-106), comparison and
  collation (J-107), SIZE ERROR (J-108), DISPLAY (J-109).
- By hand (J-110, J-112): `AutotestRtIntegrationTest`, golden `test/katana/rt/autotest/results.golden`; 24 of 378,
  19 passing the testsuite, all equal to cobc -std=ibm or a finding.
- Through the lowering, a census (J-246, research §54): `AutotestsLoweredIntegrationTest`, golden `lowered.golden`
  (a floor on PASS that only rises). Of 378: 36 have no `prog.cob`, 162 do not parse (each with its first error,
  J-248), 6 fail in the IR or the lowering, 4 fail javac, 80 stop at run time on a construct the lowering names, 41
  differ from the testsuite's stdout, 49 pass. J-248: a nested program names its container by the class it is
  written in (5 cases compiled). J-249: the census gives the parser the case's own directory as its copybook path:
  now 153 do not parse, 9 fail in the IR or the lowering, 82 stop at run time, 53 pass. J-251: the `>>` directives that
  decide the text (IF family, DEFINE AS literal, `>>D`) are read (R-081): 146 do not parse, 84 stop at run time, 44 differ, 55 pass.
- The census runs each case under its own compile flags (J-256, R-045 amended): `Autotests` extracts them from the check
  that compiles the program (`Case.flags`, written as `flags.txt`); `CompileFlags` maps `-DNAME[=VALUE]`,
  `-fdebugging-line` (`Settings.debugging`) and `-fixed` to the front end's settings and sets nothing else; a case with a
  flag it does not map ends at `FLAG`, the flags named in the golden. Of 378: 36 no `prog.cob`, 87 FLAG, 99 do not parse,
  7 fail in the IR or the lowering, 3 fail javac, 72 stop at run time, 30 differ, 44 pass (14 of the old 55 passes carry
  a flag not yet mapped, 3 cases that failed pass under their `-D`). Ghost's ruling on the floor: a case with an unmapped flag is still
  run without it; if it passes it ends at `FLAGPASS` (passes without its flag), else at `FLAG`. So of the 378: 36 no
  `prog.cob`, 72 FLAG, 15 FLAGPASS, 99 do not parse, 7 fail in the IR or the lowering, 3 fail javac, 72 stop at run time, 30
  differ, 44 PASS. Two floors in `test/katana/rt/autotest/floors.txt`, both only rising, raised by hand: PASS, and PASS and FLAGPASS together (R-086, J-259; first 44 and 15
  each, then 45 and 59 once R-083 moved a case up); a re-record that lowers either fails the census before it writes the golden. `ExtensionsTest` holds that no fixture or proof path
  sets `Settings.extensions` (R-081 amended).

Open: the 323 that do not pass; the 21 FAILs left after the sorting (J-271), as the Next items below; the 36 with no `prog.cob` (CALL between programs, 3.4); six that begin at PROGRAM-ID with no IDENTIFICATION DIVISION header (`run_misc-190`, `-221`, `run_ml-605`, `-685`, `-716`, `-753`: the case's own text, J-269; whether IBM allows it waits for the Language Reference, R-082); `XML PARSE ... PROCESSING PROCEDURE` (`run_ml-1081`, `-1170`, `-1253`, `-1328`, `-789`, `-856`); and a run that loops
is left to the JVM's exit (a daemon thread, see the `// Unsure:` in the census test).

Built (J-271): the 31 FAIL cases sorted against cobc -std=ibm, each run under its expected check's own command beside Katana's (method and per-case table in J-271); the golden's FAIL lines end with the key (`FailSorting`): 6 D-010 (`d010` 3; `d010-cobc` 3, where cobc's ibm config also differs from IBM's text, D-004/D-005), 1 setting (`run_subscripts-71`, recorded with runtime checks on), 3 harness (the census's run is not the case's command), 21 left. Counts, stages and floors unchanged.
Built (J-273): R-090's step 1: EXIT PERFORM [CYCLE], EXIT PARAGRAPH and EXIT SECTION parse (`Cobol.g4`'s exit statement; `word` predicate, so PARAGRAPH and CYCLE stay names) and are lowered (`lower/Exits.java`); held by `EXITS.cbl` in the lowering differential against cobc -std=ibm; `run_misc-2433` and `run_misc-2410` PASS, the census 52 PASS, 12 FLAGPASS, 30 FAIL, 100 NOPARSE, 5 NOJAVAC, floors 52 and 64 (raised by hand).
Next, first (R-090, J-272), the rest in order: (2) a `$` line in column 7 an error (`front/Lines.java`;
`run_subscripts-506`, and `run_subscripts-437`, a coincidental PASS: the floor lowered by hand with its reason);
(3) a level number outside IBM's set refused (`sir/build/DataMap.java`); (4) ACCEPT FROM and DISPLAY UPON resolved
against SPECIAL-NAMES and IBM's environment-names, else a finding (`lower/Verbs.java`). Each with `FailSorting`, the
golden, the floors and the journal in the same commits. Then, from the sorting, largest first (each case once; `grep FAIL test/katana/rt/autotest/lowered.golden` lists them by key):
1. The census runs the case's own command (13 cases: `harness` 3, `debug-line` 2 and `debug-data` 4 which need `katana.debug=Y` for `COB_SET_DEBUG`, `files` 3 and `env-name` 1 which need their working folder, `DD_` variables and arguments): a process per case in a copy of the case's folder, the expected check's environment (`COB_CURRENT_DATE` in `Now`'s form), arguments and pipe, which also ends the daemon thread `// Unsure:`. `run_file-5918` and `used_binaries-1453` then PASS (two cases up, the floors raised by hand); the rest become differences the census can name.
2. Debugging (6): `debug-line` (DEBUG-ITEM's line: Katana six zero-padded digits and a line at START PROGRAM; cobc blank-padded; needs LR 6.4 p. 19, R-082) and `debug-data` (4, an extension; the lowering already names it, so only the census's switch is missing).
3. Level 78 constants (3: `run_extensions-2831`, `-2854`, `-2880`): read as an item with no PICTURE, DISPLAYed as nothing, no finding (P-03). Either build the constant or name it; the construct is not IBM's, so Ghost's ruling on R-081's extensions first.
4. Variable-length records over text files (3: `run_file-241`, `-5646`, `-5782`): `SeqFile.readVarying` throws an EOFException on a text file (`RECORD VARYING FROM 5 TO 500`), and a `LINE SEQUENTIAL` `ASSIGN TO EXTERNAL` file with `RECORD IS VARYING` ends in status 47 after its OPEN; cause not diagnosed; the first step is item 1's run.
5. P-scaled DISPLAY (3: `data_display-410`, `run_fundamental-8760`, `data_binary-2143`): Katana writes the stored digits, cobc -std=ibm the PICTURE's digits with the P positions, the testsuite the value with a point. Settle IBM's rule against the Language Reference (R-082) and, if cobc's is wrong, a D- row in `docs/DISCOVERIES.md`; `data_binary-2143` has no oracle (cobc -std=ibm rejects its VALUEs).
6. One each: `run_fundamental-72` (a floating-point literal in DISPLAY is written as `123E0`, a silent wrong value; the sign of `+12,3` waits for the Language Reference), `run_fundamental-3100` (overlapping reference-modified MOVE, `-fmove-ibm`: R-088 says `rt/Move` is held to IBM's MOVE and this case says it is not for overlap), `run_extensions-2785` (`ENVIRONMENT-NAME`/`-VALUE`, `ARGUMENT-VALUE`, `ARGUMENT-NUMBER`: written and read as a plain SYSOUT and blanks, no finding), `run_subscripts-506` (`$SET SSRANGE` ignored; the profile has no SSRANGE option; `run_subscripts-71` waits on the same), `run_misc-14909` (spaces in numeric items: IBM undefined under ibm.invdata NO; probably a finding against the case, for Ghost).
Built (J-263): R-087 to R-089 in `CompileFlags` and the census: the census names its profile (`profile/ibm.profile`, TRUNC(STD), asserted); `-fnotrunc`, `-fno-trunc`, `-fno-binary-truncate` stay FLAG with "recorded under TRUNC(BIN); the census runs ibm.profile (STD)", `-fbinary-comp-1` with its permanent reason; `-farithmetic-osvs`, `-fmove-ibm`, `-fodoslide`, `-fcomment-paragraphs=ok`, `-faccept-display-extensions=error` set nothing; `-fsign=ebcdic` and `-fdefault-file-colseq=` wait for the lane's codepage, `-fsign=ascii` is permanent, `-fcomplex-odo` waits for the Language Reference; the acceptance group reads "a form the dialect does not have", and a FLAGPASS under it is listed in the golden as `# grammar leak` (none today). The census is now 65 FLAG, 11 FLAGPASS, 102 do not parse, 4 javac, 74 stop at run time, 31 differ, 48 PASS, floors 48 and 59.  The variably-located-items lowering item, which `-fodoslide`'s four cases check (`lower/Exprs.java`'s `// Unsure:`; two of the four now stop at run time, one does not parse on `ALL`, the fourth is still held by `-fnotrunc`).
Built (J-265, R-088): items after an ODO table are located after its current length (`lower/Slide.java`, `Table.slide`), a group's length the sum of its tables' current ones, LENGTH OF and FUNCTION LENGTH as sending items: `run_extensions-609` and `-739` PASS, `run_extensions-517` FLAG to FLAGPASS (three ODO tables on one object), the census 64 FLAG, 12 FLAGPASS, 72 NORUN, 50 PASS, floors 50 and 62. Left of that item: `run_extensions-1151` (`INITIALIZE ... ALL TO VALUE` does not parse; nothing to do with the slide), `run_file-11361` (held by `-fnotrunc` and EXTFH), what INITIALIZE and a VALUE do to a group holding an ODO table (taken at the maximum, `// Unsure:` in `Exprs.variable`), a table nested in an OCCURS (`-fcomplex-odo`, waits for the Language Reference), and IBM's text for all of it, which is recollection until the manuals are in `corpus/ibm-manuals/` (R-082).
Before (J-256): the 87 that end at `FLAG` or `FLAGPASS`, by the flag (`-std=` 17, `-fnotrunc` and `-fno-trunc` 14, `-free` 6 with
`--free`, `-fprof` 5, `-fsystem-name`, `-fodoslide`, `-fcallfh` 4 each, ...): each flag is mapped when Katana's profile
already means it, a front-end or lowering change when it asks for one, or left a `FLAG` with its reason. A flag is
never mapped to nothing to turn a `FLAG` into a pass. Built (J-259): the three Ghost decided (J-257): `-std=ibm` and
`-std=ibm-strict` mapped, run as is (R-083); a valueless `-D NAME` defined with its value unknown, a value compared
or redefined an error naming the flag, `Settings` carrying the marker (R-084); `-free` left a `FLAG` or `FLAGPASS`,
"a source format the dialect does not have" (R-085). The census is now 70 FLAG, 14 FLAGPASS, 100 do not parse, 4 javac, 45 PASS (`run_fundamental-4746` moved up from FLAGPASS). Built (J-261): a reason for every flag left, in the golden (`CompileFlags.reason`); the listing and diagnostics flags (`-t`, `-fno-theaders`, `-fno-tmessages`, `-fdiagnostics-show-option`) mapped as no-effect: 70 FLAG, 12 FLAGPASS, 100 do not parse, 4 javac, 47 PASS, floors 47 and 59. Next: the dialect decisions the reasons name, for Ghost (binary truncation, 17 cases, first).
Built (J-267, Ghost's go; N-009, N-010): the census golden's format, three problems fixed together in one re-record (stages, counts, floors and the `runnable 378 ...` line unchanged):
a FLAG or FLAGPASS line holds `flag [reason key]` joined by ` | ` (a flag with a space, `run_extensions-2904`, no longer runs into the next); every header line starts with `# `;
each reason is written once in a `# reason <key>: <text>` block at the end, from `CompileFlags.reason` (`reasonKey`, `reasonOfKey` read its table). `theGoldensFlagsReadBackAsTheCasesOwn` reads the flags back and compares them with each case's `flags.txt`.
Taken and not built (J-268, research 61): `ROUNDED MODE` (10 cases) stays an error: IBM's phrase is recollection only (no manual in the sandbox, R-082) and its modes need a sticky digit `rt/` does not carry (`ArithProfile.last`); pinned by `ParseTest`. Census unchanged. It waits for the Language Reference or Ghost's ruling; the causes after it are one-offs.
Then, by count (J-248, J-249, J-251): the 99 that do not parse, whose largest causes are the `>>` directives left (`SET CONSTANT`, `DEFINE ... AS PARAMETER`,
`SOURCE FORMAT`, `TURN`: 6, each an error by R-081; `$SET` out of column 7: 1),
`MODE` after ROUNDED (10+), a PROGRAM-ID where the grammar expects an identification header (7), `procedure` in lower
case or after a different header (5); a copybook not found (7, J-249: `xfhfcd.cpy`, which GnuCOBOL installs and the
corpus does not hold, Ghost's word); the 4 javac failures (3 call `bind1`..`bind3` for a USING the class does not
declare; 1 calls `.value()` on `Unsupported.fail`); the CALL of a nested program that the run unit cannot find
(4 cases: `CALL prog2` from inside `PROG`).
3.2, next in number, is met by 3.4's runs and folded there, R-071.

Can be done now (R-071): the lowering it needs is live. Its check at scale, the runnable autotests through the
lowering, is also every pass's unit corpus (6.1 to 6.7's "Open"), so it comes before 6.8; 6.9 carries it too.

### 3.3 `lower/`

Status: partly built (J-114 to J-139); open: NIST DB205A's ACCEPT defect (J-212); can be done now.
Workpath: `docs/paths/lowering.md`.

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

Can be done now (R-071): what is left is a defect, not an architecture, NIST DB205A's lowering failing at an ACCEPT
(a ClassCastException, Value$Name to Value$Field, J-212).

### 3.4 NIST, module by module, through both lanes

Status: partly built (NIST modules by floors that only rise); open: every module at its full count under both
profiles; can be done now.
Workpath: `docs/paths/oracle.md`.

Check: the Java lane reproduces the same PASS/FAIL lines as the 9,610 GnuCOBOL passes, under both
`-std=cobol85` and `-std=ibm` profiles; the per-module pass count is recorded and held by a test
that may only rise (the bench discipline, P-01).

Carried in (J-102): a COMP-5 of 1 or 2 digits is 1 byte under cobc and a halfword on IBM (`rt/`, the
data map), so a record holding one has a different length and layout in the two lanes; such a program is
compared by a finding naming it, until a profile flag or a layout shim reconciles them.
Carried in (R-020 C-5): an all-literal COMPUTE is excluded from the lane comparison by a finding, never
skipped silently: cobc folds it with more places than Appendix A, and a mainframe may yet show IBM folds
too; the lowering applies IBM's table to literals.

Moved from 3.2 (R-071), its check met here: "every NIST NC and IC program's PERFORM/GO TO behaviour
reproduces its REPORT"; `rt/Dispatcher` built (J-113), held to cobc -std=ibm by 81 control-flow programs.

### 3.5 Findings, second set

Status: open; can be done now.
Workpath: `docs/paths/front.md`.

Check: every finding class has a program that raises it (Fable, 2026-10-17, J-199).

### 3.7 Tithe pass 3

Status: open; can be done now.
Workpath: `docs/paths/process.md`.

Check: tithe pass 3's evaluation, as pass 2's was (Fable, 2026-10-17, J-199).

Carried in (Fable, 2026-10-06): a line in GHOST-IN-THE-KERNEL's "check that cannot fail" paragraph,
since J-082's pattern (an old helper still at work where no test looked; a check whose expected side
came from the code under test) has now been met four times (J-082, J-098 twice, J-099).

## Phase 4: CardDemo batch, end to end

## Phase 5: the Intent IR and the proof ledger

## Phase 6: proven refactoring passes

Phase 3 runs beside phase 6 and closes on one condition: 3.1's check as the roadmap wrote it, the runnable
autotests through the lowering at scale, which is also R-020 C-1's trial (Fable, 2026-10-17, J-199).

Every pass's check (ROADMAP 6): all phase-3 and phase-4 goldens byte-identical; the
paragraph-entry trace equal; the ledger has a line per changed symbol; the pass's own unit corpus
(the GnuCOBOL autotests that exercise its construct) passes.

### 6.1 Pattern structuring

Status: partly built (J-210, J-211); open: its own unit corpus, which waits on 3.1.
Workpath: `docs/paths/passes.md`.

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

Status: partly built (J-212, J-213); open: a main line entered from elsewhere (Ghost's to decide), and its unit
corpus, which waits on 3.1.
Workpath: `docs/paths/passes.md`.

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

Status: partly built (docs/PASSES.md, its row); open: what its body lists, and its unit corpus, which waits on 3.1.
Workpath: `docs/paths/passes.md`.

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

Status: partly built (docs/PASSES.md, its row); open: what its body lists, and its unit corpus, which waits on 3.1.
Workpath: `docs/paths/passes.md`.

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

Status: partly built (docs/PASSES.md, its row); open: the closed enum, which can be done now; money in records,
which waits on 6.7.
Workpath: `docs/paths/passes.md`.

Check: as above.

Built (J-218, research §42, R-070): an Enum or Flag fact gives its item names, not a closed type: each condition
name a predicate method the condition tests call, a promoted item's single values constants a MOVE or SET ... TO
TRUE assigns by name; a Money fact makes a promoted item an `rt` `Money`, every store still COBOL's. CardDemo's
batch: 20 items named (87 predicates, 16 constants), 7 Money, 6 money record fields refused (6.7); NIST: 29 named
(62 predicates), 20 Money; every CardDemo job and NIST module as literal, each new name in the ledger with its
fact's state (all `unconfirmed`). Open: a closed enum, where a pass proves an item holds only its 88 values (PIC X
items promoted first); money in records with 6.7. (R-071: the closed enum can be done now, on 6.6's dataflow;
money in records waits with 6.7's storage other than the bytes.)

### 6.6 Stateless subprograms

Status: partly built (docs/PASSES.md, its row); open: what its body lists, and its unit corpus, which waits on 3.1.
Workpath: `docs/paths/passes.md`.

Check: as above.

Built (J-221, research §44): `passes/Dataflow`, a definite-assignment dataflow over WORKING-STORAGE's bytes. A program
whose every written byte is written before it is read on every path, with no file, EXTERNAL or GLOBAL item, POINTER,
nested program, declaratives or ALTER, is lowered as INITIAL. NIST: 9 of its 23 CALLed subprograms; CardDemo:
none (CSUTLDTC passes CEEDAYS an argument nothing wrote first). Measured, not built: loop bounds would prove at most
196 of NIST's 6,194 promoted stores and none of CardDemo's 67; PICTURE ranges alone would prove 63 and 18.

Built (J-222, research §45, 6.6b): the PICTURE rule, `passes/Fits`. An exact store whose value always fits its one
promoted receiver is a plain assignment: 18 of CardDemo's 72 exact statements, 31 of NIST's 8,177. Loop bounds
are not built (Ghost, 2026-10-22).

Not built (R-071): the interval rule with loops' bounds, moved here from 6.3; J-221 measured it would prove none of
CardDemo's stores, and Ghost built the PICTURE rule alone.

### 6.7 Record classes

Status: partly built (J-223, J-224); open: views over REDEFINES, which can be done now; storage other than the
bytes, which waits on a proof.
Workpath: `docs/paths/passes.md`.

Check: as above.

With it (moved from 6.3): the items 6.3 leaves in bytes because a record holding them is read, written or moved
whole (NIST's largest group, 793 by a group MOVE), and tables (OCCURS) as arrays.

Built (J-223, research §46): record classes as typed views (Ghost's choice), each group 01 of WORKING-STORAGE or a
file that nothing aliases a class over its bytes: named `Field` members, numeric getters, fixed tables' accessors.
CardDemo's batch 143 of 178 group records, NIST 4,787 of 7,205; every run as literal. Built (J-224, research §47,
6.7b): LINKAGE records, each class built where its CALL binds it (CardDemo 3 of 6, NIST 17 of 21). Open: records
with a REDEFINES (768 of NIST's refusals), which can be done now (a view over shared bytes is still exact); and
storage other than the bytes behind the same class, with 6.5's money in records, which waits on a proof that nothing
reaches a record's bytes but its class (no item builds that yet: Ghost's to place, R-071).

### 6.8 Refactoring distance, per pass and cumulative

Status: open; after 3.1 (R-071).
Workpath: `docs/paths/passes.md`.

Check: as above.

Moved from 3.6 (R-071): the literal Java's line count, nesting and dispatcher size per program, the baseline
every pass is measured against, is this item's first column; nothing measured it before the passes it measures.

### 6.9 Tithe pass 6

Status: open; after 6.8.
Workpath: `docs/paths/process.md`.

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

Status: partly built; open: what its body lists.
Workpath: `docs/paths/oracle.md`.

Check: parses with no error nodes; data map built; Java compiles; time and memory recorded.

## Phase 8A: the divergence report

Runs after: 7.1.

Ghost's goal (2026-10-22, R-072): take a finished Java port Katana did not write and the COBOL it came from, and
report each place the Java may diverge: why (the COBOL semantic it misses), the logic traced step by step from the
COBOL statement through the conversion, and a fix where one is possible. Each divergence `proven` (a cited input
record reproduces it, the lanes' bytes shown) or `possible` (static only); none reported without a reproducer.
Dynamic first: static reading proposes where to look, a lane run confirms. The "why" is a catalogue entry and the
fix Katana's own literal Java for the statement with its description, never a patch to theirs; no model runs at
report time (R-004). Advice: docs/research/reviews/2026-10-22-divergence-report-goal.md. Absorbs 8.5.

### 8A.1 The DD adapter and refusal

Status: built ahead (J-229); open: a real port, which waits on Ghost (oc4j's runtime, a dependency).
Workpath: `docs/paths/divergence.md`.

Check: `katana prove --java <cmd>` runs a foreign command as the java lane's program step; one oc4j CardDemo program
(`lower/prototypes/oc4j-carddemo-reference/`, run as a process only, never shipped) or a hand-written port of COPYREC
runs a fixture through the lane and the comparator, and what had to be adapted is recorded; a port that ignores the
`DD_` contract (LANE-01 to 04) is refused with a finding naming what it did not honour.

Trial run ahead of the chain (J-229, research §48, Ghost 2026-10-22): `oracle/prove/Prove`, reached as `gradle prove`,
held three hand-written ports of `ACCRUE` (`oracle/fixture/foreign/`) to the COBOL: the faithful one the SAME, the
HALF_UP one differing in two fields on 2 of 5 records by a cent, and one ignoring `DD_OUTFILE` refused. The oc4j
reference waits on its runtime, a dependency for Ghost to approve. Left for this item: the oc4j or another real port.

### 8A.2 The trap catalogue as data

Status: built ahead (J-230).
Workpath: `docs/paths/divergence.md`.

Check: divergence kinds `DV-01` onward, from `docs/research/semantic_traps.md`, each with IBM's text, the COBOL rule,
the wrong Java shape, Katana's exact Java and a generator, held by a doc test; every kind's generator gives a COBOL
program and inputs on which its wrong shape differs from cobc. The first three: high-order truncation, scale
truncation against HALF_UP, and dmax division, each caught by a mutation, the catch rate predicted first.

Begun ahead of the chain (J-230, research §49): `findings/Divergence` and `docs/DIVERGENCES.md` hold DV-01 to DV-03,
each with a generator (`oracle/prove/Generators`) and a mutation; every exact port the SAME, the mutations caught in
the receiver alone on 10, 9 and 12 of 20 records (30% to 70% predicted). Left: the rest of `semantic_traps.md` as
kinds (sign, MOVE padding and alignment, collation, PERFORM fall-through, file status).

### 8A.3 Boundary inputs from the IR

Status: built ahead (J-233); open: what its body lists.
Workpath: `docs/paths/divergence.md`.

Check: for each statement, inputs crossing its receivers' boundaries, written as a fixture under the program's
copybooks; on CardDemo the generated fixture enters every paragraph the sample data does, and drives at least one SIZE
ERROR, one high-order truncation, one negative store into an unsigned item and one dmax division the sample data never
does, counted by execution counters at the sites (the hostile review's §1, 2026-10-22).

Built ahead of the chain (J-233, research §50): `rt/Events` and `oracle/prove/Boundary`. On POSTTRAN and INTCALC the
boundary data drives SIZE_ERROR, TRUNCATED and DMAX_CUT at nine paragraphs where the sample drives no event at all,
enters every paragraph the sample does, and both lanes and both builds agree on it. Not met by data: a negative into
an unsigned item (CardDemo has no such receiver). Open: `job:` sources (TRANREPT-STUDY's transactions) and sites by
statement, not paragraph.

### 8A.4 The alignment file

Status: built ahead (J-234); open: what its body lists.
Workpath: `docs/paths/divergence.md`.

Check: `<PROGRAM>.align` maps a COBOL node, by the program's own names (R-064), to a Java symbol or line range,
inferred where a name or literal matches and `unconfirmed` until a person confirms; its coverage on the oc4j
reference predicted before it is measured; an alignment naming a node the IR does not hold is refused.

Built ahead of the chain (J-234, research §51): `oracle/prove/Align`. On oc4j's CardDemo Java: paragraphs 139 of 139,
statements 1,629 of 1,629 (its source-line comments), items 405 of the 412 the procedure names (537 of all 769:
oc4j writes nothing for an unreferenced item). On ACCRUE's hand port: 2 of 11 statements, by literal; the rest is
a person's to write. Every line `unconfirmed`; a line naming no node of the IR is refused.

### 8A.5 Attribution

Status: built ahead (J-236); open: what its body lists.
Workpath: `docs/paths/divergence.md`.

Check: for each differing field, the catalogue kinds whose probe reproduces it, ranked, "unattributed" when none
does; a mutation harness takes Katana's literal Java, breaks one rule per kind (truncation dropped, a store HALF_UP,
an exact division), runs it as the foreign port, and the report names that kind first; each mutation red before the
kind is built.

Built ahead of the chain (J-236, research §52): `oracle/prove/Attribute` with `rt/`'s probe switch (LANE-23). Each
kind's mutant attributed to its kind alone, every cell reproduced (10, 9, 12); unattributed with its probe withheld;
ACCRUE's hand-written HALF_UP port attributed to DV-02, 4 of 4.

### 8A.6 The report

Status: built ahead (J-237); open: what its body lists.
Workpath: `docs/paths/divergence.md`.

Check: per divergence: `proven` with the fixture record by hash or `possible`, the COBOL statement and origin, the
aligned Java or "unaligned", the cause kind and its text, the paragraphs entered up to the statement on that record,
and the fix as Katana's emitted Java for the statement; NE- and HY- findings apart as "no port can be exact here";
PROOF-01's quote present (the oracle is cobc -std=ibm, R-013); a `proven` line whose fixture is missing refused; two
runs write identical reports.

Built ahead of the chain (J-237, research §53): `oracle/prove/Report` and `Divergences`. On ACCRUE's hand-written
HALF_UP port: ACCT-INT proven at its COMPUTE, DV-02, aligned by literal, Katana's Java as the reference fix; ACCT-BAL
following from it; two runs identical; a proof whose fixture is missing or changed refused. Open: a per-record trace
for a program with no named paragraph, and a first report on a real port of a CardDemo program (8A.1's runtime
question for the oc4j reference).

### 8A.7 Tithe pass 8A

Status: built ahead (J-238); open: what 8A waits on, which is Ghost's.
Workpath: `docs/paths/divergence.md`.

Check: no change of behaviour and no golden changed (P-02): how a divergence report is made written where its kinds
are (docs/DIVERGENCES.md), the phase's words in the glossary, R-072's "Held by" naming the code that holds it, HANDOFF
current with what 8A waits on; the first report read outside the project (8.6's rule) under NDA (R-037), since its
fix shows the lowering's shape, handed to Ghost, whose it is to arrange.

## Phase 7: scale and the facades, continued

### 7.2 CICS facade

Status: waits on Ghost (HANDOFF).
Workpath: `docs/paths/lowering.md`.

Check: none in the roadmap; written here before the item starts.

Moved from 2.2 (R-071), the graft its facades would use: every `EXEC CICS` and `EXEC SQL` in the corpus parsing to
a command tree or a finding. Stopped (R-053): che4z's EPL-2.0 grammars enter the tree only after counsel (R-015);
measured outside the tree (J-064). 7.2's own plan, a grammar written from IBM's published syntax, may make the graft
unneeded; either way it waits on Ghost (HANDOFF).

### 7.3 DB2 facade

Status: open.
Workpath: `docs/paths/lowering.md`.

Check: none in the roadmap; written here before the item starts.

### 7.4 JCL beyond CardDemo

Status: open.
Workpath: `docs/paths/oracle.md`.

Check: none in the roadmap; written here before the item starts.

### 7.5 The Hercules lane

Status: open.
Workpath: `docs/paths/oracle.md`.

Check: none in the roadmap; written here before the item starts.

### 7.6 Tithe pass 7

Status: open.
Workpath: `docs/paths/process.md`.

Check: none in the roadmap; written here before the item starts.

## Phase 8: the product surface

### 8.1 CLI

Status: open.
Workpath: `docs/paths/process.md`.

Check: none in the roadmap; written here before the item starts.

### 8.2 The proof report, shipped

Status: open.
Workpath: `docs/paths/oracle.md`.

Check: none in the roadmap; written here before the item starts.

### 8.3 The provenance map, shipped

Status: open.
Workpath: `docs/paths/passes.md`.

Check: none in the roadmap; written here before the item starts.

### 8.4 `pack`

Status: open.
Workpath: `docs/paths/process.md`.

Check: none in the roadmap; written here before the item starts.

### 8.6 Tithe pass 8

Status: open.
Workpath: `docs/paths/process.md`.

Check: none in the roadmap; written here before the item starts.

## Phase 9: credibility and the radar

### 9.0 The provisional patent application is on file

Status: open.
Workpath: `docs/paths/process.md`.

Check: none in the roadmap; written here before the item starts.

### 9.1 Publish the NIST and CardDemo results

Status: open.
Workpath: `docs/paths/process.md`.

Check: none in the roadmap; written here before the item starts.

### 9.2 The regulatory translation

Status: open.
Workpath: `docs/paths/process.md`.

Check: none in the roadmap; written here before the item starts.

### 9.3 Approach the hyperscalers and IBM

Status: open.
Workpath: `docs/paths/process.md`.

Check: none in the roadmap; written here before the item starts.

### 9.4 The first paying shop names its JCL and DFSORT subset

Status: open.
Workpath: `docs/paths/process.md`.

Check: none in the roadmap; written here before the item starts.

### 9.5 A customer's z/OS output reconciles the lanes

Status: open.
Workpath: `docs/paths/process.md`.

Check: none in the roadmap; written here before the item starts.
