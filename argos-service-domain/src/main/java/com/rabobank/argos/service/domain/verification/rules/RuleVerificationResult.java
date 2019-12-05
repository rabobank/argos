package com.rabobank.argos.service.domain.verification.rules;


import com.rabobank.argos.domain.link.Artifact;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

import static java.util.Collections.emptySet;

@Builder
@Getter
public class RuleVerificationResult {

    private boolean valid;

    private final Set<Artifact> validatedArtifacts;

    public static RuleVerificationResult okay(Set<Artifact> validatedArtifacts) {
        return RuleVerificationResult.builder().valid(true).validatedArtifacts(validatedArtifacts).build();
    }

    public static RuleVerificationResult notOkay() {
        return RuleVerificationResult.builder().valid(false).validatedArtifacts(emptySet()).build();
    }
}
