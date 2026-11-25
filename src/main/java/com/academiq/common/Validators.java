package com.academiq.common;

import java.time.*;
import java.time.format.DateTimeParseException;

/** Input validation shared by every screen. All methods throw ValidationException with a friendly message. */
public final class Validators {
    private Validators() {}

    public static String requireText(String field, String v, int maxLen) throws ValidationException {
        if (v == null || v.trim().isEmpty()) throw new ValidationException(field + " cannot be empty.");
        if (v.trim().length() > maxLen) throw new ValidationException(field + " is too long (max " + maxLen + " characters).");
        return v.trim();
    }
    public static String optionalText(String v, int maxLen) throws ValidationException {
        if (v == null) return "";
        if (v.trim().length() > maxLen) throw new ValidationException("Text is too long (max " + maxLen + " characters).");
        return v.trim();
    }
    public static LocalDate parseDate(String field, String v) throws ValidationException {
        try { return LocalDate.parse(v == null ? "" : v.trim()); }
        catch (DateTimeParseException e) { throw new ValidationException(field + " must be a date in yyyy-MM-dd format (e.g. 2026-12-31)."); }
    }
    public static LocalTime parseTime(String field, String v) throws ValidationException {
        try { return LocalTime.parse(v == null ? "" : v.trim()); }
        catch (DateTimeParseException e) { throw new ValidationException(field + " must be a time in HH:mm format (e.g. 09:30)."); }
    }
    public static int parseInt(String field, String v) throws ValidationException {
        try { return Integer.parseInt(v.trim()); }
        catch (Exception e) { throw new ValidationException(field + " must be a whole number."); }
    }
    public static double parseDouble(String field, String v) throws ValidationException {
        try { return Double.parseDouble(v.trim()); }
        catch (Exception e) { throw new ValidationException(field + " must be a number."); }
    }
    public static String requireEmail(String v) throws ValidationException {
        String e = requireText("Email", v, 100);
        if (!e.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
            throw new ValidationException("Please enter a valid email address.");
        return e.toLowerCase();
    }
    public static int requireSelection(String field, int id) throws ValidationException {
        if (id <= 0) throw new ValidationException("Please select a " + field + ".");
        return id;
    }
}
