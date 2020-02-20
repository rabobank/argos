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
package com.rabobank.argos.service.domain.verification;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.hamcrest.Matchers.empty;

import com.rabobank.argos.domain.ArgosError;
import com.rabobank.argos.domain.layout.ArtifactType;
import com.rabobank.argos.domain.layout.Layout;
import com.rabobank.argos.domain.layout.LayoutSegment;
import com.rabobank.argos.domain.layout.Step;
import com.rabobank.argos.domain.layout.rule.MatchRule;
import com.rabobank.argos.domain.layout.rule.Rule;
import com.rabobank.argos.domain.layout.rule.RuleType;
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.domain.link.Link;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import com.rabobank.argos.service.domain.verification.rules.AllowRuleVerification;
import com.rabobank.argos.service.domain.verification.rules.MatchRuleVerification;
import com.rabobank.argos.service.domain.verification.rules.RuleVerification;

@ExtendWith(MockitoExtension.class)
class VerificationContextsProviderContextTest {
    
    private String URI = "uri";
    
    private MatchRule matchRuleSegment1Segment2 = new MatchRule("file3", null, ArtifactType.MATERIALS, null, "segment2", null);
    private MatchRule matchRuleSegment1Segment3 = new MatchRule("file1", null, ArtifactType.PRODUCTS, null, "segment3", "step1");
    private MatchRule matchRuleSegment2Segment3 = new MatchRule("file2", null, ArtifactType.MATERIALS, null, "segment3", "step1");
    private MatchRule matchRuleSegment4Segment2 = new MatchRule(URI, null, ArtifactType.PRODUCTS, null, "segment2", null);
    private MatchRule matchRuleExpectedEndProduct = new MatchRule("file1", null, ArtifactType.PRODUCTS, null, "segment1", "step1");
    
    private Step segment1Step1 = Step.builder().name("step1").expectedMaterials(List.of(matchRuleSegment1Segment2))
            .expectedProducts(List.of(matchRuleSegment1Segment3)).build();
    private Step segment2Step1 = Step.builder().name("step1").expectedMaterials(List.of(matchRuleSegment2Segment3)).build();
    private Step segment3Step1 = Step.builder().name("step1").expectedMaterials(List.of()).build();
    private Step segment4Step1 = Step.builder().name("step1").expectedMaterials(List.of(matchRuleSegment4Segment2)).build();
    
    private LayoutSegment segment1 = LayoutSegment.builder().name("segment1").steps(List.of(segment1Step1)).build();
    private LayoutSegment segment2 = LayoutSegment.builder().name("segment2").steps(List.of(segment2Step1)).build();
    private LayoutSegment segment3 = LayoutSegment.builder().name("segment3").steps(List.of(segment3Step1)).build();
    private LayoutSegment segment4 = LayoutSegment.builder().name("segment4").steps(List.of(segment4Step1)).build();
    
    private Layout layout1 = Layout.builder().layoutSegments(List.of(segment1, segment2, segment3)).expectedEndProducts(List.of(matchRuleExpectedEndProduct)).build();
    
    private Map<LayoutSegment, Set<LayoutSegment>> graph;

    @BeforeEach
    void setUp() throws Exception {
        graph = new HashMap<>();
        graph.put(segment1, new HashSet<>());
        graph.put(segment2, new HashSet<>());
        graph.put(segment3, new HashSet<>());
        graph.get(segment2).add(segment1);
        graph.get(segment3).add(segment1);
        graph.get(segment3).add(segment2);
        
    }

    @Test
    void topologicalSort() {
        
        Queue<LayoutSegment> expectedList = new LinkedList<>();
        expectedList.add(segment1);
        expectedList.add(segment2);
        expectedList.add(segment3);
        
        Queue<LayoutSegment> sortedList = VerificationContextsProviderContext.topologicalSort(graph);
        assertEquals(expectedList, sortedList);
        
        Throwable exception = assertThrows(ArgosError.class, () -> {
            Map<LayoutSegment, Set<LayoutSegment>> graph2 = new HashMap<>();
            graph2.put(segment1, new HashSet<>());
            graph2.put(segment2, new HashSet<>());
            graph2.put(segment3, new HashSet<>());
            graph2.put(segment4, new HashSet<>());
            graph2.get(segment2).add(segment1);
            graph2.get(segment2).add(segment4);
            graph2.get(segment3).add(segment2);
            graph2.get(segment4).add(segment3);
            VerificationContextsProviderContext.topologicalSort(graph2);
          });
        assertEquals("layout segment graph has at least 1 cycle.", exception.getMessage());
        
    }
    
