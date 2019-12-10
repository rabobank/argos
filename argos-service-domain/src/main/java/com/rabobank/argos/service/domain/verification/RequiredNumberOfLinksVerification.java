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

import com.rabobank.argos.domain.Signature;
import com.rabobank.argos.domain.layout.Step;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
        Map<Integer, List<LinkMetaBlock>> linkMetaBlockMap = context.getLinkMetaBlocks().stream()
                .collect(groupingBy(f -> f.getLink().hashCode()));

        List<LinkMetaBlock> invalidLinkMetaBlock = linkMetaBlockMap.values().stream()
                .filter(linkMetaBlocks -> isInvalid(linkMetaBlocks, context))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        log.info("{} invalid Link Meta Blocks", invalidLinkMetaBlock.size());
        context.removeLinkMetaBlocks(invalidLinkMetaBlock);

        return VerificationRunResult.okay();
    }

    private boolean isInvalid(List<LinkMetaBlock> linkMetaBlocks, VerificationContext context) {
        String stepName = linkMetaBlocks.get(0).getLink().getStepName();
        Step step = context.getStepByStepName(stepName);
        if (linkMetaBlocks.size() >= step.getRequiredNumberOfLinks()) {
            Set<String> linkKeyIds = linkMetaBlocks.stream().map(LinkMetaBlock::getSignature).map(Signature::getKeyId).collect(Collectors.toSet());
            return !linkKeyIds.containsAll(step.getAuthorizedKeyIds());
        } else {
            return true;
        }
    }
}
