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

import com.rabobank.argos.domain.layout.LayoutSegment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.rabobank.argos.service.domain.verification.Verification.Priority.BUILDSTEPS_COMPLETED;

@Component
@Slf4j
public class BuildStepsCompletedVerification implements Verification {
    @Override
    public Priority getPriority() {
        return BUILDSTEPS_COMPLETED;
    }

    @Override
    public VerificationRunResult verify(VerificationContext context) {

        return context.layoutSegments()
                .stream().map(segment -> verifyForSegment(segment, context))
                .filter(verificationRunResult -> !verificationRunResult.isRunIsValid())
                .findFirst()
                .orElse(VerificationRunResult.okay());
    }

    private VerificationRunResult verifyForSegment(LayoutSegment segment, VerificationContext context) {
        List<String> expectedSteps = context.getExpectedStepNamesBySegmentName(segment.getName());
        List<String> actualStepNamesFromLinks = context.getStepNamesFromLinksBySegmentName(segment.getName());
        return VerificationRunResult.builder().runIsValid(
                actualStepNamesFromLinks.size() == expectedSteps.size() && expectedSteps.containsAll(actualStepNamesFromLinks))
                .build();
    }
}
