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
package com.rabobank.argos.domain.layout;

import com.rabobank.argos.domain.Signature;
import com.rabobank.argos.domain.layout.rule.MatchRule;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.UUID.randomUUID;

@Getter
@Setter
@Builder
public class LayoutMetaBlock {
    private String supplyChainId;

    @Builder.Default
    private String layoutMetaBlockId = randomUUID().toString();

    private List<Signature> signatures;

    private Layout layout;

    public Boolean allLayoutSegmentsAreResolved(List<String> resolvedSegmentNames) {
        return layout.getLayoutSegments().size() == resolvedSegmentNames.size();
    }

    public List<MatchFilter> expectedEndProducts() {
        return layout.getExpectedEndProducts();
    }


    public List<MatchRule> productMatchRulesForResolvedSegments(List<String> resolvedSegments) {
        return layout.getLayoutSegments()
                .stream()
                .filter(layoutSegment -> resolvedSegments.contains(layoutSegment.getName()))
                .flatMap(layoutSegment -> layoutSegment.getSteps()
                        .stream()
                        .map(step -> step.getExpectedProducts()
                                .stream()
                                .filter(rule -> rule instanceof MatchRule))
                        .map(rule -> ((MatchRule) rule))
                ).collect(Collectors.toList());
    }
}
