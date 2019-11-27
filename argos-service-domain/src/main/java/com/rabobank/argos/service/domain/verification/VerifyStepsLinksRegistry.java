package com.rabobank.argos.service.domain.verification;

import com.rabobank.argos.domain.layout.Step;
import com.rabobank.argos.domain.link.LinkMetaBlock;

import java.util.List;

public interface VerifyStepsLinksRegistry {
    Step getStepByStepName(String stepName);

    List<LinkMetaBlock> getLinksByStepName(String stepName);
}
