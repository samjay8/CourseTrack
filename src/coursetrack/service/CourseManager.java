package coursetrack.service;

import coursetrack.exception.CourseNotFoundException;
import coursetrack.exception.DuplicateCourseException;
import coursetrack.exception.InvalidCourseException;
import coursetrack.exception.StorageException;
import coursetrack.model.Course;
import coursetrack.model.CourseValidator;
import coursetrack.storage.CourseRepository;
import coursetrack.storage.LoadResult;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The business logic of CourseTrack: it owns the list of courses and enforces
 * the rules (unique codes, case-insensitive search, unsaved-change tracking).
 *
 * <p>Both user interfaces (console and GUI) call this class, so the rules are
 * written once. It knows nothing about the screen, and it talks to storage
 * only through the {@link CourseRepository} interface.</p>
 *
 * <p><b>Recursion:</b> {@link #findByCode(String)} and {@link #totalUnits()}
 * are implemented with recursive helper methods that walk the list by index.</p>
 */
public class CourseManager {

    /** Typical minimum credit load for one semester. */
    public static final int MIN_SEMESTER_UNITS = 15;

    /** Typical maximum credit load for one semester. */
    public static final int MAX_SEMESTER_UNITS = 24;

    /** Courses in the order they were added (or loaded). */
    private final List<Course> courses = new ArrayList<>();
    private final CourseRepository repository;
    private boolean unsavedChanges = false;
    private Path currentFile = null;

    /**
     * @param repository where courses are saved to and loaded from
     */
    public CourseManager(CourseRepository repository) {
        this.repository = repository;
    }

    // ------------------------------------------------------------------ add / view

    /**
     * Adds a course if no course with the same code exists.
     *
     * @param course a validated course
     * @throws DuplicateCourseException if the code is already recorded
     */
    public void addCourse(Course course) throws DuplicateCourseException {
        if (indexOf(course.getCode(), 0) != -1) {
            throw new DuplicateCourseException(course.getCode());
        }
        courses.add(course);
        unsavedChanges = true;
    }

    /**
     * Returns the courses in insertion order. The list is a read-only view, so
     * other classes cannot add or remove items behind the manager's back.
     *
     * @return an unmodifiable view of all courses
     */
    public List<Course> getAll() {
        return Collections.unmodifiableList(courses);
    }

    /**
     * Returns a sorted copy of the courses. The stored order is not changed.
     *
     * @param field what to sort by
     * @return a new, sorted, read-only list
     */
    public List<Course> getSorted(SortField field) {
        List<Course> copy = new ArrayList<>(courses);
        copy.sort(field.comparator());
        return Collections.unmodifiableList(copy);
    }

    /**
     * @param code an already-normalised code such as "COS201"
     * @return true if a course with this code is recorded
     */
    public boolean containsCode(String code) {
        return indexOf(code, 0) != -1;
    }

    /** @return number of courses recorded */
    public int size() {
        return courses.size();
    }

    /** @return true if no courses are recorded */
    public boolean isEmpty() {
        return courses.isEmpty();
    }

    // ------------------------------------------------------------------ search (recursive)

    /**
     * Finds a course by its code. The search is case- and space-insensitive
     * because the code is normalised first: "cos 201" finds "COS201".
     *
     * @param rawCode the code as typed
     * @return the matching course
     * @throws InvalidCourseException  if the code is not in the AAA999 format
     * @throws CourseNotFoundException if no course has that code (including an empty list)
     */
    public Course findByCode(String rawCode) throws InvalidCourseException, CourseNotFoundException {
        String code = CourseValidator.normalizeCode(rawCode);
        int index = indexOf(code, 0);
        if (index == -1) {
            throw new CourseNotFoundException(code);
        }
        return courses.get(index);
    }

    /**
     * Recursive linear search.
     * <ul>
     *   <li>Base case 1: {@code index} has passed the end of the list - not found, return -1.</li>
     *   <li>Base case 2: the course at {@code index} has the code - return {@code index}.</li>
     *   <li>Recursive case: search the rest of the list, starting at {@code index + 1}.</li>
     * </ul>
     * Each call looks at one course, so the problem shrinks by one every time
     * and is guaranteed to reach a base case.
     */
    private int indexOf(String code, int index) {
        if (index >= courses.size()) {
            return -1;
        }
        if (courses.get(index).getCode().equals(code)) {
            return index;
        }
        return indexOf(code, index + 1);
    }

    // ------------------------------------------------------------------ total (recursive)

    /**
     * Adds up the units of every course. An empty list gives 0.
     *
     * @return total credit units
     */
    public int totalUnits() {
        return sumUnits(0);
    }

    /**
     * Recursive sum.
     * <ul>
     *   <li>Base case: {@code index} is past the end - nothing left to add, return 0.</li>
     *   <li>Recursive case: units of this course + the sum of the rest.</li>
     * </ul>
     */
    private int sumUnits(int index) {
        if (index >= courses.size()) {
            return 0;
        }
        return courses.get(index).getUnits() + sumUnits(index + 1);
    }

    /**
     * @return the course count, total units and how they compare to the semester limits
     */
    public CourseSummary getSummary() {
        return new CourseSummary(courses.size(), totalUnits(),
                MIN_SEMESTER_UNITS, MAX_SEMESTER_UNITS);
    }

    // ------------------------------------------------------------------ edit / delete (extras)

    /**
     * Replaces the title and units of an existing course. The code cannot be
     * edited because it identifies the course; delete and re-add instead.
     *
     * @param rawCode  code of the course to edit
     * @param newTitle the new title
     * @param newUnits the new units
     * @return the updated course
     * @throws InvalidCourseException  if the code, title or units are invalid
     * @throws CourseNotFoundException if no course has that code
     */
    public Course updateCourse(String rawCode, String newTitle, int newUnits)
            throws InvalidCourseException, CourseNotFoundException {
        Course old = findByCode(rawCode);
        Course updated = new Course(old.getCode(), newTitle, newUnits);
        courses.set(courses.indexOf(old), updated);
        unsavedChanges = true;
        return updated;
    }

    /**
     * Removes a course.
     *
     * @param rawCode code of the course to remove
     * @return the course that was removed
     * @throws InvalidCourseException  if the code is not in the AAA999 format
     * @throws CourseNotFoundException if no course has that code
     */
    public Course removeCourse(String rawCode) throws InvalidCourseException, CourseNotFoundException {
        Course course = findByCode(rawCode);
        courses.remove(course);
        unsavedChanges = true;
        return course;
    }

    // ------------------------------------------------------------------ save / load

    /**
     * Saves every course (an empty list gives a file with only the header).
     *
     * @param file where to save
     * @throws StorageException if the file cannot be written
     */
    public void saveTo(Path file) throws StorageException {
        repository.save(file, courses);
        currentFile = file;
        unsavedChanges = false;
    }

    /**
     * Replaces the current list with the valid courses from a file. If the
     * file cannot be opened at all, the current list is left untouched.
     *
     * @param file where to load from
     * @return what was loaded and which lines were skipped
     * @throws StorageException if the file is missing or unreadable
     */
    public LoadResult loadFrom(Path file) throws StorageException {
        LoadResult result = repository.load(file);   // may throw: then nothing changes
        courses.clear();
        courses.addAll(result.getCourses());
        currentFile = file;
        unsavedChanges = false;
        return result;
    }

    /** @return true if courses were added, edited or deleted since the last save or load */
    public boolean hasUnsavedChanges() {
        return unsavedChanges;
    }

    /** @return the file last saved to or loaded from, or null if none yet */
    public Path getCurrentFile() {
        return currentFile;
    }
}
