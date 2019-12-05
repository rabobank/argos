package com.rabobank.argos.domain.layout.rule;

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

import com.rabobank.argos.domain.link.Artifact;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

class CreateRuleTest {

    public static final String PATHARTIFACTJAVA = "/path/artifact.java";
    public static final String HASH = "hash";
    private CreateRule createRule;
    private Set<Artifact> artifacts;
    private Set<Artifact> products;
    private Set<Artifact> materials;

    @BeforeEach
    void setUp() {
        createRule = CreateRule
                .builder()
                .pattern(PATHARTIFACTJAVA)
                .build();
        artifacts = new HashSet<>();
        artifacts.add(Artifact.builder().hash(HASH).uri(PATHARTIFACTJAVA).build());
        products = new HashSet<>();
        products.add(Artifact.builder().hash(HASH).uri(PATHARTIFACTJAVA).build());
    }

    @Test
    void verifyWithCorrectArtifactsWillReturnResult() {
        Set<Artifact> result = createRule.verify(artifacts, materials, products);
        assertThat(result, hasSize(1));
    }

    @Test
    void verifyWithInCorrectArtifactsWillReturnEmptyResult() {
        products = new HashSet<>();
        products.add(Artifact.builder().hash(HASH).uri("/path/wrong.java").build());
        Set<Artifact> result = createRule.verify(artifacts, materials, products);
        assertThat(result, hasSize(0));
    }

    @Test
    void verifyWithSameMaterialsWillReturnEmptyResult() {
        products = new HashSet<>();
        products.add(Artifact.builder().hash(HASH).uri(PATHARTIFACTJAVA).build());
        materials = new HashSet<>();
        materials.add(Artifact.builder().hash(HASH).uri(PATHARTIFACTJAVA).build());
        Set<Artifact> result = createRule.verify(artifacts, materials, products);
        assertThat(result, hasSize(0));
    }
}
