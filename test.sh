#!/bin/sh
# Compile the program and the JUnit 5 tests, then run every test.
# Exits with a non-zero status if any test fails.
set -e
cd "$(dirname "$0")"
JUNIT=lib/junit-platform-console-standalone-1.10.2.jar
# Java on Windows (Git Bash) separates class-path entries with ';', elsewhere ':'
SEP=":"
case "$(uname -s)" in MINGW*|MSYS*|CYGWIN*) SEP=";" ;; esac

rm -rf out
javac -d out -sourcepath src src/coursetrack/Main.java
javac -d out/test -cp "out${SEP}${JUNIT}" test/coursetrack/*/*.java
java -jar "$JUNIT" execute --class-path "out${SEP}out/test" --scan-class-path out/test \
     --details=tree --disable-banner "$@"
