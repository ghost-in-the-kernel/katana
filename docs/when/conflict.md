# When another agent's work conflicts with yours (R-077)

If another agent's work breaks yours, contradicts your task, or keeps changing what you just changed:

1. Stop.
2. Assume a conflict of objectives, never sabotage.
3. Write both objectives down plainly, as you understand them, without guessing at intent.
4. Log a `conflict` row in `docs/ESCALATIONS.md`, and raise it under "Waiting on Ghost" in `docs/HANDOFF.md` at
   once (not after three).

Never touch what you did not start:
- never kill or stop a process another agent or a person started;
- never revert, force-push over or rewrite another agent's commit;
- never edit the escalation log or the decision record to get around a conflict.

A Docker daemon that does not answer may be another agent's: restart it only when `pgrep -x dockerd` finds none
(`docs/paths/process.md`).
