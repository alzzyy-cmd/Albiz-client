#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
: "${JAVA_HOME:?Use a Java 21 JDK}"
bash gradlew addonClasspath --max-workers=1
rm -rf larp-addon/classes
mkdir -p larp-addon/classes larp-addon/test-classes
"$JAVA_HOME/bin/javac" -proc:none -encoding UTF-8 --release 21 -cp "$(cat logs/addon-classpath.txt)" -d larp-addon/classes $(find larp-addon/src/main/java -name '*.java')
"$JAVA_HOME/bin/javac" -proc:none --release 21 -cp larp-addon/classes -d larp-addon/test-classes larp-addon/tests/RulesCpuTest.java
"$JAVA_HOME/bin/java" -cp larp-addon/classes:larp-addon/test-classes RulesCpuTest
python tools/package_revision.py
