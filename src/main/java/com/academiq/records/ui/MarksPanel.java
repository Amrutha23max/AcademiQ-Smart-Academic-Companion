package com.academiq.records.ui;

import com.academiq.common.*;
import com.academiq.records.model.MarksRecord;
import com.academiq.records.service.MarksCalculator;
import javafx.geometry.Insets;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/** Screen "Marks": internal assessment marks with subject totals, percentage and configured grade. */
public class MarksPanel extends BasePanel {
    private final MarksCalculator calc = new MarksCalculator();
    private final SubjectChoice subject = new SubjectChoice(false);
    private final TextField assessment = Ui.field(22), obtained = Ui.field(8), max = Ui.field(8);
    private final TextArea totals = new TextArea();
    private final TableList table = new TableList(9, new String[]{"Subject", "Assessment", "Obtained", "Maximum", "Percent"}, new int[]{24, 24, 9, 9, 9});
    private List<MarksRecord> current = new ArrayList<>();

    public MarksPanel() {
        VBox top = new VBox(6);
        top.getChildren().add(Ui.heading("Internal Marks"));

        GridPane form = Ui.form();
        Ui.addRow(form, 0, "Subject", subject);
        Ui.addRow(form, 1, "Assessment (e.g. Internal 1)", assessment);
        Ui.addRow(form, 2, "Marks obtained", obtained);
        Ui.addRow(form, 3, "Maximum marks", max);
        top.getChildren().add(form);

        top.getChildren().add(Ui.row(
            Ui.button("Add", e -> add()),
            Ui.button("Update selected", e -> update()),
            Ui.button("Delete selected", e -> delete())
        ));
        top.setPadding(new Insets(0, 0, 10, 0));

        setTop(top);
        setCenter(table);

        totals.setEditable(false);
        totals.setFont(Ui.MONO);
        totals.setPrefRowCount(4);
        totals.setStyle("-fx-font-family: Consolas; -fx-font-size: 12px; -fx-background-color: white; -fx-border-color: #D1D5DB; -fx-border-radius: 4;");
        VBox.setMargin(totals, new Insets(8, 0, 0, 0));
        setBottom(totals);

        table.addSelectionListener(e -> select());
    }

    @Override
    public String getTitle() { return "Marks"; }

    @Override
    public void onShow() {
        subject.reload();
        reload();
    }

    private void reload() {
        try {
            current = calc.list();
            List<Object[]> rows = new ArrayList<>();
            List<Integer> ids = new ArrayList<>();
            for (MarksRecord m : current) {
                rows.add(new Object[]{
                    m.getSubjectName(),
                    m.getAssessment(),
                    m.getObtained(),
                    m.getMax(),
                    String.format("%.1f%%", MarksCalculator.percentage(m.getObtained(), m.getMax()))
                });
                ids.add(m.getId());
            }
            table.setRows(rows, ids);

            StringBuilder sb = new StringBuilder();
            MarksCalculator.bySubject(current).forEach((name, list) -> {
                double pct = MarksCalculator.percentage(list);
                sb.append(String.format("%-24s total %.1f / %.1f   %.1f%%   grade: %s%n",
                    name, MarksCalculator.totalObtained(list), MarksCalculator.totalMax(list), pct, MarksCalculator.grade(pct)));
            });
            totals.setText(sb.length() == 0 ? "No marks recorded yet." : sb.toString());
        } catch (DatabaseException e) {
            Ui.showError(e);
        }
    }

    private void select() {
        for (MarksRecord m : current) {
            if (m.getId() == table.getSelectedId()) {
                subject.selectId(m.getSubjectId());
                assessment.setText(m.getAssessment());
                obtained.setText("" + m.getObtained());
                max.setText("" + m.getMax());
            }
        }
    }

    private void add() {
        try {
            calc.add(subject.selectedSubjectId(), assessment.getText(), obtained.getText(), max.getText());
            reload();
            status("Marks added.");
        } catch (AcademiqException e) {
            Ui.showError(e);
        }
    }

    private void update() {
        try {
            calc.update(table.getSelectedId(), subject.selectedSubjectId(), assessment.getText(), obtained.getText(), max.getText());
            reload();
            status("Marks updated.");
        } catch (AcademiqException e) {
            Ui.showError(e);
        }
    }

    private void delete() {
        try {
            int id = Validators.requireSelection("marks record", table.getSelectedId());
            if (Dialogs.confirm("Delete this marks record?")) {
                calc.delete(id);
                reload();
            }
        } catch (AcademiqException e) {
            Ui.showError(e);
        }
    }
}
