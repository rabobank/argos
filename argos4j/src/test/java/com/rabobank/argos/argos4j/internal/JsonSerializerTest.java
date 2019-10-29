package com.rabobank.argos.argos4j.internal;

import com.rabobank.argos.domain.model.Artifact;
import com.rabobank.argos.domain.model.ByProducts;
import com.rabobank.argos.domain.model.EnvironmentVariable;
import com.rabobank.argos.domain.model.Link;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static com.rabobank.argos.domain.model.HashAlgorithm.SHA256;
import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonSerializerTest {


    @Test
    void serialize() {

        String serialized = new JsonSigningSerializer().serialize(Link.builder()
                .materials(Arrays.asList(
                        Artifact.builder().uri("zbc.jar").hashAlgorithm(SHA256).hash("hash1").build(),
                        Artifact.builder().uri("abc.jar").hashAlgorithm(SHA256).hash("hash2").build()))
                .products(Arrays.asList(
                        Artifact.builder().uri("_bc.jar").hashAlgorithm(SHA256).hash("hash3").build(),
                        Artifact.builder().uri("_abc.jar").hashAlgorithm(SHA256).hash("hash4").build()))
                .command(Arrays.asList("z", "a"))
                .byProducts(ByProducts.builder()
                        .returnValue(23)
                        .stderr("stderr")
                        .stdout("stdout")
                        .build())
                .environment(Arrays.asList(EnvironmentVariable.builder().key("z").value("v").build(),
                        EnvironmentVariable.builder().key("a").value("d").build()))
                .build());
        assertEquals("{\"byProducts\":{\"returnValue\":23,\"stderr\":\"stderr\",\"stdout\":\"stdout\"},\"command\":[\"z\",\"a\"],\"environment\":[{\"key\":\"z\",\"value\":\"v\"},{\"key\":\"a\",\"value\":\"d\"}],\"materials\":[{\"hash\":\"hash2\",\"hashAlgorithm\":\"SHA256\",\"uri\":\"abc.jar\"},{\"hash\":\"hash1\",\"hashAlgorithm\":\"SHA256\",\"uri\":\"zbc.jar\"}],\"products\":[{\"hash\":\"hash4\",\"hashAlgorithm\":\"SHA256\",\"uri\":\"_abc.jar\"},{\"hash\":\"hash3\",\"hashAlgorithm\":\"SHA256\",\"uri\":\"_bc.jar\"}],\"stepName\":null}", serialized);
    }
}