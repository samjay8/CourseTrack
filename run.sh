#!/bin/sh
# Compile CourseTrack and start it.
#   ./run.sh          console menu
#   ./run.sh --gui    Swing window
set -e
cd "$(dirname "$0")"
rm -rf out
# -sourcepath lets javac find every class Main uses, so no file list is needed
javac -d out -sourcepath src src/coursetrack/Main.java
java -cp out coursetrack.Main "$@"
