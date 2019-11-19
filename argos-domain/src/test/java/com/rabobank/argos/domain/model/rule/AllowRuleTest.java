package com.rabobank.argos.domain.model.rule;

import com.rabobank.argos.domain.exceptions.RuleVerificationError;
import com.rabobank.argos.domain.model.Artifact;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

class AllowRuleTest {


    private AllowRule allowRule;
    private Set<Artifact> artifacts;

    @BeforeEach
    void setUp() {
        allowRule = AllowRule
                .builder()
                .pattern("/path/artifact.java")
                .build();
        artifacts = new HashSet<>();
        artifacts.add(Artifact.builder().hash("hash").uri("/path/artifact.java").build());
    }

    @Test
    void verify_With_Correct_Pattern_Will_Return_Result() throws RuleVerificationError {
        assertThat(allowRule.verify(artifacts, null, null).size(), is(1));
    }
}