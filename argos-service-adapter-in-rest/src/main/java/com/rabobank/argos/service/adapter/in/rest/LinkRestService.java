package com.rabobank.argos.service.adapter.in.rest;


import com.rabobank.argos.domain.LinkMetaBlockRepository;
import com.rabobank.argos.domain.SupplyChainRepository;
import com.rabobank.argos.domain.model.LinkMetaBlock;
import com.rabobank.argos.domain.model.SupplyChain;
import com.rabobank.argos.service.adapter.in.rest.api.handler.LinkApi;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestLinkMetaBlock;
import com.rabobank.argos.service.adapter.in.rest.mapper.LinkMetaBlockMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
public class LinkRestService implements LinkApi {

    private final LinkMetaBlockRepository linkMetaBlockRepository;

    private final SupplyChainRepository supplyChainRepository;

    private final LinkMetaBlockMapper converter;

    @Override
    public ResponseEntity<Void> createLink(String supplyChainId, @Valid RestLinkMetaBlock restLinkMetaBlock) {
        log.info("supplyChainId : {}", supplyChainId);

        supplyChainRepository.findBySupplyChainId(supplyChainId).orElseGet(() -> {
            supplyChainRepository.save(SupplyChain.builder().supplyChainId(supplyChainId).build());
            return null;
        });

        LinkMetaBlock linkMetaBlock = converter.convertFromRestLinkMetaBlock(restLinkMetaBlock);

        linkMetaBlock.setSupplyChainId(supplyChainId);

        linkMetaBlockRepository.save(linkMetaBlock);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<List<RestLinkMetaBlock>> findLink(String supplyChainId, String optionalHash) {
        supplyChainRepository.findBySupplyChainId(supplyChainId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "supply chain not found : " + supplyChainId)
        );

        return new ResponseEntity<>(Optional.ofNullable(optionalHash).map(hash -> linkMetaBlockRepository.findBySupplyChainAndSha(supplyChainId, hash))
                .orElseGet(() -> linkMetaBlockRepository.findBySupplyChainId(supplyChainId))
                .stream().map(converter::convertToRestLinkMetaBlock).collect(toList()), HttpStatus.OK);
    }

}
