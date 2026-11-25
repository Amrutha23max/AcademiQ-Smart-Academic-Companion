package com.academiq.account.service;

import com.academiq.common.AcademiqException;

/** Custom exception: wrong email/password. */
public class AuthenticationException extends AcademiqException {
    public AuthenticationException(String message) { super(message); }
}
