package com.rabobank.argos.service.adapter.in.rest.layout;

import com.rabobank.argos.domain.model.LayoutMetaBlock;
import com.rabobank.argos.domain.repository.LayoutMetaBlockRepository;
import com.rabobank.argos.domain.repository.SupplyChainRepository;
import com.rabobank.argos.service.adapter.in.rest.api.handler.LayoutApi;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestLayoutMetaBlock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
public class LayoutRestService implements LayoutApi {

    private final SupplyChainRepository supplyChainRepository;

    private final LayoutMetaBlockMapper converter;

    private final LayoutMetaBlockRepository repository;

    @Override
    public ResponseEntity<RestLayoutMetaBlock> createLayout(String supplyChainId, RestLayoutMetaBlock restLayoutMetaBlock) {
        log.info("createLayout for supplyChainId {}", supplyChainId);
        if (supplyChainRepository.findBySupplyChainId(supplyChainId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "supply chain not found : " + supplyChainId);
        }

        LayoutMetaBlock layoutMetaBlock = converter.convertFromRestLayoutMetaBlock(restLayoutMetaBlock);

        layoutMetaBlock.setSupplyChainId(supplyChainId);

        repository.save(layoutMetaBlock);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{layoutMetaBlockId}")
                .buildAndExpand(layoutMetaBlock.getLayoutMetaBlockId())
                .toUri();
        return ResponseEntity
                .created(location)
                .body(converter.convertToRestLayoutMetaBlock(layoutMetaBlock));
    }

    @Override
    public ResponseEntity<RestLayoutMetaBlock> getLayout(String supplyChainId, String layoutId) {
        return null;
    }

    @Override
    public ResponseEntity<List<RestLayoutMetaBlock>> findLayout(String supplyChainId) {
        return null;
    }
}
