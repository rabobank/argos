package com.rabobank.argos.service.domain.verification;

import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.domain.layout.Step;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import lombok.Builder;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

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
        linksByStepName = linkMetaBlocks
                .stream()
                .collect(Collectors.groupingBy(linkMetaBlock -> linkMetaBlock.getLink().getStepName()));
    }

    public Step getStepByStepName(String stepName) {
        if (!stepByStepName.containsKey(stepName)) {
            throw new VerificationError("step with name: " + stepName + " could not be found");
        }
        return stepByStepName.get(stepName);
    }

    public List<LinkMetaBlock> getLinksByStepName(String stepName) {
        if (!linksByStepName.containsKey(stepName)) {
            throw new VerificationError("LinkMetaBlocks with step name: " + stepName + " could not be found");
        }
        return linksByStepName.get(stepName);
    }

    public void removeLinkMetaBlocks(List<LinkMetaBlock> linkMetaBlocksToRemove) {
        linkMetaBlocks.removeAll(linkMetaBlocksToRemove);
        linksByStepName.values().forEach(blocks -> blocks.removeAll(linkMetaBlocksToRemove));
    }

    public List<String> getExpectedStepNames() {
        return layoutMetaBlock.getLayout().getSteps().stream().map(Step::getStepName).collect(toList());
    }
}
