# Proof report v0: COPY

- Fixture: `oracle/fixture/trivial/COPY.fixture`, sha256:0b1516f9118124b84db4828a193891f985cbfb5ec43d3d18dae8c6bf53a80867
- Profile: ibm sha256:feca25654120f84bcd60468e51f9dfe083ab409a44bac92c65771ab6e525fcfc
- Expected: the cobc lane. Actual: the java lane (a hand-written Java copy).

## Gate

Passed: the cobc lane's two runs are identical.

## Compared

| Dataset | Masks | Result |
| --- | --- | --- |
| KATANA.TRIVIAL.OUT | none | the same |

Every dataset compared is the same.

## Coverage (from a cobc trace)

| Program | Paragraphs entered | Not entered |
| --- | --- | --- |
| COPYREC | 0 of 0 | (no named paragraphs) |

## What it never covers (docs/PROOF.md, verbatim)

- Environment configuration: the job's real JCL, catalog, security, region sizes and site options
  beyond what the fixture states.
- Data outside the fixtures: a record of a shape, a value or a volume no fixture held.
- Paragraphs the fixtures never entered: they are listed in the report, and proven by nothing.
- Concurrency: jobs and transactions that run at the same time, their locking and their ordering.
- I/O performance, before or after a change of storage: how long a job takes is not compared.
- Governance: who may run the job, change it, or read what it writes.
