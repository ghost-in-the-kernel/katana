#!/bin/bash
cd /home/claude/cobol-research/tools/nist
export COBC=cobc COBCRUN=cobcrun COBCRUN_DIRECT="" COB_HAS_ISAM=yes
start=$(date +%s)
for M in NC SM IC SQ RL IX ST SG OB IF RW DB; do
  echo "=== $M start $(date +%T)"
  (cd $M && make -k test-local >$M.run.log 2>&1; make diff >../$M.diff.log 2>&1)
done
echo "=== done in $(( $(date +%s) - start ))s"
perl summary.pl */report.txt > summary.log 2>&1
diff summary.txt summary.log > summary.diff || true
