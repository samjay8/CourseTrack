package coursetrack.service;

import coursetrack.exception.CourseNotFoundException;
import coursetrack.exception.DuplicateCourseException;
import coursetrack.exception.InvalidCourseException;
import coursetrack.exception.StorageException;
import coursetrack.model.Course;
import coursetrack.storage.CourseRepository;
import coursetrack.storage.LoadResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the business rules in CourseManager. A fake in-memory repository
 * is used, which also shows why CourseRepository is an interface: the manager
 * can be tested without touching the disk.
 */
class CourseManagerTest {

    /** Remembers what was "saved"; can be told to fail. */
    private static class FakeRepository implements CourseRepository {
        List<Course> saved = new ArrayList<>();
        boolean fail = false;

        @Override
        public void save(Path location, List<Course> courses) throws StorageException {
            if (fail) {
                throw new StorageException("disk full");
            }
            saved = new ArrayList<>(courses);
        }

        @Override
        public LoadResult load(Path location) throws StorageException {
            throw new StorageException("File not found: " + location);
        }
    }

    private FakeRepository repo;
    private CourseManager manager;

    @BeforeEach
    void setUp() {
        repo = new FakeRepository();
        manager = new CourseManager(repo);
    }

    private void addSample() throws Exception {
        manager.addCourse(new Course("COS201", "Computer Programming I", 3));
        manager.addCourse(new Course("MTH201", "Mathematical Methods I", 3));
        manager.addCourse(new Course("GST201", "Entrepreneurship", 2));
    }

    @Test
    @DisplayName("T12 courses are added and kept in insertion order")
    void addAndView() throws Exception {
        addSample();
        assertEquals(3, manager.size());
        assertEquals("COS201", manager.getAll().get(0).getCode());
        assertEquals("GST201", manager.getAll().get(2).getCode());
    }

    @Test
    @DisplayName("T13 duplicate code is rejected, even with different case/spacing")
    void duplicateRejected() throws Exception {
        addSample();
        assertThrows(DuplicateCourseException.class,
                () -> manager.addCourse(new Course("cos 201", "Another Title", 2)));
        assertEquals(3, manager.size());
    }

    @Test
    @DisplayName("T14 getAll returns a read-only view")
    void unmodifiableView() throws Exception {
        addSample();
        List<Course> view = manager.getAll();
        assertThrows(UnsupportedOperationException.class, () -> view.remove(0));
        assertThrows(UnsupportedOperationException.class, view::clear);
    }

    @Test
    @DisplayName("T15 recursive search finds first, middle and last items, case-insensitively")
    void searchFound() throws Exception {
        addSample();
        assertEquals("COS201", manager.findByCode("COS201").getCode());
        assertEquals("MTH201", manager.findByCode("mth201").getCode());
        assertEquals("GST201", manager.findByCode(" gst 201 ").getCode());
    }

    @Test
    @DisplayName("T16 search for a missing code throws CourseNotFoundException")
    void searchNotFound() throws Exception {
        addSample();
        assertThrows(CourseNotFoundException.class, () -> manager.findByCode("PHY101"));
    }

    @Test
    @DisplayName("T17 search on an empty list throws CourseNotFoundException (base case reached at once)")
    void searchEmpty() {
        assertThrows(CourseNotFoundException.class, () -> manager.findByCode("COS201"));
    }

    @Test
    @DisplayName("T18 search with a badly formed code throws InvalidCourseException")
    void searchInvalidCode() throws Exception {
        addSample();
        assertThrows(InvalidCourseException.class, () -> manager.findByCode("12AB"));
    }

    @Test
    @DisplayName("T19 recursive total: empty list = 0, then 3+3+2 = 8")
    void totalUnits() throws Exception {
        assertEquals(0, manager.totalUnits());
        addSample();
        assertEquals(8, manager.totalUnits());
    }

