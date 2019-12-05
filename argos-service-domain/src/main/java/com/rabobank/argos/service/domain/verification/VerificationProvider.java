package com.rabobank.argos.service.domain.verification;

/*-
 * #%L
 * Argos Supply Chain Notary
 * %%
 * Copyright (C) 2019 Rabobank Nederland
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import com.rabobank.argos.service.domain.link.LinkMetaBlockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Comparator;
import java.util.List;

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
        return runIdResolver.getRunId(layoutMetaBlock, productsToVerify)
                .map(runId -> verifyRun(runId, layoutMetaBlock))
                .orElse(VerificationRunResult.builder().runIsValid(false).build());
    }

    private VerificationRunResult verifyRun(String runId, LayoutMetaBlock layoutMetaBlock) {
        return verifyRun(linkMetaBlockRepository.findByRunId(layoutMetaBlock.getSupplyChainId(), runId), layoutMetaBlock);
    }

    private VerificationRunResult verifyRun(List<LinkMetaBlock> linkMetaBlocks, LayoutMetaBlock layoutMetaBlock) {
        VerificationContext context = VerificationContext.builder()
                .linkMetaBlocks(linkMetaBlocks)
                .layoutMetaBlock(layoutMetaBlock)
                .build();
        return verifications.stream()
                .map(verification -> verification.verify(context))
                .filter(result -> !result.isRunIsValid())
                .findFirst().orElse(VerificationRunResult.okay());
    }

}
