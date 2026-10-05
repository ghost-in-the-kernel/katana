#!/usr/bin/env sh
# The pre-push check (R-050, while CI has no minutes): what CI would run, on a fresh clone of HEAD,
# in the pinned image with the network off, choosing the suites the change touches (R-008 tithe 2a).
#   scripts/prepush.sh          the suites the commits since origin/main (or $BASE) touch, and why
#   scripts/prepush.sh --all    every suite (the phase-boundary tithe always runs this)
# A change to sir/, lower/ or rt/ runs the quick NIST (NC and IF, about 140 programs; tithe pass 2,
# C2); a change to oracle/nist/ or the profile runs all of it, which then covers the quick one.
# The corpus is copied from this checkout's verified clones (hard links), not fetched; fetch.sh then
# checks each pinned one is at its commit. One container runs `gradle build` and every chosen suite.
set -e
cd "$(dirname "$0")/.."
ROOT=$(pwd)
ALL=; [ "$1" = "--all" ] && ALL=yes
BASE=${BASE:-origin/main}
CHANGED=$(git diff --name-only "$BASE"...HEAD; git diff --name-only HEAD)
suites=""; why=""
add() { case " $suites " in *" $1 "*) ;; *) suites="$suites $1"; why="$why
  $1: $2";; esac; }
if [ -n "$ALL" ]; then
  for s in nist fixture lane front sir; do add $s "--all"; done
else
  for f in $CHANGED; do
    case "$f" in
      *.md|theGhost.md|docs/*|README*|LICENSE) ;;
      oracle/nist/*|test/katana/oracle/nist/*) add nist "$f" ;;
      oracle/fixture/*|test/katana/oracle/fixture/*) add fixture "$f"; add lane "$f" ;;
      oracle/*|test/katana/oracle/*) add lane "$f" ;;
      profile/*|test/katana/profile/*) add nist "$f"; add fixture "$f"; add lane "$f" ;;
      front/*|test/katana/front/*) add front "$f"; add sir "$f" ;;
      sir/*|test/katana/sir/*) add sir "$f"; add quick "$f" ;;
      lower/*|rt/*|test/katana/lower/*|test/katana/rt/*) add quick "$f" ;;
      test/*.java|scripts/*) ;;
      *) for s in nist fixture lane front sir; do add $s "$f (the build or the corpus)"; done ;;
    esac
  done
fi
tests=""
for s in $suites; do
  case $s in
    nist) tests="$tests --tests katana.oracle.nist.*" ;;
    fixture) tests="$tests --tests katana.oracle.fixture.*" ;;
    lane) tests="$tests --tests katana.oracle.lane.* --tests katana.oracle.compare.*" ;;
    front) tests="$tests --tests katana.front.*" ;;
    sir) tests="$tests --tests katana.sir.*" ;;
    quick) tests="$tests --tests katana.oracle.nist.ModuleRunIntegrationTest" ;;
  esac
done
PROPS=""
case " $suites " in *" nist "*) ;; *" quick "*) PROPS="-Dkatana.nist.modules=NC,IF" ;; esac
echo "suites:${suites:- none (the fast loop and the doc tests only)}$why"
WORK=$(mktemp -d /tmp/claude-0/prepush.XXXXXX)
start=$(date +%s)
git clone -q "$ROOT" "$WORK/katana"
for c in nist-cobol85-test-suite carddemo omp-cobol-course; do cp -al "$ROOT/corpus/$c" "$WORK/katana/corpus/$c"; done
sh "$WORK/katana/corpus/fetch.sh" nist-cobol85-test-suite carddemo omp-cobol-course >/dev/null
cd "$WORK/katana"
TASKS="build"; [ -n "$tests" ] && TASKS="build integration $tests"
status=0
docker run --rm --network none -u "$(id -u)" -v "$PWD":/katana katana gradle $TASKS $PROPS --offline --warning-mode fail >"$WORK/gate.log" 2>&1 || status=$?
end=$(date +%s)
echo "pre-push check: $( [ $status -eq 0 ] && echo passed || echo FAILED ) in $((end - start)) s on $(git rev-parse --short HEAD) (log: $WORK/gate.log)"
[ $status -eq 0 ] && rm -rf "$WORK"
exit $status
