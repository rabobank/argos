package com.rabobank.argos.domain.model.rule;


import com.rabobank.argos.domain.model.Artifact;
import lombok.Builder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public final class ModifyRule extends Rule {
    @Builder
    public ModifyRule(String pattern) {
        super(pattern);
    }

    @Override
    public Set<Artifact> verify(Set<Artifact> artifacts, Set<Artifact> materials, Set<Artifact> products) {
        Set<Artifact> consumed = new HashSet<>();
        // Filter queued artifacts using the rule pattern
        Set<Artifact> filteredArtifacts = filterArtifacts(artifacts);
        Map<String, Artifact> productsMap = this.createMap(products);
        Map<String, Artifact> materialsMap = this.createMap(materials);
        for (Artifact artifact : filteredArtifacts) {
            // Consume filtered artifacts that have different hashes
            if (productsMap.containsKey(artifact.getUri())
                    && materialsMap.containsKey(artifact.getUri())
                    && (!productsMap.get(artifact.getUri()).getHash().equals(materialsMap.get(artifact.getUri()).getHash()))) {
                consumed.add(artifact);
            }
        }
        return consumed;
    }

    private Map<String, Artifact> createMap(Set<Artifact> artifacts) {
        Map<String, Artifact> result = new HashMap<>();
        if (artifacts != null) {
            for (Artifact artifact : artifacts) {
                result.put(artifact.getUri(), artifact);
            }
        }
        return result;
    }

}
