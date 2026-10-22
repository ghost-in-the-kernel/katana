# Divergences: how a Java port departs from its COBOL

The catalogue the divergence report (phase 8A, R-072) attributes a difference by: each kind is one way a Java port
of a COBOL program departs from it, with the COBOL rule and the IBM text it rests on, the exact Java that keeps the
rule (what the report offers as the fix), and the wrong shape a port takes. Each kind has a generator,
`oracle/prove/Generators`, that writes a COBOL program and inputs on which the wrong shape differs from the cobc lane,
and a mutation, the exact port with that one rule broken (`test/katana/oracle/prove/DivergenceKindsIntegrationTest.java`).
`findings/Divergence.java` is this table; `DivergencesDocTest` holds the two alike. The seed is
`docs/research/semantic_traps.md`; a kind is added from it with its generator and its mutation, red before the kind
is built.

| Code | Kind | The COBOL rule | Exact Java | Wrong Java shape |
| --- | --- | --- | --- | --- |
| `DV-01` | high-order truncation | a result too large for its receiver loses its high-order digits, with no SIZE ERROR phrase (LR 6.4 p. 285; TRUNC(STD)) | the value modulo 10 to the receiver's integer digits (`Arithmetic.store` without the phrase) | a fixed-width writer cutting the formatted value at its right end |
| `DV-02` | scale truncation | excess low-order digits are dropped, not rounded, without ROUNDED (LR 6.4 p. 263) | the value set to the receiver's scale, rounding down (`Decimal.store`, ROUNDED false) | HALF_UP, the common reading of a COMPUTE |
| `DV-03` | intermediate precision (dmax) | a quotient is carried to dmax decimal places, the most of any operand or receiver, before the next operation (PG 6.4 Appendix A) | each operation at Appendix A's places (`Decimal.divide` at dmax) | the whole quotient in `MathContext.DECIMAL128`, cut only at the store |
