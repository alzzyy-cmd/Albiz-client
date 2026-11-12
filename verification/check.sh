#!/bin/bash
# usage: check.sh <project-dir> files...   -> ECJ errors restricted to given files, minus unresolved-MC noise
J=/usr/local/lib/python3.11/dist-packages/jdk4py/java-runtime/bin/java
P=$1; shift
cd $P
$J -Xmx1500m -jar /home/user/tools/ecj.jar -21 -nowarn -proceedOnError -d none -sourcepath src/main/java -cp "$(ls /home/user/tools/libs/*.jar 2>/dev/null | tr '\n' ':')" "$@" > /tmp/ecj.out 2>&1
python3 - "$@" <<'PY'
import re,sys
files=set(sys.argv[1:])
txt=open('/tmp/ecj.out',errors='replace').read()
blocks=re.split(r'\n----------\n',txt)
keep=0;noise=0
for b in blocks:
    m=re.search(r'(ERROR|WARNING) in (\S+) \(at line (\d+)\)\n(.*)',b,re.S)
    if not m or m[1]!='ERROR':continue
    f=m[2]
    if not any(f.endswith(x) for x in files):continue
    msg=b.strip().split('\n')[-1]
    if re.search(r'cannot be resolved|is not visible|The import .* cannot|is undefined for the type (Object|class_)|The hierarchy of the type|inconsistent|indirectly referenced|Cannot infer type|The target type of this expression|Incompatible operand types|The operator .* is undefined|Type mismatch: cannot convert from element type Object',msg):
        noise+=1;continue
    keep+=1;print(b.strip()[:700]);print('---')
print(f'REAL-CANDIDATE errors: {keep}; unresolved-dependency noise: {noise}')
PY
