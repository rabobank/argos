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

import com.rabobank.argos.domain.layout.Layout;
import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.domain.layout.LayoutSegment;
import com.rabobank.argos.domain.layout.Step;
import com.rabobank.argos.domain.layout.rule.Rule;
import com.rabobank.argos.domain.layout.rule.RuleType;
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.domain.link.Link;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import com.rabobank.argos.service.domain.verification.RulesVerification;
import com.rabobank.argos.service.domain.verification.VerificationContext;
import com.rabobank.argos.service.domain.verification.rules.AllowRuleVerification;
import com.rabobank.argos.service.domain.verification.rules.DisallowRuleVerification;
import com.rabobank.argos.service.domain.verification.rules.RuleVerification;
import com.rabobank.argos.service.domain.verification.rules.RuleVerificationContext;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.rabobank.argos.service.domain.verification.Verification.Priority.RULES;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RulesVerificationTest {

    public static final String STEP_NAME = "stepName";
    public static final String SEGMENT_NAME = "segmentName";
    
    private RuleVerification allowRuleVerification = new AllowRuleVerification();
    
    private RuleVerification disAllowRuleVerification = new DisallowRuleVerification();

    @Mock
    private RulesVerification verification;

    private VerificationContext verificationContext;
    
    private Step step;

    @Mock
    private LinkMetaBlock linkMetaBlock;

    private List<LinkMetaBlock> linkMetaBlocks;

    @Mock
    private LayoutMetaBlock layoutMetaBlock;

    @Mock
    private Layout layout;
    @Mock
    private LayoutSegment layoutSegment;

    private Rule allowAllRule = new Rule(RuleType.ALLOW, "**");
    
    private Rule allowRuleWithNotFound = new Rule(RuleType.ALLOW, "not found");
    
    private Rule disAllowAllRule = new Rule(RuleType.DISALLOW, "**");
    
    private Rule deleteRule = new Rule(RuleType.DELETE, "**");

    private Artifact artifact1 = new Artifact("artifact1", "hash");

    private Artifact artifact2 = new Artifact("artifact2", "hash");

    @Captor
    private ArgumentCaptor<RuleVerificationContext<?>> ruleVerificationContextArgumentCaptor;
    
    @BeforeAll
    static void setUpBeforeClass() throws Exception {
        
        
    }

    @BeforeEach
    void setUp() {
        verification = new RulesVerification(List.of(allowRuleVerification, disAllowRuleVerification));
        verification.init();
        
    }

    @Test
    void getPriority() {
        assertThat(verification.getPriority(), is(RULES));
    }

    @Test
    void verifyAllowAllRuleWithMaterialAndProductRules() {
        step = Step.builder()
                .name(STEP_NAME)
                .expectedMaterials(List.of(allowAllRule))
                .expectedProducts(List.of(allowAllRule)).build();
        linkMetaBlocks = new ArrayList<>(List.of(LinkMetaBlock
                .builder().link(Link.builder()
                        .materials(List.of(artifact1))
                        .products(List.of(artifact2))
                        .layoutSegmentName(SEGMENT_NAME)
                        .stepName(STEP_NAME).build()).build()));
        setupMocks();
        assertThat(verification.verify(verificationContext).isRunIsValid(), is(true));
    }
    
    @Test
    void verifyAllowRuleWithNotConsumed() {
        step = Step.builder()
                .name(STEP_NAME)
                .expectedMaterials(List.of(allowRuleWithNotFound))
                .expectedProducts(List.of(allowRuleWithNotFound)).build();
        linkMetaBlocks = new ArrayList<>(List.of(LinkMetaBlock
                .builder().link(Link.builder()
                        .materials(List.of(artifact1))
                        .products(List.of(artifact2))
                        .layoutSegmentName(SEGMENT_NAME)
                        .stepName(STEP_NAME).build()).build()));
        setupMocks();
        assertThat(verification.verify(verificationContext).isRunIsValid(), is(false));
    }
    
    @Test
    void verifyListOfRules() {
        step = Step.builder()
                .name(STEP_NAME)
                .expectedMaterials(List.of(allowRuleWithNotFound, allowAllRule))
                .expectedProducts(List.of(allowRuleWithNotFound, allowAllRule)).build();
        linkMetaBlocks = new ArrayList<>(List.of(LinkMetaBlock.builder()
                .link(Link.builder()
                .materials(List.of(artifact1))
                .products(List.of(artifact2))
                .layoutSegmentName(SEGMENT_NAME)
                .stepName(STEP_NAME).build())
                .build()));
        setupMocks();
        assertThat(verification.verify(verificationContext).isRunIsValid(), is(true));
    }
    
    @Test
    void verifyRuleFails() {
        step = Step.builder()
                .name(STEP_NAME)
                .expectedMaterials(List.of(allowRuleWithNotFound, disAllowAllRule))
                .expectedProducts(List.of(allowRuleWithNotFound, disAllowAllRule)).build();
        linkMetaBlocks = new ArrayList<>(List.of(LinkMetaBlock
                .builder().link(Link.builder()
                        .materials(List.of(artifact1))
                        .products(List.of(artifact2))
                        .layoutSegmentName(SEGMENT_NAME)
                        .stepName(STEP_NAME).build()).build()));
        setupMocks();
        assertThat(verification.verify(verificationContext).isRunIsValid(), is(false));

    }

    @Test
    void verifyArtifactsNoRules() {
        step = Step.builder()
                .name(STEP_NAME)
                .expectedMaterials(List.of())
                .expectedProducts(List.of()).build();
        linkMetaBlocks = new ArrayList<>(List.of(LinkMetaBlock
                .builder().link(Link.builder()
                        .materials(List.of(artifact1))
                        .products(List.of(artifact2))
                        .layoutSegmentName(SEGMENT_NAME)
                        .stepName(STEP_NAME).build()).build()));
        setupMocks();
        assertThat(verification.verify(verificationContext).isRunIsValid(), is(false));

    }

    @Test
    void verifyNotImplementedRule() {
        step = Step.builder()
                .name(STEP_NAME)
                .expectedMaterials(List.of(deleteRule))
                .expectedProducts(List.of(deleteRule)).build();
        linkMetaBlocks = new ArrayList<>(List.of(LinkMetaBlock
                .builder().link(Link.builder()
                        .materials(List.of(artifact1))
                        .products(List.of(artifact2))
                        .layoutSegmentName(SEGMENT_NAME)
                        .stepName(STEP_NAME).build()).build()));
        setupMocks();
        assertThat(verification.verify(verificationContext).isRunIsValid(), is(false));

    }

    private void setupMocks() {
        when(layoutMetaBlock.getLayout()).thenReturn(layout);
        when(layout.getLayoutSegments()).thenReturn(Collections.singletonList(layoutSegment));
        when(layoutSegment.getSteps()).thenReturn(Collections.singletonList(step));
        when(layoutSegment.getName()).thenReturn(SEGMENT_NAME);
        verificationContext = VerificationContext
                .builder()
                .layoutMetaBlock(layoutMetaBlock)
                .linkMetaBlocks(linkMetaBlocks)
                .build();
    }
}
