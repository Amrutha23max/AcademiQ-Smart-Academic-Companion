package com.academiq.planner.service;

import com.academiq.common.*;
import java.sql.*;
import java.util.*;

/** Collects the numbers shown on the dashboard, reading the tables directly with SQL. */
public class DashboardService {
    public static class Data {
        public int pending, overdue, submitted, upcomingExams, goalsDone, goalsTotal;
        public List<String[]> deadlines = new ArrayList<>();      // title, subject, due
        public List<String[]> exams = new ArrayList<>();          // exam, subject, date time
        public Map<String, Double> attendance = new LinkedHashMap<>();
        public Map<String, Double> marks = new LinkedHashMap<>();
    }

    public Data load() throws DatabaseException {
        Data d = new Data();
        int uid = Session.userId();
        try (Connection c = DatabaseConnection.get()) {
            d.pending = count(c, "SELECT COUNT(*) FROM assignments WHERE user_id=? AND status='PENDING' AND due_date>=CURDATE()", uid);
            d.overdue = count(c, "SELECT COUNT(*) FROM assignments WHERE user_id=? AND status='PENDING' AND due_date<CURDATE()", uid);
            d.submitted = count(c, "SELECT COUNT(*) FROM assignments WHERE user_id=? AND status='SUBMITTED'", uid);
            d.upcomingExams = count(c, "SELECT COUNT(*) FROM exams WHERE user_id=? AND exam_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 14 DAY)", uid);
            d.goalsTotal = count(c, "SELECT COUNT(*) FROM study_goals WHERE user_id=?", uid);
            d.goalsDone = count(c, "SELECT COUNT(*) FROM study_goals WHERE user_id=? AND completed=1", uid);

            rows(c, "SELECT a.title, s.name, a.due_date FROM assignments a JOIN subjects s ON s.id=a.subject_id " +
                    "WHERE a.user_id=? AND a.status='PENDING' ORDER BY a.due_date LIMIT 6", uid, 3, d.deadlines);
            rows(c, "SELECT e.exam_name, s.name, CONCAT(e.exam_date,' ',e.exam_time) FROM exams e JOIN subjects s ON s.id=e.subject_id " +
                    "WHERE e.user_id=? AND e.exam_date>=CURDATE() ORDER BY e.exam_date, e.exam_time LIMIT 6", uid, 3, d.exams);
            percentMap(c, "SELECT s.name, a.classes_attended*100.0/a.classes_conducted FROM attendance a JOIN subjects s ON s.id=a.subject_id " +
                          "WHERE a.user_id=? AND a.classes_conducted>0 ORDER BY s.name", uid, d.attendance);
            percentMap(c, "SELECT s.name, SUM(m.obtained_marks)*100.0/SUM(m.max_marks) FROM marks m JOIN subjects s ON s.id=m.subject_id " +
                          "WHERE m.user_id=? GROUP BY s.name ORDER BY s.name", uid, d.marks);
        } catch (SQLException e) { throw new DatabaseException("Could not load the dashboard: " + e.getMessage(), e); }
        return d;
    }

    private int count(Connection c, String sql, int uid) {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, uid);
            try (ResultSet r = ps.executeQuery()) { return r.next() ? r.getInt(1) : 0; }
        } catch (SQLException e) { return 0; }       // table of another member not installed yet -> show 0
    }
    private void rows(Connection c, String sql, int uid, int cols, List<String[]> out) {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, uid);
            try (ResultSet r = ps.executeQuery()) {
                while (r.next()) { String[] row = new String[cols]; for (int i = 0; i < cols; i++) row[i] = r.getString(i + 1); out.add(row); }
            }
        } catch (SQLException e) { /* table not available */ }
    }
    private void percentMap(Connection c, String sql, int uid, Map<String, Double> out) {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, uid);
            try (ResultSet r = ps.executeQuery()) { while (r.next()) out.put(r.getString(1), r.getDouble(2)); }
        } catch (SQLException e) { /* table not available */ }
    }
}
