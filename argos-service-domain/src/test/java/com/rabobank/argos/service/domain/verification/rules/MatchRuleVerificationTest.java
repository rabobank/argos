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
package com.rabobank.argos.service.domain.verification.rules;

import com.rabobank.argos.domain.layout.ArtifactType;
import com.rabobank.argos.domain.layout.rule.MatchRule;
import com.rabobank.argos.domain.layout.rule.RuleType;
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.domain.link.Link;
import com.rabobank.argos.service.domain.verification.ArtifactsVerificationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class MatchRuleVerificationTest {

    private static final String DESTINATION_STEP_NAME = "destinationStepName";
    private static final String DESTINATION_SEGMENT_NAME = "destinationSegmentName";
    private static final String SRC_SEGMENT_NAME = "sourceSegmentName";
    private static final String DESTINATION_PATH_PREFIX = "dest/dir";
    private static final String DESTINATION_PATH_PREFIX2 = "dest/dir2";
    private static final String SRC_PATH_PREFIX = "src";
    private static final String URI = "cool.jar";
    private static final String URI2 = "src/*";
    private static final String HASH = "hash";
    
    private MatchRuleVerification verification;

    @Mock
    private RuleVerificationContext<MatchRule> context;
    
    @Mock
    private ArtifactsVerificationContext artifactsContext;
    
    @Mock(answer=Answers.RETURNS_DEEP_STUBS)
    private Map<String, Map<String, Set<Link>>> linksMap;

    private Artifact sourceArtifactWithPrfx = new Artifact(SRC_PATH_PREFIX+"/"+URI, HASH);

    private Artifact destinationArtifactWithPrfx = new Artifact(DESTINATION_PATH_PREFIX+"/"+URI, HASH);

    private Artifact destinationArtifactWithPrfx2 = new Artifact(DESTINATION_PATH_PREFIX2+"/"+URI, HASH);

    private Artifact artifactWithoutPrfx = new Artifact(URI, HASH);
    
    private Artifact destinationOtherHash = new Artifact(URI, "other hash");

    @Mock
    private Link destinationLink;
    
    @Mock
    private Link sourceLink;

    @BeforeEach
    void setUp() {
        verification = new MatchRuleVerification(); 
    }

    @Test
    void getRuleType() {
        assertThat(verification.getRuleType(), is(RuleType.MATCH));
    }

    @Test
    void verifyMatchRuleDestMaterials() {
        MatchRule matchRule = new MatchRule(URI, null, ArtifactType.MATERIALS, null, null, DESTINATION_STEP_NAME);
        when(context.getRule()).thenReturn(matchRule);
        when(context.getFilteredArtifacts(null)).thenReturn(Set.of(artifactWithoutPrfx));
        when(context.getSegmentName()).thenReturn(SRC_SEGMENT_NAME);
        
        when(context.getLinkBySegmentNameAndStepName(SRC_SEGMENT_NAME, DESTINATION_STEP_NAME)).thenReturn(Optional.of(destinationLink));
        when(destinationLink.getMaterials()).thenReturn(List.of(artifactWithoutPrfx));
        assertThat(verification.verify(context), is(true));
        verify(context, times(1)).consume(Set.of(artifactWithoutPrfx));
        
    }
    
    @Test
    void verifyMatchRuleFails() {
        MatchRule matchRule = new MatchRule(URI2, null, ArtifactType.MATERIALS, null, null, DESTINATION_STEP_NAME);
        when(context.getRule()).thenReturn(matchRule);
        when(context.getSegmentName()).thenReturn(SRC_SEGMENT_NAME);
        
        when(context.getLinkBySegmentNameAndStepName(SRC_SEGMENT_NAME, DESTINATION_STEP_NAME)).thenReturn(Optional.of(destinationLink));
        when(destinationLink.getMaterials()).thenReturn(List.of(artifactWithoutPrfx));

        when(context.getFilteredArtifacts(null)).thenReturn(Set.of(sourceArtifactWithPrfx));
        matchRule.setDestinationType(ArtifactType.MATERIALS);
        assertThat(verification.verify(context), is(false));
        verify(context, times(0)).consume(anySet());
        
        when(context.getFilteredArtifacts(null)).thenReturn(Set.of(sourceArtifactWithPrfx));
        when(destinationLink.getProducts()).thenReturn(List.of(artifactWithoutPrfx));
        matchRule.setDestinationType(ArtifactType.PRODUCTS);
        assertThat(verification.verify(context), is(false));
        verify(context, times(0)).consume(anySet());
        
    }
    
    @Test
    void verifyMatchRuleDestProducts() {
        MatchRule matchRule = new MatchRule(URI, null, ArtifactType.PRODUCTS, null, null, DESTINATION_STEP_NAME);
        when(context.getRule()).thenReturn(matchRule);
        when(context.getFilteredArtifacts(null)).thenReturn(Set.of(artifactWithoutPrfx));
        when(context.getSegmentName()).thenReturn(SRC_SEGMENT_NAME);
        
        when(context.getLinkBySegmentNameAndStepName(SRC_SEGMENT_NAME, DESTINATION_STEP_NAME)).thenReturn(Optional.of(destinationLink));
        when(destinationLink.getProducts()).thenReturn(List.of(artifactWithoutPrfx));
        assertThat(verification.verify(context), is(true));
        verify(context, times(1)).consume(Set.of(artifactWithoutPrfx));
        
    }

    @Test
    void verifyMatchRuleOtherSegment() {
        MatchRule matchRule = new MatchRule(URI, null, ArtifactType.MATERIALS, null, DESTINATION_SEGMENT_NAME, DESTINATION_STEP_NAME);
        when(context.getRule()).thenReturn(matchRule);
        when(context.getFilteredArtifacts(null)).thenReturn(Set.of(artifactWithoutPrfx));
        when(context.getLinkBySegmentNameAndStepName(DESTINATION_SEGMENT_NAME, DESTINATION_STEP_NAME)).thenReturn(Optional.of(destinationLink));
        when(destinationLink.getMaterials()).thenReturn(List.of(artifactWithoutPrfx));
        
        when(context.getFilteredArtifacts(null)).thenReturn(Set.of(artifactWithoutPrfx));

        assertThat(verification.verify(context), is(true));
        verify(context, times(1)).consume(Set.of(artifactWithoutPrfx));
    }
    
    @Test
    void verifyMatchRuleOtherSegmentEmptyFilter() {
        MatchRule matchRule = new MatchRule(URI, null, ArtifactType.MATERIALS, null, DESTINATION_SEGMENT_NAME, DESTINATION_STEP_NAME);
        when(context.getRule()).thenReturn(matchRule);
        when(context.getLinkBySegmentNameAndStepName(DESTINATION_SEGMENT_NAME, DESTINATION_STEP_NAME)).thenReturn(Optional.of(destinationLink));
        when(destinationLink.getMaterials()).thenReturn(List.of(artifactWithoutPrfx));
        
        when(context.getFilteredArtifacts(null)).thenReturn(Set.of());

        assertThat(verification.verify(context), is(true));
        verify(context, times(1)).consume(Set.of());
    }
    
    @Test
    void verifyMatchRuleOtherSegmentWithSrcPrefix() {
        MatchRule matchRule = new MatchRule(URI, SRC_PATH_PREFIX, ArtifactType.MATERIALS, null, DESTINATION_SEGMENT_NAME, DESTINATION_STEP_NAME);
        when(context.getRule()).thenReturn(matchRule);
        when(context.getLinkBySegmentNameAndStepName(DESTINATION_SEGMENT_NAME, DESTINATION_STEP_NAME)).thenReturn(Optional.of(destinationLink));
        when(destinationLink.getMaterials()).thenReturn(List.of(artifactWithoutPrfx));
        
        when(context.getFilteredArtifacts(SRC_PATH_PREFIX)).thenReturn(Set.of(sourceArtifactWithPrfx));

        assertThat(verification.verify(context), is(true));
        verify(context, times(1)).consume(Set.of(sourceArtifactWithPrfx));
    }

    @Test
    void verifyMatchRuleOtherSegmentWithSrcPrefixInvalid() {
        MatchRule matchRule = new MatchRule(URI, SRC_PATH_PREFIX, ArtifactType.MATERIALS, null, DESTINATION_SEGMENT_NAME, DESTINATION_STEP_NAME);
        when(context.getRule()).thenReturn(matchRule);
        when(context.getLinkBySegmentNameAndStepName(DESTINATION_SEGMENT_NAME, DESTINATION_STEP_NAME)).thenReturn(Optional.of(destinationLink));
        
        when(context.getFilteredArtifacts(SRC_PATH_PREFIX)).thenReturn(Set.of(sourceArtifactWithPrfx));
        
        when(destinationLink.getMaterials()).thenReturn(List.of(sourceArtifactWithPrfx));

        assertThat(verification.verify(context), is(false));
        verify(context, times(0)).consume(anySet());
    }

    @Test
    void verifyMatchRuleOtherSegmentWithDestPrefix() {
        MatchRule matchRule = new MatchRule(URI, null, ArtifactType.MATERIALS, DESTINATION_PATH_PREFIX, DESTINATION_SEGMENT_NAME, DESTINATION_STEP_NAME);
        when(context.getRule()).thenReturn(matchRule);
        when(context.getLinkBySegmentNameAndStepName(DESTINATION_SEGMENT_NAME, DESTINATION_STEP_NAME)).thenReturn(Optional.of(destinationLink));
        when(destinationLink.getMaterials()).thenReturn(List.of(destinationArtifactWithPrfx));
        
        when(context.getFilteredArtifacts(null)).thenReturn(Set.of(artifactWithoutPrfx));

        assertThat(verification.verify(context), is(true));
        verify(context, times(1)).consume(Set.of(artifactWithoutPrfx));
    }
    
    @Test
    void verifyMatchRuleOtherSegmentWithDestPrefixInvalid() {
        MatchRule matchRule = new MatchRule(URI, null, ArtifactType.MATERIALS, DESTINATION_PATH_PREFIX, DESTINATION_SEGMENT_NAME, DESTINATION_STEP_NAME);
        when(context.getRule()).thenReturn(matchRule);
        when(context.getLinkBySegmentNameAndStepName(DESTINATION_SEGMENT_NAME, DESTINATION_STEP_NAME)).thenReturn(Optional.of(destinationLink));
        
        when(context.getFilteredArtifacts(null)).thenReturn(Set.of(artifactWithoutPrfx));
        
        when(destinationLink.getMaterials()).thenReturn(List.of(sourceArtifactWithPrfx));

        assertThat(verification.verify(context), is(false));
        verify(context, times(0)).consume(anySet());
    }

    @Test
    void verifyMatchRuleOtherSegmentWithSrcAndDestPrefix() {
        MatchRule matchRule = new MatchRule(URI, SRC_PATH_PREFIX, ArtifactType.MATERIALS, DESTINATION_PATH_PREFIX, DESTINATION_SEGMENT_NAME, DESTINATION_STEP_NAME);
        when(context.getRule()).thenReturn(matchRule);
        when(context.getLinkBySegmentNameAndStepName(DESTINATION_SEGMENT_NAME, DESTINATION_STEP_NAME)).thenReturn(Optional.of(destinationLink));
        when(destinationLink.getMaterials()).thenReturn(List.of(destinationArtifactWithPrfx));
        
        when(context.getFilteredArtifacts(SRC_PATH_PREFIX)).thenReturn(Set.of(sourceArtifactWithPrfx));

        assertThat(verification.verify(context), is(true));
        verify(context, times(1)).consume(Set.of(sourceArtifactWithPrfx));
        
    }
    
    @Test
    void verifyMatchRuleOtherSegmentWithSrcAndDestPrefixInvalid() {
        MatchRule matchRule = new MatchRule(URI, SRC_PATH_PREFIX, ArtifactType.MATERIALS, DESTINATION_PATH_PREFIX, DESTINATION_SEGMENT_NAME, DESTINATION_STEP_NAME);
        when(context.getRule()).thenReturn(matchRule);
        when(context.getLinkBySegmentNameAndStepName(DESTINATION_SEGMENT_NAME, DESTINATION_STEP_NAME)).thenReturn(Optional.of(destinationLink));
        
        when(context.getFilteredArtifacts(SRC_PATH_PREFIX)).thenReturn(Set.of(sourceArtifactWithPrfx));
        
        when(destinationLink.getMaterials()).thenReturn(List.of(destinationArtifactWithPrfx2));

        assertThat(verification.verify(context), is(false));
        verify(context, times(0)).consume(anySet());
    }

    @Test
    void verifyExpectedMaterialsDifferentHash() {

        MatchRule matchRule = new MatchRule(URI, null, ArtifactType.MATERIALS, null, null, DESTINATION_STEP_NAME);
        when(context.getRule()).thenReturn(matchRule);
        when(context.getFilteredArtifacts(null)).thenReturn(Set.of(artifactWithoutPrfx));
        when(context.getSegmentName()).thenReturn(SRC_SEGMENT_NAME);
        
        when(context.getLinkBySegmentNameAndStepName(SRC_SEGMENT_NAME, DESTINATION_STEP_NAME)).thenReturn(Optional.of(destinationLink));
        when(destinationLink.getMaterials()).thenReturn(List.of(destinationOtherHash));
        assertThat(verification.verify(context), is(false));
        verify(context, times(0)).consume(anySet());
    }

}
