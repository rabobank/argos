/*
 * Copyright (C) 2019 Rabobank Nederland
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rabobank.argos.service.domain.verification.rules;

import com.rabobank.argos.domain.layout.DestinationType;
import com.rabobank.argos.domain.layout.rule.MatchRule;
import com.rabobank.argos.domain.layout.rule.RuleType;
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.domain.link.Link;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import com.rabobank.argos.service.domain.verification.VerificationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class MatchRuleVerificationTest {

    private static final String DESTINATION_STEP_NAME = "destinationStepName";
    private static final String HASH = "hash";
    private MatchRuleVerification verification;

    @Mock
    private RuleVerificationContext<MatchRule> context;

    @Mock
    private Artifact sourceArtifact;

    @Mock
    private MatchRule matchRule;

    @Mock
    private VerificationContext verificationContext;

    @Mock
    private LinkMetaBlock destinationLinkMetaBlock;

    @Mock
    private Link destinationLink;

    @Mock
    private Artifact destinationArtifact;

    @BeforeEach
    void setUp() {
        verification = new MatchRuleVerification();
    }

    @Test
    void getRuleType() {
        assertThat(verification.getRuleType(), is(RuleType.MATCH));
    }

    @Test
    void verifyExpectedProductsDestinationProducts() {

        when(context.getProducts()).thenReturn(List.of(sourceArtifact));
        when(matchRule.getDestinationType()).thenReturn(DestinationType.PRODUCTS);
        when(destinationLink.getProducts()).thenReturn(List.of(destinationArtifact));
        when(destinationArtifact.getHash()).thenReturn(HASH);

        setupMocks();

        RuleVerificationResult ruleVerificationResult = verification.verifyExpectedProducts(context);
        assertThat(ruleVerificationResult.isValid(), is(true));
        assertThat(ruleVerificationResult.getValidatedArtifacts(), contains(sourceArtifact));
    }

    @Test
    void verifyExpectedProductsDestinationMaterials() {

        when(context.getProducts()).thenReturn(List.of(sourceArtifact));
        when(matchRule.getDestinationType()).thenReturn(DestinationType.MATERIALS);
        when(destinationLink.getMaterials()).thenReturn(List.of(destinationArtifact));
        when(destinationArtifact.getHash()).thenReturn(HASH);

        setupMocks();

        RuleVerificationResult ruleVerificationResult = verification.verifyExpectedProducts(context);
        assertThat(ruleVerificationResult.isValid(), is(true));
        assertThat(ruleVerificationResult.getValidatedArtifacts(), contains(sourceArtifact));
    }

    @Test
    void verifyExpectedMaterialsDestinationMaterials() {

        when(context.getMaterials()).thenReturn(List.of(sourceArtifact));
        when(matchRule.getDestinationType()).thenReturn(DestinationType.MATERIALS);
        when(destinationLink.getMaterials()).thenReturn(List.of(destinationArtifact));
        when(destinationArtifact.getHash()).thenReturn(HASH);

        setupMocks();

        RuleVerificationResult ruleVerificationResult = verification.verifyExpectedMaterials(context);
        assertThat(ruleVerificationResult.isValid(), is(true));
        assertThat(ruleVerificationResult.getValidatedArtifacts(), contains(sourceArtifact));
    }


    @Test
    void verifyExpectedMaterialsDestinationProducts() {

        when(context.getMaterials()).thenReturn(List.of(sourceArtifact));
        when(matchRule.getDestinationType()).thenReturn(DestinationType.PRODUCTS);
        when(destinationLink.getProducts()).thenReturn(List.of(destinationArtifact));
        when(destinationArtifact.getHash()).thenReturn(HASH);

        setupMocks();

        RuleVerificationResult ruleVerificationResult = verification.verifyExpectedMaterials(context);
        assertThat(ruleVerificationResult.isValid(), is(true));
        assertThat(ruleVerificationResult.getValidatedArtifacts(), contains(sourceArtifact));
    }

    @Test
    void verifyExpectedMaterialsNoDestinationLinks() {
        when(context.getRule()).thenReturn(matchRule);
        when(context.getMaterials()).thenReturn(List.of(sourceArtifact));
        when(matchRule.getDestinationStepName()).thenReturn(DESTINATION_STEP_NAME);

        when(context.getVerificationContext()).thenReturn(verificationContext);

        RuleVerificationResult ruleVerificationResult = verification.verifyExpectedMaterials(context);
        assertThat(ruleVerificationResult.isValid(), is(false));
        assertThat(ruleVerificationResult.getValidatedArtifacts(), empty());
    }

    @Test
    void verifyExpectedMaterialsUnknownDestinationType() {
        when(context.getRule()).thenReturn(matchRule);

        when(context.getMaterials()).thenReturn(List.of(sourceArtifact));
        when(matchRule.getDestinationStepName()).thenReturn(DESTINATION_STEP_NAME);
        when(verificationContext.getLinksByStepName(DESTINATION_STEP_NAME)).thenReturn(List.of(destinationLinkMetaBlock));

        when(context.getVerificationContext()).thenReturn(verificationContext);

        RuleVerificationResult ruleVerificationResult = verification.verifyExpectedMaterials(context);
        assertThat(ruleVerificationResult.isValid(), is(false));
        assertThat(ruleVerificationResult.getValidatedArtifacts(), empty());
    }

    @Test
    void verifyExpectedMaterialsDifferentHash() {
        when(context.getMaterials()).thenReturn(List.of(sourceArtifact));
        when(matchRule.getDestinationType()).thenReturn(DestinationType.PRODUCTS);
        when(destinationLink.getProducts()).thenReturn(List.of(destinationArtifact));

        setupMocks();
        when(destinationArtifact.getHash()).thenReturn("otherHash");

        RuleVerificationResult ruleVerificationResult = verification.verifyExpectedMaterials(context);
        assertThat(ruleVerificationResult.isValid(), is(false));
        assertThat(ruleVerificationResult.getValidatedArtifacts(), empty());
    }

    @Test
    void verifyExpectedMaterialsDestinationArtifactNotFound() {
        when(context.getMaterials()).thenReturn(List.of(sourceArtifact));
        when(matchRule.getDestinationType()).thenReturn(DestinationType.PRODUCTS);
        when(destinationLink.getProducts()).thenReturn(List.of(destinationArtifact));

        setupMocks();
        when(destinationArtifact.getUri()).thenReturn("dest/not-cool.jar");

        RuleVerificationResult ruleVerificationResult = verification.verifyExpectedMaterials(context);
        assertThat(ruleVerificationResult.isValid(), is(false));
        assertThat(ruleVerificationResult.getValidatedArtifacts(), empty());
    }

    private void setupMocks() {

        when(context.getRule()).thenReturn(matchRule);

        when(matchRule.getSourcePathPrefix()).thenReturn("src/");
        when(matchRule.getDestinationPathPrefix()).thenReturn("dest/");
        when(matchRule.getPattern()).thenReturn("cool.jar");

        when(matchRule.getDestinationStepName()).thenReturn(DESTINATION_STEP_NAME);

        when(context.getVerificationContext()).thenReturn(verificationContext);
        when(verificationContext.getLinksByStepName(DESTINATION_STEP_NAME)).thenReturn(List.of(destinationLinkMetaBlock));
        when(destinationLinkMetaBlock.getLink()).thenReturn(destinationLink);


        when(destinationArtifact.getUri()).thenReturn("dest/cool.jar");
        when(sourceArtifact.getUri()).thenReturn("src/cool.jar");
        when(sourceArtifact.getHash()).thenReturn(HASH);

    }


}
