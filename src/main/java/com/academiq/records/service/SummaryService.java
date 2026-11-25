package com.academiq.records.service;

import com.academiq.common.*;
import com.academiq.records.model.*;
import java.util.*;

/** Builds the subject-wise academic summary (attendance + marks together). */
public class SummaryService {
    public static class SubjectSummary implements Exportable {
        public final String subject; public final int conducted, attended; public final double attendancePct;
        public final double obtained, max, marksPct; public final String grade;
        SubjectSummary(String subject, int conducted, int attended, double obtained, double max) {
            this.subject = subject; this.conducted = conducted; this.attended = attended;
            this.attendancePct = AttendanceService.percentage(attended, conducted);
            this.obtained = obtained; this.max = max;
            this.marksPct = MarksCalculator.percentage(obtained, max);
            this.grade = max > 0 ? MarksCalculator.grade(marksPct) : "-";
        }
        @Override public String[] csvHeader() { return new String[]{"Subject", "Attendance %", "Marks obtained", "Marks max", "Marks %", "Grade"}; }
        @Override public String[] csvRow() {
            return new String[]{subject, String.format("%.1f", attendancePct), "" + obtained, "" + max, String.format("%.1f", marksPct), grade};
        }
    }

    public List<SubjectSummary> build() throws AcademiqException {
        Map<String, AttendanceRecord> att = new TreeMap<>();
        for (AttendanceRecord a : new AttendanceService().list()) att.put(a.getSubjectName(), a);
        Map<String, List<MarksRecord>> marks = MarksCalculator.bySubject(new MarksCalculator().list());
        Set<String> subjects = new TreeSet<>();
        for (Subject s : new SubjectDAO().findAll()) subjects.add(s.getName());
        List<SubjectSummary> out = new ArrayList<>();
        for (String name : subjects) {
            AttendanceRecord a = att.get(name);
            List<MarksRecord> m = marks.getOrDefault(name, Collections.emptyList());
            out.add(new SubjectSummary(name, a == null ? 0 : a.getConducted(), a == null ? 0 : a.getAttended(),
                    MarksCalculator.totalObtained(m), MarksCalculator.totalMax(m)));
        }
        return out;
    }
}
