package com.rabobank.argos.service.adapter.in.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j(topic = "ExceptionHandler")
@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class RestServiceDefaultExceptionHandler {

    @ExceptionHandler(value = {RuntimeException.class})
    public ResponseEntity<?> handleRuntimeException(RuntimeException e) {
        log.error(e.getMessage(),e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
