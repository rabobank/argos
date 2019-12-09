/*
 * Copyright (C) 2019 Rabobank Nederland
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
import com.rabobank.argos.domain.layout.Step;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import lombok.Builder;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@Getter
public class VerificationContext {

    private final List<LinkMetaBlock> linkMetaBlocks;
    private final LayoutMetaBlock layoutMetaBlock;
    private final Map<String, List<LinkMetaBlock>> linksByStepName;
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
        return linksByStepName.getOrDefault(stepName, emptyList());
    }

    public void removeLinkMetaBlocks(List<LinkMetaBlock> linkMetaBlocksToRemove) {
        linkMetaBlocks.removeAll(linkMetaBlocksToRemove);
        linksByStepName.values().forEach(blocks -> blocks.removeAll(linkMetaBlocksToRemove));
    }

    public List<String> getExpectedStepNames() {
        return layoutMetaBlock.getLayout().getSteps().stream().map(Step::getStepName).collect(toList());
    }
}
