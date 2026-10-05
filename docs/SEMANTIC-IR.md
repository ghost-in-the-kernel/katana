# The Semantic IR: the contract between the front end and the lowering

What the program does, derived from the parse tree and the profile with no judgment anywhere, and
complete: everything the lowering emits comes from here and nothing else (R-007). Its one reader
that matters is the lowering; its one check that matters is the oracle. `SirDocTest` (the roadmap's
`sir-doc.test`) holds this document to `sir/types`: every type and invariant here is in the code, and
the reverse. The types hold data and the checks their invariants make when built, nothing else;
`sir/build` (2.5) makes them from a tree, `sir/check` (2.5) finds what the program gets wrong.

Invariants are numbered SIR-01 onward, in this document only, and a number is never reused.

## The header: the profile hash

`Program`: the `Header`, the data map, the files, the `Procedure`, the `Call`s, and the programs
nested in it. `Header`: the program's name, its source file, and the hash of the profile (R-049) it
means what it means under.

## The data map

`Item`: each data item in textual order, numbered from 1: name, level, parent, record, `Section`
(FILE, WORKING-STORAGE, LOCAL-STORAGE, LINKAGE), offset in the record and length in bytes, the
`Attributes` tuple, the item it REDEFINES, its `Occurs` (with the ODO object, its `Key`s and INDEXED
BY items), SYNC slack, its VALUE as a `Value`, and its 88s, each a `Condition` whose values are
`Value`s (a range a THRU expression). `Attributes`: `Usage`,
`Category`, digits, scale, signed, sign separate, sign leading, PICTURE, and an edited item's mask (one
symbol a character position, the currency sign written `$` and the point `.`, the `Header` saying
which they are). An item's `Flag`s: JUSTIFIED, BLANK WHEN ZERO, EXTERNAL, GLOBAL, as stated.

## The paragraph graph

`Procedure`: the USING and RETURNING items, each `Section` (a declarative one with what it is USE'd
for), each `Paragraph` in textual order with its statements, and each `Edge` (`Kind` PERFORM, GO_TO
or ALTER) from one paragraph to another, through the end of a PERFORM range.

## Statements

`Statement`: a verb, its operands, each an `Operand` with its role, and its phrases, each a `Branch`
with its condition and statements. What it operates on is a `Value`: a `Field` (subscripted and
reference-modified as written), a `File`, a `Target` paragraph, a `Name` that is not data, a
`Literal` (its text, its `Kind` and its decoded payload, SIR-08), a `Figurative` constant, a
`ConditionName`, or an `Expression` of these. A statement's roles and an expression's operators are
the grammar's (front/grammar), not chosen here; sir/roles.golden lists every verb's roles and branch
labels (tithe pass 2, A6).

## Files

`FileDescriptor`: its name, its ASSIGN, `Organization`, `Access`, RECORD KEY, each `AlternateKey`,
RELATIVE KEY, FILE STATUS item, its records, and its smallest and largest record.

## CALL interfaces

`Call`: the target, each `Argument` with its `Mode` (BY REFERENCE, CONTENT or VALUE), and the
RETURNING field. A program's own interface is its `Procedure`'s USING and RETURNING. An IR that breaks
an invariant is refused with `Invalid`, whose message starts with the invariant's number.

## The contract

What a back end must honor, as lines (`sir/contract`, docs/ROADMAP.md 2.6): each line is a fact's
kind, its program, and the fact, written from the IR by the kind's row in a table; a new kind of fact
is a row. A back end's output read back as lines is compared with the IR's fact for fact. The kinds:

| Kind | Covers |
| --- | --- |
| `profile` | the profile the program means what it means under |
| `editing` | the currency sign and decimal point a mask's `$` and `.` stand for |
| `item` | each item's place, attribute tuple, edit mask and flags |
| `redefines` | each item that redefines another |
| `occurs` | each table's bounds, ODO object, keys and indexes |
| `value` | each item's VALUE |
| `condition` | each 88 and its values |
| `file` | each file's organization, access, keys, status and records |
| `paragraph` | each paragraph's place in textual order and its section |
| `falls` | control that falls from a paragraph into the next |
| `edge` | each PERFORM, GO TO and ALTER between paragraphs |
| `statement` | each statement's verb, operands with their roles, and branches |
| `call` | each CALL's target, arguments with their modes, and RETURNING |
| `using` | the program's own USING and RETURNING items |

Goldens: each program's contract, every line, is held in `sir/golden/<PROG>.contract` (COPYREC and one
NIST program of each module, the NIST sources beside it as EXEC85 extracted them; `IrGoldenTest`), so a
change to the IR lands as a reviewable diff. The lowering's goldens will stand beside them in the same
shape: `lower/golden/<PROG>.java`, the generated Java byte for byte, a refactor changing none (P-02).

## Invariants

- **SIR-01 The header names the profile by its hash.** `sha256:` and 64 hex digits; a program means
  what it means under one profile (R-007, R-049). It also says the program's CURRENCY SIGN and
  whether DECIMAL-POINT IS COMMA.
- **SIR-02 Items are numbered in textual order.** 1, 2, ... with a parent before its children; a
  record (level 01 or 77) is its own record and every other item's record precedes it.
- **SIR-03 An item lies inside its parent and its record.** Its offset and length fit its parent's
  extent (the parent's length times its OCCURS maximum) and, unless it redefines, its record's length;
  an OCCURS runs from a minimum to a maximum no smaller.
- **SIR-04 Only a numeric item has digits and a sign.** A numeric or numeric-edited item has 1 to 38
  digits (a floating-point one none); every other has none and is unsigned; a PICTURE is always
  written, "" for none; an edited item, and only one, has a mask, without S, V or P.
- **SIR-05 Every reference resolves.** Each item an item, a file, a key, an ODO object, the procedure
  or a statement names is in the data map, and each file a statement names is one of the program's.
- **SIR-06 The paragraph graph is closed.** Paragraphs are numbered 0, 1, ... in textual order;
  statements under no paragraph name (at the start of the body or of a section) are a paragraph named
  ""; every edge joins two of them.
- **SIR-07 Everything has an origin.** Each item, condition, paragraph, section, statement, file, CALL,
  edge and the header carry the place in the COBOL they came from (2.3's source map), as the IR's own
  `Origin` (file, line, column, and its `Kind`: source, replaced or added), so a reader of the IR needs
  nothing of the front end.
- **SIR-08 A literal carries what it means.** Its `Kind` (alphanumeric, hex, null-terminated,
  national, DBCS, numeric, float, boolean, CICS) and its payload: the characters with quotes undone,
  the bytes as pairs of hex digits, or a signed unscaled integer with its scale (value = payload ×
  10^-scale; only a number has a scale), so the lowering decodes nothing. cobc is its oracle
  (`LiteralIntegrationTest`).
