#!/bin/bash
# usage: unit.sh <project> <main-src-globs...> -- <test files...>   (pure-Java classes only)
J=/usr/local/lib/python3.11/dist-packages/jdk4py/java-runtime/bin/java
P=$1; shift; cd $P; MAIN=(); TEST=(); t=0
for a in "$@"; do if [ "$a" = "--" ]; then t=1; elif [ $t = 0 ]; then MAIN+=("$a"); else TEST+=("$a"); fi; done
rm -rf /tmp/unit && mkdir -p /tmp/unit
$J -jar /home/user/tools/ecj.jar -21 -nowarn -d /tmp/unit $(find /home/user/tools/junitstub /home/user/tools/runner -name '*.java') "${MAIN[@]}" "${TEST[@]}" 2>&1 | tail -30
CLASSES=$(for f in "${TEST[@]}"; do echo $f | sed 's|src/test/java/||;s|\.java$||;s|/|.|g'; done)
$J -cp /tmp/unit RunTests $CLASSES
