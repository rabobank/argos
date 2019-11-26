package com.rabobank.argos.service.domain.verification;

public class VerificationError extends RuntimeException {
    public VerificationError(String message) {
        super(message);
    }
}
