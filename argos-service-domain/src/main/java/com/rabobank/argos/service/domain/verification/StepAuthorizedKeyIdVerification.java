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

import com.rabobank.argos.domain.layout.Step;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.rabobank.argos.service.domain.verification.Verification.Priority.STEP_AUTHORIZED_KEYID;

@Component
@Slf4j
public class StepAuthorizedKeyIdVerification implements Verification {

    @Override
    public Priority getPriority() {
        return STEP_AUTHORIZED_KEYID;
    }

    @Override
    public VerificationRunResult verify(VerificationContext context) {
        Optional<Step> failedStepAuthorizedKeyIdVerification = context.getLayoutMetaBlock().getLayout().getSteps()
                .stream()
                .filter(step -> linksWhereNotSignedByAuthorizedFuntionary(context, step))
                .findFirst();
        failedStepAuthorizedKeyIdVerification
                .ifPresent(step ->
                        log.info("failed verification step:{}, authorizedkeys: {} , link keys: {}",
                                step.getStepName(),
                                step.getAuthorizedKeyIds(),
                                context.getLinksByStepName(step.getStepName())
                        )
                );
        return VerificationRunResult.builder().runIsValid(failedStepAuthorizedKeyIdVerification.isEmpty()).build();
    }

    private static boolean linksWhereNotSignedByAuthorizedFuntionary(VerificationContext context, Step step) {
        return !context.getLinksByStepName(step.getStepName()).stream()
                .filter(link -> step.getAuthorizedKeyIds().contains(link.getSignature().getKeyId()))
                .findFirst().isPresent();

    }
}
