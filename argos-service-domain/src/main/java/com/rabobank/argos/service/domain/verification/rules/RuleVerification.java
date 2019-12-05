package com.rabobank.argos.service.domain.verification.rules;

import com.rabobank.argos.domain.layout.rule.Rule;
import com.rabobank.argos.domain.layout.rule.RuleType;

public interface RuleVerification {

    RuleType getRuleType();

    RuleVerificationResult verifyExpectedProducts(RuleVerificationContext<? extends Rule> context);

    RuleVerificationResult verifyExpectedMaterials(RuleVerificationContext<? extends Rule> context);

}
