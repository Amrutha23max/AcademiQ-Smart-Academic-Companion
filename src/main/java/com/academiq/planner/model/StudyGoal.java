package com.academiq.planner.model;

import java.time.LocalDate;

public class StudyGoal {
    private int id;
    private String description;
    private LocalDate targetDate;
    private boolean completed;
    private int totalSessions, doneSessions;      // filled by the DAO for progress calculation

    public StudyGoal(int id, String description, LocalDate targetDate, boolean completed) {
        this.id = id; this.description = description; this.targetDate = targetDate; this.completed = completed;
    }
    public int getId() { return id; }
    public String getDescription() { return description; }
    public LocalDate getTargetDate() { return targetDate; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean c) { completed = c; }
    public int getTotalSessions() { return totalSessions; }
    public int getDoneSessions() { return doneSessions; }
    public void setSessionCounts(int total, int done) { totalSessions = total; doneSessions = done; }
    /** Progress: 100 when goal is completed, otherwise the share of completed sessions. */
    public double progress() {
        if (completed) return 100;
        return totalSessions == 0 ? 0 : doneSessions * 100.0 / totalSessions;
    }
}
