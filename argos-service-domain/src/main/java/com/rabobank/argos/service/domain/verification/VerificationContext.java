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
import com.rabobank.argos.domain.link.Link;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.groupingBy;

@ToString
public class VerificationContext {
    private static final String COULD_NOT_BE_FOUND = " could not be found";
    @Getter
    private final List<LinkMetaBlock> linkMetaBlocks;
    @Getter
    private final List<LinkMetaBlock> originalLinkMetaBlocks;
    @Getter
    private final LayoutMetaBlock layoutMetaBlock;
    private Map<String, Map<String, Step>> stepBySegmentNameAndStepName;
    private Map<String, Map<String, List<LinkMetaBlock>>> linkMetaBlocksBySegmentNameAndStepName;
    private Map<String, Map<Step, Link>> linksBySegmentNameAndStep;
    private Map<String, Map<String, Link>> linksBySegmentNameAndStepName;

    @Builder
    public VerificationContext(@NonNull List<LinkMetaBlock> linkMetaBlocks, 
            @NonNull LayoutMetaBlock layoutMetaBlock) {
        this.linkMetaBlocks = new ArrayList<>(linkMetaBlocks);
        this.originalLinkMetaBlocks = new ArrayList<>(linkMetaBlocks);
        this.layoutMetaBlock = layoutMetaBlock;
        
        linkMetaBlocksBySegmentNameAndStepName = this.linkMetaBlocks.stream()
                .collect(groupingBy(linkMetaBlock -> linkMetaBlock.getLink().getLayoutSegmentName(),
                        groupingBy(linkMetaBlock -> linkMetaBlock.getLink().getStepName())));

        createMaps();
    }
    
    private void createMaps() {
        stepBySegmentNameAndStepName = new HashMap<>();
        linksBySegmentNameAndStep = new HashMap<>();
        linksBySegmentNameAndStepName = new HashMap<>();
        layoutMetaBlock
                .getLayout()
                .getLayoutSegments()
                .forEach(segment -> {
                    stepBySegmentNameAndStepName.put(segment.getName(), new HashMap<>());
                    linksBySegmentNameAndStep.put(segment.getName(), new HashMap<>());
                    linksBySegmentNameAndStepName.put(segment.getName(), new HashMap<>());
                    linkMetaBlocksBySegmentNameAndStepName.putIfAbsent(segment.getName(), new HashMap<>());
                    segment.getSteps().forEach(step -> {
                        stepBySegmentNameAndStepName
                            .get(segment.getName())
                            .put(step.getName(), step);
                        linkMetaBlocksBySegmentNameAndStepName
                            .get(segment.getName())
                            .putIfAbsent(step.getName(), new ArrayList<>());
                        linksBySegmentNameAndStep
                            .get(segment.getName())
                            .putIfAbsent(step, null);
                        linksBySegmentNameAndStepName
                            .get(segment.getName())
                            .putIfAbsent(step.getName(), null);
                        List<LinkMetaBlock> metaBlocks = linkMetaBlocksBySegmentNameAndStepName
                                .getOrDefault(segment.getName(), emptyMap())
                                .getOrDefault(step.getName(), emptyList());
                        if (!metaBlocks.isEmpty() && metaBlocks.get(0).getLink() != null) {
                            linksBySegmentNameAndStep
                                .get(segment.getName())
                                .put(step, metaBlocks.get(0).getLink());
                            linksBySegmentNameAndStepName
                                .get(segment.getName())
                                .put(step.getName(), metaBlocks.get(0).getLink());
                        }
                    });
                });
        
    }

    public List<LayoutSegment> layoutSegments() {
        return layoutMetaBlock
                .getLayout()
                .getLayoutSegments();
    }

    public void removeLinkMetaBlocks(List<LinkMetaBlock> linkMetaBlocksToRemove) {
        linkMetaBlocks.removeAll(linkMetaBlocksToRemove);
        linkMetaBlocksBySegmentNameAndStepName.forEach((segmentName, stepNames) -> stepNames
                .forEach((stepName, blocks) -> blocks.removeAll(linkMetaBlocksToRemove)));
        createMaps();
    }

    public List<String> getStepNamesBySegmentName(String segmentName) {
        Map<String, List<LinkMetaBlock>> segmentMap = linkMetaBlocksBySegmentNameAndStepName.get(segmentName);
        if (segmentMap == null) {
            throw new VerificationError("layout segment with name: " + segmentName + COULD_NOT_BE_FOUND);
        } else {
            return new ArrayList<>(segmentMap.keySet());
        }
    }

    public Step getStepBySegmentNameAndStepName(String segmentName, String stepName) {
        if (!stepBySegmentNameAndStepName.containsKey(segmentName)) {
            throw new VerificationError("segment with name: " + segmentName + COULD_NOT_BE_FOUND);
        }
        if (!stepBySegmentNameAndStepName.get(segmentName).containsKey(stepName)) {
            throw new VerificationError("step with name: " + stepName + COULD_NOT_BE_FOUND);
        }
        return stepBySegmentNameAndStepName.get(segmentName).get(stepName);
    }
    
    public List<LinkMetaBlock> getLinkMetaBlocksBySegmentNameAndStepName(String segmentName, String stepName) {
        if (linkMetaBlocksBySegmentNameAndStepName.get(segmentName) == null || linkMetaBlocksBySegmentNameAndStepName.get(segmentName).get(stepName) == null) {
            return emptyList();
        }
        return linkMetaBlocksBySegmentNameAndStepName.get(segmentName).get(stepName);
    }
    
    public Map<String, Map<Step, Link>> getLinksBySegmentNameAndStep() {
        return linksBySegmentNameAndStep;
    }
    
    public Map<String, Map<String, Link>> getLinksBySegmentNameAndStepName() {
        return linksBySegmentNameAndStepName;
    }
    
}
