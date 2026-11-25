package com.academiq.common;

import java.io.*;
import java.util.Properties;

/**
 * Loads academiq.properties (working directory first, then classpath).
 * Used for DB settings, attendance threshold, grade rules, etc. so nothing is hard-coded.
 */
public final class AppConfig {
    private static final Properties P = new Properties();
    static {
        P.setProperty("db.url", "jdbc:mysql://localhost:3306/academiq?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true");
        P.setProperty("db.user", "root");
        P.setProperty("db.password", "");
        P.setProperty("attendance.threshold", "75");
        P.setProperty("reminder.hoursBefore", "48");
        P.setProperty("reminder.checkSeconds", "60");
        P.setProperty("files.dir", "academiq_files");
        File f = new File("academiq.properties");
        try (InputStream in = f.exists() ? new FileInputStream(f)
                : AppConfig.class.getResourceAsStream("/academiq.properties")) {
            if (in != null) P.load(in);
        } catch (IOException e) {
            System.err.println("Could not read academiq.properties, using defaults: " + e.getMessage());
        }
    }
    private AppConfig() {}

    public static String get(String key, String def) { return P.getProperty(key, def); }
    public static double getDouble(String key, double def) {
        try { return Double.parseDouble(P.getProperty(key)); } catch (Exception e) { return def; }
    }
    public static int getInt(String key, int def) {
        try { return Integer.parseInt(P.getProperty(key).trim()); } catch (Exception e) { return def; }
    }
    /** Returns all keys starting with prefix (used for grade rules like grade.A=90). */
    public static Properties withPrefix(String prefix) {
        Properties out = new Properties();
        for (String k : P.stringPropertyNames())
            if (k.startsWith(prefix)) out.setProperty(k.substring(prefix.length()), P.getProperty(k));
        return out;
    }
}
