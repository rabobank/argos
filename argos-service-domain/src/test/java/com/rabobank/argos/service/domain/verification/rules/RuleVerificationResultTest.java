package com.rabobank.argos.service.domain.verification.rules;

import com.rabobank.argos.domain.link.Artifact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;

@ExtendWith(MockitoExtension.class)
class RuleVerificationResultTest {

    @Mock
    private Artifact artifact;

    @Test
    void okay() {
        RuleVerificationResult ruleVerificationResult = RuleVerificationResult.okay(Set.of(artifact));
        assertThat(ruleVerificationResult.isValid(), is(true));
        assertThat(ruleVerificationResult.getValidatedArtifacts(), contains(artifact));
    }

    @Test
    void notOkay() {
        RuleVerificationResult ruleVerificationResult = RuleVerificationResult.notOkay();
        assertThat(ruleVerificationResult.isValid(), is(false));
        assertThat(ruleVerificationResult.getValidatedArtifacts(), empty());
    }
}