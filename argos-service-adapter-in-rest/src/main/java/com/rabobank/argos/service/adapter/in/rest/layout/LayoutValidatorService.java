package com.rabobank.argos.service.adapter.in.rest.layout;

import com.rabobank.argos.domain.model.Layout;
import com.rabobank.argos.domain.model.LayoutMetaBlock;
import com.rabobank.argos.domain.repository.KeyPairRepository;
import com.rabobank.argos.domain.repository.SupplyChainRepository;
import com.rabobank.argos.service.adapter.in.rest.SignatureValidatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class LayoutValidatorService {

    private final SupplyChainRepository supplyChainRepository;

    private final SignatureValidatorService signatureValidatorService;

    private final KeyPairRepository keyPairRepository;

    public void validate(LayoutMetaBlock layoutMetaBlock) {

        if (!supplyChainRepository.exists(layoutMetaBlock.getSupplyChainId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "supply chain not found : " + layoutMetaBlock.getSupplyChainId());
        }

        Layout layout = layoutMetaBlock.getLayout();
        layout.getAuthorizedKeyIds().forEach(this::keyExists);
        layout.getSteps().forEach(step -> step.getAuthorizedKeyIds().forEach(this::keyExists));

        layoutMetaBlock.getSignatures().forEach(signature -> signatureValidatorService.validateSignature(layout, signature));
    }

    private void keyExists(String keyId) {
        if (!keyPairRepository.exists(keyId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "keyId " + keyId + " not found");
        }
    }
}
