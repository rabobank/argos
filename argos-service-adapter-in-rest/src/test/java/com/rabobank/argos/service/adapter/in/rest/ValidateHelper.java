/*
 * Copyright (C) 2019 - 2020 Rabobank Nederland
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

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ValidateHelper {

    @Builder
    @EqualsAndHashCode
    @ToString
    public static class ValidationError {
        private final String path;
        private final String message;
    }

    public static <T> List<ValidationError> validate(T object) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        return validator.validate(object).stream()
                .sorted(Comparator.comparing((ConstraintViolation<T> cv) -> cv.getPropertyPath().toString())
                        .thenComparing(ConstraintViolation::getMessage))
                .map(cv -> ValidationError.builder().message(cv.getMessage()).path(cv.getPropertyPath().toString()).build())
                .collect(Collectors.toList());
    }

    public static ValidationError[] expectedErrors(String... errors) {
        List<ValidationError> validationErrors = new ArrayList<>();
        for (int i = 0; i < errors.length; i = i + 2) {
            validationErrors.add(ValidationError.builder().path(errors[i]).message(errors[i + 1]).build());
        }
        return validationErrors.toArray(new ValidationError[0]);
    }
}
