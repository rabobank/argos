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
import com.rabobank.argos.argos4j.LocalFileCollector;
import com.rabobank.argos.argos4j.LocalZipFileCollector;
import com.rabobank.argos.argos4j.RemoteFileCollector;
import com.rabobank.argos.argos4j.RemoteZipFileCollector;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ArtifactCollectorFactory {


    private static final Map<Class<? extends FileCollector>, Class<? extends ArtifactCollector>> MAPPING = new HashMap<>();

    static {
        MAPPING.put(LocalFileCollector.class, LocalArtifactCollector.class);
        MAPPING.put(LocalZipFileCollector.class, ZipArtifactCollector.class);
        MAPPING.put(RemoteFileCollector.class, RemoteArtifactCollector.class);
        MAPPING.put(RemoteZipFileCollector.class, RemoteArtifactCollector.class);
    }

    public static ArtifactCollector build(FileCollector fileCollector) {
        Class<? extends ArtifactCollector> artifactCollectorClass = Objects.requireNonNull(MAPPING.get(fileCollector.getClass()), "not implemented");
        try {
            return artifactCollectorClass.getConstructor(fileCollector.getClass()).newInstance(fileCollector);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            throw new Argos4jError(e.getMessage(), e);
        } catch (InvocationTargetException e) {
            throw new Argos4jError(e.getCause().getMessage(), e.getCause());
        }
    }
}
