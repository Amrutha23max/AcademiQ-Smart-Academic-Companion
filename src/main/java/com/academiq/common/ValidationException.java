package com.academiq.common;

/** Thrown when user input is invalid (empty field, bad date, bad number...). */
public class ValidationException extends AcademiqException {
    public ValidationException(String message) { super(message); }
}
