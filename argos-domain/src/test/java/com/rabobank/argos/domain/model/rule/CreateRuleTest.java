package com.rabobank.argos.domain.model.rule;

import com.rabobank.argos.domain.model.Artifact;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

class CreateRuleTest {

    private CreateRule createRule;
    private Set<Artifact> artifacts;
    private Set<Artifact> products;
    private Set<Artifact> materials;

    @BeforeEach
    void setUp() {
        createRule = CreateRule
                .builder()
                .pattern("/path/artifact.java")
                .build();
        artifacts = new HashSet<>();
        artifacts.add(Artifact.builder().hash("hash").uri("/path/artifact.java").build());
        products = new HashSet<>();
        products.add(Artifact.builder().hash("hash").uri("/path/artifact.java").build());
    }

    @Test
    void verify_With_Correct_Artifacts_Will_Return_Result() {
        Set<Artifact> result = createRule.verify(artifacts, materials, products);
        assertThat(result, hasSize(1));
    }

    @Test
    void verify_With_InCorrect_Artifacts_Will_Return_Empty_Result() {
        products = new HashSet<>();
        products.add(Artifact.builder().hash("hash").uri("/path/wrong.java").build());
        Set<Artifact> result = createRule.verify(artifacts, materials, products);
        assertThat(result, hasSize(0));
    }

    @Test
    void verify_With_Same_Materials_Will_Return_Empty_Result() {
        products = new HashSet<>();
        products.add(Artifact.builder().hash("hash").uri("/path/artifact.java").build());
        materials = new HashSet<>();
        materials.add(Artifact.builder().hash("hash").uri("/path/artifact.java").build());
        Set<Artifact> result = createRule.verify(artifacts, materials, products);
        assertThat(result, hasSize(0));
    }
}