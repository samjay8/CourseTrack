package coursetrack.exception;

/**
 * Thrown when the user tries to add a course whose code is already recorded.
 * Course codes are unique, so two "COS201" entries are not allowed.
 */
public class DuplicateCourseException extends CourseException {

    /**
     * @param code the normalised code that already exists
     */
    public DuplicateCourseException(String code) {
        super("A course with code " + code + " already exists.");
    }
}
