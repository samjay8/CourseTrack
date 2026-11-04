package coursetrack;

import coursetrack.exception.StorageException;
import coursetrack.service.CourseManager;
import coursetrack.storage.FileCourseRepository;
import coursetrack.storage.LoadResult;
import coursetrack.ui.console.ConsoleMenu;
import coursetrack.ui.gui.CourseGUI;

import javax.swing.SwingUtilities;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Entry point. Builds the objects and starts the chosen interface.
 *
 * <pre>
 *   java -cp out coursetrack.Main                 console menu (default)
 *   java -cp out coursetrack.Main --gui           Swing window
 *   java -cp out coursetrack.Main courses.csv     console, loading a file first
 *   java -cp out coursetrack.Main --echo          console, echoing piped input
 * </pre>
 *
 * <p>{@code --echo} prints each line read from standard input. It is used when
 * a test script is piped into the program, so the saved transcript shows what
 * was "typed" next to each prompt.</p>
 */
public final class Main {

    private Main() {
    }

    /**
     * @param args optional flags and an optional file to load, see the class description
     */
    public static void main(String[] args) {
        boolean gui = false;
        boolean echo = false;
        Path startFile = null;
        for (String arg : args) {
            if (arg.equalsIgnoreCase("--gui")) {
                gui = true;
            } else if (arg.equalsIgnoreCase("--echo")) {
                echo = true;
            } else {
                startFile = Paths.get(arg);
            }
        }

        // both interfaces share the same service and storage objects
        CourseManager manager = new CourseManager(new FileCourseRepository());

        if (gui) {
            final Path file = startFile;
            // Swing components must be created on the Event Dispatch Thread
            SwingUtilities.invokeLater(() -> {
                CourseGUI window = new CourseGUI(manager);
                if (file != null) {
                    window.loadAtStartup(file);
                }
                window.setVisible(true);
            });
        } else {
            if (startFile != null) {
                loadForConsole(manager, startFile);
            }
            new ConsoleMenu(manager, System.in, System.out, echo).run();
        }
    }

    private static void loadForConsole(CourseManager manager, Path file) {
        try {
            LoadResult result = manager.loadFrom(file);
            System.out.println("Loaded " + result.getCourses().size() + " course(s) from " + file
                    + (result.getSkippedCount() > 0
                       ? " (" + result.getSkippedCount() + " invalid line(s) skipped)" : ""));
        } catch (StorageException e) {
            System.out.println("  [!] " + e.getMessage());
        }
    }
}
