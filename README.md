<div align="center">

# 🎓 CourseTrack

### A Student Course Management Mini-System in Java

**COS 201 — Computer Programming I · Mid-Semester Lab Assessment**

Built by **Samuel Ojetunde** · B.Sc. Data Science, 200 Level, 1st Semester · MIVA Open University

[![Java 17](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](#-getting-started)
[![Swing GUI](https://img.shields.io/badge/GUI-Swing-5382A1?style=for-the-badge)](#-screenshots)
[![JUnit 5](https://img.shields.io/badge/Tests-45%20passing-25A162?style=for-the-badge&logo=junit5&logoColor=white)](docs/testing.md)
[![No frameworks](https://img.shields.io/badge/Build-plain%20javac-555555?style=for-the-badge)](#-getting-started)

[Overview](#-overview) · [Features](#-features) · [Getting Started](#-getting-started) · [Project Structure](#-project-structure) · [Screenshots](#-screenshots)

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

## 👤 About the Author

**Samuel Ojetunde**, B.Sc. Data Science (200 Level, 1st Semester), MIVA Open University. GitHub: [@samjay8](https://github.com/samjay8)
