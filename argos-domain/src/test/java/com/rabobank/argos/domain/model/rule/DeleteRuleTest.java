package com.rabobank.argos.domain.model.rule;

import com.rabobank.argos.domain.model.Artifact;
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