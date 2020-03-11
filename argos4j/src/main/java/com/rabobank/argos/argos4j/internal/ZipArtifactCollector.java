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

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;

public class ZipArtifactCollector implements ArtifactCollector {


    private final ZipStreamArtifactCollector zipStreamArtifactCollector;
    private final URI uri;

    public ZipArtifactCollector(FileCollector fileCollector) {
        uri = fileCollector.getUri();
        zipStreamArtifactCollector = new ZipStreamArtifactCollector(fileCollector);
    }

    @Override
    public List<Artifact> collect() {
        try (FileInputStream fis = new FileInputStream(uri.getPath())) {
            return zipStreamArtifactCollector.collect(fis);
        } catch (IOException e) {
            throw new Argos4jError(e.getMessage(), e);
        }
    }
}
