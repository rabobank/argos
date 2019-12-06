/*
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
package com.rabobank.argos.service.domain.verification;

import com.rabobank.argos.domain.link.LinkMetaBlock;
import com.rabobank.argos.domain.signing.SignatureValidator;
import com.rabobank.argos.service.domain.key.KeyPairRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.rabobank.argos.service.domain.verification.Verification.Priority.LINK_METABLOCK_SIGNATURE;
import static java.util.stream.Collectors.toList;


@Component
@RequiredArgsConstructor
public class LinkMetaBlockSignatureVerification implements Verification {

    private final SignatureValidator signatureValidator;

    private final KeyPairRepository keyPairRepository;

    @Override
    public Priority getPriority() {
        return LINK_METABLOCK_SIGNATURE;
    }

    @Override
    public VerificationRunResult verify(VerificationContext context) {
        context.removeLinkMetaBlocks(context.getLinkMetaBlocks().stream()
                .filter(linkMetaBlock -> !okay(linkMetaBlock)).collect(toList()));
        return VerificationRunResult.okay();
    }

    private boolean okay(LinkMetaBlock linkMetaBlock) {
        return keyPairRepository.findByKeyId(linkMetaBlock.getSignature().getKeyId())
                .map(keyPair -> signatureValidator.isValid(linkMetaBlock.getLink(),
                        linkMetaBlock.getSignature().getSignature(), keyPair.getPublicKey()))
                .orElse(false);
    }


}
