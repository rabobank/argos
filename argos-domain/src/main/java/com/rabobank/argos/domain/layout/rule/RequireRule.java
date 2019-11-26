package com.rabobank.argos.domain.layout.rule;


import com.rabobank.argos.domain.layout.exceptions.RuleVerificationError;
import com.rabobank.argos.domain.link.Artifact;
import lombok.Builder;

import java.util.HashSet;
import java.util.Set;

public final class RequireRule extends Rule {

    @Builder
    public RequireRule(String pattern) {
        super(pattern);
    }

    @Override
    public Set<Artifact> verify(Set<Artifact> artifacts, Set<Artifact> materials, Set<Artifact> products) throws RuleVerificationError {
        if (filterArtifacts(artifacts).isEmpty()) {
            throw new RuleVerificationError(this, null);
        }
        return new HashSet<>();
    }
}
