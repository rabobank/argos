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

class RequireRuleTest {

    private RequireRule requireRule;

    private Set<Artifact> artifacts;

    @BeforeEach
    public void setup() {
        requireRule = RequireRule
                .builder()
                .pattern("/path/artifact.java")
                .build();
        artifacts = new HashSet<>();
        artifacts.add(Artifact.builder().hash("hash").uri("/path/artifact.java").build());
    }

    @Test
    void verify_With_Correct_Pattern_Will_Return_EmptySet() throws RuleVerificationError {
        Set<Artifact> result = requireRule.verify(artifacts, null, null);
        assertThat(result.isEmpty(), is(true));
    }

    @Test
    void verify_With_InCorrect_Pattern_Will_Throw_RuleVerificationError() {
        artifacts = new HashSet<>();
        artifacts.add(Artifact.builder().hash("hash").uri("/path/wrong.java").build());
        assertThrows(RuleVerificationError.class, () -> {
            requireRule.verify(artifacts, null, null);
        });

    }
}