package com.rabobank.argos.service.domain.verification.rules;

import com.rabobank.argos.domain.layout.rule.AllowRule;
import com.rabobank.argos.domain.layout.rule.Rule;
import org.springframework.stereotype.Component;

import static java.util.stream.Collectors.toSet;

@Component
public class CreateRuleVerification implements RuleVerification {
    @Override
    public Class<? extends Rule> getRuleClass() {
        return AllowRule.class;
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
