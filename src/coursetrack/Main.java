package coursetrack;

import coursetrack.service.CourseManager;
import coursetrack.storage.FileCourseRepository;
import coursetrack.ui.console.ConsoleMenu;

/**
 * Entry point. Builds the objects and starts the chosen interface.
 *
 * <pre>
 *   java -cp out coursetrack.Main          console menu (default)
 *   java -cp out coursetrack.Main --echo   console, echoing piped input (for transcripts)
 * </pre>
 */
public final class Main {

    private Main() {
    }

    /**
     * @param args optional flags, see the class description
     */
    public static void main(String[] args) {
        boolean echo = false;
        for (String arg : args) {
            if (arg.equalsIgnoreCase("--echo")) {
                echo = true;
            }
        }
        CourseManager manager = new CourseManager(new FileCourseRepository());
        new ConsoleMenu(manager, System.in, System.out, echo).run();
    }
}
