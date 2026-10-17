# The Intent IR: what a program means, as facts with their evidence

What the program means, where the Semantic IR (docs/SEMANTIC-IR.md) is what it does (R-007). Every fact
is about a thing the Semantic IR holds, by its numbers; it carries the evidence a rule or a person read, a
confidence, and where it stands: inferred, confirmed or refuted. No oracle proves a fact; a person
confirms or refutes it (5.3's review file), and a pass acts only on a fact not refuted, saying when it
acts on one no person confirmed (R-029). `IntentDocTest` holds this document to `intent/types`: every
type and invariant here is in the code, and the reverse. The types hold data and the checks their
invariants make when built; `intent/rules` (5.2) makes facts from the IR.

Invariants are numbered INT-01 onward, in this document only, and a number is never reused.

## A program's facts

`Intent` is one program's facts, under the profile hash its Semantic IR was built with. `Fact` is one
fact: its `Kind`, its `Subject`, its `Evidence`, its confidence and its `State`. A fact is known by its
kind and its subject, written as its `id` (`enum item 12 APPL-AOK,APPL-EOF`), so a rule's next version
that infers it again is the same fact, and a person's word on it stays with it.

## Kinds and subjects

Each `Kind` is about one shape of `Subject`:

| Kind | Subject | Says |
| --- | --- | --- |
| `MONEY` | `Datum` | the item is an amount of money |
| `ENUM` | `Choice` | the item holds one of its condition-names' values |
| `FLAG` | `Datum` | the item is a yes-or-no |
| `COUNTER` | `Datum` | the item counts |
| `DATE` | `Datum` | the item is a date or a time |
| `TAGGED_UNION` | `Union` | the item and the items that REDEFINES it are alternatives, the tag saying which |
| `FUNCTION` | `Range` | the paragraphs a PERFORM names are a function |
| `REDUCIBLE` | `Paragraphs` | the paragraphs' control flow is reducible |
| `STATELESS` | `Whole` | the subprogram keeps nothing between CALLs |
| `NEVER_OVERFLOWS` | `Datum` | no value the program stores overflows the item |

`Datum` is an item by its id; `Choice` an item and two or more of its condition-names; `Union` an item,
the items that REDEFINES it and the tag item (0 with none known); `Range` the first and last paragraph
a PERFORM names, by index; `Paragraphs` a set of paragraphs; `Whole` a program by its PROGRAM-ID.

## Evidence and state

`Evidence` names the rule that inferred the fact and its version (`person` for a fact a person wrote)
and the IR nodes it read, each a `Node`: an `ItemNode`, a `ParagraphNode`, a `StatementNode` (a
paragraph's statement by its place, as the contract numbers it) or an `EdgeNode` of the paragraph graph.
`State` is where the fact stands, its `Status` `INFERRED`, `CONFIRMED` or `REFUTED`, and for the last two
who said so and on what date. A refused fact or set is `Invalid`, its message starting with the
invariant's number.

## Invariants

- **INT-01 A fact is about its kind's shape of subject.** An enum about a `Choice`, a function about a
  `Range`, and so on as the table says.
- **INT-02 A subject is well formed.** Items numbered from 1, paragraphs from 0; a `Choice` has two or
  more distinct condition-names; a `Union` one or more distinct alternatives, none the item or the tag; a
  `Range` ends at or after it starts; `Paragraphs` ascending, none twice; a `Whole` names a program.
- **INT-03 Evidence names its rule, its version and at least one node.** A node's numbers are numbers the
  IR could hold.
- **INT-04 Confidence is more than 0 and at most 1.**
- **INT-05 A confirmed or refuted fact says who and when; an inferred one names no one.** When is an ISO
  date.
- **INT-06 A program's facts are one each per kind and subject.** The set names its program and its
  profile hash (`sha256:` and 64 hex digits, as SIR-01).
- **INT-07 A program's facts are that program's.** Checked against its Semantic IR: its name and profile
  hash, and every node a fact names one of its own: an item it numbers, a condition-name of that item, an
  alternative that REDEFINES the item, a paragraph, a statement of that paragraph, an edge of its graph.
