#!/bin/sh
# Re-creates the console evidence in docs/evidence/logs/.
# Each sessions/NN-name.in file holds the keys "typed" in one real run of the
# program; it is piped into CourseTrack and everything printed is saved to
# logs/NN-name.log. --echo makes the program print each input line after its
# prompt, so the log reads like the screen of an interactive session.
#
# Run from anywhere:  sh docs/evidence/run-sessions.sh
set -e
cd "$(dirname "$0")/../.."
rm -rf out docs/evidence/output
mkdir -p docs/evidence/logs docs/evidence/output
javac -d out -sourcepath src src/coursetrack/Main.java

run() {
    name=$1
    shift
    java -cp out coursetrack.Main --echo "$@" \
        < "docs/evidence/sessions/$name.in" > "docs/evidence/logs/$name.log" 2>&1
    echo "recorded docs/evidence/logs/$name.log"
}

run 01-add-course
run 02-view-all              sample-data/courses.csv
run 03-search                sample-data/courses.csv
run 04-total-units           sample-data/courses.csv
run 05-save                  sample-data/courses.csv
run 06-load
run 07-exit-unsaved          sample-data/courses.csv
run 08-invalid-menu
run 09-field-validation
run 10-empty-list
run 11-load-unsaved-warning
run 12-extras-edit-delete    sample-data/courses.csv
run 13-round-trip
run 14-load-corrupted-file
run 15-load-missing-file
run 16-duplicate-code        sample-data/courses.csv
run 17-extras-sort           sample-data/courses.csv
run 18-extras-summary-overload sample-data/courses.csv
