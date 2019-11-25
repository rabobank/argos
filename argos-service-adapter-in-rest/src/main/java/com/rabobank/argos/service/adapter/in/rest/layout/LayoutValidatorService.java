package com.rabobank.argos.service.adapter.in.rest.layout;

import com.rabobank.argos.domain.model.Layout;
import com.rabobank.argos.domain.model.LayoutMetaBlock;
import com.rabobank.argos.domain.model.Signature;
import com.rabobank.argos.domain.repository.KeyPairRepository;
import com.rabobank.argos.domain.repository.SupplyChainRepository;
import com.rabobank.argos.service.adapter.in.rest.SignatureValidatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LayoutValidatorService {

    private final SupplyChainRepository supplyChainRepository;

    private final SignatureValidatorService signatureValidatorService;

    private final KeyPairRepository keyPairRepository;

    public void validate(LayoutMetaBlock layoutMetaBlock) {
        validateSupplyChain(layoutMetaBlock);
        validateAutorizationKeyIds(layoutMetaBlock.getLayout());
        validateSignatures(layoutMetaBlock);
    }

    private void validateSupplyChain(LayoutMetaBlock layoutMetaBlock) {
        if (!supplyChainRepository.exists(layoutMetaBlock.getSupplyChainId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "supply chain not found : " + layoutMetaBlock.getSupplyChainId());
        }
    }

    private void validateSignatures(LayoutMetaBlock layoutMetaBlock) {
        Set<String> uniqueKeyIds = layoutMetaBlock.getSignatures().stream().map(Signature::getKeyId).collect(Collectors.toSet());
        if (layoutMetaBlock.getSignatures().size() != uniqueKeyIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "layout can't be signed more than one time with the same keyId");
        }

        layoutMetaBlock.getSignatures().forEach(signature -> signatureValidatorService.validateSignature(layoutMetaBlock.getLayout(), signature));
    }

    private void validateAutorizationKeyIds(Layout layout) {
        layout.getAuthorizedKeyIds().forEach(this::keyExists);
        layout.getSteps().forEach(step -> step.getAuthorizedKeyIds().forEach(this::keyExists));
    }

    private void keyExists(String keyId) {
        if (!keyPairRepository.exists(keyId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "keyId " + keyId + " not found");
        }
    }
}
