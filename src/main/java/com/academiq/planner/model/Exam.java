package com.academiq.planner.model;

import com.academiq.common.Exportable;
import java.time.*;
import java.time.temporal.ChronoUnit;

public class Exam implements Exportable {
    private int id, subjectId;
    private String subjectName = "", name;
    private LocalDate date;
    private LocalTime time;

    public Exam(int id, int subjectId, String name, LocalDate date, LocalTime time) {
        this.id = id; this.subjectId = subjectId; this.name = name; this.date = date; this.time = time;
    }
    public int getId() { return id; }
    public int getSubjectId() { return subjectId; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String s) { subjectName = s; }
    public String getName() { return name; }
    public LocalDate getDate() { return date; }
    public LocalTime getTime() { return time; }
    public long daysLeft() { return ChronoUnit.DAYS.between(LocalDate.now(), date); }
    public String countdown() {
        long d = daysLeft();
        return d < 0 ? "completed" : d == 0 ? "TODAY" : d + " day(s) left";
    }
    @Override public String[] csvHeader() { return new String[]{"Subject", "Exam", "Date", "Time"}; }
    @Override public String[] csvRow() { return new String[]{subjectName, name, date.toString(), time.toString()}; }
}
