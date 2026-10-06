# Findings: what Katana says first

What the input program does that Katana cannot make exact (F-08), found in the Semantic IR before any
Java is written (docs/ROADMAP.md 2.7) and reported first in a build's output, one line each, by kind
then place (`findings/Finding.java`). A finding is never silent output: the build says it and the
lowering (phase 3) refuses or marks what it covers. `FindingsDocTest` holds this table to the code.

| Code | Kind | Why it cannot be made exact |
| --- | --- | --- |
| `NE-01` | TRUNC(OPT) | a binary item's value past its PICTURE depends on the code IBM's optimizer generated |
| `NE-02` | overlapping PERFORM ranges | which range's exit returns control depends on the order they were entered at run time |
| `NE-03` | recursive PERFORM | a range performed from inside itself has no defined return under IBM's rules |
| `NE-04` | division without ON SIZE ERROR | a division by zero leaves the receiving field as IBM's code left it, which is not defined |
| `NE-05` | ODO data beyond the count | bytes of a table past its OCCURS DEPENDING count are undefined, and a move of its record carries them |
| `NE-06` | COMP-1 or COMP-2 | IBM's hexadecimal floating point has no exact Java equivalent |

## Valid under hypothesis

A program with one of these is exact under a reading Katana takes from cobc's ibm dialect, not yet
proven on IBM (R-049's mechanism): the proof report lists it as valid under that hypothesis, apart from
the findings above, and one mainframe run clears every program that carries it at once.

| Code | Kind | Why it is valid only under a hypothesis |
| --- | --- | --- |
| `HY-01` | GO TO out of a PERFORM range | exact under cobc's reading of PERFORM's return (R-021 C-1), which a mainframe run settles for every such program at once |

Not yet found: reads of uninitialized storage (F-08's last class), which needs the data flow the
Intent IR builds (phase 5); until then it is listed here and nowhere else.
