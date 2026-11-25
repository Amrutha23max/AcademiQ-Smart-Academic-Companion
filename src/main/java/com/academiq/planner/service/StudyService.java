package com.academiq.planner.service;

import com.academiq.common.*;
import com.academiq.planner.dao.StudyDAO;
import com.academiq.planner.model.*;
import java.time.LocalDate;
import java.util.*;

/** Study goals, sessions, progress percentage and streak. */
public class StudyService {
    private final StudyDAO dao = new StudyDAO();

    public int addGoal(String description, String targetDate) throws AcademiqException {
        return dao.save(new StudyGoal(0, Validators.requireText("Goal", description, 200), Validators.parseDate("Target date", targetDate), false));
    }
    public void setGoalCompleted(int goalId, boolean done) throws AcademiqException {
        StudyGoal g = dao.findById(Validators.requireSelection("goal", goalId));
        if (g == null) throw new ValidationException("Goal not found.");
        g.setCompleted(done); dao.update(g);
    }
    public void deleteGoal(int goalId) throws AcademiqException { dao.delete(Validators.requireSelection("goal", goalId)); }
    public List<StudyGoal> goals() throws DatabaseException { return dao.findAll(); }

    public int addSession(int goalId, String date, String minutes) throws AcademiqException {
        Validators.requireSelection("goal", goalId);
        int m = Validators.parseInt("Duration (minutes)", minutes);
        if (m <= 0 || m > 960) throw new ValidationException("Duration must be between 1 and 960 minutes.");
        return dao.saveSession(new StudySession(0, goalId, Validators.parseDate("Session date", date), m, false));
    }
    public void setSessionCompleted(int sessionId, boolean done) throws AcademiqException {
        dao.setSessionCompleted(Validators.requireSelection("session", sessionId), done);
    }
    public void deleteSession(int sessionId) throws AcademiqException { dao.deleteSession(Validators.requireSelection("session", sessionId)); }
    public List<StudySession> sessions(int goalId) throws DatabaseException { return dao.sessionsOf(goalId); }

    /** Overall progress across all goals (average of goal progress). */
    public double overallProgress() throws DatabaseException {
        List<StudyGoal> gs = dao.findAll();
        return gs.isEmpty() ? 0 : gs.stream().mapToDouble(StudyGoal::progress).average().orElse(0);
    }

    /** Streak = number of consecutive days (ending today, or yesterday if nothing is done yet today) with a completed session. */
    public int streak() throws DatabaseException {
        SortedSet<LocalDate> days = dao.completedDates();
        LocalDate d = LocalDate.now();
        if (!days.contains(d)) d = d.minusDays(1);
        int n = 0;
        while (days.contains(d)) { n++; d = d.minusDays(1); }
        return n;
    }
}
