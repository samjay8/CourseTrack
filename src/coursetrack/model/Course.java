package coursetrack.model;

import coursetrack.exception.InvalidCourseException;

/**
 * One course a student is registered for: a code, a title and its credit units.
 *
 * <p><b>Immutable:</b> all fields are {@code private final} and there are no
 * setters. Once a {@code Course} has passed validation in the constructor it
 * can never become invalid, and it is safe to hand out from the manager.
 * "Editing" a course means creating a new {@code Course} with the new values
 * (see {@link #withTitle(String)} and {@link #withUnits(int)}).</p>
 *
 * <p><b>Identity:</b> two courses are equal when they have the same code, which
 * matches the rule that codes are unique. {@link Comparable} orders courses by
 * code, which is their natural order.</p>
 */
public final class Course implements Comparable<Course> {

    private final String code;
    private final String title;
    private final int units;

    /**
     * Creates a validated course. The code and title are normalised first, so
     * {@code new Course("cos 201", "programming i", 3)} stores
     * "COS201", "Programming I", 3.
     *
     * @param code  course code, e.g. "COS201" (spaces and case do not matter)
     * @param title course title (trimmed and converted to Title Case)
     * @param units credit units, 1 to 6
     * @throws InvalidCourseException if any field is invalid
     */
    public Course(String code, String title, int units) throws InvalidCourseException {
        this.code = CourseValidator.normalizeCode(code);
        this.title = CourseValidator.normalizeTitle(title);
        this.units = CourseValidator.validateUnits(units);
    }

    /**
     * Builds a course from three raw text values, as typed in the console,
     * the GUI text fields, or read from a file.
     *
     * @param code  raw code text
     * @param title raw title text
     * @param units raw units text, e.g. " 3 "
     * @return a validated course
     * @throws InvalidCourseException if any value is invalid
     */
    public static Course fromText(String code, String title, String units)
            throws InvalidCourseException {
        return new Course(code, title, CourseValidator.parseUnits(units));
    }

    /** @return the normalised course code, e.g. "COS201" */
    public String getCode() {
        return code;
    }

    /** @return the course title in Title Case */
    public String getTitle() {
        return title;
    }

    /** @return the number of credit units */
    public int getUnits() {
        return units;
    }

    /**
     * Returns the study level implied by the first digit of the code,
     * e.g. COS201 is a 200-level course.
     *
     * @return 100, 200, 300 ... (0 for codes such as GST001)
     */
    public int getLevel() {
        return Character.getNumericValue(code.charAt(3)) * 100;
    }

    /**
     * @param newTitle the replacement title
     * @return a new course with the same code and units but a new title
     * @throws InvalidCourseException if the new title is invalid
     */
    public Course withTitle(String newTitle) throws InvalidCourseException {
        return new Course(code, newTitle, units);
    }

    /**
     * @param newUnits the replacement unit value
     * @return a new course with the same code and title but new units
     * @throws InvalidCourseException if the new units are out of range
     */
    public Course withUnits(int newUnits) throws InvalidCourseException {
        return new Course(code, title, newUnits);
    }

    /** Orders courses alphabetically by code (their natural order). */
    @Override
    public int compareTo(Course other) {
        return this.code.compareTo(other.code);
    }

    /** Two courses are equal when their codes are equal. */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Course)) {
            return false;
        }
        Course other = (Course) obj;
        return code.equals(other.code);
    }

    /** Consistent with {@link #equals(Object)}: based on the code only. */
    @Override
    public int hashCode() {
        return code.hashCode();
    }

    /** @return a readable one-line description, e.g. "COS201 - Programming I (3 units)" */
    @Override
    public String toString() {
        return code + " - " + title + " (" + units + (units == 1 ? " unit)" : " units)");
    }
}
