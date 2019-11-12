package com.rabobank.argos.argos4j.internal;


import com.rabobank.argos.argos4j.Argos4jError;
import com.rabobank.argos.argos4j.Argos4jSettings;
import com.rabobank.argos.domain.model.Artifact;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.input.UnixLineEndingInputStream;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.util.encoders.Hex;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class ArtifactCollector {

    private final Argos4jSettings settings;
    private List<Artifact> artifacts = new ArrayList<>();
    private PathMatcher matcher;
    private final String basePath;


    public ArtifactCollector(Argos4jSettings settings, String basePath) {
        this.settings = settings;
        this.basePath = basePath;
        this.matcher = FileSystems.getDefault().getPathMatcher("glob:" + this.settings.getExcludePatterns());
        if (basePath != null && !Paths.get(basePath).toFile().exists()) {
            throw new Argos4jError("Base path " + basePath + " doesn't exist");
        }
    }

    public List<Artifact> collect(String... filePaths) {
        for (String path : filePaths) {
            recurseAndCollect(path);
        }
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
        SHA256Digest digest = new SHA256Digest();
        byte[] result = new byte[digest.getDigestSize()];
        try (InputStream file = settings.isNormalizeLineEndings() ?
                new UnixLineEndingInputStream(new FileInputStream(filename), false) :
                new FileInputStream(filename)) {
            int length;
            while ((length = file.read(result)) != -1) {
                digest.update(result, 0, length);
            }
            digest.doFinal(result, 0);
        } catch (IOException e) {
            throw new Argos4jError("The file " + filename + " couldn't be recorded: " + e.getMessage());
        }
        // We should be able to submit more hashes, but we will do sha256
        // only for the time being
        return Hex.toHexString(result);
    }

}