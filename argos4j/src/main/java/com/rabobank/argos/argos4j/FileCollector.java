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
package com.rabobank.argos.argos4j;

import lombok.Getter;

import javax.annotation.Nullable;
import java.util.Optional;


@Getter
public abstract class FileCollector {

    public static final String DEFAULT_EXCLUDE_PATTERNS = "**.{git,link}**";

    private final String excludePatterns;

    private final boolean normalizeLineEndings;

    public FileCollector(@Nullable String excludePatterns, @Nullable Boolean normalizeLineEndings) {
        this.excludePatterns = Optional.ofNullable(excludePatterns).orElse(DEFAULT_EXCLUDE_PATTERNS);
        this.normalizeLineEndings = Optional.ofNullable(normalizeLineEndings).orElse(false);
    }
}
