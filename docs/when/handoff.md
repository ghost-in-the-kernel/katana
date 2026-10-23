# When you hand a task to an agent, or finish one (R-078, R-079)

## Handing out a task

A new task gets a fresh Claude Sonnet agent, briefed with the task, the rules, and the report wanted, not with
what earlier runs found: what a run learned is carried by the repository (the journal, TODO.md, the workpath
files). Resume an agent only within one unfinished task, on the same files, while its context is small. A
fresh agent at a merge boundary or a change of workpath also tests that the repository carries what it needs.
End the brief with "finish as `docs/when/handoff.md` says, then report": a brief that describes only the report is
read as the whole finish, and the debrief is missed (J-253, N-005).

## Finishing a task

1. The fast loop green, and the item's own check where it has one.
2. Add your debrief to `docs/NAVIGATION.md`: what slowed you down or misled you in finding your way, and what you
   suggest. Not the task's own findings; those go in the journal.
3. Report what you did, what you read, what is left.

## The review, about every ten commits (Opus)

Read `docs/NAVIGATION.md`'s entries for what agents converge on. Nothing actionable: write the journal entry and
empty the entries. Actionable: write the journal entry, empty the entries, check each item is valid against the
code, and fix the valid ones (a tithe). An open question goes to a Fable instance; its answer goes in the
journal entry. Title the commit "Navigation review <n>" ("Tithe: navigation review <n>" only when it is also the tithe due), and set `Reviewed through:` to the last commit the review read that is not a merge
(the gates count work, not merges: J-254).
