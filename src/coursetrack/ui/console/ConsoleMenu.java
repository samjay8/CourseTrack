package coursetrack.ui.console;

import coursetrack.exception.CourseException;
import coursetrack.exception.CourseNotFoundException;
import coursetrack.exception.DuplicateCourseException;
import coursetrack.exception.InvalidCourseException;
import coursetrack.exception.StorageException;
import coursetrack.model.Course;
import coursetrack.model.CourseValidator;
import coursetrack.service.CourseManager;
import coursetrack.service.CourseSummary;
import coursetrack.service.SortField;
import coursetrack.storage.LoadResult;

import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

/**
 * The text-based menu. It reads the user's choice, decides what to do with a
 * {@code switch}, calls {@link CourseManager}, and prints the result.
 *
 * <p>Options 1-7 are exactly the operations the brief requires. Options 8-11
 * are clearly labelled extras.</p>
 *
 * <p><b>Recursion for input:</b> {@link #readChoice(String, int, int)},
 * {@link #readValid(String, FieldParser)} and {@link #readYesNo(String)} call
 * themselves again when the input is invalid, so the user is re-prompted until
 * the answer is acceptable.</p>
 */
public class ConsoleMenu {

    /** File used when the user just presses Enter at a file-name prompt. */
    public static final String DEFAULT_FILE = "courses.csv";

    private static final int EXIT = 7;
    private static final int LAST_OPTION = 11;
    private static final int MIN_TITLE_WIDTH = 30;

    private final CourseManager manager;
    private final Scanner input;
    private final PrintStream out;
    private final boolean echoInput;

    /**
     * Something that turns typed text into a value or rejects it.
     * Lets one recursive prompt method serve code, title and units.
     */
    @FunctionalInterface
    private interface FieldParser<T> {
        T parse(String text) throws InvalidCourseException;
    }

    /** Thrown internally when standard input ends (e.g. a piped script runs out). */
    private static class EndOfInputException extends RuntimeException {
    }

    /**
     * @param manager   the service that holds the courses
     * @param in        where input is read from (normally System.in)
     * @param out       where output is written (normally System.out)
     * @param echoInput true to print each line read; used when input is piped
     *                  from a script so the saved transcript shows what was typed
     */
    public ConsoleMenu(CourseManager manager, InputStream in, PrintStream out, boolean echoInput) {
        this.manager = manager;
        this.input = new Scanner(in);
        this.out = out;
        this.echoInput = echoInput;
    }

    /** Shows the menu repeatedly until the user chooses Exit (or input ends). */
    public void run() {
        out.println("Welcome to CourseTrack - Student Course Management System");
        boolean running = true;
        try {
            while (running) {                       // iteration: the main menu loop
                printMenu();
                int choice = readChoice("Enter your choice (1-" + LAST_OPTION + "): ", 1, LAST_OPTION);
                out.println();
                running = handleChoice(choice);
            }
        } catch (EndOfInputException e) {
            out.println();
            out.println("Input ended. Exiting CourseTrack.");
            return;
        }
        out.println("Goodbye!");
    }

    private void printMenu() {
        out.println();
        out.println("================ CourseTrack Menu ================");
        out.println("  Required operations");
        out.println("   1. Add Course");
        out.println("   2. View All Courses");
        out.println("   3. Search Course by Code");
        out.println("   4. Compute Total Units");
        out.println("   5. Save to File");
        out.println("   6. Load from File");
        out.println("   7. Exit Program");
        out.println("  Extra operations");
        out.println("   8. Edit Course");
        out.println("   9. Delete Course");
        out.println("  10. Sort / View Courses By...");
        out.println("  11. Semester Summary");
        out.println("==================================================");
    }

    /**
     * Decision making for the menu.
     *
     * @return false when the program should stop
     */
    private boolean handleChoice(int choice) {
        switch (choice) {
            case 1:  addCourse();            break;
            case 2:  viewAll();              break;
            case 3:  searchCourse();         break;
            case 4:  showTotalUnits();       break;
            case 5:  saveToFile();           break;
            case 6:  loadFromFile();         break;
            case EXIT: return !confirmExit();
            case 8:  editCourse();           break;
            case 9:  deleteCourse();         break;
            case 10: sortCourses();          break;
            case 11: showSummary();          break;
            default: error("Unknown option " + choice + "."); // cannot happen: readChoice checks range
        }
        return true;
    }

    // ================================================================ 1. Add

