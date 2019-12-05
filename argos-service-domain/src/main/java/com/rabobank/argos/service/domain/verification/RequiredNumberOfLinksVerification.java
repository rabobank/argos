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

import static com.rabobank.argos.service.domain.verification.Verification.Priority.REQUIRED_NUMBER_OF_LINKS;

@Component
@Slf4j
public class RequiredNumberOfLinksVerification implements Verification {
    @Override
    public Priority getPriority() {
        return REQUIRED_NUMBER_OF_LINKS;
    }

    @Override
    public VerificationRunResult verify(VerificationContext context) {

        Optional<Step> failedRequiredNumberOfLinks = context
                .getLayoutMetaBlock()
                .getLayout()
                .getSteps()
                .stream()
                .filter(step -> stepDoesNotHaveRequiredNumberOfLinks(step, context))
                .findFirst();

        failedRequiredNumberOfLinks
                .ifPresent(step -> log.info("failed verification step:{}, requiredNumberOfLinks: {} , actual links: {}",
                        step.getStepName(),
                        step.getRequiredNumberOfLinks(),
                        context.getLinksByStepName(step.getStepName()).size())
                );

        return VerificationRunResult
                .builder()
                .runIsValid(failedRequiredNumberOfLinks.isEmpty()).build();
    }

    private static boolean stepDoesNotHaveRequiredNumberOfLinks(Step step, VerificationContext context) {
        return step.getRequiredNumberOfLinks() > context.getLinksByStepName(step.getStepName()).size();
    }
}
