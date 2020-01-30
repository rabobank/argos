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
import com.rabobank.argos.domain.layout.Step;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.rabobank.argos.service.domain.verification.Verification.Priority.REQUIRED_NUMBER_OF_LINKS;
import static java.util.stream.Collectors.groupingBy;

@Component
@Slf4j
public class RequiredNumberOfLinksVerification implements Verification {
    @Override
    public Priority getPriority() {
        return REQUIRED_NUMBER_OF_LINKS;
    }

    @Override
    public VerificationRunResult verify(VerificationContext context) {

        return context.layoutSegments()
                .stream()
                .map(segment -> verifyForSegment(segment, context))
                .findFirst()
                .orElse(VerificationRunResult.okay());

    }

    private VerificationRunResult verifyForSegment(LayoutSegment segment, VerificationContext context) {
        Optional<String> invalidStep = context
                .getExpectedStepNamesBySegmentName(segment.getName())
                .stream()
                .filter(stepName -> !isValid(segment.getName(), stepName, context))
                .findFirst();

        return VerificationRunResult.valid(invalidStep.isEmpty());
    }

    private boolean isValid(String segmentName, String stepName, VerificationContext context) {
        Map<Integer, List<LinkMetaBlock>> linkMetaBlockMap = context
                .getLinksBySegmentNameAndStepName(segmentName, stepName).stream()
                .collect(groupingBy(f -> f.getLink().hashCode()));
        if (linkMetaBlockMap.size() == 1) {
            return isValid(linkMetaBlockMap.values().iterator().next(), context.getStepBySegmentNameAndStepName(segmentName, stepName));
        } else {
            log.info("more than one or no links with the same hash for step {}", stepName);
            return false;
        }
    }

    private boolean isValid(List<LinkMetaBlock> linkMetaBlocks, Step step) {
        return linkMetaBlocks.size() >= step.getRequiredNumberOfLinks();
    }
}