    @Test
    void createDirectedSegmentGraphTest() {

        Map<LayoutSegment, Set<LayoutSegment>> expectedGraph = new HashMap<>();
        expectedGraph.put(segment1, new HashSet<>());
        expectedGraph.put(segment2, new HashSet<>());
        expectedGraph.put(segment3, new HashSet<>());
        expectedGraph.get(segment2).add(segment1);
        expectedGraph.get(segment3).add(segment1);
        expectedGraph.get(segment3).add(segment2);
        
        Map<LayoutSegment, Set<LayoutSegment>> actualGraph = VerificationContextsProviderContext.createDirectedSegmentGraph(layout1);
        
        assertThat(actualGraph, is(expectedGraph));

        Layout layout2 = Layout.builder().layoutSegments(List.of(segment1, segment2, segment3, segment4)).build();
        
        Throwable exception = assertThrows(ArgosError.class, () -> {
            VerificationContextsProviderContext.createDirectedSegmentGraph(layout2);
          });
        assertEquals("layout segment graph has more than 1 start segment.", exception.getMessage());
    }
    
    @Test
    void initTest() {
        VerificationContextsProviderContext context = VerificationContextsProviderContext.builder().layout(layout1).build();
        context.init();
        Set<Set<LinkMetaBlock>> emptyBlocks = new HashSet<>();
        emptyBlocks.add(new HashSet<>());
        assertThat(context.getLinkMetaBlockSets(), is(emptyBlocks));
        assertThat(context.getResolvedSegments(), is(Set.of()));
        assertThat(context.getResolvedSegments(), is(Set.of()));

        assertThat(context.getSegmentGraph(), is(graph));
        
        Queue<LayoutSegment> segments = new LinkedList<>();
        segments.add(segment1);
        segments.add(segment2);
        segments.add(segment3);

        assertThat(context.getTopologicalSortedSegments(), is(segments));
        
    }
    
    @Test
    void getNextSegmentTest() {
        VerificationContextsProviderContext context = VerificationContextsProviderContext.builder().layout(layout1).build();
        context.init();
        assertThat(context.getNextSegment(), is(Optional.of(segment1)));
        assertThat(context.getNextSegment(), is(Optional.of(segment2)));
        assertThat(context.getNextSegment(), is(Optional.of(segment3)));
        assertThat(context.getNextSegment(), is(Optional.empty()));
    }
    
    @Test
    void getFirstMatchRulesAndArtifactsTest() {
        Artifact artifact1 = new Artifact("file1", "hash1");
        Artifact artifact2 = new Artifact("file2", "hash2");
        VerificationContextsProviderContext context = VerificationContextsProviderContext.builder()
                .productsToVerify(Set.of(artifact1)).layout(layout1).build();
        
        Map<String, Map<MatchRule, Set<Artifact>>> expectedStepMap = new HashMap<>();
        expectedStepMap.put("step1", new HashMap<>());
        expectedStepMap.get("step1").put(matchRuleExpectedEndProduct, Set.of(artifact1));
        
        Map<String, Map<MatchRule, Set<Artifact>>> actualStepMap = context.getFirstMatchRulesAndArtifacts();
        
        assertThat(actualStepMap, is(expectedStepMap));
        
        Throwable exception = assertThrows(ArgosError.class, () -> {
            VerificationContextsProviderContext context2 = VerificationContextsProviderContext.builder()
                .productsToVerify(Set.of(artifact1, artifact2)).layout(layout1).build();
            context2.getFirstMatchRulesAndArtifacts();
        });
        assertEquals("Not all products to verify are consumed. Not consumed: [[Artifact(uri=file2, hash=hash2)]]", exception.getMessage());        
        
    }
    
