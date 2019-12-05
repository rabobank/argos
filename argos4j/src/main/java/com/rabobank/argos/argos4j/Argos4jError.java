package com.rabobank.argos.argos4j;

public class Argos4jError extends RuntimeException {


    public Argos4jError(String message) {
        super(message);
    }

    public Argos4jError(String message, Throwable e) {
        super(message, e);
    }
}
