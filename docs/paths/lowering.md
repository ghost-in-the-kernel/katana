# Workpath: the runtime and the lowering (rt/, lower/)

Pointed to by CLAUDE.md (R-074). A new row for this path goes in this file's table, never in CLAUDE.md.

## Instructions

- Start at `lower/README.md`: the entry points and the per-program object graph, in its load-bearing order.
- `rt/` imports nothing of Katana's and holds no `MathContext` (R-020); arithmetic is Appendix A's (`rt/Decimal`).
- `lower/` writes what the IR and the `Plan` say and decides nothing (R-024, MOD-04); all Java text goes through
  `Emitter`, so every line keeps its provenance.
- The literal lowering is held by NIST and CardDemo through both lanes; a change selects the java lane and the
  passes in the gate. Read only its verdict and failures.

- The autotest cases (`corpus/gnucobol-autotests/cases/`) and the list of the 378 runnable ones
  (`corpus/gnucobol-autotests/runnable.txt`) are written by `AutotestCorpusIntegrationTest`, which holds the
  definition of "runnable" (`runnable(Case)`); read the list, do not read the word a second way.
- A test of this path that adds an integration test or a golden also adds its row to `docs/checks.json` (R-056): the
  oracle, why it is independent, a mutation test that exists, and `gates`, which takes a pre-push suite name
  (`rt`, `lane`, `javalane`, ... as `scripts/prepush.sh` lists; `rt` selects `katana.rt.*`). The fast loop's
  `DocTests > checks()` fails until the row is there.
- A program under test opens its files relative to the JVM's working directory, the repository root (the DD
  contract in `rt/` resolves a bare name as itself): a test that runs lowered programs removes what they leave
  (`AutotestsLoweredIntegrationTest`). The older autotest tests run only programs that open no file.

## Rows

| Task | Start here | Contract | Tests |
| --- | --- | --- | --- |
| The runtime: a record's bytes, arithmetic, MOVE, files | `rt/katana/rt/Field.java`, `rt/katana/rt/Decimal.java`, `rt/katana/rt/Arithmetic.java`, `rt/katana/rt/Move.java` | `docs/PROFILE.md`, R-019, R-020 | `test/katana/rt/`, `test/katana/rt/autotest/AutotestRtIntegrationTest.java` |
| The literal lowering: a statement to Java | `lower/README.md`, `lower/Lower.java` (a program's class), `lower/Storage.java` (its items), `lower/Verbs.java` (each verb) | `docs/SEMANTIC-IR.md`, R-024 | `test/katana/lower/`, `test/katana/lower/NistJavaLaneIntegrationTest.java` |
| The autotest census: each runnable GnuCOBOL autotest through the lowering, by the stage it reaches (`FLAG`: its compile flags include one `oracle/autotest/CompileFlags.java` does not map; `FLAGPASS`: it passes without it). Its floors are R-086's, in `test/katana/rt/autotest/floors.txt`. Not in the fast loop: run it after a change to what it reads (the gate runs it for `rt/` and `lower/`). Its inputs, the cases, are written by `AutotestCorpusIntegrationTest`: run that first when what `Autotests` writes changes. The golden is the current list of every case by stage, with the unmapped flags beside each FLAG or FLAGPASS line (`flag [reason key] | flag [reason key]`; the keys' texts in the `# reason` block at its end; `theGoldensFlagsReadBackAsTheCasesOwn` holds them to `flags.txt`); a FAIL line ends with its sorting key (`FailSorting`, a judgement about why the output differs: D-010, a harness gap, a real difference; the keys' texts in the `# sorting` block; the census refuses a FAIL with none, J-271); a mapped flag is found in the cases' `flags.txt` (J-266: N-009, N-012) | `test/katana/rt/autotest/AutotestsLoweredIntegrationTest.java`, `test/katana/rt/autotest/lowered.golden`; run: `gradle integration --tests katana.rt.autotest.AutotestsLoweredIntegrationTest` (`-Dkatana.record=true` rewrites the golden) | TODO 3.1, R-045, R-086 | the census itself |
| A test that lowers a small program | `test/katana/support/Small.java`, `test/katana/support/Javac.java` | | `test/katana/lower/` |
