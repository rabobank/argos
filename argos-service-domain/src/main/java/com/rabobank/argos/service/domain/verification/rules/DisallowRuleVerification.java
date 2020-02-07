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

import com.rabobank.argos.domain.layout.rule.Rule;
import com.rabobank.argos.domain.layout.rule.RuleType;
import com.rabobank.argos.domain.link.Artifact;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Slf4j
public class DisallowRuleVerification implements RuleVerification {
    @Override
    public RuleType getRuleType() {
        return RuleType.DISALLOW;
    }

    @Override
    public Boolean verify(RuleVerificationContext<? extends Rule> context) {
        Set<Artifact> filteredArtifacts = context.getFilteredArtifacts();
        if (filteredArtifacts.isEmpty()) {
            logResult(log, filteredArtifacts, getRuleType());
            return Boolean.TRUE;
        } else {
            logErrors(log, filteredArtifacts, getRuleType());
            return Boolean.FALSE;
        }
    }
}
