# Navigation: each agent's debrief on finding its way (R-079)

After a task, the agent that did it adds an entry: what slowed it down or misled it in finding its way (a missing
row, a stale path, a doc that disagreed with the code, an instruction it met only through a failing test), and
what it suggests. Not the task's own findings: those go in the journal.

About every ten commits Opus reviews the entries for what agents converge on: the same complaint from runs that
did not see each other. The review leaves a journal entry (how many entries, what converged, what was done, the
open questions sent to a Fable instance), fixes what is valid, then empties the entries below and moves the line
`Reviewed through:` to the review's commit. `scripts/NavigationGate.java` refuses a push past twenty commits
since it.

Reviewed through: bd6afa4

An entry: `### N-<n> <date>, <task> (<commits>)`, then the lines `- **Complaints:**` and `- **Suggestions:**`.

## Entries


### N-020 2026-10-24, navigation review 5 (J-279)
- **Complaints:** (1) The session loaded Suite Builder's CLAUDE.md as its primary instructions; the brief said it did not apply and named Katana's, which routed cleanly, so this cost nothing here, but it is the third review where it was the first thing every agent reported. (2) The procedure is split over four places and none says which agent does what: `CLAUDE.md` ("When you hand a task ... or finish one") leads to `docs/when/handoff.md`, whose review section is two sentences and headed "(Opus)"; the journal entry's fields are in `docs/JOURNAL.md`; the entry's shape is only in earlier entries (J-266, J-276); `Reviewed through:` lives in `docs/NAVIGATION.md` and its rule (the last non-merge commit read) in `handoff.md`. I wrote the entry from J-266 and J-276, not from a rule. (3) "A tithe is due" has two numbers: `CLAUDE.md` says one commit in eight, the gate (`scripts/TitheGate.java`, found by reading it) refuses past sixteen and ignores merges, and `NavigationGate` has a separate ten/twenty. Nothing says which count decides a "Tithe: " prefix, so I read the gate's code to answer the brief's question. (4) `docker image ls` showed only `katana:dev`; `process.md` has the `docker tag` line, but it is the third paragraph of a long file reached only through the "start a step" line, not through the Commands paragraph that gives the `docker run ... katana gradle` command. (5) A complaint's validity is checked against files by search, and a debrief names methods loosely (N-019 wrote `expected` for `expectedCheck`'s wrapper); there was no cheap way to see that a debrief's claim was already fixed except by the commit that is named in its own header (J-278's bd6afa4 fixed two of N-019's six).
- **Suggestions:** `docs/when/handoff.md`'s review section gets one sentence on the tithe: "a review is a tithe only when `scripts/TitheGate.java` would count more than sixteen non-merge commits since the last, or Ghost says so" (and drop "(Opus)" from the heading, or say a Sonnet agent may be asked to). `CLAUDE.md`'s Commands paragraph gets "if `docker image ls` shows only `katana:dev`, `docker tag katana:dev katana`". The journal entry's field list for a review (read, already fixed, converged and fixed, outside the repository, kept as evidence, open, resolved, touched) is a template in `handoff.md`, taken from J-266.
