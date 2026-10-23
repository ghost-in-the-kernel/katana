# oracle/: the referee

Runs COBOL and Java on the same jobs and says whether they agree. It never imports `lower/` or `passes/`
(MOD-05). Its routing rows are in `docs/paths/oracle.md` and `docs/paths/divergence.md`.

## Its parts

- `fixture/`: a job as data (`Schema`, `FixtureCheck`); `fixture/carddemo/`, the CardDemo jobs.
- `lane/`: the two lanes, cobc and Java (`CobcLane`, `JavaPrograms`), their steps, IDCAMS, and the determinism
  gate (`Gate`).
- `sort/`: Katana's DFSORT (`Dfsort`, `SortCards`).
- `compare/`: two datasets field by field (`Compare`, `Layout`, `Decode`).
- `nist/` and `autotest/`: the NIST suite and the GnuCOBOL autotests, read and run.
- `proof/`: the proof report (`ProofReport`).
- `prove/`: phase 8A, a foreign Java port held to its COBOL (`Prove`, `Align`, `Attribute`, `Report`).
