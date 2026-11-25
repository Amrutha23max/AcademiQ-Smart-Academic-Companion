package com.academiq.records.service;

import com.academiq.common.*;
import com.academiq.records.dao.MarksDAO;
import com.academiq.records.exception.InvalidMarksException;
import com.academiq.records.model.MarksRecord;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Marks entry, totals, percentages and grades.
 * Grade boundaries come from academiq.properties (e.g. grade.O=90). If none are configured the grade is "N/A".
 */
public class MarksCalculator {
    private final MarksDAO dao = new MarksDAO();

    public int add(int subjectId, String assessment, String obtained, String max) throws AcademiqException {
        return dao.save(build(0, subjectId, assessment, obtained, max));
    }
    public void update(int id, int subjectId, String assessment, String obtained, String max) throws AcademiqException {
        Validators.requireSelection("marks record", id);
        dao.update(build(id, subjectId, assessment, obtained, max));
    }
    public void delete(int id) throws AcademiqException { dao.delete(Validators.requireSelection("marks record", id)); }
    public List<MarksRecord> list() throws DatabaseException { return dao.findAll(); }

    private MarksRecord build(int id, int subjectId, String assessment, String obtained, String max) throws AcademiqException {
        Validators.requireSelection("subject", subjectId);
        assessment = Validators.requireText("Assessment name", assessment, 100);
        double o = Validators.parseDouble("Obtained marks", obtained);
        double m = Validators.parseDouble("Maximum marks", max);
        if (m <= 0) throw new InvalidMarksException("Maximum marks must be greater than zero.");
        if (o < 0) throw new InvalidMarksException("Obtained marks cannot be negative.");
        if (o > m) throw new InvalidMarksException("Obtained marks (" + o + ") cannot exceed maximum marks (" + m + ").");
        return new MarksRecord(id, subjectId, assessment, o, m);
    }

    // ---- calculations ----
    public static double percentage(double obtained, double max) { return max <= 0 ? 0 : obtained * 100.0 / max; }

    public static double percentage(List<MarksRecord> records) {
        double o = records.stream().mapToDouble(MarksRecord::getObtained).sum();
        double m = records.stream().mapToDouble(MarksRecord::getMax).sum();
        return percentage(o, m);
    }
    public static double totalObtained(List<MarksRecord> r) { return r.stream().mapToDouble(MarksRecord::getObtained).sum(); }
    public static double totalMax(List<MarksRecord> r) { return r.stream().mapToDouble(MarksRecord::getMax).sum(); }

    /** Marks of the whole list grouped by subject name . */
    public static Map<String, List<MarksRecord>> bySubject(List<MarksRecord> all) {
        return all.stream().collect(Collectors.groupingBy(MarksRecord::getSubjectName, TreeMap::new, Collectors.toList()));
    }

    /** Grade for a percentage using the configured boundaries. */
    public static String grade(double percent) {
        Properties rules = AppConfig.withPrefix("grade.");
        String best = "N/A"; double bestMin = -1;
        for (String g : rules.stringPropertyNames()) {
            try {
                double min = Double.parseDouble(rules.getProperty(g).trim());
                if (percent >= min && min > bestMin) { best = g; bestMin = min; }
            } catch (NumberFormatException ignored) { }
        }
        if (rules.isEmpty()) return "N/A";
        return best.equals("N/A") ? "Below lowest grade" : best;
    }
}
