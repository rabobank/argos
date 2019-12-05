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

import com.rabobank.argos.service.adapter.in.rest.api.model.RestError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestServiceExceptionHandlerTest {

    private RestServiceExceptionHandler handler;

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private FieldError fieldError;

    @Mock
    private ResponseStatusException responseStatusException;

    @BeforeEach
    void setUp() {
        handler = new RestServiceExceptionHandler();
    }

    @Test
    void handleConstraintViolationException() {
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(Collections.singletonList(fieldError));
        when(fieldError.getField()).thenReturn("field");
        when(fieldError.getDefaultMessage()).thenReturn("message");
        ResponseEntity<RestError> response = handler.handleConstraintViolationException(methodArgumentNotValidException);
        assertThat(response.getStatusCodeValue(), is(400));
        assertThat(response.getBody().getMessage(), is("field:message"));
    }

    @Test
    void handleJsonMappingException() {
        ResponseEntity<RestError> response = handler.handleJsonMappingException();
        assertThat(response.getStatusCodeValue(), is(400));
        assertThat(response.getBody().getMessage(), is("invalid json"));
    }

    @Test
    void handleResponseStatusException() {
        when(responseStatusException.getStatus()).thenReturn(HttpStatus.NOT_FOUND);
        when(responseStatusException.getReason()).thenReturn("reason");
        ResponseEntity<RestError> response = handler.handleResponseStatusException(responseStatusException);
        assertThat(response.getStatusCodeValue(), is(404));
        assertThat(response.getBody().getMessage(), is("reason"));
    }
}
