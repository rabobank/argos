/*
 * Copyright (C) 2020 Rabobank Nederland
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
import com.rabobank.argos.domain.layout.Step;
import com.rabobank.argos.domain.link.Link;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


class BuildStepsCompletedVerificationTest {

    private static final String STEP_1 = "step1";
    private BuildStepsCompletedVerification verification;

    @BeforeEach
    void setUp() {
        verification = new BuildStepsCompletedVerification();
    }

    @Test
    void getPriority() {
        assertThat(verification.getPriority(), is(Verification.Priority.BUILDSTEPS_COMPLETED));
    }

    @Test
    void verifyOkay() {
        VerificationContext context = VerificationContext.builder()
                .layoutMetaBlock(mockLayoutMetaBlock(STEP_1))
                .linkMetaBlocks(mockLinks(STEP_1, STEP_1)).build();
        VerificationRunResult result = verification.verify(context);
        assertThat(result.isRunIsValid(), is(true));
    }

    @Test
    void verifyNoLinks() {
        VerificationContext context = VerificationContext.builder()
                .layoutMetaBlock(mockLayoutMetaBlock(STEP_1))
                .linkMetaBlocks(mockLinks()).build();
        VerificationRunResult result = verification.verify(context);
        assertThat(result.isRunIsValid(), is(false));
    }

    @Test
    void verifyToMuchLinks() {
        VerificationContext context = VerificationContext.builder()
                .layoutMetaBlock(mockLayoutMetaBlock(STEP_1))
                .linkMetaBlocks(mockLinks(STEP_1, "unknown")).build();
        VerificationRunResult result = verification.verify(context);
        assertThat(result.isRunIsValid(), is(false));
    }

    @Test
    void verifyWrongLinks() {
        VerificationContext context = VerificationContext.builder()
                .layoutMetaBlock(mockLayoutMetaBlock(STEP_1))
                .linkMetaBlocks(mockLinks("unknown")).build();
        VerificationRunResult result = verification.verify(context);
        assertThat(result.isRunIsValid(), is(false));
    }

    private LayoutMetaBlock mockLayoutMetaBlock(String... stepName) {
        List<Step> steps = Stream.of(stepName).map(step -> Step.builder().stepName(step).build()).collect(toList());
        return LayoutMetaBlock.builder().layout(Layout.builder().steps(steps).build()).build();
    }

    private List<LinkMetaBlock> mockLinks(String... stepName) {
        return Stream.of(stepName).map(step -> LinkMetaBlock.builder().link(
                Link.builder().stepName(step).build()).build()).collect(toList());
    }
}
