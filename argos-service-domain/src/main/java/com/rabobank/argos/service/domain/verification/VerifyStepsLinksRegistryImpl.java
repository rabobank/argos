package com.rabobank.argos.service.domain.verification;

import com.rabobank.argos.domain.layout.Step;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Builder
@RequiredArgsConstructor
public class VerifyStepsLinksRegistryImpl implements VerifyStepsLinksRegistry {
    @Builder.Default
    private final Map<String, List<LinkMetaBlock>> linksByStepName = Collections.emptyMap();
    @Builder.Default
    private final Map<String, Step> stepByStepName = Collections.emptyMap();

    @Override
    public Step getStepByStepName(String stepName) {
        if (!stepByStepName.containsKey(stepName)) {
            throw new VerificationError("step with name: " + stepName + " could not be found");
        }
        return stepByStepName.get(stepName);
    }

    @Override
    public List<LinkMetaBlock> getLinksByStepName(String stepName) {
        if (!linksByStepName.containsKey(stepName)) {
            throw new VerificationError("LinkMetaBlocks with name: " + stepName + " could not be found");
        }
        return linksByStepName.get(stepName);
    }
}
