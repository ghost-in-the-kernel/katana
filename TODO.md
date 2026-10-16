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

Check: none in the roadmap; written here before the item starts.

### 3.6 Refactoring distance, v0

Check: none in the roadmap; written here before the item starts.

### 3.7 Tithe pass 3

Check: none in the roadmap; written here before the item starts.

Carried in (Fable, 2026-10-06): a line in GHOST-IN-THE-KERNEL's "check that cannot fail" paragraph,
since J-082's pattern (an old helper still at work where no test looked; a check whose expected side
came from the code under test) has now been met four times (J-082, J-098 twice, J-099).

## Phase 4: CardDemo batch, end to end

### 4.1 `rt/files`

Check: NIST SQ, RL and IX modules through the Java lane; CardDemo's five KSDS files load, read and
unload identically to the cobc lane.

*Done (J-172):* SQ 85 of 85. *Done (J-180):* RL 35 of 35, relative files in `rt/RelFile`. *Next:* IX
(indexed files: RECORD KEY, ALTERNATE keys with duplicates, START on a key), then CardDemo's KSDS files.

### 4.2 `rt/sort`

Check: none in the roadmap; written here before the item starts.

### 4.3 `rt/call`

Check: none in the roadmap; written here before the item starts.

### 4.4 The stub facade

Check: none in the roadmap; written here before the item starts.

### 4.5 The four jobs through both lanes

Check: POSTTRAN, TRANREPT, INTCALC and CREASTMT produce byte-identical datasets (on logical unload),
SYSOUT and return codes against the phase-1 goldens; the Java lane consumes the same `DD_*` fixture
with no per-program adaptation.

### 4.6 Proof report, v1

Check: none in the roadmap; written here before the item starts.

### 4.7 Tithe pass 4

Check: none in the roadmap; written here before the item starts.

## Phase 5: the Intent IR and the proof ledger

### 5.1 `intent/types`

Check: none in the roadmap; written here before the item starts.

### 5.2 `intent/rules`

Check: none in the roadmap; written here before the item starts.

### 5.3 The review file

Check: none in the roadmap; written here before the item starts.

### 5.4 `ledger/`

Check: `ledger.test` refuses an edit to an existing line; every Java symbol in a build has at least
one line; a symbol removed by a pass has a `superseded-by`.

### 5.5 Tithe pass 5

Check: none in the roadmap; written here before the item starts.

## Phase 6: proven refactoring passes

Every pass's check (ROADMAP 6): all phase-3 and phase-4 goldens byte-identical; the
paragraph-entry trace equal; the ledger has a line per changed symbol; the pass's own unit corpus
(the GnuCOBOL autotests that exercise its construct) passes.

### 6.1 Pattern structuring

Check: as above.

### 6.2 Method extraction under the dispatcher

Check: as above.

### 6.3 Scalar promotion

Check: as above.

### 6.4 Exact-arithmetic substitution

Check: as above.

### 6.5 Enum and flag types

Check: as above.

### 6.6 Stateless subprograms

Check: as above.

### 6.7 Record classes

Check: as above.

### 6.8 Refactoring distance, per pass and cumulative

Check: as above.

### 6.9 Tithe pass 6

Check: none in the roadmap; written here before the item starts.

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
