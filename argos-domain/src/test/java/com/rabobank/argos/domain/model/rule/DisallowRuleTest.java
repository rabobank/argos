package com.rabobank.argos.domain.model.rule;

import com.rabobank.argos.domain.exceptions.RuleVerificationError;
import com.rabobank.argos.domain.model.Artifact;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DisallowRuleTest {
    public static final String PATHARTIFACTJAVA = "/path/artifact.java";
    private DisallowRule disAllowRule;

    private Set<Artifact> artifacts;

    @BeforeEach
    public void setup() {
        disAllowRule = DisallowRule
                .builder()
                .pattern(PATHARTIFACTJAVA)
                .build();
        artifacts = new HashSet<>();
        artifacts.add(Artifact.builder().hash("hash").uri(PATHARTIFACTJAVA).build());
    }

    @Test
    void verifyWithNonEmptyResultThrowsRuleVerificationError() {
        assertThrows(RuleVerificationError.class, () ->
                disAllowRule.verify(artifacts, null, null)
        );
    }

    @Test
    void verifyWithEmptyArtifactsReturnsEmpty() throws RuleVerificationError {
        artifacts = new HashSet<>();
        Set<Artifact> result = disAllowRule.verify(artifacts, null, null);
        assertThat(result.isEmpty(), is(true));
    }
}