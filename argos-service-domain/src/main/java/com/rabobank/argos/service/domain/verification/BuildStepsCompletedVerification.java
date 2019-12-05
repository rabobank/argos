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

import com.rabobank.argos.domain.link.Link;
import com.rabobank.argos.domain.link.LinkMetaBlock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static com.rabobank.argos.service.domain.verification.Verification.Priority.BUILDSTEPS_COMPLETED;
import static java.util.stream.Collectors.toSet;

@Component
@Slf4j
public class BuildStepsCompletedVerification implements Verification {
    @Override
    public Priority getPriority() {
        return BUILDSTEPS_COMPLETED;
    }

    @Override
    public VerificationRunResult verify(VerificationContext context) {

        Set<String> linkBuildSteps = context.getLinkMetaBlocks().stream().map(LinkMetaBlock::getLink).map(Link::getStepName)
                .collect(toSet());
        List<String> expectedSteps = context.getExpectedStepNames();
        log.info("linkBuildSteps: {} , expectedSteps: {}", linkBuildSteps, expectedSteps);

        return VerificationRunResult.builder().runIsValid(
                linkBuildSteps.size() == expectedSteps.size() && expectedSteps.containsAll(linkBuildSteps))
                .build();
    }
}
