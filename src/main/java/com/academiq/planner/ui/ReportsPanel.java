package com.academiq.planner.ui;

import com.academiq.common.*;
import com.academiq.planner.service.ReportService;
import com.academiq.planner.service.ReportService.Report;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.io.File;

/** Screen "Reports": export any record set to CSV. */
public class ReportsPanel extends BasePanel {
    private final ComboBox<Report> type = new ComboBox<>();
    private final Label result = Ui.label(" ");

    public ReportsPanel() {
        VBox top = new VBox(10);
        top.getChildren().add(Ui.heading("Reports & Export"));

        type.setItems(FXCollections.observableArrayList(Report.values()));
        type.setConverter(new StringConverter<>() {
            @Override
            public String toString(Report r) { return r != null ? r.label : ""; }
            @Override
            public Report fromString(String string) { return null; }
        });
        type.getSelectionModel().selectFirst();
        type.setStyle("-fx-font-size: 13px; -fx-background-color: white; -fx-border-color: #D1D5DB; -fx-border-radius: 4;");

        VBox body = new VBox(12);
        body.setPadding(new Insets(10));
        body.setStyle("-fx-background-color: white; -fx-border-color: #E0E0E0; -fx-border-radius: 6; -fx-background-radius: 6;");

        body.getChildren().add(Ui.label("Choose what to export. The CSV file opens in Excel / Google Sheets."));
        body.getChildren().add(Ui.row(
            Ui.label("Report:"),
            type,
            Ui.button("Export to CSV...", e -> export())
        ));
        body.getChildren().add(result);

        top.getChildren().add(body);
        setTop(top);
    }

    @Override
    public String getTitle() { return "Reports"; }

    private void export() {
        Report selected = type.getValue();
        if (selected == null) return;
        File f = Dialogs.chooseFile("Save " + selected.label + " as CSV", Dialogs.SAVE, selected.name().toLowerCase() + ".csv");
        if (f == null) return;
        try {
            int rows = new ReportService().export(selected, f);
            result.setText("Exported " + rows + " row(s) to " + f.getAbsolutePath());
            result.setStyle("-fx-text-fill: " + Ui.HEX_OK + "; -fx-font-weight: bold;");
            status("Report exported.");
        } catch (AcademiqException e) {
            Ui.showError(e);
        }
    }
}
