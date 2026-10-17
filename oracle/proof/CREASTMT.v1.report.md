# Proof report v1: CREASTMT

- Fixture: `oracle/fixture/carddemo/CREASTMT.fixture`, sha256:e265a9702d7a9c55f93925c989671edb6c302b18db9d70d20bb7687ab418562b
- Profile: ibm sha256:cc4bbf084793b6f5280da870d1aa7b6fb58d535dcfaf091d8436a36a66c4dae1
- Expected: the cobc lane. Actual: the java lane (the program lowered by Katana, run on rt/).
- Initial values: an item without VALUE is assumed to start as INITIALIZE would set it, not proven (R-057).

## Gate

Passed: the cobc lane's two runs are identical.

The job's own findings, in both lanes:
- step STEP040: CBSTM03A ended on SIGSEGV, held as its outcome; a program that reads z/OS control blocks through storage (the PSA at address 0) is outside the cobc lane, its oracle the java lane (R-027)

## Compared

| Dataset | Masks | Result |
| --- | --- | --- |
| AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS | none | the same |
| AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS | none | the same |
| AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS | none | the same |
| AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS | none | the same |
| AWS.M2.CARDDEMO.TRXFL.SEQ | none | the same |
| AWS.M2.CARDDEMO.TRXFL.VSAM.KSDS | none | the same |

Every dataset compared is the same.

Not compared, the cobc lane leaving none of them (a step it cannot run, R-027): AWS.M2.CARDDEMO.STATEMNT.HTML; AWS.M2.CARDDEMO.STATEMNT.PS.

## Findings

None.

## Valid under hypothesis

None.

## Notes

- CBSTM03A and CBSTM03B run in the java lane only (R-027), their step reading z/OS control blocks through the lane's storage facade, laid out as R-063's hypotheses say; what they write is held to oracle/lane/carddemo-java.golden and to its input (Statements), and proven by nothing

## Coverage

| Program | Paragraphs entered | Not entered | From |
| --- | --- | --- | --- |
| CBSTM03A | 24 of 25 | 9999-ABEND-PROGRAM | the java lane's trace (the cobc lane cannot run it, R-027) |
| CBSTM03B | 14 of 14 | none | the java lane's trace (the cobc lane cannot run it, R-027) |

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