    @Test
    void getMatchRulesAndArtifactsTest() {
        
        Map<RuleType, RuleVerification> ruleMap = new HashMap<>();
        ruleMap.put(RuleType.ALLOW, new AllowRuleVerification());
        ruleMap.put(RuleType.MATCH, new MatchRuleVerification());
        
        VerificationContextsProviderContext context = VerificationContextsProviderContext.builder()
                .layout(layout1).rulesVerificationMap(ruleMap).build();
        context.init();
        Artifact artifact1 = new Artifact("file1", "hash1");
        Artifact artifact2 = new Artifact("file2", "hash2");
        Artifact artifact3 = new Artifact("file3", "hash3");
        Link linkSegment1 = Link.builder().layoutSegmentName("segment1").stepName("step1").materials(List.of(artifact3)).products(List.of(artifact1)).build();
        Link linkSegment2 = Link.builder().layoutSegmentName("segment2").stepName("step1").materials(List.of(artifact2)).build();
        LinkMetaBlock blockSegement1 = LinkMetaBlock.builder().link(linkSegment1).build();
        LinkMetaBlock blockSegement2 = LinkMetaBlock.builder().link(linkSegment2).build();
        
        Map<String, Map<MatchRule, Set<Artifact>>> actualStepMap = context.getMatchRulesAndArtifacts(segment3, Set.of(blockSegement1, blockSegement2));
        
        Map<String, Map<MatchRule, Set<Artifact>>> expectedStepMap = new HashMap<>();
        expectedStepMap.put("step1", new HashMap<>());
        expectedStepMap.get("step1").put(matchRuleSegment1Segment3, new HashSet<>(List.of(artifact1)));
        expectedStepMap.get("step1").put(matchRuleSegment2Segment3, new HashSet<>(List.of(artifact2)));
        
        assertThat(actualStepMap, is(expectedStepMap));
        
    }
    
    @Test
    void getMatchRulesAndArtifactsNoBlocksTest() {
        
        Map<RuleType, RuleVerification> ruleMap = new HashMap<>();
        ruleMap.put(RuleType.ALLOW, new AllowRuleVerification());
        ruleMap.put(RuleType.MATCH, new MatchRuleVerification());
        
        VerificationContextsProviderContext context = VerificationContextsProviderContext.builder()
                .layout(layout1).rulesVerificationMap(ruleMap).build();
        context.init();
        
        Map<String, Map<MatchRule, Set<Artifact>>> actualStepMap = context.getMatchRulesAndArtifacts(segment3, Set.of());
        
        assertThat(actualStepMap.keySet(), empty());
        
    }
    
    @Test
    void addStepsWithMatchRulesAndArtifactsToMapTest() {
        Artifact artifact1 = new Artifact("file1", "hash1");
        Artifact artifact2 = new Artifact("file2", "hash2");
        Artifact artifact3 = new Artifact("file3", "hash3");
        Artifact artifact4 = new Artifact("file4", "hash4");
        MatchRule matchRule1 = new MatchRule("file1", null, ArtifactType.MATERIALS, null, "segment2", "step1");
        Rule allowRule1 = new Rule(RuleType.ALLOW, "file2");
        MatchRule matchRule2 = new MatchRule("file3", null, ArtifactType.MATERIALS, null, "segment3", "step1");
        MatchRule matchRule3 = new MatchRule("file4", null, ArtifactType.MATERIALS, null, "segment2", "step1");
        
        Map<RuleType, RuleVerification> ruleMap = new HashMap<>();
        ruleMap.put(RuleType.ALLOW, new AllowRuleVerification());
        ruleMap.put(RuleType.MATCH, new MatchRuleVerification());
        
        VerificationContextsProviderContext context = VerificationContextsProviderContext.builder()
                .layout(layout1).rulesVerificationMap(ruleMap).build();
        Map<String, Map<MatchRule, Set<Artifact>>> expectedStepMap = new HashMap<>();
        expectedStepMap.put("step1", new HashMap<>());
        expectedStepMap.get("step1").put(matchRule1, new HashSet<>(List.of(artifact1)));
        expectedStepMap.get("step1").put(matchRule3, new HashSet<>(List.of(artifact4)));
        
        Map<String, Map<MatchRule, Set<Artifact>>> actualStepMap = new HashMap<>();
        Link link = Link.builder().layoutSegmentName(segment1.getName()).materials(List.of(artifact1, artifact2, artifact3, artifact4)).build();
        context.addStepsWithMatchRulesAndArtifactsToMap(
                actualStepMap,
                link,
                segment2,
                List.of(matchRule1, allowRule1, matchRule2, matchRule3),
                ArtifactType.MATERIALS);
        
        assertThat(actualStepMap, is(expectedStepMap));
    }
    
