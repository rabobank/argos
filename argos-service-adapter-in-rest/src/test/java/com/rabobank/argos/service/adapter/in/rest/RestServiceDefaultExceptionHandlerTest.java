package com.rabobank.argos.service.adapter.in.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

class RestServiceDefaultExceptionHandlerTest {

    private RestServiceDefaultExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new RestServiceDefaultExceptionHandler();
    }

    @Test
    void handleRuntimeException() {
        ResponseEntity<?> error = exceptionHandler.handleRuntimeException(new RuntimeException("error"));
        assertThat(error.getStatusCodeValue(), is(500));
    }
}