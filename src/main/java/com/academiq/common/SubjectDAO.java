package com.academiq.common;

import java.sql.*;
import java.util.*;

/** JDBC access for subjects. */
public class SubjectDAO implements AcademicRepository<Subject> {

    @Override public int save(Subject s) throws DatabaseException {
        String sql = "INSERT INTO subjects(user_id, name, semester) VALUES (?,?,?)";
        try (Connection c = DatabaseConnection.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, Session.userId()); ps.setString(2, s.getName()); ps.setInt(3, s.getSemester());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { return k.next() ? k.getInt(1) : -1; }
        } catch (SQLException e) { throw new DatabaseException("Could not save subject: " + e.getMessage(), e); }
    }

    @Override public void update(Subject s) throws DatabaseException {
        String sql = "UPDATE subjects SET name=?, semester=? WHERE id=? AND user_id=?";
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, s.getName()); ps.setInt(2, s.getSemester()); ps.setInt(3, s.getId()); ps.setInt(4, Session.userId());
            ps.executeUpdate();
        } catch (SQLException e) { throw new DatabaseException("Could not update subject: " + e.getMessage(), e); }
    }

    @Override public void delete(int id) throws DatabaseException {
        try (Connection c = DatabaseConnection.get();
             PreparedStatement ps = c.prepareStatement("DELETE FROM subjects WHERE id=? AND user_id=?")) {
            ps.setInt(1, id); ps.setInt(2, Session.userId());
            ps.executeUpdate();
        } catch (SQLException e) { throw new DatabaseException("Could not delete subject: " + e.getMessage(), e); }
    }

    @Override public Subject findById(int id) throws DatabaseException {
        try (Connection c = DatabaseConnection.get();
             PreparedStatement ps = c.prepareStatement("SELECT id,user_id,name,semester FROM subjects WHERE id=? AND user_id=?")) {
            ps.setInt(1, id); ps.setInt(2, Session.userId());
            try (ResultSet r = ps.executeQuery()) { return r.next() ? map(r) : null; }
        } catch (SQLException e) { throw new DatabaseException("Could not read subject: " + e.getMessage(), e); }
    }

    @Override public List<Subject> findAll() throws DatabaseException {
        List<Subject> out = new ArrayList<>();
        try (Connection c = DatabaseConnection.get();
             PreparedStatement ps = c.prepareStatement("SELECT id,user_id,name,semester FROM subjects WHERE user_id=? ORDER BY semester, name")) {
            ps.setInt(1, Session.userId());
            try (ResultSet r = ps.executeQuery()) { while (r.next()) out.add(map(r)); }
        } catch (SQLException e) { throw new DatabaseException("Could not read subjects: " + e.getMessage(), e); }
        return out;
    }

    private Subject map(ResultSet r) throws SQLException {
        return new Subject(r.getInt("id"), r.getInt("user_id"), r.getString("name"), r.getInt("semester"));
    }
}
