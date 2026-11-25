package com.academiq.common;

/** A subject of the current semester. Shared model: assignments, notes, attendance, marks and exams all point to it. */
public class Subject {
    private int id;
    private int userId;
    private String name;
    private int semester;

    public Subject(int id, int userId, String name, int semester) {
        this.id = id; this.userId = userId; this.name = name; this.semester = semester;
    }
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getName() { return name; }
    public int getSemester() { return semester; }
    public void setName(String name) { this.name = name; }
    public void setSemester(int semester) { this.semester = semester; }
    @Override public String toString() { return name; }
}
