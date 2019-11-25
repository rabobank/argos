package com.rabobank.argos.service.domain;

import com.rabobank.argos.domain.model.Artifact;
import com.rabobank.argos.domain.model.LayoutMetaBlock;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class VerificationRunProvider {

    // private final SignatureValidator signatureValidator;
    // private final KeyPairRepository keyPairRepository;
    // private final LinkMetaBlockRepository linkMetaBlockRepository;

    public VerificationRunResult verifyRun(LayoutMetaBlock layoutMetaBlock, List<Artifact> productsToVerify
    ) {
        //verifySignatures(layoutMetaBlock);
        //List<LinkMetaBlock> links = getLinksForThisRun(layoutMetaBlock, productsToVerify);
        //VerifyRunStepsLinksRegistry verifyRunStepsLinksRegistry = createStepsLinksRegistry(layoutMetaBlock.getLayout().getSteps());
        return VerificationRunResult.builder().runIsValid(true).build();
    }

    /*private VerifyRunStepsLinksRegistry createStepsLinksRegistry(List<Step> steps) {
        return VerifyRunStepsLinksRegistryImpl.builder().build();
    }

    private List<LinkMetaBlock> getLinksForThisRun(LayoutMetaBlock layoutMetaBlock, List<Artifact> productsToVerify) {
        layoutMetaBlock expectedEndProducts match rules apply

        return Collections.emptyList();
    }

    private void verifySignatures(LayoutMetaBlock layoutMetaBlock) {

    }*/

    @Getter
    @Builder
    public static class VerificationRunResult {
        private boolean runIsValid = false;
        private List<String> messages;
    }


}
