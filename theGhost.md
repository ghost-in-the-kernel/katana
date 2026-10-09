# theGhost.md: every decision Katana stands on

The layer beneath the comments. Each decision has a number (R-001), what was decided, why, what holds
it (a test, a document, a file), who decided and when, a status, and the decisions it touches. A new
decision is appended; a reversed one is marked `superseded by R-nnn` in the same commit that adds its
successor, and the successor names it. `test/GhostTest.java` holds this file: ids unique and sequential,
supersession named on both sides, and no two in-force decisions tagged with the same `subject:`
that carry a `conflicts-with:` line (docs/ROADMAP.md, P-05). `docs/decisions.md` is written from it.

Statuses: `proposed`, `in force`, `superseded by R-nnn`, `withdrawn`, `refused` (decided against, so
the question is closed).

Who may write here: Ghost decides. Any agent may append a decision Ghost has made, and may add or
correct cross-references (`See also`, `Held by`) on any entry without asking, since those change what
a decision points at, not what it says. A change to an entry's `Decision`, `Why` or `Status` text is
Ghost's alone, or an agent's with Ghost's explicit word in the same session (R-042).

---

### R-001: Java 21 for everything
- **Decision:** The compiler, the runtime (`rt/`), the harness (`oracle/`) and the CLI are Java 21,
  built with Gradle. Dependencies are vendored under `third_party/` with their licenses, so a clean
  clone builds with the network off.
- **Why:** The runtime must be Java, so Java is in the tree regardless; ANTLR's home target is Java and
  the forked grammars need no bridge; the harness ships to Java shops as one jar; one toolchain means
  one test runner, one lint, one set of rule tests; the buyer's reviewer reads one language. The
  container the study ran in had Maven Central, Gradle distributions and GitHub release assets
  blocked by its proxy, which is why the dependencies are vendored.
- **Held by:** `build.gradle` (offline build in CI), `docs/AGENT.md` ("clean clone builds offline").
- **Source:** Ghost, 2026-10-04, on Reviewer G's recommendation (docs/ROADMAP.md 3.4).
- **Status:** in force
- **See also:** R-038 (the doc tests before Gradle), R-041 (Gradle from the image)
- **subject:** toolchain

