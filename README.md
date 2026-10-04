# Katana

A deterministic COBOL-to-Java migration compiler that proves itself.

Katana reads a COBOL batch program and its compile options and writes three things: Java proven to
produce byte-identical outputs to the original on the customer's own batch runs; a provenance map
tying every Java method and field to the COBOL it came from and to the proof run that licensed its
shape; and the equivalence harness itself, which can verify anyone's Java. No model runs at
conversion time. The literal translation is a pinned intermediate, never shipped; every refactoring
beyond it is a pass proven output-identical before it applies.

Maintainer: Ghost (GitHub: ghost-in-the-kernel). Private; all rights reserved (see LICENSE and
theGhost.md R-002). The roadmap is `docs/ROADMAP.md`; the feasibility study it stands on is
`docs/research/FEASIBILITY.md`. Agents start at `CLAUDE.md`.
