/*
 * Copyright (C) 2019 Rabobank Nederland
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

import com.rabobank.argos.domain.layout.Step;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.rabobank.argos.service.domain.verification.Verification.Priority.REQUIRED_NUMBER_OF_LINKS;

@Component
@Slf4j
public class RequiredNumberOfLinksVerification implements Verification {
    @Override
    public Priority getPriority() {
        return REQUIRED_NUMBER_OF_LINKS;
    }

    @Override
    public VerificationRunResult verify(VerificationContext context) {

        Optional<Step> failedRequiredNumberOfLinks = context
                .getLayoutMetaBlock()
                .getLayout()
                .getSteps()
                .stream()
                .filter(step -> stepDoesNotHaveRequiredNumberOfLinks(step, context))
                .findFirst();

        failedRequiredNumberOfLinks
                .ifPresent(step -> log.info("failed verification step:{}, requiredNumberOfLinks: {} , actual links: {}",
                        step.getStepName(),
                        step.getRequiredNumberOfLinks(),
                        context.getLinksByStepName(step.getStepName()).size())
                );

        return VerificationRunResult
                .builder()
                .runIsValid(failedRequiredNumberOfLinks.isEmpty()).build();
    }

    private static boolean stepDoesNotHaveRequiredNumberOfLinks(Step step, VerificationContext context) {
        List<LinkMetaBlock> linkMetaBlocks = context.getLinksByStepName(step.getStepName());
        return step.getRequiredNumberOfLinks() > context.getLinksByStepName(step.getStepName()).size()
                ||
                keyIdsAreNotUnique(linkMetaBlocks);
    }

    private static boolean keyIdsAreNotUnique(List<LinkMetaBlock> linkMetaBlocks) {
        Set<String> uniqueKeyIds = linkMetaBlocks
                .stream()
                .map(linkMetaBlock -> linkMetaBlock
                        .getSignature()
                        .getKeyId())
                .collect(Collectors.toSet());

        List<String> realKeyIds = linkMetaBlocks
                .stream()
                .map(linkMetaBlock -> linkMetaBlock
                        .getSignature()
                        .getKeyId())
                .collect(Collectors.toList());

        return uniqueKeyIds.size() != realKeyIds.size();
    }
}
