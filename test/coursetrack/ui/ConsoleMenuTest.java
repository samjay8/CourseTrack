package coursetrack.ui;

import coursetrack.service.CourseManager;
import coursetrack.storage.FileCourseRepository;
import coursetrack.ui.console.ConsoleMenu;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Drives the real console menu with scripted keyboard input and checks the
 * printed output, so menu decisions and re-prompting are tested too.
 */
class ConsoleMenuTest {

    @TempDir
    Path tempDir;

    private CourseManager manager;

    /** Runs the menu with the given lines as keyboard input and returns everything printed. */
    private String run(String... lines) {
        manager = new CourseManager(new FileCourseRepository());
        String script = String.join("\n", lines) + "\n";
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        new ConsoleMenu(manager, new ByteArrayInputStream(script.getBytes(StandardCharsets.UTF_8)),
                out, false).run();
        return buffer.toString(StandardCharsets.UTF_8);
    }

    @Test
    @DisplayName("T37 invalid menu input (letters, blank, 0, 12) is re-prompted, then a valid choice works")
    void invalidMenuInput() {
        String output = run("abc", "", "0", "12", "4", "7");
        assertTrue(output.contains("\"abc\" is not a number"));
        assertTrue(output.contains("Please type a number."));
        assertTrue(output.contains("0 is out of range"));
        assertTrue(output.contains("12 is out of range"));
        assertTrue(output.contains("Total units: 0"));
        assertTrue(output.contains("Goodbye!"));
    }

    @Test
    @DisplayName("T38 add re-prompts for each invalid field until it is valid")
    void addWithRetries() {
        String output = run("1", "cs1", "cos 201", "   ", "computer programming i", "x", "9", "3", "2", "7", "n");
        assertTrue(output.contains("Invalid course code \"cs1\""));
        assertTrue(output.contains("Course title cannot be empty."));
        assertTrue(output.contains("Units must be a whole number"));
        assertTrue(output.contains("Units must be between 1 and 6, but 9 was given."));
        assertTrue(output.contains("Added: COS201 - Computer Programming I (3 units)"));
        assertTrue(output.contains("COS201   Computer Programming I"));
        assertEquals(1, manager.size());
    }

    @Test
    @DisplayName("T39 duplicate code is refused before asking for the title")
    void duplicateInMenu() {
        String output = run("1", "COS201", "Programming", "3", "1", "cos201", "7", "n");
        assertTrue(output.contains("A course with code COS201 already exists."));
        assertEquals(1, manager.size());
    }

    @Test
    @DisplayName("T40 search on empty list, then not-found and found after adding")
    void searchCases() {
        String output = run("3", "1", "MTH201", "Maths", "3", "3", "PHY101", "3", "mth 201", "7", "n");
        assertTrue(output.contains("There are no courses to search."));
        assertTrue(output.contains("No course found with code PHY101."));
        assertTrue(output.contains("Found: MTH201 - Maths (3 units)"));
    }

    @Test
    @DisplayName("T41 exit with unsaved changes offers to save, and saving writes the file")
    void exitSavesChanges() throws Exception {
        Path file = tempDir.resolve("exit.csv");
        String output = run("1", "COS201", "Programming", "3", "7", "maybe", "y", file.toString());
        assertTrue(output.contains("You have unsaved changes. Save before exiting?"));
        assertTrue(output.contains("Please answer y or n."));
        assertTrue(Files.exists(file));
        assertFalse(manager.hasUnsavedChanges());
    }

    @Test
    @DisplayName("T42 load warns about unsaved changes; answering n keeps current data")
    void loadWarnsAboutUnsaved() {
        String output = run("1", "COS201", "Programming", "3", "6", "n", "7", "n");
        assertTrue(output.contains("You have unsaved changes that will be replaced."));
        assertTrue(output.contains("Load cancelled."));
        assertEquals(1, manager.size());
    }

    @Test
    @DisplayName("T43 load of a missing file shows a friendly message and keeps current data")
    void loadMissingFile() {
        String output = run("6", tempDir.resolve("nothing.csv").toString(), "7");
        assertTrue(output.contains("File not found"));
        assertTrue(output.contains("Your current courses are unchanged."));
    }

    @Test
    @DisplayName("T44 input ending unexpectedly (end of file) exits cleanly instead of crashing")
    void endOfInput() {
        String output = run("1", "COS201");
        assertTrue(output.contains("Input ended. Exiting CourseTrack."));
    }
}
