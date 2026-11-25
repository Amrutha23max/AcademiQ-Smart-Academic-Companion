package com.academiq.planner.service;

import com.academiq.common.*;
import com.academiq.planner.dao.ExamDAO;
import com.academiq.planner.model.Exam;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class ExamService {
    private final ExamDAO dao = new ExamDAO();

    public int add(int subjectId, String name, String date, String time) throws AcademiqException { return dao.save(build(0, subjectId, name, date, time)); }
    public void update(int id, int subjectId, String name, String date, String time) throws AcademiqException {
        Validators.requireSelection("exam", id);
        dao.update(build(id, subjectId, name, date, time));
    }
    public void delete(int id) throws AcademiqException { dao.delete(Validators.requireSelection("exam", id)); }
    public List<Exam> timetable() throws DatabaseException { return dao.findAll(); }
    public List<Exam> upcoming(int days) throws DatabaseException {
        LocalDate limit = LocalDate.now().plusDays(days);
        return dao.findAll().stream().filter(e -> !e.getDate().isBefore(LocalDate.now()) && !e.getDate().isAfter(limit)).collect(Collectors.toList());
    }
    private Exam build(int id, int subjectId, String name, String date, String time) throws ValidationException {
        Validators.requireSelection("subject", subjectId);
        return new Exam(id, subjectId, Validators.requireText("Exam name", name, 100), Validators.parseDate("Exam date", date), Validators.parseTime("Exam time", time));
    }
}
