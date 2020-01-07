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

import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.domain.layout.LayoutSegment;
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import com.rabobank.argos.service.domain.link.LinkMetaBlockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@Slf4j
public class VerificationProvider {

    private final LinkMetaBlockRepository linkMetaBlockRepository;
    private final RunIdResolver runIdResolver;
    private final List<Verification> verifications;

    @PostConstruct
    public void init() {
        verifications.sort(Comparator.comparing(Verification::getPriority));
        log.info("active verifications:");
        verifications.forEach(verification -> log.info("{} : {}", verification.getPriority(), verification.getClass().getSimpleName()));
    }

    public VerificationRunResult verifyRun(LayoutMetaBlock layoutMetaBlock, List<Artifact> productsToVerify) {
        return runIdResolver.getRunIdPerSegment(layoutMetaBlock, productsToVerify)
                .stream()
                .map(runIdsWithSegment -> verifyRun(runIdsWithSegment, layoutMetaBlock))
                .filter(verificationRunResult -> !verificationRunResult.isRunIsValid()).findFirst()
                .orElse(VerificationRunResult.okay());
    }

    private VerificationRunResult verifyRun(RunIdsWithSegment runIdsWithSegment, LayoutMetaBlock layoutMetaBlock) {

        List<VerificationRunResult> verificationRunResults = runIdsWithSegment.getRunIds()
                .stream()
                .peek(runId -> log.info("verify segment {} with rundId {}", runIdsWithSegment.getSegment().getName(), runId))
                .flatMap(runId -> verifyRunWithPossibleVerificationContexts(runId, runIdsWithSegment.getSegment(), layoutMetaBlock))
                .collect(Collectors.toList());

        return verificationRunResults
                .stream()
                .peek(verificationRunResult -> log.info("segment validity: {}", verificationRunResult.isRunIsValid()))
                .filter(VerificationRunResult::isRunIsValid)
                .findFirst().orElse(VerificationRunResult.valid(false));

    }

    private Stream<VerificationRunResult> verifyRunWithPossibleVerificationContexts(String runId, LayoutSegment segment, LayoutMetaBlock layoutMetaBlock) {
        List<LinkMetaBlock> linkMetaBlocks = linkMetaBlockRepository.findByRunId(layoutMetaBlock.getSupplyChainId(), runId);
        VerificationContextsProvider verificationContextsProvider = new VerificationContextsProvider(linkMetaBlocks, segment, layoutMetaBlock);
        List<VerificationContext> possibleVerificationContexts = verificationContextsProvider.calculatePossibleVerificationContexts();
        log.info("created {} verification contexts", possibleVerificationContexts.size());
        return possibleVerificationContexts
                .stream()
                .map(context -> verifications.stream()
                        .map(verification -> verification.verify(context))
                        .filter(result -> !result.isRunIsValid())
                        .findFirst().orElse(VerificationRunResult.okay())
                );

    }

}
