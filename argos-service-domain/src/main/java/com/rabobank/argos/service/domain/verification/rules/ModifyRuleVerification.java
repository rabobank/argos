package com.rabobank.argos.service.domain.verification.rules;

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

import com.rabobank.argos.domain.layout.rule.Rule;
import com.rabobank.argos.domain.layout.rule.RuleType;
import com.rabobank.argos.domain.link.Artifact;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

@Component
@Slf4j
public class ModifyRuleVerification implements RuleVerification {
    @Override
    public RuleType getRuleType() {
        return RuleType.MODIFY;
    }

    @Override
    public RuleVerificationResult verifyExpectedProducts(RuleVerificationContext<? extends Rule> context) {
        Set<Artifact> filteredProducts = context.getFilteredProducts().collect(toSet());

        if (filteredProducts.isEmpty()) {
            return RuleVerificationResult.notOkay();
        }

        Map<String, List<Artifact>> uriMap = Stream.concat(filteredProducts.stream(), context.getFilteredMaterials().collect(toSet()).stream())
                .collect(groupingBy(Artifact::getUri));

        return uriMap.values().stream()
                .filter(filterNotValidArtifacts())
                .map(artifacts -> RuleVerificationResult.notOkay())
                .findFirst()
                .orElseGet(() -> RuleVerificationResult.okay(filteredProducts));
    }

    private Predicate<List<Artifact>> filterNotValidArtifacts() {
        return artifacts -> artifacts.size() != 2 || artifacts.get(0).getHash().equals(artifacts.get(1).getHash());
    }

    @Override
    public RuleVerificationResult verifyExpectedMaterials(RuleVerificationContext<? extends Rule> context) {
        log.warn("Modify Rule in expectedMaterials not allowed");
        return RuleVerificationResult.notOkay();
    }
}
