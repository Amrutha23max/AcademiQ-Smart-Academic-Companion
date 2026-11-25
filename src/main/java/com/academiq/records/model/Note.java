package com.academiq.records.model;

import com.academiq.common.Exportable;
import java.time.LocalDate;

public class Note implements Exportable {
    private int id, subjectId;
    private String subjectName = "", title, topic, filePath;
    private LocalDate uploadDate;

    public Note(int id, int subjectId, String title, String topic, String filePath, LocalDate uploadDate) {
        this.id = id; this.subjectId = subjectId; this.title = title; this.topic = topic;
        this.filePath = filePath; this.uploadDate = uploadDate;
    }
    public int getId() { return id; }
    public int getSubjectId() { return subjectId; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String s) { subjectName = s; }
    public String getTitle() { return title; }
    public String getTopic() { return topic; }
    public String getFilePath() { return filePath; }
    public LocalDate getUploadDate() { return uploadDate; }

    @Override public String[] csvHeader() { return new String[]{"Subject", "Title", "Topic", "Uploaded", "File"}; }
    @Override public String[] csvRow() { return new String[]{subjectName, title, topic, uploadDate.toString(), filePath}; }
}
