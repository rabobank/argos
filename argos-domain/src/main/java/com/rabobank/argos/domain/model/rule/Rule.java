package com.rabobank.argos.domain.model.rule;

import com.rabobank.argos.domain.exceptions.RuleVerificationError;
import com.rabobank.argos.domain.model.Artifact;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public abstract class Rule {

    private String pattern;

    public abstract Set<Artifact> verify(Set<Artifact> artifacts, Set<Artifact> materials, Set<Artifact> products) throws RuleVerificationError;

    protected Set<Artifact> filterArtifacts(Set<Artifact> artifacts) {
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + this.getPattern());
        Set<Artifact> filteredArtifacts = new HashSet<>();
        Iterator<Artifact> artifactIterator = artifacts.iterator();
        while (artifactIterator.hasNext()) {
            Artifact artifact = artifactIterator.next();
            if (matcher.matches(Paths.get(artifact.getUri()))) {
                filteredArtifacts.add(artifact);
            }
        }
        return filteredArtifacts;
    }
}
