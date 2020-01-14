/*
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rabobank.argos.service.domain.verification;

import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.domain.layout.LayoutSegment;
import com.rabobank.argos.domain.layout.Step;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Getter
public class VerificationContext {

    private final List<LinkMetaBlock> linkMetaBlocks;
    private final List<LinkMetaBlock> originalLinkMetaBlocks;
    private final LayoutSegment segment;
    private final LayoutMetaBlock layoutMetaBlock;
    private final Map<String, List<LinkMetaBlock>> linksByStepName;
    private final Map<String, List<LinkMetaBlock>> originalLinksByStepName;
    private Map<String, Step> stepByStepName = new HashMap<>();

    @Builder
    public VerificationContext(List<LinkMetaBlock> linkMetaBlocks, LayoutMetaBlock layoutMetaBlock, LayoutSegment segment) {
        this.linkMetaBlocks = new ArrayList<>(linkMetaBlocks);
        this.originalLinkMetaBlocks = new ArrayList<>(linkMetaBlocks);
        this.layoutMetaBlock = layoutMetaBlock;
        this.segment = segment;
        segment.getSteps().forEach(step -> stepByStepName.put(step.getStepName(), step));
        linksByStepName = linkMetaBlocks.stream().collect(groupingBy(linkMetaBlock -> linkMetaBlock.getLink().getStepName()));
        originalLinksByStepName = linkMetaBlocks.stream().collect(groupingBy(linkMetaBlock -> linkMetaBlock.getLink().getStepName()));
    }

    public Step getStepByStepName(String stepName) {
        if (!stepByStepName.containsKey(stepName)) {
            throw new VerificationError("step with name: " + stepName + " could not be found");
        }
        return stepByStepName.get(stepName);
    }

    public List<LinkMetaBlock> getLinksByStepName(String stepName) {
        return linksByStepName.getOrDefault(stepName, emptyList());
    }

    public List<LinkMetaBlock> getOriginalLinksByStepName(String stepName) {
        return linksByStepName.getOrDefault(stepName, emptyList());
    }

    public void removeLinkMetaBlocks(List<LinkMetaBlock> linkMetaBlocksToRemove) {
        linkMetaBlocks.removeAll(linkMetaBlocksToRemove);
        linksByStepName.values().forEach(blocks -> blocks.removeAll(linkMetaBlocksToRemove));
    }

    public List<String> getExpectedStepNames() {

        //todo remove this code after multiple segments support is completed
        if (segment == null) {
            return layoutMetaBlock
                    .getLayout()
                    .getLayoutSegments()
                    .iterator()
                    .next()
                    .getSteps().stream().map(Step::getStepName)
                    .collect(toList());
        }

        return segment.getSteps().stream().map(Step::getStepName).collect(toList());
    }
}
