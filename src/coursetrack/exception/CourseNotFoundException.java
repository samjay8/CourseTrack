package coursetrack.exception;

/**
 * Thrown when a search, edit or delete refers to a course code
 * that is not in the list.
 */
public class CourseNotFoundException extends CourseException {

    /**
     * @param code the code that was searched for
     */
    public CourseNotFoundException(String code) {
        super("No course found with code " + code + ".");
    }
}
