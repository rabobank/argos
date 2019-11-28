package com.rabobank.argos.service.domain.verification;

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
