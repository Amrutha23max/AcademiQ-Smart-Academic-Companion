package com.academiq.common;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;

/** Runs the SQL scripts in resources/sql on startup. Every script uses CREATE TABLE IF NOT EXISTS, so it is safe to repeat. */
public final class SchemaInstaller {
    private static final String[] SCRIPTS = {"01_users_subjects_assignments.sql", "02_notes_attendance_marks.sql", "03_exams_study_reminders.sql"};
    private SchemaInstaller() {}

    public static void install() throws DatabaseException {
        try (Connection c = DatabaseConnection.get(); Statement st = c.createStatement()) {
            for (String name : SCRIPTS) {
                InputStream in = SchemaInstaller.class.getResourceAsStream("/sql/" + name);
                if (in == null) continue;               // script not found
                String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                in.close();
                for (String sql : text.split(";")) {
                    String s = sql.replaceAll("(?m)^\\s*--.*$", "").trim();
                    if (!s.isEmpty()) st.execute(s);
                }
            }
        } catch (SQLException | IOException e) {
            throw new DatabaseException("Schema installation failed: " + e.getMessage(), e);
        }
    }
}
