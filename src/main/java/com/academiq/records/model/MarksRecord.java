package com.academiq.records.model;

import com.academiq.common.Exportable;

public class MarksRecord implements Exportable {
    private int id, subjectId;
    private String subjectName = "", assessment;
    private double obtained, max;

    public MarksRecord(int id, int subjectId, String assessment, double obtained, double max) {
        this.id = id; this.subjectId = subjectId; this.assessment = assessment; this.obtained = obtained; this.max = max;
    }
    public int getId() { return id; }
    public int getSubjectId() { return subjectId; }
    public String getAssessment() { return assessment; }
    public double getObtained() { return obtained; }
    public double getMax() { return max; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String s) { subjectName = s; }

    @Override public String[] csvHeader() { return new String[]{"Subject", "Assessment", "Obtained", "Maximum"}; }
    @Override public String[] csvRow() { return new String[]{subjectName, assessment, "" + obtained, "" + max}; }
}
