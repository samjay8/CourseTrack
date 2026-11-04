package coursetrack.model;

import coursetrack.exception.InvalidCourseException;

/**
 * All the string processing and validation rules for course fields, kept in
 * one place so the console, the GUI and the file loader apply exactly the
 * same rules.
 *
 * <p>Every method is {@code static} because the rules need no state.</p>
 */
public final class CourseValidator {

    /** Three letters followed by three digits, e.g. COS201. */
    public static final String CODE_PATTERN = "^[A-Z]{3}\\d{3}$";

    /** Smallest credit-unit value a course may carry. */
    public static final int MIN_UNITS = 1;

    /** Largest credit-unit value a course may carry. */
    public static final int MAX_UNITS = 6;

    /** Longest title we accept, so the table output stays readable. */
    public static final int MAX_TITLE_LENGTH = 60;

    /** Roman numerals I to X, so "use of english ii" becomes "Use Of English II". */
    private static final String ROMAN_NUMERAL = "(?i)^(i{1,3}|iv|v|vi{1,3}|ix|x)$";

    private CourseValidator() {
        // utility class: no objects needed
    }

    /**
     * Normalises a course code: trims it, removes inner spaces and converts it
     * to upper case, then checks the pattern. "  cos 201 " becomes "COS201".
     *
     * @param rawCode the code exactly as the user typed it (may be null)
     * @return the normalised code
     * @throws InvalidCourseException if the code is blank or does not match AAA999
     */
    public static String normalizeCode(String rawCode) throws InvalidCourseException {
        if (rawCode == null || rawCode.trim().isEmpty()) {
            throw new InvalidCourseException("Course code cannot be empty.");
        }
        // String processing: remove ALL whitespace (\\s+) and upper-case it
        String code = rawCode.replaceAll("\\s+", "").toUpperCase();
        if (!code.matches(CODE_PATTERN)) {
            throw new InvalidCourseException("Invalid course code \"" + rawCode.trim()
                    + "\". Use 3 letters followed by 3 digits, e.g. COS201.");
        }
        return code;
    }

    /**
     * Normalises a course title: trims it, collapses repeated spaces and converts
     * it to Title Case. "  introduction   to computing " becomes
     * "Introduction To Computing".
     *
     * @param rawTitle the title as typed (may be null)
     * @return the cleaned title
     * @throws InvalidCourseException if the title is blank or too long
     */
    public static String normalizeTitle(String rawTitle) throws InvalidCourseException {
        if (rawTitle == null || rawTitle.trim().isEmpty()) {
            throw new InvalidCourseException("Course title cannot be empty.");
        }
        String title = toTitleCase(rawTitle.trim().replaceAll("\\s+", " "));
        if (title.length() > MAX_TITLE_LENGTH) {
            throw new InvalidCourseException("Course title is too long (maximum "
                    + MAX_TITLE_LENGTH + " characters).");
        }
        return title;
    }

    /**
     * Converts a phrase to Title Case word by word. Roman numerals are kept in
     * capitals so "programming ii" becomes "Programming II".
     *
     * @param text a trimmed phrase with single spaces between words
     * @return the phrase in Title Case
     */
    public static String toTitleCase(String text) {
        String[] words = text.split(" ");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (word.matches(ROMAN_NUMERAL)) {
                result.append(word.toUpperCase());
            } else {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1).toLowerCase());
            }
            if (i < words.length - 1) {
                result.append(' ');
            }
        }
        return result.toString();
    }

    /**
     * Converts typed unit text to a number and checks the allowed range.
     *
     * @param rawUnits the units as typed, e.g. " 3 "
     * @return the unit value
     * @throws InvalidCourseException if the text is blank, not a whole number, or out of range
     */
    public static int parseUnits(String rawUnits) throws InvalidCourseException {
        if (rawUnits == null || rawUnits.trim().isEmpty()) {
            throw new InvalidCourseException("Units cannot be empty.");
        }
        int units;
        try {
            units = Integer.parseInt(rawUnits.trim());
        } catch (NumberFormatException e) {
            // e.g. "three" or "2.5" - turn the technical error into a clear message
            throw new InvalidCourseException("Units must be a whole number, but \""
                    + rawUnits.trim() + "\" is not.");
        }
        return validateUnits(units);
    }

    /**
     * Checks that a unit value is within {@value #MIN_UNITS}..{@value #MAX_UNITS}.
     *
     * @param units the unit value
     * @return the same value, if it is valid
     * @throws InvalidCourseException if it is out of range
     */
    public static int validateUnits(int units) throws InvalidCourseException {
        if (units < MIN_UNITS || units > MAX_UNITS) {
            throw new InvalidCourseException("Units must be between " + MIN_UNITS
                    + " and " + MAX_UNITS + ", but " + units + " was given.");
        }
        return units;
    }
}
