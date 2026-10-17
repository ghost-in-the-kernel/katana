# DONE: the items finished, with their numbers kept

Moved here from `TODO.md` when done; the number stays the roadmap's (docs/ROADMAP.md section 6).
Newest last. A line says what was done and where; the decisions an item produced are in `theGhost.md`.

## Phase 0: founding

### 0.1 Repo and license

The founding commit: the repository, `LICENSE`, `.gitignore` refusing what a customer gave us and
what a run produced.

### 0.4 Import the corpus

The founding commit, as `corpus/fetch.sh`, `corpus/scan.sh` and `corpus/MANIFEST.md`; clones are
fetched by script and never committed. The check's test (a file with no license fails) is not yet
written.

### 0.5 Import the research artifacts

The founding commit: the study's prototypes under `front/prototypes/`, `lower/prototypes/` and
`oracle/prototypes/`, each a prototype to be absorbed, not a dependency.

### 0.2 The documents of 4.1 and their tests

The founding commit: `theGhost.md`, `docs/GLOSSARY.md`, `CLAUDE.md`. Then `docs/modules.json`,
`docs/AGENT.md`, `TODO.md` and this file, `docs/SEMANTIC-IR.md`, `docs/PROFILE.md` and
`docs/PROOF.md` as headings with their numbering reserved, `docs/KERNEL-MAP.md` with its format; and
the doc tests under `test/` (ghost, glossary, docs, modules, todo), run by `test/run.sh` (R-038), each
held to inputs it must reject. Decisions: R-038, R-039.

### 0.3 The container

`Dockerfile`: the official Gradle 8.14.3 / JDK 21 / Ubuntu 24.04 image pinned by digest, with
`gnucobol3` 3.1.2-5.1ubuntu1 (R-003). `build.gradle` declares no repository; its only dependencies are
the jars in `third_party/` (`third_party/MANIFEST.md`), and it refuses any Gradle but 8.14.3. The doc
tests run under JUnit (`test/DocTests.java`). `.github/workflows/ci.yml` builds the image and runs
`gradle build --offline` with `--network none`. Checked: a fresh clone built that way in 12 s.

### 0.6 Kernel map and metrics scripts

Ported from Suite Builder's `kernel-scan.mjs` and `kernel-metrics.mjs` to Java under `scripts/`,
reading source with the JDK's own parser (no dependency). `gradle metrics` prints the table of
ROADMAP 5 and the warnings of 4.2; `gradle kernelMap` appends a row per new function in a module to
`docs/KERNEL-MAP.md` (empty: no module has source yet). `test/MetricsTest.java` ratchets the recorded
values; `test/KernelMapTest.java` fails on a function the map lacks or one gone with no closing row.
Found on the way: `gradle test` skipped the doc tests after a doc-only change, because the task's
declared inputs were its classes and not the documents they read, so a broken document passed
locally (CI, from a fresh clone, was never fooled). Found by deleting a module file in a scratch clone
and watching the build stay green; fixed in its own commit, the test task now always runs.

### 0.7 Tithe pass 0

Three fresh-agent trials ("add a module"). The first two read "add" as permission, reached
`docs/modules.json` 4th and 5th and changed files; between and after them: the `adding` line in
`docs/modules.json`, the routing row naming it, `settings.gradle` saying modules are not Gradle
subprojects, the module count kept only in `modules.json`, stale test names fixed, and R-043 (a new
module is proposed, not added). The third, phrased as a proposal and asked by a contributor, passed:
`CLAUDE.md` then `docs/modules.json`, nothing changed, §3.3 named as Ghost's. Metrics recorded in
`docs/AGENT.md` at tithe pass 0. Phase 0 is done.

## Phase 1: the oracle and the corpus

### 1.1 Reproduce NIST in CI

Done in six steps (a to f). Katana's own harness (R-044, R-045), written from NIST's population file,
EXEC85 and the CCVS85 User Guide: `oracle/nist/` extracts the 459 programs with EXEC85 under cobc,
runs the twelve modules (426 runs) and reads each footer. Its golden is what NIST's footers say
(R-047): `Total 426 413 12 1 9343 5 29 143 9520`; `RECONCILIATION.md` explains every difference from
the published `Total 426 426 0 0 9610 0 20 118 9748` in 45 lines, and the fast loop rebuilds every
published row from them. CI's `nist` job runs it with the network off: 85 s on 2026-10-04 (run 43),
under the 3-minute limit its step enforces.

