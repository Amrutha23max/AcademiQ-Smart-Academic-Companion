package com.academiq.account.model;

/** A registered student. */
public class User {
    private int id;
    private String fullName;
    private String email;
    private String course;
    private String passwordHash;

    public User(int id, String fullName, String email, String course, String passwordHash) {
        this.id = id; this.fullName = fullName; this.email = email; this.course = course; this.passwordHash = passwordHash;
    }
    public int getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getCourse() { return course; }
    public String getPasswordHash() { return passwordHash; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setCourse(String course) { this.course = course; }
}
