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
package com.rabobank.argos.service.domain.verification.rules;


import com.rabobank.argos.domain.link.Artifact;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

import static java.util.Collections.emptySet;

@Builder
@Getter
public class RuleVerificationResult {

    private boolean valid;

    private final Set<Artifact> validatedArtifacts;

    public static RuleVerificationResult okay(Set<Artifact> validatedArtifacts) {
        return RuleVerificationResult.builder().valid(true).validatedArtifacts(validatedArtifacts).build();
    }

    public static RuleVerificationResult notOkay() {
        return RuleVerificationResult.builder().valid(false).validatedArtifacts(emptySet()).build();
    }
}
