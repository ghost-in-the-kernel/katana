#!/bin/bash
# Scans every repo folder in the corpus and prints one TSV row per repo.
# Columns: repo, cobol_files, cobol_lines, copybooks, jcl, data_files, programs(PROGRAM-ID),
#   EXEC CICS, EXEC SQL, SORT, REDEFINES, OCCURS DEPENDING, COMP-3, GO TO, ALTER, PERFORM..THRU, largest file, license
cd "$(dirname "$0")"
printf "repo\tcob_files\tcob_lines\tcopybooks\tjcl\tdata_files\tprogram_ids\texec_cics\texec_sql\tsort\tredefines\todo\tcomp3\tgoto\talter\tperform_thru\tlargest_file\tlicense\n"
for d in */; do
  d=${d%/}
  cobs=$(find "$d" -type f \( -iname '*.cbl' -o -iname '*.cob' -o -iname '*.cobol' -o -iname '*.tcbl' -o -iname '*.pgm' -o -iname '*.pco' -o -iname '*.sqb' -o -iname '*.eco' -o -iname '*.CBL' -o -iname '*.COB' \) -not -path '*/.git/*' | sort)
  cpys=$(find "$d" -type f \( -iname '*.cpy' -o -iname '*.copy' -o -iname '*.inc' -o -iname '*.cpb' \) -not -path '*/.git/*')
  jcls=$(find "$d" -type f \( -iname '*.jcl' -o -iname '*.prc' -o -iname '*.proc' \) -not -path '*/.git/*')
  dats=$(find "$d" -type f \( -iname '*.dat' -o -iname '*.txt' -o -iname '*.csv' -o -iname '*.ps' -o -iname '*.vsam' -o -iname '*.seq' \) -not -path '*/.git/*' -not -iname 'readme*' -not -iname 'license*' -not -iname 'cmakelists.txt')
  nf=$(printf "%s\n" "$cobs" | grep -c .)
  nl=0; [ -n "$cobs" ] && nl=$(printf "%s\n" "$cobs" | tr '\n' '\0' | xargs -0 cat 2>/dev/null | wc -l)
  ncp=$(printf "%s\n" "$cpys" | grep -c .)
  nj=$(printf "%s\n" "$jcls" | grep -c .)
  nd=$(printf "%s\n" "$dats" | grep -c .)
  cnt() { [ -n "$cobs" ] && printf "%s\n" "$cobs" | tr '\n' '\0' | xargs -0 grep -il -E "$1" 2>/dev/null | wc -l || echo 0; }
  cntm() { [ -n "$cobs" ] && printf "%s\n" "$cobs" | tr '\n' '\0' | xargs -0 grep -ih -E "$1" 2>/dev/null | wc -l || echo 0; }
  pid=$(cntm 'PROGRAM-ID')
  cics=$(cntm 'EXEC +CICS'); sql=$(cntm 'EXEC +SQL'); srt=$(cntm '^.{6}.*\bSORT\b +[A-Z0-9-]+ +(ON|ASC|DESC|USING|INPUT)')
  red=$(cntm '\bREDEFINES\b'); odo=$(cntm 'OCCURS .*DEPENDING'); c3=$(cntm 'COMP(UTATIONAL)?-3')
  gt=$(cntm '\bGO +TO\b'); alt=$(cntm '^.{6} *ALTER\b'); pt=$(cntm '\bPERFORM\b.*\bTHRU\b|\bTHROUGH\b')
  big=""; [ -n "$cobs" ] && big=$(printf "%s\n" "$cobs" | tr '\n' '\0' | xargs -0 wc -l 2>/dev/null | grep -v total | sort -rn | head -1 | awk '{print $1":"$2}')
  lic=$(ls "$d"/LICENSE* "$d"/COPYING* "$d"/License* "$d"/license* 2>/dev/null | head -1)
  lictxt="none"; [ -n "$lic" ] && lictxt=$(head -c 400 "$lic" | tr '\n' ' ' | grep -oiE 'Apache License|MIT License|GNU GENERAL PUBLIC LICENSE|GNU LESSER|Lesser General Public|Creative Commons|BSD|Mozilla Public|Public Domain|Permission is hereby granted|Eclipse Public|Unlicense|CC0' | head -1); [ -z "$lictxt" ] && lictxt="see:$(basename "$lic")"
  printf "%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n" "$d" "$nf" "$nl" "$ncp" "$nj" "$nd" "$pid" "$cics" "$sql" "$srt" "$red" "$odo" "$c3" "$gt" "$alt" "$pt" "$big" "$lictxt"
done
