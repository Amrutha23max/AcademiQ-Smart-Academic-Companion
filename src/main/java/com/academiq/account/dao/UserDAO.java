package com.academiq.account.dao;

import com.academiq.common.*;
import com.academiq.account.model.User;
import java.sql.*;

/** All SQL for the users table. */
public class UserDAO {

    public int insert(String name, String email, String course, String hash) throws DatabaseException {
        String sql = "INSERT INTO users(full_name,email,course,password_hash) VALUES (?,?,?,?)";
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name); ps.setString(2, email); ps.setString(3, course); ps.setString(4, hash);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { return k.next() ? k.getInt(1) : -1; }
        } catch (SQLException e) { throw new DatabaseException("Could not create account: " + e.getMessage(), e); }
    }

    public User findByEmail(String email) throws DatabaseException {
        return query("SELECT id,full_name,email,course,password_hash FROM users WHERE email=?", email);
    }

    public User findById(int id) throws DatabaseException {
        return query("SELECT id,full_name,email,course,password_hash FROM users WHERE id=?", id);
    }

    private User query(String sql, Object param) throws DatabaseException {
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, param);
            try (ResultSet r = ps.executeQuery()) {
                return r.next() ? new User(r.getInt(1), r.getString(2), r.getString(3), r.getString(4), r.getString(5)) : null;
            }
        } catch (SQLException e) { throw new DatabaseException("Could not read user: " + e.getMessage(), e); }
    }

    public void updateProfile(int id, String name, String course) throws DatabaseException {
        exec("UPDATE users SET full_name=?, course=? WHERE id=?", name, course, id);
    }

    public void updatePassword(int id, String hash) throws DatabaseException {
        exec("UPDATE users SET password_hash=? WHERE id=?", hash, id);
    }

    private void exec(String sql, Object... params) throws DatabaseException {
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            ps.executeUpdate();
        } catch (SQLException e) { throw new DatabaseException("Database update failed: " + e.getMessage(), e); }
    }
}
