package com.academiq.common;

/** Wraps SQLException so the UI never has to deal with JDBC types. */
public class DatabaseException extends AcademiqException {
    public DatabaseException(String message, Throwable cause) { super(message, cause); }
}
