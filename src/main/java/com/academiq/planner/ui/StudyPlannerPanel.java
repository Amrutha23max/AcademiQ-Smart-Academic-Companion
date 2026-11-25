package com.academiq.planner.ui;

import com.academiq.common.*;
import com.academiq.planner.model.*;
import com.academiq.planner.service.StudyService;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Screen "Study Planner": goals (with progress %) and the sessions of the selected goal. */
public class StudyPlannerPanel extends BasePanel {
    private final StudyService service = new StudyService();
    private final TextField goalText = Ui.field(34), goalDate = Ui.field(12), sessDate = Ui.field(12), sessMinutes = Ui.field(6);
    private final Label summary = Ui.boldLabel(" ");
    private final TableList goalTable = new TableList(6, new String[]{"Goal", "Target", "Sessions", "Progress", "Status"}, new int[]{34, 11, 9, 9, 9});
    private final TableList sessionTable = new TableList(6, new String[]{"Session date", "Minutes", "Status"}, new int[]{14, 9, 12});

    public StudyPlannerPanel() {
        VBox head = new VBox(6);
        head.getChildren().add(Ui.heading("Study Planner"));
        head.getChildren().add(summary);
        head.setPadding(new Insets(0, 0, 10, 0));
        setTop(head);

        VBox goalsBox = new VBox(6);
        goalsBox.getChildren().add(Ui.row(
            Ui.label("Goal:"), goalText,
            Ui.label("Target date:"), goalDate,
            Ui.button("Add goal", e -> addGoal()),
            Ui.button("Toggle goal done", e -> toggleGoal()),
            Ui.button("Delete goal", e -> deleteGoal())
        ));
        goalsBox.getChildren().add(goalTable);
        VBox.setVgrow(goalTable, Priority.ALWAYS);

        VBox sessionsBox = new VBox(6);
        sessionsBox.getChildren().add(Ui.row(
            Ui.boldLabel("Sessions of selected goal -"),
            Ui.label("Date:"), sessDate,
            Ui.label("Minutes:"), sessMinutes,
            Ui.button("Add session", e -> addSession()),
            Ui.button("Mark done", e -> sessionDone(true)),
            Ui.button("Mark not done", e -> sessionDone(false)),
            Ui.button("Delete session", e -> deleteSession())
        ));
        sessionsBox.getChildren().add(sessionTable);
        VBox.setVgrow(sessionTable, Priority.ALWAYS);

        VBox both = new VBox(14);
        both.getChildren().addAll(goalsBox, sessionsBox);
        setCenter(both);

        goalTable.addSelectionListener(e -> loadSessions());
        goalDate.setText(LocalDate.now().plusDays(7).toString());
        sessDate.setText(LocalDate.now().toString());
    }

    @Override
    public String getTitle() { return "Study Planner"; }

    @Override
    public void onShow() { reloadGoals(); }

    private void reloadGoals() {
        try {
            List<Object[]> rows = new ArrayList<>();
            List<Integer> ids = new ArrayList<>();
            for (StudyGoal g : service.goals()) {
                rows.add(new Object[]{
                    g.getDescription(),
                    g.getTargetDate(),
                    g.getDoneSessions() + "/" + g.getTotalSessions(),
                    String.format("%.0f%%", g.progress()),
                    g.isCompleted() ? "DONE" : "open"
                });
                ids.add(g.getId());
            }
            goalTable.setRows(rows, ids);
            summary.setText(String.format("Overall progress: %.0f%%    Study streak: %d day(s)",
                service.overallProgress(), service.streak()));
            loadSessions();
        } catch (DatabaseException e) {
            Ui.showError(e);
        }
    }

    private void loadSessions() {
        try {
            List<Object[]> rows = new ArrayList<>();
            List<Integer> ids = new ArrayList<>();
            if (goalTable.getSelectedId() > 0) {
                for (StudySession s : service.sessions(goalTable.getSelectedId())) {
                    rows.add(new Object[]{s.getDate(), s.getMinutes(), s.isCompleted() ? "done" : "planned"});
                    ids.add(s.getId());
                }
            }
            sessionTable.setRows(rows, ids);
        } catch (DatabaseException e) {
            Ui.showError(e);
        }
    }

    private void addGoal() {
        try {
            service.addGoal(goalText.getText(), goalDate.getText());
            goalText.setText("");
            reloadGoals();
            status("Goal added.");
        } catch (AcademiqException e) {
            Ui.showError(e);
        }
    }

    private void toggleGoal() {
        try {
            int id = Validators.requireSelection("goal", goalTable.getSelectedId());
            for (StudyGoal g : service.goals()) {
                if (g.getId() == id) {
                    service.setGoalCompleted(id, !g.isCompleted());
                }
            }
            reloadGoals();
        } catch (AcademiqException e) {
            Ui.showError(e);
        }
    }

    private void deleteGoal() {
        try {
            int id = Validators.requireSelection("goal", goalTable.getSelectedId());
            if (Dialogs.confirm("Delete this goal and its sessions?")) {
                service.deleteGoal(id);
                reloadGoals();
            }
        } catch (AcademiqException e) {
            Ui.showError(e);
        }
    }

    private void addSession() {
        try {
            int g = goalTable.getSelectedId();
            service.addSession(g, sessDate.getText(), sessMinutes.getText());
            reloadGoals();
        } catch (AcademiqException e) {
            Ui.showError(e);
        }
    }

    private void sessionDone(boolean done) {
        try {
            service.setSessionCompleted(sessionTable.getSelectedId(), done);
            reloadGoals();
        } catch (AcademiqException e) {
            Ui.showError(e);
        }
    }

    private void deleteSession() {
        try {
            service.deleteSession(sessionTable.getSelectedId());
            reloadGoals();
        } catch (AcademiqException e) {
            Ui.showError(e);
        }
    }
}
