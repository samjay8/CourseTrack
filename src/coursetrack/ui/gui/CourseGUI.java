package coursetrack.ui.gui;

import coursetrack.exception.CourseException;
import coursetrack.exception.StorageException;
import coursetrack.model.Course;
import coursetrack.model.CourseValidator;
import coursetrack.service.CourseManager;
import coursetrack.service.CourseSummary;
import coursetrack.service.SortField;
import coursetrack.storage.LoadResult;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * A simple Swing window over the same {@link CourseManager} the console uses.
 * The window only collects input and shows results; every rule (validation,
 * duplicates, recursion, file format) lives in the service and model classes.
 *
 * <p>Errors are shown with {@link JOptionPane}; files are picked with
 * {@link JFileChooser}.</p>
 */
public class CourseGUI extends JFrame {

    private final CourseManager manager;
    private final CourseTableModel tableModel = new CourseTableModel();
    private final JTable table = new JTable(tableModel);
    private final JTextField codeField = new JTextField(8);
    private final JTextField titleField = new JTextField(26);
    private final JTextField unitsField = new JTextField(3);
    private final JComboBox<SortField> sortBox = new JComboBox<>(SortField.values());
    private final JLabel status = new JLabel(" ");
    private final JFileChooser chooser = new JFileChooser(new File("."));

