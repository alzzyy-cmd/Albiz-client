#!/bin/bash
# usage: cmp.sh files... : compares ECJ non-noise error message multiset between orig/PC and Albiz-PC for each file
for f in "$@"; do
 for P in /home/user/work/orig/PC /home/user/Albiz-client/Albiz-PC; do
  if [ -f $P/$f ]; then /home/user/tools/check.sh $P $f > /dev/null; python3 - $f <<'PY' > /tmp/cmp_$(basename $P)_$(basename $f).txt
import re,sys,collections
txt=open('/tmp/ecj.out',errors='replace').read();c=collections.Counter()
for b in re.split(r'\n----------\n',txt):
    m=re.search(r'ERROR in (\S+) \(at line (\d+)\)',b)
    if m and m[1].endswith(sys.argv[1]): c[b.strip().split('\n')[-1]]+=1
for k,v in sorted(c.items()): print(v,k)
PY
  else : > /tmp/cmp_$(basename $P)_$(basename $f).txt; fi
 done
 echo "== $f"; diff /tmp/cmp_PC_$(basename $f).txt /tmp/cmp_Albiz-PC_$(basename $f).txt && echo "  identical error profile to original ($(wc -l < /tmp/cmp_PC_$(basename $f).txt) distinct unresolved msgs)"
done
