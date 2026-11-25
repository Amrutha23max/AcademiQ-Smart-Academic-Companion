package com.academiq;

import java.util.List;

/** Lists every screen of the application in sidebar order. ModuleHost loads them by class name. */
public final class ModuleRegistry {
    private ModuleRegistry() {}

    public static final List<String> SCREENS = List.of(
        "com.academiq.planner.ui.DashboardPanel",
        "com.academiq.account.ui.SubjectPanel",
        "com.academiq.account.ui.AssignmentPanel",
        "com.academiq.records.ui.NotesPanel",
        "com.academiq.records.ui.AttendancePanel",
        "com.academiq.records.ui.MarksPanel",
        "com.academiq.records.ui.SummaryPanel",
        "com.academiq.planner.ui.ExamPanel",
        "com.academiq.planner.ui.StudyPlannerPanel",
        "com.academiq.planner.ui.ReportsPanel",
        "com.academiq.account.ui.ProfilePanel"
    );
}
