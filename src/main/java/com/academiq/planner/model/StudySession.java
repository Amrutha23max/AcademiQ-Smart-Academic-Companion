package com.academiq.planner.model;

import java.time.LocalDate;

public class StudySession {
    private int id, goalId, minutes;
    private LocalDate date;
    private boolean completed;
    public StudySession(int id, int goalId, LocalDate date, int minutes, boolean completed) {
        this.id = id; this.goalId = goalId; this.date = date; this.minutes = minutes; this.completed = completed;
    }
    public int getId() { return id; }
    public int getGoalId() { return goalId; }
    public LocalDate getDate() { return date; }
    public int getMinutes() { return minutes; }
    public boolean isCompleted() { return completed; }
}
