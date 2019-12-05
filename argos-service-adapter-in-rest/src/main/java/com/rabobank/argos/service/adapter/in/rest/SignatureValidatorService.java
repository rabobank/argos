/**
 * Copyright (C) 2019 Rabobank Nederland
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rabobank.argos.service.adapter.in.rest;

import com.rabobank.argos.domain.Signature;
import com.rabobank.argos.domain.key.KeyPair;
import com.rabobank.argos.domain.layout.Layout;
import com.rabobank.argos.domain.link.Link;
import com.rabobank.argos.domain.signing.SignatureValidator;
import com.rabobank.argos.service.domain.key.KeyPairRepository;
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
