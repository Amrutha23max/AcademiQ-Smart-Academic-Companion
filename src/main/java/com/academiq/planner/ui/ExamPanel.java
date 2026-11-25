package com.academiq.planner.ui;

import com.academiq.common.*;
import com.academiq.planner.model.Exam;
import com.academiq.planner.service.ExamService;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/** Screen "Exam Timetable". */
public class ExamPanel extends BasePanel {
    private final ExamService service = new ExamService();
    private final SubjectChoice subject = new SubjectChoice(false);
    private final TextField name = Ui.field(24), date = Ui.field(12), time = Ui.field(8);
    private final TableList table = new TableList(14, new String[]{"Exam", "Subject", "Date", "Time", "Countdown"}, new int[]{24, 20, 11, 6, 14});
    private List<Exam> current = new ArrayList<>();

    public ExamPanel() {
        VBox top = new VBox(6);
        top.getChildren().add(Ui.heading("Exam Timetable"));

        GridPane form = Ui.form();
        Ui.addRow(form, 0, "Subject", subject);
        Ui.addRow(form, 1, "Exam name", name);
        Ui.addRow(form, 2, "Date (yyyy-MM-dd)", date);
        Ui.addRow(form, 3, "Time (HH:mm)", time);
        top.getChildren().add(form);

        top.getChildren().add(Ui.row(
            Ui.button("Add", e -> add()),
            Ui.button("Update selected", e -> update()),
            Ui.button("Delete selected", e -> delete())
        ));
        top.setPadding(new Insets(0, 0, 10, 0));

        setTop(top);
        setCenter(table);
        table.addSelectionListener(e -> select());
    }

    @Override
    public String getTitle() { return "Exam Timetable"; }

    @Override
    public void onShow() {
        subject.reload();
        reload();
    }

    private void reload() {
        try {
            current = service.timetable();
            List<Object[]> rows = new ArrayList<>();
            List<Integer> ids = new ArrayList<>();
            for (Exam e : current) {
                rows.add(new Object[]{e.getName(), e.getSubjectName(), e.getDate(), e.getTime(), e.countdown()});
                ids.add(e.getId());
            }
            table.setRows(rows, ids);
        } catch (DatabaseException e) {
            Ui.showError(e);
        }
    }

    private void select() {
        for (Exam e : current) {
            if (e.getId() == table.getSelectedId()) {
                subject.selectId(e.getSubjectId());
                name.setText(e.getName());
                date.setText(e.getDate().toString());
                time.setText(e.getTime().toString());
            }
        }
    }

    private void add() {
        try {
            service.add(subject.selectedSubjectId(), name.getText(), date.getText(), time.getText());
            reload();
            status("Exam added.");
        } catch (AcademiqException e) {
            Ui.showError(e);
        }
    }

    private void update() {
        try {
            service.update(table.getSelectedId(), subject.selectedSubjectId(), name.getText(), date.getText(), time.getText());
            reload();
            status("Exam updated.");
        } catch (AcademiqException e) {
            Ui.showError(e);
        }
    }

    private void delete() {
        try {
            int id = Validators.requireSelection("exam", table.getSelectedId());
            if (Dialogs.confirm("Delete this exam?")) {
                service.delete(id);
                reload();
            }
        } catch (AcademiqException e) {
            Ui.showError(e);
        }
    }
}