### R-002: Licensing is protective
- **Decision:** The compiler (`front/`, `sir/`, `lower/`, `intent/`, `passes/`, `ledger/`,
  `findings/`, `project/`, `cli/`) is all rights reserved, in a private repository, licensed per
  engagement. The runtime (`rt/`) is proprietary; each customer receives a perpetual, royalty-free
  license to the copy inside their own migrated suite, with no redistribution and no recurring fee.
  The harness (`oracle/`, `bench/`, the fixture formats) is under the same terms as the compiler,
  which carry an evaluation grant (ninety days, no production, results unpublished), so it stays
  evaluable. (Amended 2026-10-06: first "PolyForm Noncommercial or counsel's equivalent"; that text
  expressly permits use by government institutions and by any educational or research organization
  regardless of funding, which is two of the three things Ghost charges for.) The terms, in
  `LICENSE`: no license by default; a teaching grant to accredited institutions for classroom use and
  coursework only (not sponsored research, spin-outs, or government laboratories; widening it to
  research is Ghost's, not taken); the evaluation grant; everything else, commercial, government at
  any level, or nonprofit paid work, by written agreement with an annual fee. The fee is for using
  Katana, never for running its output: the migrated Java, its proof and its ledger belong to the
  party migrated for, and the runtime in their suite is theirs perpetually (Ghost, 2026-10-06: "the
  party that runs Katana gets to keep the output for free; the conversion is labor; no rent on their
  intellectual property"). No grant is exercised before R-037's provisional is filed, since a
  classroom is a public disclosure. Forked ProLeap grammars keep their MIT notice; che4z grammars enter only
  per R-015 after counsel; GPL code (`cobc`, `cobj`) never enters the tree, only their outputs as
  oracles. Nothing leaves the repository before R-037's provisional application is filed.
- **Why:** Ghost, 2026-10-04: the revenue funds the work, including the agents' compute; nobody uses it
  commercially without paying. The runtime's per-customer perpetual grant is the argument against the
  compile-to-JVM vendors' recurring runtime fees, which buyers resent (ISG 2024, in
  docs/research/incumbents.md). The harness stays evaluable because the route onto IBM's and the
  hyperscalers' radar is as an independent oracle for their LLM output (docs/research/FEASIBILITY.md).
- **Held by:** `LICENSE`; per-module license files before any copy leaves (`test/license.test`:
  every module named here has its file); `corpus/MANIFEST.md` for third-party terms.
- **Source:** Ghost, 2026-10-04. Not legal advice; to be confirmed with counsel before the first
  external disclosure.
- **Status:** in force
- **See also:** R-015, R-037
- **subject:** license

### R-003: GnuCOBOL 3.1.2, pinned by image digest, is the cobc lane
- **Decision:** The oracle's original lane runs GnuCOBOL 3.1.2 from the Ubuntu package, in a container
  pinned by digest. Revisited only if a NIST reference log differs from the lane's output by more than
  its version string.
- **Why:** It installs in about a minute, produced the study's `Total 426 426 0 0 9610 0 20 118 9748`
  NIST run (oracle/prototypes/nist/summary.log) matching GnuCOBOL's shipped summary, and the
  `eval-nist-cobol85` reference logs (made with 3.2.0) differed from it only in the version string
  on the program checked (NC101A).
- **Held by:** `Dockerfile` (digest); `oracle/nist` CI job (summary diff empty).
- **Source:** Ghost, 2026-10-04, on recommendation.
- **Status:** in force
- **See also:** R-A in docs/ROADMAP.md (GnuCOBOL stands in for IBM; every claim says so until a
  customer's z/OS output reconciles the lanes).
- **subject:** oracle-lane

### R-004: No model runs at conversion time
- **Decision:** Nothing in the pipeline from COBOL to shipped Java calls a language model. The one
  place a model may ever appear is to *propose* Intent IR facts that a person confirms, and not in v1
  (R-031).
- **Why:** Determinism is the product. A model in the loop makes every run non-reproducible, which is
  the incumbents' position and the gap Katana fills (docs/research/FEASIBILITY.md: no incumbent
  publishes audited pass/fail numbers; IBM's own paper calls dynamic testing the lowest-ROI check).
- **Held by:** `modules.json` (no module may import a model client); `test/source-rules`.
- **Source:** Ghost, 2026-10-03 (the original idea: "a compiler that does the work of LLMs").
- **Status:** in force
- **See also:** R-007 (the Intent IR, the only place a model could ever propose anything)
- **subject:** determinism

### R-005: Batch only, for v1
- **Decision:** Katana v1 proves and migrates batch programs and jobs. CICS online transactions and
  IMS are out of scope; `EXEC CICS` and `EXEC SQL` are lowered through facades with recording stubs
  (phase 7) so programs containing them still build, but their online behaviour is not proven.
- **Why:** The oracle for batch is byte-for-byte on datasets and exists (R-003). Online equivalence
  needs a transaction replay the study found only in Google's Dual Run and Mechanical Orchard; every
  open tool stops at CICS, DB2 and JCL (docs/research/open_source_tools.md).
- **Held by:** `docs/PROOF.md` (what a proof covers); `findings/` (a CICS program's proof report says
  what is not covered).
- **Source:** Ghost, 2026-10-04.
- **Status:** in force
- **See also:** R-003 (the batch oracle that makes batch provable), R-006 (the other scope cut)
- **subject:** scope

### R-006: One target language; no C++
- **Decision:** Java is the only back end. The Java-to-C++ idea is dropped.
- **Why:** A second target before the Semantic IR is proven stable repeats Suite Builder's Cypress
  mistake (a second generator copied instead of shared). Java to C++ is a port, not a translation
  (memory model, exceptions, strings, GC), and no mainframe buyer asks for it.
- **Held by:** `modules.json` (`lower/` has one emitter).
- **Source:** Ghost, 2026-10-04.
- **Status:** refused (C++); in force (one target)
- **See also:** R-005 (scope), R-007 (the Semantic IR a second target would have to wait for)
- **subject:** targets

### R-007: Three IRs and a ledger, kept apart
- **Decision:** The Semantic IR (what the program does; exact; mechanically derived), the Intent IR
  (what it means; inferred facts with evidence, confidence and confirmation state), and the proof
  ledger (what was done and why it was allowed; append-only) are three structures. The dialect
  profile is a fourth, pinned input whose hash appears in the IR header, the Java manifest, every
  fixture and every ledger line. Findings are the fifth. Determinism comes from the Semantic IR and
  the ledger adds nothing to it; provenance comes from the ledger and the IR adds nothing to that.
- **Why:** Intent cannot be proven by an oracle and semantics cannot carry a "why"; one structure
  bent to do both does both badly (docs/ROADMAP.md 3.2). The ledger is Suite Builder's kernel map
  pointed at the customer's program: the thing that stops a later change from destroying evidence of
  an earlier one.
- **Held by:** `modules.json` (`sir/`, `intent/`, `ledger/`, `profile/`, `findings/` as separate
  modules with their import rules); `docs/SEMANTIC-IR.md`, `docs/PROOF.md`.
- **Source:** Ghost and Reviewer G, 2026-10-04 (the morning's architecture discussion).
- **Status:** in force
- **See also:** R-004 (why the Intent IR is kept apart), R-008 (the kernel map, the ledger pointed at the tool)
- **subject:** architecture

### R-008: The navigation tithe
- **Decision:** One commit in eight changes no behaviour and improves the agent's environment or the
  code's navigability; every phase ends with a tithe pass of one to two days; the metrics in
  `docs/AGENT.md` ratchet and may not regress without a decision.
- **Why:** Ghost's finding in Suite Builder: delivery speed rose measurably once the agent's ability
  to find, read and change code with little context was treated as work; about a tenth of effort
  went there. Katana is larger, has two languages in the tree, a runtime that ships and a mounted
  corpus, so the share rises.
- **Held by:** `docs/AGENT.md` and `test/MetricsTest.java`; the kernel map (`docs/KERNEL-MAP.md`);
  from 2026-10-09, `scripts/prepush.sh`: when more than sixteen commits have landed since the last
  commit titled "Tithe" (two cycles' grace), the gate refuses a non-tithe push, and says so; a deferral
  is a decision recorded here with the commit it runs to, which the gate reads. (Amended 2026-10-09,
  J-154: the metrics tool had said "one is due" on every commit since tithe pass 2, seventy-three
  times, and stopped nothing, R-056's fourth case in the one place R-056 did not reach; a cadence that
  only warns is a cadence that is kept while there is slack and dropped exactly when it is needed.
  Nine tithe commits were owed at the amendment; they are paid as tithe pass 3a before the next
  feature, and the counter restarts at its close.)
- **Source:** Ghost, 2026-10-04; the gate on Opus's report, decided by Fable, 2026-10-09.
- **Status:** in force
- **See also:** R-040 (a tithe lands like any step), R-042 (when a metric arms), R-056
- **subject:** process

### R-009: The fixture schema
- **Decision:** A job fixture is the text format `docs/FIXTURE.md` defines and `oracle/fixture/`
  checks (FIXTURE-01 to 08): addressed by the SHA-256 of its canonical text, each starting dataset
  held by the SHA-256 of its logical records, valid for one profile named by hash. A clock pinned
  to the second alone is refused like no clock (FIXTURE-03); a run that must use such a clock (phase
  1.6's check, which shows the gate failing on it) sets it outside the fixture. A fixture states the
  JCL as written; a run that departs from it is a second fixture beside it with each departure
  commented (TRANREPT: `TRANREPT-STUDY.fixture`, the study's dates).
- **Why:** F-02: hundredths of a second leaked into TRANFILE column 326 when the clock was pinned to
  the second, so a seconds-only clock is unpinned in effect, and refusing it before a run is cheaper
  than a gate failure after two; the gate still proves itself on one (J-030). The study's TRANREPT
  run used dates the JCL does not, and its hashes are the ones phase 1.4 onward reproduce (J-031);
  keeping the JCL's fixture and the study's side by side loses neither.
- **Held by:** `docs/FIXTURE.md`; `test/katana/oracle/fixture/FixtureTest.java`, `FixtureDocTest.java`.
- **Source:** Opus drafted the schema in phase 1.3 and raised both choices; decided by Ghost,
  2026-10-04. The profile's IBM option fields (J-029) are with Ghost and Fable, not part of this.
- **Status:** in force
- **See also:** R-012 (the double-run gate, reserved), R-048
- **subject:** oracle-fixture

### R-010: DFSORT steps run on Katana's own Java sort
- **Decision:** A SORT step in a job runs on `oracle/sort`, a Java implementation of the DFSORT
  control-card subset Katana supports (SORT FIELDS, INCLUDE/OMIT, SUM, INREC/OUTREC/OUTFIL as needed,
  concatenated SORTIN), reading and writing the fixture's datasets by their declared record formats.
  The generated-COBOL-SORT variant of the study (`sortstep.cbl`) is retired. An unsupported card
  stops the step with a finding quoting the card (never a guess).
- **Why:** Phase 1.5 measured both on TRANREPT-STUDY: both reproduce the frozen hashes; the Java sort
  runs all five CardDemo SORT steps' cards (228 ms for the job), the generated COBOL sort two of five
  (334 ms, no OUTREC, no concatenation). The Java sort is also the one that can carry DFSORT's
  documented padding, truncation and stability rules (R-026, R-051) as code rather than as COBOL
  emitted around them.
- **Held by:** `oracle/sort`; the CardDemo SORT steps' frozen hashes; the finding on an unsupported
  card.
- **Source:** Opus's measurement and lean, 2026-10-04; decided by Ghost with Reviewer G.
- **Status:** in force
- **See also:** R-026, R-048, R-051
- **subject:** sort

### R-011: What the comparator normalizes, and what it refuses to
- **Decision:** The comparator normalizes text encoding (each side decoded by its declared
  encoding), the sign-zone scheme of zoned numbers, and framing (RDW, line terminators), always under
  the copybook, and masks only what the fixture declares, listing each mask applied. It never
  normalizes packed or binary bytes, record counts, record order (collation), rounding or
  truncation. A difference is grouped by field. Two zoned values that decode equal from different
  sign nibbles within one scheme (C against F on a signed field; C or D on an unsigned one) are
  **not a difference under NUMPROC(NOPFD)** and are counted as a `sign-variant` note per field, so
  they stay visible; under a profile with `ibm.numproc=PFD` they **are a difference**, because a
  PFD-compiled consumer distinguishes them (the NUMERIC test respects the PICTURE, R-049).
- **Why:** The study's encoding rule (`docs/research/oracle.md`), as built in 1.7, with the open
  question answered by the profile: whether a sign nibble matters is a property of the consumer's
  compile options, which the profile carries.
- **Claims:** C-1 the encoding rule separates representation from semantics: `verified` (the
  study's probes: `iconv` corrupts COMP-3; GnuCOBOL and IBM overpunch differ). C-2 PFD distinguishes
  C from F on a NUMERIC test: `verified` (APAR PM02897, `docs/research/ibm-options.md`).
- **Held by:** `oracle/compare/Compare.java`; `CompareTest`; the `sign-variant` count in the report.
- **Source:** built by Opus in 1.7 (J-053); decided by Fable at the phase-1 boundary, 2026-10-04.
- **Status:** in force
- **See also:** R-049, R-013
- **subject:** oracle-compare

### R-012: The double-run gate
- **Decision:** The cobc lane runs a fixture's job twice, each in its own folder, and nothing is
  compared against the original until the two runs are identical on every dataset's logical unload,
  **every SYSOUT DD**, every program step's stdout, **every step's stderr with the run folder's path
  replaced by a token**, and every step's outcome; a difference is reported by dataset, the DDs that
  reach it, records and columns. A clock not pinned to hundredths and zone is refused before the run
  (R-009); the gate is what catches what a fixture cannot state.
- **Why:** As built in 1.6, with the two open points reversed: a SYSOUT DD is a dataset the job
  writes (LANE-01 keeps a file for it), so it is held like one; stderr is where libcob says what went
  wrong, and a message that differs between two identical runs is exactly a nondeterminism the gate
  exists to catch, once the one difference the lane itself causes (the folder path) is removed. If
  stderr proves noisy in practice, the noise is named and masked by declaration (R-011), never
  dropped wholesale.
- **Claims:** C-1 two runs of a pinned fixture are identical: `verified` (CardDemo, F-02, 1.6).
  C-2 stderr is deterministic after path normalization: `verified` for the CardDemo fixtures (the
  trial, J-059: all nine through the gate with SYSOUT DDs and stderr held, no difference; CREASTMT's
  SIGSEGV message is the one stderr written).
- **Held by:** `oracle/lane/Gate.java`; `GateTest` (SYSOUT DDs and stderr held, the path a token);
  `GateIntegrationTest` (the seconds-only clock fails it).
- **Source:** built by Opus in 1.6 (J-052); decided by Fable at the phase-1 boundary, 2026-10-04,
  reversing the default on SYSOUT and stderr.
- **Status:** in force
- **See also:** R-009, R-011
- **subject:** oracle-gate

### R-013: What a proof covers, and what it never covers
- **Decision:** `docs/PROOF.md` as drafted in 1.9, with two lines added to "What it never covers":
  **the compiler's own semantics beyond GnuCOBOL's reading of them** (until a customer's mainframe
  gold reconciles the lanes on the same fixture, a proof says "equivalent under profile X as GnuCOBOL
  reads it", risk R-A); and **intent**: a proof says what the program does on the fixture's data,
  never what it was meant to do. Every proof report quotes the section verbatim (PROOF-01).
- **Why:** A buyer and an auditor read the same limits the code is held to (the TSB, NYT and Phoenix
  lessons, `docs/research/literature_and_failures.md`); the two additions are the two ways a proof is
  most likely to be over-read. "Intent" belongs here because the Intent IR (R-007) is where meaning is
  inferred and confirmed, and a proof must not be mistaken for it.
- **Claims:** C-1 these limits are the ones documented failures fell outside: `verified` (the
  literature note). C-2 GnuCOBOL's `-std=ibm` differs from IBM in measurable places: `verified`
  (`oracle/nist/IBM-CHANGES.md`, 1.2).
- **Held by:** `docs/PROOF.md`; `ProofReportTest` (PROOF-01); the two report goldens, re-recorded.
- **Source:** drafted by Opus (J-056); the additions by Fable; decided by Ghost, 2026-10-04.
- **Status:** in force
- **See also:** R-A (roadmap), R-007, R-049
- **subject:** proof

### R-015: che4z's EPL-2.0 grammars stay out; CICS and SQL command syntax is Katana's own when needed
- **Decision:** The che4z CICS and DB2 grammars (EPL-2.0) do not enter the tree. No counsel is
  anticipated; the licensing question is parked indefinitely rather than guessed. Their measurement
  (R-055: every EXEC block in CardDemo and BankDemo parses) stands as evidence that a command-level
  parse is feasible. When phase 7's facades need `EXEC CICS` and `EXEC SQL` as command trees rather
  than opaque text, Katana writes its own command grammar from IBM's published syntax (the CICS
  Application Programming Reference and the Db2 SQL Reference), the way the NIST harness was written
  from NIST's guide (R-044): a clean room, not a graft. If the project reaches a partner's or IBM's
  review, the question may be reopened with their counsel.
- **Why:** Ghost, 2026-10-05: no lawyer is available and none is expected; a license question that
  cannot be answered is not answered by proceeding. The clean-room path costs more work in phase 7
  and nothing before it (R-005), and it keeps the compiler's provenance uniform: every grammar in
  the tree is MIT (ProLeap's fork) or Katana's.
- **Claims:** C-1 IBM's command syntax for CICS and SQL is publicly documented in enough detail to
  write a parser from: `hypothesis`; phase 7.2's first step is the trial. C-2 nothing before phase 7
  needs a command tree: `verified` (R-005, R-055 C-3).
- **Held by:** `third_party/MANIFEST.md` (no EPL entry); docs/ROADMAP.md 2.2 and 7.2.
- **Source:** Ghost, 2026-10-05.
- **Status:** in force
- **See also:** R-002, R-014, R-044, R-045, R-055
- **subject:** license

### R-016: The indicator column is read permissively; a tab is expanded to the next 8-column stop, as a finding
- **Decision:** Column 7 accepts any indicator character GnuCOBOL or Enterprise COBOL accepts. A tab
  anywhere in a source or copy line is expanded to spaces to the next tab stop at columns 9, 17, 25 ...
  (every 8, from column 1) before columns are mapped, **unless the fixture declares a tab width for
  the source** (`tabs <width> <path-pattern>`; amended 2026-10-04, below); the source map keeps the
  original column; a finding records each file with tabs and the width used ("tab in source,
  expanded at N columns; a hypothesis until the Enterprise COBOL rule is read").
  The cobc lane preprocesses copybooks and sources the same way into a shadow copy, so the two lanes
  compile identical text. The 8-column assumption is a hypothesis until the Enterprise COBOL text on
  tabs is read; the finding says so.
- **Why:** CardDemo's `CUSTREC.cpy` carries tabs in the indicator area and `cobc` rejects it (the
  study); CREASTMT's full run, moved into 1.5 by R-048, cannot close without a policy. Two lanes that
  see different text prove nothing about each other, so the expansion is done once, upstream of both.
- **Amended 2026-10-04 (J-044):** at 8-column stops every tabbed line of CardDemo's `CUSTREC.cpy`
  runs past column 72 and CBSTM03A does not compile; at 4 it compiles. The width a file was written
  at is a property of that file, not of COBOL, so the fixture declares it per source pattern, default
  8; CardDemo's fixtures declare 4 for `CUSTREC.cpy`, with "compiles at 4, not at 8" recorded as the
  evidence and the hypothesis. The expansion still happens once, upstream of both lanes.
- **Held by:** `front/` (phase 2) and the cobc lane's preprocessing step; `docs/FIXTURE.md` (`tabs`);
  a fixture test on `CUSTREC.cpy`; the finding.
- **Source:** Opus raised it as a 1.5 prerequisite; decided by Ghost with Reviewer G, 2026-10-04;
  amended the same day on Opus's measurement.
- **Status:** in force
- **See also:** R-048, R-049, P-03
- **subject:** front-end

### R-017: The IR is complete: a lowering consults nothing but it, held by "no token left behind"
- **Decision:** The Semantic IR carries everything a lowering needs; `lower/` reads `sir/` and the
  profile and nothing else, never the parse tree, and decides no semantics (a keyword's meaning is
  the IR's to state, the lowering's to emit). Held by a mechanical test: for every statement
  subtree, every terminal outside a stated noise list appears in the IR as verb, role, keyword,
  operator or literal; on a probe program in the fast loop, on NIST in integration. Arithmetic and
  conditions are operator trees over a fixed vocabulary, not grammar-rule shapes. `Origin` is an IR
  type. Literals carry a kind and a decoded payload. The role vocabulary is pinned by a golden.
- **Why:** The phase-2 boundary review (J-073) found `ROUNDED` nowhere in `sir/`, EVALUATE's WHEN
  groups collapsed, INSPECT's ALL and LEADING as one role, and expressions stored as ProLeap's rule
  names, while every check passed, because the round-trip and contract checks compared the IR with
  itself. A lowering built on that IR would have had to read the tree or decide semantics, which
  MOD-03 forbids; the reviewer's estimate was six extra weeks in phase 3. The test is the one that
  cannot be always green: it reads the tree the IR was built from and asks what was left behind.
- **Claims:** C-1 keyword alternatives are where the loss is: `verified` (the probe, J-073; 37
  `(KEYWORD | KEYWORD)` alternatives in the procedure rules). C-2 the data map side is complete:
  `verified` (12,881 of 12,893 NIST fields placed as `cobc` places them, the rest explained). C-3 the
  no-token test is sufficient for completeness: `hypothesis`; phase 3.3's lowering of NIST without
  consulting the tree is the trial.
- **Held by:** the no-token-left-behind test (`test/katana/sir/NoTokenLeftTest.java` on COPYREC, the
  probe `sir/golden/PROBE.cbl` and the NIST goldens, with its own failing case; `IrIntegrationTest`'s
  `tokens-left` on the corpus, all 0); `sir/roles.golden`; per-program contract goldens
  (`sir/golden/`, `IrGoldenTest`); `ModulesTest` (`lower/` imports `sir` only).
- **Source:** the phase-2 boundary review, 2026-10-05; decided by Fable at the boundary, for Ghost.
- **Status:** in force
- **See also:** R-007, R-018, R-024, P-11
- **subject:** semantic-ir

### R-019: The literal Java is a byte-image machine; no COBOL item is ever a Java scalar in it
- **Decision:** In the literal lowering, every 01-level is a `Record` (a `byte[]` of its size), every
  data item a `Field` (a view: record, offset, length, and the IR's `Attributes` tuple, R-018),
  REDEFINES two views over the same bytes, a group MOVE a byte copy, an elementary MOVE a conversion
  through the attribute. Bytes are kept in the program's declared encoding (EBCDIC when the data is);
  conversion happens only at DISPLAY and at the lane's declared boundaries. Nothing in the literal is
  a Java `int`, `String` or `BigDecimal` field.
- **Why:** The semantic catalogue (`docs/research/semantic_traps.md`): REDEFINES, group moves, sign
  nibbles, ODO, collation and HIGH-VALUES are byte facts, and every attempt to type them away is where
  translators go wrong (Terekhov and Verhoef's "alien code"). GnuCOBOL's libcob and opensourcecobol4j's
  libcobj are this design and pass NIST (F-06). Passes may promote a Field to a scalar later, under
  proof (R-032).
- **Claims:** C-1 the byte-image design is sufficient for NIST: `verified` (opensourcecobol4j, 8,804
  of 8,918). C-2 a Field can be built from the IR's attribute tuple with no re-parsing: `verified`
  (tithe pass 2 A4: edit masks and flags as data, 750 masks matching cobc).
- **Held by:** `rt/Record`, `rt/Field`; `ModulesTest` (`rt/` imports nothing); the autotests through
  the java lane.
- **Source:** docs/ROADMAP.md phase 3 and §3.1; decided by Fable, 2026-10-05, so `rt/` could start.
- **Status:** in force
- **See also:** R-017, R-018, R-020, R-024
- **subject:** runtime

### R-020: Decimal is BigInteger plus scale, driven by an ArithProfile; MathContext is forbidden
- **Decision:** `rt/Decimal` is an unscaled `BigInteger` and an `int` scale; arithmetic is evaluated
  operation by operation under an `ArithProfile` that decides the intermediate's digits and scale
  (`IBM_COMPAT` 18/30, `IBM_EXTEND` 31/31, `EXACT` unbounded) and a `store()` that applies ROUNDED
  mode, high-order truncation by PICTURE or by binary width per `TruncMode`, SIZE ERROR
  keep-on-overflow, and division by zero as a status, never an exception. `java.math.MathContext`
  (significant-digit precision) is never used: COBOL's limits are integer digits plus decimal digits.
  The IBM profiles' intermediate rules are IBM's Appendix A, read 2026-10-05 (pages 793–801 of the
  6.4 Programming Guide, quoted in `docs/research/ibm-options.md` §4) and held as a table in
  `rt/ArithProfile` whose test checks every row; `IBM_COMPAT` and `IBM_EXTEND` build and are results,
  not findings. (Amended the same day: as first written, the IBM profiles were a finding "until
  Appendix A is read"; it was read within hours of the network opening, and the recollection it
  replaced was wrong in two rules: past the cap IBM keeps decimal places and drops high-order integer
  places when d ≤ dmax, the reverse of the recollection; in division op1 is the divisor, the
  recollection had the operands swapped.) The profile is pluggable so that a correction changes one
  table, not the runtime.
- **Why:** The catalogue's trap 4: `1/3*300` into `999V99` is 99.00 on IBM and 99.99 exact; a
  `MathContext` rounds by significant digits and matches neither. The shape is settled by the
  catalogue; only the IBM table waits on IBM's text, which this environment cannot reach (J-094).
- **Claims:** C-1 BigInteger+scale with explicit store rules reproduces libcob's observable behaviour
  on the autotests: `hypothesis`; the 378 runnable autotests through the java lane are the trial.
  C-2 the IBM intermediate-result rules: `verified` (Appendix A read and quoted, §4; `ArithProfile`'s
  table test; the reproducer `1/3*300` into `999V99` = 99.00 matching `cobc -std=ibm`). The two
  questions IBM's text left open were settled by trial (J-097): places an intermediate cannot carry
  are truncated (`verified` on cobc, with data items, never literals); ROUNDED's extra place is carried
  on the last operation only (`ArithProfile.last`): `verified` on cobc for data items (J-142: three
  candidate rules and twelve expected values written before the run, cobc gave this rule's value in
  all twelve); twice before it was marked verified on cases that could not separate the readings
  (J-097, J-099), and the status is granted now only because the trial named its rivals first. With
  a literal operand cobc carries more (`A / T + 1` is 2.7 where `A / T + ONE` is 2.6, D-023); the
  lowering counts a literal as Appendix A counts it, like any operand. IBM's "might be carried" (PG
  p. 794) is the open text and a mainframe settles IBM. C-5 cobc folds an all-literal
  expression at compile time with more places than Appendix A gives: `verified` on cobc, a finding
  against its dialect, not a rule Katana follows (the lowering applies the table to literals too).
  C-6 the cap rows (an intermediate past 30 digits): `hypothesis`, text-verified and oracle-absent.
  cobc's ibm dialect does not apply the cap (J-101: 7 of 40 cases past it match the uncapped model),
  so the only evidence is IBM's text and a model written from that text by the agent that wrote `rt/`;
  a second reading of one source is not a second source. A lowering whose program can reach the cap
  (any product or chain whose theoretical places exceed 30) says so as a finding, "arithmetic past the
  intermediate cap: proven on IBM's text, not on an IBM run", and the proof report carries it (P-03).
  A mainframe run settles C-6 and the ROUNDED `// Unsure` together; nothing else does.
- **Held by:** `rt/Decimal`, `rt/ArithProfile`; a test that `MathContext` is not imported anywhere in
  `rt/`; the finding on `IBM_*` until R-049 closes.
- **Source:** the catalogue; decided by Fable, 2026-10-05.
- **Status:** in force
- **See also:** R-019, R-022, R-049
- **subject:** runtime

### R-021: Control flow in the literal is a dispatcher with a PERFORM continuation table
- **Decision:** A program's procedure division lowers to `for (;;) switch (pc)`, one case per
  stretch of statements (a paragraph begins a stretch; a stretch is cut where the JVM's 64 KB method
  limit requires, each stretch's body a method the switch calls, which is a mechanical cut and not
  structuring; amended 2026-10-06 from "one case per paragraph"), textual fall-through as the next case, GO TO as a `pc` assignment, PERFORM as a push of
  the continuation (the pc after the range's end) and a jump, the range's end checked against the
  continuation stack on every paragraph exit (IBM's `perform-osvs` return: reaching any active range's
  exit returns), ALTER as a table update with a finding, EXIT PARAGRAPH/SECTION, STOP RUN and GOBACK
  as the lane's return, declaratives as their own cases. No paragraph is a Java method in the
  literal; structuring is a pass (R-032 to R-034).
- **Why:** Ceccato et al. (CSMR 2008): the Böhm–Jacopini dispatcher removed 79% of 531k GOTOs in 22 s
  on 8 MLOC with the program still readable, where full structuring produced unmaintainable nesting.
  A dispatcher is exact by construction for every PERFORM/GO TO shape NIST has (NC and IC pass through
  it in libcob's equivalent), and the proof of a later structuring pass is a diff against it.
- **Claims:** C-1 the dispatcher reproduces IBM PERFORM return semantics: `verified` against
  `cobc -std=ibm` (J-113: 81 programs equal, the ANSI reading differing on 21), `hypothesis` on IBM
  itself, whose text says control "must not" pass through another active PERFORM's exit (LR p. 414)
  and so does not settle the reading; a mainframe run settles it. C-2 recursive and overlapping
  PERFORM are findings, not lowered: `verified` (NE-02, NE-03 in `sir/check`). C-3 the shape C-1
  leaves open, control reaching an active PERFORM's exit from outside its range (a GO TO out of an
  inner range and back), is a finding the lowering raises, as NE-02 and NE-03 are: decided
  2026-10-06; the code is Opus's to add to `docs/FINDINGS.md` with its check.
- **Held by:** `rt/Dispatcher`; the NC/IC goldens; the findings.
- **Source:** the catalogue and the literature note; decided by Fable, 2026-10-05.
- **Status:** in force
- **See also:** R-019, R-033, R-034
- **subject:** runtime

### R-022: TRUNC(OPT) is lowered as TRUNC(STD), with a finding
- **Decision:** A program whose profile says `ibm.trunc=OPT` is lowered with STD semantics (truncate
  binary items to their PICTURE digits on store), and the build reports a finding on every binary
  item that could exceed its PICTURE: under OPT the original's result "is dependent on the particular
  code sequence generated" (IBM, `docs/research/ibm-options.md` §1), so no lowering can be proven
  against it, and STD is the only defensible golden.
- **Why:** IBM's own text says OPT results are unpredictable out of range; a proof under OPT would be
  a proof of nothing. The finding tells the customer which items to look at.
- **Claims:** C-1 IBM's OPT wording: `verified` (§1, verbatim). C-2 STD is what most OPT programs
  mean in practice: `hypothesis`; a customer's mainframe gold on an OPT program is the trial.
- **Held by:** `lower/` and `rt/store()`; the finding; `docs/PROFILE.md`.
- **Source:** the catalogue; decided by Fable, 2026-10-05.
- **Status:** in force
- **See also:** R-020, R-049, P-03
- **subject:** runtime

### R-023: COMP-1 and COMP-2 are lowered as IEEE binary float, with a finding
- **Decision:** Floating-point items lower to Java `float`/`double` (IEEE 754) and every statement
  that reads or writes one carries a finding: on z/OS the original is hexadecimal floating point, and
  bit-for-bit equivalence is not claimed. Mixed fixed/float expressions follow the profile's rule
  (hypothesis until Appendix A).
- **Why:** HFP emulation is a project of its own for a usage that CardDemo and the realistic corpus do
  not contain (F-04 feature counts); the honest output is a declared approximation (P-03).
- **Claims:** C-1 the realistic corpus has no COMP-1/COMP-2: `verified` (the scan). C-2 IEEE is close
  enough for every NIST float test: `hypothesis`; NIST's float cases through the java lane are the
  trial, and a failing one is listed in the reconciliation.
- **Held by:** `rt/Field` (float kinds); the finding NE-06.
- **Source:** the catalogue; decided by Fable, 2026-10-05.
- **Status:** in force
- **See also:** R-020, P-03
- **subject:** runtime

### R-024: One emitter writes every line of Java; passes never write Java directly
- **Decision:** `lower/Emit` is the only code in Katana that produces Java source text. It writes one
  provenance comment per statement (`// <file>:<line> <paragraph> <verb>`, from the IR's `Origin`),
  the manifest with the profile hash, and the class skeleton. `passes/` transform the IR-and-Java
  structure and call the same emitter, so a pass cannot drop a provenance comment or a manifest line.
  The lowered-Java golden format is byte-identical files under `lower/golden/` (P-02).
- **Why:** Provenance is the product (R-007); a second place that writes Java is a place that loses it.
  Suite Builder's one-emitter rule for its Java kit (G-108) is the precedent.
- **Claims:** C-1 every lowered statement carries its Origin: `verified` by construction once A2
  landed (Origin is an IR type). C-2 passes can be expressed as transformations the emitter then
  prints: `hypothesis`; phase 6's first pass is the trial.
- **Held by:** `ModulesTest` (`passes/` imports `lower/`'s emitter and no other Java-writing code);
  a test that no file outside `lower/Emit` writes a `.java`; `lower/golden/`.
- **Source:** docs/ROADMAP.md 3.3; decided by Fable, 2026-10-05.
- **Status:** in force
- **See also:** R-007, R-017, R-019
- **subject:** lowering

### R-026: Katana's sort is stable; an unordered tie in the original is a finding
- **Decision:** `oracle/sort` is stable (records with equal keys keep input order, DFSORT's
  `EQUALS`). When a SORT step's cards do not specify `EQUALS` and the input holds records with equal
  keys, the step records a finding: the mainframe's order among those records is unspecified, so a
  byte comparison of that dataset against mainframe gold may differ there without being wrong, and
  the comparator is told which records.
- **Why:** DFSORT does not guarantee stability without `EQUALS` (the study, `docs/research/oracle.md`);
  GnuCOBOL's SORT proved stable in the study. A deterministic lane needs one order; a proof needs to
  know where that order was the lane's choice rather than the program's.
- **Held by:** `oracle/sort`; the finding; the comparator's tie list.
- **Source:** the study (2026-10-04); decided by Ghost with Reviewer G.
- **Status:** in force
- **See also:** R-010, R-013
- **subject:** sort

### R-027: The facades: LE and assembler stubs, and a z/OS storage facade in the java lane only
- **Decision:** Programs call things the lanes must stand in for. LE routines (`CEE3ABD`) and
  assembler callees are stubs with recorded behaviour, in both lanes, declared by the fixture. A
  program that reads z/OS control blocks through storage (CardDemo's CBSTM03A: PSA at address 0 →
  TCB → TIOT, to DISPLAY the job, step and DD names) is **outside the cobc lane**: the lane reports
  its abend (`abend SIGSEGV`) as the step's outcome, the golden holds it, and a finding says why. Its
  oracle is the **java lane**, whose runtime (`rt/`, phase 3) answers those storage reads from the
  fixture's job and DDs through a z/OS storage facade beside the LE facade. No source overlay: the
  two lanes must prove the same program.
- **Why:** No `cobc` run can map address 0 without root and a non-portable kernel setting; an
  overlay changes the program under test. A runtime that already knows the job and the DDs can answer
  the question the program is asking. Programs that read control blocks are a real shape in the wild
  and worth a facade, not a workaround.
- **Claims:** C-1 CBSTM03A's reads are PSA→TCB→TIOT only: `verified` (its lines 266–290, J-050).
  C-2 `rt/` can answer them from the fixture: `hypothesis`; phase 3's CREASTMT end-to-end is the
  trial.
- **Held by:** the fixture's `stubs`; `oracle/lane` (abend as outcome, a finding naming R-027); `rt/` (phase 3).
- **Source:** Opus's three options and lean (J-050); decided by Fable at the phase-1 boundary,
  2026-10-04, for the lean.
- **Status:** in force
- **See also:** R-005, R-048, P-03
- **subject:** facades

### R-037: Patent protection is intended; nothing public before a provisional is filed
- **Decision:** Ghost intends to seek patent protection. Candidate claims: per-pass translation
  validation for legacy migration with an append-only provenance ledger; the double-run determinism
  gate before any comparison; the copybook-driven comparator that normalizes representation and
  refuses to normalize semantics. No public disclosure (public repository, post, talk, the phase-9
  publication) before a provisional application is on file; the full application within its twelve
  months. Demonstrations before then are under NDA or an evaluation license.
- **Why:** Public disclosure before filing forfeits patentability outside the US and starts the
  one-year US clock. Prior art to distinguish: Pnueli's translation validation (1998), Alive2 (PLDI
  2021), Google Dual Run, Mechanical Orchard's byte-for-byte gate; the combination applied to legacy
  migration with a provenance ledger is what is novel (docs/research/literature_and_failures.md).
- **Held by:** docs/ROADMAP.md phase 9.0 (gate); the repository's visibility; counsel.
- **Source:** Ghost, 2026-10-04. Not legal advice.
- **Status:** in force
- **See also:** R-002
- **subject:** disclosure

### R-038: Doc tests are plain Java 21, no dependencies, until Gradle arrives
- **Decision:** The phase-0 document tests (`ghost`, `glossary`, `docs`, `modules`, `todo`) are plain
  Java 21 classes under `test/` with shared helpers (`Check.java`, `Json.java`), compiled together and
  run by `test/run.sh`; the exit code is the verdict; no dependency. Phase 0.3 moves them under
  Gradle and JUnit with their bodies unchanged.
- **Why:** They are needed before the build system exists (phase 0.2 precedes 0.3), and a dependency
  taken for a test is a dependency the offline build must vendor. The first wording ("single-file
  programs run with `java test/<Name>.java`") was wrong: a Java single-file program cannot use a
  second source file, so each test would have carried its own copy of the helpers, against the
  "duplicate helpers: 0" metric (docs/AGENT.md).
- **Held by:** `test/DocTests.java`, run by `gradle test` since phase 0.3.
- **Source:** Opus, during phase 0.2; wording corrected by Opus; confirmed by Ghost, 2026-10-04.
- **Status:** in force
- **See also:** R-001, R-041 (where the tests went after 0.3)
- **subject:** toolchain

### R-039: Glossary aliases are enforced only when marked
- **Decision:** In `docs/GLOSSARY.md`, an alias in backticks is enforced: `test/glossary.test` fails
  when it appears in a document outside the glossary and `docs/research/`. An alias without backticks
  is a review guide and no test judges it. "Golden" is a term and is not an alias of anything.
- **Why:** The first rule ("a listed alias appearing in a doc outside the glossary fails") gave 60+
  failures on the day's documents, most of them words that are wrong for one concept and right for
  another: "run" is an execution, "source" is a source map, "error" is the tool failing, "map" is the
  provenance map. A test that fails on correct prose is a test that gets switched off (Suite Builder,
  G-107: rules that fired on good code became warnings). The study's notes under `docs/research/`
  were written before the glossary and keep their words.
- **Held by:** `test/GlossaryTest.java`; the glossary's own header.
- **Source:** Opus, during phase 0.2; decided by Ghost, 2026-10-04.
- **Status:** in force
- **See also:** R-008 (the tithe adds backticks as slips are caught)
- **subject:** glossary

### R-040: Each finished step lands on main
- **Decision:** Work happens on a branch; each roadmap step (or each commit an item is split into)
  is merged to `main` as soon as it is done and its tests pass, not held until the phase ends. Before
  merging, fetch `main`; if it moved, rebase onto it and run the tests again. `main` is never left
  with a half-done step: whatever is on it passes the fast loop and the doc tests.
- **Why:** Ghost, 2026-10-04: merging often lets a second agent be started on `main` to take one part
  of the work while the first goes on, and a branch that waits for a phase's end is a fork the second
  agent cannot stand on. Phase 0.2 sat on a branch while the plan was corrected on `main`
  twice, and needed two rebases.
- **Held by:** `CLAUDE.md` ("Before calling work done"); review.
- **Source:** Ghost, 2026-10-04; drafted by Opus at Ghost's request.
- **Status:** in force
- **See also:** R-008 (a tithe commit lands the same way)
- **subject:** process

### R-041: Gradle comes from the pinned image; no wrapper
- **Decision:** The build runs with the Gradle inside the image the `Dockerfile` pins by digest
  (8.14.3), invoked as `gradle`, not through a committed wrapper (`./gradlew`). `build.gradle` refuses
  any other Gradle version, so a build outside the image fails loudly instead of drifting.
- **Why:** The wrapper downloads its distribution from `services.gradle.org`, which redirects every
  download to `downloads.gradle.org`; the proxy the project works behind refuses that host (phase
  0.3, 2026-10-04, as the study found). The official `gradle:8.14.3-jdk21-noble` image carries the
  same Gradle with JDK 21 on Ubuntu 24.04 in one digest, so one pin replaces two. Where
  docs/ROADMAP.md says `./gradlew`, read `gradle` in the image.
- **Held by:** `build.gradle` (the version check); `Dockerfile` (the digest); CI.
- **Source:** Opus, during phase 0.3; decided by Ghost, 2026-10-04.
- **Status:** in force
- **See also:** R-001, R-038
- **subject:** toolchain

### R-042: Who writes theGhost.md; a metric arms when first met; tools live outside the modules
- **Decision:** Three housekeeping rules from phase 0.6. (a) Any agent may append a decision Ghost
  made and may add or correct `See also` and `Held by` lines on any entry; `Decision`, `Why` and
  `Status` text changes need Ghost. (b) A metric in `docs/AGENT.md` is *armed* the first time it is
  measured within its target; until then it is measured and reported, never ratcheted. "Docs lines per
  source line" (target < 0.35) arms when the first module carries real code (phase 2); the other nine
  armed in phase 0. (c) `scripts/` (the kernel-map and metrics tools) and `test/` are outside the
  modules; `modules.test` treats them as outside the product.
- **Why:** (a) `theGhost.md` is Ghost's file; the agent that proposed a decision and the agent that
  links it need no permission to keep its cross-references true, and the "decisions without See also"
  metric cannot be worked otherwise. (b) A ratchet on a metric that cannot be met before code exists
  can only ever report "worse", which trains agents to ignore it; the target is from Suite Builder at
  maturity and stays. (c) The tools that measure the product are not the product.
- **Held by:** this file's header; `test/MetricsTest.java` (an unarmed metric is reported with
  "not armed", not compared); `docs/modules.json` intro.
- **Source:** Opus raised all three during 0.6; decided by Ghost with Reviewer G, 2026-10-04.
- **Status:** in force
- **See also:** R-008, R-038
- **subject:** process

### R-043: A new module or import is a decision; an agent proposes it and stops
- **Decision:** Adding a module, or a new import between modules, changes the architecture
  (`docs/modules.json`'s set and docs/ROADMAP.md 3.3), so it is Ghost's to approve. An agent asked
  for one drafts the `modules.json` entry and the 3.3 line as a proposal (in its reply or on a branch
  that is not merged), says it is waiting on Ghost, and stops; it does not edit `main`. An agent that
  cannot tell whether the asker is Ghost says so and proposes. The fresh-agent trial is phrased the
  same way ("say what you would change, then stop") and is scored on navigation and on changing
  nothing.
- **Why:** The two phase-0 trials both read "add a module" as permission to add one; one changed
  3.3, which the adding line already named as Ghost's, and flagged that it could not tell who was
  asking. The ambiguity was the task's, not the agents'. A module set that any agent may grow is the
  Suite Builder Cypress mistake waiting to happen again (P-04).
- **Held by:** `docs/modules.json` "adding" line; the `CLAUDE.md` routing row for a module;
  docs/ROADMAP.md 0.7 and §5.
- **Source:** Opus's trial report, 2026-10-04; decided by Ghost with Reviewer G.
- **Status:** in force
- **See also:** R-007, R-042
- **subject:** architecture

### R-044: NIST in CI is Katana's own harness, written from NIST's public-domain material
- **Decision:** Phase 1.1 builds the NIST run in Java in `oracle/` (option B): extraction of the
  programs from `newcob.val`, the per-module runs under `cobc`, the reading of each program's report
  and the summary are Katana's code, written from NIST's public-domain material only (`newcob.val`,
  its EXEC85 program, the CCVS85 documentation) and from observed outputs (the study's run, the
  published totals `Total 426 426 0 0 9610 0 20 118 9748`). GnuCOBOL's NIST harness (`expand.pl`,
  `report.pl`, `summary.pl`, its Makefile templates) is neither copied nor read while writing it.
- **Why:** The oracle ships as source-available (R-002), so it cannot depend on GPL scripts; running
  GnuCOBOL's harness from a fetched tree (option A) would have met 1.1's check sooner and then had to
  be replaced. Writing it without reading the GPL scripts keeps the result clean of them.
- **Held by:** `oracle/` (no GPL text, no GPL logic); 1.1's check in CI.
- **Source:** Opus proposed during 1.1; decided by Ghost, 2026-10-04.
- **Status:** in force
- **See also:** R-002, R-003
- **subject:** oracle-lane

### R-045: GnuCOBOL and other GPL code stay off the screen while Katana's equivalent is written
- **Decision:** GnuCOBOL is two things to Katana: an oracle (its `cobc` binary in the pinned image,
  whose outputs are compared against) and a reference design (read once during the study and
  summarized in `docs/research/`). Its source code, its harness scripts and its expected-result files
  are never in this repository and never open on an agent's screen while the Katana code that does
  the same job is being written. The same holds for opensourcecobol4j and any other GPL code. What may
  stay: NIST's own material (public domain) and the study's run *outputs*, which are results, not
  code. `corpus/fetch.sh` fetches only GnuCOBOL's autotests (`tests/testsuite.src`, test input) and
  only in phase 3. JUnit 5 (EPL-2.0, unmodified, tests only, never shipped) is a different and
  lighter question from the che4z grammars (R-015: modified, shipped inside the compiler); both go to
  counsel; neither blocks phase 1.
- **Why:** R-044 made the NIST harness a clean-room rewrite from NIST's documentation. Opus deleted
  its local GnuCOBOL clone so nothing in it would be read during 1.1, and the same GPL scripts and
  expected-result files were sitting in `oracle/prototypes/nist/`, one directory over. A clean room
  with the reference on the shelf is not a clean room. Removed in the same commit as this decision:
  `expand.pl`, `report.pl`, `summary.pl`, the per-module `EXEC85.conf`, and GnuCOBOL's shipped
  `*.txt` expectations.
- **Held by:** `.gitignore` and `corpus/fetch.sh`; `oracle/prototypes/README.md`; `test/LicenseTest.java`
  (written in tithe pass 2, C4; named here since phase 1, missing until then), which fails on a file
  whose header names the GPL outside `corpus/` and `third_party/`.
- **Source:** Opus raised it in HANDOFF during 1.1b; decided by Ghost with Reviewer G, 2026-10-04.
- **Status:** in force
- **See also:** R-002, R-015, R-044
- **subject:** license

### R-046: The journal: every event recorded with fixed fields, by whoever was there
- **Decision:** `docs/JOURNAL.md` records every event in the work (a contradiction found, an
  ambiguity decided, a failure caught or missed, a change of direction, a measurement) as one entry
  with fixed fields: who, class, intent, found, options, resolved, touched, caught by, cost. It is
  not the commit log, not the status list and not the design; it is what happened while the work was
  done. Whoever was there writes the entry, an agent its own, in the same commit as the event where
  possible. Suite Builder keeps the same journal with the same fields (its G-162), so the two
  histories can be counted and compared.
- **Why:** Ghost, 2026-10-04: the methodology is being observed and formalized into standards, and a
  researcher needs to count events, not read prose spread across `theGhost.md`, `DONE.md` and commit
  messages. The journal's first thirteen entries are the day's own contradictions and their
  resolutions, each already cited in a decision; a record that exists only as decisions loses the
  failures that produced no decision (J-006, J-007, J-011).
- **Held by:** `docs/JOURNAL.md`; `test/JournalTest.java` (written in tithe pass 2, C4; named here
  since phase 1, missing until then): each id above every earlier one, every entry its Who and Class.
- **Source:** Ghost, 2026-10-04 (the brief to the Suite Builder session); recorded by Fable.
- **Status:** in force
- **See also:** R-008 (tithe), R-042 (who writes what)
- **subject:** process

### R-047: Katana's NIST golden means what NIST says, not what GnuCOBOL's harness counted
- **Decision:** The NIST golden records, per program, what the program's own footer reports under
  Katana's pinned profile. A program `cobc` cannot compile is a compile finding (P-03), not a pass; a
  test NIST marks "REQUIRE INSPECTION" is counted as inspection; a program reporting no tests is
  counted as none; DB103M's two runs are two results. The study's published total under GnuCOBOL's
  harness (F-01) is a cross-check: `oracle/nist/RECONCILIATION.md` names every program whose outcome
  differs and the reason, from a fixed list, and an unexplained difference fails the test. The 1.1
  check changes from "summary diff empty" to "every difference explained".
- **Why:** Opus, 1.1e: four of GnuCOBOL's harness conventions are visible only in the study's outputs
  and stated nowhere (twelve IF programs using the 1989 amendment's `FUNCTION MAX(IND(ALL))`, which no
  `cobc -std` accepts, credited with every test passed; inspection counted as pass; zero tests counted
  as one pass; DB103M summed). Reproducing them would reverse-engineer a GPL harness through its
  outputs (R-045) and bake undocumented choices into the oracle's foundation. From phase 3 the Java
  lane is compared against the cobc lane per program, so the golden's meaning must be the one written
  down, and NIST's is. "Every difference explained" is the stronger check: an empty diff can hide a
  wrong convention; a reconciliation line cannot.
- **Held by:** `oracle/nist/RECONCILIATION.md` and its test; docs/ROADMAP.md 1.1.
- **Source:** Opus raised it with both options and a lean, 2026-10-04; decided by Ghost with Reviewer
  G, for the option Opus leaned to.
- **Status:** in force
- **See also:** R-044, R-045, P-03
- **subject:** oracle-lane

### R-048: SORT steps run in phase 1.5, not 1.4
- **Decision:** The cobc lane (1.4) runs every step of a fixture except SORT, which stops the job with
  a finding naming phase 1.5. 1.4's check is POSTTRAN's frozen hashes, INTCALC through the PARM
  driver, and TRANREPT and CREASTMT up to their SORT steps. 1.5's check gains TRANREPT-STUDY's frozen
  hashes (`a7d14a96…` is the sort's own output) and CREASTMT end to end, with the `CUSTREC.cpy` tab
  policy (R-016) that CBSTM03A, after CREASTMT's SORT, needs.
- **Why:** TRANREPT and CREASTMT each have a SORT step, and the DFSORT subset is 1.5; a stopgap sort
  in 1.4 would be built twice, and TRANREPT is the job R-010 measures the two sort designs on.
- **Held by:** docs/ROADMAP.md 1.4 and 1.5; `TODO.md`.
- **Source:** Opus raised it at the start of 1.4 with a lean; decided by Ghost, 2026-10-04, for the
  option Opus leaned to.
- **Status:** in force
- **See also:** R-009, R-010 (reserved), R-016
- **subject:** roadmap

### R-049: The IBM compile options are profile fields now, each with a verification status
- **Decision:** The profile gains `ibm.trunc`, `ibm.numproc`, `ibm.arith`, `ibm.codepage`,
  `ibm.invdata` and `ibm.version`, with IBM's documented defaults (STD, NOPFD, COMPAT, 1140,
  NOINVDATA). They describe the source program's compilation on z/OS; the cobc lane cannot set them,
  the literal lowering reads them. Every semantic the lowering takes from one carries a status in
  `docs/PROFILE.md`: *verified* (quoted from a primary IBM source, `docs/research/ibm-options.md`) or
  *hypothesis*. A lowering that depends on a hypothesis says so in its findings and the proof report
  ("valid under hypothesis H-n"). The program collating sequence is not a field; it is read from the
  program. Appendix A (intermediate results) was wholly hypothesis when this was written and blocked
  phase 3.1; it was read on 2026-10-05 and is verified (R-020, amended).
  Two fields added 2026-10-06: `ibm.dispsign` (`COMPAT` default, or `SEP`; Programming Guide 6.4
  Table 48, read by Opus, `verified`: COMPAT overpunches a negative value's last digit and shows a
  zoned item's bytes as they are, SEP shows a leading separate sign; the cobc lane compares under SEP,
  with cobc's packed and binary departures as findings, J-109) and `ibm.wsinit` (R-057; first `ibm.wsclear`). Each row in
  `docs/PROFILE.md` is Opus's to add with its code, in this wording.
- **Why:** The study's container could not reach IBM's manuals at all (F-10). From this session the
  performance-tuning papers, the compiler-option tables, the Customization Guide and eleven APARs
  were reached and quoted: TRUNC, NUMPROC, ARITH, CODEPAGE and ZONEDATA/INVDATA semantics are now
  verified; the Programming Guide's Appendix A still is not (its PDF is reachable but too large for
  the fetch tool; its HTML topic id is unknown). Waiting for a complete reading would hold the
  profile hostage to one appendix; recording what is known with its status lets phase 1 and 2
  proceed and makes the open item visible in every proof until it closes.
- **Held by:** `docs/PROFILE.md` (the fields and the status table); `ProfileTest` (a profile states
  every field, PROFILE-02); the proof report's hypothesis line (phase 3).
- **Source:** Opus raised the fields as R-009's third choice (J-029); the sourcing by a research agent
  under Fable, 2026-10-04; decided by Ghost with Reviewer G.
- **Status:** in force
- **See also:** P-12, R-020, R-022, F-10
- **subject:** profile


### R-050: One CI run per change
- **Decision:** CI runs once for each change: on a push to `main`, where every step lands (R-040), or
  when started by hand. A push to a working branch and a pull request run nothing, since the commits
  they carry are the ones `main` gets. A second run of the same commit is overhead, not a check.
- **Why:** Every step was pushed to `main` and then to the working branch, and both pushes ran the
  whole workflow on the same commit; GitHub Actions minutes ran out on 2026-10-04 and return on
  2026-11-02. Until then a step's gate is the same image and commands run on a fresh clone before the
  push, with the results in its commit message (`docs/HANDOFF.md`).
- **Held by:** `.github/workflows/ci.yml` (its `on:`).
- **Source:** Ghost, 2026-10-05, as a directive; Opus found the duplicate runs.
- **Status:** in force
- **See also:** R-040, R-041
- **subject:** process

### R-051: A reformatted record that does not match SORTOUT's LRECL is padded or truncated as DFSORT does
- **Decision:** For fixed-length output, when the record `oracle/sort` builds is shorter than
  SORTOUT's LRECL it is padded on the right with binary zeros (X'00'); when longer, it is truncated
  on the right; the step continues with return code 0 and a finding naming the step, the two lengths
  and the records affected. The return code follows the fixture's `dfsort.pad` / `dfsort.trunc`
  setting (`RC0` default, `RC4`, or `RC16` which stops the step), as DFSORT's PAD/TRUNC options do.
- **Why:** IBM's DFSORT Application Programming Guide (z/OS 2.2, "Padding and truncation"), read
  from this session: "DFSORT pads fixed-length records with binary zeros on the right when the
  SORTOUT LRECL is larger than the SORTIN LRECL provided that: The Blockset technique is selected.
  The application is a sort or copy. PAD=RC16 is not in effect." and "DFSORT truncates fixed-length
  records on the right when the SORTOUT LRECL is smaller than the SORTIN/SORTINnn LRECL provided
  that: The application is not a conventional merge or tape work data set sort. TRUNC=RC16 is not in
  effect." PAD/TRUNC: "RC0 ... issue message ICE171I (once), set a return code of 0 and continue";
  RC4 the same with return code 4; RC16 "issue message ICE196A, terminate and give a return code of
  16"; default "usually the installation default". Two hypotheses remain and are stated in the
  finding: that the rule applies to the OUTREC/OUTFIL-reformatted length as it does to SORTIN's
  LRECL (the same page says INREC/OUTREC/OUTFIL "pad, truncate, and reformat records"), and that the
  IBM-supplied installation default is RC0. CREASTMT (328 into 350) therefore pads with X'00';
  PRTCATBL (41 into 40) truncates one byte; both run.
- **Held by:** `oracle/sort`; the finding; `docs/FIXTURE.md` (`dfsort.pad`, `dfsort.trunc`);
  `docs/research/ibm-options.md` (the quotes and their URLs).
- **Source:** Opus raised it (J-043); IBM text read by Fable; decided by Ghost with Reviewer G,
  2026-10-04.
- **Status:** in force
- **See also:** R-010, R-026, P-03
- **subject:** sort

### R-052: Claims carry a status; an uncertain claim is settled by a trial, not an argument
- **Decision:** Every decision from here on lists the factual claims it stands on under **Claims**,
  each tagged `verified` (a primary source quoted, or a run recorded) or `hypothesis` (with the
  source or trial that would settle it). A question raised during the work is answered in this order:
  (1) by a source, read; (2) by a **trial**: a fixture, test or measurement whose result is the
  answer, specified by whoever is uncertain (Fable when consulted, Opus otherwise), implemented and
  run by the implementing agent, recorded in the journal with class `trial`; (3) by argument, only for
  a design trade-off with no empirical referee, and then as a decision with its trade-off named.
  Fable is consulted as a source of answers and of trial designs, not as a debater; a trial is run
  only when the consulted answer is itself uncertain. `GhostTest` refuses a new decision with no
  Claims section (from R-053 on). A trial has two rules of its own (amended 2026-10-06, after R-020's
  C-2 was marked verified twice on cases that could not separate the readings, J-097 and J-099, and
  settled at the third attempt, J-142): **the rivals are named first**, every candidate reading
  written down with the value it predicts for each case before the run, so that the cases are chosen
  to tell the candidates apart and a case that gives the same value under every candidate is not
  evidence; and **no unexplained residue is left**: an observation that fits none of the candidates
  does not close the trial, it names a new candidate and gets its own cases (J-142's literal, D-023).
  A trial that ends with a residue is recorded `open`, not `verified`.
- **Why:** Ghost, 2026-10-04. Every decision that went well today rested on a claim that was checked
  (DFSORT's padding read; CUSTREC's tab width measured; the two sorts timed); every failure rested on
  one that was not ("I only changed documents", twice). Two models arguing converge on the plausible,
  not the true; a run converges on the true. Fable's budget is rationed by the week, so it answers
  questions and designs trials rather than arguing.
- **Claims:** C-1 the claim-status shape is usable: `verified`, R-049 and R-051 already carry it
  informally. C-2 a trial is cheaper than a debate: `hypothesis`; the study measures it as trials per
  phase against Fable minutes per phase in the journal.
- **Held by:** `GhostTest` (Claims required from R-053); `docs/JOURNAL.md` (class `trial`);
  `docs/ROADMAP.md` §4.6.
- **Source:** Ghost, 2026-10-04; recorded by Fable.
- **Status:** in force
- **See also:** R-046, R-049, R-051, P-11
- **subject:** process

### R-053: The implementing agent runs a phase to its end; decisions are classed, and only the irreversible stop it
- **Decision:** An implementing agent (Opus) works from a phase's first step to its last without
  stopping for a human, by classing each question it meets: **(a) its own** (an implementation
  choice inside a module's contract): decide, journal, continue. **(b) a trial's** (R-052): run it,
  decide on the result, journal, continue. **(c) Fable's** (a contract, an IR shape, a semantic rule,
  a reading of a source): write it under `docs/HANDOFF.md` "For Fable" with the options and a lean,
  take the **reversible default** (the option that can be switched by one small change, marked
  `// Unsure: R-053 default` in code and as a finding in any proof), and continue. **(d) Ghost's**
  (scope, license, name, money, a new module or import per R-043, anything a customer or outside
  party would see, anything that cannot be undone): write it under "For Ghost" the same way and
  continue on the reversible default **unless no reversible default exists**, in which case the
  agent stops that step, works on any step that does not depend on it, and stops the phase only when
  nothing independent remains. At the **phase boundary** (1.x → 2.x), before the next phase's first
  step: Fable answers the "For Fable" list and Ghost the "For Ghost" list; every default that is
  reversed is switched in its own commit with goldens proving the switch; the journal records each as
  class `decision` with "deferred from step N". The tithe pass at the boundary counts how many
  defaults held and how many were reversed.
- **Why:** Ghost, 2026-10-04: a chain of work that stops at every question spends more on waiting
  than on building, and most questions raised today were answered the way the agent had leaned. The
  cost of a wrong default is bounded by choosing the reversible option and marking it; the cost of
  stopping is unbounded. The irreversible stays a stop because that is the one case where a wrong
  guess cannot be bought back (ROADMAP's working rule, and Suite Builder's HANDOFF bar).
- **Claims:** C-1 most deferred questions are decided the way the agent leaned: `hypothesis`; the
  boundary tithe measures held-versus-reversed. C-2 a reversible default exists for most class (c)
  and (d) questions: `hypothesis`; the same count, with the stops recorded. C-3 switching a default
  under goldens is cheap: `hypothesis`; commits and minutes per reversal, journaled.
- **Held by:** `docs/HANDOFF.md` ("For Fable", "For Ghost" with the step each came from);
  `docs/ROADMAP.md` §4.6; the `// Unsure: R-053 default` marker that `metrics` lists; the boundary
  tithe's count.
- **Source:** Ghost, 2026-10-04; recorded by Fable.
- **Status:** in force
- **See also:** R-008, R-040, R-043, R-052
- **subject:** process

### R-054: The fast loop is measured warm; a metric also arms at the first boundary after real code
- **Decision:** (a) The fast-loop metric is the warm run: `gradle test --offline` with the daemon
  already running, which is what an agent waits after an edit; target under 20 s, measured 7 s. The
  cold run (daemon start included, 20 s) is reported beside it with its own target under 30 s,
  unarmed. (b) R-042 amended: a metric arms the first time it is within its target, **or** at the
  first phase boundary after the first module carries real code, at the value measured there, from
  which it may only improve. "Docs lines per source line" arms now at 0.45 (target 0.35).
- **Why:** Ghost, 2026-10-04: warm is what is paid, so warm is what is measured. On the docs ratio,
  the metric's job is pressure toward the target, and a metric that stays unarmed until it is met
  exerts none; arming at the measured value makes every document added pay for itself in code or in
  trimming.
- **Claims:** C-1 the tests themselves run under 4 s and the rest is Gradle: `verified` (J-057).
  C-2 the docs ratio falls as phase 2 lands code: `hypothesis`; the phase-2 boundary measures it.
- **Held by:** `docs/AGENT.md`; `MetricsTest` (warm ratcheted; cold reported; docs ratio armed at
  0.45).
- **Source:** Opus measured and listed the options (J-057); decided by Ghost, 2026-10-04.
- **Status:** in force
- **See also:** R-008, R-042
- **subject:** process

### R-055: Phase 2's six deferred questions, answered mid-phase; 2.2 is deferred, not stopped
- **Decision:** The six "For Fable" items of steps 2.1 to 2.5 hold on their defaults, with three
  wording changes to the roadmap: (a) a corpus's parse check is "every program `cobc` accepts, and
  every deliberately broken program named" (OMP is 29 plus CBL0007, whose error is the course's own
  exercise); (b) 2.1's "IBM dialect switch mirroring `ibm-strict.conf`" becomes "the dialect is the
  profile's (R-049) and 1.2's measured differences; the grammar accepts the union", because reading
  GnuCOBOL's configuration is what R-045 forbids; (c) 2.3's round trip is held from parsed text to
  source now and extended to lowered statements in 3.3. The NC124A edited-picture scale is Katana's
  (`$$,$$$.$$` has two decimal places: floating insertion after the point holds digits), listed in
  `sir/datamap.golden` as a cobc difference, with the trial Opus proposed (MOVE 12.34, DISPLAY, under
  the cobc lane) run and its result recorded in `IBM-CHANGES.md`. CICS names stay names; DFHEIBLK,
  SQLCA and the recalled DFHAID values (DFHPF13–24, DFHPA1–3) are `hypothesis` until read from IBM's
  published layouts, which happens when `sir/` first needs a field of them. **2.2 is reclassified
  from stopped to deferred:** its reversible default is that che4z's grammars stay out of the tree,
  the measurement (CardDemo 220/220 EXEC CICS and 32/32 EXEC SQL; BankDemo 351/351 and 120/120, from
  a clone outside the tree) is the evidence recorded for R-014, and EXEC blocks stay opaque tagged
  text in the IR, which is all phases 2 to 6 need under R-005. The graft lands when counsel clears
  R-015, as the first step of phase 7.2 or earlier.
- **Why:** None of the six changes behaviour; each corrects the plan's wording to what the corpus
  showed. On 2.2: R-053 says a step stops only when no reversible default exists, and one does; the
  stop was more caution than the rule asks, which is the right error to make and the one to correct.
- **Claims:** C-1 `$$,$$$.$$` has scale 2 under IBM and the standard: `verified` (the PICTURE
  rules: floating insertion symbols to the right of the decimal point are digit positions; cobc's
  own `+++++.++` gives scale 2). C-2 the recalled DFHAID values are IBM's: `hypothesis`; trial:
  compare against IBM's CICS documentation when reachable, or a second independent copybook.
  C-3 nothing in phases 2–6 needs EXEC blocks parsed: `verified` (R-005; the lowering emits them
  as facade calls in 7.2).
- **Held by:** docs/ROADMAP.md 2.1–2.3 wording; `sir/datamap.golden`; `oracle/nist/IBM-CHANGES.md`;
  `docs/HANDOFF.md`.
- **Source:** Opus listed (J-063 to J-068); answered by Fable mid-phase, 2026-10-05, while present.
- **Status:** in force
- **See also:** R-005, R-014, R-015, R-045, R-049, R-053
- **subject:** process

### R-056: A check names its oracle, carries the mutation that turns it red, and its verdict stops an action
- **Decision:** Every golden, integration and rule check in Katana declares three things, held by a
  meta-test: (a) **its oracle**: what the expected side is and where it comes from (`cobc -C`'s
  layout, NIST's reports, the grammar's tokens, a spec, a golden written by another process), and the
  meta-test refuses an oracle whose path lies inside the module under test; (b) **its killer
  mutation**: one change to the thing under test that must turn the check red (drop a contract line,
  shift an offset, remove a token, run zero tests), and the meta-test applies each mutation and fails
  if the check stays green; (c) **its consequence**: the action the verdict gates (the commit, the
  push, the merge), so a red verdict is never advisory. A test task that selected a suite and ran
  zero tests is red. Unit tests with literal expected values satisfy (a) and (b) by construction and
  declare only (c) through the task that runs them.
- **Why:** Four failures in two days with one shape. J-006: `gradle test` marked the doc tests up to
  date and zero runs counted as a pass (could not fail). J-073: the IR round trip compared the IR
  with itself and the contract count discarded its lines; every keyword alternative was lost while
  every check was green (could not fail). The CI with no minutes (R-050) was red on every commit and
  told no one anything (could not pass). J-040: the tests ran, the failure printed, and the push went
  ahead because the script did not gate on the exit code (the verdict stopped nothing). A check is
  worth what it can refute, and only when refuting stops something.
- **Claims:** C-1 the three cases that could not fail share the self-comparison or zero-run shape:
  `verified` (J-006, J-073, the IR golden's counts). C-2 one declared mutation per check is enough
  to catch the self-comparison shape: `hypothesis`; the meta-test's first run over the existing checks
  (tithe pass 2, A7) is the trial, and a check it cannot kill is a finding. C-3 (as first stated, "the oracle-path rule catches most non-independence"): **refuted** by the first
  trial (J-088): the rule fired 4 times, every hit an independent oracle that happens to live in the
  module's folder, and the 3 real mirrors were caught only because their entries declare `self`, one
  of them outside its module's folder. C-3 restated: independence is *declared*, not detected; a
  check's entry says `self` or names its oracle and why it is outside the code under test, and the
  path rule is a prompt ("say why") rather than a refusal. Whether a declaration is honest stays
  judgment (`docs/GHOST-IN-THE-KERNEL.md`). C-2 stands, narrowed: one declared mutation per check
  named every gap (6 of 18 checks have no failing case, each a finding in `docs/checks.json`) and
  closed none; closing them is phase-3 tithe work.
- **Held by:** `test/ChecksTest.java` over `docs/checks.json` (tithe pass 2, A7: every integration
  and golden test registered with its oracle, mutation test and gate; every pre-push suite selecting
  at least one test); `build.gradle`'s zero-run rule on every test task; `scripts/prepush.sh` and
  every commit script gating on the exit code (R-040).
- **Source:** Opus's analysis and draft at Ghost's request (2026-10-05, J-078); the fourth case
  added by Fable; decided by Ghost, recorded by Fable.
- **Status:** in force
- **See also:** R-017, R-040, R-050, R-052, P-11
- **subject:** checks

### R-057: An item without VALUE starts as INITIALIZE would set it, and a program that reads it first is not yet proven
- **Decision:** The literal gives an item that has no VALUE clause the value INITIALIZE would give it
  (zeros for numeric, spaces for alphanumeric, by PICTURE), which is what `cobc -std=ibm` does, so
  the two lanes agree and the oracle holds. IBM leaves such an item's initial value unpredictable
  (WSCLEAR, where used, clears WORKING-STORAGE to binary zeros, not to INITIALIZE's values), so a
  program that reads an item before writing it is not proven by the oracle: the finding for that
  read, "reads of uninitialized storage", is phase 5's (`docs/FINDINGS.md` already says so; it needs
  the Intent IR's data flow), and until it exists the proof report says in its header that initial
  values of items without VALUE are assumed, not proven. A profile field `ibm.wsinit` (`NONE`
  default, or the hex byte fresh storage is set to, `00` being the usual) records what the
  customer's original ran under, so that the finding, when it exists, can say what the original
  would have seen. (Amended 2026-10-06: first worded as `ibm.wsclear`, `NO`/`YES`, after AIX's
  WSCLEAR option; on z/OS that option does not exist, J-118, and the thing that sets fresh storage
  is Language Environment's STORAGE run-time option, whose first value is the byte heap storage is
  initialized to; one field with a byte covers both, and the lowering reads neither.)
- **Why:** The alternative, binary zeros as IBM's WSCLEAR gives, breaks the cobc oracle on every
  program that reads before writing, for no gain: neither value is what an unpredictable item held
  on the customer's machine. The oracle's agreement is worth keeping; the honesty is in the header
  until the finding can name the items.
- **Claims:** C-1 IBM: an item without VALUE has an unpredictable initial value: `verified` (LR 6.4 p. 103
  "undefined", p. 245 "unpredictable"; quoted in `docs/research/ibm-options.md` §9, J-118). C-2 (as first stated, "WSCLEAR clears
  WORKING-STORAGE to binary zeros"): **refuted for z/OS** (J-118: the z/OS Programming Guide 6.4
  names WSCLEAR once, p. 864, as AIX only). C-2 restated: on z/OS, WORKING-STORAGE without VALUE
  holds whatever Language Environment's STORAGE option set heap storage to (`STORAGE(00,...)` gives
  binary zeros; the default leaves it as found): `verified` for a program compiled with RENT (PG 6.4
  p. 40: a COBOL 6.1 or later RENT program has its WORKING-STORAGE "allocated from heap storage"; LE
  Programming Reference z/OS 3.1, SA38-0683-60, pp. 69-70: STORAGE's heap_alloc_value initializes
  "every byte of the allocated heap storage", the default NONE leaves it "uninitialized"; quoted in
  `docs/research/ibm-options.md` §9, J-120); a NORENT program's WORKING-STORAGE is not covered by
  these pages, `hypothesis`. C-3 `cobc -std=ibm`
  gives INITIALIZE's values: `verified` (J-114).
- **Held by:** `lower/`'s data map (the `// Unsure` becomes a `// Decision: R-057`); the proof
  report's header; `docs/PROFILE.md`'s `ibm.wsinit` row (added as `ibm.wsclear`, J-118; renamed).
- **Source:** Opus's default and lean (J-114); decided by Fable, 2026-10-06.
- **Status:** in force
- **See also:** R-020, R-049, P-03
- **subject:** runtime

### R-058: Every defect Katana's checks prove in an existing tool is a discovery, recorded with its proof
- **Decision:** When a check in this repository shows that an existing implementation (GnuCOBOL's
  `-std=ibm` dialect, a testsuite's expected output, a published manual's own example, any tool
  Katana is compared with) gives output that disagrees with the authority it claims to follow, that
  is a **discovery**, and it is recorded in `docs/DISCOVERIES.md` with fixed fields: its number
  (D-001 onward, never reused), the tool and version, what it does, what the authority says (quoted,
  with page or section, in `docs/research/`), the test that proves it (an existing check under
  R-056, with its killer mutation), the journal entry that found it, how many of the generated or
  corpus cases show it, and a status (`open`, `reported`, `fixed in <version>`, `withdrawn` with
  why). A discovery is distinct from a finding (a finding is about the input program; a discovery is
  about another tool) and from a claim (a claim is about Katana). A code comment or test that says
  "against cobc" cites its D-number. The register is held by a doc test: every row names a test
  that exists and a journal entry that exists; every differential test whose expected side excludes
  cases "where cobc departs" names the discovery that excludes them, so no departure is excused
  without a number. Reporting a discovery upstream is Ghost's decision each time (a bug report with a
  COBOL reproducer discloses the defect, not Katana's method, but it names the project; R-037).
- **Why:** Ghost, 2026-10-06: an evaluator of the research, or a buyer of the product, must be able
  to see what the project has already established: that the existing tools sometimes produce wrong
  output, where, and why, each with a test anyone can run. Scattered across journal entries and
  test comments, the same facts are anecdotes; in one register with fixed fields and a test holding
  it to the code, they are a result. Two days of phase 3 produced over a dozen (the intermediate cap
  not applied, literal folding, five editing departures including IBM's own `-$999.99` example,
  INSPECT phrases run one at a time, COMP-5 size error judged by capacity, two DISPLAY sign forms,
  floating insertion characters dropped), and none had a number.
- **Claims:** C-1 each discovery's reproducer is a COBOL program and an IBM citation, never GnuCOBOL
  source: `verified` by construction (R-045). C-2 the register will matter to an evaluator more than
  the pass counts: `hypothesis`; the NSF study's reviewers are the trial.
- **Held by:** `docs/DISCOVERIES.md`; its doc test (Opus adds it with the backfill from the phase-3
  journal entries, J-097 to J-120); `docs/GLOSSARY.md` ("discovery").
- **Source:** Ghost, 2026-10-06 ("if we end up with tests that prove an existing implementation has
  a bug in it, we need to be able to say to an evaluator: this is what the research project has
  already discovered, and this is why this existing tool sometimes has bad output"); recorded by
  Fable.
- **Status:** in force
- **See also:** R-045, R-046, R-052, R-056, P-03
- **subject:** checks

### R-059: An arithmetic statement with several receivers takes its sending value once, as IBM's multiple-results rule says
- **Decision:** ADD, SUBTRACT, MULTIPLY and DIVIDE without GIVING compute their sending side once,
  into a temporary, before any receiver changes, then apply it to each receiver left to right; a
  subscript on a receiver is evaluated when that receiver's turn comes, with the values as they then
  are (IBM: for `D(C)` after `C` was a receiver, "the subscript C contains the new value of C"). The
  same item named as operand and receiver (`ADD A TO A B`) is therefore not an overlap (NE-08 is two
  items over the same bytes) and has a defined result: A 2, B 3 gives A 4, B 5. For MULTIPLY and
  DIVIDE the temporary is the multiplier or the divisor, the one operand those forms share.
- **Why:** LR 6.4 pp. 297–298, "Multiple results" (quoted, `docs/research/ibm-options.md` §10): the
  statement "stores that result in a temporary location" and combines it "with each single receiving
  field". cobc re-reads the operand after its own update (B 7, and the same in MULTIPLY and DIVIDE):
  D-022, held by the program MULTRES.
- **Claims:** C-1 the rule as quoted: `verified`. C-2 for MULTIPLY and DIVIDE the temporary is the
  multiplier or divisor: `hypothesis` (IBM's example is an ADD; the rule's wording, "the result" of
  the sending side, gives this reading and no other that fits the forms); an `// Unsure` in
  `lower/ArithVerbs.shared` names it, and a mainframe or a further IBM example settles it.
- **Held by:** `lower/ArithVerbs.shared`; MULTRES in the differential tests; D-022.
- **Source:** Fable's recollection under its status (J-141's HANDOFF note), Opus's reading of the
  page the same day; decided by Fable, 2026-10-06.
- **Status:** in force
- **See also:** R-020, R-058, NE-08
- **subject:** runtime

---

Reserved, to be recorded when their phase arrives (docs/ROADMAP.md names each): R-014 CICS/DB2 grammar graft; R-018
the attribute tuple; R-025 indexed-file comparison on logical unload; R-028 coverage in every proof; R-029 refuted intent never acted on; R-030 ledger
append-only; R-031 models propose intent only, later, never v1; R-032 passes apply only when
obligations are discharged; R-033 Erosa–Hendren total structuring refused; R-034 the residual
dispatcher is acceptable output; R-035 the refactoring-distance report; R-036 the Hercules lane's
legal status.
