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
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.domain.link.Link;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class RuleVerificationContextTest {


    private RuleVerificationContext<Rule> verificationContext;

    @Mock
    private Link link;

    @Mock
    private Rule rule;

    @Mock
    private Artifact artifact1;

    @Mock
    private Artifact artifact2;

    @BeforeEach
    void setUp() {
        verificationContext = RuleVerificationContext.builder().link(link).rule(rule).build();
    }

    @Test
    void getFilteredProducts() {
        mockArtifacts();
        when(link.getProducts()).thenReturn(List.of(artifact1, artifact2));
        List<Artifact> artifacts = verificationContext.getFilteredProducts().collect(Collectors.toList());
        assertThat(artifacts, contains(artifact1));
    }

    @Test
    void getFilteredMaterials() {
        mockArtifacts();
        when(link.getMaterials()).thenReturn(List.of(artifact1, artifact2));
        List<Artifact> artifacts = verificationContext.getFilteredMaterials().collect(Collectors.toList());
        assertThat(artifacts, contains(artifact1));
    }

    @Test
    void containsSomeMaterials() {
        when(link.getMaterials()).thenReturn(List.of(artifact2));
        assertThat(verificationContext.containsSomeMaterials(List.of(artifact1)), is(false));
        assertThat(verificationContext.containsSomeMaterials(List.of(artifact2)), is(true));
        assertThat(verificationContext.containsSomeMaterials(List.of(artifact1, artifact2)), is(true));
    }

    private void mockArtifacts() {
        when(artifact1.getUri()).thenReturn("someDir/some.jar");
        when(artifact2.getUri()).thenReturn("someDir/some.html");
        when(rule.getPattern()).thenReturn("someDir/*.jar");
    }
}
