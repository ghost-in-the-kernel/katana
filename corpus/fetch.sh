#!/usr/bin/env sh
# Fetches the corpus repositories listed in MANIFEST.md as shallow clones into corpus/<name>/.
# Nothing here is committed (see .gitignore): the manifest is the record, the clones are cache.
# Redistributable core: CardDemo (Apache-2.0), NIST via eval-nist-cobol85 (public domain + Apache-2.0),
# GnuCOBOL (GPL; test input only), proleap fixtures (MIT). Restricted (private study only): BankDemo.
set -e
cd "$(dirname "$0")"
c() { [ -d "$2" ] || git clone --depth 1 "$1" "$2"; }
c https://github.com/aws-samples/aws-mainframe-modernization-carddemo carddemo
c https://github.com/AnyEvalOrg/eval-nist-cobol85 eval-nist-cobol85
c https://github.com/OCamlPro/gnucobol gnucobol
c https://github.com/uwol/proleap-cobol-parser proleap-cobol-parser
c https://github.com/openmainframeproject/cobol-programming-course omp-cobol-course
c https://github.com/TypeCobolTeam/TypeCobol typecobol
# BankDemo: "solely for internal demonstration purposes" (Rocket). Private study only; never redistributed.
c https://github.com/RocketSoftwareCOBOLandMainframe/BankDemo bankdemo
echo "fetched; run ./scan.sh to regenerate scan.tsv"
