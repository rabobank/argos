package com.rabobank.argos.domain;

public class ArgosError extends RuntimeException {
    public ArgosError(String message, Throwable e) {
        super(message, e);
    }
}
