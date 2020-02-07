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
import com.rabobank.argos.service.domain.verification.ArtifactsVerificationContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class CreateRuleVerificationTest {

    private CreateRuleVerification verification;

    @Mock
    private RuleVerificationContext<? extends Rule> context;
    
    @Mock
    private ArtifactsVerificationContext artifactsContext;

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
    void verifyExpectedArtifacts() {
        when(context.getFilteredArtifacts()).thenReturn(Set.of(artifact));
        when(context.getMaterials()).thenReturn(Set.of());
        when(context.getProducts()).thenReturn(Set.of(artifact));
        assertThat(verification.verify(context), is(true));
        verify(context, times(1)).consume(Set.of(artifact));
    }

    @Test
    void verifyExpectedProductsArtifactInMaterials() {
        when(context.getFilteredArtifacts()).thenReturn(Set.of(artifact));
        when(context.getMaterials()).thenReturn(Set.of());
        when(context.getProducts()).thenReturn(Set.of());
        assertThat(verification.verify(context), is(false));
        verify(context, times(0)).consume(anySet());
    }

    @Test
    void verifyExpectedProductsArtifactNotInProducts() {
        when(context.getFilteredArtifacts()).thenReturn(Set.of());
        assertThat(verification.verify(context), is(true));
        verify(context, times(1)).consume(Set.of());
    }

    @Test
    void verifyExpectedMaterialsIsNotValid() {
        when(context.getFilteredArtifacts()).thenReturn(Set.of(artifact));
        when(context.getMaterials()).thenReturn(Set.of(artifact));
        when(context.getProducts()).thenReturn(Set.of(artifact));
        assertThat(verification.verify(context), is(false));
        verify(context, times(0)).consume(Set.of(artifact));
    }
}
