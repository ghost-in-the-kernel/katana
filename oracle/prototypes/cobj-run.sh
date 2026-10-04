#!/bin/bash
# usage: cobj-run.sh <name> <srcdir> <copydirs...>
export COB_CONFIG_DIR=/home/claude/cobol-research/tools/opensourcecobol4j/config COB_COPY_DIR=/home/claude/cobol-research/tools/opensourcecobol4j/copy
name=$1; src=$2; shift 2; I=(); for d in "$@"; do I+=(-I "$d"); done
out=/home/claude/cobol-research/tools/runs/oc4j-$name; mkdir -p $out; log=/home/claude/cobol-research/tools/results/oc4j-$name.txt; : > $log
pass=0; fail=0
while IFS= read -r f; do
  if timeout 60 /home/claude/cobol-research/tools/opensourcecobol4j/cobj/cobj -C -j "$out" "${I[@]}" -ext cpy -ext CPY -ext cbl "$f" > $out/$(basename "$f").err 2>&1; then pass=$((pass+1)); echo "PASS $f" >> $log; else fail=$((fail+1)); echo "FAIL $f :: $(grep -m1 -E 'error|Error' $out/$(basename "$f").err | cut -c1-220)" >> $log; fi
done < <(find "$src" -type f \( -iname "*.cbl" -o -iname "*.cob" -o -iname "*.cobol" \) | sort)
echo "SUMMARY pass=$pass fail=$fail" | tee -a $log
