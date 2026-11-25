package com.academiq.records.dao;

import com.academiq.common.*;
import com.academiq.records.model.Note;
import java.sql.*;
import java.util.*;

public class NoteDAO implements AcademicRepository<Note>, Searchable<Note> {
    private static final String SELECT = "SELECT n.id,n.subject_id,s.name sname,n.title,n.topic,n.file_path,n.upload_date " +
            "FROM notes n JOIN subjects s ON s.id=n.subject_id WHERE n.user_id=? ";

    @Override public int save(Note n) throws DatabaseException {
        String sql = "INSERT INTO notes(user_id,subject_id,title,topic,file_path,upload_date) VALUES (?,?,?,?,?,?)";
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, Session.userId()); ps.setInt(2, n.getSubjectId()); ps.setString(3, n.getTitle());
            ps.setString(4, n.getTopic()); ps.setString(5, n.getFilePath()); ps.setDate(6, java.sql.Date.valueOf(n.getUploadDate()));
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { return k.next() ? k.getInt(1) : -1; }
        } catch (SQLException e) { throw new DatabaseException("Could not save note: " + e.getMessage(), e); }
    }
    @Override public void update(Note n) throws DatabaseException {
        String sql = "UPDATE notes SET subject_id=?, title=?, topic=? WHERE id=? AND user_id=?";
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, n.getSubjectId()); ps.setString(2, n.getTitle()); ps.setString(3, n.getTopic());
            ps.setInt(4, n.getId()); ps.setInt(5, Session.userId());
            ps.executeUpdate();
        } catch (SQLException e) { throw new DatabaseException("Could not update note: " + e.getMessage(), e); }
    }
    @Override public void delete(int id) throws DatabaseException {
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement("DELETE FROM notes WHERE id=? AND user_id=?")) {
            ps.setInt(1, id); ps.setInt(2, Session.userId()); ps.executeUpdate();
        } catch (SQLException e) { throw new DatabaseException("Could not delete note: " + e.getMessage(), e); }
    }
    @Override public Note findById(int id) throws DatabaseException {
        List<Note> l = run(SELECT + "AND n.id=?", id);
        return l.isEmpty() ? null : l.get(0);
    }
    @Override public List<Note> findAll() throws DatabaseException { return run(SELECT + "ORDER BY n.upload_date DESC, n.id DESC"); }
    @Override public List<Note> search(String kw) throws DatabaseException {
        String k = "%" + kw + "%";
        return run(SELECT + "AND (n.title LIKE ? OR n.topic LIKE ? OR s.name LIKE ?) ORDER BY n.upload_date DESC", k, k, k);
    }
    private List<Note> run(String sql, Object... extra) throws DatabaseException {
        List<Note> out = new ArrayList<>();
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, Session.userId());
            for (int i = 0; i < extra.length; i++) ps.setObject(i + 2, extra[i]);
            try (ResultSet r = ps.executeQuery()) {
                while (r.next()) {
                    Note n = new Note(r.getInt("id"), r.getInt("subject_id"), r.getString("title"), r.getString("topic"),
                            r.getString("file_path"), r.getDate("upload_date").toLocalDate());
                    n.setSubjectName(r.getString("sname")); out.add(n);
                }
            }
        } catch (SQLException e) { throw new DatabaseException("Could not read notes: " + e.getMessage(), e); }
        return out;
    }
}
