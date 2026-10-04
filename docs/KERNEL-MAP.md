# Kernel map: Katana's own functions, append-only

Generated from the source by the kernel-map script (phase 0.6) and committed; review its diff, never
edit a line by hand (docs/ROADMAP.md 4.3). One line per function: its module, its name, the decision
it stands on or the caller it serves, and the commit it was first seen in. A line is never changed;
a function that goes away gets a later line with `superseded-by:` or `removed:` and the commit.
`test/KernelMapTest.java` fails on a function removed or renamed without one.

| Module | Function | Stands on | First seen | Superseded |
| --- | --- | --- | --- | --- |
| oracle | `oracle/nist/Population.java:Population.Population` | no caller | 2026-10-04 |  |
| oracle | `oracle/nist/Population.java:Population.read` | no caller | 2026-10-04 |  |
| oracle | `oracle/nist/Population.java:Population.card` | called by `oracle/nist/Population.java:Population.read` | 2026-10-04 |  |
| oracle | `oracle/nist/Cobc.java:Cobc.Result.ok` | called by `oracle/nist/Exec85.java:Exec85.run` | 2026-10-04 |  |
| oracle | `oracle/nist/Cobc.java:Cobc.Cobc` | no caller | 2026-10-04 |  |
| oracle | `oracle/nist/Cobc.java:Cobc.compile` | called by `oracle/nist/Exec85.java:Exec85.run` | 2026-10-04 |  |
| oracle | `oracle/nist/Cobc.java:Cobc.execute` | called by `oracle/nist/Exec85.java:Exec85.run` | 2026-10-04 |  |
| oracle | `oracle/nist/Cobc.java:Cobc.run` | called by `oracle/nist/Cobc.java:Cobc.compile` and 1 more | 2026-10-04 |  |
| oracle | `oracle/nist/Exec85.java:Exec85.Exec85` | no caller | 2026-10-04 |  |
| oracle | `oracle/nist/Exec85.java:Exec85.cards` | no caller | 2026-10-04 |  |
| oracle | `oracle/nist/Exec85.java:Exec85.program` | called by `oracle/nist/Exec85.java:Exec85.run` | 2026-10-04 |  |
| oracle | `oracle/nist/Exec85.java:Exec85.records` | called by `oracle/nist/Exec85.java:Exec85.run` | 2026-10-04 |  |
| oracle | `oracle/nist/Exec85.java:Exec85.lines` | called by `oracle/nist/Exec85.java:Exec85.cards` and 3 more | 2026-10-04 |  |
| oracle | `oracle/nist/Exec85.java:Exec85.undefined` | no caller | 2026-10-04 |  |
| oracle | `oracle/nist/Exec85.java:Exec85.run` | called by `oracle/nist/Cobc.java:Cobc.compile` and 1 more | 2026-10-04 |  |
