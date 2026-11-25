package com.academiq.account.ui;

import com.academiq.common.*;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/** Screen "Subjects": add / edit / remove subjects. */
public class SubjectPanel extends BasePanel {
    private final SubjectDAO dao = new SubjectDAO();
    private final TextField name = Ui.field(26), semester = Ui.field(6);
    private final TableList table = new TableList(14, new String[]{"Subject", "Semester"}, new int[]{40, 10});
    private List<Subject> current = new ArrayList<>();

    public SubjectPanel() {
        VBox top = new VBox(6);
        top.getChildren().add(Ui.heading("Subjects"));

        GridPane form = Ui.form();
        Ui.addRow(form, 0, "Subject name", name);
        Ui.addRow(form, 1, "Semester", semester);
        top.getChildren().add(form);

        top.getChildren().add(Ui.row(
            Ui.button("Add", e -> add()),
            Ui.button("Update selected", e -> update()),
            Ui.button("Delete selected", e -> delete()),
            Ui.button("Clear", e -> clear())
        ));
        top.setPadding(new Insets(0, 0, 10, 0));

        setTop(top);
        setCenter(table);
        table.addSelectionListener(e -> fillForm());
    }

    @Override
    public String getTitle() { return "Subjects"; }

    @Override
    public void onShow() { reload(); }

    private void reload() {
        try {
            current = dao.findAll();
            List<Object[]> rows = new ArrayList<>();
            List<Integer> ids = new ArrayList<>();
            for (Subject s : current) {
                rows.add(new Object[]{s.getName(), s.getSemester()});
                ids.add(s.getId());
            }
            table.setRows(rows, ids);
        } catch (DatabaseException e) {
            Ui.showError(e);
        }
    }

    private void fillForm() {
        for (Subject s : current) {
            if (s.getId() == table.getSelectedId()) {
                name.setText(s.getName());
                semester.setText("" + s.getSemester());
            }
        }
    }

    private void clear() {
        name.setText("");
        semester.setText("");
    }

    private void add() {
        try {
            dao.save(new Subject(0, Session.userId(),
                Validators.requireText("Subject name", name.getText(), 100),
                Validators.parseInt("Semester", semester.getText())));
            clear();
            reload();
            status("Subject added.");
        } catch (AcademiqException e) {
            Ui.showError(e);
        }
    }

    private void update() {
        try {
            int id = Validators.requireSelection("subject", table.getSelectedId());
            dao.update(new Subject(id, Session.userId(),
                Validators.requireText("Subject name", name.getText(), 100),
                Validators.parseInt("Semester", semester.getText())));
            reload();
            status("Subject updated.");
        } catch (AcademiqException e) {
            Ui.showError(e);
        }
    }

    private void delete() {
        try {
            int id = Validators.requireSelection("subject", table.getSelectedId());
            if (!Dialogs.confirm("Delete this subject and ALL its assignments, notes, attendance and marks?")) return;
            dao.delete(id);
            clear();
            reload();
            status("Subject deleted.");
        } catch (AcademiqException e) {
            Ui.showError(e);
        }
    }
}
