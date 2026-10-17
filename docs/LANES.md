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
  `lrecl`, with no line terminators and no RDW. RECFM=V stops the job until a fixture needs it: no
  `COB_VARSEQ_FORMAT` writes IBM's RDW (format 0's length leaves out the 4-byte prefix; J-062), so the
  cobc lane will run format 0 and add or take 4 from each length at load and unload.
- **LANE-03 The step's inputs are the environment, the PARM and SYSIN.** The fixture's clock is in
  `COB_CURRENT_DATE` and its zone in `TZ`; nothing else of the machine's environment is passed (PATH
  aside). The PARM is the program's one argument (the cobc lane passes it through a driver as z/OS
  does: a halfword length, then the text). An inline SYSIN is the program's stdin, one card a line.
  `KATANA_TIOT` gives the step's own facts for the java lane's z/OS storage (LANE-20): the job, the
  step, and each DD in order, `:u` on a device, `:n` SYSOUT, in-stream or DUMMY; the cobc lane ignores it.
- **LANE-04 The step's outputs are its datasets, its stdout and its return code.** stdout is the
  step's printed output (DISPLAY); the exit code is the return code; an abend is reported on stderr as
  `CEE3ABD abend Unnnn` (the 8-bit exit status cannot hold the code), and a program the platform stops
  on a signal ended abnormally (`abend SIGSEGV`).
- **LANE-05 The java lane refuses what it does not run yet.** A fixture with a called module its setup
  has no lowered class for (LANE-19) or a stub the lowering does not call as its service (the stub
  facade, phase 4.4) is refused with a finding, as is an indexed dataset when the lane's setup has no `rt/Ksds` command (LANE-16).
  `CEE3ABD` is lowered as LE's (LANE-18); a `PATH` needs nothing of its own, its key being an alternate
  key of the KSDS it is over (J-183).

## The generated program's contract (phase 3; tithe pass 2, B2)

What `lower/` emits for each program, and what `rt/` and the java lane hold it to. `CopyRecords`
(test/katana/oracle/lane/) is its first instance, run by the check below.

