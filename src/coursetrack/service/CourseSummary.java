package coursetrack.service;

/**
 * A snapshot of the semester: how many courses, how many units, and how that
 * compares with the normal semester load limits.
 *
 * <p>Written as a Java {@code record}: an immutable data carrier whose
 * constructor, getters, {@code equals} and {@code toString} are generated.</p>
 *
 * @param courseCount number of courses recorded
 * @param totalUnits  sum of all units
 * @param minUnits    minimum normal load (below this the student is under-loaded)
 * @param maxUnits    maximum normal load (above this needs special approval)
 */
public record CourseSummary(int courseCount, int totalUnits, int minUnits, int maxUnits) {

    /** @return true if the total is above the maximum load */
    public boolean isOverLimit() {
        return totalUnits > maxUnits;
    }

    /** @return true if the total is below the minimum load */
    public boolean isUnderMinimum() {
        return totalUnits < minUnits;
    }

    /** @return units that can still be added before reaching the maximum (never negative) */
    public int remainingUnits() {
        return Math.max(0, maxUnits - totalUnits);
    }

    /** @return a one-sentence verdict on the load, including a warning when over the limit */
    public String loadStatus() {
        if (isOverLimit()) {
            return "WARNING: " + totalUnits + " units is above the maximum load of "
                    + maxUnits + " by " + (totalUnits - maxUnits) + " unit(s).";
        }
        if (isUnderMinimum()) {
            return "Below the minimum load of " + minUnits + " units by "
                    + (minUnits - totalUnits) + " unit(s).";
        }
        return "Within the normal load (" + minUnits + "-" + maxUnits + " units); "
                + remainingUnits() + " unit(s) remaining.";
    }
}
