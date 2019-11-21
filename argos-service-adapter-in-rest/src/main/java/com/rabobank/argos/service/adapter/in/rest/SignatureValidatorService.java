package com.rabobank.argos.service.adapter.in.rest;

import com.rabobank.argos.domain.model.KeyPair;
import com.rabobank.argos.domain.model.Layout;
import com.rabobank.argos.domain.model.Link;
import com.rabobank.argos.domain.model.Signature;
import com.rabobank.argos.domain.repository.KeyPairRepository;
import com.rabobank.argos.domain.signing.SignatureValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class SignatureValidatorService {

    private final SignatureValidator signatureValidator;

    private final KeyPairRepository keyPairRepository;

    public void validateSignature(Layout layout, Signature signature) {
        if (!signatureValidator.isValid(layout, signature.getSignature(), getKeyPair(signature).getPublicKey())) {
            throwInValidSignatureException();
        }
    }

    public void validateSignature(Link link, Signature signature) {
        if (!signatureValidator.isValid(link, signature.getSignature(), getKeyPair(signature).getPublicKey())) {
            throwInValidSignatureException();
        }
    }

    private void throwInValidSignatureException() {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid signature");
    }

    private KeyPair getKeyPair(Signature signature) {
        return keyPairRepository.findByKeyId(signature.getKeyId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "signature with keyId " + signature.getKeyId() + " not found"));
    }

}
