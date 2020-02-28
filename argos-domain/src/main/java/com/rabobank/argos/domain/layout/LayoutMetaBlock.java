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
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Set;

import static java.util.UUID.randomUUID;

@Getter
@Setter
@Builder
@ToString
public class LayoutMetaBlock {
    private String supplyChainId;

    @Builder.Default
    private String layoutMetaBlockId = randomUUID().toString();

    private List<Signature> signatures;

    private Layout layout;

    public Boolean allLayoutSegmentsAreResolved(Set<String> resolvedSegmentNames) {
        return layout.getLayoutSegments().size() == resolvedSegmentNames.size();
    }

    public List<MatchFilter> expectedEndProducts() {
        return layout.getExpectedEndProducts();
    }

}
