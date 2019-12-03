package com.rabobank.argos.service.domain.verification.rules;

import com.rabobank.argos.domain.layout.rule.Rule;
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.domain.link.Link;
import com.rabobank.argos.service.domain.verification.VerificationContext;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

@Builder
@Getter
@Slf4j
public class RuleVerificationContext<R extends Rule> {

    private final VerificationContext verificationContext;
    private final R rule;
    private final Link link;

    public Stream<Artifact> getFilteredProducts() {
        return filterArtifacts(link.getProducts());
    }

    public Stream<Artifact> getFilteredMaterials() {
        return filterArtifacts(link.getMaterials());
    }

    private Stream<Artifact> filterArtifacts(List<Artifact> artifacts) {
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + rule.getPattern());
        return artifacts.stream().filter(artifact -> matcher.matches(Paths.get(artifact.getUri())));
    }

    public boolean notContainsMaterials(List<Artifact> artifacts) {
        return artifacts.stream().noneMatch(artifact -> link.getMaterials().contains(artifact));
    }
}