- **LANE-06 A program is a static entry and an adapter.** Each generated program's class has
  `public static int run(Map<String,String> dd, String parm, InputStream sysin, PrintStream sysout)`:
  its DDs by name (LANE-01's, `DD_` dropped), its PARM ("" for none), SYSIN and the step's stdout; it
  returns the return code. A `main(String[] args)` adapter builds those from the `DD_` variables, the
  one argument, System.in and System.out, and exits with what `run` returned. A lane and NIST call
  `run` in one JVM: NIST's 437 programs under two profiles as a JVM each would take about five minutes
  the pre-push check does not have.
- **LANE-07 An ASSIGN name finds its file through the DDs, then the working directory.** A file
  ASSIGNed to `X` is `dd.get("X")` when the step binds that DD, else a file named `X` in the step's
  working directory, as cobc resolves it (NIST's programs ASSIGN to names such as `XXXXX001`, which
  its run writes beside them).
- **LANE-08 An abend is thrown, the return code returned.** `rt/`'s `Abend(code)` ends the program
  abnormally; the adapter, or the lane calling `run`, prints `CEE3ABD abend Unnnn` on stderr (with no
  code, `CEE3ABD abend with no code`), as LANE-04 says of the cobc lane; RETURN-CODE at STOP RUN or
  GOBACK is what `run` returns. Two abnormal endings are the same when they are the same condition, not
  the same code: the same statement, the same reason (a file status on that file), and the files as
  IBM would have left them, every open file closed at the end ("If the run unit ends abnormally and the
  TRAP(ON) runtime option is in effect, the run time closes all open files", PG 6.4 p. 179), so a
  report the program wrote before the abend is compared like any other (Fable, J-147); the abend code
  is a profile fact the proof report states, never compared, since cobc has no abend codes (J-145). The NIST golden keeps an abnormal run's exit status and reason
  (`exit=1 reason=OPEN_TFIL_status_35_on_TAPE1`).
- **LANE-09 NIST runs through a runner, not through cobc.** `ModuleRun` takes an
  `oracle/nist/Runner` (build a called module, build a main program, run it); `Cobc` is one, and the
  java lane's is the other, so phase 3.4 runs NIST through both lanes without a change to `ModuleRun`.
- **LANE-10 A variable-length record on disk is Katana's own format.** Each record of a sequential
  file whose records vary (research §13) is written after 4 bytes: its length plus 4, big-endian, in
  two, then two zero bytes; `rt/Varying` writes and reads it, and nothing else does. It resembles
  IBM's format-V record descriptor (PG 6.4 p. 171), but it is the java lane's choice, not a claim
  about IBM's datasets: no program sees it, and no lane compares it, cobc writing its own (J-164).
- **LANE-11 A LINAGE file's page on disk is cobc's print-line form.** Each record a line of its own,
  trailing spaces trimmed; ADVANCING n is n - 1 blank lines; a new page is the rest of the page body,
  the bottom margin and the next top margin as blank lines (`rt/Linage`, J-151): the lane's choice, so
  a LINAGE report compares line for line with cobc's, not a claim about a printer's.
- **LANE-12 A channel skip is a new page.** WRITE ... ADVANCING a mnemonic-name is "a skip to channels 1
  through 12, or space suppression" (LR 6.4 p. 473); where a channel stops is the printer's
  carriage-control tape, not the program's. The java lane reads C01, channel 1, as a new page, as PAGE
  writes it, and CSP as no line advanced; C02 to C12 and stacker selection are not lowered (J-172).
- **LANE-13 The object-time debugging switch is a property.** IBM's runtime option DEBUG, NODEBUG its
  default, activates USE FOR DEBUGGING sections (LR 6.4 p. 760). The java lane reads it as the system
  property `katana.debug`, Y or N, N when absent (`rt/Debug`); NIST's run notes set it for each run, as
  COB_SET_DEBUG does for cobc (J-176).
- **LANE-14 STOP literal is answered at once.** IBM suspends the program and communicates the literal
  to the operator; it resumes "only after operator intervention ... at the next executable statement"
  (LR 6.4 p. 457). A lane has no operator: the java lane writes the literal to its console (stderr) and
  goes on, as cobc does with nothing to answer it (J-177).
- **LANE-15 A relative or indexed file on disk is Katana's own format.** `rt/RelFile` and `rt/IdxFile`
  hold the file's records in memory while it is open and write them back when it closes (`rt/KeyedFile`).
  A relative file: for each record that exists, its relative record number (8 bytes), its length (4
  bytes), big-endian, then its bytes. An indexed file: its keys (count, then each one's offset, length
  and DUPLICATES), its largest record; an OPEN compares the keys' places and lengths and the largest
  record with the program's for status 39 (PG 6.4 p. 198), DUPLICATES being the file's; then each
  record's length, bytes and, for each alternate key, its place among equal values. Like LANE-10 it is
  the java lane's choice, not IBM's VSAM nor cobc's form: no program sees it, and no lane compares it;
  only the java lane reads what the java lane wrote (J-180, J-182, J-183).
- **LANE-16 The java lane loads and unloads a KSDS through `rt/Ksds`.** `oracle/` may not import `rt/`, so
  the lane runs `katana.rt.Ksds load|unload LRECL PRIME ALTERNATES SEQUENTIAL INDEXED` as a process,
  the command its setup names under `*KSDS` (`CobcLane.KSDS`), as the cobc lane runs its generated
  loaders: records in key order in and out of the `IdxFile` (LANE-15), compared on their logical
  unload (R-025; J-183).
- **LANE-17 The generated program reads the lane's clock, returns RETURN-CODE and takes the PARM.**
  CURRENT-DATE and ACCEPT FROM DATE, DAY and TIME read `COB_CURRENT_DATE` when it is set (LANE-03,
  `rt/Now`); a normal end exits with RETURN-CODE modulo 4096 (LR 6.4 pp. 24, 457), 0 when it was never
  set; a program with PROCEDURE DIVISION USING gets its one argument as z/OS passes the PARM, a halfword
  length then the text, spaces to 100 bytes, as the cobc lane's driver does (J-183).
- **LANE-18 CALL 'CEE3ABD' is LE's service.** The run ends with an abend of its first argument's value,
  `CEE3ABD abend Unnnn` on stderr and that code as the exit status, as the cobc lane's stub ends it (LE
  3.1 p. 111, research §22; R-027). Clean-up 0 performs no termination activity on IBM; the lane closes
  every file at an abend all the same (an `// Unsure` in `lower/CallVerbs.call`).
- **LANE-19 A CALL is a call into the run unit.** Each lowered program implements `rt/Subprogram`: its
  `call` binds each LINKAGE record named in its USING to the argument's storage (BY REFERENCE the
  caller's item, BY CONTENT a copy, OMITTED none), runs, and returns at EXIT PROGRAM or GOBACK with its
  PERFORMs ended (LR 6.4 p. 343); STOP RUN ends the run from any program (`rt/StopRun`). `rt/RunUnit`
  holds what the programs share (the console, UPSI, RETURN-CODE, each EXTERNAL record and file) and each
  separately compiled program called, found by its class name on the class path: a static copy for a
  CALL literal under the profile's NODYNAM, a dynamic copy otherwise, CANCEL forgetting the dynamic one
  (R-062, research §24). A nested program is a class inside its container's, held by it, its GLOBAL
  records the container's (SIR-12). A program that cannot be found raises ON EXCEPTION, else abend U4038.
  The lane passes the profile's `ibm.dynam` as the system property `katana.dynam`, NODYNAM when absent;
  a fixture's called module runs when the lane's setup names its lowered class (J-186).
- **LANE-20 The java lane answers a program's reads of z/OS control blocks.** `rt/Storage` holds the
  step's PSA at address 0, a TCB and a TIOT built from `KATANA_TIOT` (LANE-03), laid out as R-063 says (a
  hypothesis: what CBSTM03A declares, not IBM's text). A POINTER is four bytes holding an address, NULL
  zero; SET ADDRESS OF a LINKAGE record binds it to the storage at the pointer's address, as a CALL binds
  it to an argument (LANE-19); an address no block holds ends the run with abend U4038. The cobc lane
  cannot map address 0 and holds the program's abend as its outcome (R-027, J-187).
- **LANE-21 The java lane traces paragraphs as cobc does.** With `KATANA_TRACE_FILE` set (the lane sets it
  when its setup is traced, one file a step, as COB_TRACE_FILE is), each section and paragraph a lowered
  program enters is a `Program-Id: NAME Paragraph: NAME` line (`rt/Trace`), so coverage is read from either
  lane's trace alike (PROOF-04, J-191).

## The check

`test/katana/oracle/lane/TrivialLanesIntegrationTest.java`: `oracle/fixture/trivial/COPY.fixture`
copies five records. The cobc lane runs Katana's `COPYREC.cbl` twice, through the gate; the java lane
runs a hand-written Java program; the comparator finds no difference, and the two stdouts are the
same.
