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
package com.rabobank.argos.service.domain.verification.rules;

import com.rabobank.argos.domain.layout.rule.Rule;
import com.rabobank.argos.domain.layout.rule.RuleType;
import com.rabobank.argos.domain.link.Artifact;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
public class DeleteRuleVerification implements RuleVerification {
    @Override
    public RuleType getRuleType() {
        return RuleType.DELETE;
    }

    @Override
    public RuleVerificationResult verifyExpectedProducts(RuleVerificationContext<? extends Rule> context) {
        // deleteRule filteredMaterials must not be in filteredProducts
        // example pattern **/*.java not in filteredProducts but exists in filteredMaterials
        List<Artifact> filteredProducts = context.getFilteredProducts().collect(toList());
        List<Artifact> filteredMaterials = context.getFilteredMaterials().collect(toList());
        if (filteredProducts.isEmpty() && !filteredMaterials.isEmpty()) {
            return RuleVerificationResult.okay(Collections.emptySet());
        } else {
            return RuleVerificationResult.notOkay();
        }
    }

    @Override
    public RuleVerificationResult verifyExpectedMaterials(RuleVerificationContext<? extends Rule> context) {
        if (context.getFilteredMaterials().count() != 0) {
            return RuleVerificationResult.okay(Collections.emptySet());
        } else {
            return RuleVerificationResult.notOkay();
        }
    }
}
