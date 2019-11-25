package com.rabobank.argos.domain.model.rule;


import com.rabobank.argos.domain.exceptions.RuleVerificationError;
import com.rabobank.argos.domain.model.Artifact;
import lombok.Builder;

import java.util.Set;

public final class AllowRule extends Rule {
    @Builder
    public AllowRule(String pattern) {
        super(pattern);
    }

    @Override
    public Set<Artifact> verify(Set<Artifact> artifacts, Set<Artifact> materials, Set<Artifact> products) throws RuleVerificationError {
        return filterArtifacts(artifacts);
    }
}
