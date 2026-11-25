package com.academiq.account.model;

import com.academiq.common.Exportable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** One assignment. Exportable so it can be written to CSV. */
public class Assignment implements Exportable {
    public enum Status { PENDING, SUBMITTED }

    private int id;
    private int subjectId;
    private String subjectName = "";
    private String title;
    private String description;
    private LocalDate dueDate;
    private Status status;

    public Assignment(int id, int subjectId, String title, String description, LocalDate dueDate, Status status) {
        this.id = id; this.subjectId = subjectId; this.title = title;
        this.description = description; this.dueDate = dueDate; this.status = status;
    }
    public int getId() { return id; }
    public int getSubjectId() { return subjectId; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String n) { this.subjectName = n; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDate getDueDate() { return dueDate; }
    public Status getStatus() { return status; }
    public void setStatus(Status s) { this.status = s; }

    public boolean isOverdue() { return status == Status.PENDING && dueDate.isBefore(LocalDate.now()); }
    public long daysLeft() { return ChronoUnit.DAYS.between(LocalDate.now(), dueDate); }

    /** Text shown in the table: PENDING / SUBMITTED / OVERDUE (3d late). */
    public String displayStatus() {
        if (isOverdue()) return "OVERDUE (" + (-daysLeft()) + "d late)";
        if (status == Status.PENDING) return daysLeft() == 0 ? "PENDING (due today)" : "PENDING (" + daysLeft() + "d left)";
        return "SUBMITTED";
    }

    @Override public String[] csvHeader() { return new String[]{"Subject", "Title", "Description", "Due date", "Status"}; }
    @Override public String[] csvRow() { return new String[]{subjectName, title, description, dueDate.toString(), displayStatus()}; }
}
