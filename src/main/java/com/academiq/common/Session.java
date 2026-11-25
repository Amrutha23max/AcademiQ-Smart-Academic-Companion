package com.academiq.common;

/** Holds the logged-in student. Every DAO filters by Session.userId() so users only see their own data. */
public final class Session {
    private static int userId = -1;
    private static String name = "";
    private Session() {}

    public static synchronized void login(int id, String fullName) { userId = id; name = fullName; }
    public static synchronized void logout() { userId = -1; name = ""; }
    public static synchronized int userId() { return userId; }
    public static synchronized String name() { return name; }
    public static synchronized boolean isLoggedIn() { return userId > 0; }
}
