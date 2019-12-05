package com.rabobank.argos.service.domain.verification.rules;

/*-
 * #%L
 * Argos Supply Chain Notary
 * %%
 * Copyright (C) 2019 Rabobank Nederland
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
