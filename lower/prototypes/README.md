# Literal-lowering reference material (reference only; no code from here enters Katana)

- `oc4j-construct-test/CONSTR.cbl`: a 42-line program exercising packed decimal, edited pictures,
  REDEFINES, OCCURS, 88-levels, PERFORM THRU, PERFORM VARYING, COMPUTE ROUNDED and a GO TO loop.
  `CONSTR.c` is GnuCOBOL's `cobc -C` output and `CONSTR.java` opensourcecobol4j's `cobj -C` output
  for it. Both ran byte-identical to `cobc -x`. They are the reference *design* for Katana's literal
  Java (byte-image storage, decimal scratch registers, a paragraph dispatcher with PERFORM
  continuations, a provenance comment per statement). Both tools are GPL: their design is the
  specification, their outputs are oracles, their code stays out (theGhost.md R-019 to R-024).
- `oc4j-carddemo-reference/`: opensourcecobol4j's Java for CardDemo's batch programs, kept to diff
  Katana's data map (`b_N` sizes, offsets, attribute tuples) against in phase 2.5. GPL tool output
  over Apache-2.0 input; reference only, never shipped, never imported.
