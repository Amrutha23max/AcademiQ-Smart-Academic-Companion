package com.academiq.records.dao;

import com.academiq.common.*;
import com.academiq.records.model.AttendanceRecord;
import java.sql.*;
import java.util.*;

/** One attendance row per subject (UNIQUE subject_id), so save() is an "upsert". */
public class AttendanceDAO implements AcademicRepository<AttendanceRecord> {
    private static final String SELECT = "SELECT a.id,a.subject_id,s.name sname,a.classes_conducted,a.classes_attended " +
            "FROM attendance a JOIN subjects s ON s.id=a.subject_id WHERE a.user_id=? ";

    @Override public int save(AttendanceRecord r) throws DatabaseException {
        String sql = "INSERT INTO attendance(user_id,subject_id,classes_conducted,classes_attended) VALUES (?,?,?,?) " +
                     "ON DUPLICATE KEY UPDATE classes_conducted=VALUES(classes_conducted), classes_attended=VALUES(classes_attended)";
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, Session.userId()); ps.setInt(2, r.getSubjectId()); ps.setInt(3, r.getConducted()); ps.setInt(4, r.getAttended());
            ps.executeUpdate();
            return r.getSubjectId();
        } catch (SQLException e) { throw new DatabaseException("Could not save attendance: " + e.getMessage(), e); }
    }
    @Override public void update(AttendanceRecord r) throws DatabaseException { save(r); }
    @Override public void delete(int id) throws DatabaseException {
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement("DELETE FROM attendance WHERE id=? AND user_id=?")) {
            ps.setInt(1, id); ps.setInt(2, Session.userId()); ps.executeUpdate();
        } catch (SQLException e) { throw new DatabaseException("Could not delete attendance: " + e.getMessage(), e); }
    }
    @Override public AttendanceRecord findById(int id) throws DatabaseException {
        List<AttendanceRecord> l = run(SELECT + "AND a.id=?", id);
        return l.isEmpty() ? null : l.get(0);
    }
    public AttendanceRecord findBySubject(int subjectId) throws DatabaseException {
        List<AttendanceRecord> l = run(SELECT + "AND a.subject_id=?", subjectId);
        return l.isEmpty() ? null : l.get(0);
    }
    @Override public List<AttendanceRecord> findAll() throws DatabaseException { return run(SELECT + "ORDER BY s.name"); }

    private List<AttendanceRecord> run(String sql, Object... extra) throws DatabaseException {
        List<AttendanceRecord> out = new ArrayList<>();
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, Session.userId());
            for (int i = 0; i < extra.length; i++) ps.setObject(i + 2, extra[i]);
            try (ResultSet r = ps.executeQuery()) {
                while (r.next()) {
                    AttendanceRecord a = new AttendanceRecord(r.getInt("id"), r.getInt("subject_id"), r.getInt("classes_conducted"), r.getInt("classes_attended"));
                    a.setSubjectName(r.getString("sname")); out.add(a);
                }
            }
        } catch (SQLException e) { throw new DatabaseException("Could not read attendance: " + e.getMessage(), e); }
        return out;
    }
}
