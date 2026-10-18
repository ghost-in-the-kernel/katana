#!/usr/bin/env sh
# The pre-push check (R-050, while CI has no minutes): what CI would run, on a fresh clone of HEAD,
# in the pinned image with the network off, choosing the suites the change touches (R-008 tithe 2a).
#   scripts/prepush.sh          the suites the commits since origin/main (or $BASE) touch, and why
#   scripts/prepush.sh --all    every suite (the phase-boundary tithe always runs this)
# A change to sir/, lower/ or rt/ runs the quick NIST (NC and IF, about 140 programs; tithe pass 2,
# C2); a change to oracle/nist/ or the profile runs all of it, which then covers the quick one.
# The corpus is copied from this checkout's verified clones (hard links), not fetched; fetch.sh then
# checks each pinned one is at its commit. One container builds, runs every chosen suite, then the fast loop.
# First, R-008's gate (J-154): past sixteen commits since the last "Tithe" commit, a non-tithe head is
# refused unless theGhost.md records `Tithe deferred through <commit>` (scripts/TitheGate.java).
# Last, R-054's ceilings (J-163): the fast loop is run once, last, warm in the same container, and timed
# (`gradle build` ran it a first time too, for nothing: J-206), and it and the check's own time are held
# to docs/AGENT.md's ceilings (scripts/Ceilings.java).
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
  for s in nist fixture lane front sir autotest rt javalane intent ledger passes; do add $s "--all"; done
else
  for f in $CHANGED; do
    case "$f" in
      *.md|theGhost.md|docs/*|README*|LICENSE) ;;
      oracle/nist/*|test/katana/oracle/nist/*) add nist "$f" ;;
      oracle/autotest/*|test/katana/oracle/autotest/*) add autotest "$f" ;;
      oracle/fixture/*|test/katana/oracle/fixture/*) add fixture "$f"; add lane "$f" ;;
      oracle/*|test/katana/oracle/*) add lane "$f" ;;
      profile/*|test/katana/profile/*) add nist "$f"; add fixture "$f"; add lane "$f" ;;
      front/*|test/katana/front/*) add front "$f"; add sir "$f" ;;
      sir/*|test/katana/sir/*) add sir "$f"; add quick "$f"; add javalane "$f"; add intent "$f" ;;
      intent/*|test/katana/intent/*) add intent "$f"; add ledger "$f"; add passes "$f" ;;
      ledger/*|test/katana/ledger/*) add ledger "$f"; add passes "$f" ;;
      passes/*|test/katana/passes/*) add passes "$f"; add javalane "$f"; add lane "$f" ;;
      rt/*|test/katana/rt/*) add rt "$f"; add quick "$f"; add javalane "$f" ;;
      lower/*|test/katana/lower/*) add quick "$f"; add javalane "$f"; add ledger "$f"; add passes "$f" ;;
      test/*.java|scripts/*) ;;
      *) for s in nist fixture lane front sir autotest rt javalane intent ledger passes; do add $s "$f (the build or the corpus)"; done ;;
    esac
  done
fi
# Every suite selected is --all in substance, and is held to --all's ceiling (R-054, J-168).
every=yes; for s in nist fixture lane front sir autotest rt javalane intent ledger passes; do case " $suites " in *" $s "*) ;; *) every= ;; esac; done
[ -n "$every" ] && ALL=yes
tests=""
for s in $suites; do
  case $s in
    nist) tests="$tests --tests katana.oracle.nist.*" ;;
    fixture) tests="$tests --tests katana.oracle.fixture.*" ;;
    lane) tests="$tests --tests katana.oracle.lane.* --tests katana.oracle.compare.*" ;;
    front) tests="$tests --tests katana.front.*" ;;
    sir) tests="$tests --tests katana.sir.* --tests katana.front.CorpusIntegrationTest" ;;
    quick) tests="$tests --tests katana.oracle.nist.ModuleRunIntegrationTest" ;;
    autotest) tests="$tests --tests katana.oracle.autotest.*" ;;
    rt) tests="$tests --tests katana.rt.*" ;;
    javalane) tests="$tests --tests katana.lower.*" ;;
    intent) tests="$tests --tests katana.intent.*" ;;
    ledger) tests="$tests --tests katana.ledger.*" ;;
    passes) tests="$tests --tests katana.passes.* --tests katana.oracle.lane.StructuringCardDemoIntegrationTest" ;;
  esac
done
PROPS=""
case " $suites " in *" nist "*) ;; *" quick "*) PROPS="-Dkatana.nist.modules=NC,IF" ;; esac
docker run --rm --network none -u "$(id -u)" -v "$ROOT":/katana -w /katana katana java scripts/TitheGate.java /katana
echo "suites:${suites:- none (the fast loop and the doc tests only)}$why"
# A passing gate removes its clone; a failing one keeps it to be read, until the next gate removes it
# here (J-179: kept clones and test folders filled the disk). Two gates are never run at once.
for old in /tmp/claude-0/prepush.*; do [ -d "$old" ] && echo "removing an earlier gate's clone: $old" && rm -rf "$old"; done
WORK=$(mktemp -d /tmp/claude-0/prepush.XXXXXX)
start=$(date +%s)
git clone -q "$ROOT" "$WORK/katana"
for c in nist-cobol85-test-suite carddemo omp-cobol-course gnucobol-autotests; do cp -al "$ROOT/corpus/$c" "$WORK/katana/corpus/$c"; done
sh "$WORK/katana/corpus/fetch.sh" nist-cobol85-test-suite carddemo omp-cobol-course gnucobol-autotests >/dev/null
cd "$WORK/katana"
TASKS="assemble testClasses"; [ -n "$tests" ] && TASKS="assemble testClasses integration $tests"
status=0
docker run --rm --network none -u "$(id -u)" -v "$PWD":/katana katana sh -c \
  "gradle $TASKS $PROPS --offline --warning-mode fail && s=\$(date +%s) && gradle test --offline --rerun-tasks -q && echo warm-fast-loop=\$((\$(date +%s) - s))" >"$WORK/gate.log" 2>&1 || status=$?
end=$(date +%s)
if [ $status -eq 0 ]; then
  warm=$(sed -n 's/^warm-fast-loop=//p' "$WORK/gate.log")
  echo "warm fast loop: ${warm} s"
  docker run --rm --network none -u "$(id -u)" -v "$ROOT":/katana -w /katana katana java scripts/Ceilings.java /katana "$warm" "$((end - start))" ${ALL:+--all} || status=$?
fi
echo "pre-push check: $( [ $status -eq 0 ] && echo passed || echo FAILED ) in $((end - start)) s on $(git rev-parse --short HEAD) (log: $WORK/gate.log)"
[ $status -eq 0 ] && rm -rf "$WORK"
exit $status
