package com.rabobank.argos.domain.signing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabobank.argos.domain.model.Artifact;
import com.rabobank.argos.domain.model.Layout;
import com.rabobank.argos.domain.model.Link;
import com.rabobank.argos.domain.model.Step;
import com.rabobank.argos.domain.model.rule.AllowRule;
import com.rabobank.argos.domain.model.rule.CreateRule;
import com.rabobank.argos.domain.model.rule.DeleteRule;
import com.rabobank.argos.domain.model.rule.DisallowRule;
import com.rabobank.argos.domain.model.rule.MatchRule;
import com.rabobank.argos.domain.model.rule.ModifyRule;
import com.rabobank.argos.domain.model.rule.RequireRule;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

class JsonSigningSerializerTest {


    @Test
    void serializeLink() throws IOException {
        String serialized = new JsonSigningSerializer().serialize(Link.builder()
                .materials(Arrays.asList(
                        Artifact.builder().uri("zbc.jar").hash("hash1").build(),
                        Artifact.builder().uri("abc.jar").hash("hash2").build()))
                .products(Arrays.asList(
                        Artifact.builder().uri("_bc.jar").hash("hash3").build(),
                        Artifact.builder().uri("_abc.jar").hash("hash4").build()))
                .command(Arrays.asList("z", "a"))
                .build());
        assertThat(serialized, is(getExpectedJson("/expectedLinkSigning.json")));
    }

    @Test
    void serializeLayout() throws IOException {
        String serialized = new JsonSigningSerializer().serialize(Layout.builder()
                .steps(Arrays.asList(
                        Step.builder()
                                .stepName("step b")
                                .requiredNumberOfLinks(1)
                                .expectedMaterials(Arrays.asList(
                                        AllowRule.builder().pattern("AllowRule").build(),
                                        RequireRule.builder().pattern("RequireRule").build()
                                ))
                                .expectedProducts(Arrays.asList(
                                        CreateRule.builder().pattern("CreateRule").build(),
                                        ModifyRule.builder().pattern("ModifyRule").build()
                                ))
                                .build(),
                        Step.builder()
                                .stepName("step a")
                                .authorizedKeyIds(Arrays.asList("step a key 2", "step a key 1"))
                                .requiredNumberOfLinks(23)
                                .expectedCommand(Arrays.asList("3", "2", "1"))
                                .expectedProducts(Arrays.asList(
                                        DisallowRule.builder().pattern("DisAllowRule").build(),
                                        MatchRule.builder().pattern("MatchRule")
                                                .destinationPathPrefix("destinationPathPrefix")
                                                .sourcePathPrefix("sourcePathPrefix")
                                                .destinationStepName("destinationStepName")
                                                .destinationType(MatchRule.DestinationType.MATERIALS)
                                                .build(),
                                        DeleteRule.builder().pattern("DeleteRule").build()
                                ))
                                .build()
                ))
                .authorizedKeyIds(Arrays.asList("key2", "key1"))
                .build()
        );
        assertThat(serialized, is(getExpectedJson("/expectedLayoutSigning.json")));
    }

    private String getExpectedJson(String name) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readValue(getClass().getResourceAsStream(name), JsonNode.class);
        return jsonNode.toString();
    }
}