package com.rabobank.argos.service.domain;

import com.rabobank.argos.domain.model.LinkMetaBlock;
import com.rabobank.argos.domain.model.Step;

import java.util.List;

public interface VerifyRunStepsLinksRegistry {
    Step getStepByStepName();
    List<LinkMetaBlock> getLinksByStepName();
}
