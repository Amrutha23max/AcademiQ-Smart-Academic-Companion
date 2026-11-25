package com.academiq.planner.dao;

import com.academiq.common.*;
import com.academiq.planner.model.Exam;
import java.sql.*;
import java.util.*;

public class ExamDAO implements AcademicRepository<Exam> {
    private static final String SELECT = "SELECT e.id,e.subject_id,s.name sname,e.exam_name,e.exam_date,e.exam_time " +
            "FROM exams e JOIN subjects s ON s.id=e.subject_id WHERE e.user_id=? ";

    @Override public int save(Exam e) throws DatabaseException {
        String sql = "INSERT INTO exams(user_id,subject_id,exam_name,exam_date,exam_time) VALUES (?,?,?,?,?)";
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, Session.userId()); ps.setInt(2, e.getSubjectId()); ps.setString(3, e.getName());
            ps.setDate(4, java.sql.Date.valueOf(e.getDate())); ps.setTime(5, Time.valueOf(e.getTime()));
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { return k.next() ? k.getInt(1) : -1; }
        } catch (SQLException ex) { throw new DatabaseException("Could not save exam: " + ex.getMessage(), ex); }
    }
    @Override public void update(Exam e) throws DatabaseException {
        String sql = "UPDATE exams SET subject_id=?, exam_name=?, exam_date=?, exam_time=? WHERE id=? AND user_id=?";
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, e.getSubjectId()); ps.setString(2, e.getName()); ps.setDate(3, java.sql.Date.valueOf(e.getDate()));
            ps.setTime(4, Time.valueOf(e.getTime())); ps.setInt(5, e.getId()); ps.setInt(6, Session.userId());
            ps.executeUpdate();
        } catch (SQLException ex) { throw new DatabaseException("Could not update exam: " + ex.getMessage(), ex); }
    }
    @Override public void delete(int id) throws DatabaseException {
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement("DELETE FROM exams WHERE id=? AND user_id=?")) {
            ps.setInt(1, id); ps.setInt(2, Session.userId()); ps.executeUpdate();
        } catch (SQLException ex) { throw new DatabaseException("Could not delete exam: " + ex.getMessage(), ex); }
    }
    @Override public Exam findById(int id) throws DatabaseException {
        List<Exam> l = run(SELECT + "AND e.id=?", id);
        return l.isEmpty() ? null : l.get(0);
    }
    @Override public List<Exam> findAll() throws DatabaseException { return run(SELECT + "ORDER BY e.exam_date, e.exam_time"); }

    private List<Exam> run(String sql, Object... extra) throws DatabaseException {
        List<Exam> out = new ArrayList<>();
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, Session.userId());
            for (int i = 0; i < extra.length; i++) ps.setObject(i + 2, extra[i]);
            try (ResultSet r = ps.executeQuery()) {
                while (r.next()) {
                    Exam e = new Exam(r.getInt("id"), r.getInt("subject_id"), r.getString("exam_name"),
                            r.getDate("exam_date").toLocalDate(), r.getTime("exam_time").toLocalTime());
                    e.setSubjectName(r.getString("sname")); out.add(e);
                }
            }
        } catch (SQLException ex) { throw new DatabaseException("Could not read exams: " + ex.getMessage(), ex); }
        return out;
    }
}
