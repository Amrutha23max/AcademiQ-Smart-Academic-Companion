package com.academiq.records.service;

import com.academiq.common.*;
import com.academiq.records.dao.AttendanceDAO;
import com.academiq.records.exception.InvalidAttendanceException;
import com.academiq.records.model.AttendanceRecord;
import java.util.List;

/** Attendance rules and calculations. The threshold comes from academiq.properties (attendance.threshold). */
public class AttendanceService {
    private final AttendanceDAO dao = new AttendanceDAO();

    public static double threshold() { return AppConfig.getDouble("attendance.threshold", 75); }

    public void save(int subjectId, String conductedText, String attendedText) throws AcademiqException {
        Validators.requireSelection("subject", subjectId);
        int conducted = Validators.parseInt("Classes conducted", conductedText);
        int attended = Validators.parseInt("Classes attended", attendedText);
        validate(conducted, attended);
        dao.save(new AttendanceRecord(0, subjectId, conducted, attended));
    }

    /** Quick entry: one more class held, and the student was present (or absent). */
    public void recordClass(int subjectId, boolean present) throws AcademiqException {
        Validators.requireSelection("subject", subjectId);
        AttendanceRecord r = dao.findBySubject(subjectId);
        int conducted = (r == null ? 0 : r.getConducted()) + 1;
        int attended = (r == null ? 0 : r.getAttended()) + (present ? 1 : 0);
        dao.save(new AttendanceRecord(0, subjectId, conducted, attended));
    }

    public void delete(int id) throws AcademiqException { dao.delete(Validators.requireSelection("record", id)); }
    public List<AttendanceRecord> list() throws DatabaseException { return dao.findAll(); }

    static void validate(int conducted, int attended) throws InvalidAttendanceException {
        if (conducted < 0 || attended < 0) throw new InvalidAttendanceException("Counts cannot be negative.");
        if (attended > conducted) throw new InvalidAttendanceException("Classes attended (" + attended + ") cannot exceed classes conducted (" + conducted + ").");
    }

    public static double percentage(int attended, int conducted) { return conducted == 0 ? 0 : attended * 100.0 / conducted; }

    public static boolean belowThreshold(AttendanceRecord r) { return r.getConducted() > 0 && r.percentage() < threshold(); }

    /** How many more consecutive classes must be attended to reach target%? 0 = already there, -1 = impossible. */
    public static int classesNeeded(int attended, int conducted, double target) {
        if (percentage(attended, conducted) >= target) return 0;
        if (target >= 100) return -1;
        return (int) Math.ceil((target * conducted - 100.0 * attended) / (100.0 - target));
    }

    /** How many classes can be missed while staying at or above target%? */
    public static int classesCanSkip(int attended, int conducted, double target) {
        if (target <= 0) return Integer.MAX_VALUE;
        if (percentage(attended, conducted) < target) return 0;
        return (int) Math.floor(attended * 100.0 / target - conducted);
    }

    /** Human readable advice for one record. */
    public static String advice(AttendanceRecord r) {
        double t = threshold();
        if (r.getConducted() == 0) return "No classes recorded yet.";
        if (r.percentage() >= t) return String.format("OK - you can still miss %d class(es) and stay above %.0f%%.", classesCanSkip(r.getAttended(), r.getConducted(), t), t);
        int need = classesNeeded(r.getAttended(), r.getConducted(), t);
        return need < 0 ? "Target cannot be reached." : String.format("LOW - attend the next %d class(es) in a row to reach %.0f%%.", need, t);
    }
}
