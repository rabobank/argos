/*
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rabobank.argos.domain.signing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabobank.argos.domain.layout.DestinationType;
import com.rabobank.argos.domain.layout.Layout;
import com.rabobank.argos.domain.layout.LayoutSegment;
import com.rabobank.argos.domain.layout.MatchFilter;
import com.rabobank.argos.domain.layout.Step;
import com.rabobank.argos.domain.layout.rule.MatchRule;
import com.rabobank.argos.domain.layout.rule.Rule;
import com.rabobank.argos.domain.layout.rule.RuleType;
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.domain.link.Link;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

class JsonSigningSerializerTest {


    @Test
    void serializeLink() throws IOException {
        String serialized = new JsonSigningSerializer().serialize(Link.builder()
                .stepName("stepName")
                .runId("runId")
                .layoutSegmentName("layoutSegmentName")
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
                .expectedEndProducts(singletonList(MatchFilter.builder()
                        .destinationSegmentName("destinationSegmentName")
                        .destinationType(DestinationType.PRODUCTS)
                        .destinationStepName("destinationStepName")
                        .pattern("MatchFiler").build()))
                .layoutSegments(singletonList(LayoutSegment.builder().name("segment 1")
                        .steps(Arrays.asList(
                                Step.builder()
                                        .stepName("step b")
                                        .requiredNumberOfLinks(1)
                                        .expectedMaterials(Arrays.asList(
                                                new Rule(RuleType.ALLOW, "AllowRule"),
                                                new Rule(RuleType.REQUIRE, "RequireRule")
                                        ))
                                        .expectedProducts(Arrays.asList(
                                                new Rule(RuleType.CREATE, "CreateRule"),
                                                new Rule(RuleType.MODIFY, "ModifyRule")
                                        ))
                                        .build(),
                                Step.builder()
                                        .stepName("step a")
                                        .authorizedKeyIds(Arrays.asList("step a key 2", "step a key 1"))
                                        .requiredNumberOfLinks(23)
                                        .expectedCommand(Arrays.asList("3", "2", "1"))
                                        .expectedProducts(Arrays.asList(
                                                new Rule(RuleType.DISALLOW, "DisAllowRule"),
                                                MatchRule.builder().pattern("MatchRule")
                                                        .destinationPathPrefix("destinationPathPrefix")
                                                        .sourcePathPrefix("sourcePathPrefix")
                                                        .destinationStepName("destinationStepName")
                                                        .destinationType(DestinationType.MATERIALS)
                                                        .build(),
                                                new Rule(RuleType.DELETE, "DeleteRule")
                                        ))
                                        .build()
                        )).build()))
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
