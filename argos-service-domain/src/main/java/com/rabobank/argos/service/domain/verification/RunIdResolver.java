package com.rabobank.argos.service.domain.verification;

import com.rabobank.argos.domain.layout.Layout;
import com.rabobank.argos.domain.layout.LayoutMetaBlock;
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
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Component
@Slf4j
@RequiredArgsConstructor
public class RunIdResolver {

    private final LinkMetaBlockRepository linkMetaBlockRepository;

    @Getter
    @Builder
    @ToString
    private static class ExpectedProductWithRunIds {
        private final String supplyChainId;
        private final MatchFilter matchFilter;
        private List<Artifact> matchedProductsToVerify;
        @Builder.Default
        private Set<String> runIds = new TreeSet<>();

        String getStepName() {
            return matchFilter.getDestinationStepName();
        }

        List<String> getHashes() {
            return matchedProductsToVerify.stream().map(Artifact::getHash).collect(toList());
        }

    }

    public Optional<String> getRunId(LayoutMetaBlock layoutMetaBlock, List<Artifact> productsToVerify) {

        Layout layout = layoutMetaBlock.getLayout();
        List<ExpectedProductWithRunIds> expectedProductWithRunIds = layout.getExpectedEndProducts().stream().map(expectedEndProduct -> ExpectedProductWithRunIds.builder()
                .supplyChainId(layoutMetaBlock.getSupplyChainId())
                .matchFilter(expectedEndProduct)
                .matchedProductsToVerify(expectedEndProduct.matches(productsToVerify))
                .build())
                .peek(this::addRunIds)
                .peek(p -> log.info("{}", p))
                .collect(toList());


        Set<String> allRunIds = expectedProductWithRunIds.stream().map(ExpectedProductWithRunIds::getRunIds).flatMap(Set::stream).collect(Collectors.toCollection(TreeSet::new));

        return allRunIds.stream().filter(runId -> isInAll(runId, expectedProductWithRunIds)).findFirst();

    }

    private boolean isInAll(String runId, List<ExpectedProductWithRunIds> expectedProductWithRunIdsList) {
        return expectedProductWithRunIdsList.stream().allMatch(expectedProductWithRunIds -> expectedProductWithRunIds.getRunIds().contains(runId));
    }

    private void addRunIds(ExpectedProductWithRunIds expectedProductWithRunIds) {
        expectedProductWithRunIds.getRunIds().addAll(
                linkMetaBlockRepository
                        .findBySupplyChainAndStepNameAndProductHashes(
                                expectedProductWithRunIds.getSupplyChainId(),
                                expectedProductWithRunIds.getStepName(),
                                expectedProductWithRunIds.getHashes())
                        .stream().map(LinkMetaBlock::getLink).map(Link::getRunId).collect(toList()));
    }
}
