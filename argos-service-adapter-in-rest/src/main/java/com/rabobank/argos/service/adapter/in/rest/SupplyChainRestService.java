package com.rabobank.argos.service.adapter.in.rest;

import com.rabobank.argos.domain.SupplyChainRepository;
import com.rabobank.argos.domain.model.SupplyChain;
import com.rabobank.argos.service.adapter.in.rest.api.handler.SupplychainApi;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestCreateSupplyChainCommand;
import com.rabobank.argos.service.adapter.in.rest.api.model.RestSupplyChainItem;
import com.rabobank.argos.service.adapter.in.rest.mapper.SupplyChainMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.UUID.randomUUID;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api")
public class SupplyChainRestService implements SupplychainApi {

    private final SupplyChainRepository supplyChainRepository;
    private final SupplyChainMapper converter;

    @Override
    public ResponseEntity<RestSupplyChainItem> createSupplyChain(@Valid RestCreateSupplyChainCommand restCreateSupplyChainCommand) {
        validateIsUnique(restCreateSupplyChainCommand);

        SupplyChain supplyChain = converter
                .convertFromRestSupplyChainCommand(restCreateSupplyChainCommand, () -> randomUUID().toString());

        supplyChainRepository.save(supplyChain);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{supplyChainId}")
                .buildAndExpand(supplyChain.getSupplyChainId())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(converter.convertToRestRestSupplyChainItem(supplyChain));
    }

    @Override
    public ResponseEntity<RestSupplyChainItem> getSupplyChain(String supplyChainId) {
        SupplyChain supplyChain = supplyChainRepository
                .findBySupplyChainId(supplyChainId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "supply chain not found : " + supplyChainId));
        return ResponseEntity.ok(converter.convertToRestRestSupplyChainItem(supplyChain));
    }

    @Override
    public ResponseEntity<List<RestSupplyChainItem>> searchSupplyChains(@Valid String name) {
        List<SupplyChain> supplyChains;
        if (StringUtils.isEmpty(name)) {
            supplyChains = supplyChainRepository.findAll();
        } else {
            supplyChains = supplyChainRepository.findByName(name);
        }
        List<RestSupplyChainItem> supplyChainItems = supplyChains
                .stream()
                .map(s -> converter.convertToRestRestSupplyChainItem(s))
                .collect(Collectors.toList());
        return ResponseEntity.ok(supplyChainItems);
    }

    private void validateIsUnique(RestCreateSupplyChainCommand restCreateSupplyChainCommand) {
        if (!supplyChainRepository.findByName(restCreateSupplyChainCommand.getName()).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "supply chain name must be unique");
        }
    }
}
