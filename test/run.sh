#!/bin/sh
# The doc tests, plain Java 21 with no dependency (R-038), until phase 0.3 moves them into Gradle.
# Run from anywhere; exits non-zero if any test fails.
set -e
root=$(cd "$(dirname "$0")/.." && pwd)
out="$root/build/doc-tests"
rm -rf "$out" && mkdir -p "$out"
javac -d "$out" "$root"/test/*.java
status=0
for t in GhostTest GlossaryTest DocsTest ModulesTest TodoTest; do
  java -Dkatana.root="$root" -cp "$out" "$t" || status=1
done
exit $status
