package com.academiq.records.service;

import com.academiq.common.*;
import com.academiq.records.dao.NoteDAO;
import com.academiq.records.model.Note;
import java.awt.Desktop;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/** Notes: copies the chosen file into the storage folder and saves its location; also search, open and delete. */
public class NoteService {
    private final NoteDAO dao = new NoteDAO();

    public int add(int subjectId, String title, String topic, String sourcePath) throws AcademiqException {
        Validators.requireSelection("subject", subjectId);
        title = Validators.requireText("Title", title, 150);
        topic = Validators.optionalText(topic, 100);
        if (sourcePath == null || sourcePath.trim().isEmpty()) throw new ValidationException("Choose a file to attach.");
        File src = new File(sourcePath.trim());
        if (!src.isFile()) throw new ValidationException("The selected file does not exist.");
        try {
            Path dir = Paths.get(AppConfig.get("files.dir", "academiq_files"), "user" + Session.userId());
            Files.createDirectories(dir);
            Path target = dir.resolve(System.currentTimeMillis() + "_" + src.getName().replaceAll("[^A-Za-z0-9._-]", "_"));
            Files.copy(src.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
            return dao.save(new Note(0, subjectId, title, topic, target.toString(), LocalDate.now()));
        } catch (IOException e) { throw new AcademiqException("Could not store the file: " + e.getMessage(), e); }
    }

    /** subjectId <= 0 means all subjects; keyword may be empty. */
    public List<Note> list(int subjectId, String keyword) throws DatabaseException {
        List<Note> base = (keyword == null || keyword.trim().isEmpty()) ? dao.findAll() : dao.search(keyword.trim());
        return base.stream().filter(n -> subjectId <= 0 || n.getSubjectId() == subjectId).collect(Collectors.toList());
    }

    public void open(int noteId) throws AcademiqException {
        Note n = dao.findById(noteId);
        if (n == null) throw new ValidationException("Select a note first.");
        File f = new File(n.getFilePath());
        if (!f.isFile()) throw new AcademiqException("The stored file is missing: " + f);
        try {
            if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.OPEN))
                throw new AcademiqException("Opening files is not supported on this system. File is at:\n" + f.getAbsolutePath());
            Desktop.getDesktop().open(f);
        } catch (IOException e) { throw new AcademiqException("Could not open the file: " + e.getMessage(), e); }
    }

    public void delete(int noteId) throws AcademiqException {
        Note n = dao.findById(noteId);
        if (n == null) throw new ValidationException("Select a note first.");
        dao.delete(noteId);
        try { Files.deleteIfExists(Paths.get(n.getFilePath())); } catch (IOException ignored) { /* DB row already removed */ }
    }
}
