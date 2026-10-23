# Escalations: each time a Sonnet agent asked Opus, and what reached it (R-073, R-075)

The Opus agent adds a row for every escalation, after asking the Sonnet agent to replay the instructions it followed
(its workpath, the files it was routed to, the rules it read) and checking them against what the task needed at its
start. A replay the agent cannot give is fine, and logged as `no-replay`.

Gap kinds:
- `missing-row`: no routing row led to the files the task needed (`docs/paths/`).
- `missing-instruction`: the path's instructions lacked a rule the task needed.
- `not-read`: the instruction was there and the agent did not read it.
- `wrong-instruction`: an instruction or row pointed the wrong way, or is stale.
- `no-replay`: the agent could not replay what it followed.
- `none`: everything needed reached the agent; the failure was the work's own.

When a kind other than `none` has three rows, the third raises it to Ghost: a line under "Waiting on Ghost" in
`docs/HANDOFF.md` starting `Escalations, <kind>:`, for review and a routing pass. `test/EscalationsTest.java` holds
this file's form and that rule.

| Date | Task | Replay | Gap kind | What was missing, and where it belongs |
| --- | --- | --- | --- | --- |
