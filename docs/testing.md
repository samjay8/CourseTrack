# CourseTrack: Testing and Evidence

**Author:** Samuel Ojetunde, COS 201 — Computer Programming I, MIVA Open University

This document proves the program was tested. It has three parts:

1. **Automated tests (T01-T45)**: JUnit 5 tests run with `./test.sh` or `test.bat`. Full output: [`evidence/junit-results.txt`](evidence/junit-results.txt).
2. **Console sessions (C01-C18)**: the real program run with scripted keyboard input. Inputs are in [`evidence/sessions/`](evidence/sessions/), the complete program output in [`evidence/logs/`](evidence/logs/), and a real screenshot of each run in [`screenshots/`](screenshots/).
3. **GUI checks (G01-G13)**: the real Swing window driven and captured on screen.

Every "Actual output" below is copied from those real runs, not written by hand.

**Result: 45 / 45 automated tests passed, 18 / 18 console sessions and 13 / 13 GUI checks behaved as expected.**

---

## How the evidence was produced

| Evidence | How | Re-create it |
|---|---|---|
| JUnit results | JUnit 5.10.2 console launcher (`lib/junit-platform-console-standalone-1.10.2.jar`) runs every test class. | `./test.sh --disable-ansi-colors > docs/evidence/junit-results.txt` |
| Console logs | Each `sessions/NN-name.in` file is piped into `java coursetrack.Main --echo`, and everything the program prints is saved to `logs/NN-name.log`. The `--echo` flag makes the program print each line it reads, so the log shows what was "typed" after each prompt, like an interactive screen. | `sh docs/evidence/run-sessions.sh` |
| Console screenshots | Each session was run again in a **real Windows console window** (`conhost.exe`, 90 x 48 characters) on Windows 11 with Java 17.0.21, and that window was captured with the Win32 `PrintWindow` API. These are real screen captures, not drawings. The window shows the last 48 lines of the session, so the menu printed at the start of a long session may be scrolled off the top. | (capture helper not part of the submission) |
| GUI screenshots | The real `CourseGUI` window was opened on the Windows desktop, its buttons were clicked programmatically (`doClick`, which fires the same events as a mouse click), and the screen area of the window and its dialogs was captured with `java.awt.Robot`. | (capture helper not part of the submission) |
| Saved files | Sessions C05 and C07 saved real files: [`evidence/output/saved-courses.csv`](evidence/output/saved-courses.csv) and [`evidence/output/exit-save.csv`](evidence/output/exit-save.csv). | created by `run-sessions.sh` |

Test data is my real course list in [`sample-data/courses.csv`](../sample-data/courses.csv) (9 courses, 20 units) plus the deliberately broken [`sample-data/corrupted.csv`](../sample-data/corrupted.csv). Where a session needed a course that is not on my list (to show adding, or to push the load over the limit), a clearly hypothetical one is used, for example STA201 with 6 units in C18.

---

## Part 1: Automated tests (JUnit 5)

Run: `./test.sh` (Linux/macOS/Git Bash) or `test.bat` (Windows). The summary from [`junit-results.txt`](evidence/junit-results.txt):

```text
[        45 tests found           ]
[        45 tests successful      ]
[         0 tests failed          ]
```

### 1a. Model and string processing ([`CourseTest`](../test/coursetrack/model/CourseTest.java))

| ID | Feature | Input | Expected | Actual | Result |
|---|---|---|---|---|---|
| T01 | Create valid course | `COS201`, `Computer Programming I`, 3 | Fields stored; level 200 | As expected | PASS |
| T02 | Code normalisation | `"  cos 201 "`, `"m t h 2 0 3"` | `COS201`, `MTH203` | As expected | PASS |
| T03 | Invalid code patterns | `CS21`, `COS2011`, `123COS`, `CO$201`, `COSABC`, `COURSE` | Each throws `InvalidCourseException` | All rejected | PASS |
| T04 | Blank / null code | `"   "`, `null` | `InvalidCourseException` | Rejected | PASS |
| T05 | Title Case rules | `"set, logic, AND algebra"`, `"SIWES i"`, `"introduction to r programming"`, `"MATHEMATICAL methods i"` | `Set, Logic, and Algebra`, `SIWES I`, `Introduction to R Programming`, `Mathematical Methods I` | As expected | PASS |
| T06 | Bad title | `"   "`, 61 characters | `InvalidCourseException` | Rejected | PASS |
| T07 | Units parsing | `" 4 "`, `"three"`, `"2.5"`, `""` | 4; others rejected with "whole number" message | As expected | PASS |
| T08 | Units boundaries | 1, 6, 0, 7, -3 | 1 and 6 accepted; 0, 7, -3 rejected | As expected | PASS |
| T09 | `equals`/`hashCode` | `COS201` vs `cos 201` (different title) vs `COS202` | First two equal with same hash; third not equal | As expected | PASS |
| T10 | `Comparable` | MTH201, COS203, GST201 | Sorted COS203, GST201, MTH201 | As expected | PASS |
| T11 | Immutability | `withTitle`, `withUnits` | New objects; original unchanged | As expected | PASS |

