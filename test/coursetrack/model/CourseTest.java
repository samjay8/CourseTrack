package coursetrack.model;

import coursetrack.exception.InvalidCourseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests for the Course model and the CourseValidator string processing. */
class CourseTest {

    @Test
    @DisplayName("T01 valid course is created and stored as typed")
    void validCourse() throws Exception {
        Course c = new Course("COS201", "Computer Programming I", 3);
        assertEquals("COS201", c.getCode());
        assertEquals("Computer Programming I", c.getTitle());
        assertEquals(3, c.getUnits());
        assertEquals(200, c.getLevel());
    }

    @Test
    @DisplayName("T02 code is trimmed, upper-cased and inner spaces removed")
    void codeNormalised() throws Exception {
        assertEquals("COS201", new Course("  cos 201 ", "Programming", 3).getCode());
        assertEquals("MTH203", CourseValidator.normalizeCode("m t h 2 0 3"));
    }

    @Test
    @DisplayName("T03 invalid code patterns are rejected")
    void invalidCodes() {
        String[] bad = {"CS21", "COS2011", "123COS", "CO$201", "COSABC", "COURSE"};
        for (String code : bad) {
            assertThrows(InvalidCourseException.class,
                    () -> CourseValidator.normalizeCode(code), "should reject " + code);
        }
    }

    @Test
    @DisplayName("T04 blank or null code is rejected")
    void blankCode() {
        assertThrows(InvalidCourseException.class, () -> CourseValidator.normalizeCode("   "));
        assertThrows(InvalidCourseException.class, () -> CourseValidator.normalizeCode(null));
    }

    @Test
    @DisplayName("T05 title is trimmed, spaces collapsed and converted to Title Case")
    void titleCase() throws Exception {
        assertEquals("Introduction To Computer Science",
                CourseValidator.normalizeTitle("  introduction   TO computer science "));
        assertEquals("Use Of English II", CourseValidator.normalizeTitle("use of english ii"));
        assertEquals("\"Intro\" To Logic", CourseValidator.normalizeTitle("\"intro\" to logic"));
    }

    @Test
    @DisplayName("T06 blank or over-long title is rejected")
    void badTitle() {
        assertThrows(InvalidCourseException.class, () -> CourseValidator.normalizeTitle("   "));
        assertThrows(InvalidCourseException.class,
                () -> CourseValidator.normalizeTitle("x".repeat(61)));
    }

    @Test
    @DisplayName("T07 units text is parsed; non-numbers give a clear message")
    void unitsParsing() throws Exception {
        assertEquals(4, CourseValidator.parseUnits(" 4 "));
        InvalidCourseException e = assertThrows(InvalidCourseException.class,
                () -> CourseValidator.parseUnits("three"));
        assertTrue(e.getMessage().contains("whole number"));
        assertThrows(InvalidCourseException.class, () -> CourseValidator.parseUnits("2.5"));
        assertThrows(InvalidCourseException.class, () -> CourseValidator.parseUnits(""));
    }

    @Test
    @DisplayName("T08 units boundaries: 1 and 6 accepted, 0 and 7 rejected")
    void unitsBoundaries() throws Exception {
        assertEquals(1, new Course("COS201", "A", 1).getUnits());
        assertEquals(6, new Course("COS201", "A", 6).getUnits());
        assertThrows(InvalidCourseException.class, () -> new Course("COS201", "A", 0));
        assertThrows(InvalidCourseException.class, () -> new Course("COS201", "A", 7));
        assertThrows(InvalidCourseException.class, () -> new Course("COS201", "A", -3));
    }

    @Test
    @DisplayName("T09 equals and hashCode depend on the code only")
    void equalityByCode() throws Exception {
        Course a = new Course("COS201", "Programming", 3);
        Course b = new Course("cos 201", "Something Else", 2);
        Course c = new Course("COS202", "Programming", 3);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }

    @Test
    @DisplayName("T10 natural order (Comparable) sorts by code")
    void comparableByCode() throws Exception {
        List<Course> list = new ArrayList<>();
        list.add(new Course("MTH201", "B", 3));
        list.add(new Course("COS203", "C", 3));
        list.add(new Course("GST201", "A", 2));
        Collections.sort(list);
        assertEquals("COS203", list.get(0).getCode());
        assertEquals("GST201", list.get(1).getCode());
        assertEquals("MTH201", list.get(2).getCode());
    }

    @Test
    @DisplayName("T11 withTitle/withUnits return new objects; the original is unchanged (immutability)")
    void immutability() throws Exception {
        Course original = new Course("COS201", "Programming", 3);
        Course renamed = original.withTitle("advanced programming");
        Course reweighted = original.withUnits(4);
        assertEquals("Programming", original.getTitle());
        assertEquals(3, original.getUnits());
        assertEquals("Advanced Programming", renamed.getTitle());
        assertEquals(4, reweighted.getUnits());
        assertEquals("COS201 - Programming (3 units)", original.toString());
    }
}