### 1.1b The population reader (a step of 1.1)

`oracle/nist/Population.java` reads NIST's population format. The corpus README and the User Guide
both describe a member as `*HEADER,kind,name` to `*END-OF,name`; 68 of `newcob.val`'s 512 members
say otherwise, naming their main program first and themselves last
(`*HEADER,COBOL,IC101A,SUBPRG,IC102A`), and two headers carry sequence text past column 72. Found
by reading the real file rather than trusting the description; the reader's count (512: 459
programs, 51 copy members, 2 data files) now says which is true.

### 1.1e The module runs (a step of 1.1)

`oracle/nist/ModuleRun.java` runs all twelve modules under cobc as NIST's guide says (mains in order
with their SUBPRG chains, SUBRTN modules, copy libraries, data on standard input, DB103M twice), with
the per-program conditions in `run-notes`. Katana's own environment took failed tests from 329 to 5.
`programs.golden` (426 runs) is what each footer reports (R-047); `RECONCILIATION.md` explains every
program that differs from the published summary with one of eight reasons, four of them rules the
test applies, and `ReconciliationTest` rebuilds every published row exactly.

### 1.2 Run NIST under `-std=ibm`

The NIST run under both dialects; the IBM profile's own X-cards in `oracle/nist/exec85-ibm.cards`
(IBM's switches are UPSI-0 and UPSI-1). The baseline is `oracle/nist/programs-ibm.golden` (the
roadmap's `nist-ibm.txt`, named like the cobol85 golden; J-027): `Total 426 406 19 1 9126 106 29
143 9404`. 21 runs change from cobol85, each a line of `oracle/nist/IBM-CHANGES.md` with its cause;
`DialectChangesTest` fails on a change with no line, a line with no change, or a cause that does not
hold. Three causes are behaviours the profile must model, each one GnuCOBOL flag whose failures were
measured alone on the cobol85 run and must add up to the run's: `arithmetic-osvs` (61 failures over
seven programs), `binary-truncation` (8, three), `section-segments` (31, four). Seven programs use a
word the IBM dialect reserves (CHAR, NOTHING) and no longer compile; one follows a chain that does.
CI runs each dialect in its own step, each under 3 minutes.

### 1.3 The job fixture model

`docs/FIXTURE.md` is the schema; `oracle/fixture/` reads and checks a fixture against it, and
`FixtureDocTest` holds the two to each other. A fixture is a text file of lines (job, profile by
hash, clock, TZ, modules, stubs, the datasets that exist at the start and their records by SHA-256,
steps with PGM, PARM and COND, DDs with dataset, disposition, record format, copybook and encoding,
inline cards, gold), addressed by the SHA-256 of its canonical text. The four CardDemo jobs are in
`oracle/fixture/carddemo/` with no refusal; a fixture with no clock, or with seconds only, or with
no zone, is refused (`FixtureTest`), as is a breach of each other invariant (FIXTURE-01 to 08). The 18 sources they cite
hash to what they say (`FixtureSourcesIntegrationTest`, in CI's nist job); CardDemo is now fetched
at a pinned commit. The fixture carries a profile hash, so `profile/` was built as far as a fixture
needs it (`docs/PROFILE.md`): compiler, version, dialect and flags, mapped to cobc arguments and
hashed; the IBM option fields wait for the manuals. Three choices wait on Ghost as R-009
(docs/HANDOFF.md).

### 1.4 The cobc lane

`oracle/lane/` runs a fixture's job under GnuCOBOL: the catalog started as the fixture says (each
source checked against its hash, KSDSs loaded in key order with the alternate keys of the paths over
them), DDs bound by `DD_<name>` and allocated by disposition, generations, the program built under the
fixture's profile (a module and a PARM driver when its PROCEDURE DIVISION has USING), the CEE3ABD
stub reporting the abend code, IDCAMS (DELETE, DEFINE CLUSTER, REPRO, SET), IEFBR14, IEBGENER, COND
and abend bypass, and every dataset unloaded and hashed at the end. SORT stops the job with a
finding naming 1.5 (R-048). POSTTRAN reproduces the study's frozen TRANFILE unload (`aed61744…`),
and with it the reject file (`86f3b341…`) and the program's stdout (`a33ef3a7…`); INTCALC runs
through the PARM driver; TRANREPT-STUDY and CREASTMT run to their SORT. `oracle/lane/carddemo.golden`
holds all five reports; CI runs them in a step of their own. Not built, since no fixture needs them
yet: temporary datasets (the schema has none) and the COBDATFT stub (J-036).

### 1.5 The DFSORT subset

Katana's DFSORT is Java (R-010, `oracle/sort`): SYMNAMES, SORT FIELDS on CH and ZD keys (ZD with
IBM's overpunch letters), EQUALS, INCLUDE/OMIT, OUTREC with columns, blanks, constants and EDIT;
stable, with a finding naming the SORTOUT records whose order was the lane's choice when the cards
lack EQUALS (R-026); a record shorter or longer than SORTOUT's LRECL padded with X'00' or truncated
as IBM's guide states (R-051), under the fixture's `dfsort.pad`/`dfsort.trunc`; any other card a
finding quoting it. A generated COBOL SORT was measured beside it and retired (J-045). Every CardDemo
SORT step runs (TRANREPT's and PRTCATBL's in their REPROCT-inline fixtures, J-035): TRANREPT-STUDY
reproduces the frozen sort `a7d14a96…` and report `19b7f04f…` (and CBTRN03C's stdout), COMBTRAN reads
a concatenated SORTIN, PRTCATBL-INLINE truncates 41 bytes to 40 and CREASTMT pads 328 to 350. Tabs
are expanded at each file's declared width (R-016; CUSTREC.cpy at 4). **Deferred under R-053:**
CREASTMT end to end. It runs to CBSTM03A, which compiles and then reads z/OS control blocks from
address 0 and ends on SIGSEGV (J-050, HANDOFF "For Fable").

### 1.6 The determinism gate

`oracle/lane/Gate.java`: the cobc lane runs a job twice in separate folders, and `Gate.compare` holds
the two runs to each other on every dataset's logical unload, every program step's stdout and every
step's outcome; `Gate.require` is what the comparator (1.7) calls first. A difference is named by its
dataset, the DDs that reach it, the records and the columns. Every CardDemo fixture passes; POSTTRAN
with its clock pinned to the second (set outside the fixture, R-009) fails at
`TRANSACT.VSAM.KSDS (DD TRANFILE in STEP15): 257 of 257 records differ, in columns 325-326` (J-052).
R-012's text waits for Fable (HANDOFF).

### 1.7 The comparator

`oracle/compare/`: two datasets as logical record streams (fixed, lines, or RDW), compared field by
field under a copybook layout (`Layout`: levels, PICTURE, USAGE, OCCURS, REDEFINES). Text and zoned
fields compare by value across declared encodings (ascii, cp037, cp1140) and sign-zone schemes; packed
and binary fields, record counts, record order, rounding and truncation are compared as they are,
never normalized. A difference is grouped by field: how many records, and the first's record, byte
offset, both bytes and both decoded values. Masks are declared and each one applied is listed.
Nothing is compared before the gate passes (`Gate.require`). On the OMP course's EBCDIC data, a
one-byte change in a COMP-3 field is reported with its field and both values, and a byte-blind
`iconv` is caught as corrupt packed fields (ACCT-BALANCE in 44 of 45 records, ACCT-LIMIT in 9) while
every text field compares the same (J-053). R-011's text and the copybook reader wait for Fable.

### 1.8 The java lane, as a stub

The lanes share everything but the program step (`ProgramRunner`): the java lane runs a Java program
under the cobc lane's DD contract, written down as `docs/LANES.md` (LANE-01 to 05): DDs as
`DD_<name>` paths, fixed-length records, the clock and zone in the environment, the PARM as the one
argument, SYSIN as stdin, stdout, the exit code, an abend on stderr. Its stub runs sequential datasets
only and refuses the rest with a finding. The check: `oracle/fixture/trivial/COPY.fixture` through the
cobc lane (Katana's `COPYREC.cbl`, twice, through the gate) and the java lane (a hand-written Java
copy) compares with an empty diff, and the two stdouts are the same.

### 1.9 The proof report, v0

`oracle/proof/ProofReport.java` writes what was compared: the fixture and the profile by hash, the
gate's result (and nothing compared when it failed, PROOF-03), each dataset with its masks and the
comparator's findings, each program's paragraphs entered and not entered from a traced run (`-ftrace`,
which the report's test holds identical to the untraced run), and `docs/PROOF.md`'s "What it never
covers" quoted verbatim (PROOF-01). `docs/PROOF.md` is written from R-013's sentence in the roadmap,
its wording Ghost's (HANDOFF). Two reports are held as goldens: `oracle/proof/COPY.report.md` (the
trivial fixture, the cobc lane against the java lane) and `oracle/proof/POSTTRAN.report.md` (the cobc
lane against the study's frozen TRANFILE and rejects: the same; CBTRN02C 24 of 26 paragraphs entered,
the two not entered its abend and I/O-status paragraphs).

### 1.10 Tithe pass 1

Every metric re-measured in `docs/AGENT.md`; the fast loop and the docs ratio went to the boundary
(R-054). The fresh-agent trial (`SUM FIELDS=NONE`, propose only) went `CLAUDE.md`, then the DFSORT
row's three files, and changed nothing; its one gap, P-11 only in a javadoc, is in the row (J-057).
The boundary count: 6 held, 3 reversed, 0 stopped, 2 trials (J-058). Phase 1 is done.

### 2.1 Fork ProLeap's two grammars

`front/`: reference format (`Lines`), COPY with REPLACING and libraries, REPLACE and EXEC blocks
(`Preprocessor`, `Copybooks`), and the parse (`Parse`), over ProLeap's grammars forked into
`front/grammar/` (MIT notice kept) and generated at build time by the vendored ANTLR tool. Held by
`front/corpus.golden`: CardDemo 31/31, NIST 437/437 (every program of a module the NIST run compiles
that cobc accepts), BankDemo 82/82 (privately, its count only), OMP 29/30: CBL0007's deliberate error
is the one left, and the check's 30 is a question for Fable (HANDOFF). The gaps closed: ALTERNATE KEY
without RECORD, keyword arguments to intrinsics, RERUN ... RECORDS OF file, a literal closed in
column 72 and resumed by a quote, a COPY's library on its own line, and a subscript after a space
under SLL; NOT=, LENGTH OF in reference modification, PROGRAM-ID without a period and a tab in the
indicator area already parsed (`ParseTest`). DFHAID and DFHBMSCA are in `front/ibm/`. J-063.

### 2.3 The source map

`front/Mapped.java`: the text the grammar reads carries, for each character, its file, line and
column (tabs expanded, R-016) and whether it is the source's own (SOURCE), put there by REPLACE or
REPLACING at the place it replaced (REPLACED), or written by the front end (ADDED). `Parse.Result`
gives any token's or node's origin. The check, until lowering exists: every clean program of the
corpus round-trips (`RoundTrip`: each SOURCE character is its file's at its place; every token has a
file), held by `front/corpus.golden`'s counts; the lowered statements' half is 3.3's (HANDOFF). J-065.

### 2.4 `sir/types`

The Semantic IR as records, enums and sealed interfaces in `sir/types/`: the header with the profile
hash, the data map (`Item`, its `Attributes` tuple, `Occurs`, 88s), the paragraph graph (`Procedure`:
sections, paragraphs in textual order, PERFORM, GO TO and ALTER edges), statements as operations over
`Value`s with roles and branches, `FileDescriptor`s and `Call`s, each element with its origin (2.3).
`docs/SEMANTIC-IR.md` names them and SIR-01 to SIR-07; `SirDocTest` holds the two together;
`SirTypesTest` builds COPYREC's IR by hand and has each invariant refuse one violation. J-066.

### 2.5 `sir/build` and `sir/check`

Tree to IR for every program unit: the data map (2.5a: CardDemo 215/215 and NIST 12,881/12,893 of
cobc's fields, the rest named in `sir/datamap.golden`), the paragraph graph with PERFORM, GO TO and
ALTER edges, statements as the grammar shapes them, files, CALLs, the header; names resolved through
qualifiers, files, GLOBAL items of containing programs, SPECIAL-NAMES mnemonics and switches. `sir/json`
writes and reads it; `sir/check` finds a long REDEFINES, a target that names nothing, an ODO object
after its table, a file without FILE STATUS. Held by `sir/ir.golden`: CardDemo 31, NIST 437 and OMP 29
programs all built and round-tripped (BankDemo privately). J-067, J-068.

### 2.6 `sir/contract`

`sir/contract/Contract.java`: a program's contract as one line per fact, each line its kind, its
program and the fact, written by the kind's row in `KINDS` (thirteen rows: profile, item, redefines,
occurs, value, condition, file, paragraph, falls, edge, statement, call, using); `diff` compares two
contracts fact for fact. `docs/SEMANTIC-IR.md` tables the kinds (`ContractTest` holds both); COPYREC's
contract is `sir/contract/COPYREC.contract`; the corpus's contracts (CardDemo 23,802 facts, NIST
415,863, OMP 2,915) are the same lines from the IR read back from JSON (`sir/ir.golden`). J-074.

### 2.7 Findings, first set

`findings/Finding.java` (the kinds NE-01 to NE-06 and the report, by kind then place) and
`sir/check/NonExact.java`, which finds F-08's classes in the IR under the profile it was built with:
TRUNC(OPT) over binary items, overlapping and recursive PERFORM ranges, division without ON SIZE
ERROR, ODO tables, COMP-1 and COMP-2. `NonExactTest` holds the check (TRUNC(OPT) and an overlapping
range each a finding, a nested range not); `docs/FINDINGS.md` tables the kinds (`FindingsDocTest`);
`sir/ir.golden` counts them over the corpus. Uninitialized reads wait for phase 5's data flow. J-075.

### 2.8 Tithe pass 2, the phase-2 boundary

Fable's plan (HANDOFF "For Opus: tithe pass 2"), each item its own commit through the pre-push check.
The IR made complete: no token left behind (A1, R-017 in force, held by `NoTokenLeftTest` and the
corpus run: 0 tokens left on CardDemo, NIST and OMP), `Origin` its own (A2), literals with their kind
and payload under cobc as oracle (A3, SIR-08), PICTURE as an edit mask and the clauses it dropped
(A4), per-program contract goldens (A5), the role vocabulary from the grammar (A6), R-056's meta-test
over `docs/checks.json` (A7). The seams phase 3 stands on: the oracle reads COBOL through `sir/`
only (B1), the generated program's contract and NIST's `Runner` (B2, LANE-06 to 09), the Autotest
reader with the fetch left to phase 3 by R-045 (B3), `rt/` compiled against the JDK alone (B4). The
tithe: the kernel map's stale column (C1), the quick NIST (C2), `DataMap` split (C3), pass 2 measured
and two decisions' missing tests written (C4). J-076 to J-091.

## Phase 4: CardDemo batch, end to end

### 4.1 `rt/files`

Check: NIST SQ, RL and IX modules through the Java lane; CardDemo's five KSDS files load, read and
unload identically to the cobc lane.

*Done (J-172):* SQ 85 of 85. *Done (J-180):* RL 35 of 35, relative files in `rt/RelFile`. *Done
(J-182):* IX 42 of 42, indexed files in `rt/IdxFile`. *Done (J-183):* CardDemo's KSDS files load, read
and unload identically in both lanes: every CardDemo fixture but CREASTMT (its PSA, 4.4) reports in the java
lane what the cobc lane's golden holds, the KSDS files on their logical unload (R-025), the data's
translated EBCDIC signs loaded in the lane's form (R-061).

### 4.2 `rt/sort`

Check: none in the roadmap; written here before the item starts. NIST ST through the Java lane (J-184).

*Done (J-184):* SORT, MERGE, RELEASE and RETURN through `rt/SortFile`; ST 32 of 40 the same as cobc and 6
more where cobc refuses a valid program (D-032). The finding R-026 names for an output that could depend on
the order of equal keys without DUPLICATES is not built: no ST or CardDemo program has needed it.

### 4.3 `rt/call`

Check: none in the roadmap; written here before the item starts. NIST IC through the Java lane, under the
profile's NODYNAM and under DYNAM, each with its floor (J-186).

Carried in (J-183): PARM via PROCEDURE DIVISION USING is lowered (LANE-17); CALL is not, but for LE's
CEE3ABD (LANE-18, the stub facade's first routine, 4.4). CREASTMT waits here for CBSTM03B.

*Done (J-186):* CALL and CANCEL through `rt/RunUnit` (R-062, LANE-19): LINKAGE bound BY REFERENCE and BY
CONTENT, ON EXCEPTION, EXIT PROGRAM and GOBACK, STOP RUN from any program, INITIAL, nested programs with
their containers' GLOBAL records (SIR-12), EXTERNAL records and files, static and dynamic copies by
`ibm.dynam`. IC 18 of 25 under NODYNAM, 4 more where IBM's NODYNAM departs from cobc (D-033); 22 under
DYNAM. OB 7 of 7. Not built, each a finding where a program reaches it: BY VALUE, CALL ... RETURNING,
a containing program's GLOBAL file and USE GLOBAL (IC233A, IC234A). CREASTMT's CBSTM03B is lowered;
CBSTM03A stops before its first CALL at the PSA, z/OS storage (4.4).

### 4.4 The stub facade

Check: none in the roadmap; written here before the item starts. CREASTMT through the java lane end to end:
STEP040's TIOT lines name the fixture's job, step and DDs, and its statements hold each transaction of
their input once, each total its lines' sum (J-187).

*Done (J-187):* LE's CEE3ABD was lowered in 4.1 (LANE-18); no CardDemo fixture's program calls an
assembler routine (COBDATFT, MVSWAIT and CEEDAYS are called by programs no fixture runs), so the recorded
stubs wait for a fixture that needs one (P-11). The z/OS storage facade (R-027, R-063, LANE-20): POINTER,
SET ADDRESS OF and LENGTH OF lowered; `rt/Storage` from the step's facts. CREASTMT runs end to end in the
java lane: 50 statements, 262 transactions, rc 0. The layouts are hypotheses until IBM's text or a z/OS
run (HANDOFF, Trials).

### 4.5 The four jobs through both lanes

Check: POSTTRAN, TRANREPT, INTCALC and CREASTMT produce byte-identical datasets (on logical unload),
SYSOUT and return codes against the phase-1 goldens; the Java lane consumes the same `DD_*` fixture
with no per-program adaptation.

*Done (J-190):* `CardDemoJavaLaneIntegrationTest` lowers each job's programs from its own fixture (its
steps' PGMs and its modules, at its tab widths), no program named in the test. POSTTRAN, TRANREPT and INTCALC,
with TRANREPT-STUDY, PRTCATBL, PRTCATBL-INLINE, COMBTRAN and SORTTEST, report what the phase-1 golden holds,
SYSOUT the same but the DISPLAY sign form D-005 explains. CREASTMT matches the golden through STEP030; its
STEP040, which the cobc lane abends in (R-027), is held to `oracle/lane/carddemo-java.golden` and to its input
(`Statements`), its blocks' layouts R-063's hypotheses.

### 4.6 Proof report, v1

Check: none in the roadmap; written here before the item starts. The four jobs proven, the java lane against
the cobc lane, every dataset compared under its fixture's copybook, coverage from the cobc trace and from
the java lane's where the cobc lane cannot run a step, the two traces equal elsewhere; CardDemo's coverage
by the fixtures, every paragraph none entered listed (J-191).

*Done (J-191):* `oracle/proof/<JOB>.v1.report.md` for POSTTRAN, TRANREPT, TRANREPT-STUDY, INTCALC and
CREASTMT (PROOF-04), every dataset compared the same; CREASTMT's statements said not compared (R-027).
`oracle/proof/CARDDEMO.coverage.md`: 106 of 113 paragraphs of the five programs entered; the seven left are
each program's abend and I/O-error paragraphs, which no fixture's data reaches.