### 1b. Service rules and recursion ([`CourseManagerTest`](../test/coursetrack/service/CourseManagerTest.java))

These tests use a fake in-memory `CourseRepository`, so they test the rules without touching the disk.

| ID | Feature | Input | Expected | Actual | Result |
|---|---|---|---|---|---|
| T12 | Add and view | 3 courses | Size 3, insertion order kept | As expected | PASS |
| T13 | Duplicate code | Add `cos 201` when `COS201` exists | `DuplicateCourseException`; size still 3 | As expected | PASS |
| T14 | Read-only view | `getAll().remove(0)`, `clear()` | `UnsupportedOperationException` | As expected | PASS |
| T15 | Recursive search: first, middle, last | `COS201`, `mth201`, `" gst 201 "` | Found each | As expected | PASS |
| T16 | Search not found | `PHY101` | `CourseNotFoundException` | As expected | PASS |
| T17 | Search empty list | `COS201` on empty list | `CourseNotFoundException` (base case at once) | As expected | PASS |
| T18 | Search with bad code | `12AB` | `InvalidCourseException` | As expected | PASS |
| T19 | Recursive total | empty list; then 3+3+2 | 0; then 8 | As expected | PASS |
| T20 | Recursion depth | 200 courses | Total 200, last one found, no stack overflow | As expected | PASS |
| T21 | Sorting | by code, title, units | Correct orders; stored order unchanged | As expected | PASS |
| T22 | Edit | `gst201` → new title, 3 units; missing code; 9 units | Title Case title, total 9; not-found and invalid errors | As expected | PASS |
| T23 | Delete | `MTH201` twice | Removed; second delete throws `CourseNotFoundException` | As expected | PASS |
| T24 | Summary and over-limit warning | 8 units, then 32 units | Under minimum; then over 24 with `WARNING` | As expected | PASS |
| T25 | Unsaved-changes flag | add, save, delete | true, false, true | As expected | PASS |
| T26 | Failure keeps state | save fails; load of missing file | Flag stays true; list unchanged | As expected | PASS |

### 1c. File storage ([`FileCourseRepositoryTest`](../test/coursetrack/storage/FileCourseRepositoryTest.java))

Each test uses its own empty temporary folder (`@TempDir`).

| ID | Feature | Input | Expected | Actual | Result |
|---|---|---|---|---|---|
| T27 | File format | 3 courses | Header + 3 lines | As expected | PASS |
| T28 | Commas and quotes round trip | `Entrepreneurship, Innovation and Leadership`, `The "Hard" Maths` | Written quoted/escaped; read back identical | As expected | PASS |
| T29 | Save empty list | empty list | Header-only file; loads as 0 courses | As expected | PASS |
| T30 | Missing file | `does-not-exist.csv` | `StorageException` "File not found", cause `FileNotFoundException` | As expected | PASS |
| T31 | Corrupted lines | 10-line file with 6 bad lines and 1 blank | 2 kept, 6 skipped, reported as lines 3, 4, 6, 7, 8, 9 | As expected | PASS |
| T32 | Messy file | no header, byte-order mark, extra spaces | Loads `COS201`, `Computer Programming I`, 3 | As expected | PASS |
| T33 | Atomic replace | save twice to the same file | Second save replaces the first; no temp file left | As expected | PASS |
| T34 | Folder missing | save into `no-such-folder/` | `StorageException` | As expected | PASS |
| T35 | CSV parser | plain, quoted, `""` escape, empty fields | Correct field lists | As expected | PASS |
| T36 | Manager + file round trip | save from one manager, load into another | Same courses and total | As expected | PASS |
| T45 | **Real semester file** | `sample-data/courses.csv` | 9 courses, **20 units**; `MTH203` = `Set, Logic, and Algebra`; saved as `MTH203,"Set, Logic, and Algebra",2` and identical after reload | As expected | PASS |

