package com.rabobank.argos.domain.layout.rule;

import com.rabobank.argos.domain.layout.exceptions.RuleVerificationError;
import com.rabobank.argos.domain.link.Artifact;
import lombok.Builder;

import java.util.Set;

public final class DisallowRule extends Rule {
    @Builder
    public DisallowRule(String pattern) {
        super(pattern);
    }

    @Override
    public Set<Artifact> verify(Set<Artifact> artifacts, Set<Artifact> materials, Set<Artifact> products) throws RuleVerificationError {
        Set<Artifact> filteredArtifacts = filterArtifacts(artifacts);
        if (!filteredArtifacts.isEmpty()) {
            throw new RuleVerificationError(this, filteredArtifacts);
        }
        return filteredArtifacts;
    }
}
