<div align="center">

# 🎓 CourseTrack

### A Student Course Management Mini-System in Java

**COS 201 — Computer Programming I · Mid-Semester Lab Assessment**

Built by **Samuel Ojetunde** · B.Sc. Data Science, Year 2 · MIVA Open University

[![Java 17](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](#-getting-started)
[![Swing GUI](https://img.shields.io/badge/GUI-Swing-5382A1?style=for-the-badge)](#-screenshots)
[![JUnit 5](https://img.shields.io/badge/Tests-45%20passing-25A162?style=for-the-badge&logo=junit5&logoColor=white)](docs/testing.md)
[![No frameworks](https://img.shields.io/badge/Build-plain%20javac-555555?style=for-the-badge)](#-getting-started)

[Overview](#-overview) · [Features](#-features) · [Getting Started](#-getting-started) · [Project Structure](#-project-structure) · [Screenshots](#-screenshots) · [Checklist](#-assessment-requirement-checklist)

</div>

---

## 📖 Overview

**CourseTrack** helps a student record and manage the courses taken in a semester. It can add courses, display them in a table, search by course code, compute the total credit units, and save or load the list from a CSV file. It has two interfaces over the same classes: an organised **console menu** (the main deliverable) and a simple **Swing GUI**.

The sample data is my own semester: 9 courses, **20 units**.

| Code | Title | Units |
|---|---|---|
| ENT211 | Entrepreneurship and Innovation | 2 |
| MTH201 | Mathematical Methods I | 2 |
| COS201 | Computer Programming I | 3 |
| COS203 | Discrete Structures | 2 |
| DTS201 | Introduction to Data Science | 2 |
| DTS211 | Introduction to R Programming | 2 |
| MTH203 | Set, Logic, and Algebra | 2 |
| MTH209 | Introduction to Numerical Analysis | 2 |
| DTS299 | SIWES I | 3 |

## 📚 Documentation

| Document | What it contains |
|---|---|
| [`docs/assignment-brief.md`](docs/assignment-brief.md) | The official assessment brief |
| [`docs/report.pdf`](docs/report.pdf) ([markdown](docs/report.md)) | The 2-page report required by the brief |
| [`docs/design.md`](docs/design.md) | Problem definition, class diagram, algorithm + pseudocode + flowchart for every operation, recursion traces, exceptions and edge cases, design trade-offs, Big-O |
| [`docs/testing.md`](docs/testing.md) | 45 automated tests, 18 console sessions and 13 GUI checks, each with input, expected and actual output, PASS/FAIL, and a coverage map |
| [`docs/evidence/`](docs/evidence/) | Raw JUnit output, scripted console inputs and the real program output for each session |
| [`docs/screenshots/`](docs/screenshots/) | Real screenshots of the console and the GUI |
| [`docs/diagrams/`](docs/diagrams/) | Mermaid sources and rendered PNG flowcharts and class diagram |

## ✨ Features

### Required operations (menu options 1-7)
1. **Add Course**: code, title and units, each validated and re-prompted until correct; duplicate codes refused.
2. **View All Courses**: aligned table with numbering, course count and total units.
3. **Search Course by Code**: case- and space-insensitive (`mth 201` finds `MTH201`), using a **recursive** search.
4. **Compute Total Units**: **recursive** sum (an empty list gives 0).
5. **Save to File**: CSV with a header row and proper quoting; atomic save (temp file, then rename).
6. **Load from File**: reports the line number of every bad line and keeps the good ones; warns before replacing unsaved changes.
7. **Exit Program**: offers to save unsaved changes first.

### Extra operations (options 8-11, clearly labelled)
8. **Edit Course** (title and units), 9. **Delete Course**, 10. **Sort / View** by code, title or units, 11. **Semester Summary** with a warning above the 24-unit maximum load.

### Under the hood
- **Layered packages**: `model`, `service`, `storage`, `exception`, `ui.console`, `ui.gui`.
- **Immutable `Course`** with `equals`/`hashCode` on the code and `Comparable` (natural order by code).
- **`CourseRepository` interface** so the storage format can be swapped; `FileCourseRepository` is the CSV implementation.
- **Checked exception hierarchy**: `CourseException` → `InvalidCourseException`, `DuplicateCourseException`, `CourseNotFoundException`, `StorageException`.
- **Three uses of recursion**: search (`indexOf`), total (`sumUnits`), and console re-prompting (`readChoice`, `readValid`, `readYesNo`).
- **String processing**: trimming, code normalisation (`cos 201` → `COS201`), pattern check `^[A-Z]{3}\d{3}$`, Title Case that keeps `and`/`of`/`to` lower case, Roman numerals (`I`) and acronyms (`SIWES`).
- **Javadoc** on every public class and method.

## 🛠 Tech Stack

| | |
|---|---|
| Language | Java 17 (records, `switch`, try-with-resources, `java.nio.file`) |
| GUI | Swing (`JFrame`, `JTable`, `JOptionPane`, `JFileChooser`) |
| Tests | JUnit 5.10.2 via the standalone console launcher in [`lib/`](lib/) (tests only; the program has no dependencies) |
| Build | Plain `javac`, no Maven or Gradle |

## 🚀 Getting Started

You need a **JDK 17 or newer** (`javac -version`).

| Task | Windows | Linux / macOS / Git Bash |
|---|---|---|
| Run the console menu | `run.bat` | `./run.sh` |
| Run with my courses loaded | `run.bat sample-data\courses.csv` | `./run.sh sample-data/courses.csv` |
| Run the GUI | `run.bat --gui` | `./run.sh --gui` |
| Run all tests | `test.bat` | `./test.sh` |
| Re-create the console evidence | | `sh docs/evidence/run-sessions.sh` |

Manual commands (what the scripts do):

```sh
javac -d out -sourcepath src src/coursetrack/Main.java    # compiles every class Main uses
java -cp out coursetrack.Main                              # console
java -cp out coursetrack.Main --gui                        # GUI
```

Optional HTML Javadoc (not committed, to keep the repository small): `javadoc -d docs/api -sourcepath src -subpackages coursetrack`

## 📁 Project Structure

```text
CourseTrack/
├── src/coursetrack/
│   ├── Main.java                      entry point: console by default, --gui for Swing
│   ├── model/
│   │   ├── Course.java                immutable course; equals/hashCode/compareTo on code
│   │   └── CourseValidator.java       trimming, casing, pattern and range rules
│   ├── service/
│   │   ├── CourseManager.java         ArrayList, recursive search and total, edit, delete, sort
│   │   ├── SortField.java             enum of sort orders with their comparators
│   │   └── CourseSummary.java         record: count, total, load-limit status
│   ├── storage/
│   │   ├── CourseRepository.java      interface: save / load
│   │   ├── FileCourseRepository.java  CSV with quoting, atomic save, line-by-line validation
│   │   └── LoadResult.java            courses loaded + "Line n: reason" for skipped lines
│   ├── exception/                     CourseException and its four subclasses
│   └── ui/
│       ├── console/ConsoleMenu.java   menu loop, switch, recursive re-prompts, table output
│       └── gui/CourseGUI.java         Swing window (+ CourseTableModel.java)
├── test/coursetrack/                  JUnit 5 tests T01-T45 (model, service, storage, ui)
├── lib/                               JUnit console launcher jar (tests only)
├── sample-data/
│   ├── courses.csv                    my 9 courses, 20 units
│   └── corrupted.csv                  deliberately broken file for the error-handling demo
├── docs/                              brief, report, design, testing, evidence, screenshots, diagrams
├── run.sh / run.bat                   compile and run
└── test.sh / test.bat                 compile and run the tests
```

## 🖼 Screenshots

All screenshots are real captures of the program running on Windows 11 with Java 17 (see [how they were captured](docs/testing.md#how-the-evidence-was-produced)).

### Console: the seven required operations

| 1. Add Course | 2. View All Courses |
|---|---|
| ![Add course](docs/screenshots/console-1-add-course.png) | ![View all](docs/screenshots/console-2-view-all.png) |
| **3. Search (found and not found)** | **4. Compute Total Units (+ summary)** |
| ![Search](docs/screenshots/console-3-search-found-and-not-found.png) | ![Total units](docs/screenshots/console-4-total-units.png) |
| **5. Save to File** | **6. Load from File** |
| ![Save](docs/screenshots/console-5-save.png) | ![Load](docs/screenshots/console-6-load.png) |
| **7. Exit with unsaved changes** | **Save/load round trip (commas survive)** |
| ![Exit](docs/screenshots/console-7-exit-unsaved-prompt.png) | ![Round trip](docs/screenshots/edge-save-load-round-trip.png) |

### Console: errors and edge cases

| Invalid menu choice | Invalid code, title and units |
|---|---|
| ![Invalid menu](docs/screenshots/error-invalid-menu-choice.png) | ![Invalid fields](docs/screenshots/error-invalid-code-title-units.png) |
| **Duplicate code** | **Missing file** |
| ![Duplicate](docs/screenshots/error-duplicate-code.png) | ![Missing file](docs/screenshots/error-missing-file.png) |
| **Corrupted file lines** | **Empty list** |
| ![Corrupted](docs/screenshots/error-corrupted-file-lines.png) | ![Empty list](docs/screenshots/edge-empty-list.png) |
| **Load warns about unsaved changes** | **Extras: edit and delete** |
| ![Load warning](docs/screenshots/edge-load-unsaved-warning.png) | ![Edit delete](docs/screenshots/extra-edit-delete.png) |
| **Extras: sort by units** | **Extras: summary over the limit** |
| ![Sort](docs/screenshots/extra-sort-by-units.png) | ![Summary warning](docs/screenshots/extra-summary-overload-warning.png) |

### GUI

| Main window with my courses | Error dialog |
|---|---|
| ![GUI main](docs/screenshots/gui-1-main-window-with-data.png) | ![GUI error](docs/screenshots/gui-2-error-invalid-code.png) |
| **Search result** | **Total units** |
| ![GUI search](docs/screenshots/gui-6-search-found.png) | ![GUI total](docs/screenshots/gui-7-total-units.png) |
| **Exit with unsaved changes** | **Load report for a corrupted file** |
| ![GUI exit](docs/screenshots/gui-10-exit-unsaved-prompt.png) | ![GUI load report](docs/screenshots/gui-12-load-report-corrupted-lines.png) |

More GUI captures (invalid units, add, search dialog, summary, sort, file chooser, after load) are in [`docs/screenshots/`](docs/screenshots/).

## ✅ Assessment Requirement Checklist

### From the brief

| # | Requirement | Where it is satisfied | Evidence |
|---|---|---|---|
| 1.1 | Add courses (code, title, unit) | [`ConsoleMenu.addCourse`](src/coursetrack/ui/console/ConsoleMenu.java#L138), [`CourseManager.addCourse`](src/coursetrack/service/CourseManager.java#L64), [`Course`](src/coursetrack/model/Course.java) | C01, T12, T38 |
| 1.2 | Store courses in a manageable structure | `ArrayList<Course>` in [`CourseManager`](src/coursetrack/service/CourseManager.java) | T12, T14 |
| 1.3 | Display all courses clearly | [`ConsoleMenu.printTable`](src/coursetrack/ui/console/ConsoleMenu.java#L169), GUI `JTable` | C02 |
| 1.4 | Search by code | [`CourseManager.findByCode`](src/coursetrack/service/CourseManager.java#L123) | C03, T15-T18 |
| 1.5 | Calculate total units | [`CourseManager.totalUnits`](src/coursetrack/service/CourseManager.java#L159) | C04 (20 units), T19 |
| 1.6 | Save to a file | [`FileCourseRepository.save`](src/coursetrack/storage/FileCourseRepository.java#L51) | C05, T27-T29 |
| 1.7 | Load from a file | [`FileCourseRepository.load`](src/coursetrack/storage/FileCourseRepository.java#L104) | C06, C13, T30-T32 |
| 2.1 | Classes and objects | 17 classes, interfaces, enums and records in 7 packages; [class diagram](docs/design.md#2-architecture-and-class-diagram) | T01-T11 |
| 2.2 | Methods for operations | One method per operation in [`ConsoleMenu`](src/coursetrack/ui/console/ConsoleMenu.java) and [`CourseManager`](src/coursetrack/service/CourseManager.java) | [design §4](docs/design.md#4-operations-algorithm-pseudocode-and-flowchart) |
| 2.3 | Decision making for menu selection | `switch` in [`ConsoleMenu.handleChoice`](src/coursetrack/ui/console/ConsoleMenu.java#L118) | C08, T37 |
| 2.4 | Handling invalid input | [`readChoice`](src/coursetrack/ui/console/ConsoleMenu.java#L378), [`readValid`](src/coursetrack/ui/console/ConsoleMenu.java#L404), [`readYesNo`](src/coursetrack/ui/console/ConsoleMenu.java#L415) | C08, C09, T37, T38 |
| 2.5 | String processing (trim, casing, validation) | [`CourseValidator`](src/coursetrack/model/CourseValidator.java): [`normalizeCode`](src/coursetrack/model/CourseValidator.java#L48), [`toTitleCase`](src/coursetrack/model/CourseValidator.java#L95), [`parseUnits`](src/coursetrack/model/CourseValidator.java#L145) | T02-T07, C09 |
| 2.6 | Clean, structured style | Layered packages, Javadoc, single-purpose methods | [design §2](docs/design.md#2-architecture-and-class-diagram) |
| 3.1 | Iteration (loops) | menu `while` loop in [`run`](src/coursetrack/ui/console/ConsoleMenu.java#L76); `for` loops in `printTable`, `toTitleCase`; `while` read loop in `load` | C02 |
| 3.2 | Recursion | [`indexOf`](src/coursetrack/service/CourseManager.java#L142), [`sumUnits`](src/coursetrack/service/CourseManager.java#L170), [`readChoice`](src/coursetrack/ui/console/ConsoleMenu.java#L378) | [design §5](docs/design.md#5-recursion-base-cases-recursive-cases-and-traces), T15-T20 |
| 3.3 | Error handling with exceptions | [`exception/`](src/coursetrack/exception/) hierarchy; `try/catch` in both UIs | [design §7](docs/design.md#7-exceptions-and-edge-conditions) |
| 3.4 | File reading and writing | `BufferedReader`/`BufferedWriter` with try-with-resources in [`FileCourseRepository`](src/coursetrack/storage/FileCourseRepository.java) | T27-T36, T45 |
| 3.5 | GUI or organised console | [`ConsoleMenu`](src/coursetrack/ui/console/ConsoleMenu.java) and [`CourseGUI`](src/coursetrack/ui/gui/CourseGUI.java) | Screenshots above |
| 4.1-4.7 | Operations 1-7 | Menu options 1-7 in [`printMenu` / `handleChoice`](src/coursetrack/ui/console/ConsoleMenu.java#L118); GUI buttons 1-7 | C01-C07 |
| S1 | All source code files | [`src/`](src/), [`test/`](test/) | |
| S2 | Screenshot of the program running | [`docs/screenshots/`](docs/screenshots/) (18 console + 13 GUI) | |
| S3 | 1-2 page report: what it does, how features work, challenges/error handling, where recursion is used | [`docs/report.pdf`](docs/report.pdf) | |

### From the lecturer's feedback on earlier labs

| Feedback point | How this submission addresses it | Where |
|---|---|---|
| Missing supporting documentation | README, design, testing and report documents | [Documentation table](#-documentation) |
| Missing problem definition | Inputs, outputs, processing, constraints | [design §1](docs/design.md#1-problem-definition) |
| Missing algorithm and pseudocode | Algorithm and pseudocode for the menu and all 7 operations | [design §4](docs/design.md#4-operations-algorithm-pseudocode-and-flowchart) |
| Missing flowchart | 8 flowcharts + class diagram, in Mermaid and as PNG | [design §4](docs/design.md#4-operations-algorithm-pseudocode-and-flowchart), [`docs/diagrams/`](docs/diagrams/) |
| Not enough test cases with corresponding outputs | 45 automated + 18 console + 13 GUI cases, each with expected and actual output | [`docs/testing.md`](docs/testing.md) |
| No proof the program was tested | Raw JUnit output and the real program output of every session | [`docs/evidence/`](docs/evidence/) |
| No screenshots of execution | 31 real screenshots | [Screenshots](#-screenshots) |
| Exceptions and edge conditions not documented | Exception hierarchy and 18 edge conditions, each linked to a test and a log | [design §7](docs/design.md#7-exceptions-and-edge-conditions) |
| Incomplete report | 2-page report covering every point in the brief | [`docs/report.pdf`](docs/report.pdf) |
| No explanation of structure and execution | Package table, class diagram, request flow, design decisions, Big-O | [design §2](docs/design.md#2-architecture-and-class-diagram), [§8](docs/design.md#8-design-decisions-and-trade-offs), [§9](docs/design.md#9-complexity-big-o) |
| No evidence of each feature in operation | One log and one screenshot per operation and per error case | [testing Part 2](docs/testing.md#part-2-console-sessions-on-the-real-program) |
| Show understanding of how and why it works | Recursion traces, trade-offs table, Javadoc comments explaining each rule | [design §5](docs/design.md#5-recursion-base-cases-recursive-cases-and-traces), [§8](docs/design.md#8-design-decisions-and-trade-offs) |

## 👤 About the Author

**Samuel Ojetunde**, B.Sc. Data Science (Year 2), MIVA Open University. GitHub: [@samjay8](https://github.com/samjay8)
