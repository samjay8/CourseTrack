package coursetrack.storage;

import coursetrack.exception.StorageException;
import coursetrack.model.Course;
import coursetrack.service.CourseManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests for CSV saving and loading. Each test gets its own empty temporary folder. */
class FileCourseRepositoryTest {

    @TempDir
    Path tempDir;

    private final FileCourseRepository repo = new FileCourseRepository();

    private List<Course> sample() throws Exception {
        List<Course> list = new ArrayList<>();
        list.add(new Course("COS201", "Computer Programming I", 3));
        list.add(new Course("GST201", "Entrepreneurship, Innovation and Leadership", 2));
        list.add(new Course("MTH201", "The \"Hard\" Maths", 3));
        return list;
    }

    @Test
    @DisplayName("T27 save writes a header row and one line per course")
    void saveFormat() throws Exception {
        Path file = tempDir.resolve("courses.csv");
        repo.save(file, sample());
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        assertEquals(4, lines.size());
        assertEquals("code,title,units", lines.get(0));
        assertEquals("COS201,Computer Programming I,3", lines.get(1));
    }

    @Test
    @DisplayName("T28 titles with commas and quotes are quoted/escaped and survive a round trip")
    void roundTripWithCommasAndQuotes() throws Exception {
        Path file = tempDir.resolve("courses.csv");
        repo.save(file, sample());
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        assertEquals("GST201,\"Entrepreneurship, Innovation and Leadership\",2", lines.get(2));
        assertEquals("MTH201,\"The \"\"Hard\"\" Maths\",3", lines.get(3));

        LoadResult result = repo.load(file);
        assertEquals(0, result.getSkippedCount());
        assertEquals("Entrepreneurship, Innovation and Leadership", result.getCourses().get(1).getTitle());
        assertEquals("The \"Hard\" Maths", result.getCourses().get(2).getTitle());
        assertEquals(sample(), result.getCourses());
    }

    @Test
    @DisplayName("T29 saving an empty list gives a header-only file that loads as 0 courses")
    void saveEmpty() throws Exception {
        Path file = tempDir.resolve("empty.csv");
        repo.save(file, new ArrayList<>());
        assertEquals(List.of("code,title,units"), Files.readAllLines(file));
        assertEquals(0, repo.load(file).getCourses().size());
    }

    @Test
    @DisplayName("T30 loading a missing file throws StorageException caused by FileNotFoundException")
    void missingFile() {
        StorageException e = assertThrows(StorageException.class,
                () -> repo.load(tempDir.resolve("does-not-exist.csv")));
        assertTrue(e.getMessage().startsWith("File not found"));
        assertTrue(e.getCause() instanceof FileNotFoundException);
    }

    @Test
    @DisplayName("T31 corrupted lines are skipped and reported by line number; good lines are kept")
    void corruptedLines() throws Exception {
        Path file = tempDir.resolve("bad.csv");
        Files.write(file, List.of(
                "code,title,units",                 // 1 header
                "COS201,Computer Programming I,3",  // 2 ok
                "CS21,Bad Code,3",                  // 3 bad code
                "MTH201,Maths,three",               // 4 bad units
                "",                                 // 5 blank: ignored, not an error
                "STA201,Missing Units",             // 6 wrong field count
                "COS201,Duplicate,3",               // 7 duplicate
                "PHY201,\"Unclosed,2",              // 8 unclosed quote
                "COS203,Discrete Structures,9",     // 9 units out of range
                "GST201,Use Of English,2"));        // 10 ok
        LoadResult result = repo.load(file);
        assertEquals(2, result.getCourses().size());
        assertEquals(6, result.getSkippedCount());
        List<String> p = result.getProblems();
        assertTrue(p.get(0).startsWith("Line 3:"));
        assertTrue(p.get(1).startsWith("Line 4:"));
        assertTrue(p.get(2).startsWith("Line 6:"));
        assertTrue(p.get(3).startsWith("Line 7:") && p.get(3).contains("duplicate"));
        assertTrue(p.get(4).startsWith("Line 8:"));
        assertTrue(p.get(5).startsWith("Line 9:"));
    }

