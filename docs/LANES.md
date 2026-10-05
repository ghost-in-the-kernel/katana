# The lanes and their DD contract

A lane runs a fixture's job (docs/FIXTURE.md). The cobc lane runs the COBOL under GnuCOBOL (phase
1.4); the java lane runs the same job with each program step a Java program (phase 1.8, a stub until
`rt/` in phase 3). Both lanes share everything but the program step: the starting catalog, DD
allocation and dispositions, generations, the utilities (IDCAMS, IEFBR14, IEBGENER, SORT), COND and
abend bypass, and the unload and hash of every dataset at the end (`oracle/lane/`). What each lane's
program sees is the same, so a difference in what they write is the programs' difference.

Invariants are numbered LANE-01 onward, in this document only, and a number is never reused.

## The DD contract

- **LANE-01 A DD is an environment variable.** `DD_<name>` holds the path of the dataset's file (for a
  concatenation, the first), `/dev/null` for DUMMY, a file of 80-byte cards for an inline DD, a file
  the lane keeps for SYSOUT. The program finds its files by name, never by path.
- **LANE-02 Records are fixed-length, back to back.** A sequential dataset is its records at its
  `lrecl`, with no line terminators and no RDW.
- **LANE-03 The step's inputs are the environment, the PARM and SYSIN.** The fixture's clock is in
  `COB_CURRENT_DATE` and its zone in `TZ`; nothing else of the machine's environment is passed (PATH
  aside). The PARM is the program's one argument (the cobc lane passes it through a driver as z/OS
  does: a halfword length, then the text). An inline SYSIN is the program's stdin, one card a line.
- **LANE-04 The step's outputs are its datasets, its stdout and its return code.** stdout is the
  step's printed output (DISPLAY); the exit code is the return code; an abend is reported on stderr as
  `CEE3ABD abend Unnnn` (the 8-bit exit status cannot hold the code), and a program the platform stops
  on a signal ended abnormally (`abend SIGSEGV`).
- **LANE-05 The java lane's stub runs sequential datasets only.** A fixture with an indexed dataset, a
  stub or a called module is refused by the java lane with a finding; those wait for `rt/` (phase 3).

## The check

`test/katana/oracle/lane/TrivialLanesIntegrationTest.java`: `oracle/fixture/trivial/COPY.fixture`
copies five records. The cobc lane runs Katana's `COPYREC.cbl` twice, through the gate; the java lane
runs a hand-written Java program; the comparator finds no difference, and the two stdouts are the
same.
