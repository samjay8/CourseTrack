package coursetrack.ui.gui;

import coursetrack.model.Course;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapts a list of courses to the rows and columns a {@link javax.swing.JTable}
 * needs. The table asks this model for each cell value when it paints.
 */
class CourseTableModel extends AbstractTableModel {

    private static final String[] COLUMNS = {"No.", "Code", "Title", "Units"};

    private List<Course> rows = new ArrayList<>();

    /** Replaces the rows shown and tells the table to repaint. */
    void setCourses(List<Course> courses) {
        rows = new ArrayList<>(courses);
        fireTableDataChanged();
    }

    /** @return the course displayed in the given row */
    Course getCourseAt(int row) {
        return rows.get(row);
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return (column == 0 || column == 3) ? Integer.class : String.class;
    }

    @Override
    public Object getValueAt(int row, int column) {
        Course c = rows.get(row);
        switch (column) {
            case 0:  return row + 1;
            case 1:  return c.getCode();
            case 2:  return c.getTitle();
            default: return c.getUnits();
        }
    }
}
