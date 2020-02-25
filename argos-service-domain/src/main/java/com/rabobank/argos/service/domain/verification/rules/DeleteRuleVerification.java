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

import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j
public class DeleteRuleVerification implements RuleVerification {
    @Override
    public RuleType getRuleType() {
        return RuleType.DELETE;
    }

    @Override
    public Boolean verify(RuleVerificationContext<? extends Rule> context) {
        // deleteRule filteredMaterials must not be in filteredProducts
        // example pattern **/*.java not in filteredProducts but exists in filteredMaterials
        Set<Artifact> filteredArtifacts = context.getFilteredArtifacts();
        Set<Artifact> complement = new HashSet<>(context.getMaterials());
        complement.removeAll(context.getProducts());
        if (filteredArtifacts.stream().allMatch(complement::contains)) {
            context.consume(filteredArtifacts);
            logInfo(log, filteredArtifacts, getRuleType());
            return Boolean.TRUE;
        } else {
            logErrors(log, filteredArtifacts, getRuleType());
            return Boolean.FALSE;
        }
    }
}