### 1d. Console menu driven by scripted input ([`ConsoleMenuTest`](../test/coursetrack/ui/ConsoleMenuTest.java))

| ID | Feature | Input lines | Expected | Actual | Result |
|---|---|---|---|---|---|
| T37 | Invalid menu input | `abc`, blank, `0`, `12`, `4`, `7` | Four error messages, then total 0, then Goodbye | As expected | PASS |
| T38 | Add with retries | bad code, good code, blank title, title, `x`, `9`, `3` | One error per bad value, then "Added: COS201 - Computer Programming I (3 units)" | As expected | PASS |
| T39 | Duplicate in menu | add `COS201`, add `cos201` | "A course with code COS201 already exists." | As expected | PASS |
| T40 | Search cases | search empty, add MTH201, search PHY101, search `mth 201` | Empty message, not found, found | As expected | PASS |
| T41 | Exit saves changes | add, `7`, `maybe`, `y`, file | Re-prompt for y/n, file written, flag cleared | As expected | PASS |
| T42 | Load warns about unsaved data | add, `6`, `n` | Warning, "Load cancelled.", course kept | As expected | PASS |
| T43 | Load missing file (menu) | `6`, missing path | "File not found", "Your current courses are unchanged." | As expected | PASS |
| T44 | Input ends early | `1`, `COS201`, then end of input | "Input ended. Exiting CourseTrack." (no crash) | As expected | PASS |

---

## Part 2: Console sessions on the real program

Each row is one real run. "Actual output" is quoted from the log file; the screenshot is a capture of the same session in a real console window.

