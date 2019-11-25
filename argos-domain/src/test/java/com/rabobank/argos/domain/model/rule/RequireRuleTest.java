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

    public static final String PATHARTIFACTJAVA = "/path/artifact.java";
    private RequireRule requireRule;

    private Set<Artifact> artifacts;

    @BeforeEach
    public void setup() {
        requireRule = RequireRule
                .builder()
                .pattern(PATHARTIFACTJAVA)
                
                .build();
        artifacts = new HashSet<>();
        artifacts.add(Artifact.builder().hash("hash").uri(PATHARTIFACTJAVA).build());
    }

    @Test
    void verifyWithCorrectPatternWillReturnEmptySet() throws RuleVerificationError {
        Set<Artifact> result = requireRule.verify(artifacts, null, null);
        assertThat(result.isEmpty(), is(true));
    }

    @Test
    void verifyWithInCorrectPatternWillThrowRuleVerificationError() {
        artifacts = new HashSet<>();
        artifacts.add(Artifact.builder().hash("hash").uri("/path/wrong.java").build());
        assertThrows(RuleVerificationError.class, () ->
                requireRule.verify(artifacts, null, null)
        );

    }
}