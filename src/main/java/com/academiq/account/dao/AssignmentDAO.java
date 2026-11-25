package com.academiq.account.dao;

import com.academiq.common.*;
import com.academiq.account.model.Assignment;
import java.sql.*;
import java.util.*;

/** JDBC persistence for assignments. */
public class AssignmentDAO implements AcademicRepository<Assignment>, Searchable<Assignment> {
    private static final String SELECT =
        "SELECT a.id, a.subject_id, s.name AS subject_name, a.title, a.description, a.due_date, a.status " +
        "FROM assignments a JOIN subjects s ON s.id = a.subject_id WHERE a.user_id=? ";

    @Override public int save(Assignment a) throws DatabaseException {
        String sql = "INSERT INTO assignments(user_id,subject_id,title,description,due_date,status) VALUES (?,?,?,?,?,?)";
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, Session.userId()); ps.setInt(2, a.getSubjectId()); ps.setString(3, a.getTitle());
            ps.setString(4, a.getDescription()); ps.setDate(5, java.sql.Date.valueOf(a.getDueDate())); ps.setString(6, a.getStatus().name());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { return k.next() ? k.getInt(1) : -1; }
        } catch (SQLException e) { throw new DatabaseException("Could not save assignment: " + e.getMessage(), e); }
    }

    @Override public void update(Assignment a) throws DatabaseException {
        String sql = "UPDATE assignments SET subject_id=?, title=?, description=?, due_date=?, status=? WHERE id=? AND user_id=?";
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, a.getSubjectId()); ps.setString(2, a.getTitle()); ps.setString(3, a.getDescription());
            ps.setDate(4, java.sql.Date.valueOf(a.getDueDate())); ps.setString(5, a.getStatus().name());
            ps.setInt(6, a.getId()); ps.setInt(7, Session.userId());
            ps.executeUpdate();
        } catch (SQLException e) { throw new DatabaseException("Could not update assignment: " + e.getMessage(), e); }
    }

    @Override public void delete(int id) throws DatabaseException {
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement("DELETE FROM assignments WHERE id=? AND user_id=?")) {
            ps.setInt(1, id); ps.setInt(2, Session.userId());
            ps.executeUpdate();
        } catch (SQLException e) { throw new DatabaseException("Could not delete assignment: " + e.getMessage(), e); }
    }

    @Override public Assignment findById(int id) throws DatabaseException {
        List<Assignment> l = run(SELECT + "AND a.id=?", id);
        return l.isEmpty() ? null : l.get(0);
    }

    @Override public List<Assignment> findAll() throws DatabaseException {
        return run(SELECT + "ORDER BY a.due_date");
    }

    @Override public List<Assignment> search(String keyword) throws DatabaseException {
        String k = "%" + keyword + "%";
        return run(SELECT + "AND (a.title LIKE ? OR a.description LIKE ? OR s.name LIKE ?) ORDER BY a.due_date", k, k, k);
    }

    private List<Assignment> run(String sql, Object... extra) throws DatabaseException {
        List<Assignment> out = new ArrayList<>();
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, Session.userId());
            for (int i = 0; i < extra.length; i++) ps.setObject(i + 2, extra[i]);
            try (ResultSet r = ps.executeQuery()) {
                while (r.next()) {
                    Assignment a = new Assignment(r.getInt("id"), r.getInt("subject_id"), r.getString("title"),
                        r.getString("description"), r.getDate("due_date").toLocalDate(), Assignment.Status.valueOf(r.getString("status")));
                    a.setSubjectName(r.getString("subject_name"));
                    out.add(a);
                }
            }
        } catch (SQLException e) { throw new DatabaseException("Could not read assignments: " + e.getMessage(), e); }
        return out;
    }
}
