package com.rabobank.argos.domain.model.rule;

import com.rabobank.argos.domain.model.Artifact;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

class ModifyRuleTest {

    public static final String PATHARTIFACTJAVA = "/path/artifact.java";
    public static final String HASH = "hash";
    public static final String HASHMODIFIED = "hash-modified";
    private ModifyRule modifyRule;
    private Set<Artifact> artifacts;
    private Set<Artifact> products;
    private Set<Artifact> materials;

    @BeforeEach
    void setUp() {
        modifyRule = ModifyRule
                .builder()
                .pattern(PATHARTIFACTJAVA)
                .build();
        artifacts = new HashSet<>();
        artifacts.add(Artifact.builder().hash(HASH).uri(PATHARTIFACTJAVA).build());
        products = new HashSet<>();
        products.add(Artifact.builder().hash(HASHMODIFIED).uri(PATHARTIFACTJAVA).build());
        materials = new HashSet<>();
        materials.add(Artifact.builder().hash(HASH).uri(PATHARTIFACTJAVA).build());
    }

    @Test
    void verifywithModifiedArtifactsShouldReturnResult() {
        Set<Artifact> result = modifyRule.verify(artifacts, materials, products);
        assertThat(result, hasSize(1));
    }

    @Test
    void verifywithUnModifiedArtifactsShouldReturnEmptyResult() {
        products = new HashSet<>();
        products.add(Artifact.builder().hash(HASH).uri(PATHARTIFACTJAVA).build());
        Set<Artifact> result = modifyRule.verify(artifacts, materials, products);
        assertThat(result, hasSize(0));
    }
}