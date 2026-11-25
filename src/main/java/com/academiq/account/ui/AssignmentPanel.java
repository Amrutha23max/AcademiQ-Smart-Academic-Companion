package com.academiq.account.ui;

import com.academiq.common.*;
import com.academiq.account.model.Assignment;
import com.academiq.account.service.AssignmentService;
import com.academiq.account.service.AssignmentService.Filter;
import com.academiq.account.service.AssignmentService.SortBy;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Screen "Assignments": CRUD, mark submitted, filter by subject/status, sort. */
public class AssignmentPanel extends BasePanel {
    private final AssignmentService service = new AssignmentService();
    private final SubjectChoice subject = new SubjectChoice(false);
    private final SubjectChoice filterSubject = new SubjectChoice(true);
    private final ComboBox<Filter> filterStatus = new ComboBox<>();
    private final ComboBox<SortBy> sortBy = new ComboBox<>();
    private final TextField title = Ui.field(30), desc = Ui.field(30), due = Ui.field(12);
    private final TableList table = new TableList(12, new String[]{"Title", "Subject", "Due", "Status"}, new int[]{28, 20, 12, 22});
    private List<Assignment> current = new ArrayList<>();

    public AssignmentPanel() {
        VBox top = new VBox(6);
        top.getChildren().add(Ui.heading("Assignments & Deadlines"));

        GridPane form = Ui.form();
        Ui.addRow(form, 0, "Subject", subject);
        Ui.addRow(form, 1, "Title", title);
        Ui.addRow(form, 2, "Description", desc);
        Ui.addRow(form, 3, "Due date (yyyy-MM-dd)", due);
        top.getChildren().add(form);

        filterStatus.setItems(FXCollections.observableArrayList(Filter.values()));
        filterStatus.getSelectionModel().selectFirst();
        filterStatus.setStyle("-fx-font-size: 13px; -fx-background-color: white; -fx-border-color: #D1D5DB; -fx-border-radius: 4;");

        sortBy.setItems(FXCollections.observableArrayList(SortBy.values()));
        sortBy.getSelectionModel().selectFirst();
        sortBy.setStyle("-fx-font-size: 13px; -fx-background-color: white; -fx-border-color: #D1D5DB; -fx-border-radius: 4;");

        VBox south = new VBox(6);
        south.getChildren().add(Ui.row(
            Ui.button("Add", e -> add()),
            Ui.button("Update selected", e -> update()),
            Ui.button("Delete selected", e -> delete()),
            Ui.button("Mark submitted", e -> submit()),
            Ui.button("Clear", e -> clear())
        ));

        south.getChildren().add(Ui.row(
            Ui.boldLabel("Show:"), filterSubject, filterStatus,
            Ui.boldLabel("Sort by:"), sortBy,
            Ui.button("Apply", e -> reload())
        ));

        top.getChildren().add(south);
        top.setPadding(new Insets(0, 0, 10, 0));

        setTop(top);
        setCenter(table);
        table.addSelectionListener(e -> fillForm());
    }

    @Override
    public String getTitle() { return "Assignments"; }

    @Override
    public void onShow() {
        subject.reload();
        filterSubject.reload();
        reload();
    }

    private void reload() {
        try {
            Filter selFilter = filterStatus.getValue() != null ? filterStatus.getValue() : Filter.ALL;
            SortBy selSort = sortBy.getValue() != null ? sortBy.getValue() : SortBy.DUE_DATE;
            current = service.list(filterSubject.selectedSubjectId(), selFilter, selSort);

            List<Object[]> rows = new ArrayList<>();
            List<Integer> ids = new ArrayList<>();
            for (Assignment a : current) {
                rows.add(new Object[]{a.getTitle(), a.getSubjectName(), a.getDueDate(), a.displayStatus()});
                ids.add(a.getId());
            }
            table.setRows(rows, ids);
        } catch (DatabaseException e) {
            Ui.showError(e);
        }
    }

    private void fillForm() {
        for (Assignment a : current) {
            if (a.getId() == table.getSelectedId()) {
                subject.selectId(a.getSubjectId());
                title.setText(a.getTitle());
                desc.setText(a.getDescription());
                due.setText(a.getDueDate().toString());
            }
        }
    }

    private void clear() {
        title.setText("");
        desc.setText("");
        due.setText(LocalDate.now().plusDays(7).toString());
    }

    private void add() {
        try {
            service.add(subject.selectedSubjectId(), title.getText(), desc.getText(), due.getText());
            clear();
            reload();
            status("Assignment added.");
        } catch (AcademiqException e) {
            Ui.showError(e);
        }
    }

    private void update() {
        try {
            service.update(table.getSelectedId(), subject.selectedSubjectId(), title.getText(), desc.getText(), due.getText());
            reload();
            status("Assignment updated.");
        } catch (AcademiqException e) {
            Ui.showError(e);
        }
    }

    private void delete() {
        try {
            int id = Validators.requireSelection("assignment", table.getSelectedId());
            if (Dialogs.confirm("Delete the selected assignment?")) {
                service.delete(id);
                clear();
                reload();
                status("Assignment deleted.");
            }
        } catch (AcademiqException e) {
            Ui.showError(e);
        }
    }

    private void submit() {
        try {
            service.markSubmitted(Validators.requireSelection("assignment", table.getSelectedId()));
            reload();
            status("Marked as submitted.");
        } catch (AcademiqException e) {
            Ui.showError(e);
        }
    }
}
