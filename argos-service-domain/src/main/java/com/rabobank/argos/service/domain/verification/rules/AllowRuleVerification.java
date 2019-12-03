package com.rabobank.argos.service.domain.verification.rules;

import com.rabobank.argos.domain.layout.rule.Rule;
import com.rabobank.argos.domain.layout.rule.RuleType;
import org.springframework.stereotype.Component;

import static java.util.stream.Collectors.toSet;

@Component
public class AllowRuleVerification implements RuleVerification {
    @Override
    public RuleType getRuleType() {
        return RuleType.ALLOW;
    }

    @Override
    public RuleVerificationResult verifyExpectedProducts(RuleVerificationContext<? extends Rule> context) {
        return RuleVerificationResult.okay(context.getFilteredProducts().collect(toSet()));
    }

    @Override
    public RuleVerificationResult verifyExpectedMaterials(RuleVerificationContext<? extends Rule> context) {
        return RuleVerificationResult.okay(context.getFilteredMaterials().collect(toSet()));
    }
}
