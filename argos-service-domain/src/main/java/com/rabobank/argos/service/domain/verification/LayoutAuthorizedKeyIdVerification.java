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

import com.rabobank.argos.domain.Signature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.rabobank.argos.service.domain.verification.Verification.Priority.LAYOUT_AUTHORIZED_KEYID;

@Component
@Slf4j
public class LayoutAuthorizedKeyIdVerification implements Verification {

    @Override
    public Priority getPriority() {
        return LAYOUT_AUTHORIZED_KEYID;
    }

    @Override
    public VerificationRunResult verify(VerificationContext context) {
        Optional<Signature> failedLayoutAuthorizedKeyIdVerification = context.getLayoutMetaBlock().getSignatures()
                .stream()
                .filter(signature -> layoutWasNotSignedByAuthorizedFunctionary(context, signature))
                .findFirst();
        failedLayoutAuthorizedKeyIdVerification
                .ifPresent(signature ->
                        log.info("failed verification authorizedkeys: {} , signature key: {}",
                                context.getLayoutMetaBlock().getLayout().getAuthorizedKeyIds(),
                                signature.getKeyId()
                        )
                );
        return VerificationRunResult.builder().runIsValid(failedLayoutAuthorizedKeyIdVerification.isEmpty()).build();
    }

    private static boolean layoutWasNotSignedByAuthorizedFunctionary(VerificationContext context, Signature signature) {
        return !context.getLayoutMetaBlock().getLayout().getAuthorizedKeyIds().contains(signature.getKeyId());
    }
}
