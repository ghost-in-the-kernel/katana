# When a code fix fails, or you are asked to help one (R-073, R-075)

## If you are a Claude Sonnet model

Attempt a code fix once. A cause you can see and a fix that is easy: make it, and do not escalate. If the attempt
fails in a way that is hard (the check still red after your one try, or the cause unclear), stop and ask a Claude
Opus instance: the Agent tool with `model: "opus"`, given the task, what you tried, the failing output and the
files. Then apply its answer and run the check yourself.

When Opus asks you to replay the instructions you followed (your workpath, the files you were routed to, the rules
you read), answer briefly. "I cannot remember" is a fine answer; do not spend long on it.

## If you are the Opus instance asked to help

1. First ask the Sonnet agent to replay the instructions it followed.
2. Check that what the task needed reached it at its start (CLAUDE.md, its workpath file, the item's text).
3. Log the escalation in `docs/ESCALATIONS.md` by gap kind (that file lists the kinds).
4. A third escalation of one kind goes to Ghost under "Waiting on Ghost" in `docs/HANDOFF.md`, for a routing pass.
5. Then help with the fix.
