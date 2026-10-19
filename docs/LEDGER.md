# The proof ledger: what was done to the Java, and why it was allowed

Append-only (R-030): a mapping is never edited, only superseded by a later line that cites it. Every Java
symbol a build holds has at least one line; the provenance map is written from the live lines, and the
proof report says how many each pass wrote and every act on a fact no person confirmed (R-029). It is the
kernel map pointed at the customer's program (R-007, docs/ROADMAP.md 3.2). `LedgerDocTest` holds this
document to `ledger/`: the line's fields and the invariants here are the code's, and the reverse.

Invariants are numbered LED-01 onward, in this document only, and a number is never reused.

## A line

`seq | prev | pass | fact | state | run | symbol | node | profile | time | supersedes`

`seq` numbers the line from 1; `prev` is the first 16 hex digits of SHA-256 over the line before (sixteen
zeros for the first). `pass` is what made the symbol: `literal` for the lowering, or a pass by name and
version. `fact` is the intent fact that licensed it (`-` for none) and `state` whether a person confirmed it
(`confirmed`, `unconfirmed`, or `-`). `run` is the oracle run that proved the Java (a proof report and its
hash), or `-` where none has. `symbol` is the Java symbol as the lowering's manifest names it (a class, its
fields `f<id>` and records `r<id>`, its stretch methods `s<label>`, a pass's methods such as structuring's
`perform<first>` and scalar promotion's variables, named for their items (`wsTranCount`, pass `scalars-v1`); a nested program's class inside its container's, `Outer.Inner`), or `-` for a symbol a pass removed. `node` is the COBOL it stands on (`program
NAME`, `item <id>`, `paragraph <index>`). `profile` is the profile hash, `time` an instant, `supersedes` the
earlier line this one stands in for (`-` for none). A symbol is live while some line names it and no later
line supersedes that one.

## Invariants

- **LED-01 Lines are only ever appended.** Numbered 1, 2, ... each carrying the hash of the one before, and
  the file's `end` line the hash of the last, so a line edited, removed, inserted or moved breaks the chain
  and the ledger is refused, read from its file alone; a ledger and the one it was share all of the earlier
  one's lines. The chain holds no secret: it stops an edit made by hand or by mistake, and one that
  recomputes every hash after it is caught only against an earlier copy (`appendedTo`, the repository's
  history).
- **LED-02 A line says what it needs to.** Its pass, symbol and node, the profile by its hash, the time as
  an instant; a fact's state confirmed or unconfirmed, never refuted (R-029), and no state without a fact;
  a removal supersedes something; no field holds the separator.
- **LED-03 A line supersedes only an earlier line.**
- **LED-04 Against a build, every symbol has a live line and every live line's symbol is in the build.** A
  symbol a pass removed has a line superseding its last one.