    @Test
    @DisplayName("T20 recursion handles a long list (200 courses) without stack overflow")
    void recursionDepth() throws Exception {
        for (int i = 100; i < 300; i++) {
            manager.addCourse(new Course("TST" + i, "Test", 1));
        }
        assertEquals(200, manager.totalUnits());
        assertEquals("TST299", manager.findByCode("TST299").getCode());
    }

    @Test
    @DisplayName("T21 sorting by code, title and units returns sorted copies; stored order unchanged")
    void sorting() throws Exception {
        addSample();
        assertEquals("COS201", manager.getSorted(SortField.CODE).get(0).getCode());
        assertEquals("GST201", manager.getSorted(SortField.CODE).get(1).getCode());
        assertEquals("Computer Programming I", manager.getSorted(SortField.TITLE).get(0).getTitle());
        assertEquals("Entrepreneurship", manager.getSorted(SortField.TITLE).get(1).getTitle());
        List<Course> byUnits = manager.getSorted(SortField.UNITS);
        assertEquals(2, byUnits.get(2).getUnits());
        assertEquals("COS201", byUnits.get(0).getCode());   // tie on 3 units broken by code
        assertEquals("MTH201", manager.getAll().get(1).getCode());
    }

    @Test
    @DisplayName("T22 edit replaces title and units but keeps the code")
    void editCourse() throws Exception {
        addSample();
        Course updated = manager.updateCourse("gst201", "entrepreneurship and innovation", 3);
        assertEquals("GST201", updated.getCode());
        assertEquals("Entrepreneurship and Innovation", manager.findByCode("GST201").getTitle());
        assertEquals(9, manager.totalUnits());
        assertThrows(CourseNotFoundException.class, () -> manager.updateCourse("PHY101", "X", 2));
        assertThrows(InvalidCourseException.class, () -> manager.updateCourse("COS201", "X", 9));
    }

    @Test
    @DisplayName("T23 delete removes the course; deleting a missing code fails")
    void deleteCourse() throws Exception {
        addSample();
        manager.removeCourse("MTH201");
        assertEquals(2, manager.size());
        assertThrows(CourseNotFoundException.class, () -> manager.findByCode("MTH201"));
        assertThrows(CourseNotFoundException.class, () -> manager.removeCourse("MTH201"));
    }

    @Test
    @DisplayName("T24 summary reports count, total and a warning above 24 units")
    void summary() throws Exception {
        addSample();
        CourseSummary s = manager.getSummary();
        assertEquals(3, s.courseCount());
        assertEquals(8, s.totalUnits());
        assertTrue(s.isUnderMinimum());
        for (int i = 0; i < 4; i++) {
            manager.addCourse(new Course("CSC30" + i, "Extra", 6));   // +24 units = 32
        }
        s = manager.getSummary();
        assertTrue(s.isOverLimit());
        assertEquals(0, s.remainingUnits());
        assertTrue(s.loadStatus().startsWith("WARNING"));
    }

    @Test
    @DisplayName("T25 unsaved-changes flag: set by add/edit/delete, cleared by save")
    void unsavedFlag() throws Exception {
        assertFalse(manager.hasUnsavedChanges());
        addSample();
        assertTrue(manager.hasUnsavedChanges());
        manager.saveTo(Paths.get("x.csv"));
        assertFalse(manager.hasUnsavedChanges());
        assertEquals(3, repo.saved.size());
        manager.removeCourse("COS201");
        assertTrue(manager.hasUnsavedChanges());
    }

    @Test
    @DisplayName("T26 a failed save keeps the unsaved flag; a failed load leaves the list untouched")
    void failuresKeepState() throws Exception {
        addSample();
        repo.fail = true;
        assertThrows(StorageException.class, () -> manager.saveTo(Paths.get("x.csv")));
        assertTrue(manager.hasUnsavedChanges());
        assertThrows(StorageException.class, () -> manager.loadFrom(Paths.get("missing.csv")));
        assertEquals(3, manager.size());
    }
}
