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

class DeleteRuleTest {

    public static final String PATHARTIFACTJAVA = "/path/artifact.java";
    private DeleteRule deleteRule;
    private Set<Artifact> artifacts;
    private Set<Artifact> products;
    private Set<Artifact> materials;

    @BeforeEach
    void setUp() {
        deleteRule = DeleteRule
                .builder()
                .pattern(PATHARTIFACTJAVA)
                .build();
        artifacts = new HashSet<>();
        artifacts.add(Artifact.builder().hash("hash").uri(PATHARTIFACTJAVA).build());
        materials = new HashSet<>();
        materials.add(Artifact.builder().hash("hash").uri(PATHARTIFACTJAVA).build());
    }

    @Test
    void verifyWithDeletedMaterialsWillReturnResult() {
        Set<Artifact> result = deleteRule.verify(artifacts, materials, products);
        assertThat(result, hasSize(1));
    }

    @Test
    void verifyWithNoDeletedMaterialsWillReturnEmptyResult() {
        products = new HashSet<>();
        products.add(Artifact.builder().hash("hash").uri(PATHARTIFACTJAVA).build());
        Set<Artifact> result = deleteRule.verify(artifacts, materials, products);
        assertThat(result, hasSize(0));
    }
}
