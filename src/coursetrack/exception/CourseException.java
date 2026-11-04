package coursetrack.exception;

/**
 * Base class for every problem CourseTrack reports to the user.
 *
 * <p>It is a <em>checked</em> exception, so the compiler forces callers
 * (the console menu and the GUI) to handle it. Catching
 * {@code CourseException} catches all the more specific subclasses.</p>
 */
public class CourseException extends Exception {

    /**
     * @param message a user-friendly explanation of what went wrong
     */
    public CourseException(String message) {
        super(message);
    }

    /**
     * @param message a user-friendly explanation of what went wrong
     * @param cause   the lower-level exception that caused this one
     */
    public CourseException(String message, Throwable cause) {
        super(message, cause);
    }
}
