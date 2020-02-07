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
import com.rabobank.argos.domain.link.LinkMetaBlock;
import com.rabobank.argos.domain.signing.SignatureValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.PublicKey;
import java.util.Optional;

import static com.rabobank.argos.service.domain.verification.Verification.Priority.LINK_METABLOCK_SIGNATURE;
import static java.util.stream.Collectors.toList;


@Component
@RequiredArgsConstructor
@Slf4j
public class LinkMetaBlockSignatureVerification implements Verification {

    private final SignatureValidator signatureValidator;

    @Override
    public Priority getPriority() {
        return LINK_METABLOCK_SIGNATURE;
    }

    @Override
    public VerificationRunResult verify(VerificationContext context) {
        context.removeLinkMetaBlocks(context.getLinkMetaBlocks().stream()
                .filter(linkMetaBlock -> !okay(context.getLayoutMetaBlock(), linkMetaBlock)).collect(toList()));
        return VerificationRunResult.okay();
    }

    private boolean okay(LayoutMetaBlock layoutMetaBlock, LinkMetaBlock linkMetaBlock) {
        return getPublicKey(layoutMetaBlock, linkMetaBlock.getSignature().getKeyId())
                .map(keyPair -> signatureValidator.isValid(linkMetaBlock.getLink(),
                        linkMetaBlock.getSignature().getSignature(), keyPair))
                .orElse(false);
    }

    private Optional<PublicKey> getPublicKey(LayoutMetaBlock layoutMetaBlock, String keyId) {
        Optional<PublicKey> publicKeyOptional = layoutMetaBlock.getLayout().getKeys().stream()
                .filter(publicKey -> publicKey.getId().equals(keyId))
                .map(com.rabobank.argos.domain.layout.PublicKey::getKey).findFirst();
        if (publicKeyOptional.isEmpty()) {
            log.info("key with id: {} not found in layout", keyId);
        }
        return publicKeyOptional;
    }

}
