package com.academiq.planner.service;

import com.academiq.common.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;

/** CSV export of the stored records. */
public class ReportService {
    public enum Report {
        ASSIGNMENTS("Assignments", "SELECT s.name Subject, a.title Title, a.description Description, a.due_date DueDate, a.status Status " +
                "FROM assignments a JOIN subjects s ON s.id=a.subject_id WHERE a.user_id=? ORDER BY a.due_date"),
        ATTENDANCE("Attendance", "SELECT s.name Subject, a.classes_conducted Conducted, a.classes_attended Attended, " +
                "ROUND(IF(a.classes_conducted=0,0,a.classes_attended*100/a.classes_conducted),2) Percentage " +
                "FROM attendance a JOIN subjects s ON s.id=a.subject_id WHERE a.user_id=? ORDER BY s.name"),
        MARKS("Marks", "SELECT s.name Subject, m.assessment Assessment, m.obtained_marks Obtained, m.max_marks Maximum " +
                "FROM marks m JOIN subjects s ON s.id=m.subject_id WHERE m.user_id=? ORDER BY s.name, m.id"),
        EXAMS("Exam timetable", "SELECT s.name Subject, e.exam_name Exam, e.exam_date ExamDate, e.exam_time ExamTime " +
                "FROM exams e JOIN subjects s ON s.id=e.subject_id WHERE e.user_id=? ORDER BY e.exam_date, e.exam_time"),
        STUDY("Study goals", "SELECT g.description Goal, g.target_date TargetDate, IF(g.completed=1,'Done','Open') Status, " +
                "COUNT(s.id) Sessions, COALESCE(SUM(s.completed),0) SessionsDone FROM study_goals g LEFT JOIN study_sessions s ON s.goal_id=g.id " +
                "WHERE g.user_id=? GROUP BY g.id, g.description, g.target_date, g.completed ORDER BY g.target_date");

        public final String label; final String sql;
        Report(String label, String sql) { this.label = label; this.sql = sql; }
    }

    /** Writes the selected report to a CSV file and returns the number of data rows. */
    public int export(Report report, File target) throws AcademiqException {
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement(report.sql)) {
            ps.setInt(1, Session.userId());
            try (ResultSet r = ps.executeQuery(); PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(target), StandardCharsets.UTF_8))) {
                ResultSetMetaData md = r.getMetaData();
                int cols = md.getColumnCount();
                String[] header = new String[cols];
                for (int i = 0; i < cols; i++) header[i] = md.getColumnLabel(i + 1);
                out.write('\uFEFF');                       // BOM so Excel reads UTF-8 correctly
                out.println(line(header));
                int n = 0;
                while (r.next()) {
                    String[] row = new String[cols];
                    for (int i = 0; i < cols; i++) row[i] = r.getString(i + 1);
                    out.println(line(row)); n++;
                }
                return n;
            }
        } catch (SQLException e) {
            throw new DatabaseException("This report is not available (is that module's table installed?): " + e.getMessage(), e);
        } catch (IOException e) { throw new AcademiqException("Could not write the file: " + e.getMessage(), e); }
    }

    /** Generic export of any list of Exportable records . */
    public int export(List<? extends Exportable> items, File target) throws AcademiqException {
        if (items.isEmpty()) throw new ValidationException("Nothing to export.");
        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(target), StandardCharsets.UTF_8))) {
            out.write('\uFEFF');
            out.println(line(items.get(0).csvHeader()));
            for (Exportable e : items) out.println(line(e.csvRow()));
            return items.size();
        } catch (IOException e) { throw new AcademiqException("Could not write the file: " + e.getMessage(), e); }
    }

    static String line(String[] cells) {
        StringJoiner j = new StringJoiner(",");
        for (String c : cells) {
            String s = c == null ? "" : c;
            if (s.contains(",") || s.contains("\"") || s.contains("\n")) s = "\"" + s.replace("\"", "\"\"") + "\"";
            j.add(s);
        }
        return j.toString();
    }
}
