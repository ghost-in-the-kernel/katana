#!/usr/bin/env sh
# Fetches the corpus repositories listed in MANIFEST.md as shallow clones into corpus/<name>/.
# Nothing here is committed (see .gitignore): the manifest is the record, the clones are cache.
# With names (`corpus/fetch.sh nist-cobol85-test-suite`) it fetches only those, as CI does.
# Redistributable core: CardDemo (Apache-2.0), NIST via eval-nist-cobol85 (public domain + Apache-2.0),
# GnuCOBOL (GPL; test input only), proleap fixtures (MIT). Restricted (private study only): BankDemo.
set -e
cd "$(dirname "$0")"
ONLY="$*"
# wanted DIR: true when no names were given, or DIR is one of them.
wanted() { [ -z "$ONLY" ] || case " $ONLY " in *" $1 "*) return 0 ;; *) return 1 ;; esac; }
c() { wanted "$2" || return 0; [ -d "$2" ] || git clone --depth 1 "$1" "$2"; }
# p URL DIR COMMIT: a clone pinned to one commit, for sources a golden depends on.
p() {
  wanted "$2" || return 0
  [ -d "$2" ] || { git init -q "$2" && git -C "$2" fetch -q --depth 1 "$1" "$3" && git -C "$2" checkout -q FETCH_HEAD; }
  [ "$(git -C "$2" rev-parse HEAD)" = "$3" ] || { echo "$2 is not at $3" >&2; exit 1; }
}
# CardDemo: pinned, since the fixtures in oracle/fixture/carddemo/ hold its data by hash (phase 1.3).
p https://github.com/aws-samples/aws-mainframe-modernization-carddemo carddemo 59cc6c2fd7ebd7ef7925cad552a01a4b8b6e4d5e
c https://github.com/AnyEvalOrg/eval-nist-cobol85 eval-nist-cobol85
# GnuCOBOL's source is never fetched whole (R-045: its code stays off every screen while Katana's
# equivalent is written). Phase 3 fetches only tests/testsuite.src, the autotests, by sparse checkout:
#   git clone --depth 1 --filter=blob:none --sparse https://github.com/OCamlPro/gnucobol gnucobol-autotests
#   git -C gnucobol-autotests sparse-checkout set tests/testsuite.src
c https://github.com/uwol/proleap-cobol-parser proleap-cobol-parser
# NIST CCVS85's population file, the source of the NIST run (R-044): pinned, and checked by hash.
p https://github.com/Zaneham/nist-cobol85-test-suite nist-cobol85-test-suite 0428c3cfd51ad4f34703b5b3f7529cdfab613f55
if wanted nist-cobol85-test-suite; then
  echo "744a04982095a3abea29a9df5faf63d226083edecc7b5bf34bc412eae0d53274  nist-cobol85-test-suite/newcob.val" | sha256sum -c --quiet
fi
# The OMP course: pinned, since the comparator's check reads its EBCDIC data (phase 1.7).
p https://github.com/openmainframeproject/cobol-programming-course omp-cobol-course 1f037d24326124f4f2c5cd2d31e5220ceb549580
c https://github.com/TypeCobolTeam/TypeCobol typecobol
# BankDemo: "solely for internal demonstration purposes" (Rocket). Private study only; never redistributed.
c https://github.com/RocketSoftwareCOBOLandMainframe/BankDemo bankdemo
echo "fetched${ONLY:+: $ONLY}; run ./scan.sh to regenerate scan.tsv"
