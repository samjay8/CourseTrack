package coursetrack.storage;

import coursetrack.exception.InvalidCourseException;
import coursetrack.exception.StorageException;
import coursetrack.model.Course;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Saves and loads courses as a CSV (comma-separated values) text file.
 *
 * <pre>
 * code,title,units
 * COS201,Computer Programming I,3
 * GST212,"Philosophy, Logic And Human Existence",2
 * </pre>
 *
 * <p><b>Why CSV with quoting?</b> CSV opens in Excel and any text editor. A
 * title may itself contain a comma, so such a field is wrapped in double
 * quotes, and a double quote inside a field is written twice ({@code ""}).
 * This is the standard CSV rule (RFC 4180), so titles survive a round trip
 * exactly.</p>
 *
 * <p><b>Why an atomic save?</b> The data is first written to a temporary file
 * in the same folder and only then renamed over the real file. If the program
 * crashes half-way, the old file is still complete instead of half-written.</p>
 */
public class FileCourseRepository implements CourseRepository {

    /** The first line of every file this class writes. */
    public static final String HEADER = "code,title,units";

    private static final int FIELD_COUNT = 3;

    // ------------------------------------------------------------------ save

    @Override
    public void save(Path location, List<Course> courses) throws StorageException {
        Path target = location.toAbsolutePath();
        Path folder = target.getParent();
        if (folder == null || !Files.isDirectory(folder)) {
            throw new StorageException("Cannot save: the folder " + folder + " does not exist.");
        }

        Path temp = null;
        try {
            // 1. write everything to a temporary file next to the target
            temp = Files.createTempFile(folder, "coursetrack-", ".tmp");
            // try-with-resources closes the writer even if an exception occurs
            try (BufferedWriter writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
                writer.write(HEADER);
                writer.newLine();
                for (Course course : courses) {
                    writer.write(toCsvLine(course));
                    writer.newLine();
                }
            }
            // 2. replace the real file in one step
            moveIntoPlace(temp, target);
        } catch (IOException e) {
            deleteQuietly(temp);
            throw new StorageException("Could not save to " + target.getFileName()
                    + ": " + e.getMessage(), e);
        }
    }

    /** Renames temp over target, atomically when the file system supports it. */
    private static void moveIntoPlace(Path temp, Path target) throws IOException {
        try {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void deleteQuietly(Path file) {
        if (file == null) {
            return;
        }
        try {
            Files.deleteIfExists(file);
        } catch (IOException ignored) {
            // nothing more we can do; the original error is more important
        }
    }

    // ------------------------------------------------------------------ load

    @Override
    public LoadResult load(Path location) throws StorageException {
        LoadResult result = new LoadResult();
        Set<String> seenCodes = new HashSet<>();   // to detect duplicate codes in the file

        try (BufferedReader reader = new BufferedReader(
                new FileReader(location.toFile(), StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (lineNumber == 1) {
                    line = line.replace("﻿", "");          // Excel adds a byte-order mark
                }
                if (line.trim().isEmpty()) {
                    continue;                                   // ignore blank lines
                }
                if (lineNumber == 1 && line.trim().equalsIgnoreCase(HEADER)) {
                    continue;                                   // skip the header row
                }
                try {
                    Course course = parseCourse(line);
                    if (!seenCodes.add(course.getCode())) {
                        result.addProblem(lineNumber, "duplicate code " + course.getCode());
                    } else {
                        result.addCourse(course);
                    }
                } catch (InvalidCourseException e) {
                    // a corrupted line: record it and carry on with the next one
                    result.addProblem(lineNumber, e.getMessage());
                }
            }
        } catch (FileNotFoundException e) {
            throw new StorageException("File not found: " + location
                    + ". Check the name, or save a list first.", e);
        } catch (IOException e) {
            throw new StorageException("Could not read " + location + ": " + e.getMessage(), e);
        }
        return result;
    }

    /** Turns one CSV data line into a validated Course. */
    private static Course parseCourse(String line) throws InvalidCourseException {
        List<String> fields = parseCsvLine(line);
        if (fields.size() != FIELD_COUNT) {
            throw new InvalidCourseException("expected " + FIELD_COUNT
                    + " fields but found " + fields.size());
        }
        return Course.fromText(fields.get(0), fields.get(1), fields.get(2));
    }

    // ------------------------------------------------------------------ CSV helpers

    /**
     * Formats a course as one CSV line, quoting fields where needed.
     *
     * @param course the course to format
     * @return e.g. {@code GST212,"Philosophy, Logic And Human Existence",2}
     */
    static String toCsvLine(Course course) {
        return escape(course.getCode()) + "," + escape(course.getTitle()) + "," + course.getUnits();
    }

    /** Wraps a field in quotes if it contains a comma or quote; doubles inner quotes. */
    static String escape(String field) {
        if (field.contains(",") || field.contains("\"")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    /**
     * Splits one CSV line into fields, honouring quotes. Reads the line one
     * character at a time and remembers whether it is inside quotes.
     *
     * @param line a single line of the file
     * @return the unescaped fields
     * @throws InvalidCourseException if a quoted field is never closed
     */
    static List<String> parseCsvLine(String line) throws InvalidCourseException {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    boolean nextIsQuote = i + 1 < line.length() && line.charAt(i + 1) == '"';
                    if (nextIsQuote) {
                        current.append('"');   // "" inside quotes means one real quote
                        i++;
                    } else {
                        inQuotes = false;      // closing quote
                    }
                } else {
                    current.append(c);
                }
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == ',') {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        if (inQuotes) {
            throw new InvalidCourseException("a quoted field is not closed");
        }
        fields.add(current.toString());
        return fields;
    }
}
