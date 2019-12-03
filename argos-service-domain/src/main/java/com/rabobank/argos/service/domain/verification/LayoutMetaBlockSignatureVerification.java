package com.rabobank.argos.service.domain.verification;

/*-
 * #%L
 * Argos Supply Chain Notary
 * %%
 * Copyright (C) 2019 Rabobank Nederland
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.domain.signing.SignatureValidator;
import com.rabobank.argos.service.domain.key.KeyPairRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.rabobank.argos.service.domain.verification.Verification.Priority.LAYOUT_METABLOCK_SIGNATURE;


@Component
@RequiredArgsConstructor
public class LayoutMetaBlockSignatureVerification implements Verification {

    private final SignatureValidator signatureValidator;

    private final KeyPairRepository keyPairRepository;

    @Override
    public Priority getPriority() {
        return LAYOUT_METABLOCK_SIGNATURE;
    }

    @Override
    public VerificationRunResult verify(VerificationContext context) {
        return verify(context.getLayoutMetaBlock());
    }

    private VerificationRunResult verify(LayoutMetaBlock layoutMetaBlock) {
        return VerificationRunResult.builder()
                .runIsValid(layoutMetaBlock
                        .getSignatures()
                        .stream()
                        .allMatch(signature -> keyPairRepository.findByKeyId(signature.getKeyId())
                                .map(
                                        keyPair -> signatureValidator
                                                .isValid(layoutMetaBlock.getLayout(), signature.getSignature(), keyPair
                                                        .getPublicKey()))
                                .orElse(false))
                )
                .build();

    }
}
