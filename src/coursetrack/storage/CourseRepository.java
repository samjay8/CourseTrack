package coursetrack.storage;

import coursetrack.exception.StorageException;
import coursetrack.model.Course;

import java.nio.file.Path;
import java.util.List;

/**
 * Describes <em>what</em> storage must do (save and load a list of courses)
 * without saying <em>how</em>.
 *
 * <p>{@link coursetrack.service.CourseManager} depends only on this interface.
 * The current implementation is {@link FileCourseRepository} (a CSV file), but
 * a JSON file or a database could be added later without changing the manager,
 * the console menu or the GUI. It also lets tests swap in a fake repository.</p>
 */
public interface CourseRepository {

    /**
     * Writes all the given courses to the location, replacing what was there.
     *
     * @param location where to save
     * @param courses  the courses to save (may be empty)
     * @throws StorageException if the data cannot be written
     */
    void save(Path location, List<Course> courses) throws StorageException;

    /**
     * Reads courses from the location. Bad records are skipped and reported in
     * the result instead of stopping the whole load.
     *
     * @param location where to load from
     * @return the valid courses plus a description of every skipped line
     * @throws StorageException if the location does not exist or cannot be read
     */
    LoadResult load(Path location) throws StorageException;
}
