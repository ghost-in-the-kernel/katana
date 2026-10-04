# The Semantic IR: the contract between the front end and the lowering

What the program does, derived from the parse tree and the profile with no judgment anywhere, and
complete: everything the lowering emits comes from here and nothing else (R-007). Its one reader
that matters is the lowering; its one check that matters is the oracle. `test/sir-doc.test` holds
this document to `sir/types`: every exported type and invariant in the doc, and the reverse.
Filled in phase 2.4 (docs/ROADMAP.md); until then, headings only.

Invariants are numbered SIR-01 onward, in this document only, and a number is never reused.

## The header: the profile hash

## The data map

## The paragraph graph

## Statements

## Files

## CALL interfaces

## Invariants
