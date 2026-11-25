package com.academiq.planner.ui;

import com.academiq.common.*;
import javafx.application.Platform;
import javafx.scene.control.TextArea;

/** ReminderChannel that writes a log line, updates the status bar, beeps and shows a dialog. Safe to call from any thread. */
public class PopupReminderChannel implements ReminderChannel {
    private final TextArea log;

    public PopupReminderChannel(TextArea log) {
        this.log = log;
    }

    @Override
    public void notify(String title, String message) {
        Platform.runLater(() -> {
            log.appendText("[" + java.time.LocalTime.now().withNano(0) + "] " + message.replace("\n", "\n           ") + "\n");
            ModuleHost.setStatus(title + ": " + message.split("\n")[0] + (message.contains("\n") ? " ..." : ""));
            try {
                java.awt.Toolkit.getDefaultToolkit().beep();
            } catch (Throwable ignored) {}
            Dialogs.info(title + "\n" + message);
        });
    }
}
