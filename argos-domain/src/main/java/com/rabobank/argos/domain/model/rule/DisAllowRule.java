package com.rabobank.argos.domain.model.rule;

import com.rabobank.argos.domain.exceptions.RuleVerificationError;
import com.rabobank.argos.domain.model.Artifact;
import lombok.Builder;

import java.util.Set;

public final class DisAllowRule extends Rule {
    @Builder
    public DisAllowRule(String pattern) {
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
