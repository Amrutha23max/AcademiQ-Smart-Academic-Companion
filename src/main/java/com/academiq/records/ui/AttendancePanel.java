package com.academiq.records.ui;

import com.academiq.common.*;
import com.academiq.records.model.AttendanceRecord;
import com.academiq.records.service.AttendanceService;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/** Screen "Attendance": totals per subject, percentage, low-attendance warning and advice. */
public class AttendancePanel extends BasePanel {
    private final AttendanceService service = new AttendanceService();
    private final SubjectChoice subject = new SubjectChoice(false);
    private final TextField conducted = Ui.field(8), attended = Ui.field(8);
    private final Label advice = Ui.boldLabel(" ");
    private final TableList table = new TableList(12, new String[]{"Subject", "Attended", "Conducted", "Percent", "Status"}, new int[]{26, 9, 10, 9, 12});
    private List<AttendanceRecord> current = new ArrayList<>();

    public AttendancePanel() {
        VBox top = new VBox(6);
        top.getChildren().add(Ui.heading("Attendance"));

        GridPane form = Ui.form();
        Ui.addRow(form, 0, "Subject", subject);
        Ui.addRow(form, 1, "Classes conducted", conducted);
        Ui.addRow(form, 2, "Classes attended", attended);
        top.getChildren().add(form);

        VBox south = new VBox(6);
        south.getChildren().add(Ui.row(
            Ui.button("Save totals", e -> save()),
            Ui.button("+1 Present", e -> record(true)),
            Ui.button("+1 Absent", e -> record(false)),
            Ui.button("Delete selected", e -> delete())
        ));

        south.getChildren().add(Ui.row(
            Ui.label(String.format("Required attendance: %.0f%%", AttendanceService.threshold())),
            advice
        ));
        top.getChildren().add(south);
        top.setPadding(new Insets(0, 0, 10, 0));

        setTop(top);
        setCenter(table);
        table.addSelectionListener(e -> select());
    }

    @Override
    public String getTitle() { return "Attendance"; }

    @Override
    public void onShow() {
        subject.reload();
        reload();
    }

    private void reload() {
        try {
            current = service.list();
            List<Object[]> rows = new ArrayList<>();
            List<Integer> ids = new ArrayList<>();
            for (AttendanceRecord r : current) {
                rows.add(new Object[]{
                    r.getSubjectName(),
                    r.getAttended(),
                    r.getConducted(),
                    String.format("%.1f%%", r.percentage()),
                    AttendanceService.belowThreshold(r) ? "LOW !" : "OK"
                });
                ids.add(r.getId());
            }
            table.setRows(rows, ids);
        } catch (DatabaseException e) {
            Ui.showError(e);
        }
    }

    private void select() {
        for (AttendanceRecord r : current) {
            if (r.getId() == table.getSelectedId()) {
                subject.selectId(r.getSubjectId());
                conducted.setText("" + r.getConducted());
                attended.setText("" + r.getAttended());
                advice.setText(AttendanceService.advice(r));
                advice.setTextFill(AttendanceService.belowThreshold(r) ? Ui.DANGER : Ui.OK);
            }
        }
    }

    private void save() {
        try {
            service.save(subject.selectedSubjectId(), conducted.getText(), attended.getText());
            reload();
            warnIfLow();
            status("Attendance saved.");
        } catch (AcademiqException e) {
            Ui.showError(e);
        }
    }

    private void record(boolean present) {
        try {
            service.recordClass(subject.selectedSubjectId(), present);
            reload();
            warnIfLow();
            status("Class recorded.");
        } catch (AcademiqException e) {
            Ui.showError(e);
        }
    }

    private void warnIfLow() {
        for (AttendanceRecord r : current) {
            if (r.getSubjectId() == subject.selectedSubjectId() && AttendanceService.belowThreshold(r)) {
                Dialogs.info("Warning: attendance in " + r.getSubjectName() + " is " +
                    String.format("%.1f%%", r.percentage()) + ".\n" + AttendanceService.advice(r));
            }
        }
    }

    private void delete() {
        try {
            int id = Validators.requireSelection("record", table.getSelectedId());
            if (Dialogs.confirm("Delete this attendance record?")) {
                service.delete(id);
                reload();
            }
        } catch (AcademiqException e) {
            Ui.showError(e);
        }
    }
}
