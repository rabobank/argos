package com.rabobank.argos.service.domain.verification.rules;

import com.rabobank.argos.domain.layout.rule.Rule;
import com.rabobank.argos.domain.layout.rule.RuleType;
import com.rabobank.argos.domain.link.Artifact;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class CreateRuleVerificationTest {

    private CreateRuleVerification verification;

    @Mock
    private RuleVerificationContext<? extends Rule> context;

    @Mock
    private Artifact artifact;

    @BeforeEach
    void setUp() {
        verification = new CreateRuleVerification();
    }

    @Test
    void getRuleType() {
        assertThat(verification.getRuleType(), is(RuleType.CREATE));
    }

    @Test
    void verifyExpectedProducts() {
        when(context.getFilteredProducts()).thenReturn(Stream.of(artifact));
        when(context.containsSomeMaterials(List.of(artifact))).thenReturn(false);
        RuleVerificationResult ruleVerificationResult = verification.verifyExpectedProducts(context);
        assertThat(ruleVerificationResult.isValid(), is(true));
        assertThat(ruleVerificationResult.getValidatedArtifacts(), contains(artifact));
    }

    @Test
    void verifyExpectedProductsArtifactInMaterials() {
        when(context.getFilteredProducts()).thenReturn(Stream.of(artifact));
        when(context.containsSomeMaterials(List.of(artifact))).thenReturn(true);
        RuleVerificationResult ruleVerificationResult = verification.verifyExpectedProducts(context);
        assertThat(ruleVerificationResult.isValid(), is(false));
        assertThat(ruleVerificationResult.getValidatedArtifacts(), empty());
    }

    @Test
    void verifyExpectedProductsArtifactNotInProducts() {
        when(context.getFilteredProducts()).thenReturn(Stream.of());
        RuleVerificationResult ruleVerificationResult = verification.verifyExpectedProducts(context);
        assertThat(ruleVerificationResult.isValid(), is(false));
        assertThat(ruleVerificationResult.getValidatedArtifacts(), empty());
    }

    @Test
    void verifyExpectedMaterials() {
        RuleVerificationResult ruleVerificationResult = verification.verifyExpectedMaterials(context);
        assertThat(ruleVerificationResult.isValid(), is(false));
        assertThat(ruleVerificationResult.getValidatedArtifacts(), empty());
    }
}