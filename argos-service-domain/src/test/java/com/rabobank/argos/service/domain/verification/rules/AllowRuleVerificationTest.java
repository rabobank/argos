package com.rabobank.argos.service.domain.verification.rules;

import com.rabobank.argos.domain.layout.rule.Rule;
import com.rabobank.argos.domain.layout.rule.RuleType;
import com.rabobank.argos.domain.link.Artifact;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class AllowRuleVerificationTest {

    private AllowRuleVerification verification;

    @Mock
    private RuleVerificationContext<? extends Rule> context;

    @Mock
    private Artifact artifact;

    @BeforeEach
    void setUp() {
        verification = new AllowRuleVerification();
    }

    @Test
    void getRuleType() {
        assertThat(verification.getRuleType(), is(RuleType.ALLOW));
    }

    @Test
    void verifyExpectedProducts() {
        when(context.getFilteredProducts()).thenReturn(Stream.of(artifact));
        RuleVerificationResult ruleVerificationResult = verification.verifyExpectedProducts(context);
        assertThat(ruleVerificationResult.isValid(), is(true));
        assertThat(ruleVerificationResult.getValidatedArtifacts(), contains(artifact));
    }

    @Test
    void verifyExpectedMaterials() {
        when(context.getFilteredMaterials()).thenReturn(Stream.of(artifact));
        RuleVerificationResult ruleVerificationResult = verification.verifyExpectedMaterials(context);
        assertThat(ruleVerificationResult.isValid(), is(true));
        assertThat(ruleVerificationResult.getValidatedArtifacts(), contains(artifact));
    }
}