package com.rabobank.argos.service.domain.verification.rules;

import com.rabobank.argos.domain.layout.Step;
import com.rabobank.argos.domain.layout.rule.Rule;
import com.rabobank.argos.domain.layout.rule.RuleType;
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.domain.link.Link;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import com.rabobank.argos.service.domain.verification.VerificationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static com.rabobank.argos.service.domain.verification.Verification.Priority.RULES;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RulesVerificationTest {

    private static final String STEP_NAME = "stepName";
    @Mock
    private RuleVerification ruleVerification;

    @Mock
    private RulesVerification verification;

    @Mock
    private VerificationContext verificationContext;

    @Mock
    private Step step;

    @Mock
    private LinkMetaBlock linkMetaBlock;

    @Mock
    private Rule expectedMaterialRule;

    @Mock
    private Rule expectedProductRule;

    @Mock
    private Link link;

    @Mock
    private RuleVerificationResult productRuleVerificationResult;

    @Mock
    private RuleVerificationResult materialRuleVerificationResult;

    @Mock
    private Artifact materialArtifact;

    @Mock
    private Artifact productArtifact;

    @Captor
    private ArgumentCaptor<RuleVerificationContext<?>> ruleVerificationContextArgumentCaptor;

    @BeforeEach
    void setUp() {
        verification = new RulesVerification(List.of(ruleVerification));
    }

    @Test
    void getPriority() {
        assertThat(verification.getPriority(), is(RULES));
    }

    @Test
    void verifyHappyFlow() {
        when(ruleVerification.verifyExpectedProducts(any(RuleVerificationContext.class))).thenReturn(productRuleVerificationResult);
        setupMocks();

        when(link.getMaterials()).thenReturn(List.of(materialArtifact));
        when(link.getProducts()).thenReturn(List.of(productArtifact));

        when(materialRuleVerificationResult.isValid()).thenReturn(true);
        when(productRuleVerificationResult.isValid()).thenReturn(true);

        when(materialRuleVerificationResult.getValidatedArtifacts()).thenReturn(Set.of(materialArtifact));
        when(productRuleVerificationResult.getValidatedArtifacts()).thenReturn(Set.of(productArtifact));

        assertThat(verification.verify(verificationContext).isRunIsValid(), is(true));

        verify(ruleVerification).verifyExpectedMaterials(ruleVerificationContextArgumentCaptor.capture());
        RuleVerificationContext<?> ruleVerificationContext = ruleVerificationContextArgumentCaptor.getValue();
        assertThat(ruleVerificationContext.getLink(), sameInstance(link));
        assertThat(ruleVerificationContext.getRule(), sameInstance(expectedMaterialRule));
        assertThat(ruleVerificationContext.getVerificationContext(), sameInstance(verificationContext));
    }

    @Test
    void verifyMaterialRuleFailed() {
        when(ruleVerification.verifyExpectedProducts(any(RuleVerificationContext.class))).thenReturn(productRuleVerificationResult);
        setupMocks();

        when(materialRuleVerificationResult.isValid()).thenReturn(false);
        when(productRuleVerificationResult.isValid()).thenReturn(true);

        when(materialRuleVerificationResult.getValidatedArtifacts()).thenReturn(Set.of(materialArtifact));
        when(productRuleVerificationResult.getValidatedArtifacts()).thenReturn(Set.of(productArtifact));

        assertThat(verification.verify(verificationContext).isRunIsValid(), is(false));
        verify(verificationContext).removeLinkMetaBlocks(List.of(linkMetaBlock));

    }

    @Test
    void verifyProductRuleFailed() {
        when(ruleVerification.verifyExpectedProducts(any(RuleVerificationContext.class))).thenReturn(productRuleVerificationResult);
        setupMocks();

        when(materialRuleVerificationResult.isValid()).thenReturn(true);
        when(productRuleVerificationResult.isValid()).thenReturn(false);

        when(materialRuleVerificationResult.getValidatedArtifacts()).thenReturn(Set.of(materialArtifact));
        when(productRuleVerificationResult.getValidatedArtifacts()).thenReturn(Set.of(productArtifact));

        assertThat(verification.verify(verificationContext).isRunIsValid(), is(false));
        verify(verificationContext).removeLinkMetaBlocks(List.of(linkMetaBlock));

    }

    @Test
    void verifyNotAllProductArtifactsChecked() {
        when(ruleVerification.verifyExpectedProducts(any(RuleVerificationContext.class))).thenReturn(productRuleVerificationResult);
        setupMocks();

        when(materialRuleVerificationResult.isValid()).thenReturn(true);
        when(productRuleVerificationResult.isValid()).thenReturn(true);

        when(link.getMaterials()).thenReturn(List.of(materialArtifact));
        when(link.getProducts()).thenReturn(List.of(productArtifact));

        when(materialRuleVerificationResult.getValidatedArtifacts()).thenReturn(Set.of(materialArtifact));
        when(productRuleVerificationResult.getValidatedArtifacts()).thenReturn(Set.of());

        assertThat(verification.verify(verificationContext).isRunIsValid(), is(false));
        verify(verificationContext).removeLinkMetaBlocks(List.of(linkMetaBlock));

    }

    @Test
    void verifyNotAllMaterialArtifactsChecked() {
        when(ruleVerification.verifyExpectedProducts(any(RuleVerificationContext.class))).thenReturn(productRuleVerificationResult);
        setupMocks();

        when(materialRuleVerificationResult.isValid()).thenReturn(true);
        when(productRuleVerificationResult.isValid()).thenReturn(true);

        when(link.getMaterials()).thenReturn(List.of(materialArtifact));
        when(link.getProducts()).thenReturn(List.of(productArtifact));

        when(materialRuleVerificationResult.getValidatedArtifacts()).thenReturn(Set.of());
        when(productRuleVerificationResult.getValidatedArtifacts()).thenReturn(Set.of(productArtifact));

        assertThat(verification.verify(verificationContext).isRunIsValid(), is(false));
        verify(verificationContext).removeLinkMetaBlocks(List.of(linkMetaBlock));

    }

    @Test
    void verifyNotImplementedRule() {
        setupMocks();

        when(expectedProductRule.getRuleType()).thenReturn(RuleType.DELETE);
        when(materialRuleVerificationResult.isValid()).thenReturn(true);


        when(materialRuleVerificationResult.getValidatedArtifacts()).thenReturn(Set.of(materialArtifact));

        assertThat(verification.verify(verificationContext).isRunIsValid(), is(false));
        verify(verificationContext).removeLinkMetaBlocks(List.of(linkMetaBlock));

    }

    private void setupMocks() {
        when(ruleVerification.getRuleType()).thenReturn(RuleType.ALLOW);
        verification.init();
        when(verificationContext.getExpectedStepNames()).thenReturn(List.of(STEP_NAME));
        when(verificationContext.getStepByStepName(STEP_NAME)).thenReturn(step);
        when(linkMetaBlock.getLink()).thenReturn(link);
        when(link.getStepName()).thenReturn(STEP_NAME);
        when(verificationContext.getLinksByStepName(STEP_NAME)).thenReturn(List.of(linkMetaBlock));

        when(step.getStepName()).thenReturn(STEP_NAME);
        when(expectedMaterialRule.getRuleType()).thenReturn(RuleType.ALLOW);
        when(step.getExpectedMaterials()).thenReturn(List.of(expectedMaterialRule));

        when(expectedProductRule.getRuleType()).thenReturn(RuleType.ALLOW);
        when(step.getExpectedProducts()).thenReturn(List.of(expectedProductRule));

        when(ruleVerification.verifyExpectedMaterials(any(RuleVerificationContext.class))).thenReturn(materialRuleVerificationResult);


    }
}
