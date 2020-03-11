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

import java.util.Objects;

public class ArtifactCollectorFactory {

    public static ArtifactCollector build(FileCollector fileCollector) {
        Objects.requireNonNull(fileCollector.getType());
        Objects.requireNonNull(fileCollector.getUri());
        Objects.requireNonNull(fileCollector.getSettings());
        switch (fileCollector.getType()) {
            case LOCAL_FILE:
            case LOCAL_DIRECTORY:
                return new LocalArtifactCollector(fileCollector);
            case LOCAL_ZIP:
                return new ZipArtifactCollector(fileCollector);
            case REMOTE_ZIP:
            case REMOTE_FILE:
                return new RemoteArtifactCollector(fileCollector);
            default:
                throw new Argos4jError(fileCollector.getType() + " not implemented");

        }
    }
}
