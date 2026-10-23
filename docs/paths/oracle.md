# Workpath: the oracle (fixtures, the lanes, DFSORT, the comparator, the proof report)

Pointed to by CLAUDE.md (R-074). A new row for this path goes in this file's table, never in CLAUDE.md.

## Instructions

- `oracle/` never imports `lower/` or `passes/` (MOD-05): the referee does not read the players' code.
- The oracle is cobc under `-std=ibm` and Katana's harness, not IBM; say so in a claim (R-057).
- A golden changes only with `-Dkatana.record=true` and a reviewed diff in its own commit; a refactor changes none
  (P-02).
- Nothing a run produced enters the repository (R-037): a run's output goes under `build/`.
- Integration runs are long: `gradle integration --tests <class>` to a file, then read the result's failures.

- A new integration or golden test also gets its row in `docs/checks.json` (R-056): the oracle, why it is
  independent, a mutation test that exists, and `gates`, which takes a pre-push suite name (`nist`, `fixture`,
  `lane`, `front`, `sir`, `autotest`, `rt`, `javalane`, `intent`, `ledger`, `passes`, or `fast`). The fast loop's
  `DocTests > checks()` fails until it is there.

## Rows

| Task | Start here | Contract | Tests |
| --- | --- | --- | --- |
| A job fixture: what a lane runs; the CardDemo jobs | `oracle/fixture/Schema.java`, `oracle/fixture/FixtureCheck.java`, `oracle/fixture/carddemo/` | `docs/FIXTURE.md` | `test/katana/oracle/fixture/FixtureTest.java`, `test/katana/oracle/fixture/FixtureDocTest.java` |
| The cobc lane and the determinism gate | `oracle/lane/CobcLane.java`, `oracle/lane/Steps.java`, `oracle/lane/Gate.java`, `oracle/lane/carddemo.golden` | `docs/FIXTURE.md`, R-048 | `test/katana/oracle/lane/CardDemoLaneIntegrationTest.java`, `test/katana/oracle/lane/GateIntegrationTest.java` |
| Katana's DFSORT: a SORT step's cards over its records | `oracle/sort/Dfsort.java`, `oracle/sort/SortCards.java`, `oracle/lane/Sorts.java` | R-010, R-026, R-051; P-11 (SUM, INREC and other cards are built when a fixture needs them) | `test/katana/oracle/sort/SortTest.java` |
| The java lane and the DD contract both lanes keep, record formats (RECFM) included | `docs/LANES.md`, `oracle/lane/JavaPrograms.java`, `oracle/lane/ProgramRunner.java`, `oracle/fixture/trivial/COPY.fixture` | `docs/LANES.md` | `test/katana/oracle/lane/TrivialLanesIntegrationTest.java` |
| The CardDemo lanes' test support | `test/katana/oracle/lane/CardDemoLane.java`, `test/katana/support/Javac.java` | `docs/LANES.md` | `test/katana/oracle/lane/CardDemoJavaLaneIntegrationTest.java` |
| GnuCOBOL's autotests read: each case's program, expected output and compile flags (R-045 as amended) | `oracle/autotest/Autotests.java` (the reader; `Case.flags`), `oracle/autotest/CompileFlags.java` (a case's flags to `Settings`, R-083 to R-085) | R-045, `docs/paths/lowering.md` (the census) | `test/katana/oracle/autotest/AutotestsTest.java`, `test/katana/oracle/autotest/CompileFlagsTest.java`; `AutotestCorpusIntegrationTest` writes the cases and `runnable.txt` |
| The comparator: two datasets field by field | `oracle/compare/Compare.java`, `oracle/compare/Layout.java`, `oracle/compare/Decode.java` | R-011 | `test/katana/oracle/compare/CompareTest.java`, `test/katana/oracle/compare/CompareOmpIntegrationTest.java` |
| The proof report, v0 | `oracle/proof/ProofReport.java`, `docs/PROOF.md`, `oracle/proof/POSTTRAN.report.md`, `oracle/lane/Paragraphs.java` | `docs/PROOF.md` (PROOF-01 to 03), R-013 | `test/katana/oracle/proof/ProofReportTest.java`, `test/katana/oracle/lane/ProofIntegrationTest.java` |
