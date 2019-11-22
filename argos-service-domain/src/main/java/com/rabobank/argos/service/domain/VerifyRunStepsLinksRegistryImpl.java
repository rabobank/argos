package com.rabobank.argos.service.domain;

import com.rabobank.argos.domain.model.LinkMetaBlock;
import com.rabobank.argos.domain.model.Step;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Builder
@RequiredArgsConstructor
public class VerifyRunStepsLinksRegistryImpl implements VerifyRunStepsLinksRegistry {
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
