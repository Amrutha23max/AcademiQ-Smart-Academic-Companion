package com.academiq.planner.dao;

import com.academiq.common.*;
import com.academiq.planner.model.*;
import java.sql.*;
import java.util.*;

/** Goals (generic repository) plus the sessions that belong to a goal. */
public class StudyDAO implements AcademicRepository<StudyGoal> {

    @Override public int save(StudyGoal g) throws DatabaseException {
        try (Connection c = DatabaseConnection.get();
             PreparedStatement ps = c.prepareStatement("INSERT INTO study_goals(user_id,description,target_date,completed) VALUES (?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, Session.userId()); ps.setString(2, g.getDescription());
            ps.setDate(3, java.sql.Date.valueOf(g.getTargetDate())); ps.setBoolean(4, g.isCompleted());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { return k.next() ? k.getInt(1) : -1; }
        } catch (SQLException e) { throw new DatabaseException("Could not save goal: " + e.getMessage(), e); }
    }
    @Override public void update(StudyGoal g) throws DatabaseException {
        try (Connection c = DatabaseConnection.get();
             PreparedStatement ps = c.prepareStatement("UPDATE study_goals SET description=?, target_date=?, completed=? WHERE id=? AND user_id=?")) {
            ps.setString(1, g.getDescription()); ps.setDate(2, java.sql.Date.valueOf(g.getTargetDate()));
            ps.setBoolean(3, g.isCompleted()); ps.setInt(4, g.getId()); ps.setInt(5, Session.userId());
            ps.executeUpdate();
        } catch (SQLException e) { throw new DatabaseException("Could not update goal: " + e.getMessage(), e); }
    }
    @Override public void delete(int id) throws DatabaseException {
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement("DELETE FROM study_goals WHERE id=? AND user_id=?")) {
            ps.setInt(1, id); ps.setInt(2, Session.userId()); ps.executeUpdate();
        } catch (SQLException e) { throw new DatabaseException("Could not delete goal: " + e.getMessage(), e); }
    }
    @Override public StudyGoal findById(int id) throws DatabaseException {
        for (StudyGoal g : findAll()) if (g.getId() == id) return g;
        return null;
    }
    @Override public List<StudyGoal> findAll() throws DatabaseException {
        String sql = "SELECT g.id,g.description,g.target_date,g.completed, COUNT(s.id) total, COALESCE(SUM(s.completed),0) done " +
                     "FROM study_goals g LEFT JOIN study_sessions s ON s.goal_id=g.id WHERE g.user_id=? " +
                     "GROUP BY g.id,g.description,g.target_date,g.completed ORDER BY g.target_date";
        List<StudyGoal> out = new ArrayList<>();
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, Session.userId());
            try (ResultSet r = ps.executeQuery()) {
                while (r.next()) {
                    StudyGoal g = new StudyGoal(r.getInt("id"), r.getString("description"), r.getDate("target_date").toLocalDate(), r.getBoolean("completed"));
                    g.setSessionCounts(r.getInt("total"), r.getInt("done")); out.add(g);
                }
            }
        } catch (SQLException e) { throw new DatabaseException("Could not read goals: " + e.getMessage(), e); }
        return out;
    }

    // ---- sessions ----
    public int saveSession(StudySession s) throws DatabaseException {
        // only allow sessions for a goal that belongs to the logged-in user
        if (findById(s.getGoalId()) == null) throw new DatabaseException("Goal not found.", null);
        try (Connection c = DatabaseConnection.get();
             PreparedStatement ps = c.prepareStatement("INSERT INTO study_sessions(goal_id,session_date,duration_minutes,completed) VALUES (?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, s.getGoalId()); ps.setDate(2, java.sql.Date.valueOf(s.getDate()));
            ps.setInt(3, s.getMinutes()); ps.setBoolean(4, s.isCompleted());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { return k.next() ? k.getInt(1) : -1; }
        } catch (SQLException e) { throw new DatabaseException("Could not save session: " + e.getMessage(), e); }
    }
    public void setSessionCompleted(int sessionId, boolean done) throws DatabaseException {
        String sql = "UPDATE study_sessions s JOIN study_goals g ON g.id=s.goal_id SET s.completed=? WHERE s.id=? AND g.user_id=?";
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBoolean(1, done); ps.setInt(2, sessionId); ps.setInt(3, Session.userId());
            ps.executeUpdate();
        } catch (SQLException e) { throw new DatabaseException("Could not update session: " + e.getMessage(), e); }
    }
    public void deleteSession(int sessionId) throws DatabaseException {
        String sql = "DELETE s FROM study_sessions s JOIN study_goals g ON g.id=s.goal_id WHERE s.id=? AND g.user_id=?";
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, sessionId); ps.setInt(2, Session.userId()); ps.executeUpdate();
        } catch (SQLException e) { throw new DatabaseException("Could not delete session: " + e.getMessage(), e); }
    }
    public List<StudySession> sessionsOf(int goalId) throws DatabaseException {
        String sql = "SELECT s.id,s.goal_id,s.session_date,s.duration_minutes,s.completed FROM study_sessions s " +
                     "JOIN study_goals g ON g.id=s.goal_id WHERE s.goal_id=? AND g.user_id=? ORDER BY s.session_date";
        List<StudySession> out = new ArrayList<>();
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, goalId); ps.setInt(2, Session.userId());
            try (ResultSet r = ps.executeQuery()) {
                while (r.next()) out.add(new StudySession(r.getInt(1), r.getInt(2), r.getDate(3).toLocalDate(), r.getInt(4), r.getBoolean(5)));
            }
        } catch (SQLException e) { throw new DatabaseException("Could not read sessions: " + e.getMessage(), e); }
        return out;
    }
    /** Distinct dates on which at least one session was completed (for the streak). */
    public SortedSet<java.time.LocalDate> completedDates() throws DatabaseException {
        String sql = "SELECT DISTINCT s.session_date FROM study_sessions s JOIN study_goals g ON g.id=s.goal_id WHERE g.user_id=? AND s.completed=1";
        SortedSet<java.time.LocalDate> out = new TreeSet<>();
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, Session.userId());
            try (ResultSet r = ps.executeQuery()) { while (r.next()) out.add(r.getDate(1).toLocalDate()); }
        } catch (SQLException e) { throw new DatabaseException("Could not read study history: " + e.getMessage(), e); }
        return out;
    }
}
