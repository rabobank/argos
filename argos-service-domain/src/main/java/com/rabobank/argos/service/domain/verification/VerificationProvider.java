package com.rabobank.argos.service.domain.verification;

import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.domain.layout.MatchFilter;
import com.rabobank.argos.domain.layout.Step;
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.domain.link.Link;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import com.rabobank.argos.service.domain.link.LinkMetaBlockRepository;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class VerificationProvider {

    // private final SignatureValidator signatureValidator;
    // private final KeyPairRepository keyPairRepository;
    private final LinkMetaBlockRepository linkMetaBlockRepository;

    public VerificationRunResult verifyRun(LayoutMetaBlock layoutMetaBlock, List<Artifact> productsToVerify
    ) {
        //verifySignatures(layoutMetaBlock);
        List<LinkMetaBlock> links = getLinksForThisRun(layoutMetaBlock, productsToVerify);
        //VerifyRunStepsLinksRegistry verifyRunStepsLinksRegistry = createStepsLinksRegistry(layoutMetaBlock.getLayout().getSteps());
        return VerificationRunResult.builder().runIsValid(true).build();
    }

    private VerifyStepsLinksRegistry createStepsLinksRegistry(List<Step> steps) {
        return VerifyStepsLinksRegistryImpl.builder().build();
    }

    private List<LinkMetaBlock> getLinksForThisRun(LayoutMetaBlock layoutMetaBlock, List<Artifact> productsToVerify) {
        //layoutMetaBlock expectedEndProducts match rules apply

        Map<String, List<String>> hashRunIdMap = new HashMap<>();


        productsToVerify.forEach(artifact -> {
            matches(artifact, layoutMetaBlock.getLayout().getExpectedEndProducts()).forEach(matchFilter -> {
                hashRunIdMap.put(artifact.getHash(), linkMetaBlockRepository
                        .findBySupplyChainAndStepNameAndProductHash(layoutMetaBlock.getSupplyChainId(), matchFilter.getDestinationStepName(), artifact.getHash())
                        .stream().map(LinkMetaBlock::getLink).map(Link::getStepName).collect(Collectors.toList()));
            });
        });

        Set<String> runIds = hashRunIdMap.values().stream().flatMap(List::stream).collect(Collectors.toSet());

        TreeMap<Long, String> map = new TreeMap<>();

        runIds.forEach(runid -> {
            long count = hashRunIdMap.entrySet()
                    .stream()
                    .filter(entry -> entry.getValue().contains(runid))
                    .count();
            map.put(count, runid);
        });


        String value = map.descendingMap().firstEntry().getValue();


        return linkMetaBlockRepository.findByRunId(layoutMetaBlock.getSupplyChainId(), value);
    }


    private List<MatchFilter> matches(Artifact artifact, List<MatchFilter> matchFilters) {
        return matchFilters.stream().filter(matchFilter -> matchFilter.matchUri(artifact.getUri())).collect(Collectors.toList());
    }
/*
    private void verifySignatures(LayoutMetaBlock layoutMetaBlock) {

    }*/

    @Getter
    @Builder
    public static class VerificationRunResult {
        private boolean runIsValid = false;
    }


}
