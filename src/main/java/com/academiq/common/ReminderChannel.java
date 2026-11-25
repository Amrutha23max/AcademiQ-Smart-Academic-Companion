package com.academiq.common;

/** Where a reminder is delivered (popup, status bar, log...). */
public interface ReminderChannel {
    void notify(String title, String message);
}
