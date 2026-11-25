package com.academiq.records.ui;

import com.academiq.common.*;
import com.academiq.records.service.SummaryService;
import com.academiq.records.service.SummaryService.SubjectSummary;
import javafx.geometry.Insets;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/** Screen "Academic Summary": one row per subject combining attendance and marks. */
public class SummaryPanel extends BasePanel {
    private final TableList table = new TableList(16,
            new String[]{"Subject", "Attendance", "Att %", "Marks", "Marks %", "Grade"}, new int[]{26, 12, 8, 14, 9, 14});

    public SummaryPanel() {
        VBox top = new VBox(6);
        top.getChildren().add(Ui.heading("Subject-wise Academic Summary"));
        top.getChildren().add(Ui.row(Ui.button("Refresh", e -> reload())));
        top.setPadding(new Insets(0, 0, 10, 0));

        setTop(top);
        setCenter(table);
    }

    @Override
    public String getTitle() { return "Academic Summary"; }

    @Override
    public void onShow() { reload(); }

    private void reload() {
        try {
            List<Object[]> rows = new ArrayList<>();
            List<Integer> ids = new ArrayList<>();
            int i = 1;
            for (SubjectSummary s : new SummaryService().build()) {
                rows.add(new Object[]{
                    s.subject,
                    s.attended + "/" + s.conducted,
                    String.format("%.1f", s.attendancePct),
                    s.obtained + "/" + s.max,
                    String.format("%.1f", s.marksPct),
                    s.grade
                });
                ids.add(i++);
            }
            table.setRows(rows, ids);
        } catch (AcademiqException e) {
            Ui.showError(e);
        }
    }
}
