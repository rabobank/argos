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
package com.rabobank.argos.argos4j.internal;

import com.rabobank.argos.argos4j.Argos4jError;
import com.rabobank.argos.argos4j.FileCollector;
import com.rabobank.argos.domain.link.Artifact;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipStreamArtifactCollector {

    private FileCollector fileCollector;

    private final PathMatcher excludeMatcher;

    public ZipStreamArtifactCollector(FileCollector fileCollector) {
        this.fileCollector = fileCollector;
        this.excludeMatcher = FileSystems.getDefault().getPathMatcher("glob:" + this.fileCollector.getExcludePatterns());
    }

    public List<Artifact> collect(InputStream inputStream) {
        List<Artifact> artifacts = new ArrayList<>();
        try (BufferedInputStream bis = new BufferedInputStream(inputStream);
             ZipInputStream zis = new ZipInputStream(bis)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String fileName = entry.getName();
                if (!entry.isDirectory() && !excludeMatcher.matches(Paths.get(fileName))) {
                    artifacts.add(Artifact.builder()
                            .uri(fileName.replace("\\", "/"))
                            .hash(HashUtil.createHash(zis, fileName, fileCollector.isNormalizeLineEndings()))
                            .build());
                }
            }
        } catch (IOException e) {
            throw new Argos4jError(e.getMessage(), e);
        }
        return artifacts;
    }
}