    private void addCourse() {
        out.println("--- Add Course ---");
        String code = readValid("Course code (e.g. COS201): ", CourseValidator::normalizeCode);
        if (manager.containsCode(code)) {
            // check early so the user does not type a title for nothing
            error("A course with code " + code + " already exists.");
            return;
        }
        String title = readValid("Course title: ", CourseValidator::normalizeTitle);
        int units = readValid("Units (" + CourseValidator.MIN_UNITS + "-"
                + CourseValidator.MAX_UNITS + "): ", CourseValidator::parseUnits);
        try {
            Course course = new Course(code, title, units);
            manager.addCourse(course);
            out.println("Added: " + course);
        } catch (DuplicateCourseException | InvalidCourseException e) {
            error(e.getMessage());
        }
    }

    // ================================================================ 2. View all

    private void viewAll() {
        out.println("--- All Courses ---");
        printTable(manager.getAll());
    }

    /**
     * Prints courses as an aligned table with a total row. The title column is
     * as wide as the longest title, so every column lines up.
     */
    private void printTable(List<Course> list) {
        if (list.isEmpty()) {
            out.println("No courses recorded yet. Use option 1 to add or option 6 to load.");
            return;
        }
        int titleWidth = MIN_TITLE_WIDTH;
        for (Course c : list) {
            titleWidth = Math.max(titleWidth, c.getTitle().length());
        }
        // e.g. "%-4s %-8s %-36s %5s%n": '-' means left-align, the number is the width
        String format = "%-4s %-8s %-" + titleWidth + "s %5s%n";
        String rule = "-".repeat(4 + 1 + 8 + 1 + titleWidth + 1 + 5);

        out.printf(format, "No.", "Code", "Title", "Units");
        out.println(rule);
        int number = 1;
        for (Course c : list) {
            out.printf(format, number + ".", c.getCode(), c.getTitle(), c.getUnits());
            number++;
        }
        out.println(rule);
        out.printf(format, "", "", list.size() + " course(s)   Total units:", manager.totalUnits());
    }

    // ================================================================ 3. Search

    private void searchCourse() {
        out.println("--- Search Course by Code ---");
        if (manager.isEmpty()) {
            out.println("There are no courses to search. Add or load courses first.");
            return;
        }
        String code = readLine("Enter course code to search: ");
        try {
            Course found = manager.findByCode(code);
            out.println("Found: " + found);
        } catch (CourseNotFoundException | InvalidCourseException e) {
            error(e.getMessage());
        }
    }

    // ================================================================ 4. Total

    private void showTotalUnits() {
        out.println("--- Total Units ---");
        if (manager.isEmpty()) {
            out.println("Total units: 0 (no courses recorded yet)");
        } else {
            out.println("Total units for " + manager.size() + " course(s): " + manager.totalUnits());
        }
    }

    // ================================================================ 5. Save

    private void saveToFile() {
        out.println("--- Save to File ---");
        if (manager.isEmpty()
                && !readYesNo("The list is empty. Save an empty file anyway? (y/n): ")) {
            out.println("Save cancelled.");
            return;
        }
        Path file = readFileName();
        try {
            manager.saveTo(file);
            out.println("Saved " + manager.size() + " course(s) to " + file + ".");
        } catch (StorageException e) {
            error(e.getMessage());
        }
    }

    // ================================================================ 6. Load

    private void loadFromFile() {
        out.println("--- Load from File ---");
        if (manager.hasUnsavedChanges()
                && !readYesNo("You have unsaved changes that will be replaced. Continue? (y/n): ")) {
            out.println("Load cancelled. Your current courses are unchanged.");
            return;
        }
        Path file = readFileName();
        try {
            LoadResult result = manager.loadFrom(file);
            out.println("Loaded " + result.getCourses().size() + " course(s) from " + file + ".");
            if (result.getSkippedCount() > 0) {
                out.println("Skipped " + result.getSkippedCount() + " invalid line(s):");
                for (String problem : result.getProblems()) {
                    out.println("  - " + problem);
                }
            }
        } catch (StorageException e) {
            error(e.getMessage());
            out.println("Your current courses are unchanged.");
        }
    }

    private Path readFileName() {
        String suggestion = manager.getCurrentFile() != null
                ? manager.getCurrentFile().toString() : DEFAULT_FILE;
        String name = readLine("File name [" + suggestion + "]: ");
        return Paths.get(name.isEmpty() ? suggestion : name);
    }

    // ================================================================ 7. Exit

    /** @return true if it is safe to exit */
    private boolean confirmExit() {
        if (!manager.hasUnsavedChanges()) {
            return true;
        }
        if (!readYesNo("You have unsaved changes. Save before exiting? (y/n): ")) {
            out.println("Changes discarded.");
            return true;
        }
        Path file = readFileName();
        try {
            manager.saveTo(file);
            out.println("Saved " + manager.size() + " course(s) to " + file + ".");
            return true;
        } catch (StorageException e) {
            error(e.getMessage());
            out.println("Not exiting, so your changes are not lost.");
            return false;
        }
    }