| ID | Feature | Input (typed) | Expected output | Actual output (from log) | Result | Log / Screenshot |
|---|---|---|---|---|---|---|
| C01 | **1. Add Course** | `1`, `cos 201`, `computer programming i`, `3` | Course added with normalised code and Title Case | `Added: COS201 - Computer Programming I (3 units)` | PASS | [log](evidence/logs/01-add-course.log) / [png](screenshots/console-1-add-course.png) |
| C02 | **2. View All Courses** | start with `courses.csv`, `2` | Aligned table of 9 courses, total 20 | `9 course(s)   Total units: 20` with all 9 rows aligned | PASS | [log](evidence/logs/02-view-all.log) / [png](screenshots/console-2-view-all.png) |
| C03 | **3. Search** found and not found | `3`, `mth 201`; `3`, `PHY101` | Found MTH201; not-found message | `Found: MTH201 - Mathematical Methods I (2 units)`; `[!] No course found with code PHY101.` | PASS | [log](evidence/logs/03-search.log) / [png](screenshots/console-3-search-found-and-not-found.png) |
| C04 | **4. Compute Total Units** (+ summary) | `4`, `11` | 20 units; within 15-24 | `Total units for 9 course(s): 20`; `Status : Within the normal load (15-24 units); 4 unit(s) remaining.` | PASS | [log](evidence/logs/04-total-units.log) / [png](screenshots/console-4-total-units.png) |
| C05 | **5. Save to File** | `5`, `docs/evidence/output/saved-courses.csv` | 9 courses saved; MTH203 title quoted | `Saved 9 course(s) to docs\evidence\output\saved-courses.csv.` File line: `MTH203,"Set, Logic, and Algebra",2` | PASS | [log](evidence/logs/05-save.log) / [png](screenshots/console-5-save.png) / [file](evidence/output/saved-courses.csv) |
| C06 | **6. Load from File** | `6`, `sample-data/courses.csv` | 9 courses loaded | `Loaded 9 course(s) from sample-data\courses.csv.` | PASS | [log](evidence/logs/06-load.log) / [png](screenshots/console-6-load.png) |
| C07 | **7. Exit** with unsaved changes | add ENT211, `7`, `maybe`, `y`, file | Asks to save, rejects `maybe`, saves, exits | `[!] Please answer y or n.` … `Saved 1 course(s) to docs\evidence\output\exit-save.csv.` `Goodbye!` | PASS | [log](evidence/logs/07-exit-unsaved.log) / [png](screenshots/console-7-exit-unsaved-prompt.png) |
| C08 | Invalid menu choice | `abc`, blank, `0`, `15`, `2.5`, `4` | Error for each, then works | `"abc" is not a number…`, `Please type a number.`, `0 is out of range…`, `15 is out of range…`, `"2.5" is not a number…`, then `Total units: 0` | PASS | [log](evidence/logs/08-invalid-menu.log) / [png](screenshots/error-invalid-menu-choice.png) |
| C09 | Invalid code, title, units | `cs 21`, `cos2011`, `cos 203`, blank, `discrete   structures`, `two`, `0`, `9`, `2` | Re-prompt on each bad value | `Invalid course code "cs 21"…`, `…"cos2011"…`, `Course title cannot be empty.`, `Units must be a whole number, but "two" is not.`, `…but 0 was given.`, `…but 9 was given.`, `Added: COS203 - Discrete Structures (2 units)` | PASS | [log](evidence/logs/09-field-validation.log) / [png](screenshots/error-invalid-code-title-units.png) |
| C10 | Empty list: save, total, search | `5`, `n`, `4`, `3` | Confirm before empty save; total 0; no search | `The list is empty. Save an empty file anyway? (y/n): n` `Save cancelled.` `Total units: 0 (no courses recorded yet)` `There are no courses to search.` | PASS | [log](evidence/logs/10-empty-list.log) / [png](screenshots/edge-empty-list.png) |
| C11 | Load with unsaved changes | add DTS201, `6`, `n`, `6`, `y`, file | Warn; `n` keeps data; `y` loads | `Load cancelled. Your current courses are unchanged.` then `Loaded 9 course(s)…` | PASS | [log](evidence/logs/11-load-unsaved-warning.log) / [png](screenshots/edge-load-unsaved-warning.png) |
| C12 | Extra: Edit and Delete | `8`, `mth203`, `set,   logic, AND algebra`, Enter; `9`, `ENT211`, `y` | Title normalised, units kept; ENT211 deleted | `Updated: MTH203 - Set, Logic, and Algebra (2 units)` `Deleted ENT211.` | PASS | [log](evidence/logs/12-extras-edit-delete.log) / [png](screenshots/extra-edit-delete.png) |
| C13 | **Save/load round trip** with commas | `6`, `docs/evidence/output/saved-courses.csv`, `2` | Same 9 courses, 20 units; `Set, Logic, and Algebra` intact | `Loaded 9 course(s)…`; row `7. MTH203 Set, Logic, and Algebra 2`; `Total units: 20` | PASS | [log](evidence/logs/13-round-trip.log) / [png](screenshots/edge-save-load-round-trip.png) |
| C14 | Corrupted file | `6`, `sample-data/corrupted.csv` | Keep good lines, list bad line numbers | `Loaded 3 course(s)…` `Skipped 6 invalid line(s):` Lines 3, 4, 6, 7, 8, 9 with reasons | PASS | [log](evidence/logs/14-load-corrupted-file.log) / [png](screenshots/error-corrupted-file-lines.png) |
| C15 | Missing file | `6`, `missing-file.csv` | Friendly message, data unchanged | `[!] File not found: missing-file.csv. Check the name, or save a list first.` `Your current courses are unchanged.` | PASS | [log](evidence/logs/15-load-missing-file.log) / [png](screenshots/error-missing-file.png) |
| C16 | Duplicate code | `1`, `cos 201` (already loaded) | Refused before title is asked | `[!] A course with code COS201 already exists.` | PASS | [log](evidence/logs/16-duplicate-code.log) / [png](screenshots/error-duplicate-code.png) |
| C17 | Extra: Sort by units | `10`, `3` | 3-unit courses first, ties by code | COS201, DTS299 (3 units) then the 2-unit courses in code order | PASS | [log](evidence/logs/17-extras-sort.log) / [png](screenshots/extra-sort-by-units.png) |
| C18 | Extra: Summary over the limit | add hypothetical STA201 (6 units), `11` | WARNING above 24 | `Total units : 26` `Status : WARNING: 26 units is above the maximum load of 24 by 2 unit(s).` | PASS | [log](evidence/logs/18-extras-summary-overload.log) / [png](screenshots/extra-summary-overload-warning.png) |

**Round-trip detail (C05 → C13).** The saved file [`saved-courses.csv`](evidence/output/saved-courses.csv) contains:

```text
code,title,units
ENT211,Entrepreneurship and Innovation,2
MTH201,Mathematical Methods I,2
COS201,Computer Programming I,3
COS203,Discrete Structures,2
DTS201,Introduction to Data Science,2
DTS211,Introduction to R Programming,2
MTH203,"Set, Logic, and Algebra",2
MTH209,Introduction to Numerical Analysis,2
DTS299,SIWES I,3
```

