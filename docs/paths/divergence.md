# Workpath: the divergence report (phase 8A: a foreign Java port held to its COBOL)

Pointed to by CLAUDE.md (R-074). A new row for this path goes in this file's table, never in CLAUDE.md.

## Instructions

- `docs/DIVERGENCES.md` says how a report is made: the lanes, the inputs, the alignment, attribution by probe,
  and what a proven entry needs.
- A port or a program a customer gave us never enters the repository (R-037); a run's work goes under `build/`.
- A divergence is proven only by a run that differs; an alignment no person confirmed is said to be unconfirmed
  (R-064).
- The probes (`-Dkatana.mutate=DV-0n`) live in `rt/` and change nothing unless set (LANE-23).

- A new integration or golden test also gets its row in `docs/checks.json` (R-056): the oracle, why it is
  independent, a mutation test that exists, and `gates`, which takes a pre-push suite name (as `scripts/prepush.sh`
  lists them: `nist`, `fixture`, `lane`, `front`, `sir`, `autotest`, `rt`, `javalane`, `intent`, `ledger`,
  `passes`) or `fast`. The fast loop's `DocTests > checks()` fails until the row is there.

## Rows

| Task | Start here | Contract | Tests |
| --- | --- | --- | --- |
| The lanes, the inputs and the alignment | `oracle/prove/Prove.java`, `oracle/prove/Generators.java`, `oracle/prove/Boundary.java`, `oracle/prove/Align.java` | `docs/DIVERGENCES.md`, R-072 | `test/katana/oracle/prove/`, `test/katana/oracle/lane/BoundaryIntegrationTest.java` |
| The kinds, attribution and the report | `findings/Divergence.java`, `oracle/prove/Attribute.java`, `oracle/prove/Divergences.java`, `oracle/prove/Report.java` | `docs/DIVERGENCES.md` | `test/katana/oracle/prove/ReportTest.java`, `test/katana/oracle/prove/AttributionIntegrationTest.java` |
