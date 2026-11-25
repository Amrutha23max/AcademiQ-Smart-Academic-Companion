package com.academiq.records.dao;

import com.academiq.common.*;
import com.academiq.records.model.MarksRecord;
import java.sql.*;
import java.util.*;

public class MarksDAO implements AcademicRepository<MarksRecord> {
    private static final String SELECT = "SELECT m.id,m.subject_id,s.name sname,m.assessment,m.obtained_marks,m.max_marks " +
            "FROM marks m JOIN subjects s ON s.id=m.subject_id WHERE m.user_id=? ";

    @Override public int save(MarksRecord m) throws DatabaseException {
        String sql = "INSERT INTO marks(user_id,subject_id,assessment,obtained_marks,max_marks) VALUES (?,?,?,?,?)";
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, Session.userId()); ps.setInt(2, m.getSubjectId()); ps.setString(3, m.getAssessment());
            ps.setDouble(4, m.getObtained()); ps.setDouble(5, m.getMax());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { return k.next() ? k.getInt(1) : -1; }
        } catch (SQLException e) { throw new DatabaseException("Could not save marks: " + e.getMessage(), e); }
    }
    @Override public void update(MarksRecord m) throws DatabaseException {
        String sql = "UPDATE marks SET subject_id=?, assessment=?, obtained_marks=?, max_marks=? WHERE id=? AND user_id=?";
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, m.getSubjectId()); ps.setString(2, m.getAssessment()); ps.setDouble(3, m.getObtained());
            ps.setDouble(4, m.getMax()); ps.setInt(5, m.getId()); ps.setInt(6, Session.userId());
            ps.executeUpdate();
        } catch (SQLException e) { throw new DatabaseException("Could not update marks: " + e.getMessage(), e); }
    }
    @Override public void delete(int id) throws DatabaseException {
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement("DELETE FROM marks WHERE id=? AND user_id=?")) {
            ps.setInt(1, id); ps.setInt(2, Session.userId()); ps.executeUpdate();
        } catch (SQLException e) { throw new DatabaseException("Could not delete marks: " + e.getMessage(), e); }
    }
    @Override public MarksRecord findById(int id) throws DatabaseException {
        List<MarksRecord> l = run(SELECT + "AND m.id=?", id);
        return l.isEmpty() ? null : l.get(0);
    }
    @Override public List<MarksRecord> findAll() throws DatabaseException { return run(SELECT + "ORDER BY s.name, m.id"); }

    private List<MarksRecord> run(String sql, Object... extra) throws DatabaseException {
        List<MarksRecord> out = new ArrayList<>();
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, Session.userId());
            for (int i = 0; i < extra.length; i++) ps.setObject(i + 2, extra[i]);
            try (ResultSet r = ps.executeQuery()) {
                while (r.next()) {
                    MarksRecord m = new MarksRecord(r.getInt("id"), r.getInt("subject_id"), r.getString("assessment"),
                            r.getDouble("obtained_marks"), r.getDouble("max_marks"));
                    m.setSubjectName(r.getString("sname")); out.add(m);
                }
            }
        } catch (SQLException e) { throw new DatabaseException("Could not read marks: " + e.getMessage(), e); }
        return out;
    }
}
