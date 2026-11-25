package com.academiq.planner.ui;

import com.academiq.common.*;
import com.academiq.planner.service.*;
import com.academiq.planner.service.DashboardService.Data;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;

/** Home screen: counters, upcoming deadlines/exams, attendance & marks charts, reminder log. Owns the background ReminderService. */
public class DashboardPanel extends BasePanel {
    private final Label pending = counter(), overdue = counter(), exams = counter(), goals = counter(), streak = counter();
    private final TableList deadlines = new TableList(5, new String[]{"Next deadlines", "Subject", "Due"}, new int[]{22, 16, 11});
    private final TableList examList = new TableList(5, new String[]{"Next exams", "Subject", "When"}, new int[]{22, 16, 19});
    private final BarChart attendanceChart = new BarChart("Attendance % by subject", Ui.PRIMARY);
    private final BarChart marksChart = new BarChart("Marks % by subject", Ui.OK);
    private final TextArea reminderLog = new TextArea("No reminders yet.\n");
    private ReminderService reminders;

    public DashboardPanel() {
        VBox top = new VBox(10);
        top.getChildren().add(Ui.heading("Dashboard"));

        // counters
        HBox cards = new HBox(10);
        cards.setAlignment(Pos.CENTER);
        cards.getChildren().addAll(
            card("Pending", pending),
            card("Overdue", overdue),
            card("Exams (14 days)", exams),
            card("Goals done", goals),
            card("Study streak", streak)
        );
        for (javafx.scene.Node n : cards.getChildren()) {
            HBox.setHgrow(n, Priority.ALWAYS);
        }
        top.getChildren().add(cards);

        top.getChildren().add(Ui.row(
            Ui.button("Refresh", e -> refresh()),
            Ui.button("Check reminders now", e -> checkNow()),
            Ui.button("Re-announce all reminders", e -> resetReminders())
        ));
        top.setPadding(new Insets(0, 0, 10, 0));
        setTop(top);

        // tables and charts
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col1, col2);

        RowConstraints row1 = new RowConstraints();
        row1.setVgrow(Priority.ALWAYS);
        RowConstraints row2 = new RowConstraints();
        row2.setVgrow(Priority.ALWAYS);
        grid.getRowConstraints().addAll(row1, row2);

        grid.add(deadlines, 0, 0);
        grid.add(examList, 1, 0);
        grid.add(attendanceChart, 0, 1);
        grid.add(marksChart, 1, 1);

        setCenter(grid);

        // reminder log
        VBox south = new VBox(4);
        south.setPadding(new Insets(10, 0, 0, 0));
        south.getChildren().add(Ui.boldLabel("Reminders"));
        reminderLog.setEditable(false);
        reminderLog.setFont(Ui.MONO);
        reminderLog.setPrefRowCount(4);
        reminderLog.setStyle("-fx-font-family: Consolas; -fx-font-size: 12px; -fx-background-color: white; -fx-border-color: #D1D5DB; -fx-border-radius: 4;");
        south.getChildren().add(reminderLog);
        setBottom(south);
    }

    private static Label counter() {
        Label l = new Label("0");
        l.setFont(Font.font("System", FontWeight.BOLD, 24));
        l.setTextFill(Ui.PRIMARY_DARK);
        return l;
    }

    private static VBox card(String caption, Label value) {
        VBox p = new VBox(4);
        p.setAlignment(Pos.CENTER);
        p.setPadding(new Insets(10));
        p.setStyle("-fx-background-color: white; -fx-border-color: #D1D5DB; -fx-border-radius: 6; -fx-background-radius: 6;");

        Label c = new Label(caption);
        c.setFont(Font.font("System", 12));
        c.setTextFill(Color.web("#555555"));

        p.getChildren().addAll(value, c);
        return p;
    }

    @Override
    public String getTitle() { return "Dashboard"; }

    @Override
    public void onShow() { refresh(); }

    @Override
    public void start() {
        reminders = new ReminderService(new PopupReminderChannel(reminderLog), () -> Platform.runLater(this::refresh));
        reminders.start();
    }

    @Override
    public void stop() {
        if (reminders != null) reminders.stop();
    }

    public void refresh() {
        new Thread(() -> {
            try {
                Data d = new DashboardService().load();
                int streakDays = new StudyService().streak();
                Platform.runLater(() -> apply(d, streakDays));
            } catch (AcademiqException e) {
                Platform.runLater(() -> status(e.getMessage()));
            }
        }, "academiq-dashboard-load").start();
    }

    private void apply(Data d, int streakDays) {
        pending.setText("" + d.pending);
        overdue.setText("" + d.overdue);
        overdue.setTextFill(d.overdue > 0 ? Ui.DANGER : Ui.PRIMARY_DARK);
        exams.setText("" + d.upcomingExams);
        goals.setText(d.goalsDone + "/" + d.goalsTotal);
        streak.setText(streakDays + " d");

        fill(deadlines, d.deadlines);
        fill(examList, d.exams);
        attendanceChart.setData(d.attendance);
        marksChart.setData(d.marks);
    }

    private void fill(TableList t, List<String[]> rows) {
        List<Object[]> r = new ArrayList<>();
        List<Integer> ids = new ArrayList<>();
        if (rows != null) {
            for (int i = 0; i < rows.size(); i++) {
                r.add(rows.get(i));
                ids.add(i + 1);
            }
        }
        t.setRows(r, ids);
    }

    private void checkNow() {
        new Thread(() -> {
            try {
                int n = (reminders != null ? reminders : new ReminderService(new PopupReminderChannel(reminderLog), null)).checkNow();
                if (n == 0) ModuleHost.setStatus("No new deadline reminders.");
            } catch (AcademiqException e) {
                ModuleHost.setStatus(e.getMessage());
            }
        }, "academiq-manual-check").start();
    }

    private void resetReminders() {
        try {
            new ReminderService(null, null).resetAnnounced();
            checkNow();
        } catch (AcademiqException e) {
            Ui.showError(e);
        }
    }
}
