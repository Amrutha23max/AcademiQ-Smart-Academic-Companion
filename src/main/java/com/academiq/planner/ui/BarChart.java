package com.academiq.planner.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;

import java.util.Map;

/** Bar chart of percentages (0-100) per subject. */
public class BarChart extends BorderPane {
    private final javafx.scene.chart.BarChart<String, Number> chart;
    private final XYChart.Series<String, Number> series;

    public BarChart(String title, Color barColor) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setTickLabelRotation(0);

        NumberAxis yAxis = new NumberAxis(0, 100, 25);
        yAxis.setLabel("%");

        chart = new javafx.scene.chart.BarChart<>(xAxis, yAxis);
        chart.setTitle(title);
        chart.setLegendVisible(false);
        chart.setAnimated(false);

        series = new XYChart.Series<>();
        chart.getData().add(series);

        chart.setStyle("-fx-font-size: 11px;");
        setPadding(new Insets(4));
        setStyle("-fx-background-color: white; -fx-border-color: #D1D5DB; -fx-border-radius: 6; -fx-background-radius: 6;");
        setCenter(chart);
        setPrefSize(380, 230);
    }

    public void setData(Map<String, Double> values) {
        Platform.runLater(() -> {
            series.getData().clear();
            if (values != null && !values.isEmpty()) {
                for (Map.Entry<String, Double> e : values.entrySet()) {
                    series.getData().add(new XYChart.Data<>(e.getKey(), Math.max(0, Math.min(100, e.getValue()))));
                }
            }
        });
    }
}
