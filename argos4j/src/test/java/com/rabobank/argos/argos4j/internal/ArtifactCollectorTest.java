package com.rabobank.argos.argos4j.internal;

import com.rabobank.argos.argos4j.Argos4jError;
import com.rabobank.argos.argos4j.Argos4jSettings;
import com.rabobank.argos.domain.model.Artifact;
import com.rabobank.argos.domain.model.HashAlgorithm;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArtifactCollectorTest {

    @TempDir
    static File sharedTempDir;

    private static File onFileDir;
    private static File multilevelDir;
    private static File ignoredFile;

    @BeforeAll
    static void setUp() throws IOException {
        onFileDir = new File(sharedTempDir, "on file dir");
        onFileDir.mkdir();
        FileUtils.write(new File(onFileDir, "text.txt"), "cool dit\r\nan other line", "UTF-8");
        ignoredFile = new File(onFileDir, "notMe.git");
        FileUtils.write(ignoredFile, "ignore", "UTF-8");

        multilevelDir = new File(sharedTempDir, "multilevel dir");
        multilevelDir.mkdir();
        FileUtils.write(new File(multilevelDir, "level1.txt"), "level 1 file\ran other line", "UTF-8");
        File level1 = new File(multilevelDir, "level1");
        level1.mkdir();
        FileUtils.write(new File(level1, "level2.txt"), "level 2 file\nan other line", "UTF-8");
        FileUtils.writeByteArrayToFile(new File(level1, "level2.zip"), createZip("i am in a zip file"));

        File level1Empty = new File(multilevelDir, "level1Empty");
        level1Empty.mkdir();
        FileUtils.write(new File(level1Empty, "not me.link"), "ignore file", "UTF-8");


        File level1Gone = new File(multilevelDir, "level1Gone");
        level1Gone.mkdir();

        Files.createSymbolicLink(new File(onFileDir, "linkdir").toPath(), level1.toPath());
        Files.createSymbolicLink(new File(onFileDir, "linkdirGone").toPath(), level1Gone.toPath());

        assertTrue(level1Gone.delete());

    }

    static private byte[] createZip(String content) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {


            ZipEntry entry = new ZipEntry("test.txt");
            entry.setTimeLocal(LocalDateTime.of(2019, 12, 31, 23, 34, 0, 0));
            zos.putNextEntry(entry);
            zos.write(content.getBytes());
            zos.closeEntry();
        }
        return baos.toByteArray();
    }

    @Test
    void collectOnFileWithBasePath() {
        List<Artifact> artifacts = sort(new ArtifactCollector(Argos4jSettings.builder().build(), onFileDir.getPath()).collect(""));
        assertThat(artifacts, hasSize(3));
        checkLevel2File(artifacts.get(0), "linkdir");
        checkLevel2Zip(artifacts.get(1), "linkdir");
        checkTextartifact(artifacts.get(2));
    }

    private void checkTextartifact(Artifact artifact) {
        assertThat(artifact.getUri(), is("text.txt"));
        assertThat(artifact.getHashAlgorithm(), is(HashAlgorithm.SHA256));
        assertThat(artifact.getHash(), is("616e953d8784d4e15a17055a91ac7539bca32350850ac5157efffdda6a719a7b"));
    }

    @Test
    void collectMultiLevelWithBasePath() {
        List<Artifact> artifacts = sort(new ArtifactCollector(Argos4jSettings.builder().normalizeLineEndings(true).build(), multilevelDir.getPath()).collect(""));
        assertThat(artifacts, hasSize(3));
        Artifact artifact1 = artifacts.get(0);
        assertThat(artifact1.getUri(), is("level1.txt"));
        assertThat(artifact1.getHashAlgorithm(), is(HashAlgorithm.SHA256));
        assertThat(artifact1.getHash(), is("6f67ddc1ecfc504571641ed25caff36ccf525edf75263c8d91b5e3de66410713"));

        checkLevel2File(artifacts.get(1), "level1");
        checkLevel2Zip(artifacts.get(2), "level1");
    }

    private void checkLevel2Zip(Artifact artifact, String baseDir) {
        assertThat(artifact.getUri(), is(baseDir + "/level2.zip"));
        assertThat(artifact.getHashAlgorithm(), is(HashAlgorithm.SHA256));
        assertThat(artifact.getHash(), is("06b2edb9c90eec831a8047275b77c57ca44a0e486ff259f513a72654011f0481"));
    }

    private void checkLevel2File(Artifact artifact, String baseDir) {
        assertThat(artifact.getUri(), is(baseDir + "/level2.txt"));
        assertThat(artifact.getHashAlgorithm(), is(HashAlgorithm.SHA256));
        assertThat(artifact.getHash(), is("c5721bee86deedfd45ad61431a7e43a184782fd9aaa1620d750de06a72984300"));
    }

    @Test
    void collectOnFileWithBasePathNotFollowLinks() {
        List<Artifact> artifacts = new ArtifactCollector(Argos4jSettings.builder().followSymlinkDirs(false).build(), onFileDir.getPath()).collect("");
        assertThat(artifacts, hasSize(1));
        checkTextartifact(artifacts.get(0));
    }

    private List<Artifact> sort(List<Artifact> artifacts) {
        artifacts.sort(Comparator.comparing(Artifact::getUri));
        return artifacts;
    }

    @Test
    void collectOnFileWithBasePathNotFollowLinksAndNormalizeLineEndings() {
        List<Artifact> artifacts = new ArtifactCollector(Argos4jSettings.builder().followSymlinkDirs(false).normalizeLineEndings(false).build(), onFileDir.getPath()).collect("");
        assertThat(artifacts, hasSize(1));
        Artifact artifact = artifacts.get(0);
        assertThat(artifact.getUri(), is("text.txt"));
        assertThat(artifact.getHashAlgorithm(), is(HashAlgorithm.SHA256));
        assertThat(artifact.getHash(), is("cb6bdad36690e8024e7df13e6796ae6603f2cb9cf9f989c9ff939b2ecebdcb91"));
    }

    @Test
    void collectOnFileWithExcludePattern() {
        List<Artifact> artifacts = new ArtifactCollector(Argos4jSettings.builder().excludePatterns("**.txt").followSymlinkDirs(false).build(), onFileDir.getPath()).collect("");
        assertThat(artifacts, hasSize(1));
        Artifact artifact = artifacts.get(0);
        assertThat(artifact.getUri(), endsWith("notMe.git"));
        assertThat(artifact.getHashAlgorithm(), is(HashAlgorithm.SHA256));
        assertThat(artifact.getHash(), is("5f0af516936c6ab13dfce52362f84a3c0aa8d87aca8f2bcaf55ad4e1e0178034"));
    }

    @Test
    void collectWrongBasePath() {
        Argos4jError error = assertThrows(Argos4jError.class, () -> new ArtifactCollector(Argos4jSettings.builder().build(), "notthere").collect(""));
        assertThat(error.getMessage(), is("Base path notthere doesn't exist"));
    }

    @Test
    void collectOnFileWithoutBasePathNotFollowLinks() {
        List<Artifact> artifacts = new ArtifactCollector(Argos4jSettings.builder().followSymlinkDirs(false).build(), null).collect(onFileDir.getPath());
        assertThat(artifacts, hasSize(1));
        Artifact artifact = artifacts.get(0);
        assertThat(artifact.getUri(), endsWith("on file dir/text.txt"));
        assertThat(artifact.getHashAlgorithm(), is(HashAlgorithm.SHA256));
        assertThat(artifact.getHash(), is("616e953d8784d4e15a17055a91ac7539bca32350850ac5157efffdda6a719a7b"));
    }

    @Test
    void collectOneFileThatIsInTheIgnoreFilter() {
        List<Artifact> artifacts = new ArtifactCollector(Argos4jSettings.builder().followSymlinkDirs(false).build(), null).collect(ignoredFile.getPath());
        assertThat(artifacts, hasSize(0));

    }


}