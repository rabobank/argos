package com.rabobank.argos.domain.layout.rule;

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