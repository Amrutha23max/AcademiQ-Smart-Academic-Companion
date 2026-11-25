package com.academiq.common;

/** Base checked exception for all application errors. */
public class AcademiqException extends Exception {
    public AcademiqException(String message) { super(message); }
    public AcademiqException(String message, Throwable cause) { super(message, cause); }
}
