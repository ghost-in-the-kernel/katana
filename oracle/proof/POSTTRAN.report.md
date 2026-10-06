# Proof report v0: POSTTRAN

- Fixture: `oracle/fixture/carddemo/POSTTRAN.fixture`, sha256:28e093663a9f84406ca41207c36c2edee9db591ae30b5070a118257c6de92b63
- Profile: ibm sha256:a5298bfd77c97fd3f26b37103ba2e6ed51a164dbd90b0435ff2d88f27939e77b
- Expected: the cobc lane. Actual: the study's frozen datasets (F-02, oracle/prototypes/carddemo/post1).

## Gate

Passed: the cobc lane's two runs are identical.

## Compared

| Dataset | Masks | Result |
| --- | --- | --- |
| AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS | none | the same |
| AWS.M2.CARDDEMO.DALYREJS.G0001V00 | none | the same |

Every dataset compared is the same.

## Coverage (from a cobc trace)

| Program | Paragraphs entered | Not entered |
| --- | --- | --- |
| CBTRN02C | 24 of 26 | 9999-ABEND-PROGRAM, 9910-DISPLAY-IO-STATUS |

## What it never covers (docs/PROOF.md, verbatim)

- Environment configuration: the job's real JCL, catalog, security, region sizes and site options
  beyond what the fixture states.
- Data outside the fixtures: a record of a shape, a value or a volume no fixture held.
- Paragraphs the fixtures never entered: they are listed in the report, and proven by nothing.
- Concurrency: jobs and transactions that run at the same time, their locking and their ordering.
- I/O performance, before or after a change of storage: how long a job takes is not compared.
- Governance: who may run the job, change it, or read what it writes.
- The compiler's own semantics beyond GnuCOBOL's reading of them: until a customer's mainframe output
  reconciles the lanes on the same fixture, a proof says "equivalent under profile X as GnuCOBOL reads
  it", and says so (R-013).
- Intent: a proof says what the program does on the fixture's data, never what it was meant to do.
