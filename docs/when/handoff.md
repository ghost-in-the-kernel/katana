# When you hand a task to an agent, or finish one (R-078, R-079)

## Handing out a task

A new task gets a fresh Claude Sonnet agent, briefed with the task, the rules, and the report wanted, not with
what earlier runs found: what a run learned is carried by the repository (the journal, TODO.md, the workpath
files). Resume an agent only within one unfinished task, on the same files, while its context is small. A
fresh agent at a merge boundary or a change of workpath also tests that the repository carries what it needs.
End the brief with "finish as `docs/when/handoff.md` says, then report": a brief that describes only the report is
read as the whole finish, and the debrief is missed (J-253, N-005).

## Finishing a task

1. The fast loop green, and the item's own check where it has one: the check the TODO item's `Check:` names that fits your
   change. Where that is a whole set too large to run for each change, it is the part you touched. For 3.1 (the 359
   autotests) it is the census, `gradle integration --tests katana.rt.autotest.AutotestsLoweredIntegrationTest`, run after a change
   to what the census reads or maps (`oracle/autotest/`, `front/`, `lower/`, `rt/`, `profile/`); a change to what `Autotests` writes
   runs `AutotestCorpusIntegrationTest` first. A change to documents or a comment: the fast loop alone.
2. Add your debrief to `docs/NAVIGATION.md`: what slowed you down or misled you in finding your way, and what you
   suggest. Not the task's own findings; those go in the journal.
3. Report what you did, what you read, what is left.

## The review, about every ten commits (a fresh Sonnet agent; Opus gates and merges it, R-079)

Read `docs/NAVIGATION.md`'s entries for what agents converge on. Nothing actionable: write the journal entry and
empty the entries. Actionable: write the journal entry, empty the entries, check each item is valid against the
code as it is now (a debrief records what an agent met, not the tree: J-264), and fix the valid ones in docs, never
behaviour or a golden. A complaint is acted on when agents that did not see each other raise it, in this set or in an
earlier review's kept evidence (reviews 3 and 4 counted it so: J-266, J-276); one alone is kept as evidence. An open question is listed in the journal entry and the report for Opus to send to a Fable instance. The
journal entry keeps the trace (J-266 is the shape): how many entries, what was already fixed, what converged and
was fixed, what was kept, what is open. Which count says the tithe is due is `scripts/TitheGate.java`'s (sixteen
commits of work since the last "Tithe", merges not counted), not CLAUDE.md's one in eight, which is the aim. Title the commit "Navigation review <n>" ("Tithe: navigation review <n>" only when it is also the tithe due), and set `Reviewed through:` to the last commit the review read that is not a merge
(the gates count work, not merges: J-254).