`Set, Logic, and Algebra` has two commas, so it is written in quotes. Without quoting, the loader would see five fields on that line and reject it. C13 loads this file back and shows the title unchanged and the total still 20; T45 checks the same thing automatically.

---

## Part 3: GUI checks

The GUI uses the same `CourseManager`, so the rules are already covered above. These checks show each GUI feature working on screen.

| ID | Feature | Action | Expected | Actual | Result | Screenshot |
|---|---|---|---|---|---|---|
| G01 | Main window with data | Start with `courses.csv` | 9 rows in the table, status "Loaded 9 course(s)" | As expected | PASS | [png](screenshots/gui-1-main-window-with-data.png) |
| G02 | Invalid code dialog | Code `cs 21`, click Add | Error dialog with the invalid-code message | "Invalid course code "cs 21"…" | PASS | [png](screenshots/gui-2-error-invalid-code.png) |
| G03 | Invalid units dialog | Code `sta 201`, units `9`, click Add | Error dialog | "Units must be between 1 and 6, but 9 was given." | PASS | [png](screenshots/gui-3-error-invalid-units.png) |
| G04 | Add | units `3`, click Add | Row 10 STA201 added | As expected | PASS | [png](screenshots/gui-4-after-add.png) |
| G05-G06 | Search | Search `mth 201` | Row selected, "Found: MTH201…" | As expected | PASS | [png](screenshots/gui-5-search-dialog.png), [png](screenshots/gui-6-search-found.png) |
| G07 | Total units | Click Total Units | 23 units for 10 courses | As expected | PASS | [png](screenshots/gui-7-total-units.png) |
| G08 | Summary | Click Summary | Within 15-24 | As expected | PASS | [png](screenshots/gui-8-summary.png) |
| G09 | Sort | Choose "units (highest first)", click Sort | 3-unit courses first | As expected | PASS | [png](screenshots/gui-9-sorted-by-units.png) |
| G10 | Exit with unsaved changes | Click Exit | Yes / No / Cancel dialog | As expected | PASS | [png](screenshots/gui-10-exit-unsaved-prompt.png) |
| G11 | File chooser | Load (accept unsaved warning) | `JFileChooser` filtered to CSV | As expected | PASS | [png](screenshots/gui-11-load-file-chooser.png) |
| G12 | Load report | Open `corrupted.csv` | Warning listing lines 3, 4, 6, 7, 8, 9 | As expected | PASS | [png](screenshots/gui-12-load-report-corrupted-lines.png) |
| G13 | After load | Close report | 3 valid courses shown | As expected | PASS | [png](screenshots/gui-13-after-load.png) |

---

## Part 4: Coverage map (requirement → tests)

| Requirement / risk | Automated tests | Console / GUI evidence |
|---|---|---|
| 1 Add Course | T01, T12, T38 | C01, C09, G04 |
| 2 View All Courses | T12, T14 | C02, C13, G01 |
| 3 Search by Code (recursive) | T15, T16, T17, T18, T20, T40 | C03, G05-G06 |
| 4 Compute Total Units (recursive) | T19, T20, T45 | C04, C13, G07 |
| 5 Save to File | T27, T28, T29, T33, T34, T36, T45 | C05, C07 |
| 6 Load from File | T30, T31, T32, T36, T43, T45 | C06, C13, C14, C15, G11-G13 |
| 7 Exit Program | T41 | C07, G10 |
| Invalid input handling | T03-T08, T37, T38, T44 | C08, C09, G02, G03 |
| String processing (trim, case, Title Case, pattern) | T02, T05, T15, T32 | C01, C09, C12 |
| Duplicate codes | T13, T39 | C16 |
| Exceptions (hierarchy and messages) | T16, T18, T26, T30, T34 | C14, C15 |
| Recursion (search, total, re-prompt) | T15-T20, T37, T38 | C03, C04, C08, C09 |
| Classes and objects, encapsulation | T09-T11, T14 | — |
| Extras: edit, delete, sort, summary | T21-T24 | C12, C17, C18, G08, G09 |
| Unsaved-change safety | T25, T26, T41, T42 | C07, C11, G10 |

By package: `model` T01-T11, `service` T12-T26, `storage` T27-T36 and T45, `ui.console` T37-T44, `ui.gui` G01-G13 (manual on-screen checks; no automated GUI tests).
