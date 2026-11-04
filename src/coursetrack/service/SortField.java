package coursetrack.service;

import coursetrack.model.Course;

import java.util.Comparator;

/**
 * The ways the course list can be sorted for display. Each constant carries
 * the {@link Comparator} that does the ordering, so adding a new sort option
 * is one line here and no {@code if/else} chain elsewhere.
 */
public enum SortField {

    /** Alphabetical by code (the natural order of {@link Course}). */
    CODE("code", Comparator.naturalOrder()),

    /** Alphabetical by title, ignoring case. */
    TITLE("title", Comparator.comparing(Course::getTitle, String.CASE_INSENSITIVE_ORDER)),

    /** Highest units first; courses with equal units are ordered by code. */
    UNITS("units (highest first)",
            Comparator.comparingInt(Course::getUnits).reversed()
                      .thenComparing(Comparator.naturalOrder()));

    private final String label;
    private final Comparator<Course> comparator;

    SortField(String label, Comparator<Course> comparator) {
        this.label = label;
        this.comparator = comparator;
    }

    /** @return the comparator that implements this ordering */
    public Comparator<Course> comparator() {
        return comparator;
    }

    /** @return a short label for menus, e.g. "title" */
    @Override
    public String toString() {
        return label;
    }
}
