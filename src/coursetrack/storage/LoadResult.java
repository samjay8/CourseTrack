package coursetrack.storage;

import coursetrack.model.Course;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The outcome of loading a file: the courses that were read successfully and
 * a message for every line that had to be skipped (with its line number).
 */
public final class LoadResult {

    private final List<Course> courses = new ArrayList<>();
    private final List<String> problems = new ArrayList<>();

    /** @param course a course that was read successfully */
    void addCourse(Course course) {
        courses.add(course);
    }

    /**
     * @param lineNumber the 1-based line number in the file
     * @param reason     why the line was skipped
     */
    void addProblem(int lineNumber, String reason) {
        problems.add("Line " + lineNumber + ": " + reason);
    }

    /** @return the valid courses, in file order (read-only) */
    public List<Course> getCourses() {
        return Collections.unmodifiableList(courses);
    }

    /** @return one message per skipped line (read-only) */
    public List<String> getProblems() {
        return Collections.unmodifiableList(problems);
    }

    /** @return how many lines were skipped */
    public int getSkippedCount() {
        return problems.size();
    }
}
