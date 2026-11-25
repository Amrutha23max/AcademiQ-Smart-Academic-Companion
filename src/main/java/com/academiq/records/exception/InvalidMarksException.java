package com.academiq.records.exception;

import com.academiq.common.ValidationException;

/** Custom exception: obtained marks negative, maximum <= 0, or obtained > maximum. */
public class InvalidMarksException extends ValidationException {
    public InvalidMarksException(String message) { super(message); }
}
