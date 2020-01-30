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
import com.rabobank.argos.domain.link.Link;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerificationContextTest {
    public static final String STEP_NAME = "stepName";
    public static final String SEGMENT_NAME = "segmentName";
    public static final Step STEP = Step.builder().stepName(STEP_NAME).build();
    private VerificationContext verificationContext;

    private List<LinkMetaBlock> linkMetaBlocks;

    @Mock
    private LayoutMetaBlock layoutMetaBlock;

    @Mock
    private Layout layout;

    @Mock
    private LayoutSegment layoutSegment;

    @BeforeEach
    void setup() {

        linkMetaBlocks = new ArrayList<>(List.of(LinkMetaBlock
                .builder().link(Link.builder()
                        .layoutSegmentName(SEGMENT_NAME)
                        .stepName(STEP_NAME).build()).build()));
        when(layoutMetaBlock.getLayout()).thenReturn(layout);
        when(layout.getLayoutSegments()).thenReturn(Collections.singletonList(layoutSegment));
        when(layoutSegment.getSteps()).thenReturn(Collections.singletonList(STEP));
        when(layoutSegment.getName()).thenReturn(SEGMENT_NAME);
        verificationContext = VerificationContext
                .builder()
                .layoutMetaBlock(layoutMetaBlock)
                .linkMetaBlocks(linkMetaBlocks)
                .build();
    }

    @Test
    void layoutMetaBlock() {
        assertThat(verificationContext.getLayoutMetaBlock(), sameInstance(layoutMetaBlock));
    }

    @Test
    void getStepByStepNameWithValidStepReturnsResult() {
        Step step = verificationContext.getStepBySegmentNameAndStepName(SEGMENT_NAME, STEP_NAME);
        assertThat(step.getStepName(), is(STEP_NAME));
    }

    @Test
    void getLinksByStepNameWithInValidStepReturnsException() {
        VerificationError error = assertThrows(VerificationError.class, () -> verificationContext.getStepBySegmentNameAndStepName(SEGMENT_NAME, "incorrect"));
        assertThat(error.getMessage(), Is.is("step with name: incorrect could not be found"));
    }


    @Test
    void getStepByStepName() {
        assertThat(verificationContext.getStepBySegmentNameAndStepName(SEGMENT_NAME, STEP_NAME), sameInstance(STEP));
    }

    @Test
    void removeLinkMetaBlocks() {
        verificationContext.removeLinkMetaBlocks(List.of(linkMetaBlocks.get(0)));
        assertThat(verificationContext.getLinkMetaBlocks(), empty());
    }
}
