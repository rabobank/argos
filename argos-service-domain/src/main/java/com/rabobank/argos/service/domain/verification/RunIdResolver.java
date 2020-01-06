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

import com.rabobank.argos.domain.layout.Layout;
import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.domain.layout.LayoutSegment;
import com.rabobank.argos.domain.layout.MatchFilter;
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.domain.link.Link;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import com.rabobank.argos.service.domain.link.LinkMetaBlockRepository;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static com.rabobank.argos.domain.layout.DestinationType.MATERIALS;
import static com.rabobank.argos.domain.layout.DestinationType.PRODUCTS;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

@Component
@Slf4j
@RequiredArgsConstructor
public class RunIdResolver {

    private final LinkMetaBlockRepository linkMetaBlockRepository;

    @Getter
    @Builder
    @ToString
    private static class MatchedProductWithRunIds {
        private final String supplyChainId;
        private final MatchFilter matchFilter;
        private List<Artifact> matchedProductsToVerify;
        @Builder.Default
        private Set<String> runIds = new TreeSet<>();

        String getSegmentName() {
            return matchFilter.getDestinationSegmentName();
        }

        String getStepName() {
            return matchFilter.getDestinationStepName();
        }

        List<String> getHashes() {
            return matchedProductsToVerify.stream().map(Artifact::getHash).collect(toList());
        }

    }

    public List<RunIdsWithSegment> getRunIdPerSegment(LayoutMetaBlock layoutMetaBlock, List<Artifact> productsToVerify) {
        Layout layout = layoutMetaBlock.getLayout();

        return layout.getLayoutSegments().stream().map(segment -> RunIdsWithSegment.builder()
                .segment(segment)
                .runIds(getRunIdsForSegment(layoutMetaBlock, segment, productsToVerify)).build())
                .collect(toList());
    }

    private Set<String> getRunIdsForSegment(LayoutMetaBlock layoutMetaBlock, LayoutSegment segment, List<Artifact> productsToVerify) {
        Layout layout = layoutMetaBlock.getLayout();
        List<MatchedProductWithRunIds> matchedProductWithRunIds = layout
                .getExpectedEndProducts()
                .stream()
                .filter(expectedEndProduct -> expectedEndProduct.getDestinationSegmentName().equals(segment.getName()))
                .map(expectedEndProduct -> MatchedProductWithRunIds.builder()
                        .supplyChainId(layoutMetaBlock.getSupplyChainId())
                        .matchFilter(expectedEndProduct)
                        .matchedProductsToVerify(matches(expectedEndProduct, productsToVerify))
                        .build())
                .peek(this::addRunIds)
                .peek(p -> log.info("segment:{} {}", segment.getName(), p))
                .collect(toList());


        return matchedProductWithRunIds
                .stream()
                .map(MatchedProductWithRunIds::getRunIds)
                .flatMap(Set::stream).collect(toCollection(TreeSet::new));
    }

    private List<Artifact> matches(MatchFilter matchFilter, List<Artifact> productsToVerify) {
        return productsToVerify.stream().filter(artifact -> ArtifactMatcher.matches(artifact.getUri(), matchFilter.getPattern())).collect(toList());
    }

    private void addRunIds(MatchedProductWithRunIds matchedProductWithRunIds) {
        if (PRODUCTS == matchedProductWithRunIds.matchFilter.getDestinationType()) {
            searchInProductHashes(matchedProductWithRunIds);
        } else if (MATERIALS == matchedProductWithRunIds.matchFilter.getDestinationType()) {
            searchInMaterialsHashes(matchedProductWithRunIds);
        }
    }

    private void searchInMaterialsHashes(MatchedProductWithRunIds matchedProductWithRunIds) {
        matchedProductWithRunIds.getRunIds().addAll(
                linkMetaBlockRepository
                        .findBySupplyChainAndSegmentNameAndStepNameAndMaterialHash(
                                matchedProductWithRunIds.getSupplyChainId(),
                                matchedProductWithRunIds.getSegmentName(),
                                matchedProductWithRunIds.getStepName(),
                                matchedProductWithRunIds.getHashes())
                        .stream()
                        .map(LinkMetaBlock::getLink)
                        .map(Link::getRunId).collect(toList()));
    }

    private void searchInProductHashes(MatchedProductWithRunIds matchedProductWithRunIds) {
        matchedProductWithRunIds.getRunIds().addAll(
                linkMetaBlockRepository
                        .findBySupplyChainAndSegmentNameAndStepNameAndProductHashes(
                                matchedProductWithRunIds.getSupplyChainId(),
                                matchedProductWithRunIds.getSegmentName(),
                                matchedProductWithRunIds.getStepName(),
                                matchedProductWithRunIds.getHashes())
                        .stream()
                        .map(LinkMetaBlock::getLink)
                        .map(Link::getRunId).collect(toList()));
    }


}
