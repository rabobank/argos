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
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Getter
public class VerificationContext {
    public static final String COULD_NOT_BE_FOUND = " could not be found";
    @Getter
    private final List<LinkMetaBlock> linkMetaBlocks;
    @Getter
    private final List<LinkMetaBlock> originalLinkMetaBlocks;
    @Getter
    private final LayoutMetaBlock layoutMetaBlock;
    private final Map<String, List<LinkMetaBlock>> linksByStepName;
    private final Map<String, List<LinkMetaBlock>> originalLinksByStepName;
    private Map<String, Map<String, Step>> stepBySegmentNameAndStepName = new HashMap<>();
    private Map<String, Map<String, List<LinkMetaBlock>>> linksBySegmentNameAndStepName = new HashMap<>();
    private Map<String, Map<String, List<LinkMetaBlock>>> originallinksBySegmentNameAndStepName = new HashMap<>();

    @Builder
    public VerificationContext(List<LinkMetaBlock> linkMetaBlocks, LayoutMetaBlock layoutMetaBlock) {
        this.linkMetaBlocks = new ArrayList<>(linkMetaBlocks);
        this.originalLinkMetaBlocks = new ArrayList<>(linkMetaBlocks);
        this.layoutMetaBlock = layoutMetaBlock;
        linksByStepName = linkMetaBlocks.stream().collect(groupingBy(linkMetaBlock -> linkMetaBlock.getLink().getStepName()));
        originalLinksByStepName = linkMetaBlocks.stream().collect(groupingBy(linkMetaBlock -> linkMetaBlock.getLink().getStepName()));

        layoutMetaBlock
                .getLayout()
                .getLayoutSegments().forEach(segment -> {
            stepBySegmentNameAndStepName
                    .put(segment.getName(), new HashMap<>());
            segment.getSteps()
                    .forEach(step -> stepBySegmentNameAndStepName
                            .get(segment.getName())
                            .put(step.getStepName(), step));

        });

        linksBySegmentNameAndStepName = this.linkMetaBlocks.stream()
                .collect(groupingBy(linkMetaBlock -> linkMetaBlock.getLink().getLayoutSegmentName(),
                        groupingBy(linkMetaBlock -> linkMetaBlock.getLink().getStepName())));

        originallinksBySegmentNameAndStepName = this.originalLinkMetaBlocks.stream()
                .collect(groupingBy(linkMetaBlock -> linkMetaBlock.getLink().getLayoutSegmentName(),
                        groupingBy(linkMetaBlock -> linkMetaBlock.getLink().getStepName())));

    }


    public List<LayoutSegment> layoutSegments() {
        return layoutMetaBlock
                .getLayout()
                .getLayoutSegments();
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
        linksBySegmentNameAndStepName.entrySet()
                .forEach(stringMapEntry -> stringMapEntry.getValue()
                        .entrySet().forEach(stringListEntry -> stringListEntry
                                .getValue().removeAll(linkMetaBlocksToRemove)));
    }

    public List<String> getExpectedStepNamesBySegmentName(String segmentName) {
        return layoutMetaBlock
                .getLayout()
                .getLayoutSegments()
                .stream().filter(segment -> segment.getName().equals(segmentName))
                .findFirst()
                .orElseThrow(() -> new VerificationError("layout segment with name: " + segmentName + COULD_NOT_BE_FOUND))
                .getSteps().stream().map(Step::getStepName)
                .collect(toList());
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

    public Map<String, List<LinkMetaBlock>> getLinksBySegmentName(String segmentName) {
        return linksBySegmentNameAndStepName.getOrDefault(segmentName, emptyMap());
    }

    public List<String> getStepNamesFromLinksBySegmentName(String name) {
        return getLinksBySegmentName(name)
                .entrySet().stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public List<LinkMetaBlock> getLinksBySegmentNameAndStepName(String segmentName, String stepName) {
        return getLinksBySegmentName(segmentName).getOrDefault(stepName, emptyList());
    }
}
