package com.rabobank.argos.service.adapter.in.rest.link;


import com.rabobank.argos.domain.link.LinkMetaBlock;
import com.rabobank.argos.service.adapter.in.rest.SignatureValidatorService;
import com.rabobank.argos.service.adapter.in.rest.api.handler.LinkApi;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestLinkMetaBlock;
import com.rabobank.argos.service.domain.link.LinkMetaBlockRepository;
import com.rabobank.argos.service.domain.supplychain.SupplyChainRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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

    private final SignatureValidatorService signatureValidatorService;

    @Override
    public ResponseEntity<Void> createLink(String supplyChainId, RestLinkMetaBlock restLinkMetaBlock) {
        log.info("createLink supplyChainId : {}", supplyChainId);
        if (supplyChainRepository.findBySupplyChainId(supplyChainId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "supply chain not found : " + supplyChainId);
        }

        LinkMetaBlock linkMetaBlock = converter.convertFromRestLinkMetaBlock(restLinkMetaBlock);
        signatureValidatorService.validateSignature(linkMetaBlock.getLink(), linkMetaBlock.getSignature());
        linkMetaBlock.setSupplyChainId(supplyChainId);
        linkMetaBlockRepository.save(linkMetaBlock);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<List<RestLinkMetaBlock>> findLink(String supplyChainId, String optionalHash) {
        if (supplyChainRepository.findBySupplyChainId(supplyChainId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "supply chain not found : " + supplyChainId);
        }

        return new ResponseEntity<>(Optional.ofNullable(optionalHash).map(hash -> linkMetaBlockRepository.findBySupplyChainAndSha(supplyChainId, hash))
                .orElseGet(() -> linkMetaBlockRepository.findBySupplyChainId(supplyChainId))
                .stream().map(converter::convertToRestLinkMetaBlock).collect(toList()), HttpStatus.OK);
    }

}
