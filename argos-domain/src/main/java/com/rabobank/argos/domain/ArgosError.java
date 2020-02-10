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
package com.rabobank.argos.domain;

import lombok.Getter;

@Getter
public class ArgosError extends RuntimeException {

    public enum Level {
        WARNING, ERROR
    }

    private final Level level;

    public ArgosError(String message, Throwable e) {
        this(message, e, Level.ERROR);
    }

    public ArgosError(String message) {
        this(message, Level.ERROR);
    }

    public ArgosError(String message, Throwable e, Level level) {
        super(message, e);
        this.level = level;
    }

    public ArgosError(String message, Level level) {
        super(message);
        this.level = level;
    }
}
