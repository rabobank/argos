package com.rabobank.argos.service.adapter.in.rest.layout;

import com.rabobank.argos.domain.model.LayoutMetaBlock;
import com.rabobank.argos.domain.repository.LayoutMetaBlockRepository;
import com.rabobank.argos.domain.repository.SupplyChainRepository;
import com.rabobank.argos.service.adapter.in.rest.SignatureValidatorService;
import com.rabobank.argos.service.adapter.in.rest.api.handler.LayoutApi;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestLayoutMetaBlock;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestVerifyCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
public class LayoutRestService implements LayoutApi {

    private final SupplyChainRepository supplyChainRepository;

    private final LayoutMetaBlockMapper converter;

    private final LayoutMetaBlockRepository repository;

    private final SignatureValidatorService signatureValidatorService;

    @Override
    public ResponseEntity<RestLayoutMetaBlock> createLayout(String supplyChainId, RestLayoutMetaBlock restLayoutMetaBlock) {
        log.info("createLayout for supplyChainId {}", supplyChainId);
        if (supplyChainRepository.findBySupplyChainId(supplyChainId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "supply chain not found : " + supplyChainId);
        }


        LayoutMetaBlock layoutMetaBlock = converter.convertFromRestLayoutMetaBlock(restLayoutMetaBlock);
        layoutMetaBlock.getSignatures().forEach(signature -> signatureValidatorService.validateSignature(layoutMetaBlock.getLayout(), signature));


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
    public ResponseEntity<RestLayoutMetaBlock> updateLayout(String supplyChainId, String layoutId, RestLayoutMetaBlock restLayoutMetaBlock) {
        log.info("updateLayout for supplyChainId {}", supplyChainId);
        LayoutMetaBlock layoutMetaBlock = converter.convertFromRestLayoutMetaBlock(restLayoutMetaBlock);
        layoutMetaBlock.getSignatures().forEach(signature -> signatureValidatorService.validateSignature(layoutMetaBlock.getLayout(), signature));
        layoutMetaBlock.setSupplyChainId(supplyChainId);
        layoutMetaBlock.setLayoutMetaBlockId(layoutId);
        if (repository.update(supplyChainId, layoutId, layoutMetaBlock)) {
            return ResponseEntity.ok(converter.convertToRestLayoutMetaBlock(layoutMetaBlock));
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "layout not found");
        }
    }

    @Override
    public ResponseEntity<RestLayoutMetaBlock> getLayout(String supplyChainId, String layoutId) {
        return repository.findBySupplyChainAndId(supplyChainId, layoutId)
                .map(converter::convertToRestLayoutMetaBlock)
                .map(ResponseEntity::ok).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "layout not found"));
    }

    @Override
    public ResponseEntity<Boolean> performVerification(String supplyChainId, @Valid RestVerifyCommand restVerifyCommand) {
        return ResponseEntity.ok(true);
    }

    @Override
    public ResponseEntity<List<RestLayoutMetaBlock>> findLayout(String supplyChainId) {
        return ResponseEntity.ok(repository.findBySupplyChainId(supplyChainId).stream()
                .map(converter::convertToRestLayoutMetaBlock)
                .collect(Collectors.toList()));
    }
}
