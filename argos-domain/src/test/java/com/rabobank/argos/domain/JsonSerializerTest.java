package com.rabobank.argos.domain;
import com.rabobank.argos.domain.model.Artifact;
import com.rabobank.argos.domain.model.Link;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

class JsonSerializerTest {


    @Test
    void serialize() {
        String serialized = new JsonSigningSerializer().serialize(Link.builder()
                .materials(Arrays.asList(
                        Artifact.builder().uri("zbc.jar").hash("hash1").build(),
                        Artifact.builder().uri("abc.jar").hash("hash2").build()))
                .products(Arrays.asList(
                        Artifact.builder().uri("_bc.jar").hash("hash3").build(),
                        Artifact.builder().uri("_abc.jar").hash("hash4").build()))
                .command(Arrays.asList("z", "a"))
                .build());
        assertThat(serialized, is("{\"command\":[\"z\",\"a\"],\"materials\":[{\"hash\":\"hash2\",\"uri\":\"abc.jar\"},{\"hash\":\"hash1\",\"uri\":\"zbc.jar\"}],\"products\":[{\"hash\":\"hash4\",\"uri\":\"_abc.jar\"},{\"hash\":\"hash3\",\"uri\":\"_bc.jar\"}],\"stepName\":null}"));
    }
}