    // ================================================================ 8-11. Extras

    private void editCourse() {
        out.println("--- Edit Course (extra) ---");
        if (manager.isEmpty()) {
            out.println("There are no courses to edit.");
            return;
        }
        try {
            Course old = manager.findByCode(readLine("Code of the course to edit: "));
            out.println("Editing: " + old);
            out.println("(Press Enter to keep the current value.)");
            String title = readValid("New title [" + old.getTitle() + "]: ",
                    text -> text.isEmpty() ? old.getTitle() : CourseValidator.normalizeTitle(text));
            int units = readValid("New units [" + old.getUnits() + "]: ",
                    text -> text.isEmpty() ? old.getUnits() : CourseValidator.parseUnits(text));
            Course updated = manager.updateCourse(old.getCode(), title, units);
            out.println("Updated: " + updated);
        } catch (CourseException e) {
            error(e.getMessage());
        }
    }

    private void deleteCourse() {
        out.println("--- Delete Course (extra) ---");
        if (manager.isEmpty()) {
            out.println("There are no courses to delete.");
            return;
        }
        try {
            Course course = manager.findByCode(readLine("Code of the course to delete: "));
            if (readYesNo("Delete " + course + "? (y/n): ")) {
                manager.removeCourse(course.getCode());
                out.println("Deleted " + course.getCode() + ".");
            } else {
                out.println("Nothing deleted.");
            }
        } catch (CourseException e) {
            error(e.getMessage());
        }
    }

    private void sortCourses() {
        out.println("--- Sort / View Courses By (extra) ---");
        SortField[] fields = SortField.values();
        for (int i = 0; i < fields.length; i++) {
            out.println("  " + (i + 1) + ". " + fields[i]);
        }
        int pick = readChoice("Sort by (1-" + fields.length + "): ", 1, fields.length);
        out.println("Courses sorted by " + fields[pick - 1] + ":");
        printTable(manager.getSorted(fields[pick - 1]));
    }

    private void showSummary() {
        out.println("--- Semester Summary (extra) ---");
        CourseSummary s = manager.getSummary();
        out.println("Courses recorded : " + s.courseCount());
        out.println("Total units      : " + s.totalUnits());
        out.println("Normal load      : " + s.minUnits() + " to " + s.maxUnits() + " units");
        out.println("Status           : " + s.loadStatus());
    }

    // ================================================================ input helpers

    /** Prints a prompt and returns the trimmed line the user typed. */
    private String readLine(String prompt) {
        out.print(prompt);
        if (!input.hasNextLine()) {
            throw new EndOfInputException();
        }
        // remove an invisible byte-order mark that some Windows tools put before piped input
        String line = input.nextLine().replace("﻿", "");
        if (echoInput) {
            out.println(line);
        }
        return line.trim();                // string processing: always trim input
    }

    /**
     * Recursive prompt for a whole number in a range.
     * Base case: the input is a number between min and max - return it.
     * Recursive case: the input is blank, not a number or out of range -
     * explain the problem and ask again by calling this method.
     */
    private int readChoice(String prompt, int min, int max) {
        String text = readLine(prompt);
        if (text.isEmpty()) {
            error("Please type a number.");
            return readChoice(prompt, min, max);
        }
        int value;
        try {
            value = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            error("\"" + text + "\" is not a number. Please enter " + min + " to " + max + ".");
            return readChoice(prompt, min, max);
        }
        if (value < min || value > max) {
            error(value + " is out of range. Please enter " + min + " to " + max + ".");
            return readChoice(prompt, min, max);
        }
        return value;
    }

    /**
     * Recursive prompt for a course field.
     * Base case: the parser accepts the text - return the value.
     * Recursive case: the parser throws InvalidCourseException - show its
     * message and ask again.
     */
    private <T> T readValid(String prompt, FieldParser<T> parser) {
        String text = readLine(prompt);
        try {
            return parser.parse(text);
        } catch (InvalidCourseException e) {
            error(e.getMessage());
            return readValid(prompt, parser);
        }
    }

    /** Recursive yes/no prompt: accepts y/yes/n/no in any case, otherwise asks again. */
    private boolean readYesNo(String prompt) {
        String answer = readLine(prompt).toLowerCase();
        if (answer.equals("y") || answer.equals("yes")) {
            return true;
        }
        if (answer.equals("n") || answer.equals("no")) {
            return false;
        }
        error("Please answer y or n.");
        return readYesNo(prompt);
    }

    private void error(String message) {
        out.println("  [!] " + message);
    }
}
