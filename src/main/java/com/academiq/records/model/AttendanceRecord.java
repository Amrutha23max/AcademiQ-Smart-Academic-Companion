package com.academiq.records.model;

import com.academiq.common.Exportable;

public class AttendanceRecord implements Exportable {
    private int id, subjectId, conducted, attended;
    private String subjectName = "";

    public AttendanceRecord(int id, int subjectId, int conducted, int attended) {
        this.id = id; this.subjectId = subjectId; this.conducted = conducted; this.attended = attended;
    }
    public int getId() { return id; }
    public int getSubjectId() { return subjectId; }
    public int getConducted() { return conducted; }
    public int getAttended() { return attended; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String s) { subjectName = s; }
    /** Percentage; 0 when no class has been conducted (no division by zero). */
    public double percentage() { return conducted == 0 ? 0 : attended * 100.0 / conducted; }

    @Override public String[] csvHeader() { return new String[]{"Subject", "Conducted", "Attended", "Percentage"}; }
    @Override public String[] csvRow() { return new String[]{subjectName, "" + conducted, "" + attended, String.format("%.2f", percentage())}; }
}
