/*
 * Copyright (C) 2019 - 2020 Rabobank Nederland
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

import com.rabobank.argos.domain.layout.rule.Rule;
import com.rabobank.argos.domain.layout.rule.RuleType;
import com.rabobank.argos.domain.link.Artifact;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteRuleVerificationTest {


    private DeleteRuleVerification deleteRuleVerification;
    @Mock
    private RuleVerificationContext<? extends Rule> context;

    @Mock
    private Artifact artifact;

    @BeforeEach
    void setup() {
        deleteRuleVerification = new DeleteRuleVerification();
    }

    @Test
    void getRuleType() {
        assertThat(deleteRuleVerification.getRuleType(), is(RuleType.DELETE));
    }

    @Test
    void verifyExpectedProducts() {
        when(context.getFilteredProducts()).thenReturn(Stream.empty());
        when(context.getFilteredMaterials()).thenReturn(Stream.of(artifact));
        RuleVerificationResult result = deleteRuleVerification.verifyExpectedProducts(context);
        assertThat(result.isValid(), is(true));
        assertThat(result.getValidatedArtifacts(), hasSize(0));
    }

    @Test
    void verifyExpectedMaterials() {
        when(context.getFilteredMaterials()).thenReturn(Stream.of(artifact));
        RuleVerificationResult result = deleteRuleVerification.verifyExpectedMaterials(context);
        assertThat(result.isValid(), is(true));
        assertThat(result.getValidatedArtifacts(), hasSize(0));

    }

    @Test
    void verifyExpectedProductsWithNonDeletedMaterialsShouldProduceInvalid() {
        when(context.getFilteredProducts()).thenReturn(Stream.of(artifact));
        when(context.getFilteredMaterials()).thenReturn(Stream.of(artifact));
        RuleVerificationResult result = deleteRuleVerification.verifyExpectedProducts(context);
        assertThat(result.isValid(), is(false));
        assertThat(result.getValidatedArtifacts(), hasSize(0));
    }

    @Test
    void verifyExpectedMaterialsWithEmptyMaterialsShouldProduceInvalid() {
        when(context.getFilteredMaterials()).thenReturn(Stream.empty());
        RuleVerificationResult result = deleteRuleVerification.verifyExpectedMaterials(context);
        assertThat(result.isValid(), is(false));
        assertThat(result.getValidatedArtifacts(), hasSize(0));
    }
}
