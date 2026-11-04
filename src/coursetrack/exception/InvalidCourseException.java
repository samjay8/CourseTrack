package coursetrack.exception;

/**
 * Thrown when a course code, title or unit value fails validation
 * (for example a code like "CS21" or a unit value of "abc" or 9).
 */
public class InvalidCourseException extends CourseException {

    /**
     * @param message explanation of which field is invalid and why
     */
    public InvalidCourseException(String message) {
        super(message);
    }
}
