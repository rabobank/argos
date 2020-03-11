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
import com.rabobank.argos.argos4j.FileCollectorSettings;
import com.rabobank.argos.domain.link.Artifact;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.input.UnixLineEndingInputStream;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.rabobank.argos.argos4j.FileCollector.FileCollectorType.LOCAL_DIRECTORY;
import static com.rabobank.argos.argos4j.FileCollector.FileCollectorType.LOCAL_FILE;

@Slf4j
public class LocalArtifactCollector implements ArtifactCollector {

    private final FileCollectorSettings settings;
    private final String basePath;
    private List<Artifact> artifacts = new ArrayList<>();
    private final PathMatcher matcher;
    private final FileCollector fileCollector;


    public LocalArtifactCollector(FileCollector fileCollector) {
        this.settings = fileCollector.getSettings();
        this.fileCollector = fileCollector;

        this.matcher = FileSystems.getDefault().getPathMatcher("glob:" + this.settings.getExcludePatterns());
        if (fileCollector.getType() == LOCAL_DIRECTORY) {
            basePath = fileCollector.getUri().getPath();
            if (basePath != null && !Paths.get(basePath).toFile().exists()) {
                throw new Argos4jError("Base path " + basePath + " doesn't exist");
            }
        } else if (fileCollector.getType() == LOCAL_FILE) {
            basePath = null;
        } else {
            throw new Argos4jError(fileCollector.getType() + " not supported");
        }
    }

    @Override
    public List<Artifact> collect() {
        recurseAndCollect(fileCollector.getType() == LOCAL_DIRECTORY ? "" : fileCollector.getUri().getPath());
        return artifacts;
    }

    private void recurseAndCollect(String file) {
        if (this.matcher.matches(Paths.get(file))) {
            return;
        }

        Path path = Optional.ofNullable(basePath).map(theBasePath -> Paths.get(theBasePath, file)).orElseGet(() -> Paths.get(file));

        if (!path.toFile().exists()) {
            log.warn("path: {} does not exist, skipping..", path);
        } else {
            if (Files.isRegularFile(path)) {
                // normalize path separator and create Artifact
                this.artifacts.add(Artifact.builder().uri(file.replace("\\", "/"))
                        .hash(createHash(path.toString())).build());
            } else {
                if ((Files.isSymbolicLink(path) && settings.isFollowSymlinkDirs())
                        || (path.toFile().isDirectory() && !Files.isSymbolicLink(path))) {
                    collectDirectory(path);
                }
            }
        }
    }

    private void collectDirectory(Path path) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path entry : stream) {
                if (this.matcher.matches(entry)) {
                    // exclude entry
                    continue;
                }
                // remove base path from path and the first char with "/"

                String relPath = Optional.ofNullable(basePath).map(theBasePath -> Paths.get(theBasePath).relativize(entry)).orElse(entry).toString();

                recurseAndCollect(relPath);
            }
        } catch (IOException e) {
            throw new Argos4jError(e.getMessage());
        }
    }

    private String createHash(String filename) {
        MessageDigest digest = DigestUtils.getSha256Digest();
        byte[] result = new byte[digest.getDigestLength()];
        try (InputStream file = settings.isNormalizeLineEndings() ?
                new UnixLineEndingInputStream(new FileInputStream(filename), false) :
                new FileInputStream(filename)) {
            int length;
            while ((length = file.read(result)) != -1) {
                digest.update(result, 0, length);
            }
        } catch (IOException e) {
            throw new Argos4jError("The file " + filename + " couldn't be recorded: " + e.getMessage());
        }
        return Hex.encodeHexString(digest.digest());
    }

}