    @Test
    @DisplayName("T32 file without header, with a byte-order mark and messy values still loads")
    void headerlessAndMessy() throws Exception {
        Path file = tempDir.resolve("messy.csv");
        Files.write(file, List.of("﻿cos 201 ,  computer   programming i , 3 "),
                StandardCharsets.UTF_8);
        LoadResult result = repo.load(file);
        assertEquals(0, result.getSkippedCount());
        Course c = result.getCourses().get(0);
        assertEquals("COS201", c.getCode());
        assertEquals("Computer Programming I", c.getTitle());
        assertEquals(3, c.getUnits());
    }

    @Test
    @DisplayName("T33 save replaces an existing file completely and leaves no temp files behind")
    void atomicReplace() throws Exception {
        Path file = tempDir.resolve("courses.csv");
        repo.save(file, sample());
        List<Course> one = new ArrayList<>();
        one.add(new Course("COS205", "Software Engineering", 2));
        repo.save(file, one);
        assertEquals(1, repo.load(file).getCourses().size());
        try (var files = Files.list(tempDir)) {
            assertEquals(1, files.count(), "only courses.csv should remain");
        }
    }

    @Test
    @DisplayName("T34 saving into a folder that does not exist throws StorageException")
    void saveToMissingFolder() {
        Path file = tempDir.resolve("no-such-folder").resolve("courses.csv");
        assertThrows(StorageException.class, () -> repo.save(file, new ArrayList<>()));
    }

    @Test
    @DisplayName("T35 CSV parser: plain, quoted, escaped-quote and empty fields")
    void csvParser() throws Exception {
        assertEquals(List.of("A", "B", "C"), FileCourseRepository.parseCsvLine("A,B,C"));
        assertEquals(List.of("A", "B,C", "D"), FileCourseRepository.parseCsvLine("A,\"B,C\",D"));
        assertEquals(List.of("say \"hi\""), FileCourseRepository.parseCsvLine("\"say \"\"hi\"\"\""));
        assertEquals(List.of("", "", ""), FileCourseRepository.parseCsvLine(",,"));
        assertEquals("plain", FileCourseRepository.escape("plain"));
        assertEquals("\"a,b\"", FileCourseRepository.escape("a,b"));
    }

    @Test
    @DisplayName("T36 manager + file repository: save, load into a fresh manager, same data")
    void managerRoundTrip() throws Exception {
        Path file = tempDir.resolve("semester.csv");
        CourseManager first = new CourseManager(repo);
        for (Course c : sample()) {
            first.addCourse(c);
        }
        first.saveTo(file);

        CourseManager second = new CourseManager(repo);
        LoadResult result = second.loadFrom(file);
        assertEquals(3, result.getCourses().size());
        assertEquals(first.getAll(), second.getAll());
        assertEquals(first.totalUnits(), second.totalUnits());
        assertEquals(file, second.getCurrentFile());
    }

    @Test
    @DisplayName("T45 real semester file: 9 courses, 20 units, \"Set, Logic, and Algebra\" survives save and reload")
    void realSemesterData() throws Exception {
        CourseManager manager = new CourseManager(repo);
        LoadResult loaded = manager.loadFrom(Path.of("sample-data", "courses.csv"));
        assertEquals(0, loaded.getSkippedCount());
        assertEquals(9, manager.size());
        assertEquals(20, manager.totalUnits());
        assertEquals("Set, Logic, and Algebra", manager.findByCode("MTH203").getTitle());

        Path copy = tempDir.resolve("copy.csv");
        manager.saveTo(copy);
        assertTrue(Files.readAllLines(copy).contains("MTH203,\"Set, Logic, and Algebra\",2"));
        CourseManager reloaded = new CourseManager(repo);
        reloaded.loadFrom(copy);
        assertEquals(manager.getAll(), reloaded.getAll());
        assertEquals("Set, Logic, and Algebra", reloaded.findByCode("MTH203").getTitle());
        assertEquals(20, reloaded.totalUnits());
    }
}
