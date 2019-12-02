package com.rabobank.argos.service.domain.verification.rules;

import com.rabobank.argos.domain.layout.rule.Rule;
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.domain.link.Link;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@Builder
@Getter
@Slf4j
public class RuleVerificationContext<R extends Rule> {

    private final R rule;
    private final Link link;
    private final Set<Artifact> validatedArtifacts;

    public List<Artifact> getFilteredProducts() {
        return filterArtifacts(link.getProducts());
    }

    public List<Artifact> getFilteredMaterials() {
        return filterArtifacts(link.getMaterials());
    }

    private List<Artifact> filterArtifacts(List<Artifact> artifacts) {
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + rule.getPattern());
        return artifacts.stream().filter(artifact -> matcher.matches(Paths.get(artifact.getUri()))).collect(toList());
    }

    public void addValidatedArtifacts(List<Artifact> artifacts) {
        log.info("add {} validated artifacts", artifacts.size());
        validatedArtifacts.addAll(artifacts);
    }

    public boolean notContainsMaterials(List<Artifact> artifacts) {
        return artifacts.stream().noneMatch(artifact -> link.getMaterials().contains(artifact));
    }
}
