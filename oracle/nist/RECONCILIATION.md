# NIST: Katana's golden against the published summary

Katana's NIST golden (`programs.golden`) is what each program's own footer reports under Katana's
profile (R-047). The published summary (`published.summary`, `Total 426 426 0 0 9610 0 20 118 9748`)
is the study's run under GnuCOBOL's harness, which counts some things NIST's material does not say
to. Every program whose outcome differs is a line below, with one reason. `ReconciliationTest`
applies the lines to the golden and must rebuild every published row exactly; a difference with no
line, or a line that does not account for it, fails.

The reasons, a fixed list (`Reconciliation.java`). The first four are rules that turn Katana's
outcome into the published one; the rest state the published outcome, because cobc or the published
run did something NIST's source does not.

- `inspection-as-pass`: tests NIST reports as needing inspection are counted as passed.
- `deleted-as-pass`: tests NIST's source deletes are counted as passed.
- `none-as-one`: a program that reports no tests (a chain step, a display-only manual program) is
  counted as one test passed.
- `runs-summed`: a program run twice (DB103M, DB-1) has its second run counted with the first's.
- `cobc-rejects`: cobc 3.1.2 does not compile NIST's source: a compile finding (P-03), not a pass.
- `cobc-behaviour`: the program runs and cobc's own behaviour fails tests the published run passed.
- `source-altered`: the published count cannot come from NIST's source as written.
- `not-supported`: NIST says the program need not be run without a module cobc lacks.

| Program | Run | Reason | Published | Why |
| --- | --- | --- | --- | --- |
| NC107A | 1 | `inspection-as-pass` | - | 5 tests NIST reports as needing inspection |
| NC110M | 1 | `none-as-one` | - | displays one PASS line and prints no footer |
| NC113M | 1 | `inspection-as-pass` | - | manual program: "15 TESTS REQUIRE VISUAL INSPECTION" |
| NC121M | 1 | `inspection-as-pass` | - | 2 tests need inspection |
| NC135A | 1 | `inspection-as-pass` | - | 1 test needs inspection |
| NC174A | 1 | `deleted-as-pass` | - | SWH-TEST-7 is deleted in NIST's source |
| NC204M | 1 | `cobc-behaviour` | `ok 15 15 0 0 0` | 3 ACCEPT tests into a 200-character field whose data spans two card lines; cobc fills it from one |
| NC214M | 1 | `none-as-one` | - | reports no tests |
| NC217A | 1 | `source-altered` | `ok 82 82 0 0 0` | NIST comments out STR-TEST-GF-4's STRING and deletes the test ("ANSC INTERPRETATION"); the published run counts it run and passed, one test more than the source as written |
| NC220M | 1 | `inspection-as-pass` | - | 2 tests need inspection |
| SM206A | 1 | `cobc-behaviour` | `ok 16 14 0 2 0` | PST-TEST-009: a debugging line inside COPY REPLACING pseudo-text |
| SM208A | 1 | `deleted-as-pass` | - | REP-TEST-7 is deleted in NIST's source |
| SQ106A | 1 | `deleted-as-pass` | - | 6 READ-short tests are deleted in NIST's source |
| SQ129A | 1 | `none-as-one` | - | reports no tests |
| IX111A | 1 | `none-as-one` | - | a step of IX109A's chain, no report of its own |
| ST102A | 1 | `none-as-one` | - | a step of ST101A's chain (the sort), no report of its own |
| ST109A | 1 | `none-as-one` | - | reports no tests |
| ST110A | 1 | `none-as-one` | - | a step of ST109A's chain, no report of its own |
| ST112M | 1 | `none-as-one` | - | reports no tests |
| ST113M | 1 | `none-as-one` | - | a step of ST112M's chain, no report of its own |
| ST115A | 1 | `none-as-one` | - | reports no tests |
| ST116A | 1 | `none-as-one` | - | a step of ST115A's chain, no report of its own |
| ST120A | 1 | `none-as-one` | - | a step of ST119A's chain, no report of its own |
| ST122A | 1 | `none-as-one` | - | reports no tests |
| ST123A | 1 | `none-as-one` | - | a step of ST122A's chain, no report of its own |
| OBSQ3A | 1 | `none-as-one` | - | reports no tests |
| OBNC1M | 1 | `cobc-behaviour` | `ok 6 6 0 0 0` | STOP literal: with no operator to answer, cobc goes on, so STOP-GF-9-TEST runs and fails ("EXECUTION DID NOT HALT") |
| IF119A | 1 | `cobc-rejects` | `ok 23 23 0 0 0` | `FUNCTION MAX(IND(ALL))`, the 1989 amendment's ALL subscript, which no `cobc -std` accepts |
| IF120A | 1 | `cobc-rejects` | `ok 17 17 0 0 0` | `FUNCTION MAX(IND(ALL))`, the 1989 amendment's ALL subscript, which no `cobc -std` accepts |
| IF121A | 1 | `cobc-rejects` | `ok 17 17 0 0 0` | `FUNCTION MAX(IND(ALL))`, the 1989 amendment's ALL subscript, which no `cobc -std` accepts |
| IF122A | 1 | `cobc-rejects` | `ok 17 17 0 0 0` | `FUNCTION MAX(IND(ALL))`, the 1989 amendment's ALL subscript, which no `cobc -std` accepts |
| IF123A | 1 | `cobc-rejects` | `ok 23 23 0 0 0` | `FUNCTION MAX(IND(ALL))`, the 1989 amendment's ALL subscript, which no `cobc -std` accepts |
| IF128A | 1 | `cobc-rejects` | `ok 16 16 0 0 0` | `FUNCTION MAX(IND(ALL))`, the 1989 amendment's ALL subscript, which no `cobc -std` accepts |
| IF129A | 1 | `cobc-rejects` | `ok 17 17 0 0 0` | `FUNCTION MAX(IND(ALL))`, the 1989 amendment's ALL subscript, which no `cobc -std` accepts |
| IF132A | 1 | `cobc-rejects` | `ok 15 15 0 0 0` | `FUNCTION MAX(IND(ALL))`, the 1989 amendment's ALL subscript, which no `cobc -std` accepts |
| IF137A | 1 | `cobc-rejects` | `ok 17 17 0 0 0` | `FUNCTION MAX(IND(ALL))`, the 1989 amendment's ALL subscript, which no `cobc -std` accepts |
| IF138A | 1 | `cobc-rejects` | `ok 16 16 0 0 0` | `FUNCTION MAX(IND(ALL))`, the 1989 amendment's ALL subscript, which no `cobc -std` accepts |
| IF141A | 1 | `cobc-rejects` | `ok 16 16 0 0 0` | `FUNCTION MAX(IND(ALL))`, the 1989 amendment's ALL subscript, which no `cobc -std` accepts |
| IF402M | 1 | `cobc-rejects` | `ok 1 1 0 0 0` | `FUNCTION MAX(IND(ALL))`, the 1989 amendment's ALL subscript, which no `cobc -std` accepts |
| DB103M | 2 | `runs-summed` | - | the published second run counts both runs (28) |
| DB205A | 1 | `not-supported` | `ok 0 0 0 0 0` | monitors a Communication procedure; "need not be run if the Communications Module is not supported" (DB-5); it waits to the time limit |
| DB301M | 1 | `none-as-one` | - | reports no tests |
| DB302M | 1 | `none-as-one` | - | reports no tests |
| DB303M | 1 | `none-as-one` | - | reports no tests |
| DB305M | 1 | `none-as-one` | - | reports no tests |
