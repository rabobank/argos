/*
 * Copyright (C) 2019 - 2020 Rabobank Nederland
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
package com.rabobank.argos.service.domain.verification;

import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.domain.signing.SignatureValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.PublicKey;
import java.util.Optional;

import static com.rabobank.argos.service.domain.verification.Verification.Priority.LAYOUT_METABLOCK_SIGNATURE;

@Component
@Slf4j
@RequiredArgsConstructor
public class LayoutMetaBlockSignatureVerification implements Verification {

    private final SignatureValidator signatureValidator;

    @Override
    public Priority getPriority() {
        return LAYOUT_METABLOCK_SIGNATURE;
    }

    @Override
    public VerificationRunResult verify(VerificationContext context) {
        return verify(context.getLayoutMetaBlock());
    }

    private VerificationRunResult verify(LayoutMetaBlock layoutMetaBlock) {
        boolean isValid = layoutMetaBlock
                .getSignatures()
                .stream()
                .allMatch(signature -> getPublicKey(layoutMetaBlock, signature.getKeyId())
                        .map(publicKey -> signatureValidator.isValid(layoutMetaBlock.getLayout(), signature.getSignature(), publicKey))
                        .orElse(false));
        if (!isValid) {
            log.info("failed LayoutMetaBlockSignatureVerification ");
        }
        return VerificationRunResult.builder()
                .runIsValid(isValid)
                .build();

    }

    private Optional<PublicKey> getPublicKey(LayoutMetaBlock layoutMetaBlock, String keyId) {
        return layoutMetaBlock.getLayout().getKeys().stream()
                .filter(publicKey -> publicKey.getId().equals(keyId))
                .map(com.rabobank.argos.domain.layout.PublicKey::getKey).findFirst();
    }
}
