package com.rabobank.argos.service.domain.verification;

import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.domain.layout.Step;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Getter
public class VerificationContext {

    private final List<LinkMetaBlock> linkMetaBlocks;
    private final LayoutMetaBlock layoutMetaBlock;
    private Map<String, List<LinkMetaBlock>> linksByStepName = new HashMap<>();
    private Map<String, Step> stepByStepName = new HashMap<>();

    @Builder
    public VerificationContext(List<LinkMetaBlock> linkMetaBlocks, LayoutMetaBlock layoutMetaBlock) {
        this.linkMetaBlocks = linkMetaBlocks;
        this.layoutMetaBlock = layoutMetaBlock;
        layoutMetaBlock.getLayout().getSteps().forEach(step -> stepByStepName.put(step.getStepName(), step));
        linkMetaBlocks.forEach(linkMetaBlock -> linksByStepName.getOrDefault(linkMetaBlock.getLink().getStepName(), new ArrayList<>()).add(linkMetaBlock));
    }


    public Step getStepByStepName(String stepName) {
        if (!stepByStepName.containsKey(stepName)) {
            throw new VerificationError("step with name: " + stepName + " could not be found");
        }
        return stepByStepName.get(stepName);
    }


    public List<LinkMetaBlock> getLinksByStepName(String stepName) {
        if (!linksByStepName.containsKey(stepName)) {
            throw new VerificationError("LinkMetaBlocks with name: " + stepName + " could not be found");
        }
        return linksByStepName.get(stepName);
    }

}
