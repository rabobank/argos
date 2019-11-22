package com.rabobank.argos.service.domain;

import com.rabobank.argos.domain.model.Artifact;
import com.rabobank.argos.domain.model.LayoutMetaBlock;
import com.rabobank.argos.domain.model.LinkMetaBlock;
import com.rabobank.argos.domain.model.Step;
import com.rabobank.argos.domain.signing.SignatureValidator;
import com.rabobank.argos.service.domain.repository.KeyPairRepository;
import com.rabobank.argos.service.domain.repository.LinkMetaBlockRepository;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class VerificationRunProvider {

    private final SignatureValidator signatureValidator;
    private final KeyPairRepository keyPairRepository;
    private final LinkMetaBlockRepository linkMetaBlockRepository;

    public VerificationRunResult verifyRun(LayoutMetaBlock layoutMetaBlock, List<Artifact> expectedProducts) {
        verifySignatures(layoutMetaBlock);
        List<LinkMetaBlock> links = getLinksForThisRun(layoutMetaBlock, expectedProducts);
        Map<Step, List<LinkMetaBlock>> stepsLinkRegistry = createStepsLinksRegistry(layoutMetaBlock.getLayout().getSteps());

        return VerificationRunResult.builder().runIsValid(true).build();
    }

    private Map<Step, List<LinkMetaBlock>> createStepsLinksRegistry(List<Step> steps) {
        return new HashMap<>();
    }

    private List<LinkMetaBlock> getLinksForThisRun(LayoutMetaBlock layoutMetaBlock, List<Artifact> expectedProducts) {
        return Collections.emptyList();
    }

    private void verifySignatures(LayoutMetaBlock layoutMetaBlock) {

    }

    @Getter
    @Builder
    public static class VerificationRunResult {
        private boolean runIsValid = false;
        private List<String> messages;
    }


}
