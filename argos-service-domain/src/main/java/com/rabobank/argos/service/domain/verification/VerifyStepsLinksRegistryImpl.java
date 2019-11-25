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
    private final Map<String, List<LinkMetaBlock>> linksByStepName;
    private final Map<String, List<Step>> stepsByStepName;

    @Override
    public Step getStepByStepName() {
        return null;
    }

    @Override
    public List<LinkMetaBlock> getLinksByStepName() {
        return Collections.emptyList();
    }
}
