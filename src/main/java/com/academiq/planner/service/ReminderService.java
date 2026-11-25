package com.academiq.planner.service;

import com.academiq.common.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Background deadline checker. A daemon thread runs checkNow() every reminder.checkSeconds seconds.
 * It never touches UI nodes directly; the ReminderChannel is responsible for switching to the JavaFX thread.
 * checkNow() is synchronized because the timer and the "check now" button can both call it.
 */
public class ReminderService {
    private ScheduledExecutorService executor;
    private final ReminderChannel channel;
    private final Runnable afterCheck;

    public ReminderService(ReminderChannel channel, Runnable afterCheck) { this.channel = channel; this.afterCheck = afterCheck; }

    public synchronized void start() {
        if (executor != null && !executor.isShutdown()) return;
        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "academiq-reminders"); t.setDaemon(true); return t;
        });
        int seconds = Math.max(5, AppConfig.getInt("reminder.checkSeconds", 60));
        executor.scheduleWithFixedDelay(this::safeCheck, 2, seconds, TimeUnit.SECONDS);
    }

    /** Clean shutdown, called when the window closes or the user logs out. */
    public synchronized void stop() {
        if (executor == null) return;
        executor.shutdown();
        try { if (!executor.awaitTermination(2, TimeUnit.SECONDS)) executor.shutdownNow(); }
        catch (InterruptedException e) { executor.shutdownNow(); Thread.currentThread().interrupt(); }
        executor = null;
    }

    private void safeCheck() {
        try { checkNow(); }
        catch (Exception e) { System.err.println("Reminder check failed: " + e.getMessage()); }   // keep the timer alive
    }

    /** Finds pending assignments due within the window (or overdue) that have not been announced yet. Returns how many. */
    public synchronized int checkNow() throws DatabaseException {
        if (!Session.isLoggedIn()) return 0;
        int days = (int) Math.ceil(AppConfig.getInt("reminder.hoursBefore", 48) / 24.0);
        String sql = "SELECT a.id, a.title, s.name, a.due_date FROM assignments a JOIN subjects s ON s.id=a.subject_id " +
                     "WHERE a.user_id=? AND a.status='PENDING' AND a.due_date <= DATE_ADD(CURDATE(), INTERVAL ? DAY) " +
                     "AND a.id NOT IN (SELECT assignment_id FROM reminders) ORDER BY a.due_date";
        List<Integer> ids = new ArrayList<>();
        StringBuilder msg = new StringBuilder();
        try (Connection c = DatabaseConnection.get()) {
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, Session.userId()); ps.setInt(2, days);
                try (ResultSet r = ps.executeQuery()) {
                    while (r.next()) {
                        java.time.LocalDate due = r.getDate(4).toLocalDate();
                        long left = java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), due);
                        String when = left < 0 ? "OVERDUE by " + (-left) + " day(s)" : left == 0 ? "due TODAY" : "due in " + left + " day(s)";
                        msg.append(r.getString(2)).append(" (").append(r.getString(3)).append(") - ").append(when).append('\n');
                        ids.add(r.getInt(1));
                    }
                }
            }
            for (int id : ids) {
                try (PreparedStatement ps = c.prepareStatement("INSERT IGNORE INTO reminders(user_id, assignment_id) VALUES (?,?)")) {
                    ps.setInt(1, Session.userId()); ps.setInt(2, id); ps.executeUpdate();
                }
            }
        } catch (SQLException e) { throw new DatabaseException("Reminder check failed: " + e.getMessage(), e); }
        if (!ids.isEmpty()) channel.notify("Deadline reminder", msg.toString().trim());
        if (afterCheck != null) afterCheck.run();
        return ids.size();
    }

    /** Forget which reminders were shown, so they will be announced again (useful for demos). */
    public synchronized void resetAnnounced() throws DatabaseException {
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement("DELETE FROM reminders WHERE user_id=?")) {
            ps.setInt(1, Session.userId()); ps.executeUpdate();
        } catch (SQLException e) { throw new DatabaseException("Could not reset reminders: " + e.getMessage(), e); }
    }
}
