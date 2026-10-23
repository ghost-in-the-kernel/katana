# Divergences: how a Java port departs from its COBOL

The catalogue the divergence report (phase 8A, R-072) attributes a difference by: each kind is one way a Java port
of a COBOL program departs from it, with the COBOL rule and the IBM text it rests on, the exact Java that keeps the
rule (what the report offers as the fix), and the wrong shape a port takes. Each kind has a generator,
`oracle/prove/Generators`, that writes a COBOL program and inputs on which the wrong shape differs from the cobc lane,
and a mutation, the exact port with that one rule broken (`test/katana/oracle/prove/DivergenceKindsIntegrationTest.java`).
`findings/Divergence.java` is this table; `DivergencesDocTest` holds the two alike. The seed is
`docs/research/semantic_traps.md`; a kind is added from it with its generator and its mutation, red before the kind
is built.

## How a report is made

A divergence report (phase 8A, R-072) holds a foreign port of one COBOL program to the cobc lane under one fixture.
It is made in this order; each piece is `oracle/prove/`'s, and `test/katana/oracle/prove/ReportIntegrationTest.java`
makes one end to end:

1. `Prove` runs the COBOL twice through the gate and the port once, under the DD contract (docs/LANES.md). A port
   that leaves an output unwritten is refused.
2. `Boundary` drives the fields the IR's arithmetic names toward their limits, so the fixture reaches what the
   sample does not (8A.3). `Generators` writes a case of one kind (8A.2).
3. `Align` maps the port's Java onto the COBOL by the program's names. Every line is `unconfirmed` until a person
   says otherwise (8A.4).
4. `Attribute` puts every differing cell to each kind's probe: Katana's literal Java with that one rule broken,
   `-Dkatana.mutate` (LANE-23). The kinds are ranked by the cells they reproduce (8A.5).
5. `Divergences` makes the entries:
   - `proven`, with the fixture and input record by hash;
   - `possible`, a statement of a kind's shape the fixture never took across the boundary (no event of the kind,
     LANE-22).

   Each entry has its statement, its aligned Java, its cause or what it follows from, its trace, and Katana's Java
   for the statement as the fix.
6. `Report` writes it: the NE- and HY- findings apart, docs/PROOF.md's "What it never covers" quoted, nothing that
   varies between runs. It refuses a proof whose fixture is missing or changed.

**What a report never claims:**
- That the port forgot a rule: it says the port's output is the COBOL's with that rule broken, on these cells.
- That IBM agrees: the oracle is cobc -std=ibm (R-013).
- That a cell no input reached is the same.

The fix is a reference, never a patch, and a report leaves the project only under NDA until the provisional is filed
(R-037).

## The kinds

| Code | Kind | The COBOL rule | Exact Java | Wrong Java shape |
| --- | --- | --- | --- | --- |
| `DV-01` | high-order truncation | a result too large for its receiver loses its high-order digits, with no SIZE ERROR phrase (LR 6.4 p. 285; TRUNC(STD)) | the value modulo 10 to the receiver's integer digits (`Arithmetic.store` without the phrase) | a fixed-width writer cutting the formatted value at its right end |
| `DV-02` | scale truncation | excess low-order digits are dropped, not rounded, without ROUNDED (LR 6.4 p. 263) | the value set to the receiver's scale, rounding down (`Decimal.store`, ROUNDED false) | HALF_UP, the common reading of a COMPUTE |
| `DV-03` | intermediate precision (dmax) | a quotient is carried to dmax decimal places, the most of any operand or receiver, before the next operation (PG 6.4 Appendix A) | each operation at Appendix A's places (`Decimal.divide` at dmax) | the whole quotient in `MathContext.DECIMAL128`, cut only at the store |
