package com.rabobank.argos.domain.layout.rule;


import com.rabobank.argos.domain.link.Artifact;
import lombok.Builder;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class CreateRule extends Rule {
    @Builder
    public CreateRule(String pattern) {
        super(pattern);
    }

    @Override
    public Set<Artifact> verify(Set<Artifact> artifacts, Set<Artifact> materials, Set<Artifact> products) {
        Set<Artifact> createdArtifacts = new HashSet<>();
        createdArtifacts.addAll(products);
        if (materials != null) {
            createdArtifacts.removeAll(materials);
        }
        // Filter queued artifacts using the rule pattern
        Set<Artifact> filteredArtifacts = filterArtifacts(artifacts);
        // Consume filtered artifacts that are products but not materials
        // (products - materials)
        return filteredArtifacts.stream().filter(createdArtifacts::contains)
                .collect(Collectors.toSet());
    }

}