    @Test
    void permutateAndAddLinkMetaBlocksTest() {
        Artifact artifact111 = new Artifact("file111", "hash111");
        Artifact artifact112 = new Artifact("file112", "hash112");
        Artifact artifact121 = new Artifact("file121", "hash121");
        Artifact artifact122 = new Artifact("file122", "hash122");
        Artifact artifact211 = new Artifact("file211", "hash211");
        Artifact artifact212 = new Artifact("file212", "hash212");

        Link link111 = Link.builder().layoutSegmentName("segment1").stepName("step11").materials(List.of(artifact111)).build();
        Link link112 = Link.builder().layoutSegmentName("segment1").stepName("step11").materials(List.of(artifact112)).build();
        Link link121 = Link.builder().layoutSegmentName("segment1").stepName("step12").materials(List.of(artifact121)).build();
        Link link122 = Link.builder().layoutSegmentName("segment1").stepName("step12").materials(List.of(artifact122)).build();
        LinkMetaBlock block111 = LinkMetaBlock.builder().link(link111).build();
        LinkMetaBlock block112 = LinkMetaBlock.builder().link(link112).build();
        LinkMetaBlock block121 = LinkMetaBlock.builder().link(link121).build();
        LinkMetaBlock block122 = LinkMetaBlock.builder().link(link122).build();
        Set<Set<LinkMetaBlock>> beginSets = new HashSet<>();
        beginSets.add(Set.of(block111, block121));
        beginSets.add(Set.of(block111, block122));
        beginSets.add(Set.of(block112, block121));
        beginSets.add(Set.of(block112, block122));
        
        Set<Set<LinkMetaBlock>> startSets = new HashSet<>();
        startSets.add(new HashSet<>());

        Set<Set<LinkMetaBlock>> actualSets = VerificationContextsProviderContext.permutateAndAddLinkMetaBlocks(Set.of(block111, block112, block121, block122), startSets);
        
        assertThat(actualSets, is(beginSets));

        Link link211 = Link.builder().layoutSegmentName("segment2").stepName("step21").materials(List.of(artifact211)).build();
        Link link212 = Link.builder().layoutSegmentName("segment2").stepName("step21").materials(List.of(artifact212)).build();
        LinkMetaBlock block211 = LinkMetaBlock.builder().link(link211).build();
        LinkMetaBlock block212 = LinkMetaBlock.builder().link(link212).build();
        
        Set<Set<LinkMetaBlock>> expectedSets = new HashSet<>();
        expectedSets.add(Set.of(block111, block121, block211));
        expectedSets.add(Set.of(block112, block121, block211));
        expectedSets.add(Set.of(block111, block122, block211));
        expectedSets.add(Set.of(block112, block122, block211));
        expectedSets.add(Set.of(block111, block121, block212));
        expectedSets.add(Set.of(block112, block121, block212));
        expectedSets.add(Set.of(block111, block122, block212));
        expectedSets.add(Set.of(block112, block122, block212));

        actualSets = VerificationContextsProviderContext.permutateAndAddLinkMetaBlocks(Set.of(block211, block212), beginSets);
        
        assertThat(actualSets, is(expectedSets));
    }
    
    @Test
    void permutateOnStepsInSegmentTest() {
        Artifact artifact111 = new Artifact("file111", "hash111");
        Artifact artifact112 = new Artifact("file112", "hash112");
        Artifact artifact121 = new Artifact("file121", "hash121");
        Artifact artifact122 = new Artifact("file122", "hash122");

        Link link111 = Link.builder().layoutSegmentName("segment1").stepName("step11").materials(List.of(artifact111)).build();
        Link link112 = Link.builder().layoutSegmentName("segment1").stepName("step11").materials(List.of(artifact112)).build();
        Link link121 = Link.builder().layoutSegmentName("segment1").stepName("step12").materials(List.of(artifact121)).build();
        Link link122 = Link.builder().layoutSegmentName("segment1").stepName("step12").materials(List.of(artifact122)).build();
        LinkMetaBlock block111 = LinkMetaBlock.builder().link(link111).build();
        LinkMetaBlock block112 = LinkMetaBlock.builder().link(link112).build();
        LinkMetaBlock block121 = LinkMetaBlock.builder().link(link121).build();
        LinkMetaBlock block122 = LinkMetaBlock.builder().link(link122).build();
        Set<Set<LinkMetaBlock>> expectedSets = new HashSet<>();
        expectedSets.add(Set.of(block111));
        expectedSets.add(Set.of(block112));
        Set<Set<LinkMetaBlock>> actualSets = VerificationContextsProviderContext.permutateOnStepsInSegment(Set.of(block111, block112));
        
        assertThat(actualSets, is(expectedSets));
        
        expectedSets = new HashSet<>();
        expectedSets.add(Set.of(block111, block121));
        expectedSets.add(Set.of(block112, block121));
        expectedSets.add(Set.of(block111, block122));
        expectedSets.add(Set.of(block112, block122));
        actualSets = VerificationContextsProviderContext.permutateOnStepsInSegment(Set.of(block111, block112, block121, block122));
        
        assertThat(actualSets, is(expectedSets));
    }

}
