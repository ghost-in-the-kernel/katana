# TODO: the open items, in order

The roadmap as a chain: items are done in this order, never out of it, and an item that cannot be
done as written stops the chain and goes to Ghost. Each item's plan is its step in docs/ROADMAP.md
section 6, by the same number; its check is the roadmap's, copied here. A done item moves to
docs/DONE.md with its number kept. `test/TodoTest.java` holds this file: headings in numeric order, and
the first open item has a check.

## Phase 1: the oracle and the corpus

### 1.1 Reproduce NIST in CI

Check: the summary diff against GnuCOBOL's shipped summary is empty, in CI, under 3 minutes.

### 1.2 Run NIST under `-std=ibm`

Check: a `nist-ibm.txt` baseline committed and held by a test.

### 1.3 The job fixture model

Check: the four CardDemo jobs (POSTTRAN, TRANREPT, INTCALC, CREASTMT) are expressible; a fixture
with an unpinned clock is refused.

### 1.4 The cobc lane

Check: POSTTRAN and TRANREPT reproduce the frozen hashes from F-02 (`aed61744…` TRANFILE unload;
`a7d14a96…`, `19b7f04f…` TRANREPT); INTCALC and CREASTMT run once the PARM driver and the
`CUSTREC.cpy` tab policy exist.

### 1.5 The DFSORT subset

Check: every CardDemo SORT step runs; an unsupported card is a finding, not a guess.

### 1.6 The determinism gate

Check: with the clock pinned to seconds only, the gate fails naming TRANFILE column 326; with
hundredths and zone, it passes.

### 1.7 The comparator

Check: an injected one-byte change in a COMP-3 field is reported with the field name and both
decoded values; an `iconv`-style byte-blind transcode of the OMP data is detected as corruption.

### 1.8 The java lane

Check: a hand-written Java program that copies its input to its output passes a trivial fixture
through both lanes with an empty diff.

### 1.9 The proof report, v0

Check: none in the roadmap; written here before the item starts.

### 1.10 Tithe pass 1

Check: none in the roadmap; written here before the item starts.

## Phase 2: the front end and the Semantic IR

### 2.1 Fork ProLeap's two grammars

Check: 31/31 CardDemo, 82/82 BankDemo (privately), 30/30 OMP, and every NIST program `cobc` accepts,
parse to a tree with no error nodes; a test holds those counts and may only rise.

### 2.2 Drive che4z's CICS and DB2 grammars

Check: every `EXEC CICS` and `EXEC SQL` in the corpus parses to a command tree or is a finding.

### 2.3 The source map

Check: a round trip from any lowered statement back to the COBOL text is exact.

### 2.4 `sir/types`

Check: none in the roadmap; written here before the item starts.

### 2.5 `sir/build`

Check: the data map matches `cobc -C`'s `b_N` sizes, offsets and attribute tuples on every program
it accepts (F-06's oracle for the data map, used as a test); the IR serializes to JSON and
round-trips.

### 2.6 `sir/contract`

Check: none in the roadmap; written here before the item starts.

### 2.7 Findings, first set

Check: a program using TRUNC(OPT) or an overlapping PERFORM range produces the finding before any
lowering.

### 2.8 Tithe pass 2

Check: none in the roadmap; written here before the item starts.

## Phase 3: literal lowering and the runtime, proven on NIST

### 3.1 `rt/` core

Check: the 359 GnuCOBOL autotests, run as unit tests of `rt/` through hand-lowered programs first,
then through the lowering; each tests one rule, which is why they come first.

### 3.2 `rt/Dispatcher`

Check: every NIST NC and IC program's PERFORM/GO TO behaviour reproduces its REPORT.

### 3.3 `lower/`

Check: the emitted data map equals the IR's; every statement has its comment; the Java compiles with
`javac` against `rt/` only.

### 3.4 NIST, module by module, through both lanes

Check: the Java lane reproduces the same PASS/FAIL lines as the 9,610 GnuCOBOL passes, under both
`-std=cobol85` and `-std=ibm` profiles; the per-module pass count is recorded and held by a test
that may only rise (the bench discipline, P-01).

### 3.5 Findings, second set

Check: none in the roadmap; written here before the item starts.

### 3.6 Refactoring distance, v0

Check: none in the roadmap; written here before the item starts.

### 3.7 Tithe pass 3

Check: none in the roadmap; written here before the item starts.

## Phase 4: CardDemo batch, end to end

### 4.1 `rt/files`

Check: NIST SQ, RL and IX modules through the Java lane; CardDemo's five KSDS files load, read and
unload identically to the cobc lane.

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
