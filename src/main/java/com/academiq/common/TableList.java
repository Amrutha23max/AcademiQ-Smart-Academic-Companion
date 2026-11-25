package com.academiq.common;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;

import java.util.ArrayList;
import java.util.List;

/** A TableView wrapper where each row remembers the database id it represents. */
public class TableList extends BorderPane {
    private final TableView<ObservableList<Object>> table = new TableView<>();
    private final List<Integer> ids = new ArrayList<>();
    private final String[] titles;
    private final int[] widths;

    @FunctionalInterface
    public interface TableEventListener {
        void onEvent(Object event);
    }

    public TableList(int visibleRows, String[] titles, int[] widths) {
        this.titles = titles;
        this.widths = widths;

        table.setStyle(
            "-fx-font-size: 13px; " +
            "-fx-background-color: white; " +
            "-fx-border-color: #D1D5DB; " +
            "-fx-border-radius: 4;"
        );
        table.setPrefHeight(Math.max(160, visibleRows * 28 + 35));

        for (int i = 0; i < titles.length; i++) {
            final int colIdx = i;
            TableColumn<ObservableList<Object>, String> col = new TableColumn<>(titles[i]);
            col.setCellValueFactory(cellData -> {
                ObservableList<Object> row = cellData.getValue();
                if (row != null && colIdx < row.size() && row.get(colIdx) != null) {
                    return new SimpleStringProperty(row.get(colIdx).toString());
                }
                return new SimpleStringProperty("");
            });

            if (widths != null && i < widths.length) {
                col.setPrefWidth(widths[i] * 8.5);
            }
            table.getColumns().add(col);
        }

        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        setCenter(table);
    }

    public void setRows(List<Object[]> rows, List<Integer> rowIds) {
        ids.clear();
        ids.addAll(rowIds);
        ObservableList<ObservableList<Object>> data = FXCollections.observableArrayList();
        for (Object[] r : rows) {
            data.add(FXCollections.observableArrayList(r));
        }
        table.setItems(data);
    }

    public void clear() {
        table.getItems().clear();
        ids.clear();
    }

    /** Database id of the selected row, or -1. */
    public int getSelectedId() {
        int idx = table.getSelectionModel().getSelectedIndex();
        return (idx < 0 || idx >= ids.size()) ? -1 : ids.get(idx);
    }

    public int getRowCount() {
        return ids.size();
    }

    public void addSelectionListener(TableEventListener l) {
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && l != null) l.onEvent(newV);
        });
    }

    public void addSelectionListener(Runnable r) {
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && r != null) r.run();
        });
    }

    public void addDoubleClickListener(TableEventListener l) {
        table.setRowFactory(tv -> {
            TableRow<ObservableList<Object>> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty()) && l != null) {
                    l.onEvent(event);
                }
            });
            return row;
        });
    }

    public void addDoubleClickListener(Runnable r) {
        table.setRowFactory(tv -> {
            TableRow<ObservableList<Object>> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty()) && r != null) {
                    r.run();
                }
            });
            return row;
        });
    }
}
