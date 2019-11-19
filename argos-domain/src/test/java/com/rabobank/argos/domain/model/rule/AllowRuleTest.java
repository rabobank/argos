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


    public static final String PATHARTIFACTJAVA = "/path/artifact.java";
    public static final String HASH = "hash";
    private AllowRule allowRule;
    private Set<Artifact> artifacts;

    @BeforeEach
    void setUp() {
        allowRule = AllowRule
                .builder()
                .pattern(PATHARTIFACTJAVA)
                .build();
        artifacts = new HashSet<>();
        artifacts.add(Artifact.builder().hash(HASH).uri(PATHARTIFACTJAVA).build());
    }

    @Test
    void verifyWithCorrectPatternWillReturnResult() throws RuleVerificationError {
        assertThat(allowRule.verify(artifacts, null, null).size(), is(1));
    }
}