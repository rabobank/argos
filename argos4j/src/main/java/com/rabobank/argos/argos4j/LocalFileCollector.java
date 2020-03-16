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

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.Optional;

@Getter
public class LocalFileCollector extends FileCollector {

    private final boolean followSymlinkDirs;

    /**
     * used to make all artifact uris relative from the base path
     */
    private final Path basePath;

    /**
     * is the file or directory path
     */
    private final Path path;

    @Builder
    public LocalFileCollector(@Nullable String excludePatterns, @Nullable Boolean normalizeLineEndings, @Nullable Boolean followSymlinkDirs, @Nullable Path basePath, @NonNull Path path) {
        super(excludePatterns, normalizeLineEndings);
        this.followSymlinkDirs = Optional.ofNullable(followSymlinkDirs).orElse(true);
        this.basePath = basePath;
        this.path = path;
    }
}
