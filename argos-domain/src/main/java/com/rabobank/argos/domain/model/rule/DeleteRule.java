package com.rabobank.argos.domain.model.rule;

import com.rabobank.argos.domain.model.Artifact;
import lombok.Builder;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class DeleteRule extends Rule {
    @Builder
    public DeleteRule(String pattern) {
        super(pattern);
    }

    @Override
    public Set<Artifact> verify(Set<Artifact> artifacts, Set<Artifact> materials, Set<Artifact> products) {
        // Filter queued artifacts using the rule pattern
        Set<Artifact> filteredArtifacts = filterArtifacts(artifacts);
        // Consume filtered artifacts that are products but not materials
        // (materials - products)
        Set<Artifact> deletedArtifacts = new HashSet<>();
        deletedArtifacts.addAll(materials);
        if (products != null) {
            deletedArtifacts.removeAll(products);
        }
        return filteredArtifacts.stream().filter(deletedArtifacts::contains)
                .collect(Collectors.toSet());
    }
}
