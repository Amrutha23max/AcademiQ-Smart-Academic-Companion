package com.academiq.records.exception;

import com.academiq.common.ValidationException;

/** Custom exception: negative counts, or attended > conducted. */
public class InvalidAttendanceException extends ValidationException {
    public InvalidAttendanceException(String message) { super(message); }
}
