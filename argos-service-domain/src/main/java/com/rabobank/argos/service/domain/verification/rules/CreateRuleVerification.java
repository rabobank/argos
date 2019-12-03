package com.rabobank.argos.service.domain.verification.rules;

import com.rabobank.argos.domain.layout.rule.Rule;
import com.rabobank.argos.domain.layout.rule.RuleType;
import com.rabobank.argos.domain.link.Artifact;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
@Slf4j
public class CreateRuleVerification implements RuleVerification {
    @Override
    public RuleType getRuleType() {
        return RuleType.CREATE;
    }

    @Override
    public RuleVerificationResult verifyExpectedProducts(RuleVerificationContext<? extends Rule> context) {
        List<Artifact> filteredProducts = context.getFilteredProducts().collect(toList());
        if (!filteredProducts.isEmpty() && context.notContainsMaterials(filteredProducts)) {
            return RuleVerificationResult.okay(new HashSet<>(filteredProducts));
        } else {
            return RuleVerificationResult.notOkay();
        }
    }

    @Override
    public RuleVerificationResult verifyExpectedMaterials(RuleVerificationContext<? extends Rule> context) {
        log.warn("AllowRule in expectedMaterials not allowed");
        return RuleVerificationResult.notOkay();
    }
}
