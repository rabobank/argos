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

import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ModifyRuleVerificationTest {

    private static final String ARTIFACT_URI = "uri";
    private static final String PRODUCT_HASH = "productHash";
    private static final String MATERIAL_HASH = "materialHash";

    private ModifyRuleVerification verification;

    @Mock
    private RuleVerificationContext<? extends Rule> context;

    @Mock
    private Artifact productArtifact;

    @Mock
    private Artifact materialArtifact;

    @BeforeEach
    void setUp() {
        verification = new ModifyRuleVerification();
    }

    @Test
    void getRuleType() {
        assertThat(verification.getRuleType(), is(RuleType.MODIFY));
    }

    @Test
    void verifyExpectedProductsHappyFlow() {
        when(productArtifact.getUri()).thenReturn(ARTIFACT_URI);
        when(materialArtifact.getUri()).thenReturn(ARTIFACT_URI);

        when(productArtifact.getHash()).thenReturn(PRODUCT_HASH);
        when(materialArtifact.getHash()).thenReturn(MATERIAL_HASH);

        when(context.getFilteredProducts()).thenReturn(Stream.of(productArtifact));
        when(context.getFilteredMaterials()).thenReturn(Stream.of(materialArtifact));

        RuleVerificationResult ruleVerificationResult = verification.verifyExpectedProducts(context);

        assertThat(ruleVerificationResult.isValid(), is(true));
        assertThat(ruleVerificationResult.getValidatedArtifacts(), contains(productArtifact));
    }

    @Test
    void verifyExpectedProductsHashEquals() {
        when(productArtifact.getUri()).thenReturn(ARTIFACT_URI);
        when(materialArtifact.getUri()).thenReturn(ARTIFACT_URI);

        when(productArtifact.getHash()).thenReturn(PRODUCT_HASH);
        when(materialArtifact.getHash()).thenReturn(PRODUCT_HASH);

        when(context.getFilteredProducts()).thenReturn(Stream.of(productArtifact));
        when(context.getFilteredMaterials()).thenReturn(Stream.of(materialArtifact));

        RuleVerificationResult ruleVerificationResult = verification.verifyExpectedProducts(context);

        assertThat(ruleVerificationResult.isValid(), is(false));
        assertThat(ruleVerificationResult.getValidatedArtifacts(), empty());
    }

    @Test
    void verifyExpectedProductsNoProductMatch() {
        when(context.getFilteredProducts()).thenReturn(Stream.of());
        RuleVerificationResult ruleVerificationResult = verification.verifyExpectedProducts(context);

        assertThat(ruleVerificationResult.isValid(), is(false));
        assertThat(ruleVerificationResult.getValidatedArtifacts(), empty());
    }

    @Test
    void verifyExpectedProductsNoMaterialMatch() {
        when(productArtifact.getUri()).thenReturn(ARTIFACT_URI);

        when(context.getFilteredProducts()).thenReturn(Stream.of(productArtifact));
        when(context.getFilteredMaterials()).thenReturn(Stream.of());

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
