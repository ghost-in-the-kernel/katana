# rt/: the runtime the lowered Java runs against

The only code a converted program needs (R-019). It imports nothing of Katana's, and no `MathContext` (R-020):
arithmetic is Appendix A's. Tests: `test/katana/rt/`, each area held to cobc -std=ibm by a differential.

## Where to start

- `RunUnit`: a run unit's codepage, UPSI, SYSIN and SYSOUT, its RETURN-CODE and its storage facade.
- `Field` and `Record`: an item's bytes; `Attr`, its attributes; `Table`, OCCURS.
- `Decimal`, `Arithmetic`, `Scalars`, `Money`: arithmetic and the stores into a receiver.
- `Move`, `Edit` and `Editor`, `Display`, `Compare` and `Collation`, `Inspect`, `Strings`: the verbs.
- `SeqFile`, `RelFile`, `IdxFile`, `KeyedFile` and `Ksds`, `SortFile`, `Linage`: files.
- `Dispatcher` and `Subprogram`: PERFORM and GO TO (R-021), CALL.
- `Trace`, `PerformTrace`, `Events`: what a run reports about itself (LANE-22); the probes (LANE-23) are in
  `Decimal`, off unless `-Dkatana.mutate` is set.