    /**
     * Builds the window. Call {@code setVisible(true)} to show it.
     *
     * @param manager the service holding the courses
     */
    public CourseGUI(CourseManager manager) {
        super("CourseTrack - Student Course Manager");
        this.manager = manager;
        chooser.setFileFilter(new FileNameExtensionFilter("CSV course files (*.csv)", "csv"));

        add(buildFormPanel(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildButtonPanel(), BorderLayout.EAST);
        status.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        add(status, BorderLayout.SOUTH);

        // closing the window goes through the same unsaved-changes check as Exit
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitProgram();
            }
        });

        refreshTable(manager.getAll());
        setSize(new Dimension(860, 430));
        setLocationRelativeTo(null);
    }

    // ------------------------------------------------------------------ layout

    private JPanel buildFormPanel() {
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.setBorder(BorderFactory.createTitledBorder("Course details"));
        form.add(new JLabel("Code:"));
        form.add(codeField);
        form.add(new JLabel("Title:"));
        form.add(titleField);
        form.add(new JLabel("Units (1-6):"));
        form.add(unitsField);
        codeField.setName("codeField");
        titleField.setName("titleField");
        unitsField.setName("unitsField");
        return form;
    }

    private JScrollPane buildTablePanel() {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(22);
        table.getColumnModel().getColumn(0).setMaxWidth(45);
        table.getColumnModel().getColumn(1).setMaxWidth(90);
        table.getColumnModel().getColumn(3).setMaxWidth(60);
        // clicking a row copies it into the form, ready for Edit or Delete
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (!e.getValueIsAdjusting() && row >= 0) {
                Course c = tableModel.getCourseAt(row);
                codeField.setText(c.getCode());
                titleField.setText(c.getTitle());
                unitsField.setText(String.valueOf(c.getUnits()));
            }
        });
        return new JScrollPane(table);
    }

    private JPanel buildButtonPanel() {
        JPanel buttons = new JPanel(new GridLayout(0, 1, 4, 4));
        buttons.setBorder(BorderFactory.createTitledBorder("Operations"));
        buttons.add(button("1. Add Course", "add", this::addCourse));
        buttons.add(button("2. View All", "viewAll", () -> refreshTable(manager.getAll())));
        buttons.add(button("3. Search by Code", "search", this::searchCourse));
        buttons.add(button("4. Total Units", "total", this::showTotal));
        buttons.add(button("5. Save to File", "save", this::saveToFile));
        buttons.add(button("6. Load from File", "load", this::loadFromFile));
        buttons.add(button("7. Exit", "exit", this::exitProgram));
        buttons.add(new JLabel("Extras:"));
        buttons.add(button("Edit Selected", "edit", this::editCourse));
        buttons.add(button("Delete Selected", "delete", this::deleteCourse));
        JPanel sortRow = new JPanel(new BorderLayout(4, 0));
        sortRow.add(sortBox, BorderLayout.CENTER);
        sortRow.add(button("Sort", "sort", this::sortCourses), BorderLayout.EAST);
        buttons.add(sortRow);
        buttons.add(button("Summary", "summary", this::showSummary));
        return buttons;
    }

    private JButton button(String text, String name, Runnable action) {
        JButton b = new JButton(text);
        b.setName(name);
        b.addActionListener(e -> action.run());
        return b;
    }

    // ------------------------------------------------------------------ operations

    private void addCourse() {
        try {
            Course course = Course.fromText(codeField.getText(), titleField.getText(),
                    unitsField.getText());
            manager.addCourse(course);
            refreshTable(manager.getAll());
            clearForm();
            setStatus("Added " + course);
        } catch (CourseException e) {
            showError("Cannot add course", e.getMessage());
        }
    }

    private void searchCourse() {
        if (manager.isEmpty()) {
            showError("Search", "There are no courses to search. Add or load courses first.");
            return;
        }
        String code = JOptionPane.showInputDialog(this, "Course code to search:", codeField.getText());
        if (code == null) {
            return;                                     // user pressed Cancel
        }
        try {
            Course found = manager.findByCode(code);
            refreshTable(manager.getAll());
            int row = manager.getAll().indexOf(found);
            table.setRowSelectionInterval(row, row);
            JOptionPane.showMessageDialog(this, "Found: " + found, "Search",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (CourseException e) {
            showError("Search", e.getMessage());
        }
    }

    private void showTotal() {
        String message = manager.isEmpty()
                ? "Total units: 0 (no courses recorded yet)"
                : "Total units for " + manager.size() + " course(s): " + manager.totalUnits();
        JOptionPane.showMessageDialog(this, message, "Total Units", JOptionPane.INFORMATION_MESSAGE);
    }

    private void saveToFile() {
        if (manager.isEmpty() && !confirm("The list is empty. Save an empty file anyway?")) {
            return;
        }
        Path file = chooseFile(true);
        if (file != null) {
            save(file);
        }
    }

    /** @return true if the save worked */
    private boolean save(Path file) {
        try {
            manager.saveTo(file);
            setStatus("Saved " + manager.size() + " course(s) to " + file.getFileName());
            return true;
        } catch (StorageException e) {
            showError("Save failed", e.getMessage());
            return false;
        }
    }

    private void loadFromFile() {
        if (manager.hasUnsavedChanges()
                && !confirm("You have unsaved changes that will be replaced. Continue?")) {
            return;
        }
        Path file = chooseFile(false);
        if (file == null) {
            return;
        }
        try {
            LoadResult result = manager.loadFrom(file);
            refreshTable(manager.getAll());
            setStatus("Loaded " + result.getCourses().size() + " course(s) from " + file.getFileName());
            if (result.getSkippedCount() > 0) {
                JOptionPane.showMessageDialog(this,
                        "Loaded " + result.getCourses().size() + " course(s). Skipped "
                                + result.getSkippedCount() + " invalid line(s):\n"
                                + String.join("\n", result.getProblems()),
                        "Load report", JOptionPane.WARNING_MESSAGE);
            }
        } catch (StorageException e) {
            showError("Load failed", e.getMessage());
        }
    }

    private void exitProgram() {
        if (manager.hasUnsavedChanges()) {
            int answer = JOptionPane.showConfirmDialog(this,
                    "You have unsaved changes. Save before exiting?", "Unsaved changes",
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            if (answer == JOptionPane.CANCEL_OPTION || answer == JOptionPane.CLOSED_OPTION) {
                return;
            }
            if (answer == JOptionPane.YES_OPTION) {
                Path file = chooseFile(true);
                if (file == null || !save(file)) {
                    return;                             // do not lose data if saving failed
                }
            }
        }
        dispose();
    }

    private void editCourse() {
        Course selected = selectedCourse();
        if (selected == null) {
            return;
        }
        try {
            int units = CourseValidator.parseUnits(unitsField.getText());
            Course updated = manager.updateCourse(selected.getCode(), titleField.getText(), units);
            refreshTable(manager.getAll());
            setStatus("Updated " + updated + " (the code itself cannot be changed)");
        } catch (CourseException e) {
            showError("Cannot edit course", e.getMessage());
        }
    }

    private void deleteCourse() {
        Course selected = selectedCourse();
        if (selected == null || !confirm("Delete " + selected + "?")) {
            return;
        }
        try {
            manager.removeCourse(selected.getCode());
            refreshTable(manager.getAll());
            clearForm();
            setStatus("Deleted " + selected.getCode());
        } catch (CourseException e) {
            showError("Cannot delete course", e.getMessage());
        }
    }

    private void sortCourses() {
        SortField field = (SortField) sortBox.getSelectedItem();
        refreshTable(manager.getSorted(field));
        setStatus("Sorted by " + field + " (View All restores the original order)");
    }

    private void showSummary() {
        CourseSummary s = manager.getSummary();
        String message = "Courses recorded: " + s.courseCount()
                + "\nTotal units: " + s.totalUnits()
                + "\nNormal load: " + s.minUnits() + " to " + s.maxUnits() + " units"
                + "\n\n" + s.loadStatus();
        int icon = s.isOverLimit() ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE;
        JOptionPane.showMessageDialog(this, message, "Semester Summary", icon);
    }

    // ------------------------------------------------------------------ helpers

    /** Loads a file at start-up (used by Main when a file name is given). */
    public void loadAtStartup(Path file) {
        try {
            LoadResult result = manager.loadFrom(file);
            refreshTable(manager.getAll());
            setStatus("Loaded " + result.getCourses().size() + " course(s) from " + file.getFileName());
        } catch (StorageException e) {
            showError("Load failed", e.getMessage());
        }
    }

    private Course selectedCourse() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showError("No course selected", "Click a course in the table first.");
            return null;
        }
        return tableModel.getCourseAt(row);
    }

    private Path chooseFile(boolean forSaving) {
        int result = forSaving ? chooser.showSaveDialog(this) : chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        File file = chooser.getSelectedFile();
        if (forSaving && !file.getName().contains(".")) {
            file = new File(file.getPath() + ".csv");   // add the extension if missing
        }
        return file.toPath();
    }

    private void refreshTable(List<Course> courses) {
        tableModel.setCourses(courses);
        setStatus(manager.size() + " course(s), " + manager.totalUnits() + " unit(s)"
                + (manager.hasUnsavedChanges() ? "  - unsaved changes" : ""));
    }

    private void clearForm() {
        codeField.setText("");
        titleField.setText("");
        unitsField.setText("");
        codeField.requestFocusInWindow();
    }

    private boolean confirm(String question) {
        return JOptionPane.showConfirmDialog(this, question, "Please confirm",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private void setStatus(String text) {
        status.setText(text);
    }
}
