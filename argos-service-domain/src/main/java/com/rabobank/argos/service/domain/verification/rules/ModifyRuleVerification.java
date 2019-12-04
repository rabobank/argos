package com.rabobank.argos.service.domain.verification.rules;

import com.rabobank.argos.domain.layout.rule.Rule;
import com.rabobank.argos.domain.layout.rule.RuleType;
import com.rabobank.argos.domain.link.Artifact;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
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
                .filter(artifacts -> artifacts.size() != 2)
                .filter(artifacts -> artifacts.get(0).getHash().equals(artifacts.get(1).getHash()))
                .map(artifacts -> RuleVerificationResult.notOkay())
                .findFirst()
                .orElseGet(() -> RuleVerificationResult.okay(filteredProducts));
    }

    @Override
    public RuleVerificationResult verifyExpectedMaterials(RuleVerificationContext<? extends Rule> context) {
        log.warn("Modify Rule in expectedMaterials not allowed");
        return RuleVerificationResult.notOkay();
    }
}
