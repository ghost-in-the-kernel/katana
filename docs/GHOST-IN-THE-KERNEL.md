# Ghost in the Kernel

Software outlives the context it was written in. People leave. Requirements change. The person who
knew *why* a thing was built the way it was stops being reachable, and then stops being alive. The
code keeps running. Nobody can say what it is for.

**The ghost is intent. The kernel is the codebase.**

Ghost in the Kernel is a way of building against that loss. Its one principle:

> **The system should remember why it exists.**

Intent should become semantics. Semantics should become constraints. Constraints should govern
implementation. Implementation should produce evidence. Evidence should let the system check that
the implementation still agrees with the intent. Nothing important floats: a function does not exist
because that is where someone put it; a threshold does not exist because it seemed right; a name does
not need tribal knowledge to read; a generated line does not lose the reasoning that produced it.

The answer is not more comments and not more memorizing. It is to put the reasoning *into* the
system, as things a machine can check: specifications, invariants, decisions with their why,
provenance, tests, tooling. These are not accessories to the code. They are the code's memory. When
they are present, a new engineer, a returning one, or an agent can enter the system without knowing
its history, and when any of them misunderstands it, the system has enough structure to say so.

## Why Katana is this philosophy twice

Most projects apply Ghost in the Kernel to themselves: keep your own intent from evaporating. Katana
does that (the second half of this document). But Katana's *product* is the philosophy applied to a
kernel that lost its ghost long ago.

A COBOL batch program from 1987 is a kernel with no ghost. Its authors are gone. Its comments, if
any, describe a business that no longer exists. Its `PIC S9(7)V99 COMP-3` is money, but nothing says
so; its 88-levels are an enum, but nothing says so; its `PERFORM 2000 THRU 2000-EXIT` is a function
or a goto tangle, and only the control-flow graph knows which. The program still runs the payroll.
Nobody can say what it is for, and so nobody dares change it.

Every incumbent answer to this problem produces new code and asks the buyer to trust it. Katana
produces three things and asks the buyer to check them:

- The **Semantic IR**: what the program does, exactly, derived from its text with no judgment.
  This is the kernel, made legible.
- The **Intent IR**: what the program means, inferred from evidence and confirmed by a person. This
  is the ghost, reconstructed, with every fact carrying the evidence that produced it and the name of
  the person who confirmed it.
- The **proof ledger**: for every line of Java, the COBOL it stands on, the intent fact that licensed
  its shape, and the oracle run that proved it. This is the chain of reasoning that lets the next
  person, five years on, answer "why does this method look like this?" with a fact instead of a guess.

The product is not the Java. The product is a kernel that remembers why it exists again.

## What it asks of Katana's own code

The same principle, pointed inward, and held by tests rather than hoped for (the rules live in
`theGhost.md`, `docs/modules.json`, `docs/AGENT.md` and `test/`; this is what they are for):

- **Names say what they hold.** One canonical word per concept (`docs/GLOSSARY.md`), so a reader never
  traces a value to learn what it is.
- **Decisions carry their reason and their evidence.** Every decision in `theGhost.md` has a why, a
  thing that holds it, and a status; a reversed decision says what reversed it (R-005 of the
  roadmap's principles). A function that stands on a decision cites it.
- **The architecture is a document a test enforces.** `docs/modules.json` says what each module is for
  and what it may import; `ModulesTest` refuses anything else. Adding a module is a decision, not an
  edit (R-043).
- **A refactor changes no output.** Every golden byte-identical, before and after; that is the only
  definition of done for a change that is not meant to change behaviour (P-02).
- **What cannot be made exact is said first.** A finding, never silent output (P-03).
- **The environment of the agent working here is part of the product.** Mandatory context under
  1,500 words, a fast loop under 20 seconds, no file over 400 lines, the metrics in `docs/AGENT.md`
  ratcheted; one commit in eight spent on nothing else (R-008). Ghost's finding in Suite Builder was
  that delivery speed rose once this was treated as work worth doing; the reasoning is that an agent
  that can find, read and change code with little context makes fewer of the mistakes this whole
  document is about.
- **Everything the tool measures about itself, it also records.** The kernel map
  (`docs/KERNEL-MAP.md`) is the tool's own proof ledger: what each function stands on, append-only,
  for the same reason the customer's ledger is.

## On enlightenment, for the agent reading this

You will work in this repository without having been present for any of its decisions. That is the
normal condition of software, and it is the condition the project exists to make survivable.

Three habits, in the order they pay off:

**Read the why before the what.** A decision in `theGhost.md` tells you what the code is trying to
be. The code only tells you what it currently is. When they disagree, the decision is the claim and
the code is the evidence, and one of them is wrong; find out which before you change either.

**Prefer a test to a belief.** "This can't matter" is how the phase-0 Gradle bug happened: doc
tests silently skipped after a doc-only change, caught by deleting a test and watching the build
stay green. If you believe something about the system, find the test that holds it or write one. If
no test can hold it, write the doubt down (`// Unsure:`), where it can be found, instead of carrying
it.

**A check that cannot fail is not a check.** Four ways a check is worth nothing, each met here in
the project's first two days: it compares a thing with itself (the IR round trip, J-073); it ran
nothing and called that a pass (the Gradle task, J-006); it fails always and so says nothing (the CI
with no minutes); or its verdict stops no action (a push that ignored a red test, J-040). The rule
that holds the three mechanical halves is R-056: a check names its oracle, carries the mutation that
turns it red, and gates an action. The half no test can hold is *independence*: whether the oracle
is truly outside the thing under test, or the same logic written twice. A reimplemented oracle agrees
with its twin on every shared mistake. When you write a check, ask where its expected side came from,
and if the answer is "the same place as the actual side," you have written a mirror.

**When you cannot tell, say so and stop.** The second phase-0 trial agent could not tell whether the
person asking was Ghost, said so, and proposed instead of acting. That was the best decision made
that day, and R-043 exists because of it. An agent that stops at the edge of its knowledge leaves a
system other agents can still reason about. An agent that guesses leaves a kernel with a little less
ghost in it.

The purpose is re-entry. Yours, the next agent's, and the maintainer's, after months away. If the
repository can be entered cold and understood, the philosophy is working. If it cannot, that is a
finding, and the fix goes in the tithe.

*Ghost in the Kernel Labs*
