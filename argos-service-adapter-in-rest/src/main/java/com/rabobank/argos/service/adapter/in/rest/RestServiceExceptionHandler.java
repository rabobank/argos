/**
 * Copyright (C) 2019 Rabobank Nederland
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rabobank.argos.service.adapter.in.rest;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestError;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Collectors;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RestServiceExceptionHandler {

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public ResponseEntity<RestError> handleConstraintViolationException(
            MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getAllErrors()
                .stream()
                .filter(FieldError.class::isInstance).map(error -> ((FieldError) error).getField() + ":" + error.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining(", "));

        return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(createMessage(message));
    }

    @ExceptionHandler(value = {JsonMappingException.class})
    public ResponseEntity<RestError> handleJsonMappingException() {
        return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(createMessage("invalid json"));
    }

    @ExceptionHandler(value = {ResponseStatusException.class})
    public ResponseEntity<RestError> handleResponseStatusException(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatus()).body(createMessage(ex.getReason()));
    }

    private RestError createMessage(String message) {
        return new RestError().message(message);
    }

}
