package com.academiq.account.service;

import com.academiq.common.*;
import com.academiq.account.dao.AssignmentDAO;
import com.academiq.account.model.Assignment;
import com.academiq.account.model.Assignment.Status;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/** Business rules for assignments: validation, filtering, sorting, upcoming/overdue detection. */
public class AssignmentService {
    public enum Filter { ALL, PENDING, SUBMITTED, OVERDUE }
    public enum SortBy { DUE_DATE, SUBJECT, STATUS }

    private final AssignmentDAO dao = new AssignmentDAO();

    public int add(int subjectId, String title, String desc, String dueDate) throws AcademiqException {
        return dao.save(build(0, subjectId, title, desc, dueDate, Status.PENDING));
    }

    public void update(int id, int subjectId, String title, String desc, String dueDate) throws AcademiqException {
        Assignment old = dao.findById(id);
        if (old == null) throw new ValidationException("Select an assignment first.");
        dao.update(build(id, subjectId, title, desc, dueDate, old.getStatus()));
    }

    public void delete(int id) throws AcademiqException {
        if (id <= 0) throw new ValidationException("Select an assignment first.");
        dao.delete(id);
    }

    public void markSubmitted(int id) throws AcademiqException {
        Assignment a = dao.findById(id);
        if (a == null) throw new ValidationException("Select an assignment first.");
        a.setStatus(Status.SUBMITTED);
        dao.update(a);
    }

    private Assignment build(int id, int subjectId, String title, String desc, String due, Status st) throws ValidationException {
        Validators.requireSelection("subject", subjectId);
        return new Assignment(id, subjectId, Validators.requireText("Title", title, 150),
                Validators.optionalText(desc, 500), Validators.parseDate("Due date", due), st);
    }

    /** Filters and sorts the assignments. subjectId <= 0 means all subjects. */
    public List<Assignment> list(int subjectId, Filter filter, SortBy sort) throws DatabaseException {
        List<Assignment> all = dao.findAll();
        Comparator<Assignment> cmp = switch (sort) {
            case DUE_DATE -> Comparator.comparing(Assignment::getDueDate);
            case SUBJECT -> Comparator.comparing(Assignment::getSubjectName).thenComparing(Assignment::getDueDate);
            case STATUS -> Comparator.comparing((Assignment a) -> a.getStatus().name()).thenComparing(Assignment::getDueDate);
        };
        return all.stream()
            .filter(a -> subjectId <= 0 || a.getSubjectId() == subjectId)
            .filter(a -> switch (filter) {
                case ALL -> true;
                case PENDING -> a.getStatus() == Status.PENDING && !a.isOverdue();
                case SUBMITTED -> a.getStatus() == Status.SUBMITTED;
                case OVERDUE -> a.isOverdue();
            })
            .sorted(cmp)
            .collect(Collectors.toList());
    }

    /** Pending assignments due within the next N days (used by the dashboard and reminders). */
    public List<Assignment> upcoming(int days) throws DatabaseException {
        LocalDate limit = LocalDate.now().plusDays(days);
        return dao.findAll().stream()
            .filter(a -> a.getStatus() == Status.PENDING && !a.getDueDate().isBefore(LocalDate.now()) && !a.getDueDate().isAfter(limit))
            .collect(Collectors.toList());
    }

    public List<Assignment> search(String keyword) throws DatabaseException { return dao.search(keyword); }
}